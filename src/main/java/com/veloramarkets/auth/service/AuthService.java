package com.veloramarkets.auth.service;

import com.veloramarkets.auth.dto.AuthResponse;
import com.veloramarkets.auth.dto.RegisterRequest;
import com.veloramarkets.common.exception.BadRequestException;
import com.veloramarkets.user.entity.Role;
import com.veloramarkets.user.entity.User;
import com.veloramarkets.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.veloramarkets.auth.dto.LoginRequest;
import com.veloramarkets.security.JwtService;
import com.veloramarkets.portfolio.entity.Portfolio;
import com.veloramarkets.portfolio.repository.PortfolioRepository;

import java.math.BigDecimal;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PortfolioRepository portfolioRepository;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            PortfolioRepository portfolioRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.portfolioRepository = portfolioRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String email = request.email().trim().toLowerCase();
        String username = request.username().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException(
                    "An account with this email already exists"
            );
        }

        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException(
                    "This username is already taken"
            );
        }

        User user = new User();

        user.setFullName(request.fullName().trim());
        user.setUsername(username);
        user.setEmail(email);

        user.setPassword(
                passwordEncoder.encode(request.password())
        );

        user.setPhone(request.phone());
        user.setCountry(request.country());
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);

        Portfolio portfolio = new Portfolio();

        portfolio.setUser(savedUser);
        portfolio.setCashBalance(new BigDecimal("100000.00"));

        portfolioRepository.save(portfolio);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                null,
                null,
                "Account created successfully"
        );
    }

    public AuthResponse login(LoginRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Invalid email or password"
                        )
                );

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword())) {

            throw new BadRequestException(
                    "Invalid email or password"
            );
        }

        if (!user.isEnabled()) {

            throw new BadRequestException(
                    "This account has been disabled"
            );
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                token,
                "Bearer",
                "Login successful"
        );
    }
}