package com.yunbok.houseping.domain.port.out;

import com.yunbok.houseping.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserPersistencePort {
    Optional<User> findByNaverId(String naverId);
    Optional<User> findById(Long id);
    User save(User user);
    List<User> findAll();
    void deleteById(Long id);
}
