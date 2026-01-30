package com.yunbok.houseping.adapter.in.web.admin;

import com.yunbok.houseping.domain.model.User;
import com.yunbok.houseping.domain.port.in.UserManagementUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MASTER')")
public class AdminUserController {

    private final UserManagementUseCase userManagementUseCase;

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
}
