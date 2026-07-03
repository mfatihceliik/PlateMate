package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IUserBlockDao extends JpaRepository<UserBlock, Long> {

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    Optional<UserBlock> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM UserBlock b
            WHERE (b.blocker.id = :userId1 AND b.blocked.id = :userId2)
               OR (b.blocker.id = :userId2 AND b.blocked.id = :userId1)
            """)
    boolean existsBlockBetween(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT b.blocked.id FROM UserBlock b WHERE b.blocker.id = :blockerId")
    List<Long> findBlockedUserIdsByBlockerId(@Param("blockerId") Long blockerId);
}
