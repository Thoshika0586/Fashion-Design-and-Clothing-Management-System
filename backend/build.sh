#!/bin/bash
# Compiles the backend. Requires the MySQL Connector/J jar in lib/
set -e
mkdir -p bin
echo "Compiling Java sources..."
javac -cp "lib/*" -d bin $(find src -name "*.java")
echo "Build complete. Class files are in backend/bin"
