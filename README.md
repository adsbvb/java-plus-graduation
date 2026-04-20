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
#### 6. Поиск комментариев с фильтрацией
___
**Метод:** `GET`
**Endpoint:** `/admin/comments?eventId=...&userId=...&text=...&from=0&size=10`

**Параметры запроса (все опциональны):**

{eventId} — фильтрация по ID события.

{userId} — фильтрация по ID автора комментария.

{text} — поиск по тексту комментария (частичное совпадение).

{from} — начало пагинации (по умолчанию 0).

{size} — количество элементов на странице (по умолчанию 10).

**Ответ:**

200 (OK)

```json
[
  {
    "id": 2,
    "text": "text",
    "authorId": 123,
    "eventId": 456,
    "createdOn": "yyyy-MM-dd HH:mm:ss",
    "likesCount": 10
  },
  {
    "id": 3,
    "text": "another text",
    "authorId": 124,
    "eventId": 457,
    "createdOn": "yyyy-MM-dd HH:mm:ss",
    "likesCount": 5
  }
]
```
Примечание: Поиск по тексту регистронезависимый и работает по частичному совпадению.

#### 7. Получение комментария по ID
___
**Метод:** `GET`
**Endpoint:** `/admin/comments/{commentId}`

**Параметры запроса:**

{commentId} - ID комментария.

**Ответ:** 200 (OK):

```json
[
  {
    "id": 2,
    "text": "text",
    "authorId": 123,
    "eventId": 456,
    "createdOn": "yyyy-MM-dd HH:mm:ss",
    "likesCount": 10
  }
]
```
Ответ: 404 (Not Found): комментарий не найден.

### 8. Удаление комментария администратором
___
**Метод:** `DELETE`
**Endpoint:** `/admin/comments/{commentId}`

204 (No Content) — комментарий успешно удалён;

404 (Not Found) — комментарий не найден.

Параметры:
{commentId} — ID удаляемого комментария.

## Формат данных для Private/Public API:
___
```
id — идентификатор коментария (число);

text — текст комментария (строка);

event — идентификатор события (число);

create — дата и время создания в формате yyyy-MM-dd HH:mm:ss;

author — имя автора комментария (строка).
```

## Формат данных для Admin API:
___
```
id — идентификатор комментария (число);

text — текст комментария (строка);

authorId — ID автора комментария (число);

eventId — ID события (число);

createdOn — дата и время создания в формате yyyy-MM-dd HH:mm:ss;

likesCount — количество лайков (число).
```

## Коды ответов
___
**201 Created** — ресурс успешно создан;

**200 OK** — ресурс успешно обновлён;

**204 No Content** — запрос выполнен успешно (для DELETE);

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

`GET /admin/comments?eventId=456&text=отличный&from=0&size=10`
### 6. Поиск комментариев администратором:

`GET /admin/comments/789`
### 7. Получить конкретный комментарий (админ):

`GET /events/{eventId}/comments?from=0&size=100`
### 8. Удалить комментарий (админ):

`DELETE /admin/comments/789`