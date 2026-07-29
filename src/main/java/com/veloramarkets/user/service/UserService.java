package com.veloramarkets.user.service;

import com.veloramarkets.user.dto.UserResponse;
import com.veloramarkets.user.entity.User;
import com.veloramarkets.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.veloramarkets.user.dto.UpdateUserRequest;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getCurrentUser(
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found"
                        )
                );

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getCountry(),
                user.getRole().name(),
                user.isEmailVerified()
        );
    }

    public UserResponse updateCurrentUser(
            Authentication authentication,
            UpdateUserRequest request) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found"
                        )
                );

        user.setFullName(request.fullName());
        user.setUsername(request.username());
        user.setPhone(request.phone());
        user.setCountry(request.country());

        userRepository.save(user);

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getCountry(),
                user.getRole().name(),
                user.isEmailVerified()
        );
    }

}