package com.example.wiremock;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {
    
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
