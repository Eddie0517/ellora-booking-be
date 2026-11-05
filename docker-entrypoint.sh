#!/bin/sh
set -e

# If arguments are provided, execute them directly
if [ "$#" -gt 0 ]; then
    exec "$@"
fi

# Fallback default command
if [ -f "/app/app.jar" ]; then
    exec java -jar /app/app.jar
fi

exec "$@"
