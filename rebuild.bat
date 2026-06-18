@echo off
cd /d "D:\提交\软件工程实践\kai-fa-longingstays"
setlocal enabledelayedexpansion

REM Build classpath from lib jars
set CP=target\classes
for %%f in (target\lib\*.jar) do set CP=!CP!;%%f

echo Compiling all source files...
javac -cp "!CP!" -d target\classes -sourcepath src\main\java src\main\java\cn\edu\whut\sept\zuul\*.java
echo Exit code: %ERRORLEVEL%

if %ERRORLEVEL% equ 0 (
  echo === Compilation success! ===
  echo Updating JAR with all classes...
  cd target\classes
  jar uf ..\..\target\zuul-1.0-SNAPSHOT.jar cn\edu\whut\sept\zuul\*.class cn\edu\whut\sept\zuul\db\*.class
  cd ..\..
  echo === JAR updated! ===
) else (
  echo === Compilation failed! ===
)
endlocal