-- The v2.0 identity model (prompt R2-001, docs/RBAC-Design.md): human accounts, roles, the fixed permission catalog,
-- the user/role and role/permission links, and single-use activation and reset tokens. Additive only; no API operation
-- changes here. The catalog and the five built-in roles are seeded with stable ids and "on conflict do nothing", so
-- re-running against a partially seeded database is safe. ai.foresignal.security.user.Permissions and BuiltInRole are
-- the Java side of the same seed; PermissionCatalogMigrationIT fails when the two disagree.
--
-- "user" and "group" are reserved words in PostgreSQL and are quoted wherever they appear. The baseline enables no
-- citext extension, so user.email is stored normalized (trimmed, lower-cased) by UserEntity under a unique constraint.
-- Each user owns one principal of kind person (user.principal_id), through which collaboration and audit keep
-- referring to a principal. No user row is created here: an instance with no users keeps working on the bootstrap
-- admin token.

-- UserEntity
create table "user" (
    id uuid not null,
    email varchar(320) not null,
    full_name varchar(200) not null,
    password_hash varchar(255),
    status varchar(16) default 'invited' not null,
    principal_id varchar(26) not null,
    mfa_enabled boolean default false not null,
    created_by uuid,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    last_login_at timestamp(6) with time zone,
    primary key (id),
    constraint uk_user_email unique (email),
    constraint fk_user_principal foreign key (principal_id) references principal (id)
);

-- RoleEntity
create table role (
    id uuid not null,
    name varchar(100) not null,
    description varchar(500),
    built_in boolean not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id),
    constraint uk_role_name unique (name)
);

-- PermissionEntity (the fixed catalog; read-only at runtime)
create table permission (
    key varchar(64) not null,
    "group" varchar(16) not null,
    description varchar(500) not null,
    primary key (key)
);

-- UserRoleEntity
create table user_role (
    user_id uuid not null,
    role_id uuid not null,
    primary key (user_id, role_id),
    constraint fk_user_role_user foreign key (user_id) references "user" (id) on delete cascade,
    constraint fk_user_role_role foreign key (role_id) references role (id) on delete cascade
);
create index user_role_role_idx on user_role (role_id);

-- RolePermissionEntity
create table role_permission (
    role_id uuid not null,
    permission_key varchar(64) not null,
    primary key (role_id, permission_key),
    constraint fk_role_permission_role foreign key (role_id) references role (id) on delete cascade,
    constraint fk_role_permission_permission foreign key (permission_key) references permission (key)
);

-- UserTokenEntity (only the SHA-256 hex and an 8-character prefix are stored, never the token)
create table user_token (
    id uuid not null,
    user_id uuid not null,
    kind varchar(16) not null,
    token_hash varchar(64) not null,
    token_prefix varchar(8) not null,
    created_at timestamp(6) with time zone not null,
    expires_at timestamp(6) with time zone not null,
    consumed_at timestamp(6) with time zone,
    created_by uuid,
    primary key (id),
    constraint fk_user_token_user foreign key (user_id) references "user" (id) on delete cascade
);
create index user_token_hash_idx on user_token (token_hash);
create index user_token_user_kind_idx on user_token (user_id, kind);

-- The permission catalog (Permissions.java)
insert into permission (key, "group", description) values
    ('users.manage', 'identity', 'Create, edit, suspend and delete user accounts'),
    ('roles.manage', 'identity', 'Create, edit and delete custom roles'),
    ('system.settings', 'platform', 'Change instance-wide server settings'),
    ('collection.manage', 'platform', 'Choose the sources the instance collects'),
    ('collector.settings', 'platform', 'Tune the collector''s pacing and budgets'),
    ('llm.shared.manage', 'platform', 'Manage the shared LLM providers and budgets'),
    ('channels.global', 'platform', 'Manage the instance-wide notification channels'),
    ('storage.manage', 'platform', 'Manage disk, retention, archival and backup'),
    ('network.manage', 'platform', 'Manage egress routing and first-party aggregators'),
    ('audit.read', 'platform', 'Read the audit log'),
    ('self.sources', 'self', 'Choose one''s own enabled sources over the shared collection'),
    ('self.alerts', 'self', 'Own alert rules'),
    ('self.channels', 'self', 'Own notification channels'),
    ('self.llm', 'self', 'Own personal LLM providers'),
    ('self.views', 'self', 'Own saved views'),
    ('self.preferences', 'self', 'Change one''s own preferences'),
    ('self.workspaces', 'self', 'Own workspaces'),
    ('data.read', 'data', 'Read collected records'),
    ('data.export', 'data', 'Export collected records'),
    ('intel.read', 'data', 'Read intelligence events and analyses'),
    ('forecast.read', 'data', 'Read forecasts and questions')
on conflict do nothing;

