package eu.domibus.connector.plugin.transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import connector.domibus.eu.domibusconnectorgatewayservice._1.AgreementRefType;
import connector.domibus.eu.domibusconnectorgatewayservice._1.MessageAttachmentType;
import connector.domibus.eu.domibusconnectorgatewayservice._1.MessageCollaborationInfoType;
import connector.domibus.eu.domibusconnectorgatewayservice._1.MessageContentType;
import connector.domibus.eu.domibusconnectorgatewayservice._1.MessageDetailsType;
import connector.domibus.eu.domibusconnectorgatewayservice._1.MessagePartyInfoType;
import connector.domibus.eu.domibusconnectorgatewayservice._1.MessagePropertiesType;
import connector.domibus.eu.domibusconnectorgatewayservice._1.MessagePropertyType;
import connector.domibus.eu.domibusconnectorgatewayservice._1.MessageType;
import connector.domibus.eu.domibusconnectorgatewayservice._1.PartyType;
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

		MessageType message = connectorMessage.getConnectorMessage();

		transformMessageDetails(submission, message);

		transformMessageContent(submission, message);

		transformMessageAttachments(submission, message);

		return submission;
	}

	private void transformMessageAttachments(Submission submission, MessageType message) {
		List<MessageAttachmentType> messageAttachments = message.getMessageAttachments();
		if(!CollectionUtils.isEmpty(messageAttachments)){
			for(MessageAttachmentType attachment: messageAttachments){
				String contentId = generateCID();

				Collection<TypedProperty> payloadProperties = new ArrayList<TypedProperty>();
				payloadProperties.add(new TypedProperty(DomibusConnectorMessage.NAME_KEY, attachment.getAttachmentName()));
				payloadProperties.add(new TypedProperty(DomibusConnectorMessage.MIME_TYPE_KEY,attachment.getAttachmentMimeType()));
				String attachmentDescription = attachment.getAttachmentIdentifier()!=null?attachment.getAttachmentIdentifier():attachment.getAttachmentName();
				payloadProperties.add(new TypedProperty(DomibusConnectorMessage.DESCRIPTION_KEY,attachmentDescription));
				
				submission.addPayload(contentId, attachment.getAttachmentData(), payloadProperties);
			}
		}
	}

	private void transformMessageContent(Submission submission, MessageType message) {
		MessageContentType messageContent = message.getMessageContent();
		String contentId = generateCID();

		Collection<TypedProperty> payloadProperties = new ArrayList<TypedProperty>();
		payloadProperties.add(new TypedProperty(DomibusConnectorMessage.NAME_KEY, DomibusConnectorMessage.MESSAGE_CONTENT_VALUE));
		payloadProperties.add(new TypedProperty(DomibusConnectorMessage.MIME_TYPE_KEY,messageContent.getContentMimeType()));
		payloadProperties.add(new TypedProperty(DomibusConnectorMessage.DESCRIPTION_KEY,messageContent.getContentName()));
		submission.addPayload(contentId, messageContent.getContentData(), payloadProperties);
	}

	private void transformMessageDetails(Submission submission, MessageType message) {
		MessageDetailsType messageDetails = message.getMessageDetails();

		submission.setMessageId(messageDetails.getMessageId());
		submission.setRefToMessageId(messageDetails.getRefToMessageId());

		transformParties(submission, messageDetails);

		transformCollaborationInfo(submission, messageDetails);

		transformMessageProperties(submission, messageDetails);
	}

	private void transformMessageProperties(Submission submission, MessageDetailsType messageDetails) {
		MessagePropertiesType messageProperties = messageDetails.getMessageProperties();
		if(!CollectionUtils.isEmpty(messageProperties.getMessageProperties())){
			for(MessagePropertyType property:messageProperties.getMessageProperties()){
				if(property.getType()!=null){
					submission.addMessageProperty(property.getName(), property.getValue(), property.getType());
				}else{
					submission.addMessageProperty(property.getName(), property.getValue());
				}
			}
		}
	}

	private void transformCollaborationInfo(Submission submission, MessageDetailsType messageDetails) {
		MessageCollaborationInfoType collaborationInfo = messageDetails.getCollaborationInfo();

		submission.setAction(collaborationInfo.getAction());
		submission.setService(collaborationInfo.getServiceId());
		submission.setServiceType(collaborationInfo.getServiceType());
		submission.setConversationId(collaborationInfo.getConversationId());

		AgreementRefType agreementRef = collaborationInfo.getAgreementRef();
		if(agreementRef!=null){
			submission.setAgreementRef(agreementRef.getValue());
			submission.setAgreementRefType(agreementRef.getType());
		}
	}

	private void transformParties(Submission submission, MessageDetailsType messageDetails) {
		MessagePartyInfoType partyInfo = messageDetails.getPartyInfo();

		PartyType from = partyInfo.getFrom();

		submission.addFromParty(from.getPartyId(), from.getPartyIdType());
		submission.setFromRole(from.getRole());

		PartyType to = partyInfo.getTo();

		submission.addToParty(to.getPartyId(), to.getPartyIdType());
		submission.setToRole(to.getRole());
	}

	private String generateCID() {

		String cid = "cid:payload_" + UUID.randomUUID().toString();

		return cid;
	}

}
