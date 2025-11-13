package com.example.bankcards.util.components;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.stream.Collectors;

@Component
public class RandomNumberGenerator {
    private final SecureRandom random = new SecureRandom();

    public String generateNumericString(int numberSize) {
        if (numberSize <= 0) {
            throw new IllegalArgumentException("Number size must be positive");
        }

        return random.ints(numberSize, 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }
}
