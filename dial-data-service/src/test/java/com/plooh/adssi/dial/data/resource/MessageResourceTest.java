package com.plooh.adssi.dial.data.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

class MessageResourceTest extends AbstractResourceTest {

    @Test
    public void testNoMsg() throws Exception {
        String sender = "+61-455-5248-21";
        String recipient = "+43-664-5554-8121";
        String messageId = UUID.randomUUID().toString();
        checkNoMessage(sender, recipient, messageId);
    }


    @Test
    public void testReceiveMultiple() throws Exception {
        final String sender = "+61-455-5248-21";
        final String recipient = "+43-664-5554-8121";
        final String messageOneId = UUID.randomUUID().toString();
        final String messageOne = RandomStringUtils.randomAlphabetic(100);
        final String messageTwoId = UUID.randomUUID().toString();
        final String messageTwo = RandomStringUtils.randomAlphabetic(100);
        send(sender, recipient, messageOneId, messageOne);
        send(sender, recipient, messageTwoId, messageTwo);
        final Map<String, String> response = receiveAll(sender, recipient, List.of(messageOneId, messageTwoId));
        assertEquals(response.get(messageOneId), messageOne);
        assertEquals(response.get(messageTwoId), messageTwo);
    }

    @Test
    public void testSendMsg() throws Exception {
        String sender = "+61-455-5248-22";
        String recipient = "+43-664-5554-8122";
        String messageId = UUID.randomUUID().toString();
        String message = RandomStringUtils.randomAlphanumeric(100);
        final Boolean send = send(sender, recipient, messageId, message);
        assertTrue(send);
        String receivedMessage = receive(sender, recipient, messageId);
        assertEquals(message, receivedMessage);
    }

    @Test
    public void testDeleteMsg() throws Exception {
        String sender = "+61-455-5248-23";
        String recipient = "+43-664-5554-8123";
        String message = RandomStringUtils.randomAlphabetic(100);
        String messageId = UUID.randomUUID().toString();
		Boolean result = send(sender, recipient, messageId, message);
        assertTrue(result);
        String messageReceived = receive(sender, recipient, messageId);
        assertEquals(message, messageReceived);
        Boolean deletedFlag = delete(sender, recipient, messageId);
        assertTrue(deletedFlag);
        checkNoMessage(sender, recipient, messageId);
    }

    protected void checkNoMessage(final String sender, final String recipient, final String messageId) throws Exception {
        String received = receive(sender, recipient, messageId);
        assertEquals("", received);
    }
}