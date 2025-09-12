@echo off
echo Building InfoLogia Bot...
mvnw.cmd clean package -DskipTests

if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo Build successful! Starting application...
java -jar target\bot-0.0.1-SNAPSHOT.jar
pause
