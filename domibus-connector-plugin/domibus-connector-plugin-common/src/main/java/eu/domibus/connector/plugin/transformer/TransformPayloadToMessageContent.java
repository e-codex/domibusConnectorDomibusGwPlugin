package eu.domibus.connector.plugin.transformer;

import javax.xml.transform.Source;

import eu.domibus.connector.domain.transition.DomibusConnectorMessageContentType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.domain.transition.tools.ConversionTools;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.Submission;

public class TransformPayloadToMessageContent implements SubmissionPayloadToDomibusMessageTransformer {


    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(TransformPayloadToMessageContent.class);

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
        
        byte[] byteArray = ConversionTools.convertDataHandlerToByteArray(payloadWrapper.getPayloadDataHandler());

        if(LOGGER.isTraceEnabled()) {
           	LOGGER.trace("Business content XML before transformed to Source: {}", new String(byteArray));
        }
            
		source = ConversionTools.convertByteArrayToStreamSource(byteArray);

		mContent.setXmlContent(source);
        messageType.setMessageContent(mContent);

        LOGGER.debug("Successfully transformed payload [{}] to message content", payloadWrapper.getPayloadName(), mContent);

        return messageType;
    }

}
