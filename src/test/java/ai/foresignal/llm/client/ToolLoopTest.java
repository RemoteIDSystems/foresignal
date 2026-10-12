package ai.foresignal.llm.client;

import static ai.foresignal.testsupport.llm.FakeLlmClient.call;
import static ai.foresignal.testsupport.llm.FakeLlmClient.maxTokens;
import static ai.foresignal.testsupport.llm.FakeLlmClient.refusal;
import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static ai.foresignal.testsupport.llm.FakeLlmClient.toolUse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.llm.FakeLlmClient;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link ToolLoop} with {@link FakeLlmClient}: parallel tools, one results message, caps, errors, refusals. */
class ToolLoopTest {

    static final ToolLoop.Limits LIMITS = new ToolLoop.Limits(12, Duration.ofSeconds(120), 1_000_000);
    static final ChatRequest REQUEST = ChatRequest.simple(Purpose.ASK, "system", "question", 1000);

    @Test
    @DisplayName("tools of one turn run in parallel on virtual threads and all results go back in one message")
    void parallelAndOneMessage() {
        CountDownLatch bothStarted = new CountDownLatch(2);
        Set<Boolean> virtual = ConcurrentHashMap.newKeySet();
        FakeLlmClient client = new FakeLlmClient("fake")
                .then(toolUse(call("a", "slow", "{\"n\":1}"), call("b", "slow", "{\"n\":2}")))
                .then(text("done"));
        ToolLoop loop = new ToolLoop(client, c -> {
            virtual.add(Thread.currentThread().isVirtual());
            bothStarted.countDown();
            // Sequential execution would never see the second call start.
            if (!bothStarted.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("not parallel");
            }
            return "result " + c.input().get("n").asInt();
        }, LIMITS, Clock.systemUTC());

        ToolLoop.Result result = loop.run(REQUEST);

        assertThat(result.cause()).isEqualTo(ToolLoop.StopCause.END);
        assertThat(result.iterations()).isEqualTo(2);
        assertThat(virtual).containsExactly(true);
        List<ChatMessage> second = client.requests().get(1).messages();
        assertThat(second).hasSize(3);
        assertThat(second.get(1)).isInstanceOf(ChatMessage.Assistant.class);
        assertThat(((ChatMessage.Assistant) second.get(1)).rawTurn()).isEqualTo("raw-2");
        assertThat(second.get(2)).isInstanceOfSatisfying(ChatMessage.ToolResults.class, results -> {
            assertThat(results.results()).extracting(ToolResult::toolCallId).containsExactly("a", "b");
            assertThat(results.results()).extracting(ToolResult::content).containsExactly("result 1", "result 2");
            assertThat(results.results()).noneMatch(ToolResult::isError);
        });
        assertThat(result.conversation()).hasSize(4);
        assertThat(result.lastResponse().content()).isEqualTo("done");
        assertThat(result.usage()).isEqualTo(new TokenUsage(20, 10, 0));
    }

    @Test
    @DisplayName("a failing tool is returned as an error result, not dropped")
    void errorResults() {
        FakeLlmClient client = new FakeLlmClient("fake")
                .then(toolUse(call("a", "ok", "{}"), call("b", "boom", "{}")))
                .then(text("done"));
        ToolLoop loop = new ToolLoop(client, c -> {
            if (c.name().equals("boom")) {
                throw new IllegalArgumentException("bad input");
            }
            return "fine";
        }, LIMITS, Clock.systemUTC());

        loop.run(REQUEST);

        ChatMessage.ToolResults results = (ChatMessage.ToolResults) client.requests().get(1).messages().get(2);
        assertThat(results.results()).hasSize(2);
        assertThat(results.results().get(0).isError()).isFalse();
        assertThat(results.results().get(1).isError()).isTrue();
        assertThat(results.results().get(1).content()).contains("boom").contains("bad input");
        assertThat(results.results().get(1).toolName()).isEqualTo("boom");
    }

    @Test
    @DisplayName("the iteration cap stops the loop after the last turn's tools are answered")
    void iterationCap() {
        AtomicInteger runs = new AtomicInteger();
        FakeLlmClient client = new FakeLlmClient("fake").otherwise(r -> toolUse(call("x", "t", "{}")));
        ToolLoop loop = new ToolLoop(client, c -> "r" + runs.incrementAndGet(),
                new ToolLoop.Limits(3, Duration.ofSeconds(60), 1_000_000), Clock.systemUTC());

        ToolLoop.Result result = loop.run(REQUEST);

        assertThat(result.cause()).isEqualTo(ToolLoop.StopCause.MAX_ITERATIONS);
        assertThat(result.iterations()).isEqualTo(3);
        assertThat(runs).hasValue(3);
        assertThat(result.conversation().getLast()).isInstanceOf(ChatMessage.ToolResults.class);
    }

