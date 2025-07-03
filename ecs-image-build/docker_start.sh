#!/bin/bash

# Start script for account-validator-api

PORT=8080
exec java -jar -Dserver.port="${PORT}" -XX:MaxRAMPercentage=80 "account-validator-api.jar"
