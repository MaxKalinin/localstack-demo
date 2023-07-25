package com.demo.sqs.service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.List;

@Service
@Slf4j
public class SQSService {
    private final SqsClient sqsClient;
    private final String outputQueueUrl;
    private final String inputQueueUrl;
    private final ReceiveMessageRequest receiveMessageRequest;

    public SQSService(SqsClient sqsClient,
                      @Value("${spring.aws.sqs.input-queue}") String inputQueueName,
                      @Value("${spring.aws.sqs.output-queue}") String outputQueueName) {
        this.sqsClient = sqsClient;
        this.outputQueueUrl = toQueueUrl(outputQueueName);
        this.inputQueueUrl = toQueueUrl(inputQueueName);
        this.receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(inputQueueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(20)
                .build();
    }

    public void deleteMessage(Message message) {
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(inputQueueUrl)
                .receiptHandle(message.receiptHandle())
                .build();
        sqsClient.deleteMessage(deleteMessageRequest);
    }

    public List<Message> receiveMessage() {
        return sqsClient.receiveMessage(receiveMessageRequest).messages();
    }

    public void sendMessage(String message) {
        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(outputQueueUrl)
                .messageBody(message)
                .build());
    }

    private String toQueueUrl(String queueName) {
        return sqsClient.getQueueUrl(builder -> builder.queueName(queueName)).queueUrl();
    }
}
