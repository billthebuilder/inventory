package com.example.inventory.api;

import com.example.inventory.model.Role;
import com.example.inventory.model.User;
import com.example.inventory.service.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public record CreateUserRequest(@NotBlank String username, String email, @NotBlank String password, Role role) {}

    @PostMapping
    public ResponseEntity<User> create(@RequestBody CreateUserRequest req) {
        Role role = req.role() == null ? Role.USER : req.role();
        User u = userService.createUser(req.username(), req.email(), req.password(), role);
        return ResponseEntity.ok(u);
    }

    @PostMapping("/{id}/blacklist")
    public ResponseEntity<Void> setBlacklist(@PathVariable Long id, @RequestParam boolean value) {
        boolean ok = userService.blacklistUser(id, value);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        boolean ok = userService.softDeleteUser(id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
