package com.yunbok.houseping.core.service.auth;

import com.yunbok.houseping.config.oauth2.OAuth2UserInfo;
import com.yunbok.houseping.core.domain.*;
import com.yunbok.houseping.infrastructure.persistence.UserStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AuthenticationService - 인증 서비스")
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserStore userPersistencePort;

    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticationService(userPersistencePort);
    }

    @Nested
    @DisplayName("processOAuth2Login() - OAuth2 로그인 처리")
    class ProcessOAuth2Login {

        @Test
        @DisplayName("기존 사용자인 경우 마지막 로그인 시간을 업데이트한다")
        void updatesLastLoginForExistingUser() {
            // given
            OAuth2UserInfo userInfo = OAuth2UserInfo.builder()
                    .naverId("existing-naver-id")
                    .email("test@example.com")
                    .name("기존사용자")
                    .build();

            User existingUser = User.builder()
                    .id(1L)
                    .naverId("existing-naver-id")
                    .email("test@example.com")
                    .name("기존사용자")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .lastLoginAt(LocalDateTime.now().minusDays(1))
                    .build();

            when(userPersistencePort.findByNaverId("existing-naver-id"))
                    .thenReturn(Optional.of(existingUser));
            when(userPersistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            User result = service.processOAuth2Login(userInfo);

            // then
            assertThat(result.getNaverId()).isEqualTo("existing-naver-id");
            verify(userPersistencePort).save(any(User.class));
        }

        @Test
        @DisplayName("신규 마스터 사용자는 MASTER 역할과 ACTIVE 상태로 생성된다")
        void createsNewMasterUserWithCorrectRoleAndStatus() {
            // given
            ReflectionTestUtils.setField(service, "masterNaverId", "master-naver-id");

            OAuth2UserInfo userInfo = OAuth2UserInfo.builder()
                    .naverId("master-naver-id")
                    .email("master@example.com")
                    .name("마스터")
                    .build();

            when(userPersistencePort.findByNaverId("master-naver-id"))
                    .thenReturn(Optional.empty());
            when(userPersistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            User result = service.processOAuth2Login(userInfo);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userPersistencePort).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(UserRole.MASTER);
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(savedUser.getNaverId()).isEqualTo("master-naver-id");
        }

        @Test
        @DisplayName("신규 일반 사용자는 USER 역할과 ACTIVE 상태로 생성된다 (자동 회원가입)")
        void createsNewRegularUserWithActiveStatus() {
            // given
            ReflectionTestUtils.setField(service, "masterNaverId", "master-naver-id");

            OAuth2UserInfo userInfo = OAuth2UserInfo.builder()
                    .naverId("regular-naver-id")
                    .email("regular@example.com")
                    .name("일반사용자")
                    .build();

            when(userPersistencePort.findByNaverId("regular-naver-id"))
                    .thenReturn(Optional.empty());
            when(userPersistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            User result = service.processOAuth2Login(userInfo);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userPersistencePort).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("마스터 ID가 비어있으면 일반 사용자로 생성된다 (자동 ACTIVE)")
        void createsRegularUserWhenMasterIdIsEmpty() {
            // given
            ReflectionTestUtils.setField(service, "masterNaverId", "");

            OAuth2UserInfo userInfo = OAuth2UserInfo.builder()
                    .naverId("any-naver-id")
                    .email("any@example.com")
                    .name("사용자")
                    .build();

            when(userPersistencePort.findByNaverId("any-naver-id"))
                    .thenReturn(Optional.empty());
            when(userPersistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            User result = service.processOAuth2Login(userInfo);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userPersistencePort).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("마스터 ID가 null이면 일반 사용자로 생성된다 (자동 ACTIVE)")
        void createsRegularUserWhenMasterIdIsNull() {
            // given
            ReflectionTestUtils.setField(service, "masterNaverId", null);

            OAuth2UserInfo userInfo = OAuth2UserInfo.builder()
                    .naverId("any-naver-id")
                    .email("any@example.com")
                    .name("사용자")
                    .build();

            when(userPersistencePort.findByNaverId("any-naver-id"))
                    .thenReturn(Optional.empty());
            when(userPersistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            User result = service.processOAuth2Login(userInfo);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userPersistencePort).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("신규 사용자 생성 시 이메일과 이름이 저장된다")
        void savesEmailAndNameForNewUser() {
            // given
            ReflectionTestUtils.setField(service, "masterNaverId", "");

            OAuth2UserInfo userInfo = OAuth2UserInfo.builder()
                    .naverId("new-user-id")
                    .email("newuser@example.com")
                    .name("신규사용자")
                    .build();

            when(userPersistencePort.findByNaverId("new-user-id"))
                    .thenReturn(Optional.empty());
            when(userPersistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            service.processOAuth2Login(userInfo);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userPersistencePort).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
            assertThat(savedUser.getName()).isEqualTo("신규사용자");
        }
    }
}
