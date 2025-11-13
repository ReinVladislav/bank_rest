package com.example.bankcards;

import com.example.bankcards.service.interfaces.card.CardOwnerService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class UserCardControllerConfig {
    @Bean
    @Primary
    public CardOwnerService cardOwnerService() {
        return Mockito.mock(CardOwnerService.class);
    }
}
