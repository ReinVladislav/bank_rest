package com.example.bankcards;

import com.example.bankcards.service.interfaces.AuthenticationService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class AuthControllerConfig {
    @Bean
    @Primary
    public AuthenticationService authenticationService() {
        return Mockito.mock(AuthenticationService.class);
    }
}
