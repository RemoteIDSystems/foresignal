/**
 * Private-source visibility (prompt 039): which sources are private ({@link
 * ai.foresignal.security.visibility.SourceVisibility}), who may see them, and the one helper every
 * query builder uses to keep them out ({@link ai.foresignal.security.visibility.PrivateFilter}).
 * {@link ai.foresignal.security.EgressPolicy} builds on it for every egress path.
 */
package ai.foresignal.security.visibility;
