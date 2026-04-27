package com.mefy.platemate.core.utilities.mappers;

import com.mefy.platemate.entities.concrete.Vehicle;
import com.mefy.platemate.entities.dto.VehicleDto;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper implements ModelMapperService<Vehicle, VehicleDto> {
    @Override
    public VehicleDto entityToDto(Vehicle entity) {
        if (entity == null) return null;
        VehicleDto dto = new VehicleDto();
        dto.setId(entity.getId());
        dto.setPlateCode(entity.getPlateCode());
        dto.setBrand(entity.getBrand());
        dto.setModel(entity.getModel());
        dto.setColor(entity.getColor());

        if (entity.getCity() != null) {
            dto.setCityName(entity.getCity().getName());
        }
        return dto;
    }

    @Override
    public Vehicle dtoToEntity(VehicleDto dto) { return null; }
}
