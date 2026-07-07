package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;

import com.mefy.platemate.entities.concrete.City;
import com.mefy.platemate.entities.dto.CityDto;
import org.springframework.stereotype.Component;

@Component
public class CityMapper implements IMapper<City, CityDto> {
    @Override
    public CityDto entityToDto(City entity) {
        if (entity == null) return null;
        CityDto dto = new CityDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }

    @Override
    public City dtoToEntity(CityDto dto) {
        if (dto == null) return null;
        City entity = new City();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        return entity;
    }
}


