package com.coffeeplz.service;

import com.coffeeplz.dto.*;
import com.coffeeplz.entity.User;
import com.coffeeplz.entity.UserRole;
import com.coffeeplz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * 관리자 회원가입 (슈퍼관리자만 가능)
     */
    @Transactional
    public AdminResponse registerAdmin(AdminRegisterRequest request) {
        log.info("관리자 회원가입 시도: {}", request.getEmail());
        
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다");
        }

        // 관리자 계정 생성
        User admin = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhoneNumber())
                .role(UserRole.MANAGER) // 기본값: MANAGER
                .build();

        User savedAdmin = userRepository.save(admin);
        log.info("관리자 회원가입 완료: {}", savedAdmin.getEmail());

        return AdminResponse.builder()
                .id(savedAdmin.getId())
                .email(savedAdmin.getEmail())
                .name(savedAdmin.getName())
                .role(savedAdmin.getRole().name())
                .build();
    }

    /**
     * 관리자 로그인
     */
    public LoginResponse login(AdminLoginRequest request) {
        log.info("관리자 로그인 시도: {}", request.getEmail());

        // 인증 처리
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User admin = (User) authentication.getPrincipal();
        
        // 활성 상태 체크
        if (!admin.getIsActive()) {
            throw new IllegalArgumentException("비활성화된 계정입니다");
        }

        // JWT 토큰 생성
        String accessToken = jwtService.generateAccessToken(admin.getEmail());
        String refreshToken = jwtService.generateRefreshToken(admin.getEmail());

        log.info("관리자 로그인 성공: {}", admin.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    /**
     * 토큰 갱신
     */
    public LoginResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다");
        }

        String email = jwtService.extractUsername(refreshToken);
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        String newAccessToken = jwtService.generateAccessToken(email);
        String newRefreshToken = jwtService.generateRefreshToken(email);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .build();
    }

    /**
     * 관리자 정보 조회
     */
    public AdminResponse getAdminInfo(String email) {
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다"));

        return AdminResponse.builder()
                .id(admin.getId())
                .email(admin.getEmail())
                .name(admin.getName())
                .role(admin.getRole().name())
                .build();
    }

    /**
     * 관리자 프로필 수정
     */
    @Transactional
    public AdminResponse updateProfile(String email, AdminRegisterRequest request) {
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다"));

        admin.updateProfile(request.getName(), request.getPhoneNumber());
        
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            admin.updatePassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedAdmin = userRepository.save(admin);

        return AdminResponse.builder()
                .id(updatedAdmin.getId())
                .email(updatedAdmin.getEmail())
                .name(updatedAdmin.getName())
                .role(updatedAdmin.getRole().name())
                .build();
    }

    /**
     * 관리자 계정 비활성화
     */
    @Transactional
    public void deactivateAdmin(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다"));
        
        admin.deactivate();
        userRepository.save(admin);
        log.info("관리자 계정 비활성화: {}", admin.getEmail());
    }

    /**
     * 이메일로 사용자 조회 (Security용)
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
    }
} 