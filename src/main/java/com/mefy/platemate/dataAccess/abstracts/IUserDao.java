package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface IUserDao extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findByIdAndActiveTrue(Long id);

    Optional<User> findByUsernameAndActiveTrue(String username);

    List<User> findByUsernameContainingIgnoreCaseAndActiveTrue(String username);

    Optional<User> findByUsernameOrEmailAndActiveTrue(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
