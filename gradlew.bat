@rem Arena Checkpoint Gradle entry point for Windows.
@rem Restores the wrapper jar from the ASCII sidecar, or downloads the NeoForge MDK copy.
@if "%DEBUG%"=="" @echo off
setlocal
set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
for %%i in ("%DIRNAME%") do set APP_HOME=%%~fi

if not exist "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" (
  powershell -NoProfile -Command "$o=Join-Path $env:APP_HOME 'gradle\wrapper\gradle-wrapper.jar'; $p=Join-Path $env:APP_HOME 'gradle\wrapper\gradle-wrapper.jar.base64'; $sha='7d3a4ac4de1c32b59bc6a4eb8ecb8e612ccd0cf1ae1e99f66902da64df296172'; if (Test-Path $p) { [IO.File]::WriteAllBytes($o, [Convert]::FromBase64String(((Get-Content -Raw $p) -replace '\s',''))) } else { Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle/main/gradle/wrapper/gradle-wrapper.jar' -OutFile $o }; if ((Get-FileHash -Algorithm SHA256 $o).Hash.ToLower() -ne $sha) { Remove-Item $o -Force; throw 'Gradle wrapper jar hash mismatch' }"
  if errorlevel 1 exit /b 1
)

if defined JAVA_HOME (
  set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA_EXE=java.exe"
)

"%JAVA_EXE%" -Dorg.gradle.appname=%~n0 -jar "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" %*
exit /b %ERRORLEVEL%
