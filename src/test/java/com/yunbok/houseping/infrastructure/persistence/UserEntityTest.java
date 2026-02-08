package com.yunbok.houseping.infrastructure.persistence;

import com.yunbok.houseping.entity.UserEntity;
import com.yunbok.houseping.core.domain.User;
import com.yunbok.houseping.core.domain.UserRole;
import com.yunbok.houseping.core.domain.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserEntity - 사용자 JPA 엔티티")
class UserEntityTest {

    @Nested
    @DisplayName("from() - 도메인 모델에서 엔티티 생성")
    class From {

        @Test
        @DisplayName("User 도메인 모델을 엔티티로 변환한다")
        void convertsUserToEntity() {
            // given
            LocalDateTime now = LocalDateTime.now();
            User user = User.builder()
                    .id(1L)
                    .naverId("naver-123")
                    .email("test@example.com")
                    .name("테스트사용자")
                    .role(UserRole.MASTER)
                    .status(UserStatus.ACTIVE)
                    .lastLoginAt(now)
                    .build();

            // when
            UserEntity entity = UserEntity.from(user);

            // then
            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getNaverId()).isEqualTo("naver-123");
            assertThat(entity.getEmail()).isEqualTo("test@example.com");
            assertThat(entity.getName()).isEqualTo("테스트사용자");
            assertThat(entity.getRole()).isEqualTo(UserRole.MASTER);
            assertThat(entity.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(entity.getLastLoginAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("null ID도 변환된다")
        void handlesNullId() {
            // given
            User user = User.builder()
                    .naverId("naver-new")
                    .email("new@example.com")
                    .name("신규사용자")
                    .role(UserRole.USER)
                    .status(UserStatus.PENDING)
                    .build();

            // when
            UserEntity entity = UserEntity.from(user);

            // then
            assertThat(entity.getId()).isNull();
            assertThat(entity.getNaverId()).isEqualTo("naver-new");
        }
    }

    @Nested
    @DisplayName("toDomain() - 엔티티에서 도메인 모델 생성")
    class ToDomain {

        @Test
        @DisplayName("엔티티를 User 도메인 모델로 변환한다")
        void convertsEntityToUser() {
            // given
            LocalDateTime now = LocalDateTime.now();
            UserEntity entity = UserEntity.builder()
                    .id(1L)
                    .naverId("naver-123")
                    .email("test@example.com")
                    .name("테스트사용자")
                    .role(UserRole.USER)
                    .status(UserStatus.PENDING)
                    .lastLoginAt(now)
                    .createdAt(now.minusDays(1))
                    .updatedAt(now)
                    .build();

            // when
            User user = entity.toDomain();

            // then
            assertThat(user.getId()).isEqualTo(1L);
            assertThat(user.getNaverId()).isEqualTo("naver-123");
            assertThat(user.getEmail()).isEqualTo("test@example.com");
            assertThat(user.getName()).isEqualTo("테스트사용자");
            assertThat(user.getRole()).isEqualTo(UserRole.USER);
            assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
            assertThat(user.getLastLoginAt()).isEqualTo(now);
            assertThat(user.getCreatedAt()).isEqualTo(now.minusDays(1));
            assertThat(user.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("생성일과 수정일이 포함된다")
        void includesAuditFields() {
            // given
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2025, 1, 15, 12, 0);

            UserEntity entity = UserEntity.builder()
                    .id(1L)
                    .naverId("naver-123")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            // when
            User user = entity.toDomain();

            // then
            assertThat(user.getCreatedAt()).isEqualTo(createdAt);
            assertThat(user.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    @DisplayName("Builder - 엔티티 생성")
    class Builder {

        @Test
        @DisplayName("모든 필드가 올바르게 설정된다")
        void setsAllFieldsCorrectly() {
            // given
            LocalDateTime now = LocalDateTime.now();

            // when
            UserEntity entity = UserEntity.builder()
                    .id(1L)
                    .naverId("naver-id")
                    .email("email@test.com")
                    .name("이름")
                    .role(UserRole.MASTER)
                    .status(UserStatus.ACTIVE)
                    .lastLoginAt(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // then
            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getNaverId()).isEqualTo("naver-id");
            assertThat(entity.getEmail()).isEqualTo("email@test.com");
            assertThat(entity.getName()).isEqualTo("이름");
            assertThat(entity.getRole()).isEqualTo(UserRole.MASTER);
            assertThat(entity.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(entity.getLastLoginAt()).isEqualTo(now);
        }
    }
}
