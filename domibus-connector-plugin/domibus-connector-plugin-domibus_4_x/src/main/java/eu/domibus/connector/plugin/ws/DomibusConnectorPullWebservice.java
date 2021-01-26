package eu.domibus.connector.plugin.ws;

import eu.domibus.common.NotificationType;
import eu.domibus.connector.domain.transition.DomibsConnectorAcknowledgementType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.domain.transition.ObjectFactory;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DomibusConnectorPullWebservice extends AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage> implements DomibusConnectorGatewayWebService {

    public static final String PLUGIN_NAME = "DC_PULL_PLUGIN";

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusConnectorPullWebservice.class);

    @Autowired
    private DomibusConnectorMessageSubmissionTransformer messageSubmissionTransformer;

    @Autowired
    private DomibusConnectorMessageRetrievalTransformer messageRetrievalTransformer;

    private static final ObjectFactory objectFactory = new ObjectFactory();

    public DomibusConnectorPullWebservice() {
        super(PLUGIN_NAME);
        this.requiredNotifications = Stream
                .of(NotificationType.MESSAGE_RECEIVED)
                .collect(Collectors.toList());
    }

    @Override
    public DomibsConnectorAcknowledgementType submitMessage(DomibusConnectorMessageType submitMessageRequest) {
        return new SubmitMessage(submitMessageRequest, this).invoke();
    }

    @Override
    public ListPendingMessageIdsResponse listPendingMessageIds(ListPendingMessageIdsRequest listPendingMessageIdsRequest) {
        Collection<String> messageIds = super.listPendingMessages();
        ListPendingMessageIdsResponse listPendingMessageIdsResponse = new ListPendingMessageIdsResponse();
        listPendingMessageIdsResponse.getMessageIds().addAll(messageIds);
        return listPendingMessageIdsResponse;
    }

    @Override
    public DomibusConnectorMessageType getMessageById(GetMessageByIdRequest getMessageByIdRequest) {
        String messageId = getMessageByIdRequest.getMessageId();
        DomibusConnectorMessageType messageType = objectFactory.createDomibusConnectorMessageType();
        DomibusConnectorMessage m = new DomibusConnectorMessage(messageType);
        try {
            DomibusConnectorMessage domibusConnectorMessage = this.downloadMessage(messageId, m);
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

    public static class SubmitMessage {
        private final AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage> backendConnector;
        private DomibusConnectorMessageType submitMessageRequest;

        public SubmitMessage(DomibusConnectorMessageType submitMessageRequest, AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage> backendConnector) {
            this.submitMessageRequest = submitMessageRequest;
            this.backendConnector = backendConnector;
        }

        public DomibsConnectorAcknowledgementType invoke() {
            String messageID = null;
            DomibsConnectorAcknowledgementType ack = new DomibsConnectorAcknowledgementType();
            try {
                LOGGER.debug("#submitMessage: call submit of parent class");
                messageID = backendConnector.submit(new DomibusConnectorMessage(submitMessageRequest));
            } catch (Exception e) {
                ack.setResult(false);
                ack.setResultMessage(e.getMessage());
                LOGGER.error("#submitMessage: Error occured while calling submit, setting submit result to false", e);
            }

            LOGGER.info(String.format("#submitMessage: messageID is [%s]", messageID));
            if (messageID != null) {
                LOGGER.debug(String.format("#submitMessage: Message successfully submitted"));
                ack.setMessageId(messageID);
                ack.setResult(true);
            } else {
                LOGGER.error("#submitMessage: submit message failed, returning ack with result false");
                ack.setResult(false);
            }
            return ack;
        }
    }
}
