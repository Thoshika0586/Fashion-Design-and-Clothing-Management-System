@echo off
REM Compiles the backend. Requires the MySQL Connector/J jar in lib\
if not exist bin mkdir bin
echo Compiling Java sources...
dir /s /b src\*.java > sources.txt
javac -cp "lib\*" -d bin @sources.txt
del sources.txt
echo Build complete. Class files are in backend\bin
