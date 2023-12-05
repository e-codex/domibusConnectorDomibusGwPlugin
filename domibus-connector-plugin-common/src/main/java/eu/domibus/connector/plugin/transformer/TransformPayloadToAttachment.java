package eu.domibus.connector.plugin.transformer;

import eu.domibus.connector.domain.transition.DomibusConnectorMessageAttachmentType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

public class TransformPayloadToAttachment implements SubmissionPayloadToDomibusMessageTransformer {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(TransformPayloadToAttachment.class);

    @Override
    public boolean canTransform(PayloadWrapper payloadWrapper) {
        return true;
    }

    @Override
    public DomibusConnectorMessageType transformSubmissionToAttachment(PayloadWrapper payload, DomibusConnectorMessageType messageType) {
        DomibusConnectorMessageAttachmentType mAttachment = new DomibusConnectorMessageAttachmentType();
        mAttachment.setAttachment(payload.getPayloadDataHandler());
        mAttachment.setName(payload.getPayloadName());
        mAttachment.setMimeType(payload.getPayloadMimeType());
        mAttachment.setDescription(payload.getPayloadDescription());
        mAttachment.setIdentifier(payload.getPayloadDescription());
        messageType.getMessageAttachments().add(mAttachment);
        LOGGER.debug("Successfully transformed payload [{}] with size [{}] to ecodex message attachment [{}]", payload.getPayloadName(), payload.getPayload().getPayloadSize(), mAttachment);
        return messageType;
    }



}
