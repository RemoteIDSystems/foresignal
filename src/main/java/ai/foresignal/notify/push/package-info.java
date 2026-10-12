/**
 * Built-in push (prompt 031): Web Push for browsers and UnifiedPush for native Android and Linux
 * apps, which speak the same RFC 8030 / RFC 8291 wire protocol.
 *
 * <ul>
 *   <li>Crypto with the JDK only: {@link ai.foresignal.notify.push.Hkdf} (RFC 5869),
 *       {@link ai.foresignal.notify.push.WebPushCrypto} (RFC 8291, {@code aes128gcm} of RFC 8188),
 *       {@link ai.foresignal.notify.push.Vapid} (RFC 8292) with Foresignal's own key
 *       ({@link ai.foresignal.notify.push.VapidKeyService}), so no push account is needed.
 *   <li>The transport SPI shared with prompt 032: {@link ai.foresignal.notify.push.PushTarget},
 *       {@link ai.foresignal.notify.push.PushTransport}, {@link ai.foresignal.notify.push.PushResult}
 *       and {@link ai.foresignal.notify.push.PushTargetSource}.
 *   <li>Delivery: the built-in "Push" channel ({@link ai.foresignal.notify.push.PushChannelType}) fans
 *       out into per-target {@code push_dispatch} rows ({@link ai.foresignal.notify.push.PushFanout}),
 *       sent and retried independently by {@link ai.foresignal.notify.push.PushDispatchWorker}.
 *   <li>Subscriptions with registration verification, an address policy re-checked at send time
 *       ({@link ai.foresignal.net.OutboundUrlPolicy} profile {@code PUSH_ENDPOINT}) and connections pinned to the checked
 *       address ({@link ai.foresignal.notify.push.PushHttp}).
 *   <li>Native push (prompt 032): APNs and FCM transports ({@code apns}, {@code fcm} subpackages)
 *       for {@link ai.foresignal.notify.push.NativeDeviceTarget}s registered at
 *       {@code /v1/push/devices} ({@code device}), with the operator's own keys ({@code provider}),
 *       carrying only ids, severity and the minimal text of
 *       {@link ai.foresignal.notify.push.NativeContent}.
 * </ul>
 */
package ai.foresignal.notify.push;
