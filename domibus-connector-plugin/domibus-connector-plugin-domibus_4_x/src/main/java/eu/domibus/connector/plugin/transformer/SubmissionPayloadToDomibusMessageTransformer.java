package eu.domibus.connector.plugin.transformer;

import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.plugin.Submission;

import javax.activation.DataHandler;
import java.util.Collection;
import java.util.Iterator;

public interface SubmissionPayloadToDomibusMessageTransformer {

    public boolean canTransform(PayloadWrapper payloadWrapper);

    public DomibusConnectorMessageType transformSubmissionToAttachment(PayloadWrapper payloadWrapper, DomibusConnectorMessageType messageType);

    public static class PayloadWrapper {

        private final Submission.Payload payload;
        private String payloadName;
        private String payloadMimeType;
        private String payloadDescription;

        public PayloadWrapper(Submission.Payload payload) {
            if (payload == null) {
                throw new IllegalArgumentException("Payload is not allowed to be null!");
            }
            this.payload = payload;
            Collection<Submission.TypedProperty> properties = payload.getPayloadProperties();
            Iterator<Submission.TypedProperty> pIt = properties.iterator();
            while(pIt.hasNext()){
                Submission.TypedProperty prop = pIt.next();
                switch(prop.getKey()){
                    case DomibusConnectorMessage.NAME_KEY: payloadName = prop.getValue();break;
                    case DomibusConnectorMessage.MIME_TYPE_KEY: payloadMimeType = prop.getValue();break;
                    case DomibusConnectorMessage.DESCRIPTION_KEY: payloadDescription = prop.getValue();break;
                }
            }
        }

        public String getPayloadName() {
            return payloadName;
        }

        public String getPayloadMimeType() {
            return payloadMimeType;
        }

        public String getPayloadDescription() {
            return payloadDescription;
        }

        public DataHandler getPayloadDataHandler() {
            return this.payload.getPayloadDatahandler();
        }

        public Submission.Payload getPayload() {
            return payload;
        }
    }

}
