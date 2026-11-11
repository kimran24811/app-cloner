@echo off
setlocal
echo ========================================================
echo               Building OpenCloner APK
echo ========================================================

set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Using Java from: %JAVA_HOME%
java -version

echo Starting Gradle assembleDebug...
call gradlew.bat assembleDebug --stacktrace

if %ERRORLEVEL% equ 0 (
    echo.
    echo ========================================================
    echo Build SUCCESS! 
    echo APK Location: app\build\outputs\apk\debug\app-debug.apk
    echo ========================================================
) else (
    echo.
    echo ========================================================
    echo Build failed. If Android SDK is needed locally, push to 
    echo GitHub to trigger automatic cloud APK build in 2 minutes!
    echo ========================================================
)

pause
