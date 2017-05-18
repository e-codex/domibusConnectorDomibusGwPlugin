package eu.domibus.connector.plugin.domain;

import connector.domibus.eu.domibusconnectorgatewayservice._1.MessageType;

public class DomibusConnectorMessage {

	public static final String NAME_KEY = "name";
	public static final String MIME_TYPE_KEY = "MimeType";
	public static final String DESCRIPTION_KEY = "description";
	public static final String IDENTIFIER_KEY = "identifier";
	public static final String MESSAGE_CONTENT_VALUE = "messageContent";
	
	private MessageType connectorMessage = null;
	

	public DomibusConnectorMessage(MessageType connectorMessage) {
		super();
		this.connectorMessage = connectorMessage;
	}

	public MessageType getConnectorMessage() {
		return connectorMessage;
	}

	public void setConnectorMessage(MessageType connectorMessage) {
		this.connectorMessage = connectorMessage;
	}
	
	
	
	
}
