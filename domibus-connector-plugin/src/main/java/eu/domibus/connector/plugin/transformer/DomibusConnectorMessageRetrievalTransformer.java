package eu.domibus.connector.plugin.transformer;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

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
import eu.domibus.plugin.Submission.Party;
import eu.domibus.plugin.Submission.Payload;
import eu.domibus.plugin.Submission.TypedProperty;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;

@Component
public class DomibusConnectorMessageRetrievalTransformer implements MessageRetrievalTransformer<DomibusConnectorMessage> {

	@Override
	public DomibusConnectorMessage transformFromSubmission(Submission submission, DomibusConnectorMessage connectorMessage) {

		MessageType message = connectorMessage.getConnectorMessage();

		transformMessageDetails(submission, message);

		transformPayloads(submission, message);
		
		return connectorMessage;
	}

	private void transformPayloads(Submission submission, MessageType message) {
		Set<Payload> payloads = submission.getPayloads();
		if(!payloads.isEmpty()){
			Iterator<Payload> iterator = payloads.iterator();
			while(iterator.hasNext()){
				Payload payload = iterator.next();
				String payloadName = null;
				String payloadMimeType = null;
				String payloadDescription = null;
				String payloadIdentifier = null;
				Collection<TypedProperty> properties = payload.getPayloadProperties();
				Iterator<TypedProperty> pIt = properties.iterator();
				while(pIt.hasNext()){
					TypedProperty prop = pIt.next();
					switch(prop.getKey()){
					case "name": payloadName = prop.getValue();break;
					case "mime-type": payloadMimeType = prop.getValue();break;
					case "description": payloadDescription = prop.getValue();break;
					case "identifier": payloadIdentifier = prop.getValue();break;
					}

				}
				if(payloadName!=null && payloadName.equals("messageContent")){
					MessageContentType mContent = new MessageContentType();
					mContent.setContentMimeType(payloadMimeType);
					mContent.setContentData(payload.getPayloadDatahandler());
					mContent.setContentName(payloadIdentifier);
					message.setMessageContent(mContent);
				}else{
					MessageAttachmentType mAttachment = new MessageAttachmentType();
					mAttachment.setAttachmentData(payload.getPayloadDatahandler());
					mAttachment.setAttachmentName(payloadName);
					mAttachment.setAttachmentMimeType(payloadMimeType);
					mAttachment.setAttachmentDescription(payloadDescription);
					mAttachment.setAttachmentIdentifier(payloadIdentifier);
					message.getMessageAttachments().add(mAttachment);
				}

			}
		}
	}

	private void transformMessageDetails(Submission submission, MessageType message) {
		MessageDetailsType messageDetails = new MessageDetailsType();
		messageDetails.setMessageId(submission.getMessageId());
		messageDetails.setRefToMessageId(submission.getRefToMessageId());

		MessageCollaborationInfoType collaborationInfo = new MessageCollaborationInfoType();
		collaborationInfo.setAction(submission.getAction());
		collaborationInfo.setConversationId(submission.getConversationId());
		collaborationInfo.setServiceId(submission.getService());
		collaborationInfo.setServiceType(submission.getServiceType());
		if(submission.getAgreementRef()!=null){

			AgreementRefType agreementRef = new AgreementRefType();
			agreementRef.setType(submission.getAgreementRefType());
			agreementRef.setValue(submission.getAgreementRef());
			collaborationInfo.setAgreementRef(agreementRef );
		}
		messageDetails.setCollaborationInfo(collaborationInfo );

		MessagePartyInfoType partyInfo = new MessagePartyInfoType();
		Iterator<Party> fromIt = submission.getFromParties().iterator();
		Party fromNext = fromIt.next();
		PartyType from = new PartyType();
		from.setPartyId(fromNext.getPartyId());
		from.setPartyIdType(fromNext.getPartyIdType());
		from.setRole(submission.getFromRole());
		partyInfo.setFrom(from );
		Iterator<Party> toIt = submission.getFromParties().iterator();
		Party toNext = toIt.next();
		PartyType to = new PartyType();
		to.setPartyId(toNext.getPartyId());
		to.setPartyIdType(toNext.getPartyIdType());
		to.setRole(submission.getToRole());
		partyInfo.setTo(to );
		messageDetails.setPartyInfo(partyInfo );

		MessagePropertiesType messageProperties = new MessagePropertiesType();
		if(!CollectionUtils.isEmpty(submission.getMessageProperties())){
			for(TypedProperty property:submission.getMessageProperties()){

				MessagePropertyType messageProperty = new MessagePropertyType();
				messageProperty.setName(property.getKey());
				messageProperty.setType(property.getType());
				messageProperty.setValue(property.getValue());
				messageProperties.getMessageProperties().add(messageProperty );
			}
		}
		messageDetails.setMessageProperties(messageProperties );

		message.setMessageDetails(messageDetails );
	}

}
