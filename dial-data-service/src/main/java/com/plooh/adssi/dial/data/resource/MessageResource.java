package com.plooh.adssi.dial.data.resource;

import com.plooh.adssi.dial.data.service.MessageService;
import com.plooh.adssi.dial.data.service.SecurityService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@RestController
public class MessageResource {
    public static final String PROOF_OF_POSSESSION_HEADER = "X-POP";
    public static final String PROOF_OF_POSSESSION_TIMESTAMP_HEADER = "X-POP-TIMESTAMP";

    private final MessageService messageService;
    private final SecurityService securityService;

    public MessageResource(MessageService messageService, SecurityService securityService) {
        this.messageService = messageService;
        this.securityService = securityService;
    }

    @GetMapping("/messages/{sender}/{recipient}/{messageId}")
    public String receive(
        final @PathVariable("sender") String sender,
        final @PathVariable("recipient") String recipient,
        final @PathVariable("messageId") String messageId,
        final @RequestHeader(value = PROOF_OF_POSSESSION_HEADER, required = false) String recipientPopHeader,
        final @RequestHeader(value = PROOF_OF_POSSESSION_TIMESTAMP_HEADER, required = false) Instant popTimestamp) {
        securityService.assertSignature(recipientPopHeader, sender, recipient, messageId, popTimestamp, recipient);
        return messageService.get(sender, recipient, messageId);
    }

    @GetMapping("/messages/{sender}/{recipient}/multiple/{messageIds}")
    public Map<String, String> receive(
        final @PathVariable("sender") String sender,
        final @PathVariable("recipient") String recipient,
        final @PathVariable("messageIds") String[] messageIds,
        final @RequestHeader(value = PROOF_OF_POSSESSION_HEADER,required = false) String recipientPopHeaders,
        final @RequestHeader(value = PROOF_OF_POSSESSION_TIMESTAMP_HEADER, required = false) Instant popTimestamp) {
        var signatures = recipientPopHeaders.split(",");
        IntStream.range(0, messageIds.length)
            .forEach(pos -> securityService.assertSignature(signatures[pos], sender, recipient, messageIds[pos], popTimestamp, recipient));
        return messageService.get(sender, recipient, List.of(messageIds));
    }

    @PostMapping("/messages/{sender}/{recipient}/{messageId}")
    public Boolean send(
        final @PathVariable("sender") String sender,
        final @PathVariable("recipient") String recipient,
        final @PathVariable("messageId") String messageId,
        final @RequestBody String message,
        final @RequestHeader(value = PROOF_OF_POSSESSION_HEADER,required = false) String senderPopHeader,
        final @RequestHeader(value = PROOF_OF_POSSESSION_TIMESTAMP_HEADER,required = false) Instant popTimestamp) {
        securityService.assertSignature(senderPopHeader, sender, recipient, messageId, popTimestamp, message, sender);
        return messageService.set(sender, recipient, messageId, message);
    }

    @DeleteMapping("/messages/{sender}/{recipient}/{messageId}")
    public Boolean delete(
        final @PathVariable("sender") String sender,
        final @PathVariable("recipient") String recipient,
        final @PathVariable("messageId") String messageId,
        final @RequestHeader(value = PROOF_OF_POSSESSION_HEADER,required = false) String recipientPopHeader,
        final @RequestHeader(value = PROOF_OF_POSSESSION_TIMESTAMP_HEADER,required = false) Instant popTimestamp) {
        securityService.assertSignature(recipientPopHeader, sender, recipient, messageId, popTimestamp, recipient);
        return messageService.delete(sender, recipient, messageId);
    }
}
