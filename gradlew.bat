@echo off
setlocal enabledelayedexpansion
set APP_HOME=%~dp0
set PROPERTIES_FILE=%APP_HOME%gradle\wrapper\gradle-wrapper.properties
if not exist "%PROPERTIES_FILE%" (
  echo Missing %PROPERTIES_FILE%
  exit /b 1
)

for /f "tokens=1,* delims==" %%A in (%PROPERTIES_FILE%) do (
  if "%%A"=="distributionUrl" set DIST_URL=%%B
)
set DIST_URL=%DIST_URL:\:=:%
for %%F in (%DIST_URL%) do set DIST_ZIP=%%~nxF
set DIST_NAME=%DIST_ZIP:.zip=%
if "%GRADLE_USER_HOME%"=="" set GRADLE_USER_HOME=%USERPROFILE%\.gradle
set DIST_DIR=%GRADLE_USER_HOME%\wrapper\dists\%DIST_NAME%
set GRADLE_HOME=%DIST_DIR%\%DIST_NAME%
set GRADLE_BIN=%GRADLE_HOME%\bin\gradle.bat

if not exist "%GRADLE_BIN%" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "New-Item -ItemType Directory -Force '%DIST_DIR%' | Out-Null; $zip='%DIST_DIR%\\%DIST_ZIP%'; if (-not (Test-Path $zip)) { Invoke-WebRequest -Uri '%DIST_URL%' -OutFile $zip }; if (Test-Path '%GRADLE_HOME%') { Remove-Item -Recurse -Force '%GRADLE_HOME%' }; Expand-Archive -Path $zip -DestinationPath '%DIST_DIR%' -Force"
  if errorlevel 1 exit /b 1
)

call "%GRADLE_BIN%" %*
