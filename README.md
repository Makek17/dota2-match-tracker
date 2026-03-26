# Dota 2 Friend Match Tracker

Веб-застосунок на **Spring Boot 3** для відслідковування матчів ваших друзів у Dota 2. Дозволяє зручно збирати статистику з відкритих профілів, зберігати історію в базі даних та візуалізувати результати за допомогою графіків.

## Основні можливості

*   **Додавання гравців:** Підтримка як SteamID64 (з URL профілю), так і старого Account ID (SteamID32).
*   **Синхронізація з OpenDota API:** Автоматичне підтягування останніх матчів. Вбудована дедублікація - один запис матчу на базу, незалежно від кількості відслідковуваних друзів, що брали в ньому участь.
*   **Гнучка статистика:** Формування агрегованих даних (середні Kills, Deaths, Assists, KDA, GPM, XPM).
*   **Фільтрація за часом:** Відображення матчів за останній тиждень, місяць, рік або за весь час.
*   **Візуалізація (Chart.js):** Інтерактивні графіки зміни показників KDA, Kills, Deaths, Assists, GPM та XPM.
*   **Точний розрахунок Win/Loss:** Коректне визначення перемоги чи поразки конкретного гравця на основі його ігрового слоту (Radiant / Dire).

## Технологічний стек

*   **Backend:** Java 17+, Spring Boot 3.x, Spring Data JPA, Spring Web
*   **База даних:** PostgreSQL
*   **Frontend (SSR):** Thymeleaf, HTML5, CSS3
*   **Візуалізація:** Chart.js
*   **Збірка:** Maven
*   **Зовнішні API:** [OpenDota API](https://docs.opendota.com/)

## Налаштування та запуск

### 1. Підготовка бази даних
Переконайтеся, що у вас встановлено PostgreSQL. Створіть базу даних (наприклад, `dota2tracker`).

### 2. Налаштування `application.yml`
У файлі `src/main/resources/application.yml` вкажіть свої доступи до БД:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dota2tracker
    username: postgres
    password: secretpassword
  jpa:
    hibernate:
      ddl-auto: update # При першому запуску створить таблиці

opendota:
  api:
    base-url: https://api.opendota.com/api
    api-key: "" # Опціонально: додайте свій API ключ OpenDota для збільшення ліміту запитів
```

### 3. Запуск застосунку
Запустіть застосунок через Maven за допомогою команди:

```bash
./mvnw spring-boot:run
```
Або відкрийте проект у вашому IDE (IntelliJ IDEA, VS Code) і запустіть клас `Dota2TrackerApplication`.

### 4. Використання
Після успішного запуску відкрийте браузер за адресою:
**`http://localhost:8080`**

## Структура бази даних

*   **`players`** - інформація про відслідковувані Steam-акаунти.
*   **`matches`** - загальні дані про матч (ID, тривалість, режим, лобі, яка команда перемогла).
*   **`player_match_stats`** - join-таблиця зі статистикою конкретного гравця у конкретному матчі (герой, KDA, слоти тощо).

## REST API

Окрім веб-інтерфейсу, доступні базові REST-ендпоінти під префіксом `/api/players`:

*   `POST /api/players` - додати гравця
*   `GET /api/players` - список гравців
*   `POST /api/players/{id}/sync` - запустити синхронізацію матчів з OpenDota
*   `GET /api/players/{id}/stats` - отримати збережену статистику з бази
*   `DELETE /api/players/{id}` - видалити гравця та його особисту статистику

## Інтерфейс
* Головна сторінка - (https://imgur.com/a/hiYyyf0)
* Графіки статистики - (https://imgur.com/a/RPBDLxh)


## Автор

**Максим Чорний** - GitHub: (https://github.com/Makek17)

