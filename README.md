# Управление комментариями (Comments API)

API для работы с комментариями к событиям.

## Endpoints

### Private API (для авторизованных пользователей)

#### 1. Создание комментария к событию
___
**Метод:** `POST`  
**Endpoint:** `/users/{userId}/events/{eventId}/comments`

**Запрос:**
```json
{
  "text": "text"
}
```
**Ответ (201 Created):**

```json
{
  "id": 2,
  "text": "text",
  "event": 1,
  "create": "yyyy-MM-dd HH:mm:ss",
  "author": "author"
}
```
**Параметры:**

{userId} — ID пользователя, создающего комментарий;

{eventId} — ID события, к которому добавляется комментарий.

#### 2. Изменение комментария
___
**Метод:** `PATCH`
**Endpoint:** `/users/{userId}/events/{eventId}/comments/{commentId}`

**Запрос:**

```json
{
  "text": "text"
}
```
**Ответ 200:**

```json
{
  "id": 2,
  "text": "text",
  "event": 1,
  "create": "yyyy-MM-dd HH:mm:ss",
  "author": "author"
}
```
**Параметры:**

{userId} — ID пользователя;

{eventId} — ID события;

{commentId} — ID редактируемого комментария.

#### 3. Удаление комментария пользователем
___
**Метод:** `DELETE`
**Endpoint:** `/users/{userId}/events/{eventId}/comments/{commentId}`

**Ответ:**

204 (No Content) — комментарий успешно удалён;

404 Not Found — комментарий не найден.

**Параметры:**

{userId} — ID пользователя;

{eventId} — ID события;

{commentId} — ID удаляемого комментария.


#### 4. Постановка лайка комментарию
___
**Метод:** `POST`
**Endpoint:** `/users/{userId}/events/{eventId}/comments/{commentId}/likes`

**Ответ 200:**

**Параметры URL:**

{userId} — ID пользователя;

{eventId} — ID события;

{commentId} — ID комментария.

### Public API (без авторизации)
#### 5. Получение комментариев к событию
___
**Метод:** `GET`
**Endpoint:** `/events/{eventId}/comments?from=0&size=100`

**Ответ (200 OK):**

Данные ответа должны сортироваться по дате

```json
[
  {
    "id": 2,
    "text": "text",
    "event": 1,
    "create": "yyyy-MM-dd HH:mm:ss",
    "author": "author",
    "like": 10
  },
  {
    "id": 3,
    "text": "text",
    "event": 1,
    "create": "yyyy-MM-dd HH:mm:ss",
    "author": "author",
    "like": 5
  }
]
```
**Параметры:**

{eventId} — ID события, для которого запрашиваются комментарии.

{from} — начало получение комментариев

{size} — количество получаемых комментариев

### Admin API (для администраторов)
#### 6. Удаление комментария
___
**Метод:** `DELETE`
**Endpoint:** `/admin/comments/{commentId}`

**Ответ:**

204 (No Content) — комментарий успешно удалён;

404 Not Found — комментарий не найден.

**Параметры:**

{commentId} — ID удаляемого комментария.

## Формат данных
___
```
id — идентификатор коментария (число);

text — текст комментария (строка);

event — идентификатор события (число);

create — дата и время создания в формате yyyy-MM-dd HH:mm:ss;

author — имя автора комментария (строка).
```
## Коды ответов
___
**201 Created** — ресурс успешно создан;

**200 Newton Updated** — ресурс успешно обновлён;

**204 (No Content)** — запрос выполнен успешно (для DELETE);

**404 Not Found** — ресурс не найден.

## Примеры использования
___
### 1. Создать комментарий к событию:

`POST /users/123/events/456/comments`
```
{
"text": "Отличный пост!"
}
```
### 2. Изменить комментарий:

`PATCH /users/123/events/456/comments/789`
```
{
"text": "Исправленный текст"
}
```
### 3. Удалить комментарий (пользователь):

`DELETE /users/26/events/4/comments/789`
### 4. Поставить лайк комментарию:

`POST /users/123/events/456/comments/789/likes`
### 5. Получить комментарии к событию:

`GET /events/{eventId}/comments?from=0&size=100`
### 6. Удалить комментарий (админ):

`DELETE /admin/comments/789`