package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.adapters.FakePollingAdapter;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultFetchContextTest {

    private final UpstreamClient upstream = mock(UpstreamClient.class);
    private final CredentialsAccess credentials = mock(CredentialsAccess.class);
    private final AdapterStateStore state = mock(AdapterStateStore.class);
    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));

    private DefaultFetchContext context(SourceDescriptor descriptor, AtomicInteger paramCalls, AtomicInteger lastCalls) {
        return new DefaultFetchContext(
                descriptor, upstream, null,
                () -> {
                    paramCalls.incrementAndGet();
                    return ValidatedParams.empty();
                },
                credentials, clock,
                () -> {
                    lastCalls.incrementAndGet();
                    return Optional.of(Instant.parse("2025-12-31T00:00:00Z"));
                },
                state);
    }

    @Test
    @DisplayName("parameters, credentials and the last success are read lazily, once")
    void lazyAndMemoized() {
        when(credentials.forSource(anyString())).thenReturn(Map.of("apiKey", "k"));
        AtomicInteger params = new AtomicInteger();
        AtomicInteger last = new AtomicInteger();
        DefaultFetchContext ctx = context(FakePollingAdapter.defaultDescriptor("ctx-test").build(), params, last);

        verify(credentials, never()).forSource(anyString());
        assertThat(params).hasValue(0);

        ctx.params();
        ctx.params();
        ctx.credentials();
        ctx.credentials();
        ctx.lastSuccessAt();
        ctx.lastSuccessAt();

        assertThat(params).hasValue(1);
        assertThat(last).hasValue(1);
        verify(credentials, times(1)).forSource("ctx-test");
        assertThat(ctx.credentials()).containsEntry("apiKey", "k");
        assertThat(ctx.sourceId()).isEqualTo("ctx-test");
        assertThat(ctx.state()).isSameAs(state);
        assertThat(ctx.now()).isEqualTo(clock.instant());
        assertThat(ctx.log().getName()).isEqualTo("foresignal.source.ctx-test");
        assertThat(ctx.traces()).isEmpty();
        assertThat(ctx.recorder()).isNull();
    }

    @Test
    @DisplayName("the HTTP client is bound once, on first use")
    void httpBoundOnce() {
        SourceDescriptor descriptor = FakePollingAdapter.defaultDescriptor("ctx-test").build();
        SourceHttp http = mock(SourceHttp.class);
        when(upstream.forSource("ctx-test", descriptor, null)).thenReturn(http);
        DefaultFetchContext ctx = context(descriptor, new AtomicInteger(), new AtomicInteger());

        assertThat(ctx.http()).isSameAs(http);
        assertThat(ctx.http()).isSameAs(http);
        verify(upstream, times(1)).forSource("ctx-test", descriptor, null);
        assertThat(ctx.traces()).isEmpty();
    }

    @Test
    @DisplayName("a DERIVED source's http() throws")
    void derivedHasNoHttp() {
        SourceDescriptor derived = SourceDescriptor.builder("ctx-derived")
                .name("Derived")
                .kind("thing")
                .mode(Mode.DERIVED)
                .defaultBudget(Budget.of(1, 1))
                .build();
        DefaultFetchContext ctx = context(derived, new AtomicInteger(), new AtomicInteger());

        assertThatThrownBy(ctx::http).isInstanceOf(UnsupportedOperationException.class);
        verify(upstream, never()).forSource(anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("a stream context meters bytes without counting a request, and forwards callbacks")
    void streamContext() {
        UsageMeter usage = mock(UsageMeter.class);
        AtomicInteger connected = new AtomicInteger();
        AtomicInteger disconnected = new AtomicInteger();
        DefaultStreamContext ctx = new DefaultStreamContext(
                FakePollingAdapter.defaultDescriptor("ctx-stream").build(), upstream, null, ValidatedParams::empty,
                credentials, clock, Optional::empty, state, usage,
                new DefaultStreamContext.Callbacks() {
                    @Override
                    public void connected() {
                        connected.incrementAndGet();
                    }

                    @Override
                    public void disconnected(Throwable error) {
                        disconnected.incrementAndGet();
                    }
                });

        ctx.reportBytes(100);
        ctx.recordReceivedBytes(0);
        ctx.connected();
        ctx.disconnected(null);

        verify(usage).recordBytes("ctx-stream", 100);
        verify(usage, never()).record(anyString(), org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyBoolean());
        assertThat(connected).hasValue(1);
        assertThat(disconnected).hasValue(1);
        assertThat(ctx.lastSuccessAt()).isEmpty();

        DefaultStreamContext quiet = new DefaultStreamContext(
                FakePollingAdapter.defaultDescriptor("ctx-stream").build(), upstream, null, ValidatedParams::empty,
                credentials, clock, Optional::empty, state, usage, null);
        quiet.connected();
        quiet.disconnected(new RuntimeException("x"));
    }
}
