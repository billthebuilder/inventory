package com.example.inventory.service;

import com.example.inventory.model.Role;
import com.example.inventory.model.User;
import com.example.inventory.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(String username, String email, String rawPassword, Role role) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        return userRepository.save(u);
    }

    public Optional<User> findActiveByUsername(String username) {
        return userRepository.findByUsernameAndDeletedFalse(username).filter(u -> !u.isBlacklisted());
    }

    @Transactional
    public boolean blacklistUser(Long id, boolean blacklisted) {
        return userRepository.findById(id).filter(u -> !u.isDeleted()).map(u -> {
            u.setBlacklisted(blacklisted);
            userRepository.save(u);
            return true;
        }).orElse(false);
    }

    @Transactional
    public boolean softDeleteUser(Long id) {
        return userRepository.findById(id).filter(u -> !u.isDeleted()).map(u -> {
            u.setDeleted(true);
            userRepository.save(u);
            return true;
        }).orElse(false);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user.isBlacklisted()) {
            throw new UsernameNotFoundException("User blacklisted");
        }
        Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                authorities
        );
    }
}
