package com.demo.sqs.service.controller;

import com.demo.sqs.service.service.MessageProcessingHelper;
import com.demo.sqs.service.service.SQSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import software.amazon.awssdk.services.sqs.model.Message;

@Slf4j
@Controller
public class SQSEventsController {
    private final SQSService sqsService;
    private final MessageProcessingHelper processingHelper;

    public SQSEventsController(SQSService sqsService, MessageProcessingHelper processingHelper) {
        this.sqsService = sqsService;
        this.processingHelper = processingHelper;
    }

    @Scheduled(fixedDelayString = "${spring.schedulers.sqs-poll-timeout}")
    public void pollSqsMessage() {
        log.debug("Start SQS polling for processing request");
        try {
            for (Message message : sqsService.receiveMessage()) {
                log.info("Received message: {}", message);
                sqsService.deleteMessage(message);
                String updatedMessage = processingHelper.processMessage(message.body());
                sqsService.sendMessage(updatedMessage);
            }
        } catch (Exception e) {
            log.error("Exception while polling message from SQS: {}", e.getMessage());
        }
    }
}
