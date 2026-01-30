package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.User;
import com.yunbok.houseping.domain.model.UserRole;
import com.yunbok.houseping.domain.model.UserStatus;
import com.yunbok.houseping.domain.port.out.UserPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("UserManagementService - 회원 관리 서비스")
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
        @DisplayName("모든 사용자를 반환한다")
        void returnsAllUsers() {
            // given
            List<User> users = List.of(
                    createUser(1L, "user1", UserRole.MASTER, UserStatus.ACTIVE),
                    createUser(2L, "user2", UserRole.USER, UserStatus.PENDING),
                    createUser(3L, "user3", UserRole.USER, UserStatus.ACTIVE)
            );
            when(userPersistencePort.findAll()).thenReturn(users);

            // when
            List<User> result = service.getAllUsers();

            // then
            assertThat(result).hasSize(3);
            verify(userPersistencePort).findAll();
        }

        @Test
        @DisplayName("사용자가 없으면 빈 리스트를 반환한다")
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
        @DisplayName("PENDING 상태의 사용자를 ACTIVE로 변경한다")
        void changesPendingUserToActive() {
            // given
            User pendingUser = createUser(1L, "pending-user", UserRole.USER, UserStatus.PENDING);
            when(userPersistencePort.findById(1L)).thenReturn(Optional.of(pendingUser));
            when(userPersistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            service.approveUser(1L);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userPersistencePort).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 승인 시 예외가 발생한다")
        void throwsExceptionWhenUserNotFound() {
            // given
            when(userPersistencePort.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.approveUser(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("이미 ACTIVE인 사용자도 승인할 수 있다")
        void canApproveAlreadyActiveUser() {
            // given
            User activeUser = createUser(1L, "active-user", UserRole.USER, UserStatus.ACTIVE);
            when(userPersistencePort.findById(1L)).thenReturn(Optional.of(activeUser));
            when(userPersistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            service.approveUser(1L);

            // then
            verify(userPersistencePort).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("deleteUser() - 사용자 삭제")
    class DeleteUser {

        @Test
        @DisplayName("사용자를 삭제한다")
        void deletesUser() {
            // given
            User user = createUser(1L, "user-to-delete", UserRole.USER, UserStatus.ACTIVE);
            when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));
            doNothing().when(userPersistencePort).deleteById(1L);

            // when
            service.deleteUser(1L);

            // then
            verify(userPersistencePort).deleteById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 삭제 시 예외가 발생한다")
        void throwsExceptionWhenUserNotFound() {
            // given
            when(userPersistencePort.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.deleteUser(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }
    }

    private User createUser(Long id, String naverId, UserRole role, UserStatus status) {
        return User.builder()
                .id(id)
                .naverId(naverId)
                .email(naverId + "@example.com")
                .name("사용자" + id)
                .role(role)
                .status(status)
                .build();
    }
}
