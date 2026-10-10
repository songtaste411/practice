package com.clinic.identity.web;

import com.clinic.identity.repo.UserRepository;
import com.clinic.identity.security.JwtService;
import com.clinic.shared.kernel.BizException;
import com.clinic.shared.kernel.ErrorCode;
import com.clinic.shared.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录。账号不存在和密码错误返回同一句话，避免被人拿接口探账号。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwtService) {
        this.users = users;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResult> login(@Valid @RequestBody LoginRequest request) {
        var user = users.findByUsername(request.username().trim())
                .filter(found -> encoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new BizException(ErrorCode.UNAUTHORIZED, "账号或密码不对"));
        var token = jwtService.issue(user.getUsername(), user.getRoleCode());
        return ApiResponse.ok(new LoginResult(token, user.getUsername(), user.getDisplayName()));
    }

    public record LoginResult(String token, String username, String displayName) {
    }
}
