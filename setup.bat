@echo off
echo ========================================
echo    Axion Bot Setup Script
echo ========================================
echo.

echo Checking Java version...
java -version
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or newer
    pause
    exit /b 1
)

echo.
echo Checking Maven...
mvn -version
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven 3.6 or newer
    pause
    exit /b 1
)

echo.
echo Building Axion Bot...
mvn clean compile
if %errorlevel% neq 0 (
    echo ERROR: Build failed
    pause
    exit /b 1
)

echo.
echo Running tests...
mvn test
if %errorlevel% neq 0 (
    echo WARNING: Some tests failed
)

echo.
echo Creating JAR file...
mvn package
if %errorlevel% neq 0 (
    echo ERROR: Package creation failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo    Setup Complete!
echo ========================================
echo.
echo Your bot JAR file is located at:
echo target\axion-bot-1.0.0.jar
echo.
echo To run the bot:
echo 1. Set your Discord token as environment variable:
echo    set DISCORD_TOKEN=your_bot_token_here
echo.
echo 2. Run the bot:
echo    java -jar target\axion-bot-1.0.0.jar
echo.
echo For more information, see README.md and MODERATION_GUIDE.md
echo.
pause