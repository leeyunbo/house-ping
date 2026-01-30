package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.User;
import com.yunbok.houseping.domain.port.in.UserManagementUseCase;
import com.yunbok.houseping.domain.port.out.UserPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementService implements UserManagementUseCase {

    private final UserPersistencePort userPersistencePort;

    @Override
    public List<User> getAllUsers() {
        return userPersistencePort.findAll();
    }

    @Override
    @Transactional
    public void approveUser(Long userId) {
        User user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        user.approve();
        userPersistencePort.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userPersistencePort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        userPersistencePort.deleteById(userId);
    }
}
