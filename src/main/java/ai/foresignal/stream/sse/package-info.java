/**
 * The generic SSE machinery (prompt 015, Core-Data-Model §10.2).
 *
 * <ul>
 *   <li>{@link ai.foresignal.stream.sse.SseClient}: one stream, with a bounded FIFO lane for
 *       id-bearing events and a conflation lane for {@code live}/{@code touch}, a swappable
 *       {@link ai.foresignal.stream.sse.StreamFilter}, replay-then-live without gaps or duplicates,
 *       and heartbeats, all written by one virtual sender thread.
 *   <li>{@link ai.foresignal.stream.sse.SseHub}: the open clients, the {@link
 *       ai.foresignal.stream.sse.StreamLimits} (app setting {@code stream.limits}) and the fan-out.
 *   <li>{@link ai.foresignal.stream.sse.EventTopic}: the SPI a topic implements to join the
 *       multiplexed {@code /v1/events/stream}. {@link ai.foresignal.stream.sse.ChangesTopic} is the
 *       first; prompt 017 adds {@code alerts}, prompt 018 {@code notifications} and {@code system},
 *       each as a Spring bean and with no change to the hub.
 *   <li>{@link ai.foresignal.stream.sse.StreamPrincipalResolver}: who a stream belongs to; prompt
 *       023's stream tickets plug in here.
 * </ul>
 */
package ai.foresignal.stream.sse;
