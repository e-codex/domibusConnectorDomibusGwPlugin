package eu.domibus.connector.plugin.service;

import org.springframework.beans.factory.annotation.Autowired;

import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessageSubmitter;

public class MessageSubmissionService {

	@Autowired
    private MessageSubmitter<Submission> messageSubmitter = null;
	
	public final String submit(Submission submission, String name) throws Exception {
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
