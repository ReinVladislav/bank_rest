package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.UserModel;
import com.example.bankcards.util.enums.CardStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    Page<Card> findByOwnerAndIsDeleted(UserModel owner, Boolean isDeleted, Pageable pageable);

    Page<Card> findByBlockRequestedAndIsDeleted(Boolean blockRequested, Boolean isDeleted, Pageable pageable);

    boolean existsByEncryptNumber(String encryptNumber);

    Page<Card> findByIsDeleted(Boolean isDeleted, Pageable pageable);

    Optional<Card> findByIdAndIsDeleted(UUID id, Boolean isDeleted);

    List<Card> findByStatusNotAndExpirationDateBefore(CardStatus status, OffsetDateTime expirationDate);

    Optional<Card> findByIdAndOwnerAndIsDeleted(UUID id, UserModel owner, Boolean isDeleted);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Card c where c.id = ?1 and c.owner = ?2 and c.isDeleted = ?3")
    Optional<Card> findCardWithLock(UUID id, UserModel owner, Boolean isDeleted);


}
