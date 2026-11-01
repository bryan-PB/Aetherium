@echo off
setlocal
set GRADLE_VERSION=6.8.3
if "%GRADLE_USER_HOME%"=="" set GRADLE_USER_HOME=%USERPROFILE%\.gradle
set CACHE_ROOT=%GRADLE_USER_HOME%\wrapper\dists\aetherium-bootstrap
set GRADLE_HOME=%CACHE_ROOT%\gradle-%GRADLE_VERSION%
set GRADLE_BIN=%GRADLE_HOME%\bin\gradle.bat

if not exist "%GRADLE_BIN%" (
    if not exist "%CACHE_ROOT%" mkdir "%CACHE_ROOT%"
    echo Gradle %GRADLE_VERSION% no esta instalado; descargando distribucion oficial...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; $u='https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip'; $z=Join-Path '%CACHE_ROOT%' 'gradle.zip'; Invoke-WebRequest -Uri $u -OutFile $z; Expand-Archive -LiteralPath $z -DestinationPath '%CACHE_ROOT%' -Force; Remove-Item $z"
    if errorlevel 1 exit /b 1
)

call "%GRADLE_BIN%" %*
exit /b %ERRORLEVEL%
