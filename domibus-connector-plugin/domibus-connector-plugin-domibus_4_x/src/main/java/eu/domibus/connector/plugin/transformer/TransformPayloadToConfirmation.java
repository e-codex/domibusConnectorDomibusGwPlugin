package eu.domibus.connector.plugin.transformer;

import eu.domibus.connector.domain.transition.DomibusConnectorConfirmationType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageConfirmationType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import javax.activation.DataHandler;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class TransformPayloadToConfirmation implements SubmissionPayloadToDomibusMessageTransformer {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(TransformPayloadToConfirmation.class);

    private static final Set<String> typeNames =(Arrays.stream(DomibusConnectorConfirmationType.values()).map(t -> t.value()).collect(Collectors.toSet()));

    @Override
    public boolean canTransform(PayloadWrapper payloadWrapper) {
        return DomibusConnectorMessage.XML_MIME_TYPE.equals(payloadWrapper.getPayloadMimeType())
                && typeNames.contains(payloadWrapper.getPayloadDescription());
    }

    @Override
    public DomibusConnectorMessageType transformSubmissionToAttachment(PayloadWrapper payloadWrapper, DomibusConnectorMessageType messageType) {
        if (!canTransform(payloadWrapper)) {
            String error = String.format("Cannot transform this payload [%s] with [%s] transformer to confirmation! Call canTransform first!",
                    payloadWrapper, TransformPayloadToConfirmation.class);
            throw new IllegalArgumentException(error);
        }
        try {
            DomibusConnectorMessageConfirmationType confirmation = new DomibusConnectorMessageConfirmationType();

            DomibusConnectorConfirmationType domibusConnectorConfirmationType = DomibusConnectorConfirmationType.fromValue(payloadWrapper.getPayloadDescription());
            confirmation.setConfirmationType(domibusConnectorConfirmationType);

            DataHandler payloadDataHandler = payloadWrapper.getPayloadDataHandler();
            InputStream inputStream = payloadDataHandler.getInputStream();
            confirmation.setConfirmation(new StreamSource(inputStream));
            LOGGER.debug("Successfully transformed payload [{}] to confirmation [{}]", payloadWrapper.getPayloadName(), confirmation);
            messageType.getMessageConfirmations().add(confirmation);
            return messageType;
        } catch (IOException ioe) {
            String error = String.format("Cannot transform payload [%s] to confirmation because IOException occured while reading data from payload!", payloadWrapper);
            throw new RuntimeException(error, ioe);
        }
    }
}
