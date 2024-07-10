#!/bin/bash

# Start script for account-validator-api

PORT=8080
exec java -jar -Dserver.port="${PORT}" "account-validator-api.jar"
