#!/bin/bash
#
# Start script for docs.developer.ch.gov.uk

PORT=8080

exec java -jar -Dserver.port="${PORT}" "account-validator.api.ch.gov.uk.jar"