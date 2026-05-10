package com.smakroom.user.repository;

import com.smakroom.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // JPQL: find user with profile eagerly
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.username = :username")
    Optional<User> findByUsernameWithProfile(@Param("username") String username);

    // JPQL: count active users
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countActiveUsers();
}
