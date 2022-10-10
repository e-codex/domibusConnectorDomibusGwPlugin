package eu.domibus.connector.plugin.ws;

import eu.domibus.common.DeliverMessageEvent;
import eu.domibus.connector.domain.transition.DomibsConnectorAcknowledgementType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.plugin.config.property.AbstractDCPluginPropertyManager;
import eu.domibus.connector.plugin.dao.DCMessageLogDao;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.connector.plugin.entity.DCMessageLogEntity;
import eu.domibus.connector.ws.gateway.webservice.DomibusConnectorGatewayWebService;
import eu.domibus.connector.ws.gateway.webservice.GetMessageByIdRequest;
import eu.domibus.connector.ws.gateway.webservice.ListPendingMessageIdsRequest;
import eu.domibus.connector.ws.gateway.webservice.ListPendingMessageIdsResponse;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DomibusConnectorPullWebservice extends AbstractDcPluginBackendConnector implements DomibusConnectorGatewayWebService {

    public static final String PLUGIN_NAME = "DC_PULL_PLUGIN";

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusConnectorPullWebservice.class);

    @Autowired
    DCMessageLogDao dcMessageLogDao;

    @Autowired
    AbstractDCPluginPropertyManager wsPluginPropertyManager;

    public DomibusConnectorPullWebservice() {
        super(PLUGIN_NAME);
    }


    @Override
    @Transactional
    public DomibsConnectorAcknowledgementType submitMessage(DomibusConnectorMessageType submitMessageRequest) {
        return new SubmitMessage(submitMessageRequest, this).invoke();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ListPendingMessageIdsResponse listPendingMessageIds(ListPendingMessageIdsRequest listPendingMessageIdsRequest) {

        Integer knownIntegerPropertyValue = wsPluginPropertyManager.getKnownIntegerPropertyValue(AbstractDCPluginPropertyManager.DC_PLUGIN_MAX_MESSAGE_LIST);
        List<DCMessageLogEntity> all = dcMessageLogDao.findAll(knownIntegerPropertyValue);
        List<String> pendingMessageIds = all.stream().map(e -> e.getMessageId()).collect(Collectors.toList());

        ListPendingMessageIdsResponse listPendingMessageIdsResponse = new ListPendingMessageIdsResponse();
        listPendingMessageIdsResponse.getMessageIds().addAll(pendingMessageIds);
        return listPendingMessageIdsResponse;

    }

    @Override
    @Transactional
    public void deliverMessage(final DeliverMessageEvent event) {
        String messageId = event.getMessageId();
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

//
//    @Override
//    public MessageSubmissionTransformer<DomibusConnectorMessage> getMessageSubmissionTransformer() {
//        return this.messageSubmissionTransformer;
//    }
//
//    @Override
//    public MessageRetrievalTransformer<DomibusConnectorMessage> getMessageRetrievalTransformer() {
//        return this.messageRetrievalTransformer;
//    }
//
//    @Override
//    public void messageSendFailed(MessageSendFailedEvent event) {
//        LOGGER.warn("Message send failed [{}]", event.getMessageId());
//    }



}
