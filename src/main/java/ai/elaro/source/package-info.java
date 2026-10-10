/**
 * The subscription half of Elaro: which sources exist, which are enabled, and how they are
 * configured.
 *
 * <p>{@link ai.elaro.source.SourceRegistry} knows what sources exist; {@link
 * ai.elaro.source.SourceService} owns their settings and credentials. A source is subscribed when
 * its {@code source_settings} row says {@code enabled = true}, and that row is the only thing that
 * causes an upstream request — nothing a client reads or streams reaches here.
 *
 * <p>The adapter contract itself is in {@link ai.elaro.source.spi}.
 *
 * <p><strong>Credentials.</strong> No method reachable from a controller returns a credential value.
 * {@link ai.elaro.source.SourceView} carries only {@code credentialsSet}, and decryption lives on
 * {@link ai.elaro.source.CredentialsAccess}, whose intended consumer is the {@code collect} package.
 */
package ai.elaro.source;
