# Управление комментариями (Comments API)

API для работы с комментариями к событиям и вложенным комментариям.

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
  "text": "text",
  "event": "event",
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
  "text": "text",
  "event": "event",
  "create": "yyyy-MM-dd HH:mm:ss",
  "author": "author"
}
```
**Параметры:**

{userId} — ID пользователя;

{eventId} — ID события;

{commentId} — ID редактируемого комментария.

#### 3. Постановка лайка/дизлайка комментарию
___
**Метод:** `POST`
**Endpoint:** `/users/{userId}/events/{eventId}/comments/{commentId}?like=true`

**Параметры запроса:**

like=true — поставить лайк;

like=false — поставить дизлайк.

**Ответ (201 Updated):**

**Параметры URL:**

{userId} — ID пользователя;

{eventId} — ID события;

{commentId} — ID комментария.

#### 4. Создание вложенного комментария (ответа на комментарий)
___
**Метод:** `POST`
**Endpoint:** `/users/{userId}/comments/{commentId}`

**Запрос:**

```json
{
  "text": "text"
}
```
**Ответ (201 Created):**

```json
{
  "text": "text",
  "comment": "comment",
  "create": "yyyy-MM-dd HH:mm:ss",
  "author": "author"
}
```
**Параметры:**

{userId} — ID пользователя, пишущего ответ;

{commentId} — ID родительского комментария.

#### 5. Изменение вложенного комментария
___
**Метод:** `PATCH`
**Endpoint:** `/users/{userId}/comments/{commentId}`

**Запрос:**

```json
{
  "text": "text"
}
```
**Ответ 200:**

```json
{
  "text": "text",
  "comment": "comment",
  "create": "yyyy-MM-dd HH:mm:ss",
  "author": "author"
}
```
**Параметры:**

{userId} — ID пользователя;

{commentId} — ID вложенного комментария.

### Public API (без авторизации)
#### 6. Получение комментариев к событию
___
**Метод:** `GET`
**Endpoint:** `/events/{eventId}/comments`

**Ответ (200 OK):**

```json
[
  {
    "text": "text",
    "comment": "comment",
    "create": "yyyy-MM-dd HH:mm:ss",
    "author": "author"
  },
  {
    "text": "text",
    "comment": "comment",
    "create": "yyyy-MM-dd HH:mm:ss",
    "author": "author"
  }
]
```
**Параметры:**

{eventId} — ID события, для которого запрашиваются комментарии.

### Admin API (для администраторов)
#### 7. Удаление комментария
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
text — текст комментария (строка);

event — идентификатор события (строка/число);

comment — идентификатор родительского комментария (для вложенных комментариев);

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
### 3. Поставить лайк комментарию:

`PATCH /users/123/events/456/comments/789?like=true`
### 4. Получить комментарии к событию:

`GET /events/456/comments`
### 5. Удалить комментарий (админ):

`DELETE /admin/comments/789`