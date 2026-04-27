package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserProfileDao extends JpaRepository<UserProfile, Long> {
}
