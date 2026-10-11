/**
 * Collection: the only path from Elaro to an upstream.
 *
 * <p>{@link ai.elaro.collect.UpstreamClient} is the single place an HTTP request to an upstream is
 * made. Everything it does — budget, backoff, User-Agent, byte cap, metering, redaction — applies
 * to every adapter without an adapter having to remember any of it, which is what makes the
 * good-citizen guarantee in Product Charter G2 a property of the code rather than a convention.
 *
 * <p>Nothing outside this package constructs an {@code HttpClient} for upstream use, and the SPI
 * gives adapters no way to do so: they receive a {@link ai.elaro.source.spi.SourceHttp} already
 * bound to their source.
 */
package ai.elaro.collect;
