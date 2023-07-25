package com.demo.sqs.service.test;

import com.demo.sqs.service.test.aws.SqsManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Slf4j
@ActiveProfiles("component-test")
@SpringBootTest
@TestInstance(PER_CLASS)
class ComponentTest {

    @Autowired
    private SqsManager sqsManager;
    @Value("${spring.aws.sqs.input-queue}")
    private String inputQueueName;
    @Value("${spring.aws.sqs.output-queue}")
    private String outputQueueName;

    private static final String INPUT_MESSAGE = "input_message";
    private static final String MODIFICATION_POSTFIX = "_message_processed";
    private static final int EXPECTED_MESSAGES_COUNT = 1;

    @Test
    void checkServiceProcessesInputMessage() {

        //send SQS message to the input queue
        sqsManager.sendMessage(inputQueueName, INPUT_MESSAGE);

        //check message appears in the output queue
        await().untilAsserted(() -> assertThat(sqsManager.readSqsMessages(outputQueueName))
                .as("Check message appears in the output queue")
                .hasSize(EXPECTED_MESSAGES_COUNT)
                .first()
                .as("Check message from output queue contains modification postfix")
                .isEqualTo(INPUT_MESSAGE + MODIFICATION_POSTFIX)
        );

        //check message was removed from input queue
        assertThat(sqsManager.readSqsMessages(inputQueueName))
                .as("Check message was removed from input queue")
                .isEmpty();
    }
}