    @Test
    @DisplayName("the token cap stops the loop before another call")
    void tokenCap() {
        FakeLlmClient client = new FakeLlmClient("fake")
                .otherwise(r -> toolUse(new TokenUsage(400, 200, 0), call("x", "t", "{}")));
        ToolLoop loop = new ToolLoop(client, c -> "r", new ToolLoop.Limits(10, Duration.ofSeconds(60), 1000),
                Clock.systemUTC());

        ToolLoop.Result result = loop.run(REQUEST);

        assertThat(result.cause()).isEqualTo(ToolLoop.StopCause.TOKEN_CAP);
        assertThat(result.iterations()).isEqualTo(2);
        assertThat(result.usage().total()).isEqualTo(1200);
    }

    @Test
    @DisplayName("the deadline stops the loop before another call")
    void deadline() {
        MutableClock clock = new MutableClock(Instant.parse("2026-05-01T00:00:00Z"));
        FakeLlmClient client = new FakeLlmClient("fake").otherwise(r -> {
            clock.advance(Duration.ofSeconds(40));
            return toolUse(call("x", "t", "{}"));
        });
        ToolLoop loop = new ToolLoop(client, c -> "r", new ToolLoop.Limits(10, Duration.ofSeconds(100), 1_000_000),
                clock);

        ToolLoop.Result result = loop.run(REQUEST);

        assertThat(result.cause()).isEqualTo(ToolLoop.StopCause.DEADLINE);
        assertThat(result.iterations()).isEqualTo(3);
    }

    @Test
    @DisplayName("a tool still running at the deadline becomes an error result")
    void toolTimeout() {
        FakeLlmClient client = new FakeLlmClient("fake").then(toolUse(call("x", "sleepy", "{}"))).then(text("ok"));
        ToolLoop loop = new ToolLoop(client, c -> {
            Thread.sleep(10_000);
            return "late";
        }, new ToolLoop.Limits(5, Duration.ofMillis(200), 1_000_000), Clock.systemUTC());

        ToolLoop.Result result = loop.run(REQUEST);

        ChatMessage.ToolResults results = (ChatMessage.ToolResults) result.conversation().get(2);
        assertThat(results.results().getFirst().isError()).isTrue();
        assertThat(results.results().getFirst().content()).contains("deadline");
        assertThat(result.cause()).isEqualTo(ToolLoop.StopCause.DEADLINE);
    }

    @Test
    @DisplayName("a refusal stops the loop and runs no tool, even when tool calls are present")
    void refusalStops() {
        AtomicInteger runs = new AtomicInteger();
        FakeLlmClient client = new FakeLlmClient("fake").then(refusal("cyber", call("x", "t", "{}")));
        ToolLoop loop = new ToolLoop(client, c -> "r" + runs.incrementAndGet(), LIMITS, Clock.systemUTC());

        ToolLoop.Result result = loop.run(REQUEST);

        assertThat(result.cause()).isEqualTo(ToolLoop.StopCause.REFUSAL);
        assertThat(result.lastResponse().refusalCategory()).isEqualTo("cyber");
        assertThat(runs).hasValue(0);
        assertThat(client.requests()).hasSize(1);
    }

    @Test
    @DisplayName("max_tokens stops the loop before any tool runs")
    void maxTokensStops() {
        AtomicInteger runs = new AtomicInteger();
        FakeLlmClient client = new FakeLlmClient("fake").then(maxTokens(call("x", "t", "{}")));
        ToolLoop loop = new ToolLoop(client, c -> "r" + runs.incrementAndGet(), LIMITS, Clock.systemUTC());

        assertThat(loop.run(REQUEST).cause()).isEqualTo(ToolLoop.StopCause.MAX_TOKENS);
        assertThat(runs).hasValue(0);
    }

    @Test
    @DisplayName("a provider error stop reason ends the loop")
    void errorStops() {
        FakeLlmClient client = new FakeLlmClient("fake").then(new ChatResponse("", List.of(), StopReason.ERROR, null,
                null, "m", null));
        ToolLoop loop = new ToolLoop(client, c -> "r", LIMITS, Clock.systemUTC());

        assertThat(loop.run(REQUEST).cause()).isEqualTo(ToolLoop.StopCause.ERROR);
    }

    @Test
    @DisplayName("the conversation is append-only: earlier turns are passed back unchanged")
    void appendOnly() {
        FakeLlmClient client = new FakeLlmClient("fake")
                .then(toolUse(call("a", "t", "{}")))
                .then(toolUse(call("b", "t", "{}")))
                .then(text("end"));
        new ToolLoop(client, c -> "r", LIMITS, Clock.systemUTC()).run(REQUEST);

        List<ChatMessage> second = client.requests().get(1).messages();
        List<ChatMessage> third = client.requests().get(2).messages();
        assertThat(third.subList(0, second.size())).isEqualTo(second);
        assertThat(third).hasSize(second.size() + 2);
    }

    @Test
    @DisplayName("limits are validated")
    void limits() {
        assertThatThrownBy(() -> new ToolLoop.Limits(0, Duration.ofSeconds(1), 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ToolLoop.Limits(1, Duration.ofSeconds(1), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
