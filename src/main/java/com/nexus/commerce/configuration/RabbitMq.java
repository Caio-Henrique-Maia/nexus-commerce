package com.nexus.commerce.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMq {

    private final String queueName = "test-queue";

    public Queue queue(){
        return new Queue(queueName, true);
    }
}
