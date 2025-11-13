package com.example.bankcards.security;

import com.example.bankcards.entity.UserModel;
import com.example.bankcards.exception.TokenAuthenticationException;
import com.example.bankcards.util.constants.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationProvider implements AuthenticationProvider {
    private final JwtTokenUtils jwtTokenUtils;
    private final UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof TokenAuthentication tokenAuthentication) {
            String token = tokenAuthentication.getToken();
            if (jwtTokenUtils.validateToken(token)) {
                UserDetails principal = getPrincipal(token);
                if (principal == null) {
                    throw new TokenAuthenticationException(ErrorMessages.INVALID_TOKEN);
                }
                tokenAuthentication.setPrincipal(principal);
                authentication.setAuthenticated(true);
                log.info("Token authentication successful for user: {}", principal.getUsername());
                return authentication;
            } else {
                throw new TokenAuthenticationException(ErrorMessages.INVALID_TOKEN);
            }
        } else {
            authentication.setAuthenticated(false);
            return authentication;
        }

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TokenAuthentication.class.equals(authentication);
    }

    public UserDetails getPrincipal(String token) {
        String username = jwtTokenUtils.getUsernameFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (userDetails == null || !userDetails.isEnabled() || jwtTokenUtils.isRefreshToken(token)) {
            return null;
        } else {
            return userDetails;
        }
    }
}
