package com.mybank.bankingapp.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class JwtFilter extends AbstractGatewayFilterFactory<JwtFilter.Config> {

    @Value("${jwt.secret}")
    private String secret;

    public JwtFilter() {
        super(Config.class);
    }


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
               return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
            }

            //validate token
            String token = authHeader.substring(7); // Remove "Bearer "

            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(secret)
                        .build()
                        .parseSignedClaims(token)
                        .getBody();

                //Extract roles
                List<String> roles = claims.get("roles", List.class);
                log.info("Roles: {}", roles);
                if(roles==null || roles.isEmpty())
                    return unauthorizedResponse(exchange, "No roles found in token");

                //Role based access control
                String path = exchange.getRequest().getURI().getPath();
                if(path.startsWith("/accounts")){
                    if (!roles.contains("ROLE_USER") && !roles.contains("ROLE_ADMIN")) {
                        return unauthorizedResponse(exchange, "Requires USER or ADMIN role");
                    }
                } else if (path.startsWith("/transactions")) {
                    // Allow USER or ADMIN
                    if (!roles.contains("ROLE_USER") && !roles.contains("ROLE_ADMIN")) {
                        return unauthorizedResponse(exchange, "Requires USER or ADMIN role");
                    }
                } else if (path.startsWith("/fundTransfers")) {
                    // Allow USER only
                    if (!roles.contains("ROLE_USER")) {
                        return unauthorizedResponse(exchange, "Requires USER role");
                    }
                } else if (path.startsWith("/notifications")) {
                    // Allow ADMIN only
                    if (!roles.contains("ROLE_ADMIN")) {
                        return unauthorizedResponse(exchange, "Requires ADMIN role");
                    }
                }

                return chain.filter(exchange); // Token valid, proceed
            } catch (JwtException e) {
                return unauthorizedResponse(exchange, "Invalid JWT token "+e.getMessage());
            }
        };
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message){
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String errorMsg = "{\"status\": \"error\", \"message\": \"" + message + "\"}";
        byte[] bytes = errorMsg.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)))
                .then(Mono.defer(response::setComplete));
    }


    public static class Config {}
}
