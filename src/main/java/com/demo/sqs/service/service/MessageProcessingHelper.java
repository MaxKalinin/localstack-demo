package com.demo.sqs.service.service;


import org.springframework.stereotype.Component;

@Component
public class MessageProcessingHelper {

    private static final String MODIFICATION_POSTFIX = "_message_processed";

    public String processMessage(String message) {
        return message + MODIFICATION_POSTFIX;
    }
}
