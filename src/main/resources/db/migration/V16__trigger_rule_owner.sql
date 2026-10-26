-- Per-user alerts (prompt R2-008, docs/RBAC-Design.md §D). A trigger rule gains an owner: the principal of the user
-- who created it (users and principals are one-to-one, R2-001; notification_channel.owner_principal_id uses the same
-- convention). A null owner is a shared rule, managed by an admin: every rule that exists before this migration stays
-- shared, so nothing stops firing or changes where it delivers. An alert copies its rule's owner when it fires, so
-- who may see it does not change when the rule is reassigned or deleted. Additive only.

alter table trigger_rule add column owner_principal_id varchar(64);
create index trigger_rule_owner_idx on trigger_rule (owner_principal_id);

alter table alert add column owner_principal_id varchar(64);
create index alert_owner_idx on alert (owner_principal_id) where owner_principal_id is not null;
