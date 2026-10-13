/**
 * Interactive chat commands (prompt 061, Architecture §17.5): the operator talks back to Foresignal from the
 * Telegram and Matrix channels already used for alerts.
 *
 * <ul>
 *   <li>{@link ai.foresignal.chatops.ChatConnector} is the connector SPI, implemented by
 *       {@link ai.foresignal.chatops.TelegramConnector} (Bot API {@code getUpdates}) and
 *       {@link ai.foresignal.chatops.MatrixConnector} (client-server {@code /sync}).
 *   <li>{@link ai.foresignal.chatops.ChatopsManager} runs one supervised virtual thread per interactive
 *       channel, with its cursor in {@code chatops_cursor}.
 *   <li>{@link ai.foresignal.chatops.CommandRouter} maps each allowlisted chat to a
 *       {@link ai.foresignal.chatops.ChatPrincipal} of scope {@code read} or {@code agent} (never
 *       {@code admin}), checks every command against the same {@code AccessPolicy} rule as the REST
 *       operation it mirrors, rate-limits, audits, and runs it through
 *       {@link ai.foresignal.chatops.ChatCommandService}.
 * </ul>
 */
package ai.foresignal.chatops;
