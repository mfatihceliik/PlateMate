package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.Friendship;
import com.mefy.platemate.entities.concrete.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IFriendshipDao extends JpaRepository<Friendship, Long> {

    // İki kullanıcı arasında herhangi bir friendship kaydı var mı? (yön fark etmez)
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester.id = :userId1 AND f.addressee.id = :userId2) OR " +
            "(f.requester.id = :userId2 AND f.addressee.id = :userId1)")
    Optional<Friendship> findBetweenUsers(Long userId1, Long userId2);

    // Belirli bir kullanıcının kabul edilmiş arkadaşlıkları
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester.id = :userId OR f.addressee.id = :userId) AND f.status = :status")
    List<Friendship> findByUserIdAndStatus(Long userId, FriendshipStatus status);

    // Bir kullanıcıya gelen bekleyen istekler (addressee olarak)
    List<Friendship> findByAddresseeIdAndStatus(Long addresseeId, FriendshipStatus status);
}
