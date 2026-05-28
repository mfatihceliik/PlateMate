package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.UserRole;
import com.mefy.platemate.entities.concrete.UserRoleCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserRoleDao extends JpaRepository<UserRole, Long> {
    Optional<UserRole> findByCodeId(Long codeId);

    boolean existsByCodeId(Long codeId);

    default Optional<UserRole> findByCode(UserRoleCode code) {
        return findByCodeId(code == null ? null : code.getId());
    }

    default boolean existsByCode(UserRoleCode code) {
        return existsByCodeId(code == null ? null : code.getId());
    }
}
