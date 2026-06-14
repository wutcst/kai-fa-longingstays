@echo off
cd /d "D:\提交\软件工程实践\kai-fa-longingstays"
setlocal enabledelayedexpansion

REM Build classpath from lib jars
set CP=target\classes
for %%f in (target\lib\*.jar) do set CP=!CP!;%%f

echo Compiling...
javac -cp "!CP!" -d target\classes src\main\java\cn\edu\whut\sept\zuul\db\DatabaseManager.java src\main\java\cn\edu\whut\sept\zuul\GameServer.java
echo Exit code: %ERRORLEVEL%

if %ERRORLEVEL% equ 0 (
  echo === Compilation success! ===
  echo Updating JAR...
  jar uf target\zuul-1.0-SNAPSHOT.jar -C target\classes cn\edu\whut\sept\zuul\db\DatabaseManager.class -C target\classes cn\edu\whut\sept\zuul\GameServer.class
  for %%c in (target\classes\cn\edu\whut\sept\zuul\GameServer$*.class) do (
    jar uf target\zuul-1.0-SNAPSHOT.jar -C target\classes cn\edu\whut\sept\zuul\%%~nxc
  )
  echo === JAR updated! ===
) else (
  echo === Compilation failed! ===
)
endlocal
