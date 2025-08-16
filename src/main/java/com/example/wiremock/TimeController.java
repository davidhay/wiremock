package com.example.wiremock;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;

@RestController
public class TimeController {
    
    private final Clock clock;

    public TimeController(Clock clock) {
        this.clock = clock;
    }

    @GetMapping("/time")
    public Time getCurrentTime() {
        return new Time(clock.instant());
    }
    
    record Time(Instant instant) {}
}
