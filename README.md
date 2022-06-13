# Пример использования spring-data-rest

## Tестовое задание: рест сервис в объеме CRUD
Реализовать сервис в объеме CRUD по управлению информацией "Проекты -
Исполнители - Руководители проектов"

### Стэк:
- Kotlin/Java
- Gradle
- Docker
- SpringBoot(spring-data, spring-data-rest)
- Liquibase
- H2

### Функционал:
- Настройка приложения за пределами докера (переменные окружения зпдпваемые в параметрах запуска контейнера докера)
- Логирование, запись за пределами докера, монтируемый раздел локера, переменная окружения ZENITH_APP_FOLDER
- Файл базы H2 за пределами докера, монтируемый раздел локера, переменная окружения ZENITH_APP_FOLDER
- Миграция базы данных ликибэйз, \src\main\resources\db\changelog\db.changelog-master.yaml
- Отключен корс, @CrossOrigin(origins = ["*"], allowCredentials = "true")
- Юнитест - интеграционный: создание, получение, изменение, удаление сущьностей

### Дополнительно:
- HAL Explorer: /
- Swagger(OpenApi): /api/doc

### Запуск сервиса:
- gradle clean
- gradle build
- gradle docker
- gradle dockerRun
- Проверка работы сервиса: http://localhost:8080/, http://localhost:8080/api/doc