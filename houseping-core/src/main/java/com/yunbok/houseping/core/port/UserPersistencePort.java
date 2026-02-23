package com.yunbok.houseping.core.port;

import com.yunbok.houseping.core.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserPersistencePort {

    Optional<User> findByNaverId(String naverId);

    Optional<User> findById(Long id);

    User save(User user);

    List<User> findAll();

    void deleteById(Long id);
}
