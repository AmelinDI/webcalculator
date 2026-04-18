# Calc — Локальный калькулятор

REST-сервис с веб-интерфейсом для выполнения арифметических операций.

## API

**POST** `/api/calc`

**Request body:**
```json
{"a": 10, "b": 2, "op": "+"}
```

**Response:**
```json
{"result": 12}
```

**Операции:** `+`, `-`, `*`, `/`, `%`, `^`

**Ошибки:** при делении на ноль или неизвестной операции — `400 Bad Request`
```json
{"error": "Division by zero"}
```

## Запуск

```bash
mvn spring-boot:run
```

Frontend доступен по адресу **http://localhost:8080**.

## Структура

| Файл | Назначение |
|------|-----------|
| `Main.java` | Точка входа, Spring Boot приложение |
| `CalcController.java` | REST-контроллер, логика вычислений |
| `CalcRequest.java` | Запрос: a, b, op |
| `CalcResponse.java` | Ответ: result |
| `ApiError.java` | Формат ошибок |
| `GlobalExceptionHandler.java` | Перехват исключений → HTTP 400 |
| `static/index.html` | Веб-интерфейс |
