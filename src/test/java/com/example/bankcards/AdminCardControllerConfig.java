package com.example.bankcards;

import com.example.bankcards.service.interfaces.card.CardAdminService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class AdminCardControllerConfig {
    @Bean
    @Primary
    public CardAdminService cardAdminService() {
        return Mockito.mock(CardAdminService.class);
    }
}