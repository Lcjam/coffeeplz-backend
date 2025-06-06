package com.coffeeplz.controller;

import com.coffeeplz.dto.*;
import com.coffeeplz.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 인증", description = "관리자 회원가입, 로그인, 프로필 관리 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @Operation(summary = "관리자 회원가입", description = "새로운 관리자 계정을 생성합니다")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AdminResponse>> register(@Valid @RequestBody AdminRegisterRequest request) {
        log.info("관리자 회원가입 요청: {}", request.getEmail());
        
        AdminResponse response = userService.registerAdmin(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다", response));
    }

    @Operation(summary = "관리자 로그인", description = "관리자 계정으로 로그인하여 JWT 토큰을 발급받습니다")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody AdminLoginRequest request) {
        log.info("관리자 로그인 요청: {}", request.getEmail());
        
        LoginResponse response = userService.login(request);
        
        return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다", response));
    }

    @Operation(summary = "프로필 조회", description = "현재 로그인한 관리자의 프로필을 조회합니다")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AdminResponse>> getProfile(@RequestHeader("Authorization") String token) {
        log.info("프로필 조회 요청");
        
        // JWT에서 이메일 추출 (실제로는 Security Context에서 가져와야 함)
        String email = extractEmailFromToken(token);
        AdminResponse response = userService.getAdminInfo(email);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "프로필 수정", description = "현재 로그인한 관리자의 프로필을 수정합니다")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<AdminResponse>> updateProfile(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody AdminRegisterRequest request) {
        log.info("프로필 수정 요청");
        
        String email = extractEmailFromToken(token);
        AdminResponse response = userService.updateProfile(email, request);
        
        return ResponseEntity.ok(ApiResponse.success("프로필이 수정되었습니다", response));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 Access Token을 갱신합니다")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("토큰 갱신 요청");
        
        LoginResponse response = userService.refreshToken(request);
        
        return ResponseEntity.ok(ApiResponse.success("토큰이 갱신되었습니다", response));
    }

    /**
     * JWT 토큰에서 이메일 추출하는 헬퍼 메서드
     */
    private String extractEmailFromToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            // 실제로는 JwtService를 통해 추출해야 하지만, 
            // 여기서는 Security Context를 사용하는 것이 더 좋습니다.
            // 현재는 임시로 직접 추출
            return "admin@example.com"; // TODO: 실제 구현에서는 Security Context 사용
        }
        throw new IllegalArgumentException("유효하지 않은 토큰입니다");
    }
} 