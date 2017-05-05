package eu.domibus.connector.plugin.connector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.connector.plugin.service.MessageSubmissionService;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

@Component
public class DomibusConnectorMessageFacade extends AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage>{

	@Autowired
	private MessageSubmissionService submissionService;
	
	public DomibusConnectorMessageFacade(String name) {
		super(name);
	}
	
	public String send(DomibusConnectorMessage connectorMessage) throws Exception{
		return submissionService.submit(connectorMessage, this.getName());
	}

	@Override
	public void messageSendFailed(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public MessageRetrievalTransformer<DomibusConnectorMessage> getMessageRetrievalTransformer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageSubmissionTransformer<DomibusConnectorMessage> getMessageSubmissionTransformer() {
		// TODO Auto-generated method stub
		return null;
	}
	

	public MessageSubmissionService getSubmissionService() {
		return submissionService;
	}

	public void setSubmissionService(MessageSubmissionService submissionService) {
		this.submissionService = submissionService;
	}

}