-- The built-in roles (BuiltInRole.java)
insert into role (id, name, description, built_in, created_at, updated_at) values
    ('00000000-0000-4000-8000-000000000001', 'Admin', 'Full control of the instance, its users and roles', true, now(), now()),
    ('00000000-0000-4000-8000-000000000002', 'Operator', 'Runs the platform: collection, collector, shared LLM, channels, storage, network and audit', true, now(), now()),
    ('00000000-0000-4000-8000-000000000003', 'Editor', 'Reads all data and owns personal sources, alerts, channels, LLM, views and workspaces', true, now(), now()),
    ('00000000-0000-4000-8000-000000000004', 'Viewer', 'Read-only access to collected data', true, now(), now()),
    ('00000000-0000-4000-8000-000000000005', 'Auditor', 'Reads the audit log and collected data', true, now(), now())
on conflict do nothing;

-- Admin: 21 permissions
insert into role_permission (role_id, permission_key) values
    ('00000000-0000-4000-8000-000000000001', 'users.manage'),
    ('00000000-0000-4000-8000-000000000001', 'roles.manage'),
    ('00000000-0000-4000-8000-000000000001', 'system.settings'),
    ('00000000-0000-4000-8000-000000000001', 'collection.manage'),
    ('00000000-0000-4000-8000-000000000001', 'collector.settings'),
    ('00000000-0000-4000-8000-000000000001', 'llm.shared.manage'),
    ('00000000-0000-4000-8000-000000000001', 'channels.global'),
    ('00000000-0000-4000-8000-000000000001', 'storage.manage'),
    ('00000000-0000-4000-8000-000000000001', 'network.manage'),
    ('00000000-0000-4000-8000-000000000001', 'audit.read'),
    ('00000000-0000-4000-8000-000000000001', 'self.sources'),
    ('00000000-0000-4000-8000-000000000001', 'self.alerts'),
    ('00000000-0000-4000-8000-000000000001', 'self.channels'),
    ('00000000-0000-4000-8000-000000000001', 'self.llm'),
    ('00000000-0000-4000-8000-000000000001', 'self.views'),
    ('00000000-0000-4000-8000-000000000001', 'self.preferences'),
    ('00000000-0000-4000-8000-000000000001', 'self.workspaces'),
    ('00000000-0000-4000-8000-000000000001', 'data.read'),
    ('00000000-0000-4000-8000-000000000001', 'data.export'),
    ('00000000-0000-4000-8000-000000000001', 'intel.read'),
    ('00000000-0000-4000-8000-000000000001', 'forecast.read')
on conflict do nothing;

-- Operator: 12 permissions
insert into role_permission (role_id, permission_key) values
    ('00000000-0000-4000-8000-000000000002', 'system.settings'),
    ('00000000-0000-4000-8000-000000000002', 'collection.manage'),
    ('00000000-0000-4000-8000-000000000002', 'collector.settings'),
    ('00000000-0000-4000-8000-000000000002', 'llm.shared.manage'),
    ('00000000-0000-4000-8000-000000000002', 'channels.global'),
    ('00000000-0000-4000-8000-000000000002', 'storage.manage'),
    ('00000000-0000-4000-8000-000000000002', 'network.manage'),
    ('00000000-0000-4000-8000-000000000002', 'audit.read'),
    ('00000000-0000-4000-8000-000000000002', 'data.read'),
    ('00000000-0000-4000-8000-000000000002', 'data.export'),
    ('00000000-0000-4000-8000-000000000002', 'intel.read'),
    ('00000000-0000-4000-8000-000000000002', 'forecast.read')
on conflict do nothing;

-- Editor: 11 permissions
insert into role_permission (role_id, permission_key) values
    ('00000000-0000-4000-8000-000000000003', 'self.sources'),
    ('00000000-0000-4000-8000-000000000003', 'self.alerts'),
    ('00000000-0000-4000-8000-000000000003', 'self.channels'),
    ('00000000-0000-4000-8000-000000000003', 'self.llm'),
    ('00000000-0000-4000-8000-000000000003', 'self.views'),
    ('00000000-0000-4000-8000-000000000003', 'self.preferences'),
    ('00000000-0000-4000-8000-000000000003', 'self.workspaces'),
    ('00000000-0000-4000-8000-000000000003', 'data.read'),
    ('00000000-0000-4000-8000-000000000003', 'data.export'),
    ('00000000-0000-4000-8000-000000000003', 'intel.read'),
    ('00000000-0000-4000-8000-000000000003', 'forecast.read')
on conflict do nothing;

-- Viewer: 3 permissions
insert into role_permission (role_id, permission_key) values
    ('00000000-0000-4000-8000-000000000004', 'data.read'),
    ('00000000-0000-4000-8000-000000000004', 'self.views'),
    ('00000000-0000-4000-8000-000000000004', 'self.preferences')
on conflict do nothing;

-- Auditor: 2 permissions
insert into role_permission (role_id, permission_key) values
    ('00000000-0000-4000-8000-000000000005', 'audit.read'),
    ('00000000-0000-4000-8000-000000000005', 'data.read')
on conflict do nothing;
