package com.yunbok.houseping.controller.web;

import com.yunbok.houseping.core.domain.User;
import com.yunbok.houseping.core.service.user.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
// SecurityConfig에서 /admin/users/** → hasRole('MASTER') 설정됨
public class AdminUserController {

    private final UserManagementService userManagementUseCase;

    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userManagementUseCase.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users/list";
    }

    @PostMapping("/{id}/approve")
    public String approveUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userManagementUseCase.approveUser(id);
            redirectAttributes.addFlashAttribute("message", "사용자가 승인되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userManagementUseCase.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "사용자가 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/promote")
    public String promoteToAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userManagementUseCase.promoteToAdmin(id);
            redirectAttributes.addFlashAttribute("message", "관리자로 승격되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/demote")
    public String demoteToUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userManagementUseCase.demoteToUser(id);
            redirectAttributes.addFlashAttribute("message", "일반 사용자로 변경되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
