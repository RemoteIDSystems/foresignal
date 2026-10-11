/**
 * The subscription half of Foresignal: which sources exist, which are enabled, and how they are
 * configured.
 *
 * <p>{@link ai.foresignal.source.SourceRegistry} knows what sources exist; {@link
 * ai.foresignal.source.SourceService} owns their settings and credentials. A source is subscribed when
 * its {@code source_settings} row says {@code enabled = true}, and that row is the only thing that
 * causes an upstream request — nothing a client reads or streams reaches here.
 *
 * <p>The adapter contract itself is in {@link ai.foresignal.source.spi}.
 *
 * <p><strong>Credentials.</strong> No method reachable from a controller returns a credential value.
 * {@link ai.foresignal.source.SourceView} carries only {@code credentialsSet}, and decryption lives on
 * {@link ai.foresignal.source.CredentialsAccess}, whose intended consumer is the {@code collect} package.
 */
package ai.foresignal.source;
