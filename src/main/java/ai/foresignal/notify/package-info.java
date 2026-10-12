/**
 * Notification channels (prompt 018): the message model, the {@link ai.foresignal.notify.ChannelType}
 * SPI with one class per destination type in {@code notify.types}, routing of alerts and system
 * events into persisted {@code notification} rows, and the durable, rate-limited delivery worker with
 * quiet hours, a global pause, backlog folding, grouping and edit-in-place.
 */
package ai.foresignal.notify;
