package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.PlateReviewModerationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPlateReviewModerationEventDao extends JpaRepository<PlateReviewModerationEvent, Long> {
}
