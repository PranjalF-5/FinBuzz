package com.mybank.bankingapp.service;

import com.mybank.bankingapp.model.User;
import com.mybank.bankingapp.repository.UserRepository;
import com.mybank.bankingapp.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public String signup(String name, String email, String password, List<String> roles){
        if(userRepository.findByEmail(email).isPresent()){
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);
        userRepository.save(user);

        return jwtUtil.generateToken(name,email,roles);
    }

    public String login(String email, String password){
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("User not found"));

        if(!passwordEncoder.matches(password,user.getPassword())){
            throw new RuntimeException("Incorrect password");
        }

        return jwtUtil.generateToken(user.getName(),email,user.getRoles());
    }
}
