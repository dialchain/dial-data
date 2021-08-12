package com.plooh.adssi.dial.data.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.plooh.adssi.dial.data.MessagingApplicationTests;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MessageServiceTest extends MessagingApplicationTests {

    @Autowired
    private MessageService messageService;

    @Test
    public void testSetSingleEntry() {
        String message = RandomStringUtils.randomAlphabetic(100);
        String messageId = UUID.randomUUID().toString();
		Boolean result = messageService.set("+61-455-5248-21", "+43-664-5554-8121", messageId, message);
        assertTrue(result);
    }

    @Test
    public void testReadMessage() {
        String sender = "+32-456-5558-15";
        String recipient = "+32-456-5554-98";
        String message = RandomStringUtils.randomAlphabetic(100);
        String messageId = UUID.randomUUID().toString();
		Boolean result = messageService.set(sender, recipient, messageId, message);
        assertTrue(result);
        String messageReceived = messageService.get(sender, recipient, messageId);
        assertEquals(message, messageReceived);
    }

    @Test
    public void testReadMultipleMessages() {
        String sender = "+32-456-5558-15";
        String recipient = "+32-456-5554-98";
        String messageOne = RandomStringUtils.randomAlphabetic(100);
        String messageTwo = RandomStringUtils.randomAlphabetic(100);
        String messageOneId = UUID.randomUUID().toString();
        String messageTwoId = UUID.randomUUID().toString();
		messageService.set(sender, recipient, messageOneId, messageOne);
        messageService.set(sender, recipient, messageTwoId, messageTwo);

        Map<String, String> expectedMessages = new HashMap<>();
        expectedMessages.put(messageOneId, messageOne);
        expectedMessages.put(messageTwoId, messageTwo);

        Map<String, String> receivedMessages = messageService.get(sender, recipient, List.of(messageOneId, messageTwoId));
        assertEquals(receivedMessages, expectedMessages);
    }

    @Test
    public void testDeleteSingleEntry() {
        String sender = "+1-416-5558-841";
        String recipient = "+1-416-5553-187";
        String message = RandomStringUtils.randomAlphabetic(100);
        String messageId = UUID.randomUUID().toString();
		Boolean result = messageService.set(sender, recipient, messageId, message);
        assertTrue(result);
        String messageReceived = messageService.get(sender, recipient, messageId);
        assertEquals(message, messageReceived);
        Boolean deletedFlag = messageService.delete(sender, recipient, messageId);
        assertTrue(deletedFlag);
        String noMessageReceived = messageService.get(sender, recipient, messageId);
        assertNull(noMessageReceived);
    }
}