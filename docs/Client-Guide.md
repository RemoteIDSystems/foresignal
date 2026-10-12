# Foresignal — Client Guide

How a client app (the Flutter mobile and desktop apps, or your own) works with Foresignal. The API
itself is `docs/OpenAPI.json`; the object contract is `docs/Client-Contract.md`; the operator side is
`docs/Operator-Guide.md`.

## Native push for the Flutter app

Foresignal sends alerts to the app as **visible** notifications through APNs (iOS, iPadOS, macOS) and
FCM (Android), using the operator's own Apple key and Firebase project (prompt 032). The push carries
only ids, the severity and a minimal text. The app fetches the details from Foresignal itself, over
the user's own network path, so Apple and Google never see alert content.

### What a push contains

APNs body (at most 4096 bytes):

```json
{"aps": {"alert": {"title": "Foresignal", "body": "Critical alert"},
         "sound": "default", "mutable-content": 1, "interruption-level": "time-sensitive",
         "thread-id": "foresignal-<ruleId>", "category": "FORESIGNAL_ALERT"},
 "e": {"v": 1, "id": 1234, "a": 77, "s": "critical", "ev": "alert", "l": "foresignal://alerts/77"}}
```

- `e.id` is the notification id, `e.a` the alert id (null for briefings and system notices), `e.s` the
  severity, `e.ev` the event (`alert`, `briefing`, `system`) and `e.l` the deep link.
- The text depends on the device's `contentLevel`: `minimal` (default) shows "Foresignal" and
  "&lt;Severity&gt; alert", "Briefing ready" or "System notice"; `short` shows "Foresignal · &lt;severity&gt;"
  and the alert's short title (at most 40 characters).
- `interruption-level` is `passive` for info, `active` for warning, and `time-sensitive` for critical
  when the operator enabled it for an app with that capability (else `active`). During the
  operator's quiet hours (downgrade mode) pushes are `passive` without a sound.
- Foresignal never sends background ("content-available") pushes: iOS throttles them and drops them
  for an app the user force-quit.

FCM data message (all values are strings):

```json
{"message": {"token": "…",
  "data": {"v": "1", "id": "1234", "a": "77", "s": "critical", "ev": "alert",
           "l": "foresignal://alerts/77", "t": "Critical alert", "c": "FORESIGNAL_ALERT"},
  "android": {"priority": "HIGH", "ttl": "86400s", "collapse_key": "a-…"}}}
```

`t` is the minimal (or short) text. Repeats about the same entity share the APNs collapse id and the
FCM collapse key, so a newer push replaces an undelivered older one.

### Registering a device (iOS)

1. Pair the app (`foresignal://pair?…`, `POST /v1/pairing/claim`) to get a read token, store it in the
   Keychain in an access group the Notification Service Extension shares, and report the device
   with `PUT /v1/tokens/current/device` (`deviceName`, `appVersion`).
2. Request notification permission, register for remote notifications, and send the APNs token
   (hex) to Foresignal with the build's environment: `sandbox` for debug builds, `production` for
   TestFlight and App Store builds.

```http
POST /v1/push/devices
Authorization: Bearer <read token>

{"transport": "apns", "platform": "ios", "appId": "com.example.foresignal",
 "environment": "production", "token": "<64+ hex characters>", "contentLevel": "minimal"}
```

Register again whenever iOS hands the app a new token; the same token updates the existing device.
A 409 `PUSH_PROVIDER_NOT_CONFIGURED` means the operator has no APNs key for this bundle id and
environment.

### The Notification Service Extension (iOS)

Because every push has `mutable-content: 1`, iOS runs the app's Notification Service Extension
before showing it. The extension reads `e.id`/`e.a`, fetches the details from Foresignal with the
token from the shared Keychain, and rewrites the title and body. It must finish within a few
seconds; Foresignal's guidance is a 5 s budget, after which the minimal text is kept.

