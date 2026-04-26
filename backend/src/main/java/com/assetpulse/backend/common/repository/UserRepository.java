// src/main/java/com/assetpulse/repository/UserRepository.java
package com.assetpulse.backend.common.repository;

import com.assetpulse.backend.common.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}