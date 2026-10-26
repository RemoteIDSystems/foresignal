-- Egress routing & IP rotation (prompt R2-018). Additive only.
--
-- egress_route: the egress pool of GET /v1/egress. type is http or socks5 (a proxy at url, scheme://host:port),
-- interface (bind the host interface or source IP in bound_interface) or wireguard (a profile kept for the host; the
-- server cannot bring a tunnel up itself, so such a route is recorded and reported unavailable). secrets_cipher and
-- secrets_nonce hold the write-only proxy credentials or WireGuard profile as one AES-GCM sealed JSON object
-- (CredentialCipher); they are never returned. exit_ip is the public address the last test call observed.
create table egress_route (
    id uuid primary key,
    name varchar(200) not null,
    type varchar(16) not null,
    enabled boolean not null default true,
    url text,
    bound_interface varchar(200),
    secrets_cipher bytea,
    secrets_nonce bytea,
    exit_ip varchar(64),
    exit_ip_checked_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create unique index egress_route_name_idx on egress_route (lower(name));

-- egress_backoff: a throttle (429, or any Retry-After) one source received through one route. The source is held off
-- that route only, until backoff_until; the other routes stay usable for it. Persisted like source_health.backoff_until,
-- so a restart does not release the brake.
create table egress_backoff (
    source_id varchar(64) not null,
    route_id uuid not null references egress_route (id) on delete cascade,
    backoff_until timestamptz not null,
    consecutive integer not null default 1,
    primary key (source_id, route_id)
);

-- The per-source assignment (assignSourceEgress), on the source's settings row: a pinned route, and whether the source
-- stays out of rotation. Deleting a route clears the pins to it (the source falls back to the default route).
alter table source_settings add column egress_route_id uuid;
alter table source_settings add column egress_excluded boolean not null default false;
