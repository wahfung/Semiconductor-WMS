package com.wms.controller;

import com.wms.dto.ApiResponse;
import com.wms.dto.DashboardDTO;
import com.wms.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public ApiResponse<DashboardDTO> getDashboardData() {
        return ApiResponse.success(dashboardService.getDashboardData());
    }
}
