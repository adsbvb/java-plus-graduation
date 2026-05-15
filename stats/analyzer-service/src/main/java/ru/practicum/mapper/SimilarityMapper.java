package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.model.EventSimilarityEntity;

@Mapper(componentModel = "spring")
public interface SimilarityMapper {

    @Mapping(source = "timestamp", target = "updatedAt")
    @Mapping(target = "id", ignore = true)
    EventSimilarityEntity toEntity(EventSimilarityAvro avro);

    @Mapping(source = "timestamp", target = "updatedAt")
    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget EventSimilarityEntity entity, EventSimilarityAvro avro);
}
