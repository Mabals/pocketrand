package io.github.mabals.pocketrand.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import io.github.mabals.pocketrand.model.User;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final long expiryHours;

    public TokenService(JwtEncoder jwtEncoder, @Value("${pocketrand.jwt.expiry-hours}") long expiryHours) {
        this.jwtEncoder = jwtEncoder;
        this.expiryHours = expiryHours;
    }

    public String createToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("pocketrand")
                .issuedAt(now)
                .expiresAt(now.plus(expiryHours, ChronoUnit.HOURS))
                .subject(String.valueOf(user.getId()))
                .claim("name", user.getFullName())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getExpirySeconds() {
        return expiryHours * 3600;
    }
}