package com.yunbok.houseping.domain.port.in;

import com.yunbok.houseping.domain.model.User;

import java.util.List;

public interface UserManagementUseCase {
    List<User> getAllUsers();
    void approveUser(Long userId);
    void deleteUser(Long userId);
    void promoteToAdmin(Long userId);
    void demoteToUser(Long userId);
}
