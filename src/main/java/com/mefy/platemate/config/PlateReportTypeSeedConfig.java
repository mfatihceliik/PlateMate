package com.mefy.platemate.config;

import com.mefy.platemate.dataAccess.abstracts.IPlateReportTypeDao;
import com.mefy.platemate.entities.concrete.PlateReportSeverity;
import com.mefy.platemate.entities.concrete.PlateReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class PlateReportTypeSeedConfig {

    private final IPlateReportTypeDao plateReportTypeDao;

    @Bean
    public ApplicationRunner seedPlateReportTypes() {
        return args -> seedDefaultsIfMissing();
    }

    private void seedDefaultsIfMissing() {
        // Neutral policy-first vocabulary (preferred)
        createIfMissing("UNSAFE_LANE_CHANGE", "Guvensiz Serit Degisimi", "Guvensiz serit degisimi davranisi bildirildi", "unsafe_lane_change", PlateReportSeverity.YELLOW, "#F9A825", 3, 1);
        createIfMissing("SPEEDING_REPORT", "Hiz Bildirimi", "Hiz limiti ihlali supheli davranis bildirildi", "speeding_report", PlateReportSeverity.YELLOW, "#F9A825", 3, 2);
        createIfMissing("PARKING_ISSUE", "Park Sorunu", "Trafik akisina etki eden park davranisi bildirildi", "parking_issue", PlateReportSeverity.YELLOW, "#F9A825", 2, 3);
        createIfMissing("HONKING_DISTURBANCE", "Korna Rahatsizligi", "Asiri korna kullanimi bildirildi", "honking_disturbance", PlateReportSeverity.YELLOW, "#F9A825", 2, 4);
        createIfMissing("TRAFFIC_RULE_VIOLATION", "Trafik Kurali Ihlali", "Trafik kurali ihlaline iliskin davranis bildirildi", "traffic_rule_violation", PlateReportSeverity.RED, "#E53935", 4, 5);
        createIfMissing("POSITIVE_DRIVER", "Olumlu Surucu", "Olumlu ve dikkatli surus davranisi bildirildi", "positive_driver", PlateReportSeverity.YELLOW, "#43A047", 1, 6);
        createIfMissing("HELPFUL_DRIVER", "Yardimci Surucu", "Trafikte yardimci davranis bildirildi", "helpful_driver", PlateReportSeverity.YELLOW, "#43A047", 1, 7);
        createIfMissing("RESPECTFUL_DRIVER", "Saygili Surucu", "Saygili ve guvenli davranis bildirildi", "respectful_driver", PlateReportSeverity.YELLOW, "#43A047", 1, 8);
        createIfMissing("THANK_YOU_REPORT", "Tesekkur Bildirimi", "Genel tesekkur veya memnuniyet bildirimi", "thank_you_report", PlateReportSeverity.YELLOW, "#43A047", 1, 9);
        createIfMissing("RED_LIGHT_VIOLATION", "Kirmizi Isik", "Kirmizi isik ihlali yapti", "red_light_violation", PlateReportSeverity.RED, "#E53935", 5, 3);
        createIfMissing("WRONG_WAY", "Ters Yon", "Ters yonde arac kullandi", "wrong_way", PlateReportSeverity.RED, "#E53935", 5, 4);
        createIfMissing("PHONE_USAGE", "Telefon Kullanimi", "Arac surerken telefon ile ilgileniyor", "phone_usage", PlateReportSeverity.YELLOW, "#F9A825", 3, 6);
        createIfMissing("SPEEDING", "Asiri Hiz", "Belirgin sekilde hiz sinirini asiyor", "speeding", PlateReportSeverity.RED, "#E53935", 4, 7);
        createIfMissing("ILLEGAL_PARKING", "Yasak Park", "Yasak veya engelli park alanina park etti", "illegal_parking", PlateReportSeverity.YELLOW, "#F9A825", 2, 8);
    }

    private void createIfMissing(
            String code,
            String label,
            String description,
            String iconKey,
            PlateReportSeverity severity,
            String colorHex,
            int weight,
            int sortOrder
    ) {
        if (plateReportTypeDao.existsByCode(code)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        PlateReportType type = new PlateReportType();
        type.setCode(code);
        type.setLabel(label);
        type.setDescription(description);
        type.setIconKey(iconKey);
        type.setSeverity(severity);
        type.setColorHex(colorHex);
        type.setWeight(weight);
        type.setSortOrder(sortOrder);
        type.setActive(true);
        type.setCreatedAt(now);
        type.setUpdatedAt(now);
        plateReportTypeDao.save(type);
    }
}
