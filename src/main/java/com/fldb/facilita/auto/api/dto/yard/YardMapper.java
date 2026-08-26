package com.fldb.facilita.auto.api.dto.yard;

import com.fldb.facilita.auto.api.dto.AddressDto;
import com.fldb.facilita.auto.domain.entity.company.Yard;
import com.fldb.facilita.auto.domain.model.Address;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface YardMapper {

    YardMapper INSTANCE = Mappers.getMapper(YardMapper.class);

    @Mapping(target = "address", ignore = true)
    @Mapping(target = "manager", ignore = true)
    void updateEntityFromDto(UpdateYardRequest dto, @MappingTarget Yard entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAddressFromDto(AddressDto dto, @MappingTarget Address address);
}