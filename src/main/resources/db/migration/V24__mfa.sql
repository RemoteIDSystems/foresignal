-- Multi-factor authentication (prompt R2-024). Additive only.
--
-- "user" gains the TOTP secret, sealed with the instance key (CredentialCipher, AES-GCM) as a ciphertext and nonce
-- pair like every other stored secret; it is pending while mfa_enabled is false and active once verifyMfa confirms a
-- code. mfa_enrolled_at is when it was activated; mfa_last_step is the last 30-second TOTP step accepted, so a code is
-- never accepted twice.
alter table "user" add column mfa_secret_cipher bytea;
alter table "user" add column mfa_secret_nonce bytea;
alter table "user" add column mfa_enrolled_at timestamp(6) with time zone;
alter table "user" add column mfa_last_step bigint;

-- user_recovery_code: single-use recovery codes, stored only as the SHA-256 hex of the normalized code (TokenHasher).
-- A set is replaced whole when it is regenerated; a used code keeps used_at and is never accepted again.
create table user_recovery_code (
    id uuid not null,
    user_id uuid not null,
    code_hash varchar(64) not null,
    created_at timestamp(6) with time zone not null,
    used_at timestamp(6) with time zone,
    primary key (id),
    constraint fk_user_recovery_code_user foreign key (user_id) references "user" (id) on delete cascade
);
create index user_recovery_code_user_idx on user_recovery_code (user_id);
