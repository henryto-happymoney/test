package com.happymoney.cookiecutterservice.entity;

import com.happymoney.cookiecutterservice.domain.Example;
import com.happymoney.cookiecutterservice.entity.web.ExampleDto;
import com.happymoney.cookiecutterservice.entity.dynamo.ExampleDynamo;
import com.happymoney.cookiecutterservice.schema.ExampleEventData;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ExampleMapper {

    ExampleDynamo toDynamo(Example domainModel);

    Example toDomain(ExampleDynamo dynamoDto);

    ExampleDto toJson(Example domainModel);

    Example toDomain(ExampleDto exampleDto);

    ExampleEventData toEvent(Example domainModel);
    
    default String toString(UUID uuid) {
        return uuid.toString();
    }

    default UUID toUuid(String idValue) {
        return UUID.fromString(idValue);
    }

}
