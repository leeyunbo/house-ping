package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.*;
import com.yunbok.houseping.domain.port.in.AuthenticationUseCase;
import com.yunbok.houseping.domain.port.out.UserPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements AuthenticationUseCase {

    private final UserPersistencePort userPersistencePort;

    @Value("${master.admin.naver-id:}")
    private String masterNaverId;

    @Override
    @Transactional
    public User processOAuth2Login(OAuth2UserInfo userInfo) {
        return userPersistencePort.findByNaverId(userInfo.getNaverId())
                .map(existingUser -> {
                    existingUser.updateLastLogin();
                    return userPersistencePort.save(existingUser);
                })
                .orElseGet(() -> createNewUser(userInfo));
    }

    private User createNewUser(OAuth2UserInfo userInfo) {
        boolean isMaster = masterNaverId != null
                && !masterNaverId.isBlank()
                && masterNaverId.equals(userInfo.getNaverId());

        User newUser = User.builder()
                .naverId(userInfo.getNaverId())
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .role(isMaster ? UserRole.MASTER : UserRole.USER)
                .status(UserStatus.ACTIVE)  // 네이버 로그인 시 자동 회원가입
                .build();

        newUser.updateLastLogin();
        return userPersistencePort.save(newUser);
    }
}
