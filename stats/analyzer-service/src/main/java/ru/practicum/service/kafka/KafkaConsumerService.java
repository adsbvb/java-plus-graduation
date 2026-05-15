package ru.practicum.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dal.EventSimilarityRepository;
import ru.practicum.dal.UserActionRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.mapper.SimilarityMapper;
import ru.practicum.mapper.UserActionMapper;
import ru.practicum.model.EventSimilarityEntity;
import ru.practicum.model.UserActionEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final EventSimilarityRepository similarityRepository;
    private final UserActionRepository actionRepository;
    private final SimilarityMapper similarityMapper;
    private final UserActionMapper actionMapper;


    @KafkaListener(topics = "${kafka.topic.user-actions:stats.user-actions.v1}",
            containerFactory = "userActionKafkaListenerContainerFactory")
    @Transactional
    public void consumeUserActions(List<UserActionAvro> messages) {
        log.info("Получена пакет из {} действий пользователей из Kafka", messages.size());

        List<Long> userIds = messages.stream().map(UserActionAvro::getUserId).toList();
        List<Long> eventIds = messages.stream().map(UserActionAvro::getEventId).toList();

        Map<String, UserActionEntity> existingMap = actionRepository
                .findByUserIdInAndEventIdIn(userIds, eventIds)
                .stream()
                .collect(Collectors.toMap(e -> e.getUserId() + ":" + e.getEventId(), e -> e));

        List<UserActionEntity> toSave = new ArrayList<>();
        List<UserActionEntity> toUpdate = new ArrayList<>();

        for (UserActionAvro action : messages) {
            String key = action.getUserId() + ":" + action.getEventId();
            UserActionEntity existing = existingMap.get(key);

            if (existing != null) {
                double newWeight = actionMapper.toWeight(action.getActionType());
                double currentWeight = existing.getWeight();

                if (newWeight > currentWeight) {
                    actionMapper.updateEntity(existing, action);
                    existing.setWeight(newWeight);
                    toUpdate.add(existing);
                }
            } else {
                toSave.add(actionMapper.toEntity(action));
            }
        }

        if (!toSave.isEmpty()) actionRepository.saveAll(toSave);
        if (!toUpdate.isEmpty()) actionRepository.saveAll(toUpdate);

        log.info("Обработано: saved={}, updated={}", toSave.size(), toUpdate.size());
    }

    @Transactional
    @KafkaListener(topics = "${kafka.topic.similarity:stats.events-similarity.v1}",
            containerFactory = "similarityKafkaListenerContainerFactory")
    public void consumeSimilarityEvents(List<EventSimilarityAvro> messages) {
        log.info("Получен пакет из {} событий сходства из Kafka", messages.size());

        List<Long> eventAs = messages.stream().map(EventSimilarityAvro::getEventA).toList();
        List<Long> eventBs = messages.stream().map(EventSimilarityAvro::getEventB).toList();

        Map<String, EventSimilarityEntity> existingMap = similarityRepository
                .findByEventAInAndEventBIn(eventAs, eventBs)
                .stream()
                .collect(Collectors.toMap(
                        e -> e.getEventA() + ":" + e.getEventB(),
                        e -> e
                ));

        List<EventSimilarityEntity> toSave = new ArrayList<>();
        List<EventSimilarityEntity> toUpdate = new ArrayList<>();

        for (EventSimilarityAvro similar : messages) {
            String key = similar.getEventA() + ":" + similar.getEventB();
            EventSimilarityEntity existing = existingMap.get(key);

            if (existing != null) {
                similarityMapper.updateEntity(existing, similar);
                toUpdate.add(existing);
            } else {
                toSave.add(similarityMapper.toEntity(similar));
            }
        }

        if (!toSave.isEmpty()) similarityRepository.saveAll(toSave);
        if (!toUpdate.isEmpty()) similarityRepository.saveAll(toUpdate);

        log.info("Обработано: saved={}, updated={}", toSave.size(), toUpdate.size());
    }
}