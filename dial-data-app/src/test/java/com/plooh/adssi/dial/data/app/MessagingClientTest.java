package com.plooh.adssi.dial.data.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

public class MessagingClientTest {

    private static final String URI_TPL = "http://localhost:8080/messages/{sender}/{recipient}/{messageId}";
    RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) throws Exception {
        MessagingClientTest client = new MessagingClientTest();
        client.testReadEmptyResult();
        client.testSetSingleEntry();
        client.testReadMessage();
        client.testDeleteSingleEntry();
    }
    
    private void testReadEmptyResult() throws UnsupportedEncodingException {
        String messageId = UUID.randomUUID().toString();
        String received = receive("+61-455-5248-21", "+43-664-5554-8121", messageId);
        assertNull(received);
    }

    private void testSetSingleEntry() throws Exception {
        String message = RandomStringUtils.randomAlphabetic(100);
        String messageId = UUID.randomUUID().toString();
		Boolean result = send("+61-455-5248-21", "+43-664-5554-8121", messageId, message);
        assertTrue(result);
    }

    private void testReadMessage() throws Exception {
        String sender = "+32-456-5558-15";
        String recipient = "+32-456-5554-98";
        String message = RandomStringUtils.randomAlphabetic(100);
        String messageId = UUID.randomUUID().toString();
		Boolean result = send(sender, recipient, messageId, message);
        assertTrue(result);
        String messageReceived = receive(sender, recipient, messageId);
        assertEquals(message, messageReceived);
    }

    public void testDeleteSingleEntry() throws Exception {
        String sender = "+1-416-5558-841";
        String recipient = "+1-416-5553-187";
        String message = RandomStringUtils.randomAlphabetic(100);
        String messageId = UUID.randomUUID().toString();
		Boolean result = send(sender, recipient, messageId, message);
        assertTrue(result);
        String messageReceived = receive(sender, recipient, messageId);
        assertEquals(message, messageReceived);
        Boolean deletedFlag = delete(sender, recipient, messageId);
        assertTrue(deletedFlag);
        String noMessageReceived = receive(sender, recipient, messageId);
        assertNull(noMessageReceived);
    }    

    private final URI uri(final String sender, final String recipient, final String messageId)
            throws UnsupportedEncodingException {
        UriTemplate uriTpl = new UriTemplate(URI_TPL);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sender", URLEncoder.encode(sender, "UTF-8"));
        parameters.put("recipient", URLEncoder.encode(recipient, "UTF-8"));
        parameters.put("messageId", URLEncoder.encode(messageId, "UTF-8"));
        return uriTpl.expand(parameters);
    }

    private String receive(final String sender, final String recipient, final String messageId)
            throws UnsupportedEncodingException {
        URI uri = uri(sender, recipient, messageId);
        ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
        int statusCode = result.getStatusCodeValue();
        assertEquals(200, statusCode);
        return result.getBody();
    }

    private Boolean send(final String sender, final String recipient, final String messageId, final String message) throws Exception {
        URI uri = uri(sender, recipient, messageId);
        ResponseEntity<Boolean> result = restTemplate.postForEntity(uri, message, Boolean.class);
        int statusCode = result.getStatusCodeValue();
        assertEquals(200, statusCode);
        return result.getBody();
    }

    private Boolean delete(final String sender, final String recipient, final String messageId) throws Exception {
        URI uri = uri(sender, recipient, messageId);
        ResponseEntity<Boolean> result = restTemplate.exchange(uri, HttpMethod.DELETE, null, Boolean.class);
        int statusCode = result.getStatusCodeValue();
        assertEquals(200, statusCode);
        return result.getBody();
    }
}