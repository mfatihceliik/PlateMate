package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IDiscoveryService;
import com.mefy.platemate.business.discovery.DiscoveryActivityService;
import com.mefy.platemate.business.discovery.DiscoveryAggregationService;
import com.mefy.platemate.business.discovery.DiscoveryTabService;
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
import com.mefy.platemate.entities.dto.CityPlateActivityDto;
import com.mefy.platemate.entities.dto.DiscoveryCityStatDto;
import com.mefy.platemate.entities.dto.DiscoveryHomeDto;
import com.mefy.platemate.entities.dto.DiscoveryTabsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscoveryManager implements IDiscoveryService {

    private static final Comparator<CityPlateActivityDto> BY_LAST_ACTIVITY_DESC =
            Comparator.comparing(CityPlateActivityDto::getLastActivityAt,
                    Comparator.nullsLast(Comparator.reverseOrder()));

    private final ICityDao cityDao;
    private final IMessageService messageService;
    private final LimitValidator limitValidator;
    private final TimeWindowService timeWindowService;
    private final DiscoveryAggregationService discoveryAggregationService;
    private final DiscoveryTabService discoveryTabService;
    private final DiscoveryActivityService discoveryActivityService;

    @Override
    @Transactional(readOnly = true)
    public DataResult<DiscoveryHomeDto> getHome(int limit, int cityLimit, int activityLimit) {
        Result validation = limitValidator.validateLimits(limit, cityLimit, activityLimit);
        if (!validation.isSuccess()) {
            return new ErrorDataResult<>(validation.getMessage());
        }

        TimeWindow today = timeWindowService.todayWindow();
        return new SuccessDataResult<>(buildHomeDto(today, limit, cityLimit, activityLimit),
                messageService.getMessage(Messages.DISCOVERY_HOME_FOUND));
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

    private DiscoveryHomeDto buildHomeDto(TimeWindow today, int limit, int cityLimit, int activityLimit) {
        DiscoveryTabsDto tabs             = discoveryTabService.buildTabs(limit);
        List<DiscoveryCityStatDto> cities = discoveryAggregationService.getTopCityStats(today, cityLimit);
        List<CityPlateActivityDto> topPlates = fetchTopCityPlates(cities, today);

        return new DiscoveryHomeDto(
                discoveryAggregationService.getDailyStats(today),
                tabs,
                cities,
                topPlates,
                discoveryActivityService.buildRecentActivities(activityLimit)
        );
    }

    private List<CityPlateActivityDto> fetchTopCityPlates(List<DiscoveryCityStatDto> cities,
                                                          TimeWindow today) {
        if (cities.isEmpty()) {
            return List.of();
        }
        Integer topCityId = cities.get(0).getCityId();
        return fetchAndSortCityPlates(topCityId, today);
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
