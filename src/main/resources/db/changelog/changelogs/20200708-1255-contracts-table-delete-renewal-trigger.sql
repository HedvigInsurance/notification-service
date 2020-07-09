--liquibase formatted.sql

--changeset meletis:20200708-1255-contracts-table-delete-renewal-trigger.sql


ALTER TABLE contract_state
    DROP COLUMN contract_renewal_queued_trigger_at