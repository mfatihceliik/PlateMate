package com.mefy.platemate.core.utilities.mappers;

public interface IMapper<E, D> {
    D entityToDto(E entity);

    E dtoToEntity(D dto);
}
