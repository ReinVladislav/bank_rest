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

---

## Примечания

- Порт сервера по умолчанию: `8081`
- Для базы данных используется PostgreSQL на порту `5432`
- При необходимости можно изменить настройки в `application.yml` и `docker-compose.yml`