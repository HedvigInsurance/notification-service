--liquibase formatted.sql

--changeset meletis:20200615-1455-contracts-table.sql


CREATE TABLE IF NOT EXISTS contract_state
(
    contract_id                        text not null
        constraint contract_id_pkey primary key,
    member_id text not null
        REFERENCES customerio_state(member_id),
    renewal_date                       date null,
    contract_renewal_queued_trigger_at date null
);

--rollback DROP TABLE contract_state