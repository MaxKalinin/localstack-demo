package com.demo.sqs.service.test.config;

import com.demo.sqs.service.test.aws.SqsManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

import java.util.List;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@Slf4j
@Configuration
public class TestConfig {

    private static final LocalStackContainer.Service[] REQUIRED_SERVICES = {
            LocalStackContainer.Service.SQS
    };

    @Bean
    LocalStackContainer localStackContainer() {
        LocalStackContainer localStackContainer = new LocalStackContainer(
                DockerImageName.parse("localstack/localstack:1.3.1"))
                .withServices(REQUIRED_SERVICES);
        localStackContainer.start();
        return localStackContainer;
    }

    @Bean
    StaticCredentialsProvider credentialsProvider(LocalStackContainer localStackContainer) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(localStackContainer.getAccessKey(), localStackContainer.getSecretKey())
        );
    }

    @Bean
    public SqsClient sqsClient(LocalStackContainer localStackContainer,
                               @Autowired StaticCredentialsProvider credentialsProvider,
                               @Value("${spring.aws.sqs.input-queue}") String inputQueueName,
                               @Value("${spring.aws.sqs.output-queue}") String outputQueueName) {
        SqsClient sqsClient = SqsClient.builder()
                .endpointOverride(localStackContainer.getEndpointOverride(SQS))
                .credentialsProvider(credentialsProvider)
                .build();
        List.of(inputQueueName, outputQueueName).forEach(q -> createSQSQueue(sqsClient, q));
        return sqsClient;
    }

    @Bean
    public SqsManager sqsManager(SqsClient sqsClient) {
        return new SqsManager(sqsClient);
    }

    private void createSQSQueue(SqsClient sqsClient, String queueName) {
        sqsClient.createQueue(CreateQueueRequest.builder().queueName(queueName).build());
    }
}
