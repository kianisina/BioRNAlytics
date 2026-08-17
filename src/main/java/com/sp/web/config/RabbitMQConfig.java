package com.sp.web.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String DESEQ2_QUEUE = "deseq2_queue";

    @Bean
    public Queue deseq2Queue() {
        // true means the queue survives server restarts (persistent)
        return new Queue(DESEQ2_QUEUE, true);
    }
}