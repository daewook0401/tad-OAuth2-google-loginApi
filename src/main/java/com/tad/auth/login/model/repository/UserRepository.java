package com.tad.auth.login.model.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tad.auth.login.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByPublicId(UUID publicId);
    Optional<User> findByGoogleSub(String googleSub);
    Optional<User> findByEmail(String email);
}
