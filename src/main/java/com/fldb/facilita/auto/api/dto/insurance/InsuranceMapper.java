package com.fldb.facilita.auto.api.dto.insurance;

import com.fldb.facilita.auto.domain.entity.InsuranceCompany;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InsuranceMapper {

    InsuranceMapper INSTANCE = Mappers.getMapper(InsuranceMapper.class);

    void updateEntityFromDto(UpdateInsuranceRequest dto, @MappingTarget InsuranceCompany entity);
}
