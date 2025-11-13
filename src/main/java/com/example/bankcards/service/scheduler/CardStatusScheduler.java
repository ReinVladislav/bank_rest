package com.example.bankcards.service.scheduler;

import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.enums.CardStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardStatusScheduler {

    private final CardRepository cardRepository;

    @Scheduled(cron = "0 0 0 * * *") // каждый день в полночь
    public void markExpiredCards() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<Card> expiredCards = cardRepository.findByStatusNotAndExpirationDateBefore(CardStatus.EXPIRED, now);
        expiredCards.forEach(card -> card.setStatus(CardStatus.EXPIRED));
        cardRepository.saveAll(expiredCards);
        log.info("{} cards have expired", expiredCards.size());
    }

}
