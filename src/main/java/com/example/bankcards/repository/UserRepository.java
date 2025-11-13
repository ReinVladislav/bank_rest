package com.example.bankcards.repository;

import com.example.bankcards.entity.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserModel, UUID> {

    Optional<UserModel> findByUsernameAndIsDeleted(String username, Boolean isDeleted);

    boolean existsByUsernameAndIsDeleted(String username, Boolean isDeleted);

    Optional<UserModel> findByRefreshTokenAndIsDeleted(String refreshToken, Boolean isDeleted);

    Page<UserModel> findByRole_NameAndIsDeleted(String roleName, Boolean isDeleted, Pageable pageable);

    Page<UserModel> findByRole_NameAndUsernameContainsAndIsDeleted(String roleName, String username, Boolean isDeleted, Pageable pageable);

    Optional<UserModel> findByIdAndIsDeleted(UUID id, Boolean isDeleted);
}
