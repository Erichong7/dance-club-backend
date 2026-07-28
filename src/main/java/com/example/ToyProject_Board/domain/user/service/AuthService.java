package com.example.ToyProject_Board.domain.user.service;

import com.example.ToyProject_Board.domain.user.SignupStatus;
import com.example.ToyProject_Board.domain.user.User;
import com.example.ToyProject_Board.domain.user.UserRole;
import com.example.ToyProject_Board.domain.user.dto.request.LoginRequest;
import com.example.ToyProject_Board.domain.user.dto.request.SignupRequest;
import com.example.ToyProject_Board.domain.user.dto.response.SignupRequestListResponse;
import com.example.ToyProject_Board.domain.user.dto.response.TokenResponse;
import com.example.ToyProject_Board.domain.user.repository.UserRepository;
import com.example.ToyProject_Board.global.exception.BusinessException;
import com.example.ToyProject_Board.global.exception.ErrorCode;
import com.example.ToyProject_Board.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 회원가입
    @Transactional
    public void signup(SignupRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATE);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .phoneNumber(request.getPhoneNumber())
                .signupStatus(SignupStatus.REQUESTED)
                .build();

        userRepository.save(user);
    }

    // 로그인
    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if(user.getSignupStatus() == SignupStatus.REJECTED) {
            throw new BusinessException(ErrorCode.SIGNUP_REJECTED);
        }

        if(user.getSignupStatus() == SignupStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.SIGNUP_PENDING);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        user.updateRefreshToken(refreshToken);

        return new TokenResponse(accessToken, refreshToken);
    }

    // 토큰 재발급 (RTR 방식)
    public TokenResponse reissue(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        User user = findUserById(userId);

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new BusinessException(ErrorCode.TOKEN_MISMATCH);
        }

        String newAccessToken = jwtUtil.generateAccessToken(userId);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId);

        user.updateRefreshToken(newRefreshToken);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    // 로그아웃
    public void logout(Long userId) {
        User user = findUserById(userId);
        user.updateRefreshToken(null);
    }

    // 회원가입 요청 목록 조회
    public Page<SignupRequestListResponse> getSignupRequests(Long userId, Pageable pageable) {
        verifyAdmin(userId);
        return userRepository.findBySignupStatusNot(SignupStatus.APPROVED, pageable)
                .map(SignupRequestListResponse::new);
    }

    // 회원가입 승인
    @Transactional
    public void approve(Long userId, Long requestedId) {
        verifyAdmin(userId);
        User requestedUser = findUserById(requestedId);
        requestedUser.updateApprovalStatus(SignupStatus.APPROVED);
    }
    
    // 회원가입 거절
    @Transactional
    public void reject(Long userId,Long requestedId) {
        verifyAdmin(userId);
        User requestedUser = findUserById(requestedId);
        requestedUser.updateApprovalStatus(SignupStatus.REJECTED);
    }

    private @NonNull User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void verifyAdmin(Long userId) {
        User user = findUserById(userId);
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.ADMIN_ONLY);
        }
    }
}