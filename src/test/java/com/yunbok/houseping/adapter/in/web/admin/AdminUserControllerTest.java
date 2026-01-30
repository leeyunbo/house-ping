package com.yunbok.houseping.adapter.in.web.admin;

import com.yunbok.houseping.domain.model.User;
import com.yunbok.houseping.domain.model.UserRole;
import com.yunbok.houseping.domain.model.UserStatus;
import com.yunbok.houseping.domain.port.in.UserManagementUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("AdminUserController - 회원 관리 컨트롤러")
@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private UserManagementUseCase userManagementUseCase;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    private AdminUserController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminUserController(userManagementUseCase);
    }

    @Nested
    @DisplayName("listUsers() - 회원 목록 페이지")
    class ListUsers {

        @Test
        @DisplayName("회원 목록 페이지 뷰 이름을 반환한다")
        void returnsCorrectViewName() {
            // given
            when(userManagementUseCase.getAllUsers()).thenReturn(List.of());

            // when
            String viewName = controller.listUsers(model);

            // then
            assertThat(viewName).isEqualTo("admin/users/list");
        }

        @Test
        @DisplayName("회원 목록을 모델에 추가한다")
        void addsUsersToModel() {
            // given
            List<User> users = List.of(
                    createUser(1L, UserRole.MASTER, UserStatus.ACTIVE),
                    createUser(2L, UserRole.USER, UserStatus.PENDING)
            );
            when(userManagementUseCase.getAllUsers()).thenReturn(users);

            // when
            controller.listUsers(model);

            // then
            verify(model).addAttribute("users", users);
        }
    }

    @Nested
    @DisplayName("approveUser() - 회원 승인")
    class ApproveUser {

        @Test
        @DisplayName("승인 성공 시 회원 목록 페이지로 리다이렉트한다")
        void redirectsToListPageOnSuccess() {
            // given
            doNothing().when(userManagementUseCase).approveUser(1L);

            // when
            String result = controller.approveUser(1L, redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/admin/users");
        }

        @Test
        @DisplayName("승인 성공 시 성공 메시지를 추가한다")
        void addsSuccessMessageOnSuccess() {
            // given
            doNothing().when(userManagementUseCase).approveUser(1L);

            // when
            controller.approveUser(1L, redirectAttributes);

            // then
            verify(redirectAttributes).addFlashAttribute("message", "사용자가 승인되었습니다.");
        }

        @Test
        @DisplayName("승인 실패 시 에러 메시지를 추가한다")
        void addsErrorMessageOnFailure() {
            // given
            doThrow(new IllegalArgumentException("사용자를 찾을 수 없습니다"))
                    .when(userManagementUseCase).approveUser(999L);

            // when
            String result = controller.approveUser(999L, redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/admin/users");
            verify(redirectAttributes).addFlashAttribute("error", "사용자를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("deleteUser() - 회원 삭제")
    class DeleteUser {

        @Test
        @DisplayName("삭제 성공 시 회원 목록 페이지로 리다이렉트한다")
        void redirectsToListPageOnSuccess() {
            // given
            doNothing().when(userManagementUseCase).deleteUser(1L);

            // when
            String result = controller.deleteUser(1L, redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/admin/users");
        }

        @Test
        @DisplayName("삭제 성공 시 성공 메시지를 추가한다")
        void addsSuccessMessageOnSuccess() {
            // given
            doNothing().when(userManagementUseCase).deleteUser(1L);

            // when
            controller.deleteUser(1L, redirectAttributes);

            // then
            verify(redirectAttributes).addFlashAttribute("message", "사용자가 삭제되었습니다.");
        }

        @Test
        @DisplayName("삭제 실패 시 에러 메시지를 추가한다")
        void addsErrorMessageOnFailure() {
            // given
            doThrow(new IllegalArgumentException("사용자를 찾을 수 없습니다"))
                    .when(userManagementUseCase).deleteUser(999L);

            // when
            String result = controller.deleteUser(999L, redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/admin/users");
            verify(redirectAttributes).addFlashAttribute("error", "사용자를 찾을 수 없습니다");
        }
    }

    private User createUser(Long id, UserRole role, UserStatus status) {
        return User.builder()
                .id(id)
                .naverId("naver-" + id)
                .email("user" + id + "@example.com")
                .name("사용자" + id)
                .role(role)
                .status(status)
                .build();
    }
}
