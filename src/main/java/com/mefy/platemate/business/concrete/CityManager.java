package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.ICityService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
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
        if (city == null) return new ErrorDataResult<>(messageService.getMessage(Messages.CITY_NOT_FOUND));
        return new SuccessDataResult<>(city, messageService.getMessage(Messages.CITY_FOUND));
    }
}
