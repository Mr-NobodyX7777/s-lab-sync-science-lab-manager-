#!/bin/bash
cd "$(dirname "$0")"

if [ ! -f "lib/mysql-connector-j.jar" ]; then
    echo "============================================================"
    echo "  MySQL driver not found!"
    echo "  Please download mysql-connector-j-9.x.x.jar from:"
    echo "  https://dev.mysql.com/downloads/connector/j/"
    echo "  and place it inside the 'lib' folder as:"
    echo "  lib/mysql-connector-j.jar"
    echo "============================================================"
    read -p "Press Enter to exit..."
    exit 1
fi

java -jar SLabSync.jar
