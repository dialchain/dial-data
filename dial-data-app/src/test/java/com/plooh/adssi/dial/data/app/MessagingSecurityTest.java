package com.plooh.adssi.dial.data.app;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.plooh.adssi.twindow.crypto.Common25519Service;
import com.plooh.adssi.twindow.crypto.Ed25519VerificationKey2018Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static com.plooh.adssi.dial.data.resource.MessageResource.PROOF_OF_POSSESSION_HEADER;
import static com.plooh.adssi.dial.data.resource.MessageResource.PROOF_OF_POSSESSION_TIMESTAMP_HEADER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(classes = MessagingApplication.class)
@ActiveProfiles({"security", "test"})
public class MessagingSecurityTest {

    private static final String BASE_PATH = "/messages/";

    private final OctetKeyPair senderKey = Ed25519VerificationKey2018Service.generateKeyPair("k1");
    private final OctetKeyPair recipientKey = Ed25519VerificationKey2018Service.generateKeyPair("k2");
    private final Common25519Service commonSvc = new Common25519Service(Curve.Ed25519);
    private final String senderPub58 = commonSvc.publicKeyBase58(senderKey);
    private final String recipientPub58 = commonSvc.publicKeyBase58(recipientKey);
    private final String messageId = UUID.randomUUID().toString();
    private final String payload = UUID.randomUUID().toString();

    @Autowired
    private MockMvc mvc;

    @Test
    void testSendWithOkSignature() throws Exception {
        var time = Instant.now().toString();
        doPost(
                String.format("%s/%s/%s", senderPub58, recipientPub58, messageId),
                signature(senderPub58, recipientPub58, messageId, time, payload, senderKey),
                time,
                payload
        ).andExpect(status().isOk());
    }

