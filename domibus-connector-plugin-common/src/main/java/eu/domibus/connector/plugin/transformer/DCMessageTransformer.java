package eu.domibus.connector.plugin.transformer;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DCMessageTransformer {

    private final DomibusConnectorMessageSubmissionTransformer messageSubmissionTransformer;
    private final DomibusConnectorMessageRetrievalTransformer messageRetrievalTransformer;

    public DCMessageTransformer(DomibusConnectorMessageSubmissionTransformer messageSubmissionTransformer, DomibusConnectorMessageRetrievalTransformer messageRetrievalTransformer) {
        Objects.requireNonNull(messageSubmissionTransformer);
        Objects.requireNonNull(messageRetrievalTransformer);
        this.messageSubmissionTransformer = messageSubmissionTransformer;
        this.messageRetrievalTransformer = messageRetrievalTransformer;
    }

    public DomibusConnectorMessageSubmissionTransformer getMessageSubmissionTransformer() {
        return messageSubmissionTransformer;
    }

    public DomibusConnectorMessageRetrievalTransformer getMessageRetrievalTransformer() {
        return messageRetrievalTransformer;
    }
}
