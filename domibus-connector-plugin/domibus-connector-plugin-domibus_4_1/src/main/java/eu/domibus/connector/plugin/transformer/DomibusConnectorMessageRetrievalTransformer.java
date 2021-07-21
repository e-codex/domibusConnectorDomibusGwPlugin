package eu.domibus.connector.plugin.transformer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import eu.domibus.connector.domain.transition.DomibusConnectorActionType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageDetailsType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.domain.transition.DomibusConnectorPartyType;
import eu.domibus.connector.domain.transition.DomibusConnectorServiceType;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.Submission.Party;
import eu.domibus.plugin.Submission.Payload;
import eu.domibus.plugin.Submission.TypedProperty;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;

@Component
public class DomibusConnectorMessageRetrievalTransformer implements MessageRetrievalTransformer<DomibusConnectorMessage> {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusConnectorMessageRetrievalTransformer.class);

    @Value("${connector.delivery.strip-loopback-suffix:true}")
    private boolean stripLoopbackSuffix;

	@Override
	public DomibusConnectorMessage transformFromSubmission(Submission submission, DomibusConnectorMessage connectorMessage) {
		if (submission.getMessageId() != null)
			LOGGER.debug("Starting transformation of Submission object to DomibusConnectorMessage with ebmsMessageId "+ submission.getMessageId());

		DomibusConnectorMessageType message = connectorMessage.getConnectorMessage();

		transformMessageDetails(submission, message);

		transformPayloads(submission, message);
		
		if(message.getMessageDetails().getEbmsMessageId()!=null)
			LOGGER.debug("Successfully transformed Submission object to DomibusConnectorMessage with ebmsMessageId "+ message.getMessageDetails().getEbmsMessageId());
		
//		if(LOGGER.isTraceEnabled()){
//			try {
//				String headerString = printXML(message);
//				LOGGER.trace(headerString);
//			} catch (JAXBException e1) {
//				LOGGER.error("JAXB exception occured while printXML", e1);
//			} catch (IOException ioe) {
//				LOGGER.error("IOException occured while printXML", ioe);
//			}
//		}
		
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

	}

	private static final String REMOVE_SUFFIX = "_1";

	private void transformMessageDetails(Submission submission, DomibusConnectorMessageType message) {
		DomibusConnectorMessageDetailsType messageDetails = new DomibusConnectorMessageDetailsType();
		if (stripLoopbackSuffix && submission.getMessageId().endsWith(REMOVE_SUFFIX)) {
			messageDetails.setEbmsMessageId(submission.getMessageId().substring(0, submission.getMessageId().length() - REMOVE_SUFFIX.length()));
		} else {
			messageDetails.setEbmsMessageId(submission.getMessageId());
		}
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

//does not work, cannot consume object twice! -> instead user buffer or copy first...
//	private String printXML(final DomibusConnectorMessageType object) throws JAXBException,
//		IOException {
//			JAXBContext ctx = JAXBContext.newInstance(DomibusConnectorMessage.class);
//
//			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//
//			Marshaller marshaller = ctx.createMarshaller();
//
//			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
//			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//
//            JAXBElement<DomibusConnectorMessageType> domibusConnectorMessageJAXBElement = new JAXBElement<DomibusConnectorMessageType>(new QName("uri", "local"), DomibusConnectorMessageType.class,  object);
//            marshaller.marshal(domibusConnectorMessageJAXBElement, byteArrayOutputStream);
//
//			byte[] buffer = byteArrayOutputStream.toByteArray();
//
//			byteArrayOutputStream.flush();
//			byteArrayOutputStream.close();
//
//			return new String(buffer, "UTF-8");
//		}

}
