package eu.domibus.connector.plugin.connector;

import java.util.Collection;
import java.util.List;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.MessageStatus;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

public class DomibusConnectorEndpoint extends AbstractBackendConnector<DomibusConnectorMessage, DomibusConnectorMessage> {

	

	public DomibusConnectorEndpoint(String name) {
		super(name);
		// TODO Auto-generated constructor stub
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

	@Override
	public void messageSendFailed(String arg0) {
		// TODO Auto-generated method stub
		
	}

}
