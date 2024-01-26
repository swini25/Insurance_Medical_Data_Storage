package com.appdeveloperblog.app.ws.utils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.appdeveloperblog.app.ws.models.JwtKeys;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtUtil {

    private final JwtKeys jwtKeys;

    public JwtUtil(JwtKeys jwtKeys) {
        this.jwtKeys = jwtKeys;
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(jwtKeys.getPublicKey()).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken() {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims);
    }

    private String createToken(Map<String, Object> claims) {
        PrivateKey privateKey = convertToPrivateKey(jwtKeys.getPrivateKey());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    private PrivateKey convertToPrivateKey(String privateKeyString) {
        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString.getBytes(StandardCharsets.UTF_8));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert private key string to PrivateKey", e);
        }
    }
}
