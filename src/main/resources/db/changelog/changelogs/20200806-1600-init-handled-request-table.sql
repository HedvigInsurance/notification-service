--liquibase formatted.sql

--changeset fredrikareschoug:20200806-1600-init-handled-request-table.sql

CREATE TABLE handled_request(
    request_id varchar(255) not null primary key
)

--rollback DROP TABLE handled_request
