-- Prompt 155: on-demand analysis-request round-trip. A pending request Foresignal sent to an external
-- forecaster, resolved by its correlation id when the provider's callback returns the forecast.
create table analysis_correlation (
    id            varchar(64)  primary key,
    source_id     varchar(64)  not null,
    stream_id     varchar(48)  not null,
    question_id   varchar(26)  not null,
    orientation   varchar(8)   not null default 'same',
    forecaster_id varchar(128) not null,
    horizon       varchar(32),
    status        varchar(16)  not null default 'requested',
    created_by    varchar(200) not null,
    created_at    timestamptz  not null,
    fulfilled_at  timestamptz
);

create index analysis_correlation_question_idx on analysis_correlation (question_id);