```swift
import UserNotifications

final class NotificationService: UNNotificationServiceExtension {
    private var handler: ((UNNotificationContent) -> Void)?
    private var content: UNMutableNotificationContent?

    override func didReceive(_ request: UNNotificationRequest,
                             withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void) {
        handler = contentHandler
        content = request.content.mutableCopy() as? UNMutableNotificationContent
        guard let content,
              let e = request.content.userInfo["e"] as? [String: Any],
              let notificationId = e["id"] as? Int,
              let base = SharedConfig.baseURL,            // stored at pairing
              let token = SharedKeychain.readToken() else {
            contentHandler(request.content)               // keep the fallback text
            return
        }
        // GET /v1/notifications/{id} (033): the text, the live alert state, and action capabilities
        // minted for this device, so nothing actionable ever passes through Apple.
        var fetch = URLRequest(url: base.appendingPathComponent("v1/notifications/\(notificationId)"))
        fetch.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        fetch.timeoutInterval = 5
        URLSession.shared.dataTask(with: fetch) { data, response, _ in
            if let data, (response as? HTTPURLResponse)?.statusCode == 200,
               let item = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
                if let title = item["title"] as? String { content.title = title }
                if let body = item["body"] as? String { content.body = body }
                if let actions = item["actions"] as? [[String: Any]] {
                    SharedActionCache.store(actions, for: notificationId)   // read by the action handler
                }
            }
            contentHandler(content)
        }.resume()
    }

    override func serviceExtensionTimeWillExpire() {
        if let handler, let content { handler(content) }   // the fallback text
    }
}
```

`SharedConfig`, `SharedKeychain` and `SharedActionCache` stand for the app's own helpers. Foresignal must be reachable
from the phone for the rewrite: on the LAN, over a VPN, or through Tailscale. When it is not, the
user sees the minimal text, and opening the notification follows `e.l`.

Categories are `FORESIGNAL_ALERT`, `FORESIGNAL_BRIEFING`, `FORESIGNAL_SYSTEM` and, for a firing alert
that carries actions, `FORESIGNAL_ALERT_ACTIONABLE`; register them with
`UNUserNotificationCenter.setNotificationCategories`. Give `FORESIGNAL_ALERT_ACTIONABLE` the actions
`ack` ("Acknowledge"), `snooze` ("Snooze 1h") and `mute` ("Mute 4h"); the handler is described in
"Acting on notifications" below. Live Activities stay on the client roadmap.

### Android

1. Pair the app as above and keep the token in encrypted storage.
2. Get the FCM registration token from the Firebase SDK and register it:

```http
POST /v1/push/devices
Authorization: Bearer <read token>

{"transport": "fcm", "platform": "android", "appId": "com.example.foresignal",
 "token": "<FCM registration token>"}
```

3. Foresignal sends **data messages**, so the app's `FirebaseMessagingService.onMessageReceived`
   runs in the foreground and in the background and builds the notification itself, optionally
   after fetching the details (the same endpoints as iOS).
4. Create one notification channel per severity (for example `critical`, `warning`, `info`) so the
   user can tune them, and post to the channel named by `data.s`.
5. **HIGH means visible.** Warning and critical pushes use FCM priority `HIGH`; the app must show a
   notification for every HIGH message, because Android and FCM deprioritize apps whose
   high-priority messages show nothing. Info pushes and quiet-hours pushes use `NORMAL`.

Register again on `onNewToken`. A token Google reports as `UNREGISTERED` is removed by Foresignal.

### Deep links

`e.l` (APNs) and `data.l` (FCM) come from the server's `clientLinks` setting, by default the custom
scheme `foresignal://alerts/{id}` (briefing runs `foresignal://briefings/{briefingId}/runs/{id}`).
Register the `foresignal` scheme in the app. Verified Universal Links or App Links (`https://…`) are
optional and work only for an operator who builds and signs their own app with that domain.

### Managing the device

- `GET /v1/push/devices` lists the devices of the calling token (the token itself is never shown,
  only `tokenSuffix`).
- `PUT /v1/push/devices/{deviceId}` changes `name`, `filters` (`events`, `ruleIds`, `minSeverity`,
  `tags`), `contentLevel` and `lang`.
- `POST /v1/push/devices/{deviceId}/test` sends a test notice (five per hour).
- `DELETE /v1/push/devices/{deviceId}` stops pushes, for example at sign-out.
- Revoking the app's API token removes its devices.

## The notification inbox

Every notification Foresignal sends (alerts, briefings, system events) is also an inbox item for 30
days, with **per-device** read state, so a desktop window and a phone each keep their own unread
count. Inbox calls need a token, even while open reads are on.

**Device id.** A token with device metadata (`PUT /v1/tokens/current/device`, which the paired apps
already send) is its own device. Any other client (a browser console, a script) picks a UUID once,
keeps it, and sends it as `Foresignal-Device-Id: <uuid>` on every inbox call; without either the
answer is `400 DEVICE_ID_REQUIRED`.

