package eu.domibus.connector.plugin.domain;

import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;

public class DomibusConnectorMessage {

	public static final String NAME_KEY = "name";
	public static final String MIME_TYPE_KEY = "MimeType";
	public static final String DESCRIPTION_KEY = "description";
	public static final String IDENTIFIER_KEY = "identifier";
	public static final String MESSAGE_CONTENT_VALUE = "messageContent";
	
	public static final String ORIGINAL_SENDER_PROPERTY_NAME = "originalSender";
	public static final String FINAL_RECIPIENT_PROPERTY_NAME = "finalRecipient";
	public static final String ORIGINAL_MESSAGE_ID = "originalMessageId";
	public static final String XML_MIME_TYPE = "text/xml";
	public static final String APPLICATION_MIME_TYPE = "application/octet-stream";
	public static final String CONTENT_PDF_NAME = "ContentPDF";
	public static final String CONTENT_XML_NAME = "ContentXML";


	private DomibusConnectorMessageType connectorMessage = null;
	

	public DomibusConnectorMessage(DomibusConnectorMessageType connectorMessage) {
		super();
		this.connectorMessage = connectorMessage;
	}

	public DomibusConnectorMessageType getConnectorMessage() {
		return connectorMessage;
	}

	public void setConnectorMessage(DomibusConnectorMessageType connectorMessage) {
		this.connectorMessage = connectorMessage;
	}
	
	
	
	
}
