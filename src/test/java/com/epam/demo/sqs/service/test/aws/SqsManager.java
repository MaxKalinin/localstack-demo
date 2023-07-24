package com.epam.demo.sqs.service.test.aws;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SqsManager {

    private final SqsClient sqsClient;

    public SqsManager(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void sendMessage(String queueName, String body) {
        log.info("Send message: {} to {} queue", body, queueName);
        sqsClient.sendMessage(message -> message.messageBody(body).queueUrl(toQueueUrl(queueName)));
    }

    public List<String> readSqsMessages(String queueName) {
        return sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                        .maxNumberOfMessages(10)
                        .queueUrl(toQueueUrl(queueName))
                        .build()).messages().stream()
                .map(Message::body)
                .collect(Collectors.toList());
    }

    private String toQueueUrl(String queueName) {
        return sqsClient.getQueueUrl(builder -> builder.queueName(queueName)).queueUrl();
    }
}
