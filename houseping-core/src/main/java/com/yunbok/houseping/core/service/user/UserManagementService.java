package com.yunbok.houseping.core.service.user;

import com.yunbok.houseping.core.domain.User;
import com.yunbok.houseping.core.port.UserPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserPersistencePort userPersistencePort;

    public List<User> getAllUsers() {
        return userPersistencePort.findAll();
    }

    @Transactional
    public void approveUser(Long userId) {
        User user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        user.approve();
        userPersistencePort.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userPersistencePort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        userPersistencePort.deleteById(userId);
    }

    @Transactional
    public void promoteToAdmin(Long userId) {
        User user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        user.promoteToAdmin();
        userPersistencePort.save(user);
    }

    @Transactional
    public void demoteToUser(Long userId) {
        User user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        user.demoteToUser();
        userPersistencePort.save(user);
    }
}
