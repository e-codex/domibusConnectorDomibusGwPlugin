package eu.domibus.connector.plugin.domain;

import connector.domibus.eu.domibusconnectorgatewayservice._1.MessageType;

public class DomibusConnectorMessage {

	
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
