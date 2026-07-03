package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.Friendship;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IFriendshipDao extends JpaRepository<Friendship, Long> {

    @Query("""
            select count(f)
            from Friendship f
            where (
                (f.requester.id = :userId1 and f.addressee.id = :userId2)
                or (f.requester.id = :userId2 and f.addressee.id = :userId1)
            )
            and f.statusId in :activeStatusIds
            """)
    long countActiveBetweenUsers(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2,
            @Param("activeStatusIds") Collection<Long> activeStatusIds
    );

    @Query("""
            select f
            from Friendship f
            where (f.requester.id = :userId or f.addressee.id = :userId)
              and f.statusId = :statusId
            order by coalesce(f.respondedAt, f.createdAt) desc
            """)
    List<Friendship> findByUserIdAndStatusId(@Param("userId") Long userId, @Param("statusId") Long statusId);

    List<Friendship> findByAddresseeIdAndStatusId(Long addresseeId, Long statusId);

    @Query("""
            select count(f)
            from Friendship f
            where (f.requester.id = :userId or f.addressee.id = :userId)
              and f.statusId = :statusId
            """)
    long countByUserIdAndStatusId(@Param("userId") Long userId, @Param("statusId") Long statusId);

    @Query("""
            select f
            from Friendship f
            join fetch f.requester r
            join fetch f.addressee a
            left join fetch f.statusRef s
            where r.id = :userId or a.id = :userId
            order by coalesce(f.respondedAt, f.createdAt) desc
            """)
    List<Friendship> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            select f
            from Friendship f
            where (
                (f.requester.id = :userId1 and f.addressee.id = :userId2)
                or (f.requester.id = :userId2 and f.addressee.id = :userId1)
            )
            and f.statusId in :activeStatusIds
            order by f.createdAt desc
            """)
    Optional<Friendship> findLatestActiveBetweenUsers(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2,
            @Param("activeStatusIds") Collection<Long> activeStatusIds
    );
}
