package com.mybank.bankingapp.controller;

import com.mybank.bankingapp.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

//@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/bankingapp/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String email = (String) request.get("email");
        String password = (String) request.get("password");
       List<String> roles = (List<String>) request.get("roles");
        String token = authService.signup(name, email, password, roles);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String token = authService.login(email, password);
        return ResponseEntity.ok(token);
    }
}
