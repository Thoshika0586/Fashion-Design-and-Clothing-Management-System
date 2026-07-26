@echo off
REM Runs the API server. Requires build.bat to have been run first.
java -cp "bin;lib\*" com.fashiondesign.server.ApiServer
