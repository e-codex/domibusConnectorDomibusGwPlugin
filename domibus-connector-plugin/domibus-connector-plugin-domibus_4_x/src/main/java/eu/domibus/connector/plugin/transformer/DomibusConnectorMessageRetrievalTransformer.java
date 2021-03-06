package eu.domibus.connector.plugin.transformer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

	@Override
	public DomibusConnectorMessage transformFromSubmission(Submission submission, DomibusConnectorMessage connectorMessage) {
		if (submission.getMessageId() != null)
			LOGGER.debug("Starting transformation of Submission object to DomibusConnectorMessage with ebmsMessageId "+ submission.getMessageId());

		DomibusConnectorMessageType message = connectorMessage.getConnectorMessage();

		transformMessageDetails(submission, message);

		transformPayloads(submission, message);
		
		if(message.getMessageDetails().getEbmsMessageId()!=null)
			LOGGER.debug("Successfully transformed Submission object to DomibusConnectorMessage with ebmsMessageId [{}]", message.getMessageDetails().getEbmsMessageId());

		return connectorMessage;
	}

	List<SubmissionPayloadToDomibusMessageTransformer> payloadTransformers = Arrays.asList(
			new TransformPayloadToMessageContent(),
			new TransformPayloadToConfirmation(),
			new TransformPayloadToAttachment());

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

	private void transformMessageDetails(Submission submission, DomibusConnectorMessageType message) {
		DomibusConnectorMessageDetailsType messageDetails = new DomibusConnectorMessageDetailsType();
		String ebmsId = submission.getMessageId();
		if (ebmsId.endsWith("_1")) {
			ebmsId = ebmsId.substring(0, ebmsId.length() - 2); //strip _1 see loopback message sending
			LOGGER.warn("RCV message with _1 - this suffix is stripped from the message\nBecause it is the same EBMS message. See loopback message sending for details");
		}
		messageDetails.setEbmsMessageId(ebmsId);

		messageDetails.setRefToMessageId(submission.getRefToMessageId());
		messageDetails.setConversationId(submission.getConversationId());

		DomibusConnectorActionType action = new DomibusConnectorActionType();
		action.setAction(submission.getAction());
		messageDetails.setAction(action);
		
		DomibusConnectorServiceType service = new DomibusConnectorServiceType();
		service.setServiceType(submission.getServiceType());
		service.setService(submission.getService());
		messageDetails.setService(service);

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


}
