package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IVehicleService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.plate.abstracts.IPlateValidator;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.mappers.VehicleMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.*;
import com.mefy.platemate.dataAccess.abstracts.IVehicleDao;
import com.mefy.platemate.entities.concrete.Vehicle;
import com.mefy.platemate.entities.dto.VehicleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleManager implements IVehicleService {

    private final IVehicleDao vehicleDao;
    private final VehicleMapper vehicleMapper;
    private final IPlateValidator plateValidator;
    private final IMessageService messageService;

    @Override
    public DataResult<List<VehicleDto>> getAll() {
        List<Vehicle> vehicles = vehicleDao.findAll();
        List<VehicleDto> vehicleDtos = vehicles.stream()
                .map(vehicleMapper::entityToDto)
                .collect(Collectors.toList());
        return new SuccessDataResult<>(vehicleDtos, messageService.getMessage(Messages.VEHICLES_LISTED));
    }

    @Override
    public DataResult<VehicleDto> getByPlateCode(String plateCode) {
        String formattedPlate = plateCode.replace(" ", "").toUpperCase();

        Result result = BusinessRules.run(
                checkIfPlateValid(formattedPlate)
        );

        if (result != null) {
            return new ErrorDataResult<>(result.getMessage());
        }

        Vehicle vehicle = vehicleDao.findByPlateCode(formattedPlate).orElse(null);
        Result existsResult = BusinessRules.run(checkIfVehicleExists(vehicle));
        if (existsResult != null) {
            return new ErrorDataResult<>(existsResult.getMessage());
        }

        return new SuccessDataResult<>(vehicleMapper.entityToDto(vehicle), messageService.getMessage(Messages.VEHICLE_FOUND));
    }

    @Override
    public DataResult<List<VehicleDto>> getByUserId(Long userId) {
        List<Vehicle> vehicles = vehicleDao.findByUserId(userId);
        List<VehicleDto> dtos = vehicles.stream()
                .map(vehicleMapper::entityToDto)
                .collect(Collectors.toList());
        return new SuccessDataResult<>(dtos, messageService.getMessage(Messages.VEHICLES_LISTED));
    }

    @Override
    public Result add(Vehicle vehicle) {
        String normalizedPlate = vehicle.getPlateCode().replace(" ", "").toUpperCase();

        Result result = BusinessRules.run(
                checkIfPlateValid(normalizedPlate),
                checkIfPlateExists(normalizedPlate)
        );

        if (result != null) return result;

        vehicle.setPlateCode(normalizedPlate);
        vehicleDao.save(vehicle);
        return new SuccessResult(messageService.getMessage(Messages.VEHICLE_ADDED));
    }

    @Override
    public Result update(Vehicle vehicle, Long currentUserId) {
        String normalizedPlate = vehicle.getPlateCode().replace(" ", "").toUpperCase();
        Vehicle existingVehicle = vehicleDao.findById(vehicle.getId()).orElse(null);
        
        Result result = BusinessRules.run(
                checkIfVehicleExists(existingVehicle),
                checkIfUserAuthorized(existingVehicle, currentUserId),
                checkPlateUpdate(normalizedPlate, existingVehicle)
        );

        if (result != null) return result;


        existingVehicle.setPlateCode(normalizedPlate);
        existingVehicle.setBrand(vehicle.getBrand());
        existingVehicle.setModel(vehicle.getModel());
        existingVehicle.setColor(vehicle.getColor());
        existingVehicle.setCity(vehicle.getCity());

        vehicleDao.save(existingVehicle);
        return new SuccessResult(messageService.getMessage(Messages.VEHICLE_UPDATED));
    }

    @Override
    public Result delete(Long id, Long currentUserId) {
        Vehicle vehicle = vehicleDao.findById(id).orElse(null);

        Result result = BusinessRules.run(
                checkIfVehicleExists(vehicle),
                checkIfUserAuthorized(vehicle, currentUserId)
        );

        if (result != null) return result;

        vehicleDao.deleteById(id);
        return new SuccessResult(messageService.getMessage("vehicle.deleted"));
    }

    /// ----- BUSINESS RULES -----

    private Result checkIfPlateValid(String plateCode) {
        if (!plateValidator.isValid(plateCode)) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_INVALID));
        }
        return new SuccessResult();
    }

    private Result checkIfPlateExists(String plateCode) {
        if (vehicleDao.existsByPlateCode(plateCode)) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_ALREADY_EXISTS));
        }
        return new SuccessResult();
    }

    private Result checkIfVehicleExists(String plateCode) {
        Vehicle vehicle = vehicleDao.findByPlateCode(plateCode).orElse(null);
        Result existsResult = BusinessRules.run(checkIfVehicleExists(vehicle));
        if (existsResult != null) {
            return new ErrorDataResult<>(existsResult.getMessage());
        }
        return new SuccessResult();
    }

    private Result checkIfVehicleExists(Vehicle vehicle) {
        if (vehicle == null) {
            return new ErrorResult(messageService.getMessage(Messages.VEHICLE_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private Result checkIfUserAuthorized(Vehicle vehicle, Long currentUserId) {
        if (vehicle == null) return new SuccessResult();
        
        if (!vehicle.getUser().getId().equals(currentUserId)) {
            return new ErrorResult(messageService.getMessage("vehicle.delete.unauthorized"));
        }
        return new SuccessResult();
    }

    private Result checkPlateUpdate(String newPlateCode, Vehicle existingVehicle) {
        if (existingVehicle == null) return new SuccessResult();
        
        String normalizedPlate = newPlateCode.replace(" ", "").toUpperCase();
        if (!existingVehicle.getPlateCode().equals(normalizedPlate)) {
            Result ruleResult = BusinessRules.run(
                    checkIfPlateValid(normalizedPlate),
                    checkIfPlateExists(normalizedPlate)
            );
            if (ruleResult != null) return ruleResult;
        }
        return new SuccessResult();
    }
}
