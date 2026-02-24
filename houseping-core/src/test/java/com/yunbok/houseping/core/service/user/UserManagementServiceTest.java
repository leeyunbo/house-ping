package com.yunbok.houseping.core.service.user;

import com.yunbok.houseping.core.domain.User;
import com.yunbok.houseping.core.domain.UserRole;
import com.yunbok.houseping.core.domain.UserStatus;
import com.yunbok.houseping.core.port.UserPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("UserManagementService - 사용자 관리 서비스")
@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    private UserManagementService service;

    @BeforeEach
    void setUp() {
        service = new UserManagementService(userPersistencePort);
    }

    @Nested
    @DisplayName("getAllUsers() - 전체 사용자 조회")
    class GetAllUsers {

        @Test
        @DisplayName("사용자 목록을 반환한다")
        void returnsAllUsers() {
            // given
            List<User> users = List.of(
                    createUser(1L, "user1", UserRole.USER, UserStatus.ACTIVE),
                    createUser(2L, "user2", UserRole.ADMIN, UserStatus.ACTIVE)
            );
            when(userPersistencePort.findAll()).thenReturn(users);

            // when
            List<User> result = service.getAllUsers();

            // then
            assertThat(result).hasSize(2);
            verify(userPersistencePort).findAll();
        }

        @Test
        @DisplayName("사용자가 없으면 빈 목록을 반환한다")
        void returnsEmptyListWhenNoUsers() {
            // given
            when(userPersistencePort.findAll()).thenReturn(List.of());

            // when
            List<User> result = service.getAllUsers();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("approveUser() - 사용자 승인")
    class ApproveUser {

        @Test
        @DisplayName("사용자를 승인하면 상태가 ACTIVE로 변경된다")
        void approvesUser() {
            // given
            User user = createUser(1L, "user1", UserRole.USER, UserStatus.PENDING);
            when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));

            // when
            service.approveUser(1L);

            // then
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            verify(userPersistencePort).save(user);
        }

        @Test
        @DisplayName("존재하지 않는 사용자를 승인하면 예외가 발생한다")
        void throwsWhenUserNotFound() {
            // given
            when(userPersistencePort.findById(99L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.approveUser(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("deleteUser() - 사용자 삭제")
    class DeleteUser {

        @Test
        @DisplayName("사용자를 삭제한다")
        void deletesUser() {
            // given
            User user = createUser(1L, "user1", UserRole.USER, UserStatus.ACTIVE);
            when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));

            // when
            service.deleteUser(1L);

            // then
            verify(userPersistencePort).deleteById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 사용자를 삭제하면 예외가 발생한다")
        void throwsWhenUserNotFound() {
            // given
            when(userPersistencePort.findById(99L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.deleteUser(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("promoteToAdmin() - 관리자로 승격")
    class PromoteToAdmin {

        @Test
        @DisplayName("사용자를 관리자로 승격한다")
        void promotesToAdmin() {
            // given
            User user = createUser(1L, "user1", UserRole.USER, UserStatus.ACTIVE);
            when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));

            // when
            service.promoteToAdmin(1L);

            // then
            assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
            verify(userPersistencePort).save(user);
        }

        @Test
        @DisplayName("존재하지 않는 사용자를 승격하면 예외가 발생한다")
        void throwsWhenUserNotFound() {
            // given
            when(userPersistencePort.findById(99L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.promoteToAdmin(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("MASTER 역할의 사용자를 승격하면 예외가 발생한다")
        void throwsWhenMasterUser() {
            // given
            User master = createUser(1L, "master", UserRole.MASTER, UserStatus.ACTIVE);
            when(userPersistencePort.findById(1L)).thenReturn(Optional.of(master));

            // when & then
            assertThatThrownBy(() -> service.promoteToAdmin(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("MASTER는 역할을 변경할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("demoteToUser() - 일반 사용자로 강등")
    class DemoteToUser {

        @Test
        @DisplayName("관리자를 일반 사용자로 강등한다")
        void demotesToUser() {
            // given
            User admin = createUser(1L, "admin", UserRole.ADMIN, UserStatus.ACTIVE);
            when(userPersistencePort.findById(1L)).thenReturn(Optional.of(admin));

            // when
            service.demoteToUser(1L);

            // then
            assertThat(admin.getRole()).isEqualTo(UserRole.USER);
            verify(userPersistencePort).save(admin);
        }

        @Test
        @DisplayName("존재하지 않는 사용자를 강등하면 예외가 발생한다")
        void throwsWhenUserNotFound() {
            // given
            when(userPersistencePort.findById(99L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.demoteToUser(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("MASTER 역할의 사용자를 강등하면 예외가 발생한다")
        void throwsWhenMasterUser() {
            // given
            User master = createUser(1L, "master", UserRole.MASTER, UserStatus.ACTIVE);
            when(userPersistencePort.findById(1L)).thenReturn(Optional.of(master));

            // when & then
            assertThatThrownBy(() -> service.demoteToUser(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("MASTER는 역할을 변경할 수 없습니다");
        }
    }

    private User createUser(Long id, String name, UserRole role, UserStatus status) {
        return User.builder()
                .id(id)
                .naverId("naver-" + name)
                .email(name + "@example.com")
                .name(name)
                .role(role)
                .status(status)
                .build();
    }
}
