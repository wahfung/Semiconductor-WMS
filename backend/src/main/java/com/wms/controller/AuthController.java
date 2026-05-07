package com.wms.controller;

import com.wms.dto.ApiResponse;
import com.wms.dto.LoginRequest;
import com.wms.dto.LoginResponse;
import com.wms.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ApiResponse.success(response, "登录成功");
        } catch (Exception e) {
            return ApiResponse.error("用户名或密码错误");
        }
    }

    @GetMapping("/current")
    public ApiResponse<?> getCurrentUser() {
        try {
            return ApiResponse.success(authService.getCurrentUser());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
