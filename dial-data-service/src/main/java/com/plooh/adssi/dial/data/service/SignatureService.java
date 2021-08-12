package com.plooh.adssi.dial.data.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.Ed25519Verifier;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.util.Base64URL;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import com.plooh.adssi.dial.data.config.SecurityConfig;
import com.plooh.adssi.twindow.crypto.Ed25519VerificationKey2018Service;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Map;

@Service
public class SignatureService {

    private final SecurityConfig securityConfig;

    public SignatureService(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    public boolean verifySignature(Verification verification, String key, String providedSignature) {
        OctetKeyPair signingKey = Ed25519VerificationKey2018Service.publicKeyFromBase58(key, "dummy");
        var jwsHeader = new JWSHeader(securityConfig.getAlgorithm());
        var jwsPayload = new Payload(
                Map.of(
                        "senderId", verification.getSender(),
                        "recipientId", verification.getRecipient(),
                        "messageId", verification.getMessageId(),
                        "timestamp", verification.getTimestamp(),
                        "payload", verification.getPayload()
                )
        );

        try {
            return new JWSObject(jwsHeader.toBase64URL(), jwsPayload, Base64URL.from(providedSignature)).verify(new Ed25519Verifier(signingKey));
        } catch (JOSEException| ParseException ex) {
            throw new IllegalArgumentException();
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class Verification {
        private final String sender;
        private final String recipient;
        private final String messageId;
        private final String timestamp;
        private final String payload;
    }
}
