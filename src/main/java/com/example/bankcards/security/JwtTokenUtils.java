package com.example.bankcards.security;

import com.example.bankcards.entity.UserModel;
import com.example.bankcards.exception.TokenAuthenticationException;
import com.example.bankcards.util.constants.ErrorMessages;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtTokenUtils {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration.access}")
    private Long accessTokenLifetime;
    @Value("${jwt.expiration.refresh}")
    private Long refreshTokenLifetime;

    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            throw new TokenAuthenticationException(ErrorMessages.TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new TokenAuthenticationException(ErrorMessages.INVALID_TOKEN);
        }
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public boolean isRefreshToken(String token) {
        return checkTypeToken(token, TokenType.REFRESH);
    }

    public String generateAccessToken(UserModel user) {
        return generateToken(user, TokenType.ACCESS);
    }

    public String generateRefreshToken(UserModel user) {
        return generateToken(user, TokenType.REFRESH);
    }

    private String generateToken(UserModel user, TokenType tokenType) {
        Date createdDate = new Date();
        Long tokenLifetime = tokenType.equals(TokenType.ACCESS) ?
                accessTokenLifetime
                : refreshTokenLifetime;
        Date expirationDate = new Date(createdDate.getTime() + tokenLifetime);

        JwtBuilder jwt = Jwts.builder()
                .issuedAt(createdDate)
                .expiration(expirationDate)
                .subject(user.getUsername())
                .claim("token_type", tokenType)
                .issuer("bank_cards")
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)));
        if (tokenType.equals(TokenType.ACCESS)) {
            jwt.claim("role", user.getRole().getName());
        }
        return jwt.compact();
    }

    private enum TokenType {
        ACCESS, REFRESH
    }

    private boolean checkTypeToken(String token, TokenType type) {
        String typeString = getClaimFromToken(token, claims -> claims.get("token_type", String.class));
        return TokenType.valueOf(typeString).equals(type);
    }

    private Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    private Claims getAllClaimsFromToken(String token) {
        return getJwtParser().parseSignedClaims(token).getPayload();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private JwtParser getJwtParser() {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));

        return Jwts.parser()
                .verifyWith(key)
                .build();
    }

}
