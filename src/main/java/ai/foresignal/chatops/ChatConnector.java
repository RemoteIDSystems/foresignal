package ai.foresignal.chatops;

/**
 * One chat platform's side of an interactive channel (prompt 061 connector SPI). A connector instance
 * serves one channel; {@link ChatopsManager} runs it on a supervised virtual thread: {@link #start} once,
 * then {@link #poll} in a loop, with backoff after a failure.
 *
 * <p>Connectors use only the platform's bot interface (the Telegram Bot API, the Matrix client-server API
 * with a bot account), never a user session, and send every request through {@code ChannelSender}.
 */
public interface ChatConnector {

    /**
     * Prepares the connector: checks the platform side (Telegram webhook, Matrix room encryption and
     * membership), registers commands, and on first start records the cursor so the backlog is skipped.
     *
     * @param config the channel
     * @param sink where messages and button presses go
     * @throws ChatAuthException when the credentials are refused
     * @throws ConnectorBlockedException when the operator must change something first
     * @throws java.io.IOException when the platform cannot be reached
     */
    void start(ChannelConfig config, CommandSink sink) throws java.io.IOException;

    /**
     * One long poll: waits for news, hands each message or press to the sink, then stores the cursor.
     *
     * @return how many updates were received
     * @throws ChatAuthException when the credentials are refused
     * @throws ConnectorBlockedException when the operator must change something first
     * @throws java.io.IOException when the platform cannot be reached
     */
    int poll() throws java.io.IOException;

    /** Stops the connector; a poll in progress is abandoned. */
    void stop();

    /**
     * Sends a reply through the 018 per-destination rate limiter. Failures are logged, never thrown.
     *
     * @param chat the chat (and message) to reply to
     * @param reply the reply
     */
    void reply(ChatRef chat, ChatReply reply);

    /**
     * Answers a button press (Telegram {@code answerCallbackQuery}); a no-op where there are no buttons.
     *
     * @param callbackRef the press
     * @param text the short result shown to the presser
     */
    void answerCallback(String callbackRef, String text);

    /** True when replies can carry buttons (Telegram); otherwise confirmations use text codes. */
    boolean supportsButtons();

    /** The connector's live state. */
    ConnectorState state();

    /** A loggable description of {@code error} with any credential removed. */
    default String describe(Throwable error) {
        return String.valueOf(error);
    }
}
