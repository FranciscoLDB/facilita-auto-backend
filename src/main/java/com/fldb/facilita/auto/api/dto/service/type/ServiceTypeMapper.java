package com.fldb.facilita.auto.api.dto.service.type;

import com.fldb.facilita.auto.domain.entity.ServiceType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ServiceTypeMapper {

    ServiceTypeMapper INSTANCE = Mappers.getMapper(ServiceTypeMapper.class);

    void updateEntityFromDto(UpdateServiceTypeRequest dto, @MappingTarget ServiceType entity);
}
