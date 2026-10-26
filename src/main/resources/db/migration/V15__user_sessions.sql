-- Login sessions (prompt R2-003, docs/RBAC-Design.md §C). A successful POST /v1/auth/login mints a session bearer
-- (fss_ + 32 random bytes); only its SHA-256 hex and first 8 characters are stored here, never the plaintext. A session
-- has an absolute expiry (12 h) and a sliding idle expiry (2 h, moved forward on use at most once a minute) and ends
-- early when revoked: logout, revocation by its user, suspension, disabling or a password change. Additive only.

-- UserSessionEntity
create table user_session (
    id uuid not null,
    user_id uuid not null,
    token_hash varchar(64) not null,
    token_prefix varchar(8) not null,
    created_at timestamp(6) with time zone not null,
    last_seen_at timestamp(6) with time zone,
    expires_at timestamp(6) with time zone not null,
    idle_expires_at timestamp(6) with time zone not null,
    revoked_at timestamp(6) with time zone,
    user_agent varchar(512),
    ip_address varchar(64),
    primary key (id),
    constraint fk_user_session_user foreign key (user_id) references "user" (id) on delete cascade
);
create unique index user_session_hash_idx on user_session (token_hash);
create index user_session_user_revoked_idx on user_session (user_id, revoked_at);
