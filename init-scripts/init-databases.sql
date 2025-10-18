-- Create databases for each service
CREATE DATABASE "chat-db";
CREATE DATABASE "group-db";
CREATE DATABASE "post-db";
CREATE DATABASE "keycloak";

-- Grant privileges to the user
GRANT ALL PRIVILEGES ON DATABASE "chat-db" TO anas;
GRANT ALL PRIVILEGES ON DATABASE "group-db" TO anas;
GRANT ALL PRIVILEGES ON DATABASE "post-db" TO anas;
GRANT ALL PRIVILEGES ON DATABASE "keycloak" TO anas;