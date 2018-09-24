package eu.domibus.connector.plugin.transformer;

import eu.domibus.connector.domain.transition.DomibusConnectorMessageAttachmentType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;

public class TransformPayloadToAttachment implements SubmissionPayloadToDomibusMessageTransformer {


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
        return messageType;
    }



}
