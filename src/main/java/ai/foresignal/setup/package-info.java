/**
 * First-run setup and the v2.0 cutover (prompt R2-027): whether an instance still needs setup
 * ({@link ai.foresignal.setup.FirstRunService}); the bootstrap that creates the first admin from the break-glass admin
 * token and seeds SMTP, sources, thresholds and retention ({@link ai.foresignal.setup.SetupService}); and the one-time
 * move of what the v1 operator owned to that admin ({@link ai.foresignal.setup.CutoverMigrator}), everything owner-less
 * staying shared.
 */
package ai.foresignal.setup;
