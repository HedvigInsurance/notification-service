--liquibase formatted.sql

--changeset johanjt:20200617-0941-alter-contracts-table.sql


ALTER TABLE contract_state
    ALTER COLUMN contract_renewal_queued_trigger_at SET DATA TYPE TIMESTAMP
;

--rollback ALTER TABLE contract_state ALTER COLUMN contract_renewal_queued_trigger_at SET DATA TYPE DATE
