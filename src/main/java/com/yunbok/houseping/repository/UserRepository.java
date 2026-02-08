package com.yunbok.houseping.repository;
import com.yunbok.houseping.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByNaverId(String naverId);
}
