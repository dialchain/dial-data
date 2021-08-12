package com.plooh.adssi.dial.data.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.plooh.adssi.dial.data.MessagingApplicationTests;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebAppConfiguration
@AutoConfigureMockMvc
public abstract class AbstractResourceTest extends MessagingApplicationTests {

    protected MockMvc mvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    public void cleanup() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    protected String receive(final String sender, final String recipient, final String messageId) throws Exception {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get("/messages/{sender}/{recipient}/{messageId}", sender, recipient, messageId);
        final MvcResult mvcResult = mvc.perform(req).andReturn();
        assertNotNull(mvcResult);
        final int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);
        final String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return contentAsString;
    }

    protected Map<String, String> receiveAll(final String sender, final String recipient, final List<String> messageIds) throws Exception {
        final String formattedMessageIds = String.join(",", messageIds);
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get("/messages/{sender}/{recipient}/multiple/{messageIds}", sender, recipient, formattedMessageIds);
        final MvcResult mvcResult = mvc.perform(req).andReturn();
        assertNotNull(mvcResult);
        final int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);
        final String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readValue(contentAsString,  new TypeReference<Map<String, String>>(){});
    }

    protected Boolean send(final String sender, final String recipient, final String messageId, final String message) throws Exception {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders
            .post("/messages/{sender}/{recipient}/{messageId}", sender, recipient, messageId)
            .content(message);
        final MvcResult mvcResult = mvc.perform(req).andReturn();
        final int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);
        final String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return Boolean.valueOf(contentAsString);
    }

    protected Boolean delete(final String sender, final String recipient, final String messageId) throws Exception {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.delete("/messages/{sender}/{recipient}/{messageId}", sender, recipient, messageId);
        final MvcResult mvcResult = mvc.perform(req).andReturn();
        assertNotNull(mvcResult);
        final int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);
        final String contentAsString = mvcResult.getResponse().getContentAsString();
        return Boolean.valueOf(contentAsString);
    }
}
