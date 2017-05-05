package eu.domibus.connector.plugin.ws;

import org.springframework.beans.factory.annotation.Autowired;

import connector.domibus.eu.domibusconnectorgatewayservice._1.AcknowledgementType;
import connector.domibus.eu.domibusconnectorgatewayservice._1.MessageErrorLogEntriesType;
import connector.domibus.eu.domibusconnectorgatewayservice._1.MessageType;
import connector.domibus.eu.domibusconnectorgatewayservice._1.MessagesType;
import connector.domibus.eu.domibusconnectorgatewayservice._1_0.DomibusConnectorGatewayServiceInterface;
import connector.domibus.eu.domibusconnectorgatewayservice._1_0.RequestPendingMessagesFault;
import connector.domibus.eu.domibusconnectorgatewayservice._1_0.SendMessageFault;
import eu.domibus.connector.plugin.connector.DomibusConnectorMessageFacade;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.messaging.MessagingProcessingException;

public class DomibusConnectorWebservice implements DomibusConnectorGatewayServiceInterface {



	@Autowired
	private DomibusConnectorMessageFacade messageFacade;

	@Override
	public MessagesType requestPendingMessages(Object requestPendingMessagesRequest)
			throws RequestPendingMessagesFault {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageErrorLogEntriesType getMessageErrors(String getMessageErrorsRequest) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcknowledgementType sendMessage(MessageType sendMessageRequest) throws SendMessageFault {

		String messageID = null;
		try {
			messageID = messageFacade.send(new DomibusConnectorMessage(sendMessageRequest));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		AcknowledgementType ack = new AcknowledgementType();
		if(messageID != null){
			ack.setMessageId(messageID);
			ack.setSuccess(true);
		}else{
			ack.setSuccess(false);
		}
		return ack;
	}

	@Override
	public String getMessageStatus(String getMessageStatusRequest) {
		// TODO Auto-generated method stub
		return null;
	}

	public DomibusConnectorMessageFacade getMessageFacade() {
		return messageFacade;
	}

	public void setMessageFacade(DomibusConnectorMessageFacade messageFacade) {
		this.messageFacade = messageFacade;
	}


}
