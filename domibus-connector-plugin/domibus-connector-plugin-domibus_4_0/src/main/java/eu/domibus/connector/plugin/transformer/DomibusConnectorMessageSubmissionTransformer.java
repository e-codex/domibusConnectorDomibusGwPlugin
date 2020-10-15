package eu.domibus.connector.plugin.transformer;

import static eu.domibus.connector.plugin.domain.DomibusConnectorMessage.XML_MIME_TYPE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import eu.domibus.connector.domain.transition.DomibusConnectorMessageAttachmentType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageContentType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageDetailsType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.domain.transition.DomibusConnectorPartyType;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.Submission.TypedProperty;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

@Component
public class DomibusConnectorMessageSubmissionTransformer implements MessageSubmissionTransformer<DomibusConnectorMessage> {

	private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusConnectorMessageSubmissionTransformer.class);
	
	@Override
	public Submission transformToSubmission(DomibusConnectorMessage connectorMessage) {
		
		if(connectorMessage.getConnectorMessage()==null) {
			LOGGER.error("DomibusConnectorMessage is null");
			return null;
		}
		

		DomibusConnectorMessageType message = connectorMessage.getConnectorMessage();
		
		LOGGER.debug(String.format("Starting transformation of DomibusConnectorMessage [%s] object to Submission object", connectorMessage));

		
		Submission submission = new Submission();

		transformMessageDetails(submission, message);

		transformMessageContent(submission, message);

		transformMessageAttachments(submission, message);

		transformMessageConfirmations(submission, message);
		
		LOGGER.debug("Successfully transformed DomibusConnectorMessage object to Submission object");
		
		return submission;
	}

	void transformMessageAttachments(Submission submission, DomibusConnectorMessageType message) {
		List<DomibusConnectorMessageAttachmentType> messageAttachments = message.getMessageAttachments();
		if(!CollectionUtils.isEmpty(messageAttachments)){
			for(DomibusConnectorMessageAttachmentType attachment: messageAttachments){
				String contentId = generateCID();

				Collection<TypedProperty> payloadProperties = new ArrayList<TypedProperty>();
				payloadProperties.add(new TypedProperty(DomibusConnectorMessage.NAME_KEY, attachment.getName()));
				payloadProperties.add(new TypedProperty(DomibusConnectorMessage.MIME_TYPE_KEY,attachment.getMimeType()));
				String attachmentDescription = attachment.getIdentifier()!=null?attachment.getIdentifier():attachment.getName();
				payloadProperties.add(new TypedProperty(DomibusConnectorMessage.DESCRIPTION_KEY,attachmentDescription));
				LOGGER.debug("Adding attachment [{}] as payload with payloadProperties", attachment, payloadProperties);
				submission.addPayload(contentId, attachment.getAttachment(), payloadProperties);
			}
		}
	}

	void transformMessageContent(Submission submission, DomibusConnectorMessageType message) {
		DomibusConnectorMessageContentType messageContent = message.getMessageContent();

		if (messageContent == null && message.getMessageConfirmations().size() > 0) {
			LOGGER.debug("#transformMessageContent: message Content is null and contains at least one confirmation: Message is a confirmation message!");
		} else {
            LOGGER.debug("#transformMessageContent: message is a business message!");
            String contentId = generateCID();

            Collection<TypedProperty> payloadProperties = new ArrayList<TypedProperty>();
            payloadProperties.add(new TypedProperty(DomibusConnectorMessage.NAME_KEY, DomibusConnectorMessage.MESSAGE_CONTENT_VALUE));
            payloadProperties.add(new TypedProperty(DomibusConnectorMessage.MIME_TYPE_KEY, DomibusConnectorMessage.XML_MIME_TYPE));
            payloadProperties.add(new TypedProperty(DomibusConnectorMessage.DESCRIPTION_KEY, DomibusConnectorMessage.MESSAGE_CONTENT_VALUE));

            DataHandler dataHandler = convertXmlSourceToDataHandler(messageContent.getXmlContent());

            submission.addPayload(contentId, dataHandler, payloadProperties);
        }
	}
	
	/**
     * takes a source element and converts with 
     * Transformer to an byte[] backed by ByteArrayOutputStream
     * @param xmlInput - the Source
     * @throws RuntimeException - in case of any error! //TODO: improve exceptions
     * @return the byte[]
     */
    static byte[] convertXmlSourceToByteArray(Source xmlInput) {
        try {
        	Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//            ByteArrayOutputStream output = new ByteArrayOutputStream();
            StreamResult xmlOutput=new StreamResult(new ByteArrayOutputStream());
//            StreamResult xmlOutput = new StreamResult(new OutputStreamWriter(output));
            transformer.transform(xmlInput, xmlOutput);
//            byte[] result = output.toByteArray();
//            result = new String(result, "UTF-8").getBytes("UTF-8");
           
			return xmlOutput.getOutputStream().toString().getBytes("UTF-8");
        } catch (IllegalArgumentException | TransformerException | UnsupportedEncodingException e) {
            throw new RuntimeException("Exception occured during transforming xml into byte[]", e);
        }
    }

	private DataHandler convertXmlSourceToDataHandler(Source xmlSource) {
//		byte[] xmlContent = convertXmlSourceToByteArray(xmlSource);
//		if(LOGGER.isDebugEnabled()) {
//        	LOGGER.debug("Business content XML before transformed to data handler: {}", new String(xmlContent));
//        }
//		DataHandler dataHandler = new DataHandler(new StreamSource(new ByteArrayInputStream(xmlContent)), DomibusConnectorMessage.XML_MIME_TYPE);
		DataHandler dataHandler = new DataHandler(xmlSource, DomibusConnectorMessage.XML_MIME_TYPE);
		return dataHandler;
	}

	void transformMessageDetails(Submission submission, DomibusConnectorMessageType message) {
		DomibusConnectorMessageDetailsType messageDetails = message.getMessageDetails();
		submission.setRefToMessageId(messageDetails.getRefToMessageId());
		transformParties(submission, messageDetails);
		transformCollaborationInfo(submission, messageDetails);
		transformMessageProperties(submission, messageDetails);
	}

	void transformMessageConfirmations(Submission submission, DomibusConnectorMessageType message) {
    	//TODO: correct mapping for confirmation?
		message.getMessageConfirmations()
			.stream()
			.forEach( (confirmation) -> {
				String contentId = generateCID();
//				contentId = "CONFIRMATION_" + contentId;
				Collection<TypedProperty> payloadProperties = new ArrayList<TypedProperty>();
				payloadProperties.add(new TypedProperty(DomibusConnectorMessage.NAME_KEY, confirmation.getConfirmationType().value()));
				payloadProperties.add(new TypedProperty(DomibusConnectorMessage.MIME_TYPE_KEY, XML_MIME_TYPE));
				payloadProperties.add(new TypedProperty(DomibusConnectorMessage.DESCRIPTION_KEY, confirmation.getConfirmationType().value()));
				DataHandler dh = convertXmlSourceToDataHandler(confirmation.getConfirmation());
				LOGGER.debug("Adding Confirmation [{}] as payload with properties [{}]", confirmation, payloadProperties);
				submission.addPayload(contentId, dh, payloadProperties);
			}
			);
	}


	void transformMessageProperties(Submission submission, DomibusConnectorMessageDetailsType messageDetails) {
		submission.addMessageProperty(DomibusConnectorMessage.FINAL_RECIPIENT_PROPERTY_NAME, messageDetails.getFinalRecipient());
		submission.addMessageProperty(DomibusConnectorMessage.ORIGINAL_SENDER_PROPERTY_NAME, messageDetails.getOriginalSender());
		if((messageDetails.getRefToMessageId() != null && messageDetails.getRefToMessageId().length() > 0)) {
			submission.addMessageProperty(DomibusConnectorMessage.ORIGINAL_MESSAGE_ID, messageDetails.getRefToMessageId());
		}
	}

	void transformCollaborationInfo(Submission submission, DomibusConnectorMessageDetailsType messageDetails) {

		submission.setAction(messageDetails.getAction().getAction());
		submission.setService(messageDetails.getService().getService());
		submission.setServiceType(messageDetails.getService().getServiceType());
		submission.setConversationId(messageDetails.getConversationId());
		
		//RiederB: No agreement Ref in current e-CODEX projects, so no agreement Ref from Connector

//		AgreementRefType agreementRef = collaborationInfo.getAgreementRef();
//		if(agreementRef!=null){
//			submission.setAgreementRef(agreementRef.getValue());
//			submission.setAgreementRefType(agreementRef.getType());
//		}
	}

	void transformParties(Submission submission, DomibusConnectorMessageDetailsType messageDetails) {
		
		DomibusConnectorPartyType from = messageDetails.getFromParty();

		submission.addFromParty(from.getPartyId(), from.getPartyIdType());
		submission.setFromRole(from.getRole());

		DomibusConnectorPartyType to = messageDetails.getToParty();

		submission.addToParty(to.getPartyId(), to.getPartyIdType());
		submission.setToRole(to.getRole());
	}

	private String generateCID() {
		String cid = "cid:payload_" + UUID.randomUUID().toString();
		return cid;
	}
	

}
