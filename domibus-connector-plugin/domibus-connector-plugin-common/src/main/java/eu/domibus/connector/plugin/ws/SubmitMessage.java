package eu.domibus.connector.plugin.ws;

import eu.domibus.connector.domain.transition.DomibsConnectorAcknowledgementType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.AbstractBackendConnector;

public class SubmitMessage {
    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(SubmitMessage.class);

    private final AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage> backendConnector;

    private DomibusConnectorMessageType submitMessageRequest;

    public SubmitMessage(DomibusConnectorMessageType submitMessageRequest, AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage> backendConnector) {
        this.submitMessageRequest = submitMessageRequest;
        this.backendConnector = backendConnector;
    }

    public DomibsConnectorAcknowledgementType invoke() {
        String messageID = null;
        DomibsConnectorAcknowledgementType ack = new DomibsConnectorAcknowledgementType();
        ack.setResult(true);
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
            LOGGER.debug(String.format("#submitMessage: Message successfully submitted [%s]", messageID));
            ack.setMessageId(messageID);
            ack.setResult(true);
        } else {
            LOGGER.error(String.format("#submitMessage: submit message failed, returning ack with result false [%s]", messageID));
            ack.setResult(false);
        }
        return ack;
    }
}