    @Test
    void testSendWithNokSignature() throws Exception {
        var time = Instant.now().toString();
        doPost(
                String.format("%s/%s/%s", senderPub58, recipientPub58, messageId),
                signature(senderPub58, recipientPub58, messageId, time, payload, senderKey) + "TAMPER",
                time,
                payload
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void testSendWithOutdatedSignature() throws Exception {
        var time = Instant.now().minus(Duration.ofDays(10)).toString();
        doPost(
                String.format("%s/%s/%s", senderPub58, recipientPub58, messageId),
                signature(senderPub58, recipientPub58, messageId, time, payload, senderKey),
                time,
                payload
        ).andExpect(status().isForbidden());
    }

    @Test
    void testReceiveWithOkSignature() throws Exception {
        testSendWithOkSignature();
        var time = Instant.now().toString();
        doGet(
                String.format("%s/%s/%s", senderPub58, recipientPub58, messageId),
                signature(senderPub58, recipientPub58, messageId, time, "", recipientKey),
                time
        ).andExpect(status().isOk()).andExpect(content().string(payload));
    }

    @Test
    void testReceiveWithNokSignature() throws Exception {
        testSendWithOkSignature();
        var time = Instant.now().toString();
        doGet(
                String.format("%s/%s/%s", senderPub58, recipientPub58, messageId),
                signature(senderPub58, recipientPub58, messageId, time, "", recipientKey) + "TAMPER",
                time
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void testReceiveWithOutdatedSignature() throws Exception {
        testSendWithOkSignature();
        var time = Instant.now().minus(Duration.ofDays(10)).toString();
        doGet(
                String.format("%s/%s/%s", senderPub58, recipientPub58, messageId),
                signature(senderPub58, recipientPub58, messageId, time, "", recipientKey),
                time
        ).andExpect(status().isForbidden());
    }

    @Test
    void testReceiveMultiWithOkSignature() throws Exception {
        testSendWithOkSignature();
        var time = Instant.now().toString();
        doGet(
                String.format("%s/%s/multiple/%s,%s", senderPub58, recipientPub58, messageId, messageId),
                signature(senderPub58, recipientPub58, messageId, time, "", recipientKey)
                        + "," + signature(senderPub58, recipientPub58, messageId, time, "", recipientKey),
                time
        ).andExpect(status().isOk()).andExpect(jsonPath(messageId).value(payload));
    }

    @Test
    void testReceiveMultiWithNokSignature() throws Exception {
        testSendWithOkSignature();
        var time = Instant.now().toString();
        doGet(
                String.format("%s/%s/multiple/%s,%s", senderPub58, recipientPub58, messageId, messageId),
                signature(senderPub58, recipientPub58, messageId, time, "", recipientKey) + "TAMPER"
                        + "," + signature(senderPub58, recipientPub58, messageId, time, "", recipientKey),
                time
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void testReceiveMultiWithOutdatedSignature() throws Exception {
        testSendWithOkSignature();
        var time = Instant.now().minus(Duration.ofDays(10)).toString();
        doGet(
                String.format("%s/%s/multiple/%s,%s", senderPub58, recipientPub58, messageId, messageId),
                signature(senderPub58, recipientPub58, messageId, time, "", recipientKey)
                        + "," + signature(senderPub58, recipientPub58, messageId, time, "", recipientKey),
                time
        ).andExpect(status().isForbidden());
    }

    @Test
    void testDeleteWithOkSignature() throws Exception {
        testSendWithOkSignature();
        var time = Instant.now().toString();
        doDelete(
                String.format("%s/%s/%s", senderPub58, recipientPub58, messageId),
                signature(senderPub58, recipientPub58, messageId, time, "", recipientKey),
                time
        ).andExpect(status().isOk());
    }

    @Test
    void testDeleteWithNokSignature() throws Exception {
        testSendWithOkSignature();
        var time = Instant.now().toString();
        doDelete(
                String.format("%s/%s/%s", senderPub58, recipientPub58, messageId),
                signature(senderPub58, recipientPub58, messageId, time, "", recipientKey) + "TAMPER",
                time
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteWithOutdatedSignature() throws Exception {
        testSendWithOkSignature();
        var time = Instant.now().minus(Duration.ofDays(10)).toString();
        doDelete(
                String.format("%s/%s/%s", senderPub58, recipientPub58, messageId),
                signature(senderPub58, recipientPub58, messageId, time, "", recipientKey),
                time
        ).andExpect(status().isForbidden());
    }

    private ResultActions doGet(String path, String signature, String timestamp) throws Exception {
        return mvc.perform(
                get(BASE_PATH + path)
                        .header(PROOF_OF_POSSESSION_HEADER, signature)
                        .header(PROOF_OF_POSSESSION_TIMESTAMP_HEADER, timestamp)
        );
    }

    private ResultActions doPost(String path, String signature, String timestamp, String payload) throws Exception {
        return mvc.perform(
                post(BASE_PATH + path)
                        .header(PROOF_OF_POSSESSION_HEADER, signature)
                        .header(PROOF_OF_POSSESSION_TIMESTAMP_HEADER, timestamp)
                        .content(payload)
        );
    }

    private ResultActions doDelete(String path, String signature, String timestamp) throws Exception {
        return mvc.perform(
                delete(BASE_PATH + path)
                        .header(PROOF_OF_POSSESSION_HEADER, signature)
                        .header(PROOF_OF_POSSESSION_TIMESTAMP_HEADER, timestamp)
        );
    }

    private String signature(String senderPubKey, String recipientPubKey, String messageId, String timestamp, String payload, OctetKeyPair key) throws Exception {
        var jwsHeader = new JWSHeader(JWSAlgorithm.EdDSA);
        var jwsPayload = new Payload(
                Map.of(
                        "senderId", senderPubKey,
                        "recipientId", recipientPubKey,
                        "messageId", messageId,
                        "timestamp", timestamp,
                        "payload", payload
                )
        );

        var jwe = new JWSObject(jwsHeader, jwsPayload);
        jwe.sign(new Ed25519Signer(key));
        return jwe.getSignature().toString();
    }
}
