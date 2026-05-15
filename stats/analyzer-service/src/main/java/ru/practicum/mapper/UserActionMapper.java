package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.model.ActionType;
import ru.practicum.model.UserActionEntity;

@Mapper(componentModel = "spring")
public interface UserActionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "weight", source = "actionType", qualifiedByName = "toWeight")
    @Mapping(source = "actionType", target = "actionType", qualifiedByName = "toActionType")
    @Mapping(source = "timestamp", target = "lastActionTime")
    UserActionEntity toEntity(UserActionAvro avro);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "actionType", target = "actionType", qualifiedByName = "toActionType")
    @Mapping(source = "timestamp", target = "lastActionTime")
    @Mapping(target = "weight", ignore = true)
    void updateEntity(@MappingTarget UserActionEntity entity, UserActionAvro avro);

    @Named("toActionType")
    default ActionType toActionType(ActionTypeAvro avroType) {
        switch (avroType) {
            case VIEW: return ActionType.VIEW;
            case REGISTER: return ActionType.REGISTER;
            case LIKE: return ActionType.LIKE;
            default: throw new IllegalArgumentException("Неизвестный тип действия: " + avroType);
        }
    }

    @Named("toWeight")
    default Double toWeight(ActionTypeAvro actionType) {
        switch (actionType) {
            case VIEW:
                return 0.4;
            case REGISTER:
                return 0.8;
            case LIKE:
                return 1.0;
            default:
                return 0.0;
        }
    }
}
