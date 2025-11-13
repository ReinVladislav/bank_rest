package com.example.bankcards.entity;

import com.example.bankcards.util.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "card")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Card {
    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "encrypt_number", unique = true, nullable = false)
    private String encryptNumber;

    @ManyToOne
    @JoinColumn(name = "owner", nullable = false)
    private UserModel owner;

    @Column(name = "expiration_date", nullable = false)
    @Builder.Default
    private OffsetDateTime expirationDate = OffsetDateTime.now(ZoneOffset.UTC)
            .plusYears(1)
            .truncatedTo(ChronoUnit.DAYS);// Истекает через год, оклугляю до начала следующих суток

    @Column(name = "balance", nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.valueOf(0);

    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    @Builder.Default
    private CardStatus status = CardStatus.ACTIVE;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "block_requested")
    @Builder.Default
    private Boolean blockRequested = false;

}
