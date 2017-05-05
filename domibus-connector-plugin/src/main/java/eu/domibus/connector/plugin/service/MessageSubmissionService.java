package eu.domibus.connector.plugin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import connector.domibus.eu.domibusconnectorgatewayservice._1.MessageType;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.connector.plugin.transformer.DomibusConnectorMessageSubmissionTransformer;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessageSubmitter;

@Component
public class MessageSubmissionService {

	@Autowired
    private MessageSubmitter<Submission> messageSubmitter = null;
	
	@Autowired
	private DomibusConnectorMessageSubmissionTransformer messageTransformer;
	
	public final String submit(DomibusConnectorMessage connectorMessage, String name) throws Exception {
        Submission submission = messageTransformer.transformToSubmission(connectorMessage);
		
		return messageSubmitter.submit(submission, name);
    }

    /* ---- Getters and Setters ---- */

    public MessageSubmitter<Submission> getMessageSubmitter() {
        return messageSubmitter;
    }

    public void setMessageSubmitter(MessageSubmitter<Submission> messageSubmitter) {
        this.messageSubmitter = messageSubmitter;
    }
}
