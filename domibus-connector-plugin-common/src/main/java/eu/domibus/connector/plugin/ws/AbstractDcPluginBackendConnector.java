package eu.domibus.connector.plugin.ws;

import eu.domibus.common.*;
import eu.domibus.connector.domain.transition.ObjectFactory;
import eu.domibus.connector.plugin.config.property.AbstractDCPluginPropertyManager;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.connector.plugin.transformer.DCMessageTransformer;
import eu.domibus.connector.plugin.transformer.DomibusConnectorMessageRetrievalTransformer;
import eu.domibus.connector.plugin.transformer.DomibusConnectorMessageSubmissionTransformer;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.PluginMessageListenerContainer;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.initialize.PluginInitializer;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractDcPluginBackendConnector extends AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage> {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(AbstractDcPluginBackendConnector.class);

    private final DomibusConnectorMessageSubmissionTransformer messageSubmissionTransformer;

    private final DomibusConnectorMessageRetrievalTransformer messageRetrievalTransformer;

    protected static final ObjectFactory objectFactory = new ObjectFactory();
    private final AbstractDCPluginPropertyManager propertyManager;
    private final PluginInitializer pluginInitializer;

    public AbstractDcPluginBackendConnector(String pluginName, AbstractDCPluginPropertyManager propertyManager,
                                            DCMessageTransformer messageTransformer,
                                            PluginInitializer pluginInitializer) {
        super(pluginName);
        this.messageSubmissionTransformer = messageTransformer.getMessageSubmissionTransformer();
        this.messageRetrievalTransformer = messageTransformer.getMessageRetrievalTransformer();
        this.propertyManager = propertyManager;
        this.pluginInitializer = pluginInitializer;
        this.requiredNotifications = Stream
                .of(NotificationType.MESSAGE_RECEIVED)
                .collect(Collectors.toList());
    }

    @Override
    public PluginInitializer getPluginInitializer() {
        return this.pluginInitializer;
    }
    @Override
    public DomibusPropertyManagerExt getPropertyManager() {
        return this.propertyManager;
    }

    @Override
    public boolean isEnabled(String domainCode) {
        return doIsEnabled(domainCode);
    }

    @Override
    public void setEnabled(String domainCode, boolean enabled) {
        doSetEnabled(domainCode, enabled);
    }

    @Override
    public String getDomainEnabledPropertyName() {
        return propertyManager.getDomainEnabledPropertyName();
    }


    @Override
    public MessageRetrievalTransformer<DomibusConnectorMessage> getMessageRetrievalTransformer() {
        return this.messageRetrievalTransformer;
    }

    @Override
    public MessageSubmissionTransformer<DomibusConnectorMessage> getMessageSubmissionTransformer() {
        return this.messageSubmissionTransformer;
    }

    @Override
    public void messageSendSuccess(MessageSendSuccessEvent messageId) {
        //just ignore...
        LOGGER.debug(String.format("Message with ID {%s} successfully Sent", messageId));
    }

    @Override
    public void messageSendFailed(MessageSendFailedEvent messageId) {
        //just ignore...
        LOGGER.error(String.format("Send message with messageId [%s] failed", messageId));
    }

    @Override
    public void messageReceiveFailed(MessageReceiveFailureEvent receiveFailureEvent) {
        LOGGER.error(String.format("Message receiveFailed: messageId: [%s] on endpoint [%s] with ErrorResult [%s]",
                receiveFailureEvent.getMessageId(),
                receiveFailureEvent.getEndpoint(),
                receiveFailureEvent.getErrorResult()
        ));
    }

}
