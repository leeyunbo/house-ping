package com.yunbok.houseping.infrastructure.persistence;

import com.yunbok.houseping.core.domain.User;
import com.yunbok.houseping.core.port.UserPersistencePort;
import com.yunbok.houseping.entity.UserEntity;
import com.yunbok.houseping.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserStore implements UserPersistencePort {

    private final UserRepository userRepository;

    public Optional<User> findByNaverId(String naverId) {
        return userRepository.findByNaverId(naverId)
                .map(UserEntity::toDomain);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id)
                .map(UserEntity::toDomain);
    }

    public User save(User user) {
        UserEntity entity = UserEntity.from(user);
        return userRepository.save(entity).toDomain();
    }

    public List<User> findAll() {
        return userRepository.findAll().stream()
                .map(UserEntity::toDomain)
                .toList();
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
