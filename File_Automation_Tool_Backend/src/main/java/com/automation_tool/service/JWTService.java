package com.automation_tool.service;

import com.automation_tool.dto.RefreshAndAccessTokenDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class JWTService {

    @Value("${secret.key}")
    private  String SECRET_KEY ;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RedisUtilService redisUtilService ;
    private final RedisTemplate<String, Object> redisTemplate;

    public JWTService(RedisUtilService redisUtilService, RedisTemplate<String, Object> redisTemplate) {
        this.redisUtilService = redisUtilService;
        this.redisTemplate = redisTemplate;
    }

    private String generateAccessToken(String email) {

        Map<String, String> claims = new HashMap<String, String>();
        claims.put("type", "access_token");

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+5 * 60 * 1000))
                .and()
                .signWith(getSecretKey())
                .compact();
    }

    private String generateRefreshToken(String email) {

        Map<String, String> claims = new HashMap<String, String>();
        claims.put("type", "refresh_token");

        return Jwts.builder()
            .claims()
            .add(claims)
            .subject(email)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis()+7 * 24 * 60 * 60 * 1000))
            .and()
            .signWith(getSecretKey())
            .compact();

    }

    public RefreshAndAccessTokenDTO generateToken(String email){

        String accessToken = generateAccessToken(email);
        String refreshToken = generateRefreshToken(email);
        String message ="Authenticated Successfully";
        return new RefreshAndAccessTokenDTO(accessToken, refreshToken,message);

    }

    private SecretKey getSecretKey() {

        byte[] KeyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(KeyBytes);
    }

    public String extractEmail(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        String tokenType = extractAllClaims(token).get("type", String.class);

        if (isBlacklisted(token)) {
            logger.warn("Token is blacklisted");
            return false;
        }

        return (userDetails.getUsername().equals(email) &&
                !isTokenExpired(token) &&
                !"refresh_token".equals(tokenType));

    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    public HashMap<String,String> generateAccessTokenWithRefreshToken(String refreshToken) {

        String email = extractEmail(refreshToken);
        String accessToken = generateAccessToken(email);
        return new HashMap<>(Map.of("accessToken",accessToken));
    }

    public boolean validateRefreshToken(String refreshToken, UserDetails userDetails) {

        final String email = extractEmail(refreshToken);
        String tokenType = extractAllClaims(refreshToken).get("type", String.class);

        if (isBlacklisted(refreshToken)) {
            logger.warn("Refresh Token is blacklisted");
            return false;
        }

        return (userDetails.getUsername().equals(email) &&
                !isTokenExpired(refreshToken) &&
                !"access_token".equals(tokenType));

    }

    public void blacklistToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            long ttl = expiration.getTime() - System.currentTimeMillis();
            logger.info("ttl {}",ttl);

            if (ttl > 0) {
                logger.info("condition satisfied");
                redisTemplate.opsForValue().set("blacklist:" + token, true, ttl, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            // Token may be invalid or expired already
            System.out.println("Failed to parse token for blacklisting: " + e.getMessage());
        }
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
    }


}
