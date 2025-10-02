#!/bin/bash
set -e

# Create chat-db database
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE "chat-db";
    GRANT ALL PRIVILEGES ON DATABASE "chat-db" TO $POSTGRES_USER;
EOSQL

# Create another database if needed
# psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
#     CREATE DATABASE "your-new-db-name";
#     GRANT ALL PRIVILEGES ON DATABASE "your-new-db-name" TO $POSTGRES_USER;
# EOSQL