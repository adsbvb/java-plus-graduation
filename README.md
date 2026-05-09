# ExploreWithMe (EWM) — Микросервисная архитектура

## Описание проекта

ExploreWithMe — это платформа-афиша для публикации и поиска событий. Проект представляет собой эволюцию монолитного приложения в набор микросервисов, готовых к работе в облачной среде.

### Текущий статус (этап 2):
- ✅ Выполнен переход к микросервисной архитектуре
- ✅ Внедрены Spring Cloud компоненты (Eureka, Gateway, Config Server)

---

## Архитектура проекта

### Выделенные микросервисы

| Сервис | Порт | Описание |
|--------|------|----------|
| **event-service** | 0    | Управление событиями, **категориями** и **подборками** |
| **request-service** | 0    | Управление заявками на участие |
| **user-service** | 0    | Управление пользователями (администрирование) |
| **stat-service** | 0    | Сбор и хранение статистики посещений |
| **gateway-service** | 8080 | Единая точка входа для всех клиентов |
| **discovery-service** | 8761 | Реестр сервисов (Eureka) |
| **config-server** | 8888 | Централизованное управление конфигурациями |

## API Gateway маршруты

Все запросы поступают на `http://localhost:8080` и маршрутизируются в соответствующие микросервисы.

### Маршрутизация запросов

| Путь | Сервис | Описание |
|------|--------|----------|
| `/admin/events/**` | event-service | Администрирование событий |
| `/admin/categories/**` | event-service | Администрирование категорий |
| `/admin/compilations/**` | event-service | Администрирование подборок |
| `/admin/users/**` | user-service | Администрирование пользователей |
| `/users/{userId}/events/**` | event-service | Управление событиями пользователя |
| `/users/{userId}/requests/**` | request-service | Управление заявками на участие |
| `/events/**` | event-service | Публичный просмотр событий |
| `/categories/**` | event-service | Публичный просмотр категорий |
| `/compilations/**` | event-service | Публичный просмотр подборок |

### Полный список эндпоинтов

#### Event Service (события, категории, подборки)

##### Admin эндпоинты

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| GET | `/admin/events` | Поиск событий с фильтрацией |
| PATCH | `/admin/events/{eventId}` | Редактирование события (публикация/отклонение) |
| POST | `/admin/categories` | Добавление новой категории |
| DELETE | `/admin/categories/{catId}` | Удаление категории |
| PATCH | `/admin/categories/{catId}` | Изменение категории |
| POST | `/admin/compilations` | Добавление новой подборки |
| DELETE | `/admin/compilations/{compId}` | Удаление подборки |
| PATCH | `/admin/compilations/{compId}` | Обновление подборки |

##### Private эндпоинты (пользовательские)

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| GET | `/users/{userId}/events` | Получение событий текущего пользователя |
| POST | `/users/{userId}/events` | Добавление нового события |
| GET | `/users/{userId}/events/{eventId}` | Получение информации о событии |
| PATCH | `/users/{userId}/events/{eventId}` | Изменение события |
| GET | `/users/{userId}/events/{eventId}/requests` | Получение заявок на участие |
| PATCH | `/users/{userId}/events/{eventId}/requests` | Изменение статуса заявок |

##### Public эндпоинты (открытые)

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| GET | `/events` | Получение событий с фильтрацией |
| GET | `/events/{id}` | Получение подробной информации о событии |
| GET | `/categories` | Получение списка категорий |
| GET | `/categories/{catId}` | Получение категории по ID |
| GET | `/compilations` | Получение списка подборок |
| GET | `/compilations/{compId}` | Получение подборки по ID |

---

#### User Service (пользователи)

##### Admin эндпоинты

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| GET | `/admin/users` | Получение списка пользователей |
| POST | `/admin/users` | Добавление нового пользователя |
| DELETE | `/admin/users/{userId}` | Удаление пользователя |

---

#### Request Service (заявки на участие)

##### Private эндпоинты

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| GET | `/users/{userId}/requests` | Получение заявок текущего пользователя |
| POST | `/users/{userId}/requests` | Создание заявки на участие |
| PATCH | `/users/{userId}/requests/{requestId}/cancel` | Отмена заявки |

---

### Примеры запросов

```bash
# Публичный поиск событий
GET http://localhost:8080/events?from=0&size=10&text=концерт

# Получение категорий
GET http://localhost:8080/categories

# Создание события пользователем
POST http://localhost:8080/users/1/events
Content-Type: application/json

{
  "annotation": "Интересное событие",
  "category": 1,
  "description": "Полное описание события",
  "eventDate": "2025-12-31 15:10:05",
  "location": {"lat": 55.754167, "lon": 37.62},
  "paid": false,
  "participantLimit": 10,
  "requestModeration": true,
  "title": "Мероприятие"
}

# Создание заявки на участие
POST http://localhost:8080/users/1/requests?eventId=2

# Отмена заявки
PATCH http://localhost:8080/users/1/requests/5/cancel

# Админ: публикация события
PATCH http://localhost:8080/admin/events/1
Content-Type: application/json

{
  "stateAction": "PUBLISH_EVENT"
}