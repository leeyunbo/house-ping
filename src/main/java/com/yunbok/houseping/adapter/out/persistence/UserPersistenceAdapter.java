package com.yunbok.houseping.adapter.out.persistence;

import com.yunbok.houseping.domain.model.User;
import com.yunbok.houseping.domain.port.out.UserPersistencePort;
import com.yunbok.houseping.infrastructure.persistence.UserEntity;
import com.yunbok.houseping.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findByNaverId(String naverId) {
        return userRepository.findByNaverId(naverId)
                .map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id)
                .map(UserEntity::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.from(user);
        return userRepository.save(entity).toDomain();
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll().stream()
                .map(UserEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
