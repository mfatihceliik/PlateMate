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
        createIfMissing("HIT_AND_RUN", "Carpip Kacti", "Araca veya kisiye carpip olay yerinden ayrildi", "hit_and_run", PlateReportSeverity.RED, "#E53935", 5, 1);
        createIfMissing("AGGRESSIVE_DRIVER", "Agresif Surucu", "Tehlikeli, sinirli veya saldirgan surucu", "aggressive_driver", PlateReportSeverity.YELLOW, "#F9A825", 3, 2);
        createIfMissing("RED_LIGHT_VIOLATION", "Kirmizi Isik", "Kirmizi isik ihlali yapti", "red_light_violation", PlateReportSeverity.RED, "#E53935", 5, 3);
        createIfMissing("WRONG_WAY", "Ters Yon", "Ters yonde arac kullandi", "wrong_way", PlateReportSeverity.RED, "#E53935", 5, 4);
        createIfMissing("DRUNK_DRIVING", "Alkollu", "Alkollu arac kullaniyor", "drunk_driving", PlateReportSeverity.RED, "#E53935", 5, 5);
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
