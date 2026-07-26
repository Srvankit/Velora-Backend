package com.veloramarkets.dashboard.controller;

import com.veloramarkets.dashboard.dto.DashboardResponse;
import com.veloramarkets.dashboard.service.DashboardService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse>
    getDashboard(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                dashboardService.getDashboard(
                        authentication
                )
        );
    }
}