**Syncing.**

1. `GET /v1/notifications?limit=100` returns the newest items with `read` for this device and the
   alert's live `alertState`; page older with `nextCursor` (or `before=<id>`), fetch only newer ones
   with `after=<id>`. Filters: `events=alert,system`, `minSeverity=warning`, `unread=true`.
2. `GET /v1/notifications/stream` (SSE; a browser passes a stream ticket as `?ticket=`) sends each new
   item as `event: notification` with `id: <notification id>`; reconnect with `Last-Event-ID` and the
   missed items are replayed with their read state. `event: badge` (no id) arrives when another window
   of the same device marks something read.
3. `POST /v1/notifications/read {"upToId": 1234}` marks everything up to an id read (it never moves
   back); `{"ids": [1240, 1242]}` marks single items above it. The answer is the new badge.
   **Reading never acknowledges an alert.**
4. `GET /v1/notifications/badge` gives `{unread, unreadCritical, latestId}`: unread system events
   plus unread alerts that are still firing. Native pushes carry the same number as the app badge.

`GET /v1/notifications/{id}` returns one item with its links and, for a firing alert, `actions`:
capability URLs minted for this device (single use, 12 hours).

## Presence heartbeats

A desktop client that is in front of the operator reports it, so phone channels with
`deferWhileActiveSeconds` can hold non-critical alerts back:

```http
POST /v1/clients/presence
Foresignal-Device-Id: 0b9f4a3e-7f1c-4c39-9c51-2b1d0f1f0a01
{"kind": "desktop", "state": "active", "ttlSeconds": 120}
```

Send it about every 60 seconds while the window is focused and the user is active, `"idle"` after a
few minutes without input, and `"background"` when minimized; `ttlSeconds` is at most 300. Stopping
is enough: expired presence counts as absent and phones get everything again. An open inbox or alert
stream counts as `background` only.

## Acting on notifications

Each action is a URL `<publicBaseUrl>/v1/actions/eac_…`. `POST` it (no token, no body needed) with
`Accept: application/json` to act; the answer is `{action, alertId, result}` with `result`
`acknowledged`, `snoozed`, `muted` or `already-acknowledged` / `already-resolved`. A used or expired
capability is `410 ACTION_EXPIRED`. `GET` on the same URL only shows a confirm page, so open it in a
browser when there is no way to POST.

**Service worker (Web Push).** The payload carries `actions` (at most two, `{action, title, url}`)
and `confirmUrl`:

```js
self.addEventListener("push", (event) => {
  const p = event.data.json();
  const actions = ("maxActions" in Notification && Notification.maxActions > 0)
      ? (p.actions || []).slice(0, Notification.maxActions).map((a) => ({ action: a.action, title: a.title }))
      : [];
  event.waitUntil(self.registration.showNotification(p.title || "Foresignal", {
    body: p.body, tag: p.tag, data: p, actions,
  }));
});

self.addEventListener("notificationclick", (event) => {
  const p = event.notification.data || {};
  const chosen = (p.actions || []).find((a) => a.action === event.action);
  event.notification.close();
  if (chosen) {
    // One tap acts: the capability is the credential; nothing else is sent.
    event.waitUntil(fetch(chosen.url, { method: "POST", headers: { Accept: "application/json" } }));
  } else {
    // A plain click, or a browser without action buttons (Safari): open the confirm page or the alert.
    event.waitUntil(clients.openWindow(p.confirmUrl || (p.links && (p.links.web || p.links.api)) || "/"));
  }
});
```

A browser without action buttons (no `Notification.maxActions`, as in Safari) gets none, and a click
opens the confirm page; browsers that support them show the buttons. Declarative Web Push payloads
have no service worker to POST, so they navigate to the confirm page.

**Native apps.** The push never carries a capability. For `FORESIGNAL_ALERT_ACTIONABLE` the
Notification Service Extension above caches the `actions` of `GET /v1/notifications/{id}`; the app's
`userNotificationCenter(_:didReceive:withCompletionHandler:)` (Android: the action's
`BroadcastReceiver`) looks up the cached action whose `action` matches the tapped identifier and
POSTs its `url`. When the cache is empty (the extension timed out), fetch `GET /v1/notifications/{id}`
first with the app's token.

