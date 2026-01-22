@echo off
SetLocal EnableDelayedExpansion
set "APP_VERSION=0.9.0"
set "APP_NAME=Indexador Nexus"
set "MAIN_MODULE=org.nexus.indexador/org.nexus.indexador.Main"
set "OUTPUT_DIR=release"

echo [1/4] Cleaning and Building Project...
call mvn clean package
if %errorlevel% neq 0 (
    echo Build failed!
if not defined CI pause
    exit /b %errorlevel%
)

echo [2/4] Preparing Libs...
copy "target\indexador-%APP_VERSION%.jar" "target\libs\" >nul

echo [3/4] Running jpackage...
if exist "%OUTPUT_DIR%" rmdir /s /q "%OUTPUT_DIR%"

set "JPACKAGE_CMD=jpackage"

REM Check if jpackage is in PATH
where jpackage >nul 2>nul
if %errorlevel% neq 0 (
    echo jpackage not found in PATH. Checking JAVA_HOME...
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\jpackage.exe" (
            set "JPACKAGE_CMD=%JAVA_HOME%\bin\jpackage.exe"
        ) else (
            echo jpackage not found in JAVA_HOME.
            goto :JPackageMissing
        )
    ) else (
        echo JAVA_HOME not set. Trying to find via java properties...
        for /f "tokens=2 delims==" %%A in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr "java.home"') do (
            REM Trim leading space
            for /f "tokens=*" %%B in ("%%A") do set "DETECTED_JAVA_HOME=%%B"
        )
        
        if defined DETECTED_JAVA_HOME (
            echo Found JDK at: "!DETECTED_JAVA_HOME!"
            if exist "!DETECTED_JAVA_HOME!\bin\jpackage.exe" (
                set "JPACKAGE_CMD=!DETECTED_JAVA_HOME!\bin\jpackage.exe"
                goto :FoundJPackage
            )
        )
        
        echo jpackage not found via java command.
        goto :JPackageMissing
    )
)

:FoundJPackage
echo Using: "%JPACKAGE_CMD%"

set "ICON_PARAM="
if exist "icon.ico" set "ICON_PARAM=--icon icon.ico"

"%JPACKAGE_CMD%" ^
  --name "%APP_NAME%" ^
  --module-path "target\libs" ^
  --module "%MAIN_MODULE%" ^
  --type app-image ^
  --app-version "%APP_VERSION%" ^
  --description "Editor de indices para Argentum Online" ^
  --vendor "Nexus Community" ^
  --copyright "Copyright 2026 Nexus" ^
  --dest "%OUTPUT_DIR%" ^
  %ICON_PARAM% ^
  --java-options "-Dfile.encoding=UTF-8"

if %errorlevel% neq 0 (
    echo jpackage failed!
if not defined CI pause
    exit /b %errorlevel%
)

echo [4/4] Done! Installer generated in %OUTPUT_DIR% folder.
if not defined CI pause
exit /b 0

:JPackageMissing
echo.
echo Error: jpackage.exe not found!
echo Please ensure you have JDK 14+ installed and configured.
echo You can set JAVA_HOME to your JDK installation directory.
if not defined CI pause
exit /b 1
