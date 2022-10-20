package eu.domibus.connector.plugin.ws;

import eu.domibus.common.*;
import eu.domibus.connector.domain.transition.ObjectFactory;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.connector.plugin.transformer.DomibusConnectorMessageRetrievalTransformer;
import eu.domibus.connector.plugin.transformer.DomibusConnectorMessageSubmissionTransformer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractDcPluginBackendConnector extends AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage> {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(AbstractDcPluginBackendConnector.class);

    public AbstractDcPluginBackendConnector(String pluginName) {
        super(pluginName);
        this.requiredNotifications = Stream
                .of(NotificationType.MESSAGE_RECEIVED)
                .collect(Collectors.toList());
    }

    @Autowired
    private DomibusConnectorMessageSubmissionTransformer messageSubmissionTransformer;

    @Autowired
    private DomibusConnectorMessageRetrievalTransformer messageRetrievalTransformer;

    protected static final ObjectFactory objectFactory = new ObjectFactory();


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
