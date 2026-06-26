package com.arthursouto.repository;

import com.arthursouto.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByGoogleId(String googleId);

    @Query("SELECT CASE WHEN u.isVerified = true THEN true ELSE false END FROM User u WHERE u.id = :id")
    boolean isVerifiedById(@Param("id") UUID id);
}
