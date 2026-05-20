package com.smarthealthcare.service;

import com.smarthealthcare.dto.LoginRequest;
import com.smarthealthcare.dto.LoginResponse;
import com.smarthealthcare.dto.RegisterRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);

    void logout(Long userId);
}
