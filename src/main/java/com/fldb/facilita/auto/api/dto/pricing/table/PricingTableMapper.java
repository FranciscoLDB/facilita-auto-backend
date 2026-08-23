package com.fldb.facilita.auto.api.dto.pricing.table;

import com.fldb.facilita.auto.domain.entity.PricingTable;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PricingTableMapper {

    PricingTableMapper INSTANCE = Mappers.getMapper(PricingTableMapper.class);

    void updateEntityFromDto(UpdatePricingTableRequest dto, @MappingTarget PricingTable entity);
}