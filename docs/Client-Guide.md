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
              let alertId = e["a"] as? Int,
              let base = SharedConfig.baseURL,            // stored at pairing
              let token = SharedKeychain.readToken() else {
            contentHandler(request.content)               // keep the fallback text
            return
        }
        // Until prompt 033 adds GET /v1/notifications/{id}, alerts are read from /v1/alerts/{id}.
        var fetch = URLRequest(url: base.appendingPathComponent("v1/alerts/\(alertId)"))
        fetch.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        fetch.timeoutInterval = 5
        URLSession.shared.dataTask(with: fetch) { data, response, _ in
            if let data, (response as? HTTPURLResponse)?.statusCode == 200,
               let alert = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
                if let rule = alert["ruleName"] as? String { content.title = rule }
                if let message = alert["message"] as? String { content.body = message }
            }
            contentHandler(content)
        }.resume()
    }

    override func serviceExtensionTimeWillExpire() {
        if let handler, let content { handler(content) }   // the fallback text
    }
}
```

`SharedConfig` and `SharedKeychain` stand for the app's own helpers. Foresignal must be reachable
from the phone for the rewrite: on the LAN, over a VPN, or through Tailscale. When it is not, the
user sees the minimal text, and opening the notification follows `e.l`.

Categories are `FORESIGNAL_ALERT`, `FORESIGNAL_BRIEFING` and `FORESIGNAL_SYSTEM`; register them with
`UNUserNotificationCenter.setNotificationCategories`. Their actions (acknowledge, snooze) arrive
with prompt 033. Live Activities stay on the client roadmap.

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
