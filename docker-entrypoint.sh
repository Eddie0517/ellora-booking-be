#!/bin/sh
set -e

# If arguments are provided (e.g. docker run ... java -jar /app/app.jar or mvn test)
if [ "$#" -gt 0 ]; then
    # If invoked with java -jar pointing to non-existent file, fallback to /opt/ellora/app.jar
    if [ "$1" = "java" ] && [ "$2" = "-jar" ]; then
        if [ ! -f "$3" ] && [ -f "/opt/ellora/app.jar" ]; then
            shift 2
            shift
            exec java -jar /opt/ellora/app.jar "$@"
        fi
    fi
    exec "$@"
fi

# Fallback default command
if [ -f "/opt/ellora/app.jar" ]; then
    exec java -jar /opt/ellora/app.jar
fi

if [ -f "/app/app.jar" ]; then
    exec java -jar /app/app.jar
fi

exec "$@"
