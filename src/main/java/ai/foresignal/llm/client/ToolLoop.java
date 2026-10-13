package ai.foresignal.llm.client;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * The tool-use loop (prompt 026; Ask in 028 drives it): chat, run every requested tool in parallel on
 * virtual threads, append <strong>all</strong> results in <strong>one</strong> message, repeat.
 *
 * <p>The loop stops when the model ends its turn, refuses, hits its output cap, or errors, and when
 * any of the caller's limits is reached: iterations, a wall-clock deadline or a token cap. The stop
 * reason is checked before any tool runs, so a refused or truncated turn never executes a tool. A tool
 * that throws (or does not finish before the deadline) is returned to the model as an error result
 * ({@code is_error}), never dropped. The conversation stays append-only throughout: every assistant
 * turn is appended exactly as the provider returned it.
 */
public final class ToolLoop {

    /** Runs one tool call. Its return value is the result text; an exception becomes an error result. */
    @FunctionalInterface
    public interface ToolExecutor {

        /**
         * Runs {@code call}.
         *
         * @return the result text, usually JSON
         * @throws Exception when the tool fails; the message is returned to the model
         */
        String execute(ToolCall call) throws Exception;
    }

    /**
     * What a {@link RichToolExecutor} returns (prompt 056): the result text and any images.
     *
     * @param text the result text, usually JSON
     * @param images images for providers that accept them in tool results
     */
    public record ToolOutput(String text, List<ToolResult.Image> images) {

        /** Copies the images. */
        public ToolOutput {
            text = text == null ? "" : text;
            images = images == null ? List.of() : List.copyOf(images);
        }
    }

    /**
     * A {@link ToolExecutor} whose results may carry images (prompt 056: {@code get_map_image}). The loop calls
     * {@link #executeRich}; {@link #execute} returns its text.
     */
    public interface RichToolExecutor extends ToolExecutor {

        /**
         * Runs {@code call}.
         *
         * @return the text and images
         * @throws Exception when the tool fails; the message is returned to the model
         */
        ToolOutput executeRich(ToolCall call) throws Exception;

        @Override
        default String execute(ToolCall call) throws Exception {
            return executeRich(call).text();
        }
    }

    /**
     * The loop's limits.
     *
     * @param maxIterations chat calls at most
     * @param deadline wall-clock time for the whole loop
     * @param tokenCap input plus output tokens over every call; the loop stops once reached
     */
    public record Limits(int maxIterations, Duration deadline, long tokenCap) {

        /** Validates the limits. */
        public Limits {
            if (maxIterations < 1) {
                throw new IllegalArgumentException("maxIterations must be at least 1");
            }
            Objects.requireNonNull(deadline, "deadline");
            if (tokenCap < 1) {
                throw new IllegalArgumentException("tokenCap must be positive");
            }
        }
    }

    /** Why the loop stopped. */
    public enum StopCause {
        /** The model finished its turn without asking for tools. */
        END,
        /** The model refused. */
        REFUSAL,
        /** The model hit its output cap. */
        MAX_TOKENS,
        /** The provider reported an error stop reason. */
        ERROR,
        /** {@link Limits#maxIterations()} chat calls were made. */
        MAX_ITERATIONS,
        /** {@link Limits#deadline()} passed. */
        DEADLINE,
        /** {@link Limits#tokenCap()} was reached. */
        TOKEN_CAP
    }

    /**
     * The outcome.
     *
     * @param lastResponse the final model response (null only when the deadline passed before the first call)
     * @param conversation the full conversation, the request's messages first, append-only
     * @param iterations chat calls made
     * @param cause why the loop stopped
     * @param usage tokens over every call
     */
    public record Result(ChatResponse lastResponse, List<ChatMessage> conversation, int iterations, StopCause cause,
            TokenUsage usage) {

        /** Copies the conversation. */
        public Result {
            conversation = List.copyOf(conversation);
        }
    }

    private final LlmClient client;
    private final ToolExecutor executor;
    private final Limits limits;
    private final Clock clock;

