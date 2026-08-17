package com.sp.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Wird bei programmstart ausgeführt.
 */
@SpringBootApplication
@EnableMongoRepositories
@SuppressWarnings("hideutilityclassconstructor")
public class TemplateApplication {
    
    /**
     * Wird bei programmstart ausgeführt.
     * @param args bla
     */
    public static void main(final String[] args) {
        SpringApplication.run(TemplateApplication.class, args);
    }
}