/**
 * Collaboration (prompt 130): principals, groups, notification preferences and the per-principal inbox; ownership,
 * access lists and TLP labels of work products ({@link ai.foresignal.collab.AccessResolver}); tags, comment threads
 * with mentions, attachments, graded evidence items and saved searches.
 *
 * <p>Data stays instance-wide: records and analytic objects remain readable per scope. Access lists apply only to
 * work products (investigations, workspaces, saved searches, notes, attachments, evidence items, shared tags, and the
 * chronicles and reports of later prompts), which are invisible, not forbidden, to principals without access (COL-1).
 * A work product's TLP label bounds every share ({@link ai.foresignal.security.EgressPolicy#tlp}, COL-2). Uploaded
 * files are data, never code (COL-3).
 */
package ai.foresignal.collab;
