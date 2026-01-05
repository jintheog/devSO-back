package com.example.devso.service;

import com.example.devso.dto.request.LoginRequest;
import com.example.devso.dto.request.SignupRequest;
import com.example.devso.dto.response.KakaoTokenResponse;
import com.example.devso.dto.response.KakaoUserResponse;
import com.example.devso.dto.response.TokenResponse;
import com.example.devso.dto.response.UserResponse;
import com.example.devso.entity.AuthProvider;
import com.example.devso.entity.User;
import com.example.devso.exception.CustomException;
import com.example.devso.exception.ErrorCode;
import com.example.devso.repository.UserRepository;
import com.example.devso.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final KakaoOauthService kakaoOauthService;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        // return boolean => long
        if (userRepository.existsByUsername(request.getUsername()) == 1) {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }


        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("아이디을 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtProvider.createToken(user.getUsername());

        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }

    public UserResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow();
        return UserResponse.from(user);
    }


    // 카카오 소셜 로그인 구현
    @Transactional
    public TokenResponse kakaoLogin(String code) {
        // 1. Auth code 이용해서 Access Token 발급
        KakaoTokenResponse tokenResponse = kakaoOauthService.getToken(code);

        // 2. Access Token 사용자 정보 조회
        KakaoUserResponse userInfo = kakaoOauthService.getUserInfo(tokenResponse.getAccessToken());

        // 3. 기존 카카오 사용자 조회 없으면 새로 가입
        User user = userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, String.valueOf(userInfo.getId()))
                .orElseGet(// 신규 사용자 생성
                        () ->createKakaoUser(userInfo));

        // 4. 프로필 정보 업데이트
        user.updateOauthProfile(userInfo.getKakaoAccount().getProfile().getNickname(),
                userInfo.getKakaoAccount().getProfile().getProfileImageUrl());

        // 5. JWT 발급
        String token = jwtProvider.createToken(user.getUsername());
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
      }


    // 카카오 신규 사용자 생성
    private User createKakaoUser(KakaoUserResponse userInfo) {
        String username = "kakao_" + userInfo.getId();

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(username))
                .name(userInfo.getKakaoAccount().getProfile().getNickname())
                .provider(AuthProvider.KAKAO)
                .providerId(String.valueOf(userInfo.getId()))
                .build();
        return userRepository.save(user);
    };
}
