-- NP-008: the self-service editions permission (Permissions.SELF_EDITIONS) and its grants. It lets a signed-in user
-- own and manage their own newspaper edition through /v1/me/editions without agent scope. Added to the fixed catalog
-- after V14 because it is the first permission the identity model gained since R2-001; PermissionCatalogMigrationIT
-- checks the seeded catalog against Permissions across every migration, not V14 alone.
--
-- Granted to the built-in roles that already own personal configuration: Admin (every permission, BuiltInRole.ADMIN)
-- and Editor (all Self-service and Data permissions, BuiltInRole.EDITOR). Additive, with "on conflict do nothing" so
-- the seed restores a partially seeded database without duplicates, like V14.

insert into permission (key, "group", description) values
    ('self.editions', 'self', 'Own newspaper editions')
on conflict do nothing;

insert into role_permission (role_id, permission_key) values
    ('00000000-0000-4000-8000-000000000001', 'self.editions'),
    ('00000000-0000-4000-8000-000000000003', 'self.editions')
on conflict do nothing;
