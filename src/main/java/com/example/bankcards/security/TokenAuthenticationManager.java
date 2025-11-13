package com.example.bankcards.security;

import com.example.bankcards.exception.TokenAuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenAuthenticationManager implements AuthenticationManager {

    private final List<AuthenticationProvider> providers;

    @Override
    public Authentication authenticate(Authentication authentication)throws AuthenticationException {
        try {
            return providers.stream()
                    .filter(it -> it.supports(authentication.getClass()))
                    .findFirst()
                    .orElseThrow(() -> new TokenAuthenticationException("Not found provider for token"))
                    .authenticate(authentication);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new TokenAuthenticationException("Unexpected authentication error");
        }
    }
}
