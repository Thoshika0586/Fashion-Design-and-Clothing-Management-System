#!/bin/bash
# Runs the API server. Requires build.sh to have been run first.
set -e
java -cp "bin:lib/*" com.fashiondesign.server.ApiServer
