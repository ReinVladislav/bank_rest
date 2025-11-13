package com.example.bankcards.service.implementation;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.UserModel;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.interfaces.UserService;
import com.example.bankcards.util.constants.ErrorMessages;
import com.example.bankcards.util.enums.RoleType;
import com.example.bankcards.util.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserModel getOne(UUID id) {
        return userRepository.findByIdAndIsDeleted(id, false)
                .orElseThrow(() -> {
                    log.warn("User with id={} not found", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.USER_NOT_FOUND);
                });
    }

    @Override
    public Page<UserModel> getAll(String username, RoleType role, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page-1, size);

        if (username != null && !username.isBlank()) {
            return userRepository.findByRole_NameAndUsernameContainsAndIsDeleted(role.getValue(), username, false, pageRequest);
        } else {
            return userRepository.findByRole_NameAndIsDeleted(role.getValue(), false, pageRequest);
        }
    }

    @Override
    @Transactional
    public UserModel update(UUID id, String newUsername, String newEncodedPassword) {
        log.info("Updating user id={}", id);
        UserModel user = getOne(id);
        if (!newUsername.equals(user.getUsername()) && userRepository.existsByUsernameAndIsDeleted(newUsername, false)) {
            log.warn("Username '{}' already exists, cannot update user id={}", newUsername, id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.USERNAME_EXIST);
        } else {
            user.setUsername(newUsername);
        }
        if (newEncodedPassword != null) {
            user.setPassword(newEncodedPassword);
        }
        user.setRefreshToken(null);
        log.info("User id={} updated successfully", id);
        return userRepository.save(user);
    }

    @Override
    public void delete(UUID id) {
        log.info("Deleting user with id={}", id);
        UserModel user = getOne(id);
        user.setIsDeleted(true);
        user.setRefreshToken(null);
        userRepository.save(user);
    }
    @Transactional
    @Override
    public UserModel create(String username, String encryptedPassword, RoleType roleType) {
        log.info("Creating new user with username='{}' and role={}", username, roleType);
        if (userRepository.existsByUsernameAndIsDeleted(username, false)) {
            log.warn("Cannot create user. Username '{}' already exists", username);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.USERNAME_EXIST);
        }

        UserModel newUser = UserModel.builder()
                .username(username)
                .role(getRoleByType(roleType))
                .password(encryptedPassword)
                .build();
        return userRepository.save(newUser);
    }

    private Role getRoleByType(RoleType roleType) {
        Optional<Role> role = roleRepository.findByName(roleType.getValue());
        if (role.isEmpty()) {
            log.error("Unknown role type: {}", roleType.getValue());
            throw new IllegalArgumentException("Unknown role type");
        }
        return role.get();
    }


}
