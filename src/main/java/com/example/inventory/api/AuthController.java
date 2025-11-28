package com.example.inventory.api;

import com.example.inventory.api.dto.AuthDtos.LoginRequest;
import com.example.inventory.api.dto.AuthDtos.TokenResponse;
import com.example.inventory.model.Role;
import com.example.inventory.model.User;
import com.example.inventory.security.JwtService;
import com.example.inventory.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        String username = auth.getName();
        var userOpt = userService.findActiveByUsername(username);
        Role role = userOpt.map(User::getRole).orElse(Role.USER);
        String token = jwtService.generateToken(username, Map.of("role", role.name()));
        return ResponseEntity.ok(new TokenResponse(token));
    }

    // Simple bootstrap endpoint to create a super admin if none exist (optional)
    @PostMapping("/bootstrap-super-admin")
    public ResponseEntity<?> bootstrapAdmin(@RequestParam String username, @RequestParam String password) {
        userService.createUser(username, username+"@example.com", password, Role.SUPER_ADMIN);
        return ResponseEntity.ok().build();
    }
}
