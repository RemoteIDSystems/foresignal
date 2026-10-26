/**
 * The v2.0 (Release 2) identity model (prompt R2-001): human accounts ({@code user}), roles, the fixed
 * permission catalog, the {@code user_role} and {@code role_permission} links, and single-use activation and
 * reset tokens ({@code user_token}).
 *
 * <p>Each user owns one {@code principal} of kind {@code person} (stored as {@code user.principal_id}), so
 * annotations, workspaces, sharing and audit keep referring to a principal exactly as they do for tokens.
 * The permission catalog is {@link ai.foresignal.security.user.Permissions}; the five built-in roles are
 * {@link ai.foresignal.security.user.BuiltInRole}. Both are seeded by {@code V14__identity_model.sql}, and a
 * guard test keeps the Java and the seeded rows identical. Authentication, authorization and the management
 * API are built on top of this package by the later R2 prompts.
 */
package ai.foresignal.security.user;
