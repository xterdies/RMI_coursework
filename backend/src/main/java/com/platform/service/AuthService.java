package com.platform.service;

import com.platform.api.dto.AuthDtos;
import com.platform.api.dto.UserDto;
import com.platform.api.mapper.EntityMapper;
import com.platform.domain.entity.RefreshToken;
import com.platform.domain.entity.Role;
import com.platform.domain.entity.User;
import com.platform.domain.repository.RefreshTokenRepository;
import com.platform.domain.repository.RoleRepository;
import com.platform.domain.repository.UserRepository;
import com.platform.infrastructure.security.JwtService;
import com.platform.service.exception.ConflictException;
import com.platform.service.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EntityMapper mapper;
    private final AuditService auditService;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("USER role not found"));

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(userRole)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
        auditService.log(user, "REGISTER", "User", user.getId(), "New user registered");
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        auditService.log(user, "LOGIN", "User", user.getId(), "User logged in");
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthDtos.AuthResponse refresh(AuthDtos.RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new UnauthorizedException("Refresh token expired");
        }
        return buildAuthResponse(stored.getUser());
    }

    @Transactional
    public void logout(User user) {
        refreshTokenRepository.deleteByUser(user);
        auditService.log(user, "LOGOUT", "User", user.getId(), "User logged out");
    }

    private AuthDtos.AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        String refreshTokenValue = UUID.randomUUID().toString();
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .createdAt(LocalDateTime.now())
                .build());
        UserDto userDto = mapper.toUserDto(user);
        return AuthDtos.AuthResponse.of(accessToken, refreshTokenValue, expirationMs / 1000, userDto);
    }
    public UserDto getCurrentUserDto(User user) {
        User freshUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return mapper.toUserDto(freshUser);
    }

}
