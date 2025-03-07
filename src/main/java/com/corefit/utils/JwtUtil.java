package com.corefit.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "4T2a0VS7DC6FuXfEBKGqM/p/4Xb0tIQjJLFFKdMP6AY=";
    private static final long EXPIRATION_TIME = 30L * 24 * 60 * 60 * 1000;
    private final Log logger = LogFactory.getLog(this.getClass());

    public String generateToken(long id) {
        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().after(new Date());

        } catch (ExpiredJwtException e) {
            logger.error("Token expired: " + e.getMessage());

        } catch (SignatureException e) {
            logger.error("Invalid signature: " + e.getMessage());

        } catch (MalformedJwtException e) {
            logger.error("Malformed token: " + e.getMessage());

        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported token: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            logger.error("Empty or null token: " + e.getMessage());
        }
        return false;
    }

    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}