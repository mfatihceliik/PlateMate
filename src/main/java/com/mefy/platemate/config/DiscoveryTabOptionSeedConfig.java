package com.mefy.platemate.config;

import com.mefy.platemate.dataAccess.abstracts.IDiscoveryTabOptionDao;
import com.mefy.platemate.entities.concrete.DiscoveryTabOption;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DiscoveryTabOptionSeedConfig {

    private final IDiscoveryTabOptionDao discoveryTabOptionDao;

    @Bean
    public ApplicationRunner seedDiscoveryTabOptions() {
        return args -> seedDefaultsIfMissing();
    }

    private void seedDefaultsIfMissing() {
        createIfMissing("TREND", "Trend", 0);
        createIfMissing("DANGEROUS", "Careless", 1);
        createIfMissing("GOOD_DRIVER", "Good Driver", 2);
        createIfMissing("NEW", "Newest", 3);
    }

    private void createIfMissing(String code, String label, int sortOrder) {
        if (discoveryTabOptionDao.existsByCode(code)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        DiscoveryTabOption option = new DiscoveryTabOption();
        option.setCode(code);
        option.setLabel(label);
        option.setSortOrder(sortOrder);
        option.setActive(true);
        option.setCreatedAt(now);
        option.setUpdatedAt(now);
        discoveryTabOptionDao.save(option);
    }
}
