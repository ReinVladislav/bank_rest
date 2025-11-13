package com.example.bankcards.service.implementation;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.UserCreateRequest;
import com.example.bankcards.dto.response.AuthResponse;
import com.example.bankcards.entity.UserModel;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtTokenUtils;
import com.example.bankcards.service.interfaces.AuthenticationService;
import com.example.bankcards.service.interfaces.UserCreator;
import com.example.bankcards.util.constants.ErrorMessages;
import com.example.bankcards.util.enums.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserCreator userCreator;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    @Transactional
    public AuthResponse register(UserCreateRequest request) {
        log.info("Attempting to register new user: {}", request.getUsername());

        if (!request.getPassword().equals(request.getRepeatPassword())) {
            log.warn("Password mismatch during registration for username: {}", request.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.PASSWORDS_DO_NOT_MATCH);
        }

        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        UserModel newUser = userCreator.create(
                request.getUsername(),
                encryptedPassword,
                RoleType.USER
        );
        newUser.setRefreshToken(
                jwtTokenUtils.generateRefreshToken(newUser)
        );
        userRepository.save(newUser);
        log.info("User '{}' registered successfully", newUser.getUsername());
        return createAuthResponse(newUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("User '{}' attempting to log in", request.getUsername());

        Optional<UserModel> userOpt = userRepository.findByUsernameAndIsDeleted(request.getUsername(), false);
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            log.warn("Invalid login attempt for username: {}", request.getUsername());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorMessages.INVALID_LOGIN_OR_PASSWORD);
        } else {
            UserModel user = userOpt.get();
            user.setRefreshToken(
                    jwtTokenUtils.generateRefreshToken(user)
            );
            userRepository.save(user);
            log.info("User '{}' logged in successfully", user.getUsername());
            return createAuthResponse(user);
        }
    }

    @Override
    public void logout(String refreshToken) {
        log.debug("Processing logout request...");
        UserModel user = userRepository.findByRefreshTokenAndIsDeleted(refreshToken, false)
                .orElseThrow(() -> {
                    log.warn("Logout attempt with invalid refresh token");
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorMessages.INVALID_REFRESH_TOKEN);
                });
        user.setRefreshToken(null);
        userRepository.save(user);
        log.info("User '{}' logged out successfully", user.getUsername());
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        log.debug("Refreshing tokens...");
        UserModel user = userRepository.findByRefreshTokenAndIsDeleted(refreshToken, false)
                .orElseThrow(() -> {
                    log.warn("Token refresh failed: invalid refresh token");
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorMessages.INVALID_REFRESH_TOKEN);
                });
        user.setRefreshToken(
                jwtTokenUtils.generateRefreshToken(user)
        );
        userRepository.save(user);
        log.info("Tokens refreshed successfully for user '{}'", user.getUsername());
        return createAuthResponse(user);
    }

    private AuthResponse createAuthResponse(UserModel user) {
        return AuthResponse.builder()
                .accessToken(jwtTokenUtils.generateAccessToken(user))
                .refreshToken(user.getRefreshToken())
                .build();
    }
}
