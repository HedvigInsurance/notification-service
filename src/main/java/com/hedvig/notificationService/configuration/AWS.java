package com.hedvig.notificationService.configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import lombok.val;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.support.destination.DynamicQueueUrlDestinationResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AWS {

    @Bean
    @Profile("development")
    public AmazonSQSAsync amazonSQS(AWSCredentialsProvider credentialsProvider) {
        val endpoint = "http://localhost:9324";
        val region = "elastcmq";
        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .build();
    }

    @Bean
    @Profile("development")
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(
            AmazonSQSAsync amazonSqs) {
        DynamicQueueUrlDestinationResolver dynamicQueueUrlDestinationResolver =
                new DynamicQueueUrlDestinationResolver(amazonSqs);
        dynamicQueueUrlDestinationResolver.setAutoCreate(true);

        SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory =
                new SimpleMessageListenerContainerFactory();
        simpleMessageListenerContainerFactory.setAmazonSqs(amazonSqs);
        simpleMessageListenerContainerFactory.setDestinationResolver(
                dynamicQueueUrlDestinationResolver);
        return simpleMessageListenerContainerFactory;
    }

    @Bean
    public AmazonSimpleEmailService amazonSimpleEmailService(
            AWSCredentialsProvider credentialsProvider) {
        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(Regions.EU_WEST_1)
                .build();
    }

    @Bean
    public QueueMessagingTemplate queueMessagingTemplate(AmazonSQSAsync amazonSqs) {
        return new QueueMessagingTemplate(amazonSqs);
    }
}
