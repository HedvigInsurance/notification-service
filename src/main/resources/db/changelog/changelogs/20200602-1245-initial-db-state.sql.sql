--liquibase formatted.sql

--changeset johantj:20200602-1245-initial-db-state.sql

CREATE TABLE IF NOT EXISTS customerio_state
(
    member_id                                varchar(255) not null
        constraint customerio_state_pkey primary key,
    sent_tmp_sign_event                      boolean      not null,
    underwriter_first_sign_attributes_update timestamp,
    activation_date_trigger_at               date,
    contract_created_trigger_at              timestamp,
    start_date_updated_trigger_at            timestamp
);

CREATE TABLE IF NOT EXISTS firebase_token
(
    member_id varchar(255) not null
        constraint firebase_token_pkey primary key,
    token     varchar(255) not null
);

CREATE TABLE IF NOT EXISTS mail_confirmation
(
    id              serial       not null
        constraint mail_confirmation_pkey primary key,
    confirmation_id varchar(255) not null,
    member_id       varchar(255) not null
);
