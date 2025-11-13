# Система управления банковскими картами

## Инструкция по запуску

1. **Сборка проекта**

```bash
mvn clean package
```

2. **Запуск Docker**

```bash
docker-compose up -d --build
```

---

## Доступ к Swagger UI

Swagger UI доступен по ссылке:  
[http://localhost:8081/swagger-ui/index.html#/](http://localhost:8081/swagger-ui/index.html#/)

Также OpenAPI спецификация сохранена в файл `openapi.yaml` в папке `/docs`.

Дополнительно для работы с картами пользователей доступен метод пополнения баланса:  
[http://localhost:8081/swagger-ui/index.html#/User%20Cards/deposit](http://localhost:8081/swagger-ui/index.html#/User%20Cards/deposit)

---

## Инициализация базы данных

В базе проинициализированы следующие данные:
- Пользователи:
    - `admin` (роль ADMIN, пароль совпадает с username)
    - `user1` (роль USER, пароль совпадает с username)
    - `user2` (роль USER, пароль совпадает с username)
- Для каждого пользователя с ролью USER созданы по 2 карты с разным балансом.

---

## Примечания

- Порт сервера по умолчанию: `8081`
- Для базы данных используется PostgreSQL на порту `5432`
- При необходимости можно изменить настройки в `application.yml` и `docker-compose.yml`

