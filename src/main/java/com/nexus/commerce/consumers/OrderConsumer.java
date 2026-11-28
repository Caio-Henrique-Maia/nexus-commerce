package com.nexus.commerce.consumers;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {


    @RabbitListener(queues = "test-queue")
    public void listener(@Payload String message){
        System.out.println(message);
    }
}
