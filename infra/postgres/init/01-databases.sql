-- Runs once when the PostgreSQL data directory is initialized.
-- Each microservice owns its own logical database.
CREATE DATABASE catalog_db;
CREATE DATABASE order_db;
CREATE DATABASE kitchen_db;
CREATE DATABASE payment_db;
CREATE DATABASE notification_db;
