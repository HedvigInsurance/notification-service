--liquibase formatted.sql

--changeset fredrikareschoug:20200811-1150-init-event-hash-table.sql

CREATE TABLE event_hash
(
    member_id varchar(255) not null,
    hash varchar(255) not null,
    created_at timestamp not null,
    primary key (member_id, hash)
)

--rollback DROP TABLE event_hash
