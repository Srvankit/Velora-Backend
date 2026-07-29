package com.veloramarkets.user.controller;

import com.veloramarkets.user.dto.UserResponse;
import com.veloramarkets.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.veloramarkets.user.dto.UpdateUserRequest;
import jakarta.validation.Valid;
import com.veloramarkets.user.dto.ChangePasswordRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication) {

        return ResponseEntity.ok(
                userService.getCurrentUser(authentication)
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(authentication, request);

        return ResponseEntity.ok("Password changed successfully.");
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequest request) {

        return ResponseEntity.ok(
                userService.updateCurrentUser(
                        authentication,
                        request
                )
        );
    }

}