    /**
     * Creates a loop.
     *
     * @param client the (governed) client
     * @param executor runs tool calls
     * @param limits the limits
     * @param clock the clock the deadline is measured on
     */
    public ToolLoop(LlmClient client, ToolExecutor executor, Limits limits, Clock clock) {
        this.client = Objects.requireNonNull(client, "client");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.limits = Objects.requireNonNull(limits, "limits");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** Runs the loop from {@code request}. */
    public Result run(ChatRequest request) {
        Instant deadline = clock.instant().plus(limits.deadline());
        List<ChatMessage> conversation = new ArrayList<>(request.messages());
        TokenUsage usage = TokenUsage.ZERO;
        ChatResponse last = null;
        int iterations = 0;
        while (true) {
            if (!clock.instant().isBefore(deadline)) {
                return new Result(last, conversation, iterations, StopCause.DEADLINE, usage);
            }
            if (usage.total() >= limits.tokenCap()) {
                return new Result(last, conversation, iterations, StopCause.TOKEN_CAP, usage);
            }
            last = client.chat(request.withMessages(conversation));
            iterations++;
            usage = usage.plus(last.usage());
            conversation.add(last.assistantMessage());

            StopCause terminal = switch (last.stopReason()) {
                case REFUSAL -> StopCause.REFUSAL;
                case MAX_TOKENS -> StopCause.MAX_TOKENS;
                case ERROR -> StopCause.ERROR;
                case END -> StopCause.END;
                case TOOL_USE -> last.toolCalls().isEmpty() ? StopCause.END : null;
            };
            if (terminal != null) {
                return new Result(last, conversation, iterations, terminal, usage);
            }

            java.util.concurrent.atomic.AtomicBoolean timedOut = new java.util.concurrent.atomic.AtomicBoolean();
            conversation.add(new ChatMessage.ToolResults(runTools(last.toolCalls(), deadline, timedOut)));
            if (timedOut.get()) {
                // A tool was cut off at the deadline, so the deadline has passed. The timed wait is rounded to
                // milliseconds and can return just before it, so the check at the top of the loop alone could make
                // one more model call (checkpoint 041, ToolLoopTest.toolTimeout).
                return new Result(last, conversation, iterations, StopCause.DEADLINE, usage);
            }

            if (iterations >= limits.maxIterations()) {
                return new Result(last, conversation, iterations, StopCause.MAX_ITERATIONS, usage);
            }
        }
    }

    /**
     * Runs every call in parallel on virtual threads; results keep the calls' order. {@code timedOut} is set
     * when a call did not finish before {@code deadline}.
     */
    List<ToolResult> runTools(List<ToolCall> calls, Instant deadline, java.util.concurrent.atomic.AtomicBoolean timedOut) {
        List<ToolResult> results = new ArrayList<>(calls.size());
        // Not try-with-resources: close() would wait for a tool that ignores interruption past the
        // deadline. shutdownNow() interrupts stragglers and returns at once.
        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
        try {
            List<Future<ToolOutput>> futures = new ArrayList<>(calls.size());
            for (ToolCall call : calls) {
                futures.add(pool.submit(() -> executor instanceof RichToolExecutor rich ? rich.executeRich(call)
                        : new ToolOutput(executor.execute(call), List.of())));
            }
            for (int i = 0; i < calls.size(); i++) {
                ToolCall call = calls.get(i);
                Future<ToolOutput> future = futures.get(i);
                long waitMs = Math.max(0, Duration.between(clock.instant(), deadline).toMillis());
                try {
                    ToolOutput output = future.get(waitMs, TimeUnit.MILLISECONDS);
                    results.add(ToolResult.ok(call, output.text(), output.images()));
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    results.add(ToolResult.error(call, "Tool " + call.name() + " failed: " + describe(cause)));
                } catch (java.util.concurrent.TimeoutException | CancellationException e) {
                    future.cancel(true);
                    timedOut.set(true);
                    results.add(ToolResult.error(call, "Tool " + call.name() + " did not finish before the deadline"));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    future.cancel(true);
                    results.add(ToolResult.error(call, "Tool " + call.name() + " was interrupted"));
                }
            }
        } finally {
            pool.shutdownNow();
        }
        return results;
    }

    private static String describe(Throwable t) {
        return t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage();
    }
}
