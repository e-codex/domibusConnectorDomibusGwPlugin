package eu.domibus.connector.plugin.transformer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import eu.domibus.connector.domain.transition.DomibusConnectorActionType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageDetailsType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.domain.transition.DomibusConnectorPartyType;
import eu.domibus.connector.domain.transition.DomibusConnectorServiceType;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.Submission.Party;
import eu.domibus.plugin.Submission.Payload;
import eu.domibus.plugin.Submission.TypedProperty;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;

@Component
public class DomibusConnectorMessageRetrievalTransformer implements MessageRetrievalTransformer<DomibusConnectorMessage> {
	
	private static final Log LOGGER = LogFactory.getLog(DomibusConnectorMessageRetrievalTransformer.class);

	@Override
	public DomibusConnectorMessage transformFromSubmission(Submission submission, DomibusConnectorMessage connectorMessage) {
		if (submission.getMessageId() != null)
			LOGGER.debug("Starting transformation of Submission object to DomibusConnectorMessage with ebmsMessageId "+ submission.getMessageId());

		DomibusConnectorMessageType message = connectorMessage.getConnectorMessage();

		transformMessageDetails(submission, message);

		transformPayloads(submission, message);
		
		if(message.getMessageDetails().getEbmsMessageId()!=null)
			LOGGER.debug("Successfully transformed Submission object to DomibusConnectorMessage with ebmsMessageId "+ message.getMessageDetails().getEbmsMessageId());
		
		if(LOGGER.isTraceEnabled()){
			try {
				String headerString = printXML(message);
				LOGGER.trace(headerString);
			} catch (JAXBException e1) {
				LOGGER.error("JAXB exception occured while printXML", e1);
			} catch (IOException ioe) {
				LOGGER.error("IOException occured while printXML", ioe);
			}
		}
		
		return connectorMessage;
	}

	List<SubmissionPayloadToDomibusMessageTransformer> payloadTransformers = Arrays.asList(new SubmissionPayloadToDomibusMessageTransformer[]{
			new TransformPayloadToMessageContent(),
			new TransformPayloadToConfirmation(),
			new TransformPayloadToAttachment()
	});

	private void transformPayloads(Submission submission, DomibusConnectorMessageType message) {
		Set<Payload> payloads = submission.getPayloads();
		payloads.stream()
				.map( payload ->  new SubmissionPayloadToDomibusMessageTransformer.PayloadWrapper(payload))
				.forEach( wrappedPayload -> {
					payloadTransformers.stream()
							//get the first transformer which can transform the payload
							.filter(transformer -> transformer.canTransform(wrappedPayload)).findFirst()
							.get()
							.transformSubmissionToAttachment(wrappedPayload, message);
				});

//		if(!payloads.isEmpty()){
//			Iterator<Payload> iterator = payloads.iterator();
//			while(iterator.hasNext()){
//				Payload payload = iterator.next();
//				String payloadName = null;
//				String payloadMimeType = null;
//				String payloadDescription = null;
//				Collection<TypedProperty> properties = payload.getPayloadProperties();
//				Iterator<TypedProperty> pIt = properties.iterator();
//				while(pIt.hasNext()){
//					TypedProperty prop = pIt.next();
//					switch(prop.getKey()){
//					case DomibusConnectorMessage.NAME_KEY: payloadName = prop.getValue();break;
//					case DomibusConnectorMessage.MIME_TYPE_KEY: payloadMimeType = prop.getValue();break;
//					case DomibusConnectorMessage.DESCRIPTION_KEY: payloadDescription = prop.getValue();break;
//					}
//
//				}
//				if((payloadName!=null && payloadName.equals(DomibusConnectorMessage.MESSAGE_CONTENT_VALUE)) || payload.isInBody()){
//					DomibusConnectorMessageContentType mContent = new DomibusConnectorMessageContentType();
//					Source source = null;
//					try {
//						source = new StreamSource(payload.getPayloadDatahandler().getInputStream());
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//					mContent.setXmlContent(source);
//					message.setMessageContent(mContent);
//				}else{
//					DomibusConnectorMessageAttachmentType mAttachment = new DomibusConnectorMessageAttachmentType();
//					mAttachment.setAttachment(payload.getPayloadDatahandler());
//					mAttachment.setName(payloadName);
//					mAttachment.setMimeType(payloadMimeType);
//					mAttachment.setDescription(payloadDescription);
//					mAttachment.setIdentifier(payloadDescription);
//					message.getMessageAttachments().add(mAttachment);
//				}
//
//			}
//		}
	}

