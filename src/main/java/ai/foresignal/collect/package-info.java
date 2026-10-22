/**
 * Collection: the only path from Foresignal to an upstream.
 *
 * <p>{@link ai.foresignal.collect.UpstreamClient} is the single place an HTTP request to an upstream is
 * made. Everything it does — budget, backoff, User-Agent, byte cap, metering, redaction — applies
 * to every adapter without an adapter having to remember any of it, which is what makes the
 * good-citizen guarantee in Product Charter G2 a property of the code rather than a convention.
 *
 * <p>Nothing outside this package constructs an {@code HttpClient} for upstream use, and the SPI
 * gives adapters no way to do so: they receive a {@link ai.foresignal.source.spi.SourceHttp} already
 * bound to their source.
 *
 * <p>What decides <em>when</em> a request is made lives here too (prompt 010): the
 * {@link ai.foresignal.collect.CollectorScheduler} polls enabled polling sources, the
 * {@link ai.foresignal.collect.StreamingSupervisor} holds enabled streams open behind a
 * {@link ai.foresignal.collect.PolicyAwareRecordSink}, and {@link ai.foresignal.collect.FetchExecutor}
 * runs each fetch and leaves its {@code fetch_log} row. The operator's
 * {@link ai.foresignal.collect.ManualFetchService} and {@link ai.foresignal.collect.ProbeService} are
 * the only other paths to an upstream.
 *
 * <p>How much is pulled at once is bounded in three layers (prompt 150). The
 * {@link ai.foresignal.collect.PullGovernor} is the global bound: a fair, resizable pool of
 * {@code maxConcurrentPulls} permits ({@code CollectorSettings}, default 2), one held by every scheduled
 * or manual fetch and every polling probe for as long as it pulls; streams are not gated. Beneath it
 * the per-host {@link ai.foresignal.collect.HostPacer} spaces requests to one host, and the per-source
 * {@link ai.foresignal.collect.BudgetGovernor} and each source's own thread and interval cap what one
 * source spends.
 */
package ai.foresignal.collect;
