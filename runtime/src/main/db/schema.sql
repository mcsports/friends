/**
  This file represents the sql schema of the friend backend.
  Execute jooqCodegen to create java classes for these files.
*/

CREATE TABLE IF NOT EXISTS friend_settings
(
    unique_id varchar NOT NULL PRIMARY KEY,
    see_joins boolean NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS friend_requests
(
    sender_id   varchar   NOT NULL,
    receiver_id varchar   NOT NULL,
    sent_at     timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT request_key PRIMARY KEY (sender_id, receiver_id)
);

CREATE TABLE IF NOT EXISTS friend_connections
(
    unique_id varchar NOT NULL,
    friend_id varchar NOT NULL,
    befriended_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT connection_key PRIMARY KEY (unique_id, friend_id)
);

