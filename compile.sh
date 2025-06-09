#!/bin/bash

# Build the project
mvn clean package

# Check if build was successful
if [ $? -ne 0 ]; then
    echo "Build failed"
    exit 1
fi
