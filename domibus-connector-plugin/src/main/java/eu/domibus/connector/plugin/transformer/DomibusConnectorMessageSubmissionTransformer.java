package eu.domibus.connector.plugin.transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import eu.domibus.connector.domain.transition.DomibusConnectorMessageAttachmentType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageContentType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageDetailsType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.domain.transition.DomibusConnectorPartyType;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.Submission.TypedProperty;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

@Component
public class DomibusConnectorMessageSubmissionTransformer implements MessageSubmissionTransformer<DomibusConnectorMessage> {

	private static final Log LOGGER = LogFactory.getLog(DomibusConnectorMessageSubmissionTransformer.class);
	
	@Override
	public Submission transformToSubmission(DomibusConnectorMessage connectorMessage) {
		Submission submission = new Submission();

		DomibusConnectorMessageType message = connectorMessage.getConnectorMessage();

		transformMessageDetails(submission, message);

		transformMessageContent(submission, message);

		transformMessageAttachments(submission, message);

		return submission;
	}

	private void transformMessageAttachments(Submission submission, DomibusConnectorMessageType message) {
		List<DomibusConnectorMessageAttachmentType> messageAttachments = message.getMessageAttachments();
		if(!CollectionUtils.isEmpty(messageAttachments)){
			for(DomibusConnectorMessageAttachmentType attachment: messageAttachments){
				String contentId = generateCID();

				Collection<TypedProperty> payloadProperties = new ArrayList<TypedProperty>();
				payloadProperties.add(new TypedProperty(DomibusConnectorMessage.NAME_KEY, attachment.getName()));
				payloadProperties.add(new TypedProperty(DomibusConnectorMessage.MIME_TYPE_KEY,attachment.getMimeType()));
				String attachmentDescription = attachment.getIdentifier()!=null?attachment.getIdentifier():attachment.getName();
				payloadProperties.add(new TypedProperty(DomibusConnectorMessage.DESCRIPTION_KEY,attachmentDescription));
				
				submission.addPayload(contentId, attachment.getAttachment(), payloadProperties);
			}
		}
	}

	private void transformMessageContent(Submission submission, DomibusConnectorMessageType message) {
		DomibusConnectorMessageContentType messageContent = message.getMessageContent();
		String contentId = generateCID();

		Collection<TypedProperty> payloadProperties = new ArrayList<TypedProperty>();
		payloadProperties.add(new TypedProperty(DomibusConnectorMessage.NAME_KEY, DomibusConnectorMessage.MESSAGE_CONTENT_VALUE));
		payloadProperties.add(new TypedProperty(DomibusConnectorMessage.MIME_TYPE_KEY,DomibusConnectorMessage.XML_MIME_TYPE));
		payloadProperties.add(new TypedProperty(DomibusConnectorMessage.DESCRIPTION_KEY,DomibusConnectorMessage.MESSAGE_CONTENT_VALUE));
		submission.addPayload(contentId, messageContent.getXmlContent(), payloadProperties);
	}

	private void transformMessageDetails(Submission submission, DomibusConnectorMessageType message) {
		DomibusConnectorMessageDetailsType messageDetails = message.getMessageDetails();

//		submission.setMessageId(messageDetails.getMessageId());
		submission.setRefToMessageId(messageDetails.getRefToMessageId());

		transformParties(submission, messageDetails);

		transformCollaborationInfo(submission, messageDetails);

		transformMessageProperties(submission, messageDetails);
	}

	private void transformMessageProperties(Submission submission, DomibusConnectorMessageDetailsType messageDetails) {
		submission.addMessageProperty(DomibusConnectorMessage.FINAL_RECIPIENT_PROPERTY_NAME, messageDetails.getFinalRecipient());
		submission.addMessageProperty(DomibusConnectorMessage.ORIGINAL_SENDER_PROPERTY_NAME, messageDetails.getOriginalSender());
		if(!StringUtils.isEmpty(messageDetails.getRefToMessageId())) {
			submission.addMessageProperty(DomibusConnectorMessage.ORIGINAL_MESSAGE_ID, messageDetails.getRefToMessageId());
		}
	}

	private void transformCollaborationInfo(Submission submission, DomibusConnectorMessageDetailsType messageDetails) {

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

	private void transformParties(Submission submission, DomibusConnectorMessageDetailsType messageDetails) {
		
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
