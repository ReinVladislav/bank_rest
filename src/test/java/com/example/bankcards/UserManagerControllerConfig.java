package com.example.bankcards;

import com.example.bankcards.service.interfaces.UserManager;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class UserManagerControllerConfig {
    @Bean
    @Primary
    public UserManager userManager() {
        return Mockito.mock(UserManager.class);
    }
}