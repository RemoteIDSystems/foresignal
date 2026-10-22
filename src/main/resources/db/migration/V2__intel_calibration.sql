-- Prompt 151: signal calibration. Turns the uncalibrated EventScorer confidence into a calibrated
-- probability backed by resolved real-world outcomes, and keeps the raw score beside it.

-- The uncalibrated EventScorer score, kept beside the reported confidence so the admin app can show
-- both the raw score and the calibrated probability. Null for events scored before 151.
alter table intelligence_event add column raw_confidence double precision;

-- One resolved real-world outcome used to calibrate confidence: an intel event the operator or agent
-- resolved (outcome 1) or dismissed (0), or a precursor-active finding whose forecast question resolved
-- yes (1) / no (0). Unique per subject so re-resolving updates in place.
create table intel_outcome (
    id varchar(26) not null,
    scope varchar(128) not null,
    subject_kind varchar(32) not null,
    subject_id varchar(64) not null,
    raw_score double precision not null,
    outcome double precision not null,
    resolved_at timestamp(6) with time zone not null,
    primary key (id),
    constraint intel_outcome_subject_uq unique (subject_kind, subject_id)
);
create index intel_outcome_scope_idx on intel_outcome (scope);

-- The fitted calibration of one scope: the CORP (pool-adjacent-violators) recalibration curve, the
-- 10-bin reliability table and the Brier score over its resolved outcomes. calibrated is true once at
-- least Calibration.CORP_MIN outcomes have resolved; until then the raw score is reported unchanged.
create table intel_calibration (
    scope varchar(128) not null,
    sample_n integer not null,
    calibrated boolean not null,
    brier double precision,
    curve jsonb,
    bins jsonb,
    fitted_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone not null,
    primary key (scope)
);
