package eu.domibus.connector.plugin.transformer;


import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.domain.transition.testutil.TransitionCreator;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.plugin.Submission;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class DomibusConnectorMessageRetrievalSubmissionTransformerTest {

    DomibusConnectorMessageRetrievalTransformer retrievalTransformer;
    DomibusConnectorMessageSubmissionTransformer submissionTransformer;

    @Before
    public void setUp() {
        submissionTransformer = new DomibusConnectorMessageSubmissionTransformer();
        retrievalTransformer = new DomibusConnectorMessageRetrievalTransformer();
    }


    @Test
    public void testSubmissionRetrieve() {
        DomibusConnectorMessageType epoMessage = TransitionCreator.createEpoMessage();
        epoMessage.getMessageConfirmations().add(TransitionCreator.createMessageConfirmationType_DELIVERY());
        DomibusConnectorMessage msg = new DomibusConnectorMessage(epoMessage);


        Submission submissionMessage = submissionTransformer.transformToSubmission(msg);

        DomibusConnectorMessageType retrievedEpoMessage = new DomibusConnectorMessageType();
        DomibusConnectorMessage domibusConnectorMessage = retrievalTransformer.transformFromSubmission(submissionMessage, new DomibusConnectorMessage(retrievedEpoMessage));

        retrievedEpoMessage = domibusConnectorMessage.getConnectorMessage();

        //assertThat(retrievedEpoMessage.getMessageDetails()).isEqualToComparingFieldByField(epoMessage.getMessageDetails());
        assertThat(retrievedEpoMessage.getMessageConfirmations()).as("should have one confirmation").hasSize(1);
        assertThat(retrievedEpoMessage.getMessageAttachments()).as("should have one attachment").hasSize(1);
    }

    @Test
    public void testSubmissionRetrieve_withEvidenceMessage() {
        DomibusConnectorMessageType epoMessage = TransitionCreator.createEpoMessage();
        epoMessage.setMessageContent(null);
        epoMessage.getMessageAttachments().clear();
        epoMessage.getMessageConfirmations().add(TransitionCreator.createMessageConfirmationType_DELIVERY());
        DomibusConnectorMessage msg = new DomibusConnectorMessage(epoMessage);


        Submission submissionMessage = submissionTransformer.transformToSubmission(msg);

        DomibusConnectorMessageType retrievedEpoMessage = new DomibusConnectorMessageType();
        DomibusConnectorMessage domibusConnectorMessage = retrievalTransformer.transformFromSubmission(submissionMessage, new DomibusConnectorMessage(retrievedEpoMessage));

        retrievedEpoMessage = domibusConnectorMessage.getConnectorMessage();

        //assertThat(retrievedEpoMessage.getMessageDetails()).isEqualToComparingFieldByField(epoMessage.getMessageDetails());
        assertThat(retrievedEpoMessage.getMessageConfirmations()).as("should have one confirmation").hasSize(1);
    }

}
