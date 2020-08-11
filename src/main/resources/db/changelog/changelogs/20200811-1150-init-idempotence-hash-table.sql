--liquibase formatted.sql

--changeset fredrikareschoug:20200811-1150-init-idempotence-hash-table.sql

CREATE TABLE idempotence_hash
(
    member_id varchar(255) not null,
    hash varchar(255) not null,
    created_at timestamp not null,
    primary key (member_id, hash)
)

--rollback DROP TABLE idempotence_hash
