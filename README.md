# InfoLogia Telegram Bot

## Overview

This project delivers a Telegram bot built with Spring Boot 3 and the official Telegram Bots Java SDK. The bot uses password-based authentication with role-aware command access backed by PostgreSQL.

## Authentication Flow

- `/register <password>` – create a bot account and log in during the same step.
- `/login <password>` – authenticate with the password set at registration.
- `/logout` – clear the active session for the current Telegram user.
- `/status` – display role and authorization state.
- `/profile [telegram_id]` – show the student profile for the current user; curators/admins may pass another Telegram ID.
- Protected commands such as `/quote`, `/info`, and `/profile` can only be used after a successful login.

Passwords are stored as BCrypt hashes. Every new Telegram user starts with the `STUDENT` role, while `CURATOR` and `ADMIN` can be assigned manually for extended access.

## Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL 13+ reachable by the application

## Configuration

### Environment variables

Set the following variables (or provide equivalents through your preferred secrets manager):

- `TELEGRAM_BOT_TOKEN`
- `TELEGRAM_BOT_USERNAME`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### Local dev profile

For quick experiments with a dedicated `users` database, activate the `dev` Spring profile:

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
mvn spring-boot:run
```

Adjust the defaults in `src/main/resources/application-dev.properties` to match your local PostgreSQL credentials. Production deployments should provide explicit environment variables instead of relying on the demo profile.

The application uses `spring.jpa.hibernate.ddl-auto=update` for convenience; use migrations for production stability.

## Build and Run

```bash
mvn clean package
java -jar target/bot-0.0.1-SNAPSHOT.jar
```

Alternatively use the existing `build_and_run.bat` helper if it already suits your workflow.

## Useful References

- `src/main/java/kz/infologia/bot/bot/InfologiaBot.java` – command routing and authorization checks.
- `src/main/java/kz/infologia/bot/service/UserService.java` – registration, login, and logout workflows.
- `src/main/java/kz/infologia/bot/service/StudentProfileService.java` – profile provisioning for new users.
- `src/main/java/kz/infologia/bot/command/ProfileCommand.java` – `/profile` command implementation.
- `src/main/java/kz/infologia/bot/model/StudentProfile.java` – persistence model for course data.
