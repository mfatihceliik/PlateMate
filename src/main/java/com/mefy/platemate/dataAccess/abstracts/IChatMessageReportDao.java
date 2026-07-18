package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.ChatMessageReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IChatMessageReportDao extends JpaRepository<ChatMessageReport, Long> {
}
