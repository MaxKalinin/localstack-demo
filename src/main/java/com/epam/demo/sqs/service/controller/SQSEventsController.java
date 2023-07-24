package com.epam.demo.sqs.service.controller;

import com.epam.demo.sqs.service.service.MessageProcessingHelper;
import com.epam.demo.sqs.service.service.SQSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.model.Message;

@Service
@Slf4j
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
