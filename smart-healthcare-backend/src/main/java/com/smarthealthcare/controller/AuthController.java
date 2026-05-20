package com.smarthealthcare.controller;

import com.smarthealthcare.common.Result;
import com.smarthealthcare.dto.LoginRequest;
import com.smarthealthcare.dto.LoginResponse;
import com.smarthealthcare.dto.RegisterRequest;
import com.smarthealthcare.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证接口", description = "登录、注册、登出")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @Operation(summary = "患者注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success("注册成功");
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestParam Long userId) {
        authService.logout(userId);
        return Result.success();
    }
}
