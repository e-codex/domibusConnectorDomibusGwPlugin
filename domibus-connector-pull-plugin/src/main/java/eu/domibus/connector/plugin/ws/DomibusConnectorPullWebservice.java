package eu.domibus.connector.plugin.ws;

import eu.domibus.common.DeliverMessageEvent;
import eu.domibus.connector.domain.transition.DomibsConnectorAcknowledgementType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.plugin.config.property.AbstractDCPluginPropertyManager;
import eu.domibus.connector.plugin.config.property.DCPullPluginPropertyManager;
import eu.domibus.connector.plugin.dao.DCMessageLogDao;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.connector.plugin.entity.DCMessageLogEntity;
import eu.domibus.connector.plugin.transformer.DCMessageTransformer;
import eu.domibus.connector.ws.gateway.webservice.DomibusConnectorGatewayWebService;
import eu.domibus.connector.ws.gateway.webservice.GetMessageByIdRequest;
import eu.domibus.connector.ws.gateway.webservice.ListPendingMessageIdsRequest;
import eu.domibus.connector.ws.gateway.webservice.ListPendingMessageIdsResponse;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.initialize.PluginInitializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.connector.plugin.config.DCPullPluginConfiguration.MODULE_NAME;
import static eu.domibus.connector.plugin.config.DCPullPluginConfiguration.PULL_PLUGIN_INITIALIZER;

public class DomibusConnectorPullWebservice extends AbstractDcPluginBackendConnector implements DomibusConnectorGatewayWebService {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusConnectorPullWebservice.class);


    private final DCMessageLogDao dcMessageLogDao;

    private final DCPullPluginPropertyManager wsPluginPropertyManager;

    private final  DomibusConfigurationExtService domibusConfigurationExtService;

    private final DomainContextExtService domainContextExtService;


    public DomibusConnectorPullWebservice(DCMessageTransformer messageTransformer,
                                          DCMessageLogDao dcMessageLogDao,
                                          DCPullPluginPropertyManager wsPluginPropertyManager,
                                          DomibusConfigurationExtService domibusConfigurationExtService,
                                          DomainContextExtService domainContextExtService,
                                          @Qualifier(PULL_PLUGIN_INITIALIZER) PluginInitializer pluginInitializer
                                          ) {
        super(MODULE_NAME, wsPluginPropertyManager, messageTransformer, pluginInitializer);
        this.dcMessageLogDao = dcMessageLogDao;
        this.wsPluginPropertyManager = wsPluginPropertyManager;
        this.domibusConfigurationExtService = domibusConfigurationExtService;
        this.domainContextExtService = domainContextExtService;
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

        String domainCode = "";
        if (domibusConfigurationExtService.isMultiTenantAware()) {
            DomainDTO currentDomainSafely = domainContextExtService.getCurrentDomainSafely();
            domainCode = currentDomainSafely.getCode();
        }

        DCMessageLogEntity dcMessageLogEntity = new DCMessageLogEntity(messageId, new Date(), domainCode);
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


}
