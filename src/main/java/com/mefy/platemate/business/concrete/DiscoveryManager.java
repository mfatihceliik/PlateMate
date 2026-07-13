package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IDiscoveryService;
import com.mefy.platemate.business.discovery.DiscoveryActivityService;
import com.mefy.platemate.business.discovery.DiscoveryAggregationService;
import com.mefy.platemate.business.discovery.DiscoveryPersonalizationService;
import com.mefy.platemate.business.discovery.DiscoveryTabService;
import com.mefy.platemate.business.discovery.model.ScoredDiscoveryPlate;
import com.mefy.platemate.business.utilities.LimitValidator;
import com.mefy.platemate.business.utilities.time.TimeWindow;
import com.mefy.platemate.business.utilities.time.TimeWindowService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationMapper;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.dataAccess.abstracts.ICityDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.CityPlateActivityDto;
import com.mefy.platemate.entities.dto.DiscoveryCityStatDto;
import com.mefy.platemate.entities.dto.DiscoveryDailyStatsDto;
import com.mefy.platemate.entities.dto.DiscoveryExtendedStatsDto;
import com.mefy.platemate.entities.dto.DiscoveryFeedType;
import com.mefy.platemate.entities.dto.DiscoveryHomeDto;
import com.mefy.platemate.entities.dto.DiscoveryPlateCardDto;
import com.mefy.platemate.entities.dto.DiscoveryRecentActivityDto;
import com.mefy.platemate.entities.dto.DiscoveryTabType;
import com.mefy.platemate.entities.dto.DiscoveryTabsDto;
import com.mefy.platemate.entities.dto.PlateReportTypeDto;
import com.mefy.platemate.entities.dto.request.DiscoveryTabFeedRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DiscoveryManager implements IDiscoveryService {

    private static final int HOME_FALLBACK_DAYS = 7;
    private static final int TOP_REPORT_TYPE_LIMIT = 3;
    // In-memory filtreleme/sayfalama icin aday tavani; DB tarafina tasima docs/known-violations.md'de kayitli.
    private static final int MAX_FEED_CANDIDATES = 500;
    private static final int DEFAULT_ACTIVITY_WINDOW_DAYS = 7;
    private static final int GOOD_DRIVER_PENALTY_WINDOW_DAYS = 30;
    private static final Set<Integer> ALLOWED_WINDOW_DAYS = Set.of(7, 30);

    private static final Comparator<CityPlateActivityDto> BY_LAST_ACTIVITY_DESC =
            Comparator.comparing(CityPlateActivityDto::getLastActivityAt,
                    Comparator.nullsLast(Comparator.reverseOrder()));

    private final ICityDao cityDao;
    private final IUserDao userDao;
    private final IMessageService messageService;
    private final LimitValidator limitValidator;
    private final TimeWindowService timeWindowService;
    private final DiscoveryAggregationService discoveryAggregationService;
    private final DiscoveryTabService discoveryTabService;
    private final DiscoveryActivityService discoveryActivityService;
    private final DiscoveryPersonalizationService discoveryPersonalizationService;

    @Override
    @Transactional(readOnly = true)
    public DataResult<DiscoveryHomeDto> getHome(Long userId, int limit, int cityLimit, int activityLimit) {
        Result validation = limitValidator.validateLimits(limit, cityLimit, activityLimit);
        if (!validation.isSuccess()) {
            return new ErrorDataResult<>(validation.getMessage());
        }

        TimeWindow today = timeWindowService.todayWindow();
        TimeWindow fallbackWindow = timeWindowService.lastDaysWindow(HOME_FALLBACK_DAYS);

        DiscoveryHomeDto home = buildHomeDto(today, fallbackWindow, limit, cityLimit, activityLimit);
        DiscoveryFeedType feedType = resolveFeedType(userId);
        home.setFeedType(feedType.name());
        home.setExtendedStats(buildExtendedStats(home.getDailyStats(), today));
        if (feedType == DiscoveryFeedType.PREMIUM) {
            home.setForYou(discoveryPersonalizationService.buildForYou(userId, today, home.getRecentActivities()));
        }

        return new SuccessDataResult<>(home, messageService.getMessage(Messages.DISCOVERY_HOME_FOUND));
    }

    private DiscoveryFeedType resolveFeedType(Long userId) {
        User user = userDao.findByIdAndActiveTrue(userId).orElse(null);
        return user != null && user.isPremiumActive() ? DiscoveryFeedType.PREMIUM : DiscoveryFeedType.FREE;
    }

    private DiscoveryExtendedStatsDto buildExtendedStats(DiscoveryDailyStatsDto todayStats, TimeWindow today) {
        DiscoveryDailyStatsDto yesterdayStats =
                discoveryAggregationService.getDailyStats(timeWindowService.previousDayWindow());

        return new DiscoveryExtendedStatsDto(
                yesterdayStats.getTodaySearchCount(),
                yesterdayStats.getTodayReviewCount(),
                yesterdayStats.getTodayReportCount(),
                deltaPercent(todayStats.getTodaySearchCount(), yesterdayStats.getTodaySearchCount()),
                deltaPercent(todayStats.getTodayReviewCount(), yesterdayStats.getTodayReviewCount()),
                deltaPercent(todayStats.getTodayReportCount(), yesterdayStats.getTodayReportCount()),
                discoveryAggregationService.getTopReportTypeCounts(today, TOP_REPORT_TYPE_LIMIT)
        );
    }

    private Double deltaPercent(Long todayValue, Long yesterdayValue) {
        long today = todayValue == null ? 0L : todayValue;
        long yesterday = yesterdayValue == null ? 0L : yesterdayValue;
        return (today - yesterday) * 100.0 / Math.max(yesterday, 1L);
    }

    @Override
    @Transactional(readOnly = true)
    public DataResult<PagedData<CityPlateActivityDto>> getCityPlates(Integer cityId, PaginationRequest pagination) {
        if (!cityDao.existsById(cityId)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.CITY_NOT_FOUND));
        }

        TimeWindow today = timeWindowService.todayWindow();
        List<CityPlateActivityDto> sorted = fetchAndSortCityPlates(cityId, today);
        PagedData<CityPlateActivityDto> page = paginate(sorted, pagination);

        return new SuccessDataResult<>(page, messageService.getMessage(Messages.DISCOVERY_CITY_PLATES_LISTED));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResult<PagedData<DiscoveryPlateCardDto>> getTabPlates(
            Long userId,
            String tabType,
            DiscoveryTabFeedRequest filter,
            PaginationRequest pagination
    ) {
        DiscoveryTabType tab = parseTabType(tabType);
        if (tab == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.DISCOVERY_TAB_INVALID));
        }
        if (!isFilterValid(filter)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.DISCOVERY_FILTER_INVALID));
        }
        if (filter.hasPremiumFilters() && resolveFeedType(userId) != DiscoveryFeedType.PREMIUM) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.DISCOVERY_FILTER_PREMIUM_REQUIRED));
        }

        TimeWindow window = resolveTabWindow(tab, filter.getWindowDays());
        List<ScoredDiscoveryPlate> candidates = discoveryTabService.buildTabScored(tab, MAX_FEED_CANDIDATES, window);

        Set<Long> plateIds = new HashSet<>();
        candidates.forEach(item -> plateIds.add(item.getPlate().getId()));
        Map<Long, List<PlateReportTypeDto>> topReportTypesMap = discoveryTabService.buildTopReportTypesMap(plateIds);

        List<ScoredDiscoveryPlate> filtered = candidates.stream()
                .filter(item -> matchesCity(item, filter.getCityIds()))
                .filter(item -> matchesMinRating(item, filter.getMinRating()))
                .filter(item -> matchesReportType(item, filter.getReportTypeCode(), topReportTypesMap))
                .toList();

        int total = filtered.size();
        int fromIndex = pagination.getPage() * pagination.getSize();
        int toIndex = Math.min(fromIndex + pagination.getSize(), total);
        List<DiscoveryPlateCardDto> content = fromIndex >= total
                ? List.of()
                : filtered.subList(fromIndex, toIndex).stream()
                        .map(item -> discoveryTabService.toPlateCard(item, topReportTypesMap))
                        .toList();

        PagedData<DiscoveryPlateCardDto> page = PaginationMapper.fromPage(
                new PageImpl<>(content, PageRequest.of(pagination.getPage(), pagination.getSize()), total)
        );
        return new SuccessDataResult<>(page, messageService.getMessage(Messages.DISCOVERY_TAB_LISTED));
    }

    private DiscoveryTabType parseTabType(String raw) {
        if (raw == null) return null;
        try {
            return DiscoveryTabType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private boolean isFilterValid(DiscoveryTabFeedRequest filter) {
        if (filter.getWindowDays() != null && !ALLOWED_WINDOW_DAYS.contains(filter.getWindowDays())) {
            return false;
        }
        Double minRating = filter.getMinRating();
        return minRating == null || (minRating >= 0.0 && minRating <= 5.0);
    }

    private TimeWindow resolveTabWindow(DiscoveryTabType tab, Integer windowDaysOverride) {
        int defaultDays = tab == DiscoveryTabType.GOOD_DRIVER
                ? GOOD_DRIVER_PENALTY_WINDOW_DAYS
                : DEFAULT_ACTIVITY_WINDOW_DAYS;
        int days = windowDaysOverride != null ? windowDaysOverride : defaultDays;
        return timeWindowService.lastDaysWindow(days);
    }

    private boolean matchesCity(ScoredDiscoveryPlate item, List<Integer> cityIds) {
        if (cityIds == null || cityIds.isEmpty()) return true;
        return item.getPlate().getCity() != null && cityIds.contains(item.getPlate().getCity().getId());
    }

    private boolean matchesMinRating(ScoredDiscoveryPlate item, Double minRating) {
        if (minRating == null) return true;
        Double ratingAverage = item.getPlate().getRatingAverage();
        return ratingAverage != null && ratingAverage >= minRating;
    }

    private boolean matchesReportType(
            ScoredDiscoveryPlate item,
            String reportTypeCode,
            Map<Long, List<PlateReportTypeDto>> topReportTypesMap
    ) {
        if (reportTypeCode == null) return true;
        return topReportTypesMap.getOrDefault(item.getPlate().getId(), List.of()).stream()
                .anyMatch(type -> reportTypeCode.equalsIgnoreCase(type.getCode()));
    }

    private DiscoveryHomeDto buildHomeDto(
            TimeWindow today,
            TimeWindow fallbackWindow,
            int limit,
            int cityLimit,
            int activityLimit
    ) {
        DiscoveryTabsDto tabs             = discoveryTabService.buildTabs(limit);
        List<DiscoveryCityStatDto> cities = discoveryAggregationService.getTopCityStats(today, cityLimit);
        TimeWindow cityWindow = today;
        if (cities.isEmpty()) {
            cities = discoveryAggregationService.getTopCityStats(fallbackWindow, cityLimit);
            if (!cities.isEmpty()) {
                cityWindow = fallbackWindow;
            }
        }

        List<CityPlateActivityDto> topPlates = fetchTopCityPlates(cities, cityWindow);
        List<DiscoveryRecentActivityDto> recentActivities = discoveryActivityService.buildRecentActivities(activityLimit, today);
        if (recentActivities.isEmpty()) {
            recentActivities = discoveryActivityService.buildRecentActivities(activityLimit, fallbackWindow);
        }

        return new DiscoveryHomeDto(
                discoveryAggregationService.getDailyStats(today),
                tabs,
                cities,
                topPlates,
                recentActivities,
                null,
                null,
                null
        );
    }

    private List<CityPlateActivityDto> fetchTopCityPlates(List<DiscoveryCityStatDto> cities,
                                                          TimeWindow window) {
        if (cities.isEmpty()) {
            return List.of();
        }
        Integer topCityId = cities.get(0).getCityId();
        return fetchAndSortCityPlates(topCityId, window);
    }

    private List<CityPlateActivityDto> fetchAndSortCityPlates(Integer cityId, TimeWindow today) {
        List<CityPlateActivityDto> plates = discoveryAggregationService.toCityPlateActivityRows(
                discoveryAggregationService.buildCityDailyMetrics(cityId, today)
        );
        plates.sort(BY_LAST_ACTIVITY_DESC);
        return plates;
    }
    private PagedData<CityPlateActivityDto> paginate(List<CityPlateActivityDto> rows, PaginationRequest pagination) {
        int total     = rows.size();
        int page      = pagination.getPage();
        int size      = pagination.getSize();
        int fromIndex = page * size;
        int toIndex   = Math.min(fromIndex + size, total);

        List<CityPlateActivityDto> content = fromIndex >= total ? List.of() : rows.subList(fromIndex, toIndex);
        return PaginationMapper.fromPage(new PageImpl<>(content, PageRequest.of(page, size), total));
    }
}
