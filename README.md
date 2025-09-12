# InfoLogia Telegram Bot

Образовательный телеграм бот для InfoLogia.kz - платформы подготовки к ЕНТ по информатике.

## Возможности

- 🤖 **Основные команды**: /start, /help, /info
- 🎓 **О InfoLogia**: /about - информация об образовательной платформе
- 💪 **Мотивация**: /quote - вдохновляющие цитаты для учебы
- 👥 **База данных**: Сохранение информации о пользователях
- 📊 **Логирование**: Все взаимодействия записываются в лог
- 🔧 **Гибкая конфигурация**: Настройка через application.properties

## Требования

- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Telegram Bot Token (получить у @BotFather)

## Установка и запуск

### 1. Создание Telegram бота

1. Напиши @BotFather в Telegram
2. Выполни команду `/newbot`
3. Следуй инструкциям для создания бота
4. Сохрани полученный токен

### 2. Настройка базы данных

```sql
CREATE DATABASE infologia_bot;
```

### 3. Настройка переменных окружения

Создай файл `.env` или установи переменные окружения:

```bash
export TELEGRAM_BOT_TOKEN="your_bot_token_here"
export TELEGRAM_BOT_USERNAME="your_bot_username_here"
export DB_USERNAME="postgres"
export DB_PASSWORD="your_password"
```

### 4. Запуск приложения

```bash
# Сборка проекта
mvn clean install

# Запуск
mvn spring-boot:run
```

## Конфигурация

Основные настройки находятся в `src/main/resources/application.properties`:

- `telegram.bot.token` - токен бота
- `telegram.bot.username` - имя пользователя бота
- Настройки базы данных PostgreSQL
- Настройки логирования

## Структура проекта

```
src/main/java/kz/infologia/bot/
├── BotApplication.java          # Главный класс приложения
├── bot/
│   └── InfologiaBot.java       # Основной класс бота
├── config/
│   ├── BotConfig.java          # Конфигурация бота
│   └── BotConfiguration.java   # Регистрация бота
├── model/
│   └── User.java               # Модель пользователя
├── repository/
│   └── UserRepository.java     # Репозиторий для работы с пользователями
└── service/
    └── UserService.java        # Сервис для работы с пользователями
```

## Команды бота

### 🎯 Основные команды
- `/start` - начать работу с ботом
- `/help` - показать справку по всем командам
- `/info` - информация о боте

### 🎓 О InfoLogia
- `/about` - узнать об образовательной платформе InfoLogia

### 💪 Мотивация
- `/quote` - вдохновляющая цитата для учебы

## Разработка

### Добавление новых команд

1. Добавь обработку команды в метод `onUpdateReceived` класса `InfologiaBot`
2. Создай соответствующий метод для отправки ответа

### Добавление новых моделей

1. Создай Entity класс в пакете `model`
2. Создай Repository интерфейс в пакете `repository`
3. Создай Service класс в пакете `service`
4. Добавь Liquibase changelog для создания таблицы

## Логирование

Приложение использует SLF4J с Logback. Логи сохраняются в консоль с уровнем DEBUG для пакетов бота.

## Лицензия

Этот проект создан для Infologia.kz
