package eu.domibus.connector.plugin.transformer;


import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.domain.transition.testutil.TransitionCreator;
import eu.domibus.connector.plugin.domain.DomibusConnectorMessage;
import eu.domibus.plugin.Submission;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

public class DomibusConnectorMessageSubmissionTransformerTest {


    DomibusConnectorMessageSubmissionTransformer submissionTransformer;

    @Before
    public void setUp() {
        submissionTransformer = new DomibusConnectorMessageSubmissionTransformer();
    }




    @Test
    public void testTransformMessageConfirmations() {
        DomibusConnectorMessageType epoMessage = TransitionCreator.createEpoMessage();
        epoMessage.getMessageConfirmations().add(TransitionCreator.createMessageConfirmationType_DELIVERY());
        DomibusConnectorMessage connectorMessage = new DomibusConnectorMessage(epoMessage);

        Submission submission = submissionTransformer.transformToSubmission(connectorMessage);

        Map<String, Submission.Payload> confirmations = submission.getPayloads().stream()
                .filter(p -> p.getContentId().startsWith("CONFIRMATION_"))
                .collect(
                        Collectors.toMap(Submission.Payload::getContentId, Function.identity()));

        assertThat(confirmations).hasSize(1);


    }



    @Test
    public void testTransformMessageProperties() {
        DomibusConnectorMessageType epoMessage = TransitionCreator.createEpoMessage();
        DomibusConnectorMessage connectorMessage = new DomibusConnectorMessage(epoMessage);

        Submission submission = submissionTransformer.transformToSubmission(connectorMessage);

        ArrayList<Submission.TypedProperty> messageProperties = new ArrayList<>();
        messageProperties.addAll(submission.getMessageProperties());
        assertThat(messageProperties).hasSize(3);

        Map<String, Submission.TypedProperty> collect = messageProperties.stream().collect(
                Collectors.toMap(Submission.TypedProperty::getKey, Function.identity()));

        Submission.TypedProperty finalRecipient = collect.get(DomibusConnectorMessage.FINAL_RECIPIENT_PROPERTY_NAME);
        assertThat(finalRecipient.getValue()).isEqualTo("finalRecipient");

        Submission.TypedProperty originalSender = collect.get(DomibusConnectorMessage.ORIGINAL_SENDER_PROPERTY_NAME);
        assertThat(originalSender.getValue()).isEqualTo("originalSender");
        Submission.TypedProperty originalMessageId = collect.get(DomibusConnectorMessage.ORIGINAL_MESSAGE_ID);
        assertThat(originalMessageId.getValue()).isEqualTo("refToMessageId");


    }




    @Test
    public void testTransformCollaborationInfo() {
        DomibusConnectorMessageType epoMessage = TransitionCreator.createEpoMessage();
        DomibusConnectorMessage connectorMessage = new DomibusConnectorMessage(epoMessage);

        Submission submission = submissionTransformer.transformToSubmission(connectorMessage);

        assertThat(submission.getAction()).isEqualTo("Form_A");
        assertThat(submission.getService()).isEqualTo("EPO");
        assertThat(submission.getServiceType()).isEqualTo("urn:e-codex:services:");
        assertThat(submission.getConversationId()).isEqualTo("conversation21");
    }


    @Test
    public void testTransformParties() {
        DomibusConnectorMessageType epoMessage = TransitionCreator.createEpoMessage();
        DomibusConnectorMessage connectorMessage = new DomibusConnectorMessage(epoMessage);

        Submission submission = submissionTransformer.transformToSubmission(connectorMessage);

        assertThat(submission.getToParties()).hasSize(1);
        Submission.Party toParty = submission.getToParties().stream().findFirst().get();
        assertThat(toParty.getPartyId()).isEqualTo("AT");
        assertThat(toParty.getPartyIdType()).isEqualTo("urn:oasis:names:tc:ebcore:partyid-type:iso3166-1");
        assertThat(submission.getToRole()).isEqualTo("GW");


        assertThat(submission.getFromParties()).hasSize(1);
        Submission.Party fromParty = submission.getFromParties().stream().findFirst().get();
        assertThat(fromParty.getPartyId()).isEqualTo("DE");
        assertThat(fromParty.getPartyIdType()).isEqualTo("urn:oasis:names:tc:ebcore:partyid-type:iso3166-1");
        assertThat(submission.getToRole()).isEqualTo("GW");


        assertThat(submission.getFromParties()).hasSize(1);
    }


}
