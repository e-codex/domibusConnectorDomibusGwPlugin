package eu.domibus.connector.plugin.ws;

import eu.domibus.common.NotificationType;
import eu.domibus.connector.domain.transition.DomibsConnectorAcknowledgementType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.domain.transition.ObjectFactory;
import eu.domibus.connector.plugin.config.DCPluginConfiguration;
import eu.domibus.connector.plugin.config.property.DCPluginPropertyManager;
import eu.domibus.connector.plugin.dao.DCMessageLogDao;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.connector.plugin.entity.DCMessageLogEntity;
import eu.domibus.connector.plugin.transformer.DomibusConnectorMessageRetrievalTransformer;
import eu.domibus.connector.plugin.transformer.DomibusConnectorMessageSubmissionTransformer;
import eu.domibus.connector.ws.gateway.webservice.DomibusConnectorGatewayWebService;
import eu.domibus.connector.ws.gateway.webservice.GetMessageByIdRequest;
import eu.domibus.connector.ws.gateway.webservice.ListPendingMessageIdsRequest;
import eu.domibus.connector.ws.gateway.webservice.ListPendingMessageIdsResponse;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import eu.domibus.plugin.webService.entity.WSMessageLogEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DomibusConnectorPullWebservice extends AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage> implements DomibusConnectorGatewayWebService {

    public static final String PLUGIN_NAME = "DC_PULL_PLUGIN";

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusConnectorPullWebservice.class);

    @Autowired
    private DomibusConnectorMessageSubmissionTransformer messageSubmissionTransformer;

    @Autowired
    private DomibusConnectorMessageRetrievalTransformer messageRetrievalTransformer;

    @Autowired
    DCMessageLogDao dcMessageLogDao;

    @Autowired
    DCPluginPropertyManager wsPluginPropertyManager;


    private static final ObjectFactory objectFactory = new ObjectFactory();

    public DomibusConnectorPullWebservice() {
        super(PLUGIN_NAME);
        this.requiredNotifications = Stream
                .of(NotificationType.MESSAGE_RECEIVED)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DomibsConnectorAcknowledgementType submitMessage(DomibusConnectorMessageType submitMessageRequest) {
        return new SubmitMessage(submitMessageRequest, this).invoke();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ListPendingMessageIdsResponse listPendingMessageIds(ListPendingMessageIdsRequest listPendingMessageIdsRequest) {

        Integer knownIntegerPropertyValue = wsPluginPropertyManager.getKnownIntegerPropertyValue(DCPluginConfiguration.DC_PLUGIN_MAX_MESSAGE_LIST);
        List<WSMessageLogEntity> all = dcMessageLogDao.findAll(knownIntegerPropertyValue);
        List<String> pendingMessageIds = all.stream().map(e -> e.getMessageId()).collect(Collectors.toList());

        ListPendingMessageIdsResponse listPendingMessageIdsResponse = new ListPendingMessageIdsResponse();
        listPendingMessageIdsResponse.getMessageIds().addAll(pendingMessageIds);
        return listPendingMessageIdsResponse;

    }

    @Override
    @Transactional
    public void deliverMessage(final String messageId) {
        LOGGER.debug("Download message " + messageId + " from Queue.");
        DCMessageLogEntity dcMessageLogEntity = new DCMessageLogEntity(messageId, new Date());
        dcMessageLogDao.create(dcMessageLogEntity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DomibusConnectorMessageType getMessageById(GetMessageByIdRequest getMessageByIdRequest) {
        String messageId = getMessageByIdRequest.getMessageId();
        DomibusConnectorMessageType messageType = objectFactory.createDomibusConnectorMessageType();
        DomibusConnectorMessage m = new DomibusConnectorMessage(messageType);
        try {
            DomibusConnectorMessage domibusConnectorMessage = this.downloadMessage(messageId, m);
            dcMessageLogDao.deleteByMessageId(messageId);
            return domibusConnectorMessage.getConnectorMessage();
        } catch (MessageNotFoundException e) {
            LOGGER.warn("Message could not found", e);
            throw new RuntimeException("Message could not be found!");
        }
    }


    @Override
    public MessageSubmissionTransformer<DomibusConnectorMessage> getMessageSubmissionTransformer() {
        return this.messageSubmissionTransformer;
    }

    @Override
    public MessageRetrievalTransformer<DomibusConnectorMessage> getMessageRetrievalTransformer() {
        return this.messageRetrievalTransformer;
    }

    @Override
    public void messageSendFailed(String s) {
        LOGGER.warn("Message send failed [{}]", s);
    }

    public BackendConnector.Mode getMode() {
        return Mode.PULL;
    }

}
