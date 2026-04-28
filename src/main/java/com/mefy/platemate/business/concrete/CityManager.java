package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.ICityService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.*;
import com.mefy.platemate.dataAccess.abstracts.ICityDao;
import com.mefy.platemate.entities.concrete.City;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityManager implements ICityService {
    private final ICityDao cityDao;
    private final IMessageService messageService;

    @Override
    public DataResult<List<City>> getAll() {
        return new SuccessDataResult<>(cityDao.findAll(), messageService.getMessage(Messages.CITIES_LISTED));
    }

    @Override
    public DataResult<City> getById(Integer id) {
        City city = cityDao.findById(id).orElse(null);
        
        Result result = BusinessRules.run(checkIfCityExists(city));
        if (result != null) return new ErrorDataResult<>(result.getMessage());
        
        return new SuccessDataResult<>(city, messageService.getMessage(Messages.CITY_FOUND));
    }

    private Result checkIfCityExists(City city) {
        if (city == null) {
            return new ErrorResult(messageService.getMessage(Messages.CITY_NOT_FOUND));
        }
        return new SuccessResult();
    }
}
