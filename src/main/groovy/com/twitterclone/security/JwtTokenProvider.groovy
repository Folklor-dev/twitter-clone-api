package com.twitterclone.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

import javax.crypto.SecretKey

@Component
class JwtTokenProvider {

    @Value('${app.jwtSecret}')
    private String jwtSecret

    @Value('${app.jwtExpirationInMs}')
    private int jwtExpirationInMs

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes())
    }

    String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal()

        Date now = new Date()
        Date expiration = new Date(now.getTime() + jwtExpirationInMs)

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact()
    }

    String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()

        return claims.getSubject()
    }

    boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
            return true
        } catch (Exception ex) {
            return false
        }
    }
}
