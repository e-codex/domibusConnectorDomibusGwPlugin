package eu.domibus.connector.plugin.transformer;

import eu.domibus.connector.domain.transition.DomibusConnectorMessageContentType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.plugin.Submission;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

public class TransformPayloadToMessageContent implements SubmissionPayloadToDomibusMessageTransformer {

    private static final Log LOGGER = LogFactory.getLog(TransformPayloadToMessageContent.class);

    /**
     * can transform as message body if payload name equals {@link DomibusConnectorMessage#MESSAGE_CONTENT_VALUE}
     *  OR
     * {@link Submission.Payload#isInBody} is true
     * @param payloadWrapper the payload (wrapped in payloadWrapper)
     * @return if can be transformed
     */
    @Override
    public boolean canTransform(PayloadWrapper payloadWrapper) {
        return  DomibusConnectorMessage.MESSAGE_CONTENT_VALUE.equals(payloadWrapper.getPayloadName())
                || payloadWrapper.getPayload().isInBody();

    }

    @Override
    public DomibusConnectorMessageType transformSubmissionToAttachment(PayloadWrapper payloadWrapper, DomibusConnectorMessageType messageType) {
        if (!canTransform(payloadWrapper)) {
            throw new IllegalArgumentException(String.format("cannot transform this payload with [%s]!, call canTransform first!", TransformPayloadToMessageContent.class));
        }
        LOGGER.debug(String.format("%s transformer started transforming to message content", TransformPayloadToMessageContent.class));
        DomibusConnectorMessageContentType mContent = new DomibusConnectorMessageContentType();
        Source source = null;
        try {
            source = new StreamSource(payloadWrapper.getPayloadDataHandler().getInputStream());
        } catch (IOException e) {
            String error = "Cannot load xml content from message! Payload name is: " + DomibusConnectorMessage.MESSAGE_CONTENT_VALUE;
            LOGGER.error(error, e);
            throw new RuntimeException(error, e);
        }
        mContent.setXmlContent(source);
        messageType.setMessageContent(mContent);
        return messageType;
    }

}
