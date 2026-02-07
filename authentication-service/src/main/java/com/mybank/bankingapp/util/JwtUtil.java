package com.mybank.bankingapp.util;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(String name, String email, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", name);
        claims.put("email", email);
        claims.put("roles", roles);

        return Jwts.builder()
                .claims(claims)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
}