	private void transformMessageDetails(Submission submission, DomibusConnectorMessageType message) {
		DomibusConnectorMessageDetailsType messageDetails = new DomibusConnectorMessageDetailsType();
		messageDetails.setEbmsMessageId(submission.getMessageId());
		messageDetails.setRefToMessageId(submission.getRefToMessageId());
		messageDetails.setConversationId(submission.getConversationId());

		DomibusConnectorActionType action = new DomibusConnectorActionType();
		action.setAction(submission.getAction());
		messageDetails.setAction(action);
		
		DomibusConnectorServiceType service = new DomibusConnectorServiceType();
		service.setServiceType(submission.getServiceType());
		service.setService(submission.getService());
		messageDetails.setService(service);
		
		//RiederB: No agreement Ref in current e-CODEX projects, so no agreement Ref to Connector
		
//		if(submission.getAgreementRef()!=null){
//
//			AgreementRefType agreementRef = new AgreementRefType();
//			agreementRef.setType(submission.getAgreementRefType());
//			agreementRef.setValue(submission.getAgreementRef());
//			collaborationInfo.setAgreementRef(agreementRef );
//		}
		
		Iterator<Party> fromIt = submission.getFromParties().iterator();
		Party fromNext = fromIt.next();
		DomibusConnectorPartyType from = new DomibusConnectorPartyType();
		from.setPartyId(fromNext.getPartyId());
		from.setPartyIdType(fromNext.getPartyIdType());
		from.setRole(submission.getFromRole());
		messageDetails.setFromParty(from);
		
		Iterator<Party> toIt = submission.getToParties().iterator();
		Party toNext = toIt.next();
		DomibusConnectorPartyType to = new DomibusConnectorPartyType();
		to.setPartyId(toNext.getPartyId());
		to.setPartyIdType(toNext.getPartyIdType());
		to.setRole(submission.getToRole());
		messageDetails.setToParty(to );
		
		if(!CollectionUtils.isEmpty(submission.getMessageProperties())){
			for(TypedProperty property:submission.getMessageProperties()){
				switch(property.getKey()) {
				case DomibusConnectorMessage.FINAL_RECIPIENT_PROPERTY_NAME:messageDetails.setFinalRecipient(property.getValue());break;
				case DomibusConnectorMessage.ORIGINAL_SENDER_PROPERTY_NAME:messageDetails.setOriginalSender(property.getValue());break;
                    case DomibusConnectorMessage.ORIGINAL_MESSAGE_ID: messageDetails.setRefToMessageId(property.getValue());break;
				default: LOGGER.error("Unknown ebms3 header message property: "+property.getKey());
				}
			}
		}
		
		message.setMessageDetails(messageDetails );
	}

	private String printXML(final DomibusConnectorMessageType object) throws JAXBException,
		IOException {
			JAXBContext ctx = JAXBContext.newInstance(DomibusConnectorMessage.class);
	
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	
			Marshaller marshaller = ctx.createMarshaller();
	
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            JAXBElement<DomibusConnectorMessageType> domibusConnectorMessageJAXBElement = new JAXBElement<DomibusConnectorMessageType>(new QName("uri", "local"), DomibusConnectorMessageType.class,  object);
            marshaller.marshal(domibusConnectorMessageJAXBElement, byteArrayOutputStream);
	
			byte[] buffer = byteArrayOutputStream.toByteArray();
	
			byteArrayOutputStream.flush();
			byteArrayOutputStream.close();
	
			return new String(buffer, "UTF-8");
		}

}
