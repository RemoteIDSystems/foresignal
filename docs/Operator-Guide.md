# Foresignal Operator Guide

How to run and tune a Foresignal instance through its API. Later prompts add sections for their
features. Every write below needs the admin token: `Authorization: Bearer fsr_…`.

The [README](../README.md) has the five-minute quickstart. This guide covers everything after it:
the token, sources, policies and retention, triggers and channels, inbound webhooks and external
data providers, streaming for frontends, and
running the host (storage, clock, binds, remote access, mDNS, backups and troubleshooting).

## First start and the admin token

`docker compose up -d` starts PostGIS and the broker. On the first start the broker generates an
admin token and prints it **once**, in a delimited block:

```sh
docker compose logs foresignal | grep -A3 "FORESIGNAL ADMIN TOKEN"
```

The token starts with `fsr_`. Only its SHA-256 hash is stored, so it cannot be shown again: put it
in a password manager. Send it as `Authorization: Bearer fsr_…`.

- **Rotate** (you still have the token): `POST /v1/system/admin-token/rotate` returns a new token
  once; the old one stops working immediately.
- **Reset** (you lost it): restart once with `FORESIGNAL_RESET_ADMIN_TOKEN=true`
  (for example `FORESIGNAL_RESET_ADMIN_TOKEN=true docker compose up -d foresignal` after adding the
  variable to the service's `environment`), read the new token from the log, then remove the
  variable again, or every restart replaces the token.

Data reads (`GET` of records, changes, tiles and alerts, and the streams) need no token while the
`security` setting's `openReads` is on, which is the default on a trusted network. Every write, the
fetch log and `/v1/system`'s full document need a token. The admin token is the `api_token` row
named `bootstrap`; give agents, dashboards and phones their own scoped tokens instead (see
[Tokens, pairing and the audit log](#tokens-pairing-and-the-audit-log)). An install upgraded from
before prompt 023 keeps its admin token: it moves into that row on the first start.

## Signing in and sessions

Release 2 adds user accounts beside tokens (`docs/RBAC-Design.md`). Until an admin account exists, everything works on
the admin token exactly as above.

- **Sign in:** `POST /v1/auth/login` with `{"email": "…", "password": "…"}` returns a session bearer starting `fss_`,
  shown once. Send it as `Authorization: Bearer fss_…`, like a token. A wrong email or password, or an account that is
  invited, suspended or disabled, always gets the same `401 INVALID_CREDENTIALS`; the reason is in the audit log.
- **Lifetimes:** a session lasts at most **12 hours** and ends after **2 hours** without use; each request moves the
  idle limit forward. It also ends when you sign out (`POST /v1/auth/logout`), when the password changes, and when an
  admin suspends or disables the account. A role change made through user management applies to the next request.
- **Your sessions:** `GET /v1/auth/sessions` lists them (the one you are using is `current`);
  `DELETE /v1/auth/sessions/{id}` ends one; `DELETE /v1/auth/sessions` ends all the others and keeps the current one.
  These need a session, not an API token.
- **Forgotten password:** `POST /v1/auth/password/reset-request` always answers `202`. If the account is active, a
  reset link valid for one hour is emailed (see [Account email (SMTP)](#account-email-smtp)); without SMTP it is only
  logged as "not sent", by its 8-character prefix, and an admin can send a fresh one from user management. `POST /v1/auth/password/reset` with the link's token and a new password (at least
  12 characters) ends every session of the account.
- **Activation:** an invited user sets a first password with `POST /v1/auth/activate` and the activation token
  (valid 72 hours).
- **Limits:** sign-in, reset and activation requests are limited to 10 a minute per address, and repeated failures
  from one address back off, whenever rate limiting is on.
- **What a session may do:** an operation that names a permission (`x-foresignal-permission` in the API document)
  needs that permission from one of the account's roles, else `403 PERMISSION_DENIED` naming it in
  `requiredPermission`; any other operation goes by a scope derived from the roles (Admin and Operator `admin`, Editor
  `agent`, Viewer and Auditor `read`). API tokens are unaffected: they go by scope alone, and the admin token can do
  everything. Sessions never work on ingest endpoints; use ingest keys there.

## Account email (SMTP)

The account emails — welcome with the activation link, password reset, roles changed, account suspended — go out
through one global SMTP server, separate from the email notification channels. It needs the `system.settings`
permission or the admin token.

- **Configure:** `PUT /v1/settings/smtp` with `{"enabled": true, "host": "smtp.example.org", "port": 587,
  "security": "starttls", "username": "…", "password": "…", "fromAddress": "…", "fromName": "…"}`. `security` is
  `starttls` (default, port 587), `tls` (implicit TLS, usually 465) or `none`. The from-address defaults to
  `info@remoteidsystems.com` and the name to `Foresignal`. The password is stored encrypted and never shown:
  `GET /v1/settings/smtp` lists `secretsSet: ["password"]` instead. Leave `password` out to keep it; send `""` to
  clear it.
- **Test:** `POST /v1/settings/smtp/test` with `{"to": "you@example.org"}` sends a short message with the stored
  settings and answers `{"ok": true}` or `{"ok": false, "message": "<the SMTP error>"}`.
- **What each email says:** the welcome greets the user by full name, lists their roles, and carries the activation
  link with its expiry (72 hours); the reset email carries the reset link, its expiry (one hour) and "if you did not
  request this, ignore this email"; the roles-changed email lists the new roles; the suspended email says sign-in is
  blocked until an admin restores the account. Plain text plus HTML, no tracking, no images.
- **Links:** built from the `clientLinks.activate` and `clientLinks.passwordReset` templates of
  `PUT /v1/settings/server`, defaults `foresignal://activate?u={publicBaseUrl}&t={token}` and
  `foresignal://reset-password?u={publicBaseUrl}&t={token}`. Point them at an `https://` page if your users open
  email on a device without the app. Set `publicBaseUrl` so the link names your server.
- **Without SMTP nothing fails:** inviting, resetting and suspending all still work. While accounts exist and SMTP is
  off, `/v1/system` warns `SMTP_NOT_CONFIGURED`, and user management returns each activation or reset token to you
  (`activation.token`, or the `202` body) to pass on yourself. A send that fails is treated the same way.

## Adding and managing users

User management needs the `users.manage` permission (the Admin role) or the admin token. Every change is in the audit
log.

- **Invite:** `POST /v1/users` with `{"email": "…", "fullName": "…", "roles": ["<role id>"]}` creates the account as
  `invited`, with no password, and issues an activation link valid 72 hours. The welcome email carries it; when the
  email is not sent (SMTP is off or failed, or you pass `"sendInvite": false`), the response's
  `activation.token` holds the one-time token for you to pass on, once. The built-in role ids are
  `00000000-0000-4000-8000-00000000000N` with N = 1 Admin, 2 Operator, 3 Editor, 4 Viewer, 5 Auditor. An email already
  in use is `409 EMAIL_TAKEN`.
- **Find and edit:** `GET /v1/users` (filters `status`, `q`; paged with `cursor`), `GET /v1/users/{id}`, and
  `PUT /v1/users/{id}` for the full name and email. Passwords are never set here.
- **Roles:** `POST /v1/users/{id}/roles` with `{"roles": [...]}` replaces the whole set; it applies to the user's next
  request.
- **Suspend:** `POST /v1/users/{id}/suspend` ends every session and blocks sign-in, keeping the account and its
  configuration; `POST /v1/users/{id}/unsuspend` restores it.
- **Links again:** `POST /v1/users/{id}/resend-invite` replaces an invited user's activation link (`409
  ALREADY_ACTIVE` once the account is active); `POST /v1/users/{id}/password-reset` sends a reset link and ends the
  user's sessions. Both answer `202` with the link's token only when it was not emailed. You never see or set the
  password itself.
- **Delete:** `DELETE /v1/users/{id}` moves what the user owned (alerts, channels, personal providers, views) to an
  admin first: you, when you are signed in as an admin; otherwise the longest-standing other admin. With no admin
  account at all it becomes instance-owned. `?reassign=false` deletes it instead. The answer says which, and to whom.
- **The last admin is protected:** suspending, deleting or removing `users.manage` from the only active account that
  has it is refused with `409 LAST_ADMIN`. Give a second account the Admin role first. The admin token keeps working
  regardless.

## Sources

A source is one upstream feed, such as `usgs-earthquakes`. A fresh install enables only the **core set** (prompt 150),
the live-tracking picture: `adsblol-world`, `adsblol-mil`, `airplaneslive-mil`, `adsbfi-blindspots`,
`airplaneslive-blindspots`, `opensky-states`, `derived-aircraft`, `celestrak-gp`, `usgs-earthquakes`, `nws-alerts`,
`nasa-firms`, `meshtastic-map` and `meshtastic-mqtt`. A core source that needs a credential or parameter
(`nasa-firms`, a MAP_KEY) is installed disabled until you configure it. **Every other source is installed disabled**,
free or not, so where a section below says a source "starts enabled (free)", read it as: it needs no key, and
`enable-free` turns it on. Setting `foresignal.sources.default-enable` to false (it is true) installs every source
disabled. A source gated by a required credential or a required parameter stays **disabled** until you configure it;
the `local-acars-*` slots, `usgs-water` and `snotel` count as gated too, because each refuses an empty configuration
(an endpoint, an area or a station list) although no single parameter is marked required. Custom push sources and
external providers keep their own rules. You can disable any source with `PUT /v1/sources/{id}` and
`{"enabled": false}`. These defaults apply only when a source's settings row is first created, so an existing install
keeps the rows it has. Two admin calls enable more in one go; both are idempotent, disable nothing, and answer
`{"enabled": [ids], "count": N, "skipped": M}` (`skipped` counts the disabled built-in sources each left alone):

- `POST /v1/sources/enable-core` enables the disabled core sources, the way an install seeded before prompt 150 adopts
  the core default.
- `POST /v1/sources/enable-free` enables every disabled built-in free source, except those held off for their
  bandwidth or terms (`wiki-edit-surge`, prompt 146).

Neither can tell a source you disabled on purpose from one that was never enabled, so disable those again afterwards.
Enabling a source is the only thing that makes Foresignal contact that upstream; reading or streaming data never triggers a fetch.

| Call | What it does |
|---|---|
| `GET /v1/sources` | Every source, with status, tier, license and today's budget use |
| `GET /v1/sources/{id}` | Descriptor, settings and health of one source |
| `PUT /v1/sources/{id}` | Partial settings change (admin) |
| `PUT` / `DELETE /v1/sources/{id}/credentials` | Store or remove upstream keys (admin; never returned) |
| `POST /v1/sources/{id}/fetch` | Fetch once now, ingesting normally (admin; uses budget) |
| `POST /v1/sources/{id}/probe` | Diagnostic fetch that never ingests (admin; uses budget) |
| `GET /v1/sources/health`, `GET /v1/sources/{id}/health` | Health, coverage and storage cost |
| `GET /v1/sources/{id}/usage` | Requests, bytes and ingest counters over time |
| `GET /v1/sources/{id}/fetches` | Per-fetch provenance log (admin) |
| `POST /v1/sources/enable-core`, `POST /v1/sources/enable-free` | Enable the core set, or every free source (admin) |
| `GET` / `PUT /v1/settings/collector` | How many fetches pull from upstream at once (admin) |

### How many pulls run at once

Every polling fetch, scheduled or manual, and every polling probe holds one permit of a global pull governor while it
pulls from upstream (prompt 150). The runtime setting `maxConcurrentPulls` sets how many permits there are: **2** by
default, 1 to 16 (a value outside that range is clamped). Streams are not counted. A fetch that is due while every
permit is held waits its turn, in arrival order, and its timeout starts only once it pulls. Each source keeps its own
interval, host pacing and budget beneath the governor.

```
GET /v1/settings/collector            -> {"maxConcurrentPulls": 2}
PUT /v1/settings/collector            {"maxConcurrentPulls": 6}
```

The change takes effect at once: a larger value admits waiting fetches immediately; a smaller one admits no new pull
until enough running ones finish, and interrupts none. Two is sized for the core set. With many sources enabled (after
`enable-free`, say) two permits can leave sources waiting longer than their interval; raise the value until
`GET /v1/sources/health` shows them fetching on time, within what the machine and its connection can carry.

Enable a source, with a slower poll and a smaller budget:

```json
PUT /v1/sources/usgs-earthquakes
{"enabled": true, "interval": "5m", "budget": {"perMinute": 2, "perDay": 500}}
```

Omitted fields are left unchanged. A source that needs credentials returns `409 CREDENTIALS_REQUIRED`
until you store them with `PUT /v1/sources/{id}/credentials`.

### Credentials and budgets

- **Credentials:** `PUT /v1/sources/{id}/credentials` with the fields the descriptor lists under
  `credentialFields` (for example `{"apiKey": "…"}`). They are encrypted with AES-256-GCM under the key
  in `${FORESIGNAL_DATA_DIR}/secret.key` and are never returned; a source only shows
  `credentialsSet: true`. `DELETE` removes them.
- **Budgets:** every source has a request budget (`budget.perMinute`, `budget.perDay`) that the
  broker never exceeds. A refused request is logged as a `REFUSED` fetch row, a manual fetch over
  budget answers `429`, and the scheduler simply waits. Scheduled polls, manual fetches and probes
  all spend the same budget. `budgetUsedToday` is in `GET /v1/sources`; the day is the UTC day.
- **Interval:** `interval` (shorthand such as `60s`, `5m`, `1h`, or ISO-8601) cannot go below the
  descriptor's minimum. An enable or interval change applies within seconds; the first poll after
  an enable is immediate.

### Health, usage and the fetch log at a glance

| Question | Call |
|---|---|
| Is it working? | `GET /v1/sources/{id}/health`: `status` (`healthy`, `degraded`, `failing`, `idle`, `disabled`), `statusReason`, `lastSuccessAt`, `lastError`, `consecutiveFailures`, `coverage` and `storage` |
| Which sources need attention? | `GET /v1/sources/health?status=failing,degraded` |
| How much is it costing upstream? | `GET /v1/sources/{id}/usage?resolution=hour` (requests, bytes, errors, refused, records seen, versions, touches) |
| What did each fetch do? | `GET /v1/sources/{id}/fetches` (below) |
| What does the upstream send right now? | `POST /v1/sources/{id}/probe` (a diagnostic fetch that never ingests) |

### Aircraft sources

By default Foresignal tracks aircraft the way Shadowbroker does: the same upstreams, the same circles and the same
cadence. These start enabled on a fresh install (free, no key):

| Source | What it covers | Default interval · budget |
|---|---|---|
| `adsblol-world` | Every aircraft adsb.lol sees, worldwide: six continent-sized circles requested together | 60 s · 12/min, 9,000/day |
| `adsblol-mil` | Aircraft flagged military in adsb.lol's database, worldwide | 60 s · 4/min, 3,000/day |
| `airplaneslive-mil` | Aircraft flagged military in airplanes.live's database (refused until airplanes.live approves you) | 60 s · 2/min, 2,880/day |
| `adsbfi-blindspots` | Eight 250 nm circles the worldwide feed covers thinly, from adsb.fi | 30 min · 10/min, 400/day |
| `airplaneslive-blindspots` | The same eight circles from airplanes.live (refused until approved) | 30 min · 10/min, 400/day |

`opensky-states` (worldwide, every 300 s) is part of the same profile, but Shadowbroker queries OpenSky only with an API
client, so it starts **off**: set its `clientId` and `clientSecret`, then enable it. Without credentials it runs
anonymously on 400 credits a day, which a 300 s world poll spends by mid-morning; that is an addition, not the profile.

`adsblol-world` asks adsb.lol for 2,000 nm circles (2,500 nm for Africa). adsb.lol's API description documents 250 nm as
the maximum, and the server answers larger radii anyway; if adsb.lol starts enforcing its limit, the source fails with
the upstream's error and the blind-spot sources and OpenSky still run.

**Additions, off by default.** Everything else Foresignal offers for aircraft is yours to turn on:

| Source | What it adds | Default interval · budget |
|---|---|---|
| `adsbfi-mil` | Military aircraft from adsb.fi, the keyless alternative to `airplaneslive-mil` | 30 s · 2/min, 2,880/day |
| `opensky-states` without credentials | Anonymous OpenSky (set the interval to at least 900 s) | 300 s · 2/min, 1,500/day |
| `adsblol-area-1`, `-2`, `-3` | Every aircraft within `radiusNm` (≤ 250 nm, default 250) of a point you set | 60 s · 2/min, 1,500/day each |
| `adsbfi-region-1` … `-4` | Every aircraft within `distNm` (≤ 250 nm, default 150) of a point you set | 30 s · 2/min, 2,880/day each |
| `local-adsb-*`, `local-sbs-*`, `station-readsb-*` | Your own receivers | see their sections |

Set an area slot's center before enabling it:

```json
PUT /v1/sources/adsblol-area-1
{"enabled": true, "params": {"lat": 41.98, "lon": -87.90, "radiusNm": 250}}
```

All of these are TRACK sources. Each aircraft that has moved gets a trail point every 60 s (Shadowbroker adds a point each
minute only when the position changed); an unmoved aircraft writes one every 240 s. A point comes sooner
when it deviates 1 km from its dead-reckoned track, turns 20° or changes altitude by 300 m; a callsign, registration,
type, squawk, emergency, ground state or navigation-quality change is always kept. An aircraft that stops reporting
expires after its TTL: 6 minutes for the 60 s sources, 15 for OpenSky, and 35 for the blind-spot sources, which keep an
aircraft at its last reported position until their next poll, as Shadowbroker does.

**An install seeded before 2026-10-22** keeps its own settings rows, so the profile has to be applied once:

```json
PUT /v1/sources/adsblol-mil        {"interval": "60s"}
PUT /v1/sources/airplaneslive-mil  {"interval": "60s"}
PUT /v1/sources/adsbfi-mil         {"enabled": false}
```

`adsblol-world` and the two blind-spot sources are new, so they are seeded enabled on the first start of the new version.
An `ingestPolicy` override you set on an aircraft source keeps its own keyframe.

### adsb.lol access

adsb.lol is a community network; its API is free. Its terms (the description in `https://api.adsb.lol/api/openapi.json`
and `https://github.com/adsblol/api`) say: "In the future, you will require an API key which you can get by feeding to
adsb.lol." Feeding (running a receiver that sends data to adsb.lol) is the supported route to continued access, and it
also gives access to adsb.lol's re-api and raw aggregated data. The data is ODbL: share-alike, with attribution
("adsb.lol").

Foresignal identifies itself on every request (`Foresignal/<version> (+https://foresignal.ai; install=<id>)`), honors
`Retry-After` and waits 30 s after a 429 that has none. If adsb.lol answers **HTTP 451**, or **403**, Foresignal treats it as
a refusal of this installation:

- the source goes into a **one-hour backoff** and is retried hourly, never faster;
- `GET /v1/sources/{id}/health` shows `failing` with the reason "upstream refused this client (HTTP 451); see Operator
  Guide → adsb.lol access";
- Foresignal never changes its User-Agent to look like a browser, adds browser headers or switches to another address to
  get past the refusal.

What to do: feed adsb.lol, or ask its maintainer (contact details in the API repository). When a key scheme is published,
a later Foresignal release adds an optional `apiKey` credential; until then these sources take no credentials. An HTTP 200
whose body is HTML rather than JSON is also treated as throttling: the fetch fails with "non-JSON 200, treated as
throttling" and nothing is ingested.

**adsb.fi and airplanes.live (prompt 080)** follow the same rules, and the generic refusal reason above points here for
them too:

- `adsbfi-mil` and `adsbfi-region-1..4` share the host `opendata.adsb.fi`, which allows one request per second and
  temporarily restricts an address that keeps making invalid requests (400, 401, 403, 404, 429). Foresignal keeps
  requests to that host strictly serial, at least 1.1 s apart, across all five sources; when a request cannot start
  within 30 s the run is skipped with the reason "host pacing" and nothing is sent. adsb.fi data is for personal,
  non-commercial use, with attribution to adsb.fi; the sources are `LOCAL_ONLY` by default.
- `airplaneslive-mil` is refused today: airplanes.live answers every anonymous request with HTTP 403 and asks projects to
  email `contact@airplanes.live` with links and a description. Health then reads "upstream refused this client (HTTP
  403): access approval required (contact airplanes.live)", and the source is retried hourly. Enable `adsbfi-mil` for
  military aircraft without that step.

### OpenSky credentials and credits

`opensky-states` runs anonymously unless you store API client credentials. Create an API client on your OpenSky account
page and store both values together (one without the other is refused with `400`):

```json
PUT /v1/sources/opensky-states/credentials
{"clientId": "…", "clientSecret": "…"}
```

Foresignal exchanges them for an OAuth2 token (valid 30 minutes, reused and refreshed a minute early) and never sends
basic authentication, which OpenSky no longer accepts. A refused client id or secret shows as failing with "OpenSky refused
the client credentials".

OpenSky meters `/states/all` in **credits**: 400 a day anonymous, 4,000 for a standard account, 8,000 for an active feeder
and 14,400 for a licensed account (refilled hourly, per OpenSky's documentation). One call costs 1 to 4 credits by the area
of the `bbox` parameter (≤ 25 sq° 1, ≤ 100 2, ≤ 400 3, larger or the whole world 4). So:

- a standard account polling the world can go down to the 90 s floor (`86,400 / (4,000 / 4)` ≈ 86 s);
- **anonymous world polling needs an interval of at least 900 s** (400 / 4 = 100 calls a day); a small `bbox`, such as
  `"bbox": "5,45,10,48"` (1 credit), allows more.

After every response Foresignal stores OpenSky's `X-Rate-Limit-Remaining`. When today's balance (UTC day) would drop below
the call's cost plus `creditReserve` (a parameter, default 40), the fetch is not sent: the fetch log shows a `REFUSED` row
("upstream quota deferred", with the retry time, the next UTC midnight) and health shows `degraded`, not `failing`. A 429
with `X-Rate-Limit-Retry-After-Seconds` puts the source in backoff for that long.

OpenSky's data is for non-commercial research under its terms (`https://opensky-network.org/about/terms-of-use`; read
them yourself, the page does not answer automated requests). The source is `egress: LOCAL_ONLY`: its records are served to
your own API clients, tiles, streams and notifications, but never re-served by sinks, federation, share links or feeds.
Credit OpenSky as its descriptor's `attribution` asks (Schäfer et al., IPSN 2014).

### Vessels from aisstream.io

`aisstream` streams global AIS (vessel positions and static data) over a WebSocket and merges each vessel's messages
into one complete record per MMSI. It needs a key, and stays disabled until one is stored (enabling without it is a
`409 CREDENTIALS_REQUIRED`):

1. Create a free account at <https://aisstream.io>, then an API key on its Account page. The key is shown once.
2. Store it and enable the source:

   ```json
   PUT /v1/sources/aisstream/credentials
   {"apiKey": "…"}
   ```

   ```json
   PUT /v1/sources/aisstream
   {"enabled": true, "params": {"bbox": ["-1.8,50.6,-0.9,50.95"]}}
   ```

**Use one key per Foresignal instance, and nowhere else.** aisstream allows 3 subscribed connections per account and 3
open connections per IP, and Foresignal holds one connection per source. A key shared with another program or another
instance makes connections fail in ways that look like a bad key. Storing a new key restarts the stream at once.

**Size the `bbox` to what you need.** `bbox` takes 1 to 10 boxes, each `"minLon,minLat,maxLon,maxLat"`, as a JSON array
of strings (a single string would be split at its commas); the default is the whole world. The world is thousands of
messages a minute. Since September 2026 aisstream limits the bandwidth of connections that do not use compression and
silently drops the excess; Foresignal negotiates `permessage-deflate`, and `GET /v1/sources/aisstream/health` shows
`details.compressionEnabled` once aisstream confirms it. If it shows `false`, narrow the boxes. A box must not cross the
antimeridian: split it in two. `messageTypes` limits the AIS message types (default all five the merge uses) and `mmsi`
follows up to 50 vessels only. Changing `params` while the stream runs resends the subscription on the same connection.

**Health.** A connection that has delivered no AIS message for 120 s shows `degraded` ("connected but silent for N
s"); after 300 s Foresignal drops it and reconnects on the ladder (5 s, 15 s, 60 s, 300 s, then every 900 s). A rejected
key (an error naming the key, or two closes in a row before aisstream confirms the subscription; aisstream answers an
invalid key by closing the socket) is retried only hourly; fix the key and store it again. Each connection attempt counts
against the budget (2 a minute, 144 a day).

aisstream publishes no terms of use or redistribution clause, and asks you to proxy only what your clients need, so the
source is `egress: LOCAL_ONLY`, like `opensky-states`: served to your own clients, never re-served by sinks, federation,
share links or feeds. No key means no vessels from this source; keyless official AIS (`ais-kystverket`,
`ais-digitraffic`) and your own AIS-catcher (`local-ais-{n}`) need no key (next section).

### Keyless AIS and your own AIS-catcher

Prompt 085 adds three vessel sources that need no key. `ais-kystverket` and `ais-digitraffic` start enabled (free);
`local-ais-{n}` stays disabled until you set its `endpoint`. They emit the same
`vessel` records as `aisstream` (the same payload keys, the same SAMPLED policy and 30-minute TTL), decoded by
Foresignal's own AIVDM codec.

- **`ais-kystverket`** reads the Norwegian Coastal Administration's open AIS stream (`153.44.253.27:5631`, NMEA with
  TAG blocks) over raw TCP, receive-only. It covers the Norwegian economic zone and the Svalbard and Jan Mayen
  protection zones; fishing vessels under 15 m and leisure craft under 45 m are left out by Kystverket. The data are
  under the Norwegian Licence for Open Government Data (NLOD 2.0), so the source is `OPEN`; credit "Contains data
  under the Norwegian license for Open Government data (NLOD) distributed by the Norwegian Coastal Administration"
  wherever you show it. **Best effort:** in October 2026 the endpoint was still listed as open, but connections from
  outside Norway (a US host, and the build host) timed out. While it is unreachable the source shows `failing`, and
  Foresignal backs off from 30 s to 15 minutes (at most 12 connects an hour); it never tries another route.
- **`ais-digitraffic`** reads Fintraffic's Digitraffic marine MQTT over WebSocket (Finland and the northern Baltic)
  and fetches the vessel metadata list every 12 hours. Location fixes older than 24 hours are dropped. If
  Digitraffic's own status topic has not been updated for 10 minutes, the source shows `degraded`. CC BY 4.0, `OPEN`;
  credit "Source: Fintraffic / digitraffic.fi, licence CC 4.0 BY".
- **`local-ais-{n}`** reads your own AIS-catcher over TCP on your network (next paragraphs).

```json
PUT /v1/sources/ais-digitraffic
{"enabled": true}
```

**Your AIS-catcher as a TCP server.** Start AIS-catcher with `-S <port>`, which serves its NMEA lines to TCP clients
on that port (for example `AIS-catcher -S 5012` plus your usual device options), then point a slot at it. The port is always explicit; there is
no default:

```json
PUT /v1/sources/local-ais-1
{"enabled": true, "params": {"endpoint": "tcp://192.168.1.30:5012", "stationName": "rooftop"}}
```

Foresignal only reads: nothing is ever written to the socket. The host must be on your network (a public address is
refused). `stationName` (default `local`) names your station as the observer of every record, and
`GET /v1/sources/local-ais-1/health` shows `details.messagesPerMin`, `details.distinctMmsi10m`,
`details.lastMessageAt` and `details.vessels`. A connection silent for 120 s is closed and reopened (5 s to 5 minutes).
The slots are `egress: LOCAL_ONLY`; you may set them to `OPEN`.

**Your station's position stays private by default.** AIS-catcher's NMEA carries no station position, and Foresignal
stores none unless you ask: set `publishStationPosition` to `true` together with `stationLat` and `stationLon` to get
one `ais-receiver` record on the map. Without `publishStationPosition`, `stationLat` and `stationLon` are refused
(`400 VALIDATION_FAILED`), so the position is never stored or emitted.

For an AIS-catcher somewhere else (a remote site that can reach Foresignal but not the other way round), the
`aiscatcher` HTTP push preset ("Inbound webhooks") remains the alternative.

### Local RF decoders

Prompt 086 brings in your own SDR decoders: aviation datalink metadata, radiosondes, a GNSS integrity sentinel, drone
Remote ID, Raspberry Shake triggers and NOAA Weather Radio SAME headers. Every source is disabled until you configure it
(each needs your decoder's address; the Shake and SAME sources are custom sources you create),
reads only (it never writes to your decoders, except gpsd's watch command), and accepts only addresses on your network
with an explicit port. The legal and privacy rules below are enforced in code; there is no setting to turn them off.

**Aviation datalink (`local-acars-{n}`).** Run [acars_router](https://github.com/sdr-enthusiasts/acars_router) in front
of acarsdec, dumpvdl2 and dumphfdl; its TCP servers default to 15550 (ACARS), 15555 (VDLM2) and 15556 (HFDL). Give a slot
one, two or all three:

```json
PUT /v1/sources/local-acars-1
{"enabled": true, "params": {"acarsEndpoint": "tcp://192.168.1.40:15550", "vdlm2Endpoint": "tcp://192.168.1.40:15555",
 "hfdlEndpoint": "tcp://192.168.1.40:15556", "stationId": "home"}}
```

**Legal note on datalink content.** Foresignal stores only message metadata (protocol, registration, flight, label,
mode, block and message numbers, frequency, signal level, ICAO address, ground station, and whether the message had
text). The message text is dropped before a record is built and is never stored, logged, embedded or forwarded:
interception-disclosure laws such as US 18 U.S.C. §2511 and the UK Wireless Telegraphy Act 2006 s.48 make passing on
the content of these messages unlawful. The slots are **private by definition**: `PUT … {"private": false}` is refused,
their records never reach anonymous readers, tiles, embeddings, a remote LLM, MCP or Ask tools for callers without the
private grant, or a sink, and their `LOCAL_ONLY` egress cannot be changed (`400 EGRESS_FIXED`). They are kept 7 days.
Geometry appears only when an HFDL position report was decoded.

**Radiosondes (`local-sonde`).** radiosonde_auto_rx broadcasts `PAYLOAD_SUMMARY` datagrams (its
`payload_summary_port` defaults to 55673). Listen for them and name your station:

```json
PUT /v1/sources/local-sonde
{"enabled": true, "params": {"endpoint": "udp://0.0.0.0:55673", "allowSenders": ["192.168.1.41"],
 "stationCallsign": "N0CALL-1"}}
```

`stationCallsign` becomes the observer of every record (otherwise the station auto_rx names), with the sonde's
frequency; the records are the same `radiosonde` records as `sondehub`.

**GNSS integrity sentinel (`local-gpsd-{n}`).** The gpsd slot from "Local receivers" also judges the integrity of
your GNSS every 30 seconds against a 15-minute baseline and writes one `gnss-integrity` record per receiver:
`LOST` (no fix for more than a minute), `JAMMED` (the receiver reports jamming, or signal strength fell 10 dB while
half the satellites were lost), `SUSPECT_SPOOF` (a fixed antenna whose position jumped more than 500 m or whose clock
stepped more than a second while signals grew stronger), `DEGRADED` (accuracy three times worse than usual, or fewer
than four satellites) or `OK`. Set `fixedStation` to `false` for a moving antenna (the spoofing rule then stays off),
`stationLabel` to name the station in the log, and `ubxMonitor` to `true` with a u-blox receiver to read its own
jamming indicator from gpsd's raw passthrough:

```json
PUT /v1/sources/local-gpsd-1
{"params": {"endpoint": "tcp://192.168.1.60:2947", "fixedStation": true, "ubxMonitor": true, "stationLabel": "roof"}}
```

Foresignal writes only gpsd's `?WATCH` command (with `"raw":2` for `ubxMonitor`); every write passes an allowlist of
`?WATCH`, `?VERSION`, `?DEVICES` and `?POLL`, so nothing can reconfigure your receiver. A state change is logged once at
INFO. The records stay private with the rest of the slot and are kept 30 days. Trigger on `integrityState` like any
significant field.

**Drone Remote ID (`local-remoteid`).** A WarDragon kit running [DragonSync](https://github.com/alphafox02/DragonSync)
publishes drones to an MQTT broker. Point the source at your broker (credentials go in
`PUT /v1/sources/local-remoteid/credentials` as `username` and `password`):

```json
PUT /v1/sources/local-remoteid
{"enabled": true, "params": {"brokerUrl": "mqtt://192.168.1.10:1883", "topics": ["wardragon/drones"]}}
```

**Remote ID privacy defaults.** Only ASTM F3411 / ASD-STAN Remote ID (Wi-Fi and Bluetooth) is ingested: DJI DroneID
detections are proprietary and are dropped (counted in `foresignal.remoteid.dropped{reason="dji"}`). Pilot and home
positions are removed before a record is built, so they are never stored. If you want the pilot position and your
deployment allows it, make the source private and set `keepOperatorPosition` in the same request:

```json
PUT /v1/sources/local-remoteid
{"private": true, "params": {"brokerUrl": "mqtt://192.168.1.10:1883", "keepOperatorPosition": true}}
```

It is then kept as payload `operatorPoint`, never as the record's geometry. On a source that is not private the
parameter is refused with `400 PRIVATE_REQUIRED`, and so is making the source public while it is set. Drones are TRACK
records that expire 3 minutes after their last report.

**Raspberry Shake (`rsudp-alarm` preset).** Create a custom source from the preset with your Shake's position, then
install `contrib/rsudp/foresignal_alarm.py` as rsudp's `custom` code file (instructions in `contrib/rsudp/README.md`).
Each STA/LTA alarm becomes a `seismic-trigger` record at the station point. rsudp runs custom code only on an alarm; a
reset needs your own wrapper. Unverified against a real Shake.

**NOAA Weather Radio SAME (`same` format).** Create a custom source with the kind `same-alert`, then run the recipe in
`contrib/same/` (`rtl_fm … | multimon-ng -a EAS -t raw - | foresignal-post`). Each header becomes one `same-alert`
record drawn on the named counties (the whole state for county `000`) when the reference data is loaded, valid until the
header's purge time; the three repetitions of a header collapse into one, and `NNNN` is ignored.

### Aviation safety

Prompt 087 adds the US aviation-safety picture. Every source starts enabled (free) except `faa-uasfm`, which stays
disabled until you set its `bbox`; disable any with `PUT /v1/sources/{id}`. All are public-domain US
Government data.

| Source | What it gives | Notes |
|---|---|---|
| `faa-tfr` | Temporary flight restrictions with their real polygons, floors, ceilings and validity | Times come from the XNOTAM in UTC. A TFR that starts later is stored with its `validFrom` and matches `forecastHorizon` rules only from then; one without an end stays until the FAA removes it from the list (then it is tombstoned). |
| `faa-nas-status` | Ground delay programs, ground stops, arrival and departure delays, closures, deicing | Placed at the airport from the bundled US airport table; an airport missing from it gives a record without a position. |
| `awc-sigmets`, `awc-gairmets`, `awc-cwas` | SIGMETs and AIRMETs, G-AIRMET snapshots, Center Weather Advisories | A canceled advisory is tombstoned at the next poll. A G-AIRMET snapshot becomes valid at its `validTime`. |
| `awc-pireps` | Pilot reports of the last two hours inside a box | See "Choosing a PIREP box" below. |
| `awc-tafs` | Terminal Aerodrome Forecasts, one per station | `scope` (`aoi`, `conus`, `global`) reads the AWC cache file; `stations` reads the TAF API for just those stations. METARs remain `awc-metars`. |
| `faa-uasfm` | The FAA UAS Facility Map grid: the altitude up to which the FAA may authorize drone flight near airports | `bbox` is required (the grid is national: about 380,000 cells). |
| `faa-uas-*` | National security UAS restrictions (permanent and part-time), recreational flyer sites, stadiums, prohibited areas, national defense TFR areas | Feature-kit sources, read daily. |

**AWC's limits.** The Aviation Weather Center limits every client to **100 requests per minute**, asks you to wait
between requests (about one per minute per endpoint) and blocks clients that exceed it. The defaults stay far inside:
all six AWC sources together budget 10 requests per minute, none polls faster than every 2 minutes, requests run one
after another, and a 429 puts the source in backoff for the time AWC names in `Retry-After`. If you raise an AWC
source's budget or shorten its interval, keep the sum of the six well under 100 per minute (other software on the same
public address counts too).

**Choosing a PIREP box.** AWC refuses a PIREP query without a box and returns at most 400 reports per query. The default
box is the conterminous US (`-130,20,-60,55`, west,south,east,north), which can exceed 400 reports in a busy afternoon,
so the oldest reports of the two-hour window are then missing. Set `bbox` to the region you care about (for example
your places plus a few hundred kilometers) to keep busy periods complete; Foresignal sends it to AWC latitude first.

**The UAS Facility Map** is read in full for your box on the first run and every 56 days (the FAA chart cycle); weekly
runs in between ask only for cells edited since the last run. A larger box costs more requests on full reads
(2,000 cells per request, at most 50,000 cells per run, `maxFeatures`).

**NOTAMs are not collected.** The FAA moved NOTAM distribution to the NOTAM Management Service (cutover reported for
18 April 2026); its API needs an approval requested from `notams@faa.gov` and its endpoints are not publicly documented.
TFRs, the NOTAMs that matter most on a map, come from `faa-tfr`.

### Cyber feeds and Certificate Transparency

Prompt 076 adds five sources. `cisa-kev`, `abusech-feodo` and `nvd-cves` start enabled (free); `abusech-urlhaus`
(needs `authKey`) and `ct-domain-watch` (needs `domains`) stay disabled until configured:

- **`cisa-kev`** (the CISA Known Exploited Vulnerabilities catalog) and **`abusech-feodo`** (botnet C2 servers)
  need no credential.
- **`abusech-urlhaus`** needs a free abuse.ch Auth-Key from `auth.abuse.ch`:
  `PUT /v1/sources/abusech-urlhaus/credentials` with `{"authKey": "…"}`. Malware URLs are stored as text and as a
  defanged copy (`hxxp://`, `[.]`); Foresignal never fetches them, and `fieldSpecs` marks both `noLink`, so clients
  must not render them as links. Commercial or for-profit use may require a Spamhaus subscription.
- **`nvd-cves`** syncs NVD CVEs every two hours, filtered by `minCvss` (default 7.0) and optionally `vendors`
  (CPE vendor names such as `microsoft`); KEV CVEs are always kept. An optional free NVD key (`apiKey`) removes
  the 6-second pause between requests. Each kept CVE also gets a `cve-epss` record with its EPSS probability and
  percentile as bands only (`epssBand`, `percentileBand`), refreshed daily for `epssRefreshDays` (default 30).
  Notices: "This product uses the NVD API but is not endorsed or certified by the NVD." and "EPSS scores from
  FIRST.org (first.org/epss)".
- **`ct-domain-watch`** lists certificates issued for your own domains through the SSLMate Cert Spotter API:
  set `domains` (up to 25; subdomains and matching wildcards are included) and, optionally, `expectedIssuers`
  (issuer names or CAA domains such as `letsencrypt.org`). An issuance from any other issuer sets
  `unexpectedIssuer: true`, which a rule can alert on. Unauthenticated use is limited to a few queries an hour for
  personal or evaluation use; for more, set a Cert Spotter key (`apiKey`) and raise the source's budget. Domains
  are polled round-robin, so a throttled run resumes where it stopped.

**crt.sh is never polled, and Foresignal ships no CT log client.** If you prefer not to use the Cert Spotter API,
run SSLMate's open-source monitor [`certspotter`](https://github.com/SSLMate/certspotter) (MPL-2.0) yourself and let
its hook script post each discovery to a custom PUSH source (see "Inbound webhooks"):

```json
POST /v1/custom-sources
{"id": "custom-ct", "name": "Certificate Transparency (certspotter)", "kinds": ["certificate-issuance"],
 "observedAtMode": "RECEIVED", "titleField": "watchedDomain"}
```

Put your domains in `~/.certspotter/watchlist` (one per line; a leading dot includes subdomains) and this script in
`~/.certspotter/hooks.d/foresignal` (executable; it needs `jq` and `curl`). `certspotter` sets the environment
variables for each `discovered_cert` event:

```sh
#!/bin/sh
[ "$EVENT" = "discovered_cert" ] || exit 0
jq -n --arg id "$TBS_SHA256" --arg domain "$WATCH_ITEM" --arg cert "$CERT_SHA256" \
      --arg pubkey "$PUBKEY_SHA256" --arg issuer "$ISSUER_DN" --arg nb "$NOT_BEFORE_RFC3339" \
      --arg na "$NOT_AFTER_RFC3339" --arg log "$LOG_URI" \
  '{records: [{externalId: $id, kind: "certificate-issuance", eventTime: $nb, validUntil: $na,
               payload: {watchedDomain: $domain, issuerDn: $issuer, notBefore: $nb, notAfter: $na,
                         certSha256: $cert, pubkeySha256: $pubkey, logUri: $log}}]}' |
curl -fsS -X POST "http://foresignal.local:8080/v1/ingest/custom-ct" \
     -H "X-Foresignal-Ingest-Key: $FORESIGNAL_INGEST_KEY" -H "Content-Type: application/json" --data-binary @-
```

The external id is the TBS digest, which a certificate and its precertificate share, so the pair becomes one
entity. `ISSUER_DN` is the issuer's distinguished name as the CA wrote it; Cert Spotter's curated issuer names are
not available to the self-hosted monitor.

### Local receivers

Prompt 069 adds sources for hardware you run yourself on your own network. Each family has
`foresignal.local-receivers.slots` numbered slots (default 2, at most 16; environment
`FORESIGNAL_LOCAL_RECEIVERS_SLOTS`), all disabled until you set an address and enable them:

| Source | Reads | Default interval · budget |
|---|---|---|
| `local-adsb-{n}` | readsb, dump1090 or skyaware978 `aircraft.json` (`band` 1090 or 978) | 2 s · 60/min, 50,000/day |
| `local-sbs-{n}` | SBS-1 (BaseStation) lines over TCP | stream · 2 connects/min, 200/day |
| `local-gpsd-{n}` | your GNSS receiver through gpsd (private) | stream · 2 connects/min, 200/day |
| `station-readsb-{n}` | readsb `stats.json`: messages per second, signal, noise, `state` ok, low-rate or silent | 60 s · 2/min, 2,000/day |
| `wxstation-davis-{n}` | a Davis WeatherLink Live console | 60 s (never under 10 s) · 6/min, 10,000/day |
| `wxstation-ecowitt-{n}` | an Ecowitt gateway's live data (experimental) | 60 s · 6/min, 10,000/day |

**Addresses.** Every address must resolve to a loopback, private (RFC 1918) or unique-local address. It is resolved
once when you save it and pinned, so a name that later resolves elsewhere cannot redirect Foresignal; redirects are
never followed. Sockets need an explicit port: no default is assumed.

```json
PUT /v1/sources/local-adsb-1
{"enabled": true, "params": {"url": "http://192.168.1.50/tar1090/data/aircraft.json", "band": "1090"}}

PUT /v1/sources/local-sbs-1
{"enabled": true, "params": {"endpoint": "tcp://192.168.1.50:30003"}}

PUT /v1/sources/local-gpsd-1
{"enabled": true, "params": {"endpoint": "tcp://192.168.1.60:2947"}}

PUT /v1/sources/station-readsb-1
{"enabled": true, "params": {"url": "http://192.168.1.50/tar1090/data/stats.json", "minMessagesPerSec": 1.0}}

PUT /v1/sources/wxstation-davis-1
{"enabled": true, "params": {"url": "http://192.168.1.70", "lat": 40.01, "lon": -105.27}}
```

**Enabling the receiver's outputs.**

- **readsb JSON:** readsb writes `aircraft.json` and `stats.json` with `--write-json` and serves them through
  tar1090 or its own web server (`http://host/tar1090/data/aircraft.json`); with `--net-api-port` they are on that
  port too. The path must end in `aircraft.json` (or `stats.json`).
- **readsb SBS:** off unless you start readsb with `--net-sbs-port 30003` (any port you choose; set it in
  `endpoint`).
- **dump978 / skyaware978:** skyaware978 serves UAT aircraft at `http://host:8978/data/aircraft.json` (or
  `/skyaware978/data/aircraft.json` behind the PiAware web server); set `band` to `978`.
- **gpsd:** listens on 2947 by default, on loopback only unless started with `-G` (or `GPSD_OPTIONS="-G"`); allow your
  Foresignal host in its firewall. Foresignal sends `?WATCH={"enable":true,"json":true};` and needs protocol 3.
- **Davis WeatherLink Live:** the local API is on by default; Davis asks clients not to poll more often than every
  10 s, which is the minimum interval.
- **Ecowitt:** `/get_livedata_info` has no official specification and changes with firmware; the parser is tolerant
  and counts ids it does not know (`foresignal.ecowitt.unknown_ids`). After a firmware update, watch the source's
  health for a format change. For Ecowitt's own push ("Customized upload"), use the `ecowitt` mapping preset instead.

**Firewalling UDP.** No source in this prompt listens on UDP, but later ones (AIS, APRS, radiosondes) will: a UDP
endpoint must name `allowSenders` (private IPs or CIDRs), and datagrams from anyone else are dropped unread and counted
(`foresignal.socket.udp.rejected`). Bind to the interface that faces your receivers rather than `0.0.0.0` where you
can, and keep the port closed to the internet in your router and host firewall.

**Why the data stays local.** Every local source is `egress: LOCAL_ONLY`: your own clients, tiles, SSE, MCP and
notifications see it, but sinks, federation, share links and public feed keys do not. This is your data, so unlike a
licensed source you may change it: `PUT /v1/sources/local-adsb-1 {"egress": "OPEN"}` (and `null` to go back); other
sources answer `400 EGRESS_FIXED`. `local-gpsd-{n}` is also **private**: it is your location, so it is never served to
anonymous readers, tiled, embedded or sent to a remote LLM. Weather stations store `lat`/`lon` rounded to about 1 km.

**MLAT terms.** Multilateration positions (`posSource: mlat`) are computed by the aggregator networks your receiver
feeds, under terms that forbid passing them on. Foresignal never sends an MLAT record on a sink, federation, share link
or feed, even from a source you opened, and strips them from anonymous `openReads` responses unless you set
`security.exposeMlat` to true (`PUT /v1/settings/security`). Receiver health: a local `aircraft.json` whose `now` is
more than 10 s old marks `local-adsb-{n}` degraded ("receiver feed stale"), and a `station-readsb-{n}` rule on
`state == silent` tells you when the antenna or dongle has stopped.

### Local air-quality monitors

Prompt 078 adds `airq-local-{n}`: one slot per local receiver slot (`foresignal.local-receivers.slots`, default 2),
each polling one PurpleAir, airrohr (Sensor.Community firmware) or AirGradient monitor on your network every 60 s
(30 s minimum, 4 requests/min, 3,000/day). Like the other local receivers, every slot is disabled until you set it up,
and the address follows the same rules: it must resolve to a loopback, private or unique-local address, it is resolved
once and pinned, and redirects are never followed. Use the monitor's IP address (give it a fixed DHCP lease) rather
than an mDNS `.local` name, which the Foresignal host may not resolve.

| `deviceType` | Request | Location | Indoor default |
|---|---|---|---|
| `purpleair` | `GET {url}/json?live=true` | the sensor's own `lat`/`lon`, or your `lat`/`lon` | from its `place` (`inside`/`outside`) |
| `airrohr` | `GET {url}/data.json` | your `lat` and `lon` (required) | outdoor |
| `airgradient` | `GET {url}/measures/current` | your `lat` and `lon` (required) | from the model: `I-…` indoor, `O-…` outdoor |

```json
PUT /v1/sources/airq-local-1
{"enabled": true, "params": {"deviceType": "airgradient", "url": "http://192.168.1.61", "lat": 40.01,
 "lon": -105.27, "name": "Living room"}}
```

`url` is the device's address only (no path, query or credentials); `indoor` (`true`/`false`) overrides the default;
`name` labels the slot in log lines. An indoor monitor's records are `indoor-air-quality`, an outdoor one's
`air-quality`, with `pm1, pm25, pm10, tempC, humidityPct, pressureHpa` and, on AirGradient, `co2Ppm, tvocIndex,
noxIndex`.

**Enabling each device's local API.**

- **PurpleAir:** the sensor serves its readings at `http://<ip>/json` once it is on your Wi-Fi; nothing needs turning
  on. Foresignal asks for `?live=true` (the newest readings rather than the two-minute average). PurpleAir asks for at
  least 10 s between requests. Temperature and humidity are the sensor's raw values (it reads warm); with both laser
  channels, PM values are their mean and `channelDisagreement` is true when PM2.5 A and B differ by more than
  5 µg/m³ and more than 70%: clean or replace the sensor.
- **airrohr:** the firmware serves the current values at `http://<ip>/data.json` on your network; the device has no
  location, so set `lat` and `lon`. Pressure arrives in pascals and is stored in hPa. Its push to your own API is the
  `airrohr` mapping preset (below), a different path: use one or the other.
- **AirGradient:** firmware 3.0.10 and later (ONE and Open Air) serve `http://<ip>/measures/current`; the monitor is
  also reachable as `airgradient_<serial>.local`, but give Foresignal its IP address. Set `lat` and `lon`.

**Storage.** The time base is the poll time (device clocks disagree), and the records expire after 10 minutes. A
SAMPLED policy keeps the history small: a new version only when PM2.5 moves by 5 µg/m³, PM10 by 10, CO₂ by 100 ppm,
temperature by 1 °C or humidity by 5 points (at most once a minute), when the model or the channel-disagreement flag
changes, and otherwise every 8 minutes.

**Privacy.** These monitors are at your home, so every slot is **private** (never served to anonymous readers, tiled,
embedded or sent to a remote LLM) and `egress: LOCAL_ONLY`, which you may change to `OPEN` as for the other
local receivers.

### Amateur radio sources

Prompt 082 adds five amateur-radio sources. `kiwisdr` and `openmhz-systems` start enabled (free); `pskreporter-ft8`
(needs `appContact`), `aprs-is` (needs `filter`) and `local-aprs-{n}` (needs `endpoint`) stay disabled until
configured. Whoever received a spot or a packet
(a PSK Reporter receiver, an APRS igate, your own TNC) is stored as the record's **observer**, never in its payload:
it comes back only with `include=observer` on an agent or admin token, or when `security.exposeObserver` is on.

| Source | What it is | Default interval · budget |
|---|---|---|
| `pskreporter-ft8` | PSK Reporter reception reports (FT8 by default; FT4, WSPR, JS8, CW) | 10 min (never under 5 min) · 2/min, 300/day |
| `kiwisdr` | the public KiwiSDR web receivers | 24 h · 1/min, 4/day |
| `openmhz-systems` | OpenMHz scanner systems: **blocked**, see below | 1 h · 1/min, 30/day |
| `aprs-is` | APRS from the APRS-IS network, narrow and receive-only | stream · 1 connect/min, 100/day |
| `local-aprs-{n}` | APRS your own TNC hears on RF (KISS over TCP) | stream · 1 connect/min, 1,440/day |

**PSK Reporter.** Set `appContact` to your email address: PSK Reporter asks every client for a contact and for no more
than one query every five minutes. `callsigns` limits the queries to stations you watch (one request each, so keep
the list short); a watched call matches whether it transmitted or received. Without call signs one unfiltered query
runs, capped at `rptlimit` reports.

```json
PUT /v1/sources/pskreporter-ft8
{"enabled": true, "params": {"appContact": "you@example.org", "callsigns": ["N0CALL"], "windowSeconds": 900}}
```

**KiwiSDR.** The list is fetched over plain HTTP from `rx.linkfanel.net` (its HTTPS timed out when checked); it is a
public list and nothing private is sent.

**OpenMHz.** OpenMHz answers Foresignal with a Cloudflare challenge page. The source reports `BLOCKED_CHALLENGE` and
stays `failing`; Foresignal never pretends to be a browser or solves the challenge. It starts enabled like every free
source; disable it (`PUT /v1/sources/openmhz-systems` with `{"enabled": false}`) until OpenMHz
offers API access.

**APRS-IS filter etiquette.** `aprs-is` cannot be enabled without a narrow `filter`: ask APRS-IS only for what you
need. Use a range around you (`r/lat/lon/km`, at most 250 km), a list of stations (`b/N0CALL/N0CALL-9`), call-sign
prefixes (`p/K6/W6`), a type filter with a range (`t/poimqstunw/N0CALL/50`) or a friend range (`f/N0CALL/25`); at
most five terms, and no bare `*`. A filter over a busy area still delivers several packets a second, so start small.

```json
PUT /v1/sources/aprs-is
{"enabled": true, "params": {"filter": "r/37.77/-122.42/50", "login": "N0CALL"}}
```

`login` is your call sign, or leave it empty for an anonymous login (`EL` and six characters of your install id).
`N0CALL` itself is refused by the servers. With `includeMessages` APRS messages are kept too (`aprs-message`); they
are never sent to an embedding provider. With `dedupLocal` (on by default) a packet your own TNC heard in the last
30 seconds is dropped from the APRS-IS copy, because the RF copy is first-hand.

**Receive-only guarantees.** Foresignal never transmits and never igates:
- `aprs-is` logs in with passcode `-1`, which APRS-IS treats as a receive-only client, and writes nothing after the
  login line. Its connection object has no write method at all.
- `local-aprs-{n}` opens the TNC socket receive-only: not a single byte is written, not even a KISS command, and the
  socket is never half-closed (Direwolf would read that as a disconnect).

**Direwolf.** Enable Direwolf's KISS TCP server in `direwolf.conf` with `KISSPORT 8001` (8001 is the conventional
port; set whichever you use), then point a slot at it:

```json
PUT /v1/sources/local-aprs-1
{"enabled": true, "params": {"endpoint": "tcp://192.168.1.20:8001", "stationCallsign": "N0CALL-10"}}
```

The address must be on your own network (loopback, RFC 1918 or unique-local), as for the other local receivers.
`stationCallsign` names your station as the observer of what it hears. Local APRS stays on this instance
(`LOCAL_ONLY`) unless you open it; you can also mark the source private, which rounds positions.

**MeshCore KISS is not AX.25.** A MeshCore KISS modem also speaks KISS over TCP, but its frames carry raw MeshCore
packets, not AX.25. Pointed at one, `local-aprs-{n}` connects, counts every frame in
`foresignal.aprs.local.non_ax25` and stores nothing.

### Mesh networks

Prompt 083 adds five mesh sources. `meshtastic-mqtt`, `meshtastic-map` and `meshcore-bridge` start enabled (free);
`meshtastic-local` (needs `brokerUrl`) and `meshcore-local` (needs `endpoint`) stay disabled until configured. Every
node arrives as a `mesh-node` record:
its names, role or node type, hardware, firmware, battery and last known position. The gateway or your own node that
heard it is the record's observer. Foresignal never decodes Meshtastic protobufs (they are GPL-3.0); every Meshtastic
path uses the firmware's JSON output.

| Source | What it is | Default interval · budget |
|---|---|---|
| `meshtastic-mqtt` | Meshtastic nodes on the public broker, under fixed rules | stream · 4 units/min, 100/day |
| `meshtastic-map` | the community Meshtastic map's node list | 24 h (never less) · 1/min, 2/day |
| `meshtastic-local` | your own Meshtastic node through your own MQTT broker (private) | stream · 20/min, 2,000/day |
| `meshcore-local` | your own MeshCore companion radio over its WiFi TCP firmware (private) | stream · 1 connect/min, 1,440/day |
| `meshcore-bridge` | your own MeshCore companion over USB, BLE or TCP through the bridge script (private) | push · 120/min |

**The public broker's rules, and why they are fixed.** ShadowBroker was reported to the Meshtastic project for one to
two orders of magnitude more broker traffic than a typical client, so `meshtastic-mqtt` enforces these limits in code
and no setting loosens them:
- it subscribes to `<root>/2/json/#` only, one subscription per root, at QoS 0; `root` must look like `msh/US` or
  `msh/US/CA`, and `msh/#`, `+/…` or any other wildcard is refused;
- `maxSubscriptions` is 1 by default and at most 3, and `root` plus `extraRoots` must fit in it (a PUT that breaks
  this is refused with `400 VALIDATION_FAILED`);
- the client id is `foresignal-<installId>`, so the broker's operators can identify it; keepalive 120 s; a clean
  session; it never publishes anything;
- it reconnects from 15 s up to 5 minutes with jitter, at most 12 times an hour, and each connect and subscribe costs a
  budget unit;
- it reads at most 100 messages a minute and drops the rest before parsing (counted as
  `foresignal.mesh.dropped{reason=rate}`).

Expect little data: most public traffic is protobuf, which Foresignal ignores, and the broker reduces public positions
to 10–16 bits (`positionPrecisionBits`; the record's accuracy follows from it). `includeText` keeps public channel text
as records; it is never posted anywhere.

```json
PUT /v1/sources/meshtastic-mqtt
{"enabled": true, "params": {"root": "msh/US"}}
```

**The community map** (`meshtastic.liamcottle.net`) is a hobby service; it is read at most once a day (about 29 MB,
parsed as a stream). Its license is not stated, so its records are marked "unspecified (community service)".

**Your own Meshtastic node.** Run a broker on your network (for example Mosquitto), then on the node's MQTT module set
the broker address, **enable JSON** and **disable encryption on uplink** for the channels you want to see. nRF52 nodes
(RAK4631, T-Echo and the like) cannot emit JSON; if you need one, run a protobuf-to-JSON bridge outside Foresignal
that republishes JSON to your broker (the protobuf path is GPL-3.0 and stays out of this repository). Then:

```json
PUT /v1/sources/meshtastic-local
{"enabled": true, "params": {"brokerUrl": "mqtt://192.168.1.10:1883", "root": "msh/local", "channels": ["Ops"]}}
PUT /v1/sources/meshtastic-local/credentials
{"username": "foresignal", "password": "…"}
```

The broker must be on your own network and the port explicit. With `mqtts://` Foresignal connects by name so TLS can
verify the certificate (system trust store), after checking the name still resolves to a private address. Leave
`channels` empty to see every channel. The source is private: it shows where your neighbors' nodes are.

**MeshCore: bridge or TCP firmware.**
- A companion running the **WiFi companion firmware** listens on TCP; point `meshcore-local` at it. There is no
  default port: the firmware uses 5000 only when it was built without `TCP_PORT`.

  ```json
  PUT /v1/sources/meshcore-local
  {"enabled": true, "params": {"endpoint": "tcp://192.168.1.30:5000"}}
  ```

- A **USB or BLE companion** (or any other) uses the bridge script in `contrib/meshcore-bridge`: it runs next to the
  radio, uses the `meshcore` Python package and pushes to `meshcore-bridge`. Store a key you choose (starting with
  `eik_`) as that source's `ingestKey` credential and give the bridge the same key; its README has a systemd unit.

Both read the companion's contacts (chat nodes, repeaters, rooms, sensors) and its own node, and put every channel and
direct message on the internal mesh message bus that mesh field reports use (prompt 084); messages become
`mesh-message` records only with `includeText`. Foresignal sends the companion only read commands (identify, device
query, contacts, channel names, battery, fetch messages); it never changes a setting, never reboots or resets it, never
touches keys and never sends anything to another node.

**One client per companion.** A MeshCore companion accepts a single TCP client and drops the previous one when another
connects. Inside Foresignal, `meshcore-local` and the `meshcore` channel (prompt 084) share one connection per
companion; outside it, close the MeshCore app on that companion's TCP port (or use BLE for the app), and do not run
the bridge and `meshcore-local` against the same companion.

**Repeater health.** For each repeater among your contacts, `advertStatus` is `ok` while its last advert is under
`repeaterLateAfter` (13 h), `late` until `repeaterSilentAfter` (26 h), then `silent`. It is re-checked every 15 minutes
and changes are stored as they happen, so a rule on `advertStatus` tells you when a repeater goes quiet. Health comes
from adverts alone; Foresignal never logs in to a repeater or asks it for status.

### Flood watching

Prompt 079 adds river, snow, drought and coastal water sources (see the Source Catalog, "Hydrology and coast"). `usdm`
and `ea-flood-warnings` start enabled (free); the others stay disabled until you scope them (step 1 below).
Two of them publish an official flood category you can trigger on: `nwps-gauges` (NOAA river gauges,
`floodCategory` `no_flooding|action|minor|moderate|major|…`) and `noaa-coops-tides` (NOAA tide gauges,
`floodCategory` `none|action|minor|moderate|major`, from the station's NOS flood thresholds converted to meters above
MLLW).

**1. Scope the sources to your place.** Take the box from your place (a box a little larger than the area you care
about; `nwps-gauges` accepts up to 10° × 10°, `usgs-water` 5° × 5°) and pick the tide stations near you from
`tidesandcurrents.noaa.gov` (seven-digit ids; Great Lakes stations are not supported):

```json
PUT /v1/sources/nwps-gauges
{"enabled": true, "params": {"bbox": "-98.2,29.9,-97.3,30.7"}}

PUT /v1/sources/noaa-coops-tides
{"enabled": true, "params": {"stations": ["8771450", "8771341"]}}
```

`nwps-gauges` reads each gauge's flood stages once a day (at most 10 gauges per run, so a large box has all its
thresholds after a few hours); `maxGauges` (at most 300) caps the cost. `noaa-coops-tides` polls every 6 minutes,
one request per station.

**2. A river flood rule.** One alert when a gauge near home enters minor flood or worse, resolved when it falls back:

```json
POST /v1/triggers
{"name": "River flooding near home", "sources": ["nwps-gauges"], "mode": "enter", "autoResolve": true,
 "near": {"placeIds": ["home"], "radiusKm": 30},
 "conditions": [{"path": "floodCategory", "op": "in", "value": ["minor", "moderate", "major"]}],
 "messageTemplate": "{{payload.name}}: {{payload.floodCategory}} flooding, stage {{payload.stage}} {{payload.stageUnit}}",
 "severity": "warning"}
```

For an earlier warning, watch the forecast (`forecastCategory`, present only while NWPS has a current forecast) or
the margin to the minor flood stage: `{"path": "stageToMinor", "op": "<", "value": 1}` (feet, negative once in flood).

**3. A coastal flood rule.** The same shape on the tide gauges:

```json
POST /v1/triggers
{"name": "Coastal flooding", "sources": ["noaa-coops-tides"], "mode": "enter", "autoResolve": true,
 "conditions": [{"path": "floodCategory", "op": "in", "value": ["minor", "moderate", "major"]}],
 "messageTemplate": "{{payload.name}}: water {{payload.waterLevelM}} m above MLLW ({{payload.floodCategory}})",
 "severity": "warning"}
```

Each record also carries the next predicted high and low water (`nextHighTime`, `nextHighM`, `nextLowTime`,
`nextLowM`) and the station's thresholds (`minorFloodM`, `moderateFloodM`, `majorFloodM`) in meters above MLLW.

**Other sources.** `usgs-water` gives raw USGS discharge and gage height (no flood category: combine it with
`nwps-gauges`, whose `usgsId` names the co-located USGS site); `snotel` gives snow water equivalent for melt-driven
floods; `usdm` gives the weekly drought map; `ea-flood-warnings` (England) and `eccc-hydrometric` (Canada) are the
optional equivalents outside the US. The USDM map requires its attribution wherever it is shown; Foresignal carries it
in the source descriptor.

### Energy and markets series

The energy and markets sources store time series as records without geometry: one `series-point` per period
(`externalId = {seriesId}:{period}`) and one `series-latest` per series, so a rule fires on a single entity. Browse
them with `GET /v1/series` and read one with `GET /v1/series/{sourceId}/{seriesId}` (JSON, or `?format=csv`), or the
`get_series` MCP tool.

- **`eia-petroleum`** (EIA spot prices and weekly stocks) needs a free EIA key from
  <https://www.eia.gov/opendata/>. The key is sent as the `api_key` query parameter and is redacted in every trace.
  Series ids are `eia:RWTC` (WTI Cushing), `eia:RBRTE` (Brent) and `eia:WCESTUS1` (weekly crude stocks). The spot
  prices are sourced by EIA from Refinitiv (LSEG), so the source is `LOCAL_ONLY`: its raw values reach this
  instance's own clients and notifications, never sinks, federation, share links or exports.
- **`eia-930`** (hourly US grid demand, net generation, interchange and fuel mix) uses the same key. Series are
  `eia930:{respondent}:{D|DF|NG|TI}` and `eia930:{respondent}:fuel:{fuel}`; the demand forecast (`DF`) carries
  `status=forecast`.
- **`gie-agsi`** (daily EU gas storage) needs a free key from <https://agsi.gie.eu/> (the `x-key` header). A missing
  or invalid key answers HTTP 200 with an `error` body, which shows as a rejected credential. Series are `agsi:{area}`
  (for example `agsi:DE`), value = `full` (%).
- **`ember-electricity`** (monthly generation, carbon intensity and demand) needs a free key from
  <https://ember-energy.org/>. Series are `ember:{entity}:{series}`.
- **`gb-carbon-intensity`** needs no key. Series are `carbon:gb:national`, `carbon:gb:region:{regionid}` and
  `genmix:gb`.
- **`grid-frequency-gb`** needs no key and watches Great Britain's 15-second system frequency; the companion
  `grid-frequency-fingrid` needs a Fingrid key and is **disabled by default**.
- **`ercot-public`** is **disabled by default** and needs a registered ERCOT account. Register personally at
  <https://apiexplorer.ercot.com> (ERCOT's terms forbid bot registration), subscribe to the Public API and copy the
  subscription key. Set the `username`, `password` and `subscriptionKey` credentials; Foresignal exchanges the
  username and password for a one-hour B2C token (cached in memory, never persisted). The Public API is only
  reachable from inside the United States: a 403 shows `geo-blocked` in the source's health. The keyless `eia-930`
  respondent `ERCO` covers ERCOT system load without an account.

**WTI above a threshold.** The `series-latest` record carries the newest non-forecast point, so one rule watches one
series:

```json
POST /v1/triggers
{"name": "WTI above 90", "sources": ["eia-petroleum"], "mode": "enter", "autoResolve": true,
 "conditions": [{"path": "value", "op": "gt", "value": 90}],
 "messageTemplate": "{{payload.seriesName}}: {{payload.value}} {{payload.unit}} ({{payload.period}})",
 "severity": "warning"}
```

**A statutory grid-frequency excursion** (`match` with `onlyNew`, so it fires once when the excursion opens and not
again on every revision):

```json
POST /v1/triggers
{"name": "GB frequency excursion", "sources": ["grid-frequency-gb"], "kinds": ["frequency-excursion"],
 "mode": "match", "onlyNew": true,
 "conditions": [{"path": "severity", "op": "in", "value": ["statutory", "severe"]}],
 "messageTemplate": "GB frequency {{payload.direction}} to {{payload.extremeHz}} Hz ({{payload.severity}})",
 "severity": "critical"}
```

The `grid-frequency` `{grid}:state` record (`normal`, `low`, `high`, `no-data`) is a convenient edge entity, but
remember that `no-data` means the feed is stale, never an all-clear.

### History tiers

Each source's descriptor fixes its `historyTier`:

- **STANDARD** (discrete records: earthquakes, alerts, filings). Retention is per source and per
  kind; see [Retention](#retention) for the full resolution order. Set or clear a source's own
  window with `"retention": "365d"` or `"retention": null`.
- **TRACK** (moving things: aircraft, vessels). Every TRACK source shares one window,
  `retention.trackWindow` (3 days by default). A per-source `retention` is refused with
  `400 RETENTION_TIER_FIXED`; change `retention.trackWindow` instead.

`settings.effectiveRetention` shows the window in force for either tier.

**Why TRACK refuses a per-source window.** TRACK history lives in partitions shared by every TRACK
source and is removed by dropping whole partitions, never row by row: that is what keeps millions of
positions a day cheap. A partition can only be dropped when every source in it agrees, so there is
exactly one window for all of them. STANDARD history is deleted row by row and can therefore follow
each source's and each kind's own window.

### Ingest policies

The ingest policy decides which observations become stored versions. Everything else updates live
state only. The descriptor has a default (`descriptor.defaultIngestPolicy`). Your override is
`settings.ingestPolicy`, and `settings.effectiveIngestPolicy` is the one in force.

| Mode | Stores |
|---|---|
| `ALL` | Every change, plus a keyframe |
| `SAMPLED` | Significant changes, trigger edges and keyframes. Also a sample every `minInterval` when the position deviates by `minDeviationMeters` or a threshold moves (`thresholds: true`) |
| `LATEST` | Significant changes, trigger edges and keyframes only |

Override a policy, for example to sample aircraft less often:

```json
PUT /v1/sources/adsb-area
{"ingestPolicy": {"mode": "SAMPLED", "minInterval": "60s", "keyframe": "240s",
                  "minDeviationMeters": 1000, "thresholds": true, "edgeMinInterval": "30s"}}
```

Send `"ingestPolicy": null` to go back to the descriptor default. The rules (Core-Data-Model §5.1):

- `2s ≤ minInterval ≤ keyframe`, and `edgeMinInterval ≤ minInterval` (default `minInterval / 2`).
- With a record TTL: `keyframe ≤ ttl − pollInterval − 30s`.
- `"keyframe": "none"` is allowed only when the source has no TTL.
- `SAMPLED` and `LATEST` need a source that declares `significantFields`.

A policy that breaks a rule returns `400 INGEST_POLICY_INVALID`. Every violation is listed in
`errors[]` with its field, for example `ingestPolicy.keyframe`.

**Keyframes.** A keyframe is a version stored even though nothing significant changed, so history
always has a recent full picture of every entity: `keyframe` sets how often (for a source with a
TTL the default is `ttl − pollInterval − 30s`; `none` without a TTL). Separately, the **retention
keyframe** stores a fresh version of any entity still being observed once a quarter of its retention
window has passed since its last stored version, so retention can never delete the only version of
something that is still current. It adds well under 1% on STANDARD sources.

**What is not stored still counts.** An observation that is not persisted still updates live state
(`record_live`) and the `live` and `touch` stream events. An identical re-observation is a *touch*:
it moves `observedAt` and `lastReceivedAt` without a version, at most once per `touchMin` (one
hour without a TTL, else a tenth of the TTL between 30 s and 1 h). The fetch row's `touches`,
`versionsWritten` and per-reason counts show exactly what each fetch did.

**Defaults** (Core-Data-Model §5.3): aircraft, vessels and transit are `SAMPLED` on the TRACK tier
with family-specific intervals, deviations and thresholds; everything else is `ALL` on STANDARD.

### The fetch log

`GET /v1/sources/{id}/fetches` lists one row per fetch, stream flush or push batch, newest first.
Each row has its trigger and status, HTTP status, redacted URL, ETag, body hash, license, per-reason
counts and the seq range it wrote. Request and response bodies are never kept.

- Page with `limit` (up to 1000) and pass each page's `nextAfter` as `after`.
- Filter with `status` (`OK`, `NOT_MODIFIED`, `FAILED`, `REFUSED`, `DISCARDED`) and `trigger`
  (`SCHEDULED`, `MANUAL`, `STREAM`, `PUSH`).
- A row's `id` is the `batchId` on the versions it wrote.

### Reading `storage` and `budgetHoursRemaining`

Source health includes `storage`, which shows what the source costs:

- `observationsPerMinute`, `versionsPerMinute`: what arrives and what is stored, over the last hour.
- `persistRatio`: versions ÷ observations. A low ratio on a busy feed means the policy is doing its
  job. If it is near 1 on a position feed, consider `SAMPLED`.
- `triggerEdgeShare`: share of versions forced by trigger edges. Above 0.2, a trigger rule is
  probably flapping at a threshold; raise `edgeMinInterval` or adjust the rule.
- `bytesPerDay` (7-day average) and `projectedBytes` (`bytesPerDay` × retention window): the disk
  this source will hold at steady state.
- `lateLastHour`, `clampedLastHour`: observations that arrived out of order, and ones with
  timestamps in the future.

`budgetHoursRemaining` is how many hours today's request budget lasts at the last hour's rate. It is
`null` when nothing was requested in the last hour. If it is low early in the UTC day, lengthen the
`interval` or raise `budget.perDay`.

## Egress routing (R2-018)

By default every source request leaves from this host's own address (rotation `direct`). To spread
collection over several exits — for privacy, failover, region-locked sources, or so no supplier sees
everything come from one address — build an **egress pool** and turn rotation on. The full design is in
[Network-Sourcing.md](Network-Sourcing.md); in short:

- **Add routes** with `POST /v1/egress` (permission `network.manage`): an `http` or `socks5` proxy
  (`url` = `scheme://host:port`, credentials in `secrets.username`/`secrets.password`, which are sealed
  with the instance key and never shown again), or an `interface` route binding a local interface or
  address. A `wireguard` route records its profile but is always unavailable: bring the tunnel up on the
  host and add its interface instead. SOCKS5 routes and authenticated HTTP routes carry **https only**.
- **Check each route** with `POST /v1/egress/{id}/test`, which reports the public exit IP it presents
  (from `foresignal.egress.probe-url`, default `https://checkip.amazonaws.com/`).
- **Turn rotation on** with `PUT /v1/settings/egress`: `round-robin`, `random`, `per-source-sticky` or
  `rotate-on-rate-limit`. Once it is on, a source with no usable route **fails its pull rather than going
  direct**.
- **Pin or exclude sources** with `PUT /v1/sources/{sourceId}/egress`: `routeId` pins a source to one
  exit (it waits or fails rather than moving); `excludeFromRotation` keeps it on `defaultRoute`, or
  direct when there is none.
- **Watch health** at `GET /v1/egress/{id}/health`. Three connection failures in a row mark a route
  unreachable, skip it, and raise `system.egress_unhealthy`; `system.egress_recovered` follows when it
  works again. Health is in memory and starts empty after a restart.

Rotation does not get around a provider's limits: each route still paces each host, and a 429 or
`Retry-After` received through a route holds that source off that route until it expires.

## Triggers and alerts

A trigger rule watches the change stream and opens an alert when a stored version matches. Rules live
under `/v1/triggers`; `POST /v1/triggers/{id}/test` backtests a rule against stored history without
alerting.

**A magnitude threshold** that opens one alert when a quake reaches M 6 and closes it on its own when a
revision takes it below:

```json
POST /v1/triggers
{"name": "Big quake", "sources": ["usgs-earthquakes"], "mode": "enter", "autoResolve": true,
 "conditions": [{"path": "mag", "op": ">=", "value": 6}],
 "messageTemplate": "M{{payload.mag}} {{payload.place}}", "severity": "warning"}
```

**A geofence** (GeoJSON Polygon or MultiPolygon in WGS 84; a LineString with `bufferMeters` becomes
a corridor) that alerts when a vessel enters a harbor and again when it leaves:

```json
POST /v1/triggers
{"name": "Harbor entry", "sources": ["aisstream"], "mode": "enter", "autoResolve": true,
 "geofence": {"type": "Polygon", "coordinates": [[[-97.40, 27.80], [-97.30, 27.80],
              [-97.30, 27.86], [-97.40, 27.86], [-97.40, 27.80]]]},
 "hysteresisMeters": 200, "cooldown": "30m"}
```

- **Conditions** are ANDed; operators include `>`, `>=`, `<`, `<=`, `==`, `!=`, `in`, `notIn`,
  `exists`, `matches` (a safe regex subset) and length tests. Paths are payload fields; volatile
  fields (such as USGS `updated`) are refused, because they never cause a version.
- **Mode:** `match` alerts on every matching version; `enter` when a version matches and the
  previous one did not (a new entity counts as entering); `exit` the reverse. With
  `autoResolve: true` an `enter` alert resolves on the exit edge, on a tombstone, or when the
  entity's live row expires. `hysteresisMeters` keeps a position jittering on a fence edge from
  flapping.
- **Cooldown** (`cooldown`, per entity or per rule with `cooldownScope: "rule"`) is measured on the
  observation time, so a burst of late data cannot fire a rule twice. `maxAlertsPerHour` caps a
  storm (default 120); `activeWindow` limits a rule to certain hours.
- **Templates:** `{{payload.<field>}}`, `{{title}}`, `{{source}}`, `{{externalId}}`,
  `{{observedAt}}` and the other placeholders the spec lists, plus the region placeholders of
  [Reference data and region tags](#reference-data-and-region-tags) (`{{near.text}}` …) and the weather
  placeholders of [Alert weather context](#alert-weather-context) (`{{context.weather.summary}}` …).
- **Weather context:** `weatherContext` (`auto`, the default, or `off`) decides whether the rule's geolocated
  alerts get `context.weather` (prompt 056).
- **Region presets:** `"geofenceRef": {"country": ["UA"]}` (or `admin1: ["US-TX"]`,
  `county: ["48453"]`) instead of a polygon. It needs the reference data loaded (otherwise
  `409 REFERENCE_DATA_MISSING`) and cannot be combined with `geofence`, `near` or `hysteresisMeters`.

**The alert lifecycle.** An alert is `firing`, then optionally `acknowledged`, then `resolved`:

| Call | Effect |
|---|---|
| `GET /v1/alerts?state=firing` | List, newest first (filters for rule, source, kind, severity, tag) |
| `POST /v1/alerts/{id}/ack` | Acknowledge (optional `note` and `feedback`) |
| `POST /v1/alerts/{id}/resolve` | Resolve by hand |
| `POST /v1/alerts/{id}/snooze` `{"for": "8h"}` | Suppress repeats of this alert for a while |
| `PUT /v1/alerts/{id}/feedback` | `useful`, `noise`, `late` or `false_positive`, for tuning |
| `POST /v1/triggers/{id}/snooze` | Snooze the whole rule |

`GET /v1/alerts/stream` streams `alert` (fired), `alert.updated` and `alert.resolved` events, each
with the alert's `rev` as its id, so a client resumes with `Last-Event-ID`. Every lifecycle change
also goes to the notification channels below.

### Places

A place is a named area used everywhere a "where" is needed: rules, saved views, briefings, Ask, the
`place=` filter of the read endpoints, and the weather area of interest.

```json
POST /v1/places
{"id": "home", "name": "Home", "shape": "point", "geometry": {"type": "Point", "coordinates": [-97.74, 30.27]},
 "radiusKm": 25, "timezone": "America/Chicago"}
```

- **Shapes:** a `point` with `radiusKm` (0.1–500, default 20), a `polygon` (fixed automatically; a ring
  that crosses the antimeridian is split), or a `route` (a LineString) with `bufferM` (10–50,000). The
  place's `area` is derived on every save (at most 10,000 vertices) and its `version` goes up.
- **Rules:** `"near": {"placeIds": ["home"], "radiusKm": 10}` fires inside the places' areas, grown by
  `radiusKm`. Editing a place moves its rules at once; a record that does not move never fires because
  of the edit, and one that moves afterwards is judged against the new area on its next observation.
  Alerts record `context.near` (place ids and versions).
- **Deleting** a place that rules, views or briefings use answers `409 PLACE_IN_USE` with the list;
  `?force=true` deletes it and disables those rules (their `lastError` says why). Views and briefings
  that still name it match nothing there. Exit rules on place-weather records can still fire when
  those records are tombstoned.
- **Privacy:** a home location is sensitive. Reading places always needs a token, even with
  `openReads`; `place=` filters do too. Notifications and the LLM get place names and rounded
  distances (`{{place.name}}`, `{{place.distanceKm}}`), never coordinates, and MCP returns coordinates
  only to agent tokens that ask for `detail=long`.
- **Weather AOI:** `GET /v1/places/aoi` is the union of the places, each grown to at least 150 km
  (`foresignal.weather.aoi-buffer-km`). With no places, weather sources use
  `foresignal.weather.default-aoi` (a Texas box) and report a health warning until a place exists.

### Saved views

A saved view is a named filter that the records API, map tiles and the stream render identically:

```json
POST /v1/views
{"slug": "quakes-near-home", "name": "Quakes near home", "kinds": ["earthquake"], "placeIds": ["home"],
 "conditions": [{"path": "mag", "op": ">=", "value": 2.5}], "fields": ["title", "observedAt", "payload.mag"]}
```

`GET /v1/views/quakes-near-home/records`, `/tiles.json` (its tile URLs carry the view's version) and
`/stream`; or `view=quakes-near-home` on `/v1/records`, `/v1/changes`, the streams and `/v1/search`,
combined with any other filter. A view that uses a place or a watchlist needs a token to render.

### Watchlists

A watchlist is a list of identifiers that rules and views match with
`{"op": "inWatchlist", "value": "<watchlist id>"}`: every source that declares the identity takes part.

```text
POST /v1/watchlists                       {"name": "Tails", "identity": "icao24"}
POST /v1/watchlists/{id}/import?dryRun=true   (Content-Type: text/csv)
value,label,notes,tags
a1b2c3,N123AB,Survey aircraft,survey;local
GET  /v1/watchlists/{id}/export
```

- **Identities:** `icao24` (six hex digits), `registration`, `callsign`, `mmsi` (nine digits), `imo`
  (seven digits with a valid check digit) and `custom` (rules must then give a `path`). Values are
  normalized (upper case for aircraft identifiers, digits only for MMSI and IMO) and validated; an
  import reports invalid rows and changes nothing with `dryRun=true`. `mode=replace` also removes what
  the file does not list. At most 100,000 items per list and 5 MB per import.
- **Identifier quality:** prefer ICAO 24-bit addresses for aircraft and IMO numbers for ships.
  Registrations move between airframes; MMSIs are reused, mistyped and spoofed, so an MMSI match is a
  lead, not proof.
- **PIA and LADD:** aircraft in the FAA's Privacy ICAO Address program fly under temporary addresses,
  and LADD-listed aircraft are blocked from many feeds; a watchlist cannot and should not get around
  either. Respect the operators' choice.
- **Policy:** watchlists are for aircraft, vessels and assets of public or professional interest. Do
  not use Foresignal to track private individuals.
- **Effect:** list edits take effect on each entity's next observation; nothing fires for the past.
  Alerts of watchlist rules are sensitive: anonymous readers and feed keys never see them.

### The entity registry and privacy (prompt 098)

The entity registry (`/v1/entities`) resolves records to one canonical object per aircraft, vessel,
satellite or station, from deterministic keys only (`icao24`, `registration`, `airframe`, `mmsi`,
`imo`, `norad`, `cospar`, `callsign`); there is no fuzzy name matching and no automatic merge of two
anchored entities. It is about vehicles and objects, never people or organizations.

- **No owner PII:** the FAA registry adapter stores no owner name or address; the aircraft entity's
  attributes carry only manufacturer, model, type and the like.
- **Privacy ICAO Addresses:** PIA hexes are absent from the registry and are never resolved to a
  registration; a leading `~` (readsb TIS-B) is rejected as a key.
- **Private sources:** records from private sources create no entity or link unless
  `foresignal.entity.includePrivate` is on, and links from private sources are visible only to admin
  tokens.

### "Near me" rules

A rule can follow a moving record, for example a phone that reports its position through a custom
push source (keep that source private):

```json
POST /v1/triggers
{"name": "Quake near me", "kinds": ["earthquake"], "mode": "enter",
 "conditions": [{"path": "mag", "op": ">=", "value": 5}],
 "near": {"anchor": {"source": "my-phone", "externalId": "pixel"}, "radiusKm": 150, "maxAnchorAge": "PT1H"}}
```

It fires when a quake appears near the phone's current position and when the phone moves near a quake
that is still current (re-checked when the phone has moved `minMoveMeters`, default 200 m).
`radiusKm: 0` means "the phone is inside the record's polygon", for warning areas. While the phone has
not reported for `maxAnchorAge` (default 30 minutes), or its record is gone, the rule is paused, its
`lastError` starts with `ANCHOR_STALE`, and one system notification says so; it resumes on the next
position. Templates get `{{anchor.distanceKm}}`; the phone's coordinates never leave the server.

### Settings export with places and watchlists

`GET /v1/settings/export?includePlaces=true&includeWatchlists=true` adds places, saved views and
watchlists with their items (both default false); the bundle then carries a `warnings` list, because
places reveal locations. `PUT /v1/settings/import` restores them before the rules that use them.

## Windowed rules and anomalies (prompt 100)

Besides the single-version rules above, a trigger rule has a `type`: `record` (the default engine),
`count`, `absence` or `anomaly`. Windowed rules reason over a time window rather than a version edge.
They reuse every 017 filter (sources, kinds, conditions, geofence, `near`, active window, tags,
severity, `max_alerts_per_hour`) and reject `onlyNew`, `mode` and `reappearAsEnter` (400
`WINDOW_RULE_INVALID`). All four types alert and resolve through the same 017 lifecycle, and their
alerts reference the `rule-windows` record version.

```json
{"name": "Many military aircraft near home", "type": "count",
 "sources": ["adsblol-mil"], "near": {"placeIds": ["home"], "radiusKm": 100},
 "window": {"window": "30m", "op": ">=", "threshold": 5}, "severity": "warning"}
```

```json
{"name": "NWS quiet too long", "type": "absence", "sources": ["nws-alerts"],
 "window": {"window": "2h", "coverageMin": 0.8}, "severity": "warning"}
```

```json
{"name": "Unusual vessel count in the strait", "type": "anomaly", "sources": ["aisstream"],
 "window": {"window": "1h", "groupBy": "h3:5", "direction": "up", "sensitivity": "medium",
            "minWeeks": 8, "coverageMin": 0.8}, "severity": "warning"}
```

- **Window.** `window` is the length (1 minute to 7 days); `step` is how often it is evaluated
  (default `min(window/4, 5m)`, minimum 30 s). `distinctBy` is `entity` (the 098 canonical entity
  when the record is linked, else the descriptor `entityKeys`, else `source+externalId`),
  `externalId`, or a payload path. Versions are never counted. `groupBy` is null, `source`, `place`,
  `h3:<res 3..8>`, `admin1`, `country` or a payload path. `GET /v1/triggers/{id}/window-state`
  returns the per-group state.
- **Identity limits.** One aircraft seen by two sources counts once only when 098 links them (fuse
  with `derived-aircraft` if you want that). Cluster identity ("one storm cell") is out of scope,
  which is why storm rules stay version-based.
- **Absence and the coverage gate.** An absence rule alerts only when every involved source was
  enabled and covered for the whole window. When it was not, the state is `unknown` with a reason and
  a single `system.window_rule_unknown` notification is raised; it never alerts. Receiver-down
  detection belongs to health and station events, not to absence rules. Warm-up: nothing is evaluated
  until the rule and the source have existed for one full window.
- **Anomalies.** An anomaly rule learns a baseline per slot (hour-of-week by default, or hour-of-day)
  over 8–12 weeks of coverage-ok samples, excluding flagged samples and winsorized at p99. A bucket
  fires when its tail p-value is below the sensitivity α (low 1e-4, medium 1e-3, high 1e-2) **and**
  the difference from the expected count is at least `minAbsoluteDelta`. Until all slots have
  `minWeeks` coverage-ok samples the state is `learning`; hour-of-day mode needs 14 days and marks
  results `lowConfidence`. A count drop while the coverage ratio fell at least 30% against the slot
  median is `coverage-drop` and never alerts. `GET /v1/triggers/{id}/baseline` shows the learned
  values, and `POST /v1/triggers/{id}/baseline/rebuild` recomputes them (AGENT).
- **Reading anomalies honestly.** `GET /v1/anomalies` lists the evaluated buckets. The p-value
  assumes the stated Poisson or negative-binomial model; it is **not** the probability that something
  is happening. Every result carries the detector name, version and parameters, the baseline summary,
  the watermark and evidence references, so it is reproducible from stored inputs. The four
  principles: detection is deterministic, results are reproducible, a p-value is not a probability,
  and absence of evidence is not evidence of absence.
- **MCP.** `list_anomalies` and `get_window_state` (read), and `watch_for_unusual_activity` (agent),
  which creates an anomaly rule and states the warm-up period.

## Intelligence events (prompt 101)

An **intelligence event** is a typed, located, time-bounded finding: `maritime-disruption`,
`aviation-anomaly`, `infrastructure-disruption`, `weather-impact`, `seismic` or `custom`. It is built
by a deterministic **correlation recipe** from pinned record versions, or created by an operator or
agent, and carries supporting and contradicting evidence, a confidence score, an evidence quality and
the detector version and parameters needed to reproduce it.

- **Four principles.** Detection is deterministic (an LLM may explain evidence but never adds members,
  raises confidence or changes the type); every result is reproducible (members pin
  `source/externalId@seq/version`, and `detector {name, version, params}` is stored); confidence is a
  score, **not** a calibrated probability (it becomes calibrated only when 103 validation sets
  `calibrated=true`, and every response carries `confidenceMethod`); and results are coverage-aware
  (missing data lowers evidence quality, it never counts as contradiction).
- **Evidence, inference, speculation.** `GET /v1/intel/events/{id}` separates `evidence` (members:
  records, alerts, anomalies), `inference` (type, severity, confidence, geometry, times) and
  `speculation` (alternatives and any LLM narrative, always labeled).
- **Confidence.** `c = 1 − Π_f (1 − w_f · r_f)` over distinct signal families, where `r_f` is the best
  member's source reliability (0.6 until 109 grades exist), then `×(1 − 0.25)` per active
  contradiction, capped at 0.95. Labels: low < 0.4 ≤ moderate < 0.7 ≤ high. Evidence quality: strong
  (≥ 3 families from ≥ 2 independent sources), moderate (2 families), weak (1 family), insufficient
  (members only from unhealthy sources).
- **Recipes.** Built-ins load from `src/main/resources/intel/recipes/*.json` and refresh on start
  when the bundled version is newer; the operator's `enabled` flag and parameter overrides are kept.
  A recipe whose kinds are not installed loads disabled with a warning. `PUT /v1/intel/recipes/{id}`
  enables/disables and sets overrides; `POST /v1/intel/recipes/{id}/test` dry-runs it over a seq
  range.
- **Dedup.** A candidate merges into an open event of the same type when their geometries intersect
  after buffering by the recipe's radius and their times overlap, or when they share a member. Two
  open events merge via `POST /v1/intel/events/{id}/merge {intoId}`: the older id survives, members
  move, and the merged event is tombstoned. Never across types.
- **Lifecycle.** `emerging` → `active` when the activate threshold is met; `resolved` after the quiet
  period with no new evidence; `reopened` within the reopen window; `dismissed` only by an operator
  or agent.
- **Alerts from events.** Every event is published as a record of the DERIVED source
  `foresignal-intel`, kind `intelligence-event`, so an ordinary 017 rule fires on it:

  ```json
  {"name": "Critical intelligence event", "sources": ["foresignal-intel"],
   "kinds": ["intelligence-event"], "conditions": [{"path": "severity", "op": "==", "value": "critical"}],
   "mode": "enter", "severity": "critical"}
  ```

- **Investigations.** Case files (`INV-{yyyy}-{nnnn}`) with items that keep a snapshot taken when
  added, so the evidence survives retention; `GET /v1/investigations/{id}/export?format=json|md|geojson`
  exports it, each format carrying the "Automated analysis inputs; verify before acting" header and
  license attributions. Since prompt 130 investigations have an owner, an access list and a TLP label, which the
  export prints at the top (see [People, sharing and TLP](#people-sharing-and-tlp-prompt-130)).
- **Exposure.** `POST /v1/exposure {geometry | placeId | eventId}` counts reference facilities,
  places, live aircraft and vessels and, with a population pack, an estimate. Every section carries a
  `status`; the population section is `not-installed` until a pack is imported.
- **Population pack (optional).** Download a GHSL GHS-POP GeoTIFF (EPSG:4326, 30 arcsec; © European
  Union, GHSL; CC-BY-4.0) into `${FORESIGNAL_DATA_DIR}/packs`, then
  `POST /v1/packs/population/import {"path": "packs/ghsl/GHS_POP_E2025_..._4326_30ss", "epoch": 2025}`.
  Each pixel's population is aggregated to the H3 cell (res 5 and 7) containing its center; a query
  polyfills the area at res 7 (res 5 over 50,000 km²) and sums. The estimate is a **modeled
  residential population, not a casualty or impact estimate**.

## Autonomous analyst (prompt 102)

The analyst is a **scheduled LLM reviewer**. On each run it looks at what is new — intelligence
events (101), anomalies (100) and alerts (017) — compares them with history through read-only tools,
and writes **analyst notes** with typed, cited claims. It may also **propose** watches, intelligence
events, investigation items or event annotations. It explains evidence that deterministic detectors
already produced; it never manufactures evidence.

- **With no LLM configured the feature is inert.** Enabling a profile without a chat provider is 409
  `LLM_NOT_CONFIGURED`, and every non-LLM feature is unaffected.
- **Cost and egress.** Each run spends the configured provider's budget (026). A cloud provider is an
  egress path; a note citing anything private is itself private and is excluded from non-private
  egress and from cloud providers unless the operator allows it. Use a **local (Ollama)** provider to
  keep everything on-host.
- **Profiles.** `POST /v1/analyst/profiles` takes a `schedule` (`cron` + `timezone`, or `every` ≥ 10
  minutes; default every 30 minutes), a `scope` (sources, kinds, places, bbox, event types, rule tags,
  `minSeverity`), a `lookback`, `instructions` (≤ 4,000 chars), a model `providerId` (null = the chat
  role) and `effort` (`low`/`medium`), budgets (`maxRunsPerDay`, `maxTokensPerRun`, `maxToolCalls`,
  `maxWallSeconds`) and delivery (`channelIds`, `notifyMinPriority`).
- **`allowActions` risks.** Proposals are only *proposed* by default. Executing one requires an
  operator confirm, or `allowActions=true` and the exact type in `allowedActions`. **Even then**
  auto-execution re-checks that the owner token is still unrevoked, unexpired and holds `agent`
  scope, and that the daily `maxAutoActionsPerDay` cap is not reached. A restricted token cannot own
  an action-enabled profile. The analyst can never delete or disable anything. Watches are created
  enabled and tagged `analyst`; events carry `origin=analyst`; investigation items go only into open
  investigations; `annotate_event` only appends labeled speculation and never touches members or
  confidence.
- **Reading claims.** Every note body has claims typed `evidence`, `inference` or `speculation`.
  Evidence claims cite a tool result from that run; an uncited evidence claim is downgraded to
  speculation with a warning. The model's own confidence numbers are discarded — a proposed event's
  confidence is the 101 `EventScorer` over its members. An absence claim needs a coverage-ok citation.
- **The four principles.** Evidence comes from detectors; claims are typed; no confidence inflation;
  coverage-aware (never assert something "stopped" unless the coverage gate was ok). Tool results are
  delimited and treated as data, never instructions.
- **Evaluation.** `GET /v1/analyst/evaluation?profileId&since` returns per-profile aggregates: runs by
  status, the noop share, tokens, proposal acceptance rate, notes dismissed share, useful/wrong
  ratios. `GET /v1/analyst/runs` lists every run's tokens, tool calls, duration and outcome.

## Backtesting and precursor tests (prompt 103)

Ask "what would this rule or recipe have done?" and "does X tend to precede Y within T?" over retained
history, with answers that state their own limits. A backtest replays what Foresignal **stored**, not
ground truth.

- **Jobs.** `POST /v1/backtests {type: rule|recipe|precursor|quality, targetId | definition, from|fromSeq,
  to|toSeq, variants?}` returns 202 with a job; poll `GET /v1/jobs/{jobId}` (one path for every job
  type) or `GET /v1/backtests`. One queued or running job per token (409 `BACKTEST_IN_PROGRESS`, shared
  by REST and MCP). A range longer than 90 days, empty, inverted or wholly below retention is 400
  `BACKTEST_RANGE_INVALID`; an open-ended `toSeq` is clamped to the published watermark, and a start
  below the retained floor is clamped with the `RANGE_CLAMPED_BY_RETENTION` caveat. Without a range a
  job covers the last day (precursor jobs: the last 30 days). Jobs run on
  `foresignal.backtest.concurrency` worker threads (default 1, at most 2), in 50k-seq chunks with a 60 s
  statement timeout each, reporting `progress` and checking for cancellation between chunks;
  `DELETE /v1/jobs/{jobId}` cancels, and a canceled job keeps the status `cancelled`. Finished jobs are deleted
  after 30 days.
- **Recipe jobs** report the intelligence events the recipe actually formed in the range (the
  correlation engine has no dry-run replay yet), so they show what was stored, not a re-run.
- **Caveats.** Every result carries `NOT_GROUND_TRUTH` plus each applicable caveat: `RANGE_CLAMPED_BY_RETENTION`,
  `DOWNSAMPLED_RANGE`, `SAMPLED_SOURCE`, `COVERAGE_GAPS`, `DEFINITION_DIFFERS`, `ENTITY_RESOLUTION_CHANGED`,
  `WINDOW_APPROXIMATE`, `SMALL_SAMPLE`. **Always quote them when reporting a result.**
- **No look-ahead.** Anomaly rules are replayed bucket by bucket from the stored counts, and a baseline
  trains only on buckets before the evaluated bucket. Forecast-horizon rules are matched as the live
  engine saw each version at its own ingest time, never at the replay's wall clock. Count and absence
  rules are replayed at record level only (`WINDOW_APPROXIMATE`).
- **Reading lift and p-values.** Hit rates, lifts and p-values are descriptive statistics with stated
  assumptions; they are never calibrated probabilities. Small samples are labeled `insufficient`.
- **Precursor tests.** `POST /v1/precursor-tests` defines X and Y (an alert rule, an intelligence event
  type or an anomaly rule), a lag window, a spatial rule and a dedup window; `POST
  /v1/precursor-tests/{id}/test` submits a job. The verdict is `insufficient`, `no-evidence-of-precedence`,
  `weak` or `notable`. The output reports the reverse test (flagging "co-occurrence, not precedence"), a
  deterministic circular-shift base rate and permutation p-value, and how many precursor tests this
  instance ran in the last 30 days with a Bonferroni-adjusted p-value. **Association in stored data; not
  causation.**
- **Alert quality.** `GET /v1/triggers/{ruleId}/quality?since` reports precision with a Wilson interval
  (null with fewer than five labeled alerts), median time-to-ack and time-to-resolve, flapping,
  duplicates and the severity distribution. The 017 feedback enum maps to classes as: `useful` →
  positive; `noise` and `false_positive` → negative; `late` → neutral.
- **Tuning suggestions** (`GET /v1/triggers/{ruleId}/suggestions?since&simulate`) are deterministic and
  **never applied automatically**. Each carries a rationale, a ready `TriggerRuleInput` partial patch, an
  expected effect and caveats; applying one is an ordinary rule update by an agent. With `simulate=true`
  (agent only) a backtest job measures the patched variant over the same range.

## Infrastructure graph (prompt 104)

A facility graph of power, telecom, transport, health, water and space infrastructure, built from the
094 reference datasets and, optionally, your own OpenStreetMap extract. It answers "what depends on
things inside this outage or event area?" with paths and explicit uncertainty. All of it is open data,
processed locally.

- **The OSM extract.** Download a Geofabrik extract for your region (`https://download.geofabrik.de/`,
  refresh monthly), then process it locally with `osmium` (an external tool; never linked or bundled):
  `osmium tags-filter region-latest.osm.pbf nwr/power=plant,substation,generator nw/power=line,cable
  nwr/amenity=hospital nwr/healthcare=hospital nwr/telecom=data_center,exchange nwr/aeroway=aerodrome
  nwr/landuse=port nwr/harbour=yes nwr/man_made=wastewater_plant,water_works -o infra.osm.pbf` then
  `osmium export infra.osm.pbf -f geojsonseq --add-unique-id=type_id -o infra.geojsonseq`. Put the
  result under `${FORESIGNAL_INBOX_DIR}/osm` and enable `osm-infrastructure`. **Do not use the public
  Overpass API in bulk** — Foresignal never calls it; use an extract. The file is streamed (never
  loaded whole); a file over 2,000,000 features is refused with the health reason "feature cap
  exceeded", and lines that are not valid GeoJSON features show as "parse errors: n".
- **ODbL obligations.** The OSM data is ODbL 1.0; attribution "© OpenStreetMap contributors" appears
  in every response that includes it, and the derived facility graph is a Derived Database — if you
  redistribute it, keep the ODbL and share-alike terms.
- **Edge labels.** Every edge states how it is known — `declared`, `topological`, `proximity` or
  `inferred` — and a label `confirmed | likely | possible | speculative`. **Proximity edges are never
  better than `possible`.** A path's label is its weakest edge. Results always say "derived from open
  data; not authoritative; heuristic links may be wrong." The cable data does not list each cable's
  landing points, so `cable-landing` is a geometry match (`topological`, `likely`). A power line
  reaching one substation is `likely`, reaching two is `confirmed`; impact traversal walks lines in
  both directions. An edge's license is the most restrictive license of its two facilities.
- **API.** `GET /v1/infra/nodes`, `GET /v1/infra/nodes/{id}` (with in/out edges), `GET /v1/infra/edges`,
  `POST /v1/infra/affected {geometry | eventId | record, depth, minLabel, types}`, `GET /v1/infra/status`,
  `POST /v1/infra/rebuild` (admin; 202 at once, the build runs in the background),
  `GET/PUT /v1/settings/infra-rules` (admin; `enabled` and `params` per rule apply to the next build).
  `GET /v1/infra/*` are reads, serve only the newest successful build, and page with `cursor`; a
  query area over 2,000,000 km² needs a `types` filter. Before the first build the graph endpoints
  answer 409 `INFRA_GRAPH_NOT_BUILT`. A record seed that is only a point uses its region polygon and
  is flagged `approximateArea`; a private event needs the private grant.
- **Rebuilds.** Besides `POST /v1/infra/rebuild`, the graph rebuilds 10 minutes after the last import
  of a mapped source and weekly (`foresignal.infra.rebuild-cron`, default Sunday 04:30 UTC;
  `foresignal.infra.auto-rebuild=false` turns both off). A rebuild replaces the previous graph in one
  transaction.

## Notification channels

A channel delivers alerts, briefings and system events to one destination, formatted natively for
it. Manage channels under `/v1/channels` (admin only). `GET /v1/channels/types` lists every type
with its config and secret fields. Secret fields (`secrets`) are write-only: they are encrypted at
rest, never returned (a channel lists only the names of its set secrets in `secretFields`), and a
`PUT` that omits `secrets` keeps the stored ones. `POST /v1/channels/{id}/test` sends a test message
at once and returns the destination's answer without storing anything.

A channel with an `ownerPrincipalId` is a principal's **personal channel** (prompt 130): it is created and replaced
through that principal's notification preferences, takes only that principal's personal notifications (mentions,
shares, replies) and never the instance's alert, briefing or system fan-out. See
[People, sharing and TLP](#people-sharing-and-tlp-prompt-130).

Every message is stored as a notification before it is delivered, even when no channel takes it.
Deliveries are durable: a failure retries after 10 s, 1 min, 5 min, 30 min and 2 h, then fails, and
the failure is announced as a `system` event to your *other* channels. `429` answers honor
`Retry-After`. Each channel has a rate limit sized to its destination. Look at
`GET /v1/channels/{id}/deliveries` to see what happened (`kind` is `send`, `update`, `followup` or
`digest`; `deferredReason` says why one is waiting).

Common fields: `events` (`alert`, `briefing`, `system`, and the opt-in events of later prompts such as
`situation.activated`, `situation.escalated`, `finding.significant`, `forecast.resolved`,
`forecast.resolution_needed`, `hypothesis.band_changed`, `indicator.observed`, `assumption.broken`,
`assessment.published`, `social.divergence` and `social.attention_gap`; default `["alert"]`), `ruleIds` (null for
every rule), `minSeverity`, `titleTemplate` and `bodyTemplate` (the trigger placeholders plus
`{{severity}}`, `{{summary}}`, `{{url}}`, `{{ruleName}}` and `{{message}}`; `{{observedAt|local}}`
prints `2026-05-01 07:00 CDT` in the channel timezone and `{{observedAt|ago}}` prints `3 min ago`),
`waitForEnrichmentSeconds`, `timezone`, `quietHours`, `groupWindowSeconds`, `backlogCap` and
`editInPlace`. The weather placeholders `{{context.weather.summary}}` and `{{context.weather.<section>.<field>}}`
(prompt 056) read the alert's weather context; see [Alert weather context](#alert-weather-context). The region placeholders (`{{near.text}}`, `{{country.name}}` …, prompt 040) read the
alert's region snapshot; `config.units: "imperial"` prints `{{near.text}}` in miles. Links in messages use the `server` setting's `publicBaseUrl` (default
`http://localhost:8080`); set it to the address your phone can reach.

### Setting up each type

| Type | Config | Secrets | Notes |
|---|---|---|---|
| `webhook` | `url`, `method` (POST/PUT), `headers`, `bodyTemplate` | `hmacSecret`, `header.<Name>` | See "Verifying webhook signatures" |
| `slack` | `username`, `iconEmoji` | `webhookUrl` | An incoming webhook from a Slack app |
| `mattermost` | `channel`, `username`, `iconUrl` | `webhookUrl` | An incoming webhook |
| `discord` | `username` | `webhookUrl` | Channel settings → Integrations → Webhooks |
| `teams` | — | `workflowUrl` | **Workflows URLs only** (below) |
| `google-chat` | — | `webhookUrl` | Space → Apps & integrations → Webhooks |
| `telegram` | `chatId` | `botToken` | Create a bot with @BotFather and add it to the chat |
| `matrix` | `homeserverUrl`, `roomId` | `accessToken` | A bot account invited to the room (unencrypted room) |
| `ntfy` | `serverUrl`, `topic`, `call` | `accessToken` | `call` is below |
| `gotify` | `serverUrl` | `appToken` | An application token |
| `pushover` | `device`, `sound` | `appToken`, `userKey` | Critical is an emergency alert, canceled on resolve |
| `email` | `smtpHost`, `smtpPort`, `tls`, `from`, `to`, `plainTextOnly` | `username`, `password` | `plainTextOnly` for Winlink gateways |
| `homeassistant` | `baseUrl`, `mode`, `service`, `iosCritical`, `androidAlarmStream` | `webhookId` or `accessToken` | Below |
| `apprise` | `serverUrl`, `configKey`, `tag` | `urls` | Below |
| `zulip` | `site`, `stream`, `botEmail`, `topicTemplate` | `apiKey` | Below |
| `mqtt` | `brokerUrl`, `topicPrefix`, `qos`, `protocolVersion`, `messageExpiry`, `payloadTemplate`, `publishHealth`, `publishSummary`, `summaryInterval`, `haDiscovery`, `haPrefix`, `haMaxRules`, `haMaxSources`, `haRetainDiscovery` | `username`, `password`, `caCertPem` | [MQTT and Home Assistant](#mqtt-and-home-assistant) |

- **Microsoft Teams:** Office 365 connector webhooks were retired in May 2026, so a
  `*.webhook.office.com` or `outlook.office.com/webhook` URL is refused. In Teams, open the channel's
  *Workflows*, choose "Post to a channel when a webhook request is received", and use the URL it
  gives you.
- **Home Assistant:** `mode: webhook` (default) posts the full Foresignal JSON to
  `/api/webhook/{webhookId}` so an automation can react to it. `mode: notify` calls
  `notify.{service}` (for example `mobile_app_pixel`) with a long-lived access token; critical
  alerts then use the iOS critical sound and the Android alarm stream, which break through
  do-not-disturb. Turn either off with `iosCritical: false` or `androidAlarmStream: false`.
- **ntfy `call`:** set `call` to a phone number (or `yes` for the first verified number) to get a
  voice call for **critical** alerts only. It needs an ntfy account with a verified phone number
  and works only with a server that offers calls (ntfy.sh with a paid plan).
- **Apprise:** point `serverUrl` at an apprise-api server and either name a saved configuration
  (`configKey`) or pass Apprise URLs as the secret `urls`. When some of the configuration's targets
  fail and others succeed, apprise-api answers `424`; Foresignal records the delivery as delivered
  with a warning and **does not retry**, because a retry would notify the successful targets again.
- **Zulip:** create a generic bot (Settings → Bots), subscribe it to the stream, and use its email
  as `botEmail` and its API key as the secret `apiKey`. Each rule posts to its own topic
  (`topicTemplate`, default `{{ruleName}}`), and later updates of an alert land in the same topic.

### Edit in place

When an alert is acknowledged, snoozed or resolved, the original message is edited where the
destination allows it, and a follow-up message is sent everywhere else:

| Edited in place | Follow-up message |
|---|---|
| Discord, Telegram, Matrix | Webhook, Slack, Mattermost, Teams, Google Chat, ntfy, Gotify, Pushover, email, Home Assistant, Apprise, Zulip |

The edited text shows the new state, for example "✅ Resolved 14:02 CDT — …" with the old text struck
through. Set `editInPlace: false` on a channel to always get a follow-up instead. Pushover emergency
alerts are canceled on acknowledge or resolve, so the phone stops repeating them.

### Map attachments

A geolocated alert can carry a static map: the alert's geometry over radar (or other products) on a
Natural Earth basemap, with the frame's valid time, a legend, the attribution and the footer
"Automated — not an official warning" (prompt 055). It is **off by default on every channel**, because
the image leaves your network on a cloud chat service and shows where the alert is. Turn it on per channel
in `config`:

| Key | Default | Meaning |
|---|---|---|
| `attachMap` | `none` | `static` attaches a map to each geolocated alert |
| `mapProducts` | `["mrms-reflectivity"]` | 1–4 products drawn; an alert on a `raster-frame` record defaults to the product that matched |
| `mapRadiusKm` | `50` | radius around the alert geometry (the map always contains the whole geometry) |
| `maxAttachmentBytes` | per type, below | the largest image sent on this channel |

The map is rendered **once per alert** when it fires, at 800 px, after waiting up to 5 s for a frame newer
than the alert, so it shows the weather that caused it. It is stored as media for 7 days (`role: map`) and
reused by every channel. Rendering starts at once and runs alongside the enrichment wait: a send waits for
its map at most 8 s, and never longer than the channel's `waitForEnrichmentSeconds` when that is set; past
that the alert goes out as text. A `satellite-pass` alert attaches the pass composite's 256 px thumbnail
instead of a map.

| Type | How the image is sent | Default cap |
|---|---|---|
| `discord` | multipart `files[0]`, shown in the embed (`attachment://map.png`) | 8 MB |
| `telegram` | `sendPhoto` with the text as caption (≤ 1,024 characters; longer text follows as a message) | 10 MB |
| `ntfy` | `PUT` of the PNG with `Filename: map.png`, the text in the `Message` header | 15 MB |
| `pushover` | multipart `attachment` | 5,242,880 bytes |
| `email` | inline image (`cid:map`) in the HTML part; none with `plainTextOnly` | 10 MB |
| every other type | the text plus a `[Map](…)` link to `{publicBaseUrl}/v1/media/{id}`; `webhook` also lists `attachments[{mediaId, url, contentType, bytes}]` in its default body | — |

**Size control:** an image over the channel's `maxAttachmentBytes` is replaced by a 600 px map, then by
the 256 px thumbnail, then the alert is sent as text only. The level chosen is shown on the delivery
(`attachmentLevel`: `map-800`, `map-600`, `thumbnail` or `text-only`), with the media references in
`attachments`; a retry resends the same image from the media store.

**ntfy attachments** need attachments enabled on the server: set `attachment-cache-dir` (a directory or
an S3 URL) and `base-url` in `server.yml` (ntfy's own option names; `attachment-file-size-limit` caps a
file, 15M by default). ntfy.sh has them on. When the server answers `413` (too large, or the bandwidth
limit) or `400` with code `40014` (attachments not allowed), Foresignal sends the text message instead and
records `text-only`.

**Links on the other types** point at the in-app media URL, which needs a token unless reads are open,
and works only where `publicBaseUrl` is reachable (your LAN or a VPN). There are no signed URLs in v1.
Private-source alerts never carry a map to a channel without `privatePayload: true`, and products of
private sources are left off the map for such a channel.

### Quiet hours, pause, folding and grouping

- **Quiet hours** (per channel): `{"start": "22:00", "end": "07:00", "days": ["MON", …],
  "bypassSeverity": "critical"}` in the channel `timezone` (else the `server` setting's timezone,
  else UTC). `days` are the days a window starts on; leave it out for every day. Messages inside the
  window wait until it ends; if more than one alert waited, they arrive as one digest, newest first.
  Messages at or above `bypassSeverity` are sent anyway (`null`: nothing bypasses).
- **Global pause:** `PUT /v1/settings/notifications` with `{"pausedUntil": "<time>",
  "pauseBypassSeverity": "critical"}` defers everything else on every channel until then, and each
  channel gets one digest when the pause ends. Set `pausedUntil` to `null` to end it early.
- **Backlog folding:** when more than `backlogCap` (default 20) alert messages wait for one channel,
  the oldest are folded into one digest ("+N more alerts"), so a storm cannot flood a phone.
- **Grouping** (opt-in): with `groupWindowSeconds` above 0, the first alert of a rule waits for the
  window and everything the rule fires meanwhile is sent with it as one message.

Lifecycle updates and system events are never folded into a digest.

### Verifying webhook signatures

With `hmacSecret` set, every `webhook` request carries `X-Foresignal-Timestamp` (Unix seconds) and
`X-Foresignal-Signature: sha256=<hex>`, where the hex is HMAC-SHA256 with your secret over the
timestamp, a dot and the raw request body. To verify: recompute it over the bytes you received,
compare in constant time, and reject timestamps more than a few minutes from your clock.
`X-Foresignal-Delivery` is the delivery id (the same on every retry, so you can drop duplicates).

### The origin marker

Every outbound request carries this instance's id in `X-Foresignal-Origin`, and the JSON bodies
Foresignal defines (webhook, Home Assistant, Apprise) carry it as `foresignalOrigin`; ntfy messages
carry the tag `foresignal-origin-<id>` and Gotify messages the extra `foresignal::origin`. When you
loop a channel back into Foresignal (for example ntfy into an ingest endpoint), the ntfy- and Gotify-compatible ingest endpoints (prompt 024) drop
messages carrying its own marker, so a notification can never feed itself.

### MQTT and Home Assistant

An `mqtt` channel publishes alerts, briefings and system events to your broker, keeps retained state
topics (rule states, source health, a live summary) and, with `haDiscovery: true`, makes Foresignal
appear in Home Assistant as a device without any YAML. Foresignal holds one connection per channel and
reconnects on its own (1 s, doubling to 120 s).

```json
{"name": "MQTT broker", "type": "mqtt", "events": ["alert", "briefing", "system"],
 "config": {"brokerUrl": "mqtts://broker.lan:8883", "topicPrefix": "foresignal", "haDiscovery": true},
 "secrets": {"username": "foresignal", "password": "…"}}
```

- `brokerUrl` takes `mqtt://`, `mqtts://`, `ws://` and `wss://`. A LAN broker is fine. A broker on a
  public address without TLS or without a username is saved with the warning
  `MQTT_INSECURE_PUBLIC_BROKER` in the channel's `warnings`; it is not refused.
- For a private CA, put its certificate (PEM) in the secret `caCertPem`. Hostname verification always
  stays on.
- `protocolVersion: "3.1.1"` is for old brokers; message expiry and the content type need MQTT 5.
- `POST /v1/channels/{id}/test` publishes to `{prefix}/test` and reports the PUBACK latency in
  `durationMs`. `POST /v1/channels/{id}/mqtt/republish` sends every retained state and the discovery
  config again.

**Mosquitto.** Give Foresignal its own user, and let it write only its own topics and the discovery
prefix:

```
# mosquitto.conf
listener 8883
cafile /mosquitto/certs/ca.crt
certfile /mosquitto/certs/server.crt
keyfile /mosquitto/certs/server.key
allow_anonymous false
password_file /mosquitto/config/passwd
acl_file /mosquitto/config/acl
```

```
# /mosquitto/config/acl
user foresignal
topic write foresignal/#
topic write homeassistant/#
topic read homeassistant/status

user homeassistant
topic read foresignal/#
topic readwrite homeassistant/#
```

Create the users with `mosquitto_passwd -c /mosquitto/config/passwd foresignal` (and without `-c`
for the next one).

**Topics** (`{prefix}` is `topicPrefix`, default `foresignal`):

| Topic | Retained | Content |
|---|---|---|
| `{prefix}/status` | yes | `online`, or `offline` (Last Will, and on shutdown) |
| `{prefix}/alerts/{severity}` | no, expires after `messageExpiry` | a fired alert: `event_type`, `id`, `ruleId`, `ruleName`, `severity`, `state`, `title`, `shortTitle`, `body`, `url`, `sourceId`, `externalId`, `kind`, `firedAt`, `point` (`[lon, lat]`), `tags`, `near` |
| `{prefix}/alerts/updates` | no | `{id, state, resolvedAt, acknowledgedBy}` on acknowledge, snooze, resolve |
| `{prefix}/briefings/{briefingId}` | yes | the latest run: `runId`, `title`, `at`, `url`, `status`, `markdown` (at most 16 KB) |
| `{prefix}/system/events` | no | system events (a source failing, a delivery that gave up, …) |
| `{prefix}/rules/{ruleId}/state` | yes | `{state: firing\|resolved\|idle, alertId, severity, lastFiredAt}`; cleared when the rule is deleted |
| `{prefix}/sources/{sourceId}/health` | yes | `{status, reason, lastSuccessAt, lastDataAt}`; never for private sources |
| `{prefix}/summary` | yes | the glance (`GET /v1/glance`), when it changes |

Alerts of private sources carry only the rule name, severity and link unless the channel has
`privatePayload: true`. `payloadTemplate` replaces the event payloads with your own JSON (the webhook
`bodyTemplate` placeholders); keep an `event_type` member if Home Assistant should still see alerts.

**Home Assistant.** With the MQTT integration set up and `haDiscovery: true`, a *Foresignal* device
appears with sensors for open alerts per severity, failing and degraded sources and the last briefing,
an event entity per severity (*Critical alert*, *Warning alert*, *Info alert*), and a problem sensor
per enabled rule (at most `haMaxRules`, critical first) and per non-private source (at most
`haMaxSources`). Entities show unavailable while Foresignal is down. Foresignal re-sends discovery when
Home Assistant restarts (its `online` birth message) and 5 s after rules or sources change; the config
is not retained unless you set `haRetainDiscovery: true`. Turning discovery off, or disabling or
deleting the channel, removes the device. Watched aircraft and vessels are not exposed as
`device_tracker` entities: Home Assistant uses GPS trackers only to place them in its zones.

An automation on the critical alert event (the payload's members are the event's attributes):

```yaml
automation:
  - alias: "Foresignal critical alert"
    triggers:
      - trigger: state
        entity_id: event.foresignal_critical_alert
    conditions:
      - condition: template
        value_template: "{{ trigger.from_state.state not in ['unavailable', 'unknown'] }}"
    actions:
      - action: notify.mobile_app_pixel
        data:
          title: "{{ trigger.to_state.attributes.shortTitle }}"
          message: "{{ trigger.to_state.attributes.title }}"
```

Check the entity id under *Settings → Devices → Foresignal*; it follows the device and entity names.

**Node-RED.** An *mqtt in* node on `foresignal/alerts/+` (QoS 1, output "a parsed JSON object") gives
`msg.payload.severity`, `msg.payload.title` and `msg.payload.url`; `foresignal/alerts/updates` tells
you when an alert is acknowledged or resolved, and `foresignal/status` whether Foresignal is up.

**Displays.** Devices that subscribe to MQTT, such as an AWTRIX clock, can show
`{prefix}/summary` (`alerts.open.critical`, `status`) or react to `{prefix}/alerts/critical`
directly; give them a broker user that may only read `foresignal/#`.

### Mesh alert channels

Two channel types put short alert lines on **your own** LoRa mesh: `meshtastic` (through your node's MQTT JSON
downlink) and `meshcore` (through the MeshCore companion protocol over TCP). Both send only on a private, keyed
channel, pass every transmission through an airtime governor, and can take short field reports back
(prompt 084).

```json
{"name": "Mesh alerts", "type": "meshtastic", "events": ["alert"],
 "config": {"brokerUrl": "mqtt://192.168.1.20:1883", "root": "msh/local", "gatewayNodeNum": "2864434397",
            "channelIndex": 1, "channelName": "Family", "privateChannelConfirmed": true,
            "region": "EU_868", "airtimePreset": "meshtastic-longfast", "minSeverity": "warning"},
 "secrets": {"username": "foresignal", "password": "…"}}
```

```json
{"name": "MeshCore alerts", "type": "meshcore", "events": ["alert"],
 "config": {"companionUrl": "tcp://192.168.1.30:5000", "channelIndex": 1, "region": "EU_868",
            "airtimePreset": "custom",
            "airtimeCustom": {"spreadingFactor": 10, "bandwidthKhz": 250, "codingRate": 5, "preambleSymbols": 8}}}
```

**What goes out.** One line per alert: `{sev} {title} {where} {hhmm}Z #{code}`, for example
`!! Quakes Austin 1205Z #0007`. `sev` is ASCII (`!!!` critical, `!!` warning, `i` info; an emoji would cost four
bytes), `where` is the nearest place or a 2-decimal `lat,lon`, the time is UTC, and `#code` is the alert's
4-character short id, which a reply can acknowledge. A resolution is `OK {title} #{code}`; acknowledgements and
snoozes are never sent. Meshtastic lines are at most 200 bytes; MeshCore channel lines at most 133 characters and
127 bytes (the firmware's 160-byte text limit less the `name: ` prefix it adds). `profile: "cbor"` sends the
machine-readable CBOR profile instead (Meshtastic: `~E1 ` and base64url; MeshCore: a channel datagram with
`dataType` 0xFF45, in the 0xFF00–0xFFFE development range; a production deployment should register its own value
through MeshCore's `number_allocations.md`). The channel's own `config.minSeverity` (default `warning`) applies on
top of its top-level `minSeverity`.

**Meshtastic node prerequisites.**

- In the node's MQTT settings: your broker on the LAN, **JSON enabled** (not available on nRF52 nodes),
  **encryption disabled** toward that private broker, and the root topic you set as `root`.
- A channel named `mqtt` with **downlink enabled** (the firmware takes JSON downlinks on `{root}/2/json/mqtt/`).
  Some firmware wants the gateway id on the topic: set `topicSuffixNodeId: true`.
- `gatewayNodeNum` is the node's number in decimal (the `from` of the downlink); `channelIndex` and
  `channelName` name the private channel the text goes to. `toNodeNum` sends a direct message instead.
- Foresignal publishes at QoS 1 on one client per broker (`foresignal-<installId>-mesh`, no Last Will) and marks
  the delivery sent on the broker's PUBACK: JSON gives no delivery confirmation.

**MeshCore prerequisites.** A WiFi companion firmware reachable as `tcp://host:port` (explicit port, a private
address). Foresignal shares the one TCP client the companion accepts with the `meshcore-local` source and only
ever sends the three registered send commands (channel text 0x03, channel datagram 0x3E, direct text 0x02);
`dmContactPrefix` (a contact's 12-hex key prefix) sends direct messages, and `requireAck: true` waits for the
contact's acknowledgement, retries once and ends the delivery `unacked` (not `failed`, so no failure event) when
none comes. MeshCore radio settings vary by region, so enter them in `airtimeCustom`.

**Why public channels are refused.** A public or default channel is shared by everyone in radio range, and
automated traffic there drowns human conversation and breaks the mesh's etiquette. Create and update answer
400 `MESH_PUBLIC_CHANNEL` when:

- `meshtastic`: the broker is under `meshtastic.org` or not on the LAN; `channelIndex` is 0; `channelName` is
  empty or a modem-preset name (LongFast, LongSlow, LongModerate, MediumFast, MediumSlow, ShortFast, ShortSlow,
  ShortTurbo, VLongSlow); `licensedMode` is true (ham mode disables encryption); or `privateChannelConfirmed` is
  not true.
- `meshcore`: `channelIndex` is 0, or the slot read from the companion (`CMD_GET_CHANNEL`) is empty, named
  `Public`, a `#hashtag` channel (its key derives from the name) or has an all-zero secret. The slot is read again
  on every reconnect, and nothing is sent until it is verified private; a companion that cannot be reached at save
  time does not block the save.

**Duty cycle.** Every transmission's time on air is estimated with Semtech's formula from the preset
(`meshtastic-longfast`: SF11, 250 kHz, 4/5, 16-symbol preamble; `meshtastic-mediumfast`: SF9; or `custom`) and the
message bytes plus a conservative overhead (32 bytes for Meshtastic, 24 for MeshCore). A one-hour sliding window
admits a message only when:

- the airtime stays within `dutyCyclePercent` (default **1 %** of the hour, a polite share; at most the region's
  ceiling and at most 10 %);
- at least `minSpacing` (default 30 s) has passed since the last transmission;
- fewer than `maxMessagesPerHour` (default 20) went out.

`region` sets the ceiling: 10 % for EU_868 and UK_868 (the 869.40–869.65 MHz sub-band Meshtastic's EU_868 uses)
and EU_433; US and ANZ have no duty cycle (their dwell-time rules are the radio's job), where the 10 % maximum caps
critical alerts instead. **Worked example, LongFast:** a 60-byte line plus 32 bytes of overhead is 92 bytes on the
air, 93 payload symbols of 8.192 ms plus a 165.9 ms preamble: **0.93 s**. A 1 % share is 36 s an hour, room for
about 38 such lines, so the 20-per-hour cap binds first; a full 200-byte line takes 1.99 s (18 an hour). On
MediumFast the same 60-byte line takes 0.27 s.

A message that does not fit waits (`deferredReason: airtime`); alerts released together go out as one line,
`{n} alerts, top: {title} #{code}`. Critical alerts jump the queue but never exceed the region's ceiling.
`GET /v1/channels/{channelId}/airtime` shows the window (`usedSeconds`, `limitSeconds`, `queued`,
`foldedLastHour`, `droppedLastHour`), the counters `foresignal.channel.mesh.airtime_seconds`, `…deferred`,
`…folded` and `…dropped` are tagged with the channel id, and a backlog older than an hour raises the system event
`system.mesh_airtime_backlog`.

**Field reports.** With `reportsEnabled: true`, Foresignal reads messages heard by your own node (the
`meshtastic-local` or `meshcore-local` source must be running) on the channel's **own private index** from the
senders in `allowedSenders` (sender → `report` or `ack`). Senders are Meshtastic node ids (`!a1b2c3d4`); MeshCore
channel messages do not carry the sender's key, only its advertised name, so a MeshCore sender is `name:Alice`
(anyone holding the channel key can use that name). Anything else is ignored without a reply; at most 10 commands
a minute per sender are read.

| Send | Reply | Does |
|---|---|---|
| `#E R FIRE 51.48,-0.12 smoke over the ridge` | `OK R7K2Q FIRE` | records a `field-report` in the private `mesh-reports` source |
| `#E R FLOOD @home water over the road` | `OK R…` | the same, located at place `home` |
| `#E ACK 0007` | `OK ACK #0007` | acknowledges the alert (`acknowledged_by` `mesh:!a1b2c3d4`; role `ack` only), audited |
| `#E STATUS` | `ok: open 0 crit, 2 warn, 5 info` | a summary of at most 133 characters |
| `#E HELP` | the grammar card | |

Types are FIRE, SMOKE, FLOOD, POWER, ROAD, MED, WX, SEC and OTHER; keywords are case-insensitive. A malformed
command answers `ERR {reason}`. Replies go out on the same channel through the airtime governor (a reply it
cannot fit is dropped). Report text is untrusted: it is never embedded, and Ask and briefings quote it only as
data.

**License-free operation only.** Unattended, automated transmission on amateur allocations is restricted by local
rules (for example FCC Part 97 in the US), which is one reason licensed mode is refused. These channels are for
license-free ISM operation under your region's rules.

### Chat commands

A Telegram or Matrix channel can also take commands: list and acknowledge alerts, ask questions, read or run
briefings and check health from the chat app you already get alerts in. Foresignal answers only chats you
allowlist on the channel, and each chat gets `read` or `agent` scope (never `admin`), checked exactly like an
API token of that scope.

**Privacy.** Telegram bot chats are not end-to-end encrypted, and the Matrix rooms used here must be
unencrypted. Telegram or your homeserver can read the alert titles, Ask answers and briefing excerpts
Foresignal sends. Replies carry summaries and links, never raw payloads or secrets. Leave `privateAccess`
off unless the chat may see your private sources.

**Telegram.**
1. Create a **dedicated** bot with @BotFather (`/newbot`). Interactive mode long-polls `getUpdates`, which works
   for only one consumer per bot and not while a webhook is set: do not share the token with another program.
   If the bot has a webhook, Foresignal reports `degraded` ("bot has a webhook; set takeOverWebhook or use a
   dedicated bot"); set `takeOverWebhook: true` only if you want Foresignal to delete it.
2. In groups, Telegram's **privacy mode** (on by default, `/setprivacy` in @BotFather) lets the bot see only
   messages that start with `/` and replies to it. That is all Foresignal needs; keep it on.
3. Create the channel with `interactive: true` and `discoveryMode: true`, then send `/whoami` to the bot from
   each chat. It answers with that chat's id and your user id. Discovery turns itself off after 10 minutes
   (the stored `discoveryMode` is set back to false); save the channel with `discoveryMode: true` to reopen it.
4. Put the ids into `allowedChats`. A group (negative id) must list `userIds`, otherwise any member could
   command.

```json
{"name": "Telegram ops", "type": "telegram",
 "config": {"chatId": "123456789", "interactive": true,
            "allowedChats": [{"chatId": "123456789", "name": "My phone", "scope": "agent"},
                             {"chatId": "-1001234567890", "name": "Family", "scope": "read", "userIds": ["123456789"]}]},
 "secrets": {"botToken": "…"}, "events": ["alert", "system"]}
```

**Matrix.** Create a separate bot account on your homeserver, invite it to an **unencrypted** room (an
encrypted room is ignored and the channel reports "encrypted rooms are not supported"), and use its access
token as the channel's `accessToken`. Allowlist the room id (`!abc:example.org`); a room with more than two
members must list the allowed `userIds` (`@ana:example.org`). Matrix has no buttons, so proposed Ask actions
come with a 6-character code: reply `/confirm CODE` or `/reject CODE`.

**Scopes and commands.** Messages from chats or users that are not allowlisted are ignored without a reply
(counted in `foresignal.chatops.rejected`). Every command is written to the audit log as
`chat:{channelId}:{chatId}[:{userId}]`.

| Command | Scope | What it does |
|---|---|---|
| `/help` | read | The commands this chat may use |
| `/status` | read | The glance as text |
| `/alerts [n] [critical\|warning\|info]` | read | Open alerts, newest first (at most 10); agent chats get Ack and Resolve buttons on Telegram |
| `/alert <id>` | read | Rule, record title, kind, source, observed time, state and a link |
| `/ack <id>`, `/resolve <id>` | agent | Acknowledge or resolve; recorded as the chat principal |
| `/snooze <id> <duration>` | agent | Snooze for `30m`, `2h` … (at most 7 days) |
| `/ask <question>` | read | Ask Foresignal; one conversation per chat, `/ask new` starts another. Proposed actions get Confirm/Reject buttons (Telegram) or a code (Matrix) in agent chats |
| `/brief [id\|name]` | read | The latest briefing run (first 1,500 characters and a link) |
| `/brief run <id\|name>` | agent | Run a briefing now; replies when it finishes |
| `/sources` | read | Degraded and failing sources with reasons |
| `/confirm <code>`, `/reject <code>` | agent | Decide a proposed Ask action (Matrix) |

Limits: `commandRateLimit` commands per chat per 10 minutes (30) and `askRateLimitPerHour` questions (10);
Ask also spends the LLM budget. `GET /v1/channels/{id}/interactive` shows whether the connector is connected,
its last poll and error, the discovery window and which chats are served. If the platform refuses the token,
the connector stops, a `system` event is sent to your other channels, and it starts again when you save the
channel. `PUT /v1/settings/notifications` with `{"chatops": {"enabled": false}}` stops every interactive channel
at once.

## Inbound webhooks

Some data cannot be polled: an SDR decoder, AIS-catcher, Home Assistant, a script, an n8n flow,
Alertmanager, a CAP publisher, or any homelab tool that already speaks ntfy or Gotify. These push
into a **custom source**. Pushed records go through the same writer as polled data, so versioning,
policies, touches, the change stream, triggers, tiles, search and retention all apply.

### Creating a custom source

```json
POST /v1/custom-sources
{"id": "custom-garage", "name": "Garage sensors", "kinds": ["sensor-reading"],
 "observedAtMode": "UPSTREAM", "recordTtl": "1h", "ttlAnchor": "RECEIVED",
 "volatileFields": ["rssi"], "significantFields": [], "titleField": "name",
 "tileProperties": ["value"], "rateLimitPerMinute": 60, "recordsPerMinute": 6000}
```

The response is `201` with `ingestKey` (`eik_…`), **shown once**; only its SHA-256 and first eight
characters (`ingestKeyPrefix`) are stored. Rotate it with
`POST /v1/custom-sources/{id}/ingest-key/rotate`; the old key stops working at once. The id must
start with `custom-` and be unique across all sources. The source is created enabled, appears in
`GET /v1/sources` with `custom: true`, and is always STANDARD tier.

- **Time base (`observedAtMode`).** `UPSTREAM` (the default) means every record carries its own
  `observedAt`; a record without one is rejected `MISSING_OBSERVED_AT`, never silently stamped.
  `RECEIVED` stamps the receipt time on every record (flagged `TIME_IMPUTED`). A source never
  mixes the two, and the mode can change only while the source has no records
  (`409 OBSERVED_AT_MODE_LOCKED`).
- **TTL.** `recordTtl` makes records expire from the current view; `ttlAnchor` counts it from receipt
  (`RECEIVED`, the default) or from `observedAt` (`OBSERVED`).
- **Volatile and significant fields.** Volatile fields (an RSSI, a battery level) are left out of the
  hash, so a change to them alone is a touch, not a version. Significant fields are the ones whose
  change is always worth a version; a SAMPLED or LATEST ingest policy needs them
  (`400 INGEST_POLICY_INVALID` if they are emptied). Editing either list changes the hash recipe: the
  writer rehashes the source's records before its next batch, so the edit never produces a burst of
  spurious versions.
- **Editing.** `PUT /v1/custom-sources/{id}` replaces the definition; the id cannot change, and
  removing a kind that still has current records is `409 KIND_IN_USE`.
- **Caps.** `rateLimitPerMinute` limits requests (the source's budget); `recordsPerMinute` (default
  6,000, at most 60,000) limits records. Over either is `429` with `Retry-After`, and a `REFUSED` row
  appears in `GET /v1/sources/{id}/fetches`. A body is at most 5 MB (decompressed; `gzip` is accepted)
  and 10,000 records.
- **Deleting** a custom source stops ingest; its records age out through retention.
- **`mapping`, `preset`, `presetParams` and `captureSamples`** are described under "Devices and
  mappings"; `profile`, `allowPathKey` and `ingestAllowCidrs` under "Personal location and privacy".
- **Members later prompts own** (`licenceTerms`, prompt 109) are refused with `400` until then.

Every push answers `200` with an `IngestOutcome` (`seen`, `persisted`, `touched`, `rejected` with
each item's index and reason, …) whenever the request itself was valid, even if some records were
rejected. `GET /v1/sources/{id}/fetches` shows each push with `trigger: PUSH`, the principal
(`ingest-key:<prefix>`, `compat-ntfy:<prefix>`, `compat-gotify:<prefix>` or `token:<name>`) and the
body's SHA-256.

### Credentials

Send the key as `X-Foresignal-Ingest-Key: eik_…`, as `Authorization: Bearer eik_…`, or as the
Basic-auth password (any user name), for devices that only do Basic. An admin token also works. A
key belongs to one source; pushing it to another is `401`.

### Formats

`POST /v1/ingest/{sourceId}` reads the `native` format; `POST /v1/ingest/{sourceId}/{format}` reads a
named one. `GET /v1/ingest/formats` lists them with their options. Options live in the source's
`formats` object, keyed by format id.

| Format | Body | Notes |
|---|---|---|
| `native` | GeoJSON FeatureCollection, or `{"records": [...]}` | Feature `id` is the external id; properties `kind` (optional with one kind), `observedAt`, `eventTime`, `validUntil`, `tombstone`, `quality`; the rest is the payload |
| `alertmanager` | Alertmanager webhook payload v4 | One record per alert (`fingerprint`); `kindLabel` picks the kind from a label; `resolved: tombstone` (default) or `record` |
| `cloudevents` | CloudEvents 1.0 structured, batch or binary mode | External id `subject`, else `source/id`; `typeKinds` maps CloudEvents types to kinds; answers the webhook validation handshake (`OPTIONS` with `WebHook-Request-Origin`) |
| `cap` | CAP 1.2 XML | `Alert` creates a record, `Update` also tombstones the referenced alerts, `Cancel` only tombstones; polygons and circles become geometry; non-`Actual` alerts are rejected unless `acceptNonActual` |
| `mapping` | Any JSON, or a form body | Uses the source's `mapping` member, a RecordMapping (see "Devices and mappings" below). The 024 `formats.mapping` JSON-Pointer options are no longer accepted |
| `ecowitt`, `wu-pws`, `aiscatcher`, `chirpstack-v4`, `tts-v3`, `airrohr`, `gmc` | The device's own payload | The device presets (see "Devices and mappings"); each replies the way its firmware expects |

`media` and `rasters` are the remote SatDump endpoints and `mapping-preview` is reserved
(`404 FORMAT_UNKNOWN`). A source created from a preset uses the preset's format on
`POST /v1/ingest/{sourceId}` too.

```sh
# native GeoJSON
curl -X POST http://foresignal.local:8080/v1/ingest/custom-garage \
  -H "X-Foresignal-Ingest-Key: $KEY" -H "Content-Type: application/geo+json" \
  -d '{"type":"FeatureCollection","features":[{"type":"Feature","id":"garage-temp",
       "geometry":{"type":"Point","coordinates":[-97.74,30.27]},
       "properties":{"observedAt":"2026-10-12T08:00:00Z","name":"Garage","value":31.5}}]}'

# a CloudEvent
curl -X POST http://foresignal.local:8080/v1/ingest/custom-garage/cloudevents \
  -H "Authorization: Bearer $KEY" -H "Content-Type: application/cloudevents+json" \
  -d '{"specversion":"1.0","id":"e1","source":"/ha/garage","type":"door","subject":"garage-door",
       "time":"2026-10-12T08:00:00Z","data":{"state":"open"}}'

# a CAP alert
curl -X POST http://foresignal.local:8080/v1/ingest/custom-alerts/cap \
  -H "X-Foresignal-Ingest-Key: $KEY" -H "Content-Type: application/cap+xml" --data-binary @alert.xml
```

**AIS-catcher.** Use the `aiscatcher` preset (see "Devices and mappings"); `gzip` is accepted.

**Home Assistant** (`configuration.yaml`):

```yaml
rest_command:
  foresignal_push:
    url: "http://foresignal.local:8080/v1/ingest/custom-home"
    method: POST
    headers:
      X-Foresignal-Ingest-Key: !secret foresignal_ingest_key
    content_type: "application/json"
    payload: >-
      {"records": [{"externalId": "{{ entity_id }}", "kind": "state",
        "observedAt": "{{ now().isoformat() }}", "payload": {"state": "{{ state }}"}}]}
```

**Python:**

```python
import requests
requests.post("http://foresignal.local:8080/v1/ingest/custom-garage",
              headers={"X-Foresignal-Ingest-Key": KEY},
              json={"records": [{"externalId": "probe-1", "kind": "sensor-reading",
                                 "observedAt": "2026-10-12T08:00:00Z", "payload": {"value": 21.4}}]},
              timeout=10).raise_for_status()
```

**Alertmanager** (`alertmanager.yml`), which cannot sign, authenticates with the key as a bearer
token:

```yaml
receivers:
  - name: foresignal
    webhook_configs:
      - url: "http://foresignal.local:8080/v1/ingest/custom-alerts/alertmanager"
        send_resolved: true
        http_config:
          authorization:
            type: Bearer
            credentials_file: /etc/alertmanager/foresignal-key
```

### ntfy and Gotify compatibility

Tools that publish to ntfy or Gotify can publish to Foresignal unchanged. Give such sources
`observedAtMode: RECEIVED` (the messages carry no observation time) and a TTL. The kind is the
source's single kind, or `notification` (which must then be one of its kinds); the payload is
`{title, message, priority, tags, click, topic}`. The external id comes from
`X-Foresignal-External-Id`, an ntfy tag `id=<x>`, or Gotify `extras["foresignal::record"].externalId`,
otherwise each message gets a new ULID.

- **ntfy:** the topic is the custom source id. `POST` or `PUT /compat/ntfy/{topic}` (body = message;
  `X-Title`, `X-Priority` (1–5 or `min`…`urgent`), `X-Tags`, `X-Click`),
  `GET` or `POST /compat/ntfy/{topic}/publish` (also `send`, `trigger`) with `?message=&title=…`, and
  the JSON form `POST /compat/ntfy` with `{"topic", "message", "title", "priority", "tags", "click"}`.
  Credentials: Bearer or Basic with the key, or ntfy's `?auth=` (base64 of `Bearer eik_…`).
- **Uptime Kuma:** add an ntfy notification with server URL `http://foresignal.local:8080/compat/ntfy`
  (Kuma posts the JSON form there), topic `custom-uptime`, and the ingest key as the access token.
- **Gotify:** `POST /compat/gotify/message` (JSON or form `title`, `message`, `priority`, `extras`);
  the key in `X-Gotify-Key` or `?token=` picks the source.
- **Proxmox:** add a Gotify notification target with server URL
  `http://foresignal.local:8080/compat/gotify` (Gotify clients post to `{server}/message`) and the
  ingest key as the token.

`?auth=` and `?token=` values are masked as `***` in logs and in the fetch log. A message carrying
this instance's own origin marker (the ntfy tag `foresignal-origin-<id>` or the Gotify extra
`foresignal::origin`) is acknowledged and dropped, so a notification channel pointed back at
Foresignal cannot feed itself.

### Signatures

A source can require a webhook signature per format under `verifiers.{format}`:

| Scheme | What is checked |
|---|---|
| `hmac-sha256` | `X-Signature-256: sha256=<hex>` (header configurable) over the raw body |
| `standard-webhooks` | `webhook-id`, `webhook-timestamp` and `webhook-signature: v1,<base64>` over `id.timestamp.body`, ±5 minutes, a replayed id refused for 10 minutes |
| `foresignal` | `X-Foresignal-Timestamp` and `X-Foresignal-Signature: sha256=HMAC("<ts>.<body>")`, the scheme of Foresignal's own webhook channel (instance to instance) |

```json
"verifiers": {"native": {"scheme": "hmac-sha256", "secret": "…", "keyOptional": false}}
```

With a verifier set, a request must carry a valid signature **and** the key
(`401 SIGNATURE_INVALID` otherwise); with `keyOptional: true` a valid signature alone authenticates.
Secrets are write-only: reads show `secretSet: true`, and omitting `secret` on an update keeps the
stored one. An admin token is not asked for a signature.

### A geofence rule on a custom kind

Custom kinds work in rules like any other:

```json
POST /v1/triggers
{"name": "Drone near the house", "sources": ["custom-adsb"], "kinds": ["drone"], "mode": "enter",
 "geofence": {"type": "Polygon", "coordinates": [[[-97.75,30.26],[-97.73,30.26],[-97.73,30.28],
              [-97.75,30.28],[-97.75,30.26]]]}, "severity": "warning"}
```

## Devices and mappings

Homelab devices that can only send their own payload (weather stations, AIS-catcher, LoRaWAN
servers, Alertmanager, air-quality sensors, Geiger counters) are onboarded with a **preset** or a
**RecordMapping**, with no code.

### Creating a source from a preset

```sh
curl -X POST http://foresignal.local:8080/v1/custom-sources -H "Authorization: Bearer $ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"id":"custom-backyard-weather","preset":"ecowitt","presetParams":{"lon":-97.7431,"lat":30.2672}}'
```

`GET /v1/mappings/presets` lists the presets with their mapping, defaults, parameters and setup
text. The preset fills every member you leave out (kinds, field specs, time base, TTL, significant
and volatile fields, privacy, `allowPathKey`, the mapping and format options); anything you set
wins. `lon`/`lat` are rounded to 0.01° (about 1 km) unless `exactLocation: true`. The response
carries the ingest key and `setup`: the device instructions with the concrete URL, built from
`publicBaseUrl`. **Both are shown once**: the URL can contain the key.

**Cleartext and the LAN.** Most of this firmware speaks plain HTTP and can only put the key in the
URL path or the query string. Keep such devices on the same LAN as Foresignal: pushes with a key in
the path, a key in the query (`PASSWORD`) or by GET are accepted only from private addresses
(loopback, RFC 1918, link-local, ULA) unless the source's `ingestAllowCidrs` lists the sender
(`403 INGEST_SOURCE_DENIED` otherwise). Never forward these URLs over the internet without TLS.

### Ecowitt (`ecowitt`)

Consoles and gateways (GW1000/GW1100/GW2000, HP2551, WS2910 …) in the WS View Plus or Ecowitt app:
Weather Services → **Customized**.

| Setting | Value |
|---|---|
| Customized | Enable |
| Protocol Type | Ecowitt |
| Server IP / Hostname | the host of `publicBaseUrl` (for example `192.168.1.10`) |
| Path | `/v1/ingest/<sourceId>/k/<key>/ecowitt` |
| Port | the port of `publicBaseUrl` (for example `8080`) |
| Upload Interval | 16 seconds or more |

Fields: `tempC`, `rh`, `pressureHpa` (relative), `windDirDeg`, `windSpeedMs`, `windGustMs`,
`rainRateMmH`, `rainDailyMm`, `solarWm2`, `uvIndex`, `pm25` (channel 1), `lightningKm`,
`indoorTempC`, `indoorRh` and `model` (from `stationtype`); `dateutc` is the observation time.
`PASSKEY` (a hash of the console's MAC address) is never stored. Policy LATEST with a 5-minute
keyframe: the live value follows every upload, history keeps one version per 5 minutes plus model
changes. One source per station (the external id is `station`).

### Weather Underground protocol (`wu-pws`)

Any station software that uploads with the WU PWS protocol, or an Ecowitt console with Customized →
Protocol Type **Wunderground**: Server `<host>`, Path `/v1/ingest/<sourceId>/wu-pws?`, Port
`<port>`, Station ID anything, Station Key `<key>`. Other software sends
`GET /v1/ingest/<sourceId>/wu-pws?ID=any&PASSWORD=<key>&dateutc=now&tempf=…`. The key is the
`PASSWORD` parameter; `ID` is ignored. The reply is `success`. Every observation is stamped with its
receive time (`observedAtMode: RECEIVED`), because most stations send `dateutc=now`.

### AIS-catcher (`aiscatcher`)

```
AIS-catcher … -H http://foresignal.local:8080/v1/ingest/custom-ais/aiscatcher PROTOCOL AISCATCHER USERPWD x:<key> GZIP on INTERVAL 30
```

The key is the Basic password. One vessel record per MMSI; position reports and static messages are
merged into one snapshot (`sog`, `cog`, `heading`, `navStatus`, `name`, `callsign`, `destination`,
`shipType`). The AIS "not available" values (lat 91, lon 181, speed 102.3, course 360, heading 511)
are dropped. SAMPLED vessel policy, 30-minute TTL, 3 days of history. The station position AIS-catcher
sends is never stored. Where AIS-catcher runs next to Foresignal, the built-in `local-ais` source
(prompt 085) is preferred.

### ChirpStack v4 (`chirpstack-v4`)

Applications → your application → Integrations → **HTTP**: payload encoding **JSON**, event
endpoint `http://foresignal.local:8080/v1/ingest/<sourceId>/chirpstack-v4`, header
`Authorization: Bearer <key>`. Uplinks (`?event=up`) become one record per DevEUI: `name`, `fPort`,
`fCnt` and up to 32 numeric keys of the decoded `object`; `rxInfo[0]` RSSI and SNR go to
`quality.extra` (`lora.rssi`, `lora.snr`). `location` events move the device; other events are
acknowledged and ignored; the Protobuf encoding is refused (415). Position: `object.latitude` and
`object.longitude`, else the last `location` event, else the preset's `lon`/`lat`. **Private by
default**: trackers carry personal data.

### The Things Stack v3 (`tts-v3`)

Applications → your application → Integrations → **Webhooks** → Custom webhook: format JSON, base URL
`http://foresignal.local:8080/v1/ingest/<sourceId>/tts-v3`, additional header
`Authorization: Bearer <key>`, event type **Uplink message**. Same record shape as ChirpStack, from
`uplink_message.decoded_payload`. Position: the `frm-payload` location, else the device's configured
location (`locations.user`, flagged `lora.locationSource=static`), else the preset's `lon`/`lat`.
Private by default.

### Alertmanager (`alertmanager`)

```yaml
receivers:
  - name: foresignal
    webhook_configs:
      - url: http://foresignal.local:8080/v1/ingest/<sourceId>/alertmanager
        send_resolved: true
        http_config:
          authorization:
            type: Bearer
            credentials_file: /etc/alertmanager/foresignal-key
```

The preset uses the built-in `alertmanager` format with: external id `{receiver}:{fingerprint}`,
`severity` and `alertname` as significant fields, and a resolved alert kept as a new version with
`status=resolved` and `validUntil = endsAt + 1 h`. `presetParams.tombstoneOnResolve: true` ends the
record instead; `lon`/`lat` place alerts without location labels.

### Sensor.Community airrohr (`airrohr`)

On the sensor's configuration page (`http://<sensor>/config`) → APIs → **Send data to own API**:
Server `<host>`, Path `/v1/ingest/<sourceId>/airrohr`, Port `<port>`, User anything, Password `<key>`.
The `sensordatavalues` pairs become `pm1`, `pm25`, `pm4`, `pm10` (SDS011, PMS or SPS30, the first
present wins), `tempC`, `rh`, `pressureHpa` (BME280, converted from Pa) and `firmware`; the Wi-Fi
signal goes to `quality.extra wifi.rssi`. The names match the `openaq` adapter. LATEST with a
10-minute keyframe.

### GQ GMC Geiger counter (`gmc`) — unverified

Server → Website `<host>`, URL `/v1/ingest/<sourceId>/k/<key>/gmc`. The counter sends
`GET …?AID=…&GID=…&CPM=…&ACPM=…&uSV=…`; the reply is `OK.`. Fields `cpm`, `acpm`, `doseRateUSvH`
and `geigerId`; `AID` is never stored. **This preset was written from the protocol description, not a
capture (2026-10).** Turn on `captureSamples`, check the first upload under `/samples`, and adjust the
mapping if the counter differs.

### Raspberry Shake rsudp alarm (`rsudp-alarm`) — unverified

`contrib/rsudp/foresignal_alarm.py`, run by rsudp's `custom` module on every alarm, POSTs
`{"event": "ALARM", "time": …, "station": …, "channel": …}` to `/v1/ingest/<sourceId>/k/<key>/mapping`. Each post
becomes a `seismic-trigger` record (externalId `station.channel`, `eventTime` = `time`) at the station point given as
`presetParams` `lon`/`lat`. **Written from rsudp's documentation, not a capture (2026-10).** See "Local RF decoders".

### Writing a mapping

A `RecordMapping` is data, not a script. It goes in the source's `mapping` member:

```json
{"version": 1,
 "select": "$.items[*]",
 "externalId": {"template": "probe-{{$.serial}}"},
 "observedAt": {"path": "$.ts", "format": "epochS"},
 "geometry": {"lon": {"path": "$.pos.lon"}, "lat": {"path": "$.pos.lat"}},
 "fields": [
   {"name": "tempC", "path": "$.t_f", "type": "number", "unit": {"from": "[degF]", "to": "Cel"}},
   {"name": "battery", "path": "$.bat", "type": "integer", "sentinels": [-1]}
 ],
 "drop": ["apikey"],
 "filter": [{"path": "$.type", "op": "==", "value": "reading"}]}
```

- Values are `{path}` (Jayway JSONPath; `$` is the document, or each item under `select`;
  `$['frm-payload']` quotes a name), `{template}` (substitution only) or `{const}`. Deep scan
  (`..`), function calls and regex filters are refused.
- Form and query inputs are flat objects of strings: `$.tempf`.
- `observedAt` is `{path, format: iso | epochS | epochMs | pattern:<Java DateTimeFormatter>, zone}`
  under `observedAtMode: UPSTREAM`, and `{received: true}` under RECEIVED; mixing them is refused.
- Units come only from the built-in table: `[degF]`→`Cel`; `[mi_i]/h`, `km/h`, `[kn_i]`→`m/s`;
  `[in_i]`→`mm`; `[in_i]/h`→`mm/h`; `[in_i'Hg]`, `Pa`→`hPa`; `[mi_i]`→`km`; `[ft_i]`→`m`.
- `pairs` flattens name/value arrays (`$pairs.<name>`); `spread` copies an object's scalar members;
  `tombstone`, `quality` (`accuracyMeters`, `extra`) and `eventTime`/`validUntil` are optional.
- A value that cannot be converted (or NaN) leaves that field out; an out-of-range coordinate leaves
  the geometry out.

Errors come back as `400 MAPPING_INVALID` with a path per problem, such as
`mapping.fields[3].unit.from`.

### Preview, samples and test

- `POST /v1/mappings/preview` with `{mapping, sample: {contentType, body | query}, sourceId?}`
  shows exactly what ingest would write, normalized, and writes nothing. Warnings name unmapped
  input keys, fields missing from the source's `fieldSpecs`, significant fields a SAMPLED or LATEST
  policy needs, and a time-base mismatch.
- `captureSamples: true` on the source keeps the last five raw requests in memory for one hour,
  redacted, without headers except `Content-Type`: `GET /v1/custom-sources/<id>/samples`.
- `POST /v1/custom-sources/<id>/mapping/test` runs the stored mapping, or `{"mapping": …}`, over
  those samples.

To write a mapping for a new device: create a plain custom source with `captureSamples: true`, point
the device at it, read `/samples`, preview a mapping against one of them, then save it with
`PUT /v1/custom-sources/<id>`.

### Asking the LLM for a draft

`POST /v1/mappings/suggest` with `{sample, hints: {kind, description, sourceId}, confirmEgress}`
asks the chat provider for a draft. The sample is sent as untrusted data and the draft is validated
(two repair rounds at most), then previewed. **It is never saved**: review it and save it yourself.
A remote provider needs `confirmEgress: true` (`409 EGRESS_CONFIRMATION_REQUIRED`), because the
sample leaves your network; a private source's sample is never sent to one. It spends the
provider's budget like any other call.

## External data providers

The External Data Protocol (EDP, [docs/External-Data-Protocol.md](External-Data-Protocol.md)) lets
anyone add a data source without changing Foresignal: a provider describes itself with a
**manifest** and pushes or serves **items**. `GET /v1/external/protocol` shows the protocol version
and item types; `GET /v1/external/protocol/schemas/{name}` serves the JSON Schemas.

### Registering a provider

- **By manifest or URL (admin):** `POST /v1/external/sources` with `{"manifest": {...}}` or
  `{"manifestUrl": "https://provider.example/.well-known/foresignal-edp.json"}` (fetched once), plus
  `"approve": true` to activate it at once. The response carries `ingestKey` once. The provider
  becomes the source `ext-{source.id}`.
- **By registration token:** create one with `POST /v1/external/registration-tokens`
  (`{"name", "expiresAt", "maxUses" (default 1), "sourceIdPrefix"}`); the `fsx_…` token is shown
  once. The provider calls `POST /v1/external/register` with `Authorization: Bearer fsx_…` and its
  manifest; the source is created **pending**. A provider id outside the token's prefix is
  `409 SOURCE_ID_NOT_ALLOWED`; a used-up or expired token is `401 REGISTRATION_TOKEN_INVALID`.
  `GET /v1/external/registration-tokens` lists unexpired tokens (never the secret);
  `DELETE /v1/external/registration-tokens/{id}` revokes one.

A manifest is validated first: `400 MANIFEST_INVALID` lists every error with its JSON Pointer, and
a protocol minor version above what this instance supports is `400 PROTOCOL_UNSUPPORTED`.

### Approval and the access block

Nothing from a **pending** or **suspended** provider is stored (pushes answer
`409 SOURCE_PENDING_APPROVAL` / `SOURCE_SUSPENDED`, and it is not pulled). Before approving, read
`GET /v1/external/sources/{id}`: the **access block** says how the provider gets its data
(`official`, `unofficial-api`, `scrape`, `browser`, `other`), how likely it is to break
(`fragility`) and a summary of the upstream's terms; the license's `redistribution` sets the egress
(`allowed` → `OPEN`, otherwise `LOCAL_ONLY`); `private` marks personal data. Then
`POST /v1/external/sources/{id}/approve`. `POST …/suspend` stops it (records stay);
`DELETE /v1/external/sources/{id}` removes it (records age out through retention).

Provider URLs (manifest, pull, search) must be `https` and may not reach private, loopback,
link-local, carrier-grade NAT or cloud-metadata addresses, including by DNS rebinding or redirects
(only same-host redirects are followed). For a provider on your own network, register it with
`"allowPrivateNetwork": true`; metadata addresses stay refused even then.

A provider (with its ingest key) or you can send `PUT /v1/external/sources/{id}/manifest`. Additive
changes (descriptions, presentation, new optional fields) apply at once; a change to streams, kinds,
item types, modes, payload schemas or provider URLs returns the source to **pending** until you
approve it again, and the response lists those changes in `breakingChanges`.

### Push and pull

- **Push:** the provider sends batches to `POST /v1/external/sources/{id}/items` (JSON, or NDJSON
  with an optional header line) with its ingest key, the same credentials as custom sources.
- **Pull:** a manifest with `transport.pull` makes Foresignal poll `itemsUrl?cursor=&limit=500` every
  `intervalSeconds`, following `nextCursor` and storing the cursor. Enter the credential with
  `PUT /v1/sources/{id}/credentials`: `token` for `bearer`, `value` for `header`, `username` and
  `password` for `basic`. Requests count against the source's budget; provider failures show in its
  health and in `pull.lastError`.

Layer streams become ordinary map layers (their `presentation` hints appear on the provider and in
the kind's TileJSON); atomic streams are stored, searchable and usable in rules, kept out of the
default tile layer list, and do nothing analytic until you bind them (see
[Binding external streams](#binding-external-streams-prompt-138)); `streams[].bindings` counts each
stream's bindings. `status` items change the provider's health
without creating records. Snapshot streams remove entities only after a complete, validated snapshot
of the same scope; a snapshot that would remove more than half of a scope is held and the provider
turns degraded.

### Reading validation errors

A push answers `200` with `accepted`, `rejected` (each item's index, stream, id, reason such as
`SCHEMA_INVALID`, `PAYLOAD_INVALID`, `GEOMETRY_INVALID`, `PERIOD_INVALID` or `FORECAST_INVALID`, and
`schemaErrors` with JSON Pointers) and the writer's `outcome`. Valid items in the same batch are kept.
`GET /v1/external/sources/{id}` shows per-stream counts and the last validation errors.
`POST /v1/external/validate` (agent) checks a manifest and sample items without storing anything,
for provider developers and CI.

## Binding external streams (prompt 138)

An approved provider's items are stored, searchable and usable in rules from the first push, but an atomic item does
nothing analytic on its own: it never enters a series, a canonical event, the social or news pipeline, hypothesis
evidence, an indicator or a forecast until you **bind** its stream. A binding connects one stream of one provider to
one destination (its **target**), with a target configuration and an optional filter. Bindings work on layer streams
too (a layer of `event` items can join a canonical-event family), and provenance always travels: every bound item
keeps its provider, stream and originator.

| Call | What it does |
|---|---|
| `GET /v1/external/sources/{sourceId}/bindings` (admin) | The provider's bindings with state and counts |
| `POST /v1/external/sources/{sourceId}/bindings` (admin) | Create a binding (`ExternalBindingInput`); 201 |
| `POST /v1/external/sources/{sourceId}/bindings/accept-suggested` (admin) | Create the bindings the manifest suggests; 201 |
| `GET`, `PUT`, `DELETE /v1/external/bindings/{bindingId}` (admin) | Read, replace or delete one |
| `POST /v1/external/bindings/{bindingId}/preview` (admin) | What it would do over the last 100 items, without side effects |
| `GET /v1/external/sources/{sourceId}/question-links` (read) | The provider's question links |
| `POST /v1/external/sources/{sourceId}/question-links` (agent) | Link a provider question to a ledger question; 201 |
| `DELETE /v1/external/question-links/{linkId}` (agent) | Remove a question link; 204 |

A binding body has `streamId` and `target` (required), `config` (an object, per target below), `filter` (an array of
017 conditions `{path, op, value}` on the item, ANDed, at most 20; an item's `properties` are top-level paths next to
its type's own fields such as `value`, `seriesKey`, `text` or `url`), `enabled` (default true) and `backfill` (default
true). A binding answers with its `id`, `sourceId`, `state` (`active`, `disabled`, `backfilling` or `error`),
`applied` (how many record versions it applied), `lastError`, `manifestSha256` (the manifest it was made for),
`targetRef` (what it set up, such as a chronicle layer), `createdBy`, `createdAt`, `updatedAt` and `lastAppliedAt`.
`GET /v1/external/sources/{id}` counts each stream's bindings in `streams[].bindings`.

A binding is checked against its stream: each target accepts certain item types, and a mismatch is
`400 BINDING_TARGET_INVALID`, naming the stream's item type and the target's requirement; a configuration problem
(a missing hypothesis set, an unknown place, a bad filter operator) is the same code with one error per field.

### How bindings are applied

The **binding router** follows the record stream on its own durable cursor, `external.binding.cursor`, which joins
retention's consumer clamp like every durable consumer, so version retention never deletes EDP versions the bindings
have not seen. It reads only the versions of providers that have an enabled binding; a version belongs to a stream when
it carries that stream's EDP provenance and the stream's kind, so records the bindings write themselves (converted
posts and articles) are never routed again. Each enabled binding whose filter matches is applied **exactly once per
binding and record version**: the idempotency key (binding, source, external id, version) is stored with the target's
change in one transaction, so the router, a backfill and a restart never apply a version twice. A failure rolls the
target's change back, records the binding's `lastError` and moves on, so one bad item never stalls the stream.
Deployment property `foresignal.external.bindings.auto-start` (true) runs the router.

**Backfill.** A new binding (and a replaced one whose stream, target, configuration or filter changed, or that you
re-enable) backfills from stored records within retention: the newest 100,000 stored versions of its stream at or
below the router's cursor, applied oldest first, while its state is `backfilling`; it then becomes `active`. The
router handles everything after the cursor, and the idempotency key removes any overlap. Send `"backfill": false` to
skip it.

**Receipt is the clock.** Bindings use Foresignal's own receipt for availability, never a provider's claimed time
alone (EXT-4): a series point's `knownAt` is the item's effective `knownAt`, `max(knownAt, receipt)`, and a forecast's
`made_at` is the time the ledger records it, never before Foresignal received the item.

### Targets

**`series`** (observation streams): each `seriesKey` becomes the analysis series
`series:ext-{id}/{kind}/{seriesKey}`, read by the 093/119 series reader like any other series (`GET
/v1/analytics/series/data?ref=…`). A point is the latest stored version of each period `t`; its `knownAt` is the
effective `knownAt`, so as-of reads and look-ahead guards never see a value before Foresignal had it. Daily and weekly
series are registered in the 119 analysis-series registry (and kept by the nightly registration); monthly, quarterly,
yearly and instant series are readable but not analyzed. Configuration, all optional: `unit` (else the stream's
`unit`), `transform` (`none`, `log1p` or `diff`, reported as the 117 transform hint `level`, `log-level` or
`difference` that detectors apply; values are served as delivered), `releaseLagDays` (else the stream's), `family`
(the signal family), `seriesFamily` and `regionCode`.

```json
POST /v1/external/sources/ext-hormuz-transits/bindings
{"streamId": "transits", "target": "series",
 "config": {"unit": "vessels/day", "transform": "log1p", "releaseLagDays": 1, "family": "maritime", "regionCode": "OM"}}
```

**`canonical-family`** (event streams): events join a 116 canonical-event family through the evidence resolver, so EDP
events corroborate, or are corroborated by, other sources' events. `family` is required; a bundled family takes its
matching rule (with the overrides `maxDtSeconds` and `maxKm`), and a family the bundled rules do not know gets a
proximity rule of 3,600 s and 25 km (overridable). Story families (`news-story`, `social-narrative`) are refused: bind
articles with `news` and social posts with `social`. An item counts under its **originator**, never its provider: the
item's `origin.originator`, else the manifest's `source.originator`, registered with its class and its `aggregates`,
so an originator that republishes a built-in source joins that source's evidence group and five providers relaying one
report count once. The item's `upstreamId` (or `upstreamEventId`) and `derivedFrom` are matched as upstream ids in the
namespace `upstreamNamespace` (default `upstream`); set it to the namespace a built-in source uses to match its ids.
The stream's manifest `coverage` (`area` footprint and `pDetect`, 1 when omitted) becomes a **declared**
observability estimate of the provider for that family, low confidence, inside the footprint only, used by
`canSeeAbsence` until Foresignal can measure the source.

```json
POST /v1/external/sources/ext-hormuz-transits/bindings
{"streamId": "incidents", "target": "canonical-family",
 "config": {"family": "maritime-incident", "maxDtSeconds": 7200, "maxKm": 30}}
```

**`social`** (`social-post` documents): each document becomes a 135 `social-post` record (`{platform}:{postId}`, the
post id being the provider's item id), written under the provider so its provenance, license, egress and privacy
travel with it, and goes through the social collector: the author key is salted as for built-in platforms, topics are
matched, and 136 narratives and claims and 137 comparisons include it. The platform is `config.platform`, else the
document's `properties.platform`. The collection is `import`: such posts are a sample that never feeds velocity
baselines, unless the manifest declares the stream `"complete": true`. Properties `channel`, `links`, `hashtags`,
`mentions` and `replyTo`, `quoteOf`, `repostOf` (`{platform?, postId, url?}`) are carried over, so reposts inherit the
original's originator. A document whose stream id equals its platform is refused (the converted post would replace the
provider's item).

```json
POST /v1/external/sources/ext-telegram-channels/bindings
{"streamId": "posts", "target": "social", "config": {"platform": "telegram"}}
```

**`news`** (`article` documents): each document becomes a 133 `news-article` record (external id the SHA-256 of the
canonical URL, written under the provider) with its outlet resolved from the URL's domain, so stories, coverage and
blindspots include it. As everywhere in 133 no byline or author name is stored, and the text is kept only as a
500-character summary; the provider's own item keeps the full document. `sourceCountry` (ISO 3166-1 alpha-2,
optional) is used when the outlet is new; the item's `properties.sourceCountry` wins over it.

```json
POST /v1/external/sources/ext-article-bodies/bindings
{"streamId": "articles", "target": "news", "config": {"sourceCountry": "GB"}}
```

**`evidence`** (feature, event, observation and document streams): matching items become evidence rows of a 125
hypothesis set (source `record`, the item's record reference), deduplicated by 116 evidence group like every record
reference. `hypothesisSetId` is required; `hypothesisId` rates the item against one hypothesis with `rating` (default
`C`); the Admiralty grade `sourceReliability` (`A`–`F`) and `informationCredibility` (1–6) default to `F6`, and
`relevance` (`H`, `M`, `L`) to `M`. 125 rows grade credibility as H, M or L, so the Admiralty grade maps to the weaker
of its two halves (`A` or `B` and 1 or 2 give H, `C` or 3 give M, anything else L) and is kept in the row's description.

```json
POST /v1/external/sources/ext-ukmto/bindings
{"streamId": "advisories", "target": "evidence", "filter": [{"path": "category", "op": "==", "value": "boarding"}],
 "config": {"hypothesisSetId": "01JA8Q3V7S2K9W4T6Y1B5N0M3C", "hypothesisId": "01JA8Q3V7S2K9W4T6Y1B5N0M3D",
            "rating": "C", "sourceReliability": "C", "informationCredibility": 3}}
```

**`indicator`** (feature, event, observation and document streams): when an item passes the filter, the 125 indicator
`indicatorId` is marked `observed` for feature, event and observation streams (physical data, with the item as evidence
and its ACH row), or `reported` for document streams (a report never makes an indicator observed). An indicator that is
no longer waiting is left alone.

```json
POST /v1/external/sources/ext-oref-sirens/bindings
{"streamId": "alerts", "target": "indicator", "filter": [{"path": "area", "op": "==", "value": "Haifa"}],
 "config": {"indicatorId": "01JA8Q3V7S2K9W4T6Y1B5N0M3F"}}
```

**`forecast`** (forecast streams): forecast items become forecasts of the external forecaster
`edp:{sourceId}:{streamId}` (type `external`, registered on first use) on ledger questions, scored like every
forecaster. An item with `questionRef` names its ledger question directly; an item with `question.externalId` needs a
**question link** (below). A forecast is recorded on the first item for a question, on a material move (a probability
change of at least 0.02, or a quantile moving by at least 2% of the previous 5–95% range), and otherwise at least
daily. The ledger stamps `made_at` when it records the forecast, which is never before Foresignal received the item;
the provider's `madeAt`, the effective `madeAt` and the receipt time are kept in the forecast's `inputs.edp`. A
probability needs a binary question and quantiles a numeric one. External forecasters join the 122 ensemble only after
30 resolved linked questions (questions they forecast through a link or a `questionRef`), so a market earns its weight.
No configuration.

```json
POST /v1/external/sources/ext-markets/bindings
{"streamId": "kalshi", "target": "forecast", "config": {}}
```

**`place`** (feature, event, observation and document streams): the latest bound value or document of the stream
becomes an attribute of the 036 place `placeId`, shown in the `external` block of `GET /v1/places/{placeId}`, keyed
`{sourceId}/{streamId}`: the item's reference, times and, for an observation, `seriesKey`, `t`, `value`, `unit` and
`knownAt`, for a document its type, title, the first 500 characters of its text, `url` and `publishedAt`, and for a
feature or event its properties. The binding keeps the newest item; nothing is written to the place itself.

```json
POST /v1/external/sources/ext-river-gauges/bindings
{"streamId": "levels", "target": "place", "filter": [{"path": "seriesKey", "op": "==", "value": "gauge-12"}],
 "config": {"placeId": "home"}}
```

**`situation`** (feature, event and document streams): with `situationId`, matching records join that 118 situation
as pinned `record` members (`role` `core`, the default, or `context`); with `"linker": true` instead, each record is
offered to the 118 linker, which joins it to the best-scoring situation above its threshold and never seeds a new one.
Give exactly one of the two.

```json
POST /v1/external/sources/ext-liveuamap/bindings
{"streamId": "events", "target": "situation", "config": {"linker": true}}
```

**`chronicle`** (any stream): the binding adds a layer of type `external-stream` (`{sourceId, streamId, filter}`,
named `layerName`, else "External stream {sourceId}/{streamId}") to the 131 chronicle `chronicleId`, and the
chronicle's own capture keeps copies of the stream from then on, independent of global retention. Applying an item
does nothing more. The same layer can be added directly with `POST /v1/chronicles/{id}/layers` and the selector
`{"type": "external-stream", "sourceId": "…", "streamId": "…", "filter": […]}`.

```json
POST /v1/external/sources/ext-hormuz-transits/bindings
{"streamId": "incidents", "target": "chronicle",
 "config": {"chronicleId": "01JA8Q3V7S2K9W4T6Y1B5N0M3G", "layerName": "Hormuz incidents (provider)"}}
```

### Question links

A forecast provider names its own questions (`question.externalId`). A **question link** maps one of them to a ledger
question: `POST /v1/external/sources/{sourceId}/question-links` (agent) with `streamId` (a forecast stream),
`externalQuestionId`, `questionId` and `orientation` (`same`, the default, or `inverse`, which records `1 − p`; only
binary questions can be linked inverse, and quantile forecasts cannot be inverted). The link answers with its
`forecasterId` and `lastForecastAt`. A provider question already linked is `409 QUESTION_ALREADY_LINKED`; an unknown
question `404 UNKNOWN_QUESTION`. `DELETE /v1/external/question-links/{linkId}` stops further forecasts through the link
(`404 UNKNOWN_QUESTION_LINK`); recorded forecasts stay.

```json
POST /v1/external/sources/ext-markets/question-links
{"streamId": "kalshi", "externalQuestionId": "HORMUZ-CLOSE-26DEC", "questionId": "01JA8Q3V7S2K9W4T6Y1B5N0M3H",
 "orientation": "same"}
```

### Suggested bindings, previews and changes

**Suggested bindings.** A manifest may suggest bindings per stream (`suggestedBindings`, each `{target, config}`);
`GET /v1/external/sources/{id}` shows them in the manifest. `POST …/bindings/accept-suggested` creates them in one step;
a suggestion identical to an existing binding is skipped. A suggestion that does not validate (a target that needs
configuration the provider cannot know, such as a hypothesis set) is created **disabled** in state `error`, with the
problems in `lastError`, so you can complete it with a `PUT`. Every created binding stays editable.

**Preview.** `POST /v1/external/bindings/{bindingId}/preview` evaluates the binding over the stream's last 100 items
without changing anything: `evaluated`, `matched` (items that passed the filter), `effects` (per matched item its
`itemRef`, the `action` it would take, such as `series-point`, `canonical-link`, `social-post`, `news-article`,
`evidence-added`, `indicator-observed`, `forecast`, `place-attribute`, `situation-member` or a `skip:…` reason, and
the `targetRef`) and `errors`. Preview a binding created with `"enabled": false` before you switch it on.

**Replacing and deleting.** `PUT /v1/external/bindings/{bindingId}` replaces the stream, target, configuration,
filter and enabled state (the same body as a create). Re-enabling a binding, or changing what it applies, backfills
unless the body says `"backfill": false`. `DELETE /v1/external/bindings/{bindingId}` stops it; what it created
(series points, links, posts, articles, evidence rows, forecasts, members, the chronicle layer) stays.

**Breaking manifest updates.** A manifest change to streams, kinds, item types, modes, payload schemas or provider URLs
returns the provider to `pending` (024). The bindings of every stream the update added, removed or changed are then
**disabled**: state `disabled`, `enabled: false`, and `lastError` naming the breaking changes. Each one emits
`external.binding.disabled` on the SSE topic `external` and a `system` notification (subtype
`system.binding_disabled`, linking the binding) to the channels subscribed to `system`. Approve the provider again,
check the stream with a preview, and re-enable the binding with a `PUT`.

### The `external` topic, MCP and problem codes

The SSE topic `external` of `/v1/events/stream` (admin scope, cursor key `x`) carries `external.source.state`
(`sourceId`, `state`, `change`: `registered`, `approved`, `suspended`, `manifest-changed` or `deleted`, and `at`) and
`external.binding.disabled` (`bindingId`, `sourceId`, `reason`, `at`). Its events are live only (id-less): a client
resyncs with `GET /v1/external/sources`. The filter block `external.sourceIds` (or the query parameter
`external.sourceId`) narrows it to some providers.

MCP: `list_external_sources` and `get_external_source` (read) return provider summaries (state, access route and
fragility, originator and what it aggregates, license redistribution, and each stream's mode, item type, kind, item
count and bindings; `get_external_source` adds the suggested bindings), never the manifest, a cursor or a credential;
private providers the caller may not see are left out.

| Code | When |
|---|---|
| `404 UNKNOWN_BINDING` | no such binding |
| `404 UNKNOWN_QUESTION_LINK` | no such question link |
| `400 BINDING_TARGET_INVALID` | the stream's item type does not fit the target (both named), or the configuration or filter is invalid |
| `409 QUESTION_ALREADY_LINKED` | the provider question is already linked |

Tables (Hibernate, captured by prompt 144): `external_binding`, `external_binding_applied` and
`external_question_link`; app setting `external.binding.cursor`.

### Writing a provider: the kit

Specialist sources live in their own projects. The provider kit is `docs/protocol/edp-1.0/`: the JSON Schemas of the
manifest, the batch and every item type (also served at `GET /v1/external/protocol/schemas/{name}`), and
`docs/protocol/edp-1.0/examples/`, with an annotated minimal provider per item type and a Python reference provider of
about 60 lines that reads a CSV and serves observations by push and by pull. The examples are documentation, not part
of the server. Validate a manifest and sample items with `POST /v1/external/validate`. `docs/Source-Catalog.md`
("External provider ideas") lists the specialist sources Foresignal does not ship (web maps, sirens, maritime
advisories, channels, prediction markets, market data and more), each with the stream modes and item types a provider
would use and the binding that makes it useful.

## Plugins (prompt 141)

A plugin is a parser for an upstream format Foresignal does not know. It is a WebAssembly module you install in the
data directory, and it turns the bytes Foresignal fetches into records. Plugins are off by default, and nothing changes
until you install one, switch plugins on and enable it. The design, the ABI and the security model are in
`docs/Plugin-Design.md`.

Use a plugin when Foresignal can fetch the data itself (one https URL) and only the format is new. When the data needs
its own network logic (logins, scraping, many requests), write an External Data Protocol provider instead
(`docs/External-Data-Protocol.md`, and [External data providers](#external-data-providers) above).

### What a plugin can and cannot do

A plugin runs in Chicory, a WebAssembly interpreter written in Java. It sees only the bytes it is given:

- **No access to anything else.** It has no filesystem, network, clock, environment or randomness. A module that
  imports any WASI function, or any host function but `env.foresignal_log`, is refused when it loads.
- **Memory:** at most 64 MB.
- **Time:** at most 5 seconds of wall-clock time per call, after which it is interrupted.
- **Output:** at most 16 MB and 50,000 records.
- **Input:** at most 16 MB.
- **Logging:** at most 20 log lines a call and 60 a minute. Control characters are replaced with spaces.
- **No state between calls.** Every call starts a fresh instance.
- **Records go through the normal ingest path.** The normalizer checks them as it checks any source's, and a bad
  record is rejected on its own without failing the batch.

At most two plugin calls run at once.

### Installing a plugin

1. Copy the plugin into its own directory, `$FORESIGNAL_DATA_DIR/plugins/{name}/`. A plugin name is 1 to 64 lower-case
   letters, digits and hyphens, starting with a letter or digit. The directory holds:
   - `plugin.wasm`, the module (at most 32 MB);
   - `plugin.json`, the manifest:

     ```json
     {"name": "geojson-passthrough", "version": "0.1.0", "abi": 1, "kind": "parser",
      "sha256": "<sha256 of plugin.wasm>"}
     ```

   `name` must equal the directory name, `abi` must be `1` and `kind` must be `parser`. Links are refused, so copy the
   files rather than linking them.
2. Check what Foresignal sees. Discovery reads and hashes the files but never runs them:

   ```sh
   curl -H "Authorization: Bearer $ADMIN" "$FORESIGNAL_URL/v1/plugins"
   ```

   Each plugin shows its `name`, `version`, `abi`, `kind`, its declared `sha256` (the file's own hash when the manifest
   is invalid), the `actualSha256` of the file (absent when the file is missing), `hashMatches`, `enabled`, a `state`,
   the `sourceIds` that call it and, when it will not load, an `error`.
3. Compare `actualSha256` with the hash published by whoever built the plugin, or with `sha256sum plugin.wasm` on a
   copy you built yourself.
4. Switch plugins on. This is a setting, `plugins`, and it is off by default:

   ```sh
   curl -X PUT -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
     -d '{"enabled": true}' "$FORESIGNAL_URL/v1/plugins"
   ```

5. Enable the plugin with the hash you reviewed:

   ```sh
   curl -X PUT -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
     -d '{"enabled": true, "sha256": "<reviewed sha256>"}' "$FORESIGNAL_URL/v1/plugins/geojson-passthrough"
   ```

   Foresignal refuses when:
   - plugins are off: `409 PLUGINS_DISABLED`;
   - the hash is missing: `400`;
   - the file does not hash to your value, or to the one `plugin.json` declares: `409 PLUGIN_HASH_MISMATCH`;
   - the module does not load (not WebAssembly, a forbidden import, a missing export): `400`, with the reason.

The approval is bound to the hash. If `plugin.wasm` changes later, Foresignal stops running the plugin and shows
`hash-mismatch` (or `error`, when the changed module does not load either) until you review the new file and enable it
again. To disable a plugin, send `{"enabled": false}`; that works whenever the plugin's directory exists, even while
plugins are off. Switching plugins off stops every plugin at once without forgetting which ones you enabled.

**States:**

| State | Meaning |
|---|---|
| `disabled` | Not enabled, or plugins are off |
| `enabled` | Enabled, plugins on, hashes match and the module loads; it runs when a source calls it |
| `hash-mismatch` | The file does not hash to the declared hash, or to the hash you approved |
| `error` | The manifest or the module is invalid; `error` says why |

`loadedAt` is when the running process last loaded the plugin's current module, for a parse or for the check before a
fetch; it is cleared whenever the `plugins` or `plugins.approvals` setting changes.

### Defining a `wasm-feed` source

A plugin does nothing until a source calls it. A `wasm-feed` source names a plugin, an https URL and the full
descriptor of its records. Write it in a JSON file under `$FORESIGNAL_DATA_DIR/wasm-feeds/`, then restart Foresignal.
Specs are read only at startup and never accepted over the API. Each file is a JSON array of specs:

```json
[{
  "id": "wasm-stations",
  "name": "Example stations",
  "plugin": "geojson-passthrough",
  "url": "https://data.example.org/stations.geojson",
  "kinds": ["station"],
  "attribution": "Example Data",
  "license": "CC-BY-4.0",
  "observedAtMode": "RECEIVED",
  "recordTtl": "2h",
  "ingestPolicy": {"mode": "LATEST", "keyframe": "1h"},
  "interval": "10m",
  "budget": {"perMinute": 2, "perDay": 300},
  "titleField": "name",
  "tileProperties": ["name"]
}]
```

**Required members:**

- `id`: `wasm-` followed by lower-case letters, digits and hyphens, at most 64 characters, and unique.
- `name` and `plugin`.
- `url`: https, with no user name or password in it.
- `kinds`: at least one, each lower-case letters, digits and hyphens. When the source has exactly one kind, a record
  without `kind` gets it; with several kinds, a record without a matching `kind` is rejected.

**Optional members, with their defaults:**

| Member | Default | Notes |
|---|---|---|
| `historyTier` | `STANDARD` | `TRACK` needs `recordTtl` and `ttlAnchor: OBSERVED` |
| `observedAtMode` | `RECEIVED` | With `UPSTREAM`, every record must carry `observedAt` |
| `recordTtl` | none | |
| `ttlAnchor` | `RECEIVED` | |
| `ingestPolicy` | `ALL` | |
| `interval` | 15 min | |
| `minInterval` | 5 min | |
| `budget` | 4 a minute and 400 a day | |
| `retention` | 1825 days | |
| `egress` | `OPEN` | `LOCAL_ONLY` keeps the records on this instance |
| `conditional` | true | Sends `If-None-Match` and `If-Modified-Since` |
| `maxResponseBytes` | 8 MB | At most 16 MB |
| `licenseTerms` | none | Members `id`, `name`, `commercialUse`, `shareAlike`, `attributionRequired`, `redistribution`, `attributionText` and `termsUrl`. `shareAlike` and `attributionRequired` must be present; a missing `commercialUse` or `redistribution` is treated as `UNKNOWN`, the restrictive reading |

The display fields work as in every descriptor: `titleField`, `searchFields`, `embedFields`, `tileProperties`,
`volatileFields`, `significantFields` and `fieldSpecs`. Durations are written as `10m`, `2h` or `30d`. The full table
is in `docs/Plugin-Design.md` §5.

**Validation at startup.** Foresignal checks every spec when it starts, and refuses to start if any spec is wrong. The
log lists every problem with its file, position and id. Unknown members are refused, so a misspelled key is caught. The
URL must pass the outbound URL policy's `PUBLIC_ONLY` profile; a LAN host needs an exception in `allowHosts` or
`allowCidrs` (`PUT /v1/settings/outbound`). A host that does not resolve at startup is accepted and checked again
before each fetch.

**The new source** appears in `GET /v1/sources` enabled, like every free source (a `wasm-feed` source has no
credential or parameter; it fetches only while plugins are on). Disable it if you need to, set its interval and budget,
and watch its health as usual.

**Each run:**

1. The URL is checked against the outbound URL policy.
2. The plugin must be able to run. If plugins are off, or the plugin is not enabled or no longer matches its approved
   hash, the run fails with that reason before any request is made.
3. Foresignal fetches the URL through the source's budget. A 304 means "unchanged". Redirects are never followed: a
   3xx, any other status but 200, or a body over `maxResponseBytes` fails the run.
4. The plugin parses the body. A timeout, trap, oversized or malformed answer fails the run, and the health reason
   names the plugin and the limit.
5. The records go to the writer. A record the plugin got wrong is rejected on its own. Rejections are counted in the
   metric `foresignal.plugin.rejected{source, plugin, reason}` and logged at debug under `foresignal.source.{id}`.

When the plugin answers `"complete": true` and no record was rejected, the run is the upstream's whole current set,
and records that disappeared are reconciled. Otherwise the run only adds and updates.

Plugin log lines go to the logger `foresignal.plugin.{name}`.

### Problem codes and settings

**Problem codes:**

| Code | Status | When |
|---|---|---|
| `UNKNOWN_PLUGIN` | 404 | No plugin directory of that name |
| `PLUGINS_DISABLED` | 409 | You tried to enable a plugin while plugins are off |
| `PLUGIN_HASH_MISMATCH` | 409 | The reviewed or declared hash is not the file's |

**Settings:**

- `plugins`: `{"enabled": false}` by default.
- `plugins.approvals`: `{"approved": {name: {sha256, enabledAt}}}`. It is written only by `PUT /v1/plugins/{name}`.

### Building a plugin

The example is `examples/plugins/geojson-passthrough/`, written in Rust. Build it with
`cargo build --release` for the `wasm32-unknown-unknown` target. Do not use a WASI target: Foresignal refuses WASI
imports.

The ABI is short:

- **Exports:** `memory`, `alloc(size) -> ptr`, `dealloc(ptr, size)` and `foresignal_parse(ptr, len) -> out`.
- **Optional import:** `env.foresignal_log(level, ptr, len)`.
- **Return value:** a little-endian `u32` byte count followed by the UTF-8 JSON
  `{"records": [...], "complete": bool}`.

The record fields are the ones `POST /v1/ingest/{sourceId}` accepts in `{"records": [...]}`. See
`docs/Plugin-Design.md` §3.

## Streaming for frontends, tiles and asOf

**Snapshot plus stream.** A frontend that mirrors current state:

1. `GET /v1/records?source=…&order=id` returns the first page and `watermarkSeq`. Page on with
   `cursor`; the cursor pins the snapshot at that seq, so later changes are left to the stream.
2. Open `GET /v1/changes/stream?source=…&after=<watermarkSeq>` (SSE). Every persisted `change` has
   its seq as the event id, so a reconnect with `Last-Event-ID` resumes exactly where it stopped.
3. Apply an event only if its `observedAt` is not older than what you hold.

Stream parameters: `source`, `kind`, `bbox`, `live` (default true: non-persisted position updates as
`live` events, conflated to one per entity per second), `touches` (default false: `touch` events for
re-observations) and `payload` (include the payload in `change`). `/v1/events/stream` multiplexes
changes, alerts and other topics on one connection, and its filter can be changed in place with
`PATCH /v1/events/stream/{streamId}/filter`.

**When to start over.**

- `reset` (`buffer_overflow`, `replay_cap`, or a resume below the retained floor) ends the stream:
  reconnect with the `resumeAfter` it carries, or re-snapshot when it says so.
- `resync` (`gap`, `live_cap`, `live_dropped`) keeps the stream open but means live positions were
  lost: re-read current state for the affected sources.
- `GET /v1/changes?after=<seq>` answers `410 RESYNC_REQUIRED` when retention purged records after
  that seq; take a new snapshot. Routine version deletes never cause it.
- A `heartbeat` arrives every 15 s; a proxy must not buffer the stream (see the Caddy example).

**Browsers and map loaders without an `Authorization` header** use a ticket instead of putting a
token in the URL: `POST /v1/stream-tickets` (any token, `read` or higher) and append
`?ticket=fst_…` to the stream or tile URL. See [Tickets for browser clients](#tickets-for-browser-clients).

**Tiles.** `GET /v1/tiles/{kind}/{z}/{x}/{y}.mvt` serves Mapbox Vector Tiles of current state for one
kind (for example `/v1/tiles/earthquake/3/1/3.mvt`), with `agg=grid` or `agg=hex` for aggregates.
`GET /v1/tiles.json` and `/v1/tiles/{kind}.json` are TileJSON for MapLibre.

**asOf.** `GET /v1/records?…&asOf=2026-10-11T12:00:00Z` returns what Foresignal *knew* at that time
(transaction time): each entity at the version stored then, even if it was revised later. The
response echoes `asOf` and reports `horizon`, the earliest time the involved sources' retention still
supports; earlier times are `400 ASOF_BEYOND_RETENTION`. `asOfSeq` pins the same thing by seq, and
`GET /v1/records/{source}/{id}/versions` lists every stored version.

**Time travel.** The same transaction-axis replay serves paused maps (`asOf` on tiles and TileJSON),
trails (`GET /v1/records/{source}/{id}/track`), playback (`GET /v1/history/frames`) and scrubber
histograms (`GET /v1/timeline`). Past asOf tiles and tracks are sent with
`Cache-Control: …, max-age=31536000, immutable` once their instant is at least 2 minutes old, at least
1 hour after the retention horizon and not pending downsampling, so a caching proxy (Caddy, a CDN)
may keep them; `public` is used only while `openReads` covers the path and no private source is
involved. Chrome ignores `immutable` but honors the year-long `max-age`. The client recipe is in the
Client Guide ("Time travel").

## Client SDKs and the command-line client (prompt 141)

**SDKs.** `sdk/` holds client SDKs for the API in Dart (`sdk/dart/foresignal_api`), TypeScript (`sdk/typescript`) and
Python (`sdk/python`, package `foresignal_client`), generated from `docs/OpenAPI.json` with a hand-written layer for
the streams and paging described above (resume, `reset`, heartbeats, cursors), and two External Data Protocol
provider libraries, `sdk/python-edp` and `sdk/typescript-edp`. They are not part of the broker build; `sdk/README.md`
explains how to install and regenerate them.

**Command-line client.** Foresignal ships a command-line client in `cli/`, a standalone Gradle build that is not part
of the broker. Build it with `./gradlew -p cli installDist` (or `uberJar`, or `nativeCompile` with a GraalVM JDK 21),
then run `foresignal login --url https://your-instance --token fsr_…` once. The profile goes to
`~/.config/foresignal/config.json` with owner-only permissions; `FORESIGNAL_URL` and `FORESIGNAL_TOKEN` override it.
Useful operator commands:

- `foresignal sources health`;
- `foresignal sources doctor <id>`, which prints the adapter doctor's findings, and any LLM patch labeled "proposed,
  not applied";
- `foresignal changes tail --source <id>`, which resumes and re-snapshots on its own;
- `foresignal tokens create --name … --scope read`;
- `foresignal edp validate manifest.json items.ndjson`.

Exit codes: 0 success, 1 usage or invalid input, 2 API error (the ProblemDetail `code` is printed), 3 network. The
full reference is `cli/README.md`.

## Tokens, pairing and the audit log

### Tokens and scopes

Every caller authenticates with `Authorization: Bearer fsr_…`. Scopes, least to most privileged:

| Scope | Can |
|---|---|
| `read` | Every data read (needed only when `openReads` is off), stream and tile tickets, its own device report; later Ask, search and Web Push |
| `agent` | `read` plus managing triggers and briefings and the alert lifecycle; sees private sources |
| `admin` | Everything: sources, credentials, channels, LLM providers, tokens, pairing codes, settings, the audit log |

- **Create:** `POST /v1/tokens {"name": "openclaw", "scope": "agent", "expiresAt": null}` answers
  `201` with `token` once; only its SHA-256 and its first eight characters (`tokenPrefix`) are
  stored. Every token belongs to a principal: name an existing one with `principalId` (id or
  handle), or a new one named after the token is created. Prompt 130 adds the principal endpoints
  (`/v1/principals`, `POST /v1/principals/{id}/tokens`), profiles, groups and disabling; see
  [People, sharing and TLP](#people-sharing-and-tlp-prompt-130).
- **List:** `GET /v1/tokens` shows names, scopes, prefixes, `lastUsedAt` (updated at most once a
  minute), expiry, revocation, device and restrictions, never values.
- **Revoke:** `DELETE /v1/tokens/{id}`. The token stops working at once, its tickets are dropped and
  its open streams close. The `bootstrap` token cannot be revoked (`409 BOOTSTRAP_TOKEN`); rotate it
  with `POST /v1/system/admin-token/rotate`.
- **Errors:** a missing or invalid token is `401`; a token whose scope is too low is
  `403 INSUFFICIENT_SCOPE` with `requiredScope`.
- **Always authenticated:** ticket minting, the device report and, from their prompts, Ask, search,
  hypothesis drafts and Web Push need a token even while `openReads` is on, because they cost money or send messages.

### Restricted tokens

`restrictions` narrows a token further:

```json
POST /v1/tokens
{"name": "kitchen-dashboard", "scope": "read",
 "restrictions": {"endpoints": ["GET /v1/records/**", "GET /v1/tiles/**", "GET /v1/tiles.json"]}}
```

- `endpoints` (`METHOD /v1/path/pattern`, Ant style, method may be `*`): any other request is
  `403 ENDPOINT_NOT_ALLOWED`. Each pattern must match an operation the scope can reach.
- `sources`, `kinds`, `bbox` and `area` (a GeoJSON Polygon or MultiPolygon, at most 2,000 vertices)
  narrow every read: records, history, changes, tiles, streams, search, alerts, the inbox, the digest,
  briefings, MCP tools and Ask. A request filter is intersected with the restriction, and a record
  outside it answers `404`. `excludePrivate: true` hides private sources even from an agent token,
  and `maxRequestsPerMinute` sets the token's own read rate. `placeIds` (036) limits the token to
  the union of those places' areas (an unknown or deleted place lets nothing through); it is refused
  for the anonymous profile and feed keys, because places are never anonymous. The federation
  members (113) are refused until their prompt. Admin tokens cannot carry restrictions
  (`400 RESTRICTION_NOT_ALLOWED`); an admin can mint any token anyway.

See "Exposing Foresignal" below for kiosks, family phones, feed keys and limits.

### Exposing Foresignal

Before Foresignal is reachable beyond your LAN (a tailnet shared with others, a public name behind
Caddy), close the perimeter:

- **Restricted tokens for kiosks and family phones.** Give each device a `read` token restricted to
  what it shows, for example a wall map:
  `{"name":"hall-map","scope":"read","restrictions":{"kinds":["earthquake","weather-alert"],"bbox":[-80.5,43.0,-78.5,44.5]}}`.
  A lost phone then exposes only that slice, and revoking the token cuts it off.
- **The anonymous profile.** While `openReads` is on, tokenless reads run under `security.anonymous`
  (`PUT /v1/settings/security`), for example
  `{"openReads":true,"anonymous":{"kinds":["earthquake"],"maxRequestsPerMinute":60}}`. Private
  sources are never served anonymously, and the inbox, places, watchlists and every
  always-authenticated operation need a token. `GET /v1/system` warns
  (`security.warnings: ANONYMOUS_UNRESTRICTED`, also logged) when `openReads` is on, `publicBaseUrl` is
  a public name and the profile restricts nothing; turn `openReads` off or restrict the profile.
- **Feed keys for clients that cannot send headers** (KML NetworkLinks, iCal subscriptions, feed
  readers, Home Assistant `geo_json_events`, a browser `EventSource`):
  `POST /v1/feed-keys {"name":"ha-map","prefixes":["/v1/records"],"restrictions":{"kinds":["earthquake"]},"expiresAt":"2027-06-01T00:00:00Z"}`
  returns the `fsf_` key and a ready-made URL per prefix, once. The URL `/v1/k/{key}/records?…`
  serves `GET`/`HEAD` of paths under the prefixes only, inside the key's restriction, and never
  private sources. A restriction (sources, kinds or area) is mandatory and the key expires within
  365 days. `GET /v1/feed-keys` lists keys without values; `DELETE /v1/feed-keys/{id}` revokes one,
  after which its URLs answer `410`. Treat the URL as a password: responses carry
  `Referrer-Policy: no-referrer` and `X-Robots-Tag: noindex`, and are never cached publicly.
- **Tile keys for map clients** (MapLibre, maplibre_gl, Cesium, a web map's `EventSource`): an `agent`
  token mints `POST /v1/tile-keys {"name":"kitchen-tablet","prefixes":["/v1/products/","/v1/styles","/v1/changes/stream"],"ttl":"7d"}`
  and gets the `fsk_` key and its `urlBase` once. They are the same path carrier as feed keys, with map
  prefixes only (`/v1/products/`, `/v1/styles`, `/v1/media/`, `/v1/tiles/`, `/v1/maps/static`,
  `/v1/sample`, `/v1/ground-stations/`, `/v1/changes/stream`, `/v1/events/stream`, `/v1/map/`), a lifetime of at
  most 30 days, an optional restriction, and never private sources. An unknown, expired or revoked key
  answers `401 INVALID_TILE_KEY`. Mint one per device and rotate weekly; `docs/Frontend-Guide.md` has
  the client recipes. Responses through a tile key follow the normal cache rule, so content-addressed
  tiles stay cacheable (`public` only while `openReads` is on and the key has no restriction).
- **Limits.** Every client is rate-limited per minute. Defaults: read 600, tiles 1,200, streams 30
  opens and 8 open at once, expensive (search, Ask, LLM tools, sampling) 20, write 120, and
  anonymous 120 per IP (every request without a token except map tiles, which only the tiles limit
  applies to, so one map view cannot use up a browser's reads). Change them with `PUT /v1/settings/ratelimits`; they apply immediately. To
  give a kiosk or dashboard token a different read limit, set `maxRequestsPerMinute` in its
  restrictions. Admin tokens are not limited on reads and tiles. Set `metricsPerMinute` to limit
  Prometheus scrapes per IP, and `metricsOpen: false` (`PUT /v1/settings/security`) to require an
  admin token for `/actuator/prometheus`. Behind a reverse proxy, list the proxy in
  `FORESIGNAL_TRUSTED_PROXIES`; otherwise every client shares the proxy's address and its limits.
  Repeated wrong credentials from one address get `429` with a growing `Retry-After`, up to 5
  minutes, while a correct token from the same address always works. Clients should honor
  `Retry-After`.
- **Log redaction.** Feed and tile keys are credentials in the path. Foresignal's access log (off
  unless `FORESIGNAL_ACCESS_LOG=true`) logs the URI with the key cut to its first eight characters
  (`/v1/k/fsk_AbCd…/…`, the same prefix the key listings show) and without the query string. `docker/caddy/Caddyfile.example` redacts both the key and `?ticket=` values in Caddy's
  log; do the same in any other proxy that logs request lines.
- **Outbound URLs.** Channel URLs, LLM `baseUrl`s, push endpoints and other operator-supplied URLs
  are checked against the outbound URL policy on save and on every send: link-local and cloud
  metadata addresses are always refused, and LAN addresses only where the field allows them.
  `PUT /v1/settings/outbound` adds `allowHosts`/`allowCidrs` and `denyHosts`/`denyCidrs`
  overrides (deny wins).

### Pairing a phone

A phone gets its own `read` or `agent` token without ever seeing the admin token:

1. `POST /v1/pairing-codes {"name": "Pixel", "scope": "read", "ttl": "10m"}` returns `code`
   (`XXXX-XXXX`, single use, at most 1 h) and `qrPayload`
   (`foresignal://pair?u=<publicBaseUrl>&c=<code>`).
2. Show `GET /v1/pairing-codes/{id}/qr.svg` and scan it with the app, or type the code.
3. The app calls `POST /v1/pairing/claim {"code": "…", "device": {"deviceName": "Pixel", "platform": "android"}}`
   and receives its token once.

`admin` is never pairable. Codes are stored hashed and claims are limited to 5 per minute per client
IP and 50 per hour overall (`429 RATE_LIMITED`); every claim is audited. `GET /v1/pairing-codes`
lists open codes (never their values) and `DELETE` withdraws one. The QR image is held in memory, so
after a restart create a new code. Apps report their device later with
`PUT /v1/tokens/current/device`.

### Tickets for browser clients

`EventSource` and map tile loaders cannot send headers, and tokens never belong in URLs. Mint a
ticket with the token instead:

- `POST /v1/stream-tickets {"purpose": "stream"}` → `{"ticket": "fst_…", "expiresAt": …}`. Use it as
  `?ticket=` on `/v1/changes/stream`, `/v1/alerts/stream` or `/v1/events/stream`. The first connect
  must come within 60 s; `EventSource` reconnects with the same URL keep working up to 5 minutes
  after a disconnect and 24 h in total, one stream at a time (`429 STREAM_LIMIT` for a second).
- `{"purpose": "tiles", "ttl": "10m"}` gives a ticket for `/v1/tiles/**` and TileJSON, reusable for
  any number of requests until it expires (at most 1 h).

A ticket carries its token's scope and restrictions, never more; it is refused in the
`Authorization` header and on other paths, is stripped before logging, and is held in memory: after
a restart or a `401`, mint a new one.

### Trusted proxies

Behind Tailscale Serve or Caddy, Foresignal sees the proxy's address. Tomcat honors
`X-Forwarded-For` and `X-Forwarded-Proto` only from addresses matching `FORESIGNAL_TRUSTED_PROXIES`
(a regular expression, or a comma-separated CIDR list). The default trusts loopback only
(`127\.0\.0\.1|0:0:0:0:0:0:0:1|::1`), which is right for a proxy on the host
(`tailscale serve --bg 8080`, Caddy on the host). A proxy in another container reaches Foresignal
from the Docker network, so trust that network, for example
`FORESIGNAL_TRUSTED_PROXIES='172\.(1[6-9]|2[0-9]|3[01])\.\d{1,3}\.\d{1,3}'` for the compose
`tailscale` and `web` profiles. Never trust addresses a client can reach directly: a client could then forge its
IP. The resolved client IP is what the audit log and the pairing limits use.

### Deep links (`clientLinks`)

`PUT /v1/settings/server` also sets `timezone` and `clientLinks`, the templates apps and
notifications use: `alert` (`foresignal://alerts/{id}`), `record`, `briefingRun` and `pair`. Templates
must use `foresignal://` or an `https://` link you have verified for the app. Once you set
`clientLinks`, alert notifications carry an "Open in app" link next to "Open alert".

### The audit log

`GET /v1/audit` (admin) lists, newest first: every authenticated write with its token, scope,
operation, status, target and client IP; every `401`/`403` on a write path; pairing claims; token
revocations. Filter with `since`, `until`, `tokenId`, `operationId` and `status`, and page with
`cursor` and `limit` (up to 500). Entries never contain secrets, token values or request bodies.
They are kept for `retention.audit` (default 90 days). Writing the log never slows a request: under
extreme load entries are dropped and counted (`foresignal.audit.dropped`).

## Remote MCP with your identity provider (prompt 141)

Foresignal's MCP server at `/mcp` normally takes the same Foresignal API tokens as REST (`fsr_…`, see
[Tokens and scopes](#tokens-and-scopes)). That works for local agents. A remote MCP client such as Claude, which signs
the user in through a browser, wants OAuth: it discovers where to log in, gets an access token and presents it.
Foresignal supports this by **delegating to your own identity provider**. Foresignal runs no authorization server and
no device grant. It publishes RFC 9728 metadata that names your identity provider, then verifies the access tokens
that provider issues. The agent's side of this is in the Agent Guide, "Remote MCP authorization".

### How it works

1. The MCP client calls `POST /mcp` without a token and gets `401` with
   `WWW-Authenticate: Bearer resource_metadata="https://foresignal.example/.well-known/oauth-protected-resource/mcp", scope="foresignal:read"`.
2. It reads that document (open, no token). The document's `authorization_servers` lists your issuer, and
   `resource` is the canonical resource URI (by default `{server.publicBaseUrl}/mcp`).
3. It signs the user in at your identity provider, requests the scopes it needs, and passes
   `resource=https://foresignal.example/mcp` (RFC 8707).
4. It calls `/mcp` with `Authorization: Bearer <access token>`. Foresignal checks the token and accepts it when all
   of these hold:
   - the signature verifies against the provider's JWKS;
   - `iss` equals `mcp.auth.issuer` exactly;
   - `aud` contains the audience;
   - `exp` and `nbf` hold, with 60 s of clock skew;
   - `alg` is in `mcp.auth.allowedAlgs`.
5. The token's scopes map to a Foresignal scope through `mcp.auth.scopeMap`, and that scope decides which tools the
   client sees and may call, exactly as for a token of that scope.
   - `admin` is **never** mappable: a remote MCP client can at most reach `agent` tools.
6. When a tool needs more than the token carries, the call gets `403` with
   `WWW-Authenticate: Bearer error="insufficient_scope", scope="foresignal:agent", resource_metadata="…"`. The client
   can then ask for the wider scope and retry.

The token is verified locally and never forwarded anywhere. Every identity-provider subject gets its own Foresignal
principal (handle `idp-` followed by a hash of the issuer and subject), created on first use. Audit rows read
`token:oauth:<name>`, where `<name>` is the token's `name`, `preferred_username`, `email` or subject claim. Disabling
that principal (`PATCH /v1/principals/{principalId}`) shuts the person out even while their tokens are still valid.

REST is unaffected: identity-provider tokens are accepted only on `/mcp`. On every other path such a token is not a
Foresignal token, so an operation that needs a token answers `401`. Keep using Foresignal tokens there.

### Settings `mcp.auth`

`GET /v1/settings/mcp-auth` reads the settings and `PUT /v1/settings/mcp-auth` replaces them. Both are admin-only.
Members:

| Member | Meaning |
|---|---|
| `mode` | `token` (default): Foresignal tokens only, open reads apply as on REST. `oauth`: identity-provider tokens only. `both`: either. With `oauth` or `both`, `/mcp` **always** needs a credential, so clients get the `401` that starts discovery. |
| `issuer` | Your provider's issuer identifier, exactly as it appears in `iss`, including any trailing slash. Required for `oauth` and `both`. |
| `audience` | The value tokens must carry in `aud`. Default `{server.publicBaseUrl}/mcp`. Required when `publicBaseUrl` is not set. |
| `jwksUri` | Optional. When empty, Foresignal discovers it from the issuer: first RFC 8414 (`https://host/.well-known/oauth-authorization-server/<issuer path>`), then OpenID Connect (`<issuer>/.well-known/openid-configuration`); a trailing slash of the issuer is dropped before either path is added. A discovery document is trusted only when its `issuer` equals yours. |
| `scopeMap` | Identity-provider scope → `read` or `agent`. Default `{"foresignal:read":"read","foresignal:agent":"agent"}`. `admin` is refused with `400`. |
| `scopeClaim` | `scope` (default; a space-separated string) or `scp`. Arrays and strings are read from either claim. |
| `allowedAlgs` | Any of `RS256`, `ES256`, `EdDSA`, `Ed25519` (default: all four). `none` and HMAC algorithms are never accepted. |

The issuer and JWKS URLs are fetched through the outbound URL policy (`LAN_ALLOWED`), so an identity provider on
your LAN or tailnet works. Plain `http` is accepted only when the host resolves to a loopback, private or tailnet
address; anything public needs `https`. Keys are cached for an hour. A token naming an unknown `kid` triggers at
most one refetch per minute, so after rotating keys, new tokens work within a minute.

Example, with `server.publicBaseUrl` set to `https://foresignal.example`:

```http
PUT /v1/settings/mcp-auth
Authorization: Bearer fsr_…admin…

{"mode": "both", "issuer": "https://id.example/realms/home"}
```

Then check `GET /.well-known/oauth-protected-resource/mcp` (the same document is also served at
`/.well-known/oauth-protected-resource`). It answers `404 MCP_OAUTH_DISABLED` while `mode` is `token`.

Choose `both` while you migrate, so existing agents with Foresignal tokens keep working. Choose `oauth` when every
MCP client signs in through the identity provider.

### Common setup at any identity provider

Do the same four things everywhere:

1. **Scopes.** Create the scopes `foresignal:read` and `foresignal:agent`. You can use other names if you change
   `scopeMap` to match. Grant `foresignal:agent` only to people who may create and change watches and alerts.
2. **Audience.** Make access tokens for Foresignal carry `aud` = `https://foresignal.example/mcp`, the value of
   `resource` in the metadata.
   - If your provider honors the RFC 8707 `resource` parameter, the MCP client's request sets it.
   - Otherwise, configure a fixed audience on the client or scope.
   - If your provider can only put the client ID in `aud`, set `mcp.auth.audience` to that client ID. This is weaker,
     because any token for that client is accepted, but it works.
3. **JWT access tokens.** Foresignal verifies JWTs. Opaque access tokens, which would need introspection, are not
   supported.
4. **A client for the MCP client.** Register a public client: authorization code with PKCE, and the redirect URI
   your MCP client documents. Foresignal never sees this client; it only checks the tokens.

### Keycloak

- Use the realm that holds your users. The issuer is `https://<host>/realms/<realm>`, and OpenID Connect discovery
  finds the JWKS, so leave `jwksUri` empty.
- **Client scopes:** create `foresignal:read` and `foresignal:agent` as *Optional* client scopes, type OpenID Connect,
  with "Include in token scope" on, so they appear in the `scope` claim. **(verify)**
- **Audience:** in each of these client scopes, add an *Audience* mapper with "Included Custom Audience" =
  `https://foresignal.example/mcp` and "Add to access token" on. **(verify)** Older Keycloak releases ignore the
  RFC 8707 `resource` parameter, so the fixed audience mapper is the dependable route. **(verify the release your
  instance runs)**
- **Client:** a public OpenID Connect client with "Standard flow" and PKCE (S256), whose valid redirect URIs are your
  MCP client's callback. Add the two client scopes to it as optional scopes. **(verify)**
- To keep `foresignal:agent` from some users, give the scope a role scope mapping or guard it with a client policy.
  **(verify)**
- Keycloak signs with RS256 by default; `allowedAlgs` can stay at its default.

### Authentik

- Create an *OAuth2/OpenID Provider* and an *Application* for it. The issuer is the provider's "OpenID Configuration
  Issuer", usually `https://<host>/application/o/<slug>/` **with the trailing slash**. Copy it exactly. **(verify)**
- **Scopes:** add two *Scope Mappings* (Customization → Property Mappings) with scope names `foresignal:read` and
  `foresignal:agent`, and select them on the provider. **(verify)** To restrict `foresignal:agent`, have that
  mapping's expression check group membership. **(verify)**
- **Audience:** Authentik puts the provider's client ID in `aud` **(verify)**, so either:
  - set `mcp.auth.audience` to that client ID; or
  - add `https://foresignal.example/mcp` to `aud` through a scope mapping, if your version allows it. **(verify)**
- Use client type *Public*, with your MCP client's redirect URI. Pick an RS256 or ES256 signing key; Foresignal
  accepts both.

### Pocket ID

Pocket ID is a passkey-only OpenID Connect provider.

- The issuer is your Pocket ID URL, and OpenID Connect discovery finds the JWKS.
- Create an OIDC client for the MCP client, public with PKCE, using the MCP client's callback URL. **(verify)**
- Pocket ID does not obviously offer custom scopes or a configurable access-token audience. **(verify against its
  documentation for your version)** If it does not:
  - set `mcp.auth.audience` to the client ID (if that is what its access tokens carry in `aud`);
  - map a scope it always issues, for example `scopeMap: {"openid": "read"}`.

  Every signed-in user of that client then gets `read`. Keep `agent` for a provider that can issue a separate
  scope, or for Foresignal tokens in `both` mode.
- Use Pocket ID's user groups and "allowed groups" on the client to limit who can sign in at all. **(verify)**

### Auth0

- Create an *API* (Applications → APIs) whose **Identifier** is `https://foresignal.example/mcp`. Auth0 uses this
  value as the access token's `aud`. Clients name it with Auth0's `audience` parameter; some tenants also accept the
  RFC 8707 `resource` parameter. **(verify)**
- Under the API's *Permissions*, add `foresignal:read` and `foresignal:agent`. They appear in the token's `scope`
  claim. With RBAC and "Add Permissions in the Access Token" on, they also appear in `permissions`, which Foresignal
  does not read; keep `scopeClaim: "scope"`. **(verify)**
- The issuer is `https://<tenant>.<region>.auth0.com/` **with the trailing slash**, or your custom domain. Auth0
  serves OpenID Connect discovery there. Auth0 signs access tokens with RS256 by default. **(verify)**
- Register the MCP client as a *Single Page* or *Native* application with your MCP client's callback, or enable
  Dynamic Client Registration if the client depends on it. **(verify)**

### Connecting Claude and other MCP clients

- In the MCP client, add a remote (streamable HTTP) server with URL `https://foresignal.example/mcp`. The URL must be
  reachable from wherever the client runs. A cloud-hosted client such as Claude on the web needs a public `https`
  name, which usually means a reverse proxy in front of Foresignal with `server.publicBaseUrl` set to that name.
- On the first call the client gets the `401`, reads the metadata, opens your provider's login page and stores the
  token. Nothing is configured in Foresignal per client.
- If your provider does not support Dynamic Client Registration, enter the client ID (and secret, for a confidential
  client) you registered above in the MCP client's connection settings, if it offers them. **(verify per client)**
- Clients that send no `resource` parameter still work, as long as the provider puts the expected audience in the
  token (fixed audience above).
- Local agents and scripts can keep using a Foresignal token (`Authorization: Bearer fsr_…`) with `mode` `both`.

### Troubleshooting

The `401` problem body names what failed, as `The access token is not valid here: <detail>`. The
`WWW-Authenticate` challenge carries only `error="invalid_token"`, `resource_metadata` and `scope`, never the reason.

| Detail | Cause |
|---|---|
| `the issuer does not match` | `iss` differs from `mcp.auth.issuer`, often a trailing slash or http/https |
| `the token is not for this resource (aud does not contain …)` | Audience not configured at the provider, or `mcp.auth.audience` differs |
| `no audience is configured (set mcp.auth.audience or server.publicBaseUrl)` | Neither setting is set |
| `the algorithm … is not allowed` | Widen `allowedAlgs`, or change the provider's signing key |
| `the key … is not among the identity provider's keys` | Rotation within the last minute, or the wrong `jwksUri` |
| `no key of the identity provider fits the token` | The token names no `kid` and no key of the JWKS fits it |
| `the signature does not verify` | The token was not signed by the provider's current keys |
| `the identity provider's keys could not be loaded: …` | The text after the colon says why: the URL was refused by the outbound URL policy, the provider is down, or discovery failed (including a discovery document that names another issuer); the log repeats it |
| `the token has expired` / `the token is not valid yet` | Clocks more than 60 s apart, or the token really has expired |
| `the token has no exp` | The provider issues tokens without an expiry; Foresignal refuses them |
| `the token names no subject (sub, client_id or azp)` | The token carries no subject Foresignal can map to a principal |
| `the token is not a signed JWT` | An opaque or unsigned token (see "JWT access tokens" above) |
| `the principal of this subject is disabled` | The person's principal was disabled |

`403` with `insufficient_scope` means the token is valid but its scopes do not reach the tool, or map to nothing.
The `scope` in the challenge names what to request.

## LLM

An LLM is optional. Without one, every other feature works exactly as before: briefings use
digest mode, search matches text, enrichment is skipped and Ask answers `409 LLM_NOT_CONFIGURED`.
With one, Foresignal can answer questions about its data (Ask), write briefings, enrich alerts and
search by meaning. Every LLM endpoint needs the `admin` scope.

### Providers and roles

A provider is one model API. Three types exist:

| Type | What it talks to | Defaults |
|---|---|---|
| `anthropic` | Claude, through the official Anthropic Java SDK | `baseUrl` `https://api.anthropic.com`, `chatModel` `claude-opus-5-5`, effort `medium`, `maxTokens` 16000. An `apiKey` is required. No embeddings. |
| `openai-compatible` | OpenAI, OpenRouter, Groq, Mistral, vLLM, LM Studio, llama.cpp server, LocalAI | `baseUrl` is required (for example `https://api.openai.com/v1`) |
| `ollama` | A local Ollama server | `baseUrl` `http://ollama:11434` |

Two roles use providers: `chat` (Ask, briefings, enrichment) and `embedding` (search). Assign them
with `PUT /v1/llm/settings`. A provider needs a `chatModel` for the chat role, and an
`embeddingModel` plus `embeddingDimensions` for the embedding role. Anthropic has no embeddings
endpoint, so pair a Claude chat provider with an Ollama or OpenAI-compatible embedding provider.
A provider that holds a role cannot be deleted (`409 LLM_PROVIDER_IN_USE`) or changed so that it
can no longer serve it; reassign the role first.

`baseUrl` must be `http` or `https` with no user name, password or fragment. Addresses on your own
network are allowed, because a local model is the point.

### A local model (nothing leaves the box)

```sh
docker compose --profile local-llm up -d
docker compose exec ollama ollama pull qwen3:14b
docker compose exec ollama ollama pull nomic-embed-text
```

Then add the provider and assign both roles:

```sh
curl -X POST http://localhost:8080/v1/llm/providers -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"id":"ollama","type":"ollama","chatModel":"qwen3:14b",
  "embeddingModel":"nomic-embed-text","embeddingDimensions":768}'
curl -X PUT http://localhost:8080/v1/llm/settings -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"chatProvider":"ollama","embeddingProvider":"ollama"}'
curl -X POST http://localhost:8080/v1/llm/providers/ollama/test -H "Authorization: Bearer $TOKEN"
```

The Ollama port is bound to `FORESIGNAL_BIND` (localhost by default), like the other services.
If the chat model does not support tools, the provider shows `supportsTools: false` and Ask
answers from retrieved records instead of calling tools.

### Claude

Create an API key in the Anthropic Console (<https://console.anthropic.com>), then:

```sh
curl -X POST http://localhost:8080/v1/llm/providers -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"id":"claude","type":"anthropic","apiKey":"sk-ant-..."}'
```

The key is encrypted with the install key and never returned (`apiKeySet: true`). To change
anything else later, send the provider without `apiKey` and the stored key is kept.

Foresignal sets the effort explicitly for each use: Ask and briefings `medium`, enrichment `low`.
`options.effort` on the provider overrides that for every call (`low`, `medium`, `high`, `xhigh`,
`max`); higher effort spends more tokens. Calls enable Anthropic's server-side refusal fallback, and
a refusal is reported as such (the test endpoint shows `refusalCategory`). The stable system prompt
and tool definitions are cached by Anthropic, so repeated Ask and briefing calls are cheaper; the
`cachedTokens` figures show how much was served from the cache.

### Budgets and costs

Every provider has `requestsPerMinute` (default 30) and `tokensPerDay` (default 2,000,000, counted
as input plus output tokens and reset at 00:00 UTC). Before each call Foresignal estimates its
tokens (prompt characters / 4, plus the output cap) and refuses the call with
`429 LLM_BUDGET_EXCEEDED` when the estimate does not fit in what is left today. A refused call is
never sent. The first daily-budget refusal of a day, and a provider that fails three calls in a row, raise a
`system` notification. Restarting does not reset the day: it is seeded from the recorded usage.

`GET /v1/llm/usage?provider=&from=&to=&resolution=` shows requests, input, output and cached tokens,
errors and refusals per minute, hour or day; `GET /v1/llm/providers` shows each provider's
`tokensToday`, health (`status`, `lastError`, `consecutiveFailures`) and roles; `/v1/system` shows
the `llm` block. To estimate money, multiply the tokens by your provider's prices; the test
endpoint itself spends a few tokens.

Every call is logged with its purpose, provider, model, token counts (cached included), duration
and stop reason. Prompts and responses are logged only when `logContent` is `true` in
`PUT /v1/llm/settings`, at DEBUG level. API keys are never logged.

### What leaves the box

With a frontier provider (Claude, OpenAI, or any remote OpenAI-compatible API), the records the
model reads to answer are sent to that provider. That is your choice to make. Records of private
sources (personal data such as family locations) are withheld from remote providers and sent only
to a local one: an `ollama` provider, or one you mark with `options.local: true` because it runs on
your own network. The `local-llm` profile keeps everything on this machine.

## Search

`GET /v1/search?q=…` finds records (and, once those prompts land, briefing runs and enriched
alerts) by words, by meaning, or both. It needs a token even when open reads are on, because a
semantic query spends the embedding budget.

| `mode` | How it ranks | Needs |
|---|---|---|
| `text` | Full-text match on the embedded text (`websearch_to_tsquery`), plus a substring match on each record's search fields for records that have no document | Nothing; always works |
| `semantic` | Cosine distance between the embedded query and each document | pgvector, an embedding provider and a built index; otherwise `409 SEMANTIC_UNAVAILABLE` |
| `hybrid` (default) | Reciprocal rank fusion of both | Falls back to text with `degraded: true` and a `reason` when semantic search is off |

Filters: `source`, `kind`, `docType`, `bbox`, and `since`/`until` on the effective time (event time
when the record has one, else observation time; `timeField=observed` for the observation time).
Record hits carry the persisted `seq` to cite and the current record. Private sources are never
embedded, and their records only appear in text results for callers allowed to see them. MCP
clients get the same search as the cost-bearing `search` tool.

### The database image

Semantic search stores vectors with the pgvector extension in the same PostgreSQL. The compose
`db` service builds `docker/db` (image `foresignal-db:17-3.5-pgvector`): the pinned
`postgis/postgis:17-3.5` plus `postgresql-17-pgvector` from the PostgreSQL apt repository. Existing
data volumes keep working; the first start after the switch runs `CREATE EXTENSION vector`.

```sh
docker compose build db && docker compose up -d
```

Without pgvector (an external PostgreSQL, or the plain PostGIS image) Foresignal starts normally,
text search works, and `GET /v1/search/status` says why semantic search is off.

### Choosing an embedding model

Assign an embedding provider in `PUT /v1/llm/settings` (see "LLM"). A local Ollama with
`nomic-embed-text` (768 dimensions) keeps everything on this machine; an OpenAI-compatible
provider works the same way. The first assignment builds the index from the current records;
until it finishes, `hybrid` answers from text and `semantic` is `409`.

Only sources that declare `embedFields` (text-heavy feeds such as alerts, warnings and notices) are
embedded. Each record is embedded once per text change: unchanged text is skipped without calling
the model, and new versions are batched 64 texts per call within the provider's budget. When the
budget runs out the worker pauses until the next minute and resumes where it stopped. Retention
waits for the worker (`embedding.cursor`), so a long pause holds old versions back until the clamp
is released at twice the window.

### Changing the model

Changing the embedding model (or `POST /v1/search/reindex`, which also rebuilds with the same
model) never takes search offline. The new index is built in the second vector slot while searches
keep using the old one, with queries embedded by the old model. New records are written to both.
When every document has the new vector, the new index is built concurrently and swapped in; the
old slot is dropped. `GET /v1/search/status` shows the progress:

```json
"reindex": {"state": "backfilling", "fromModel": "ollama:nomic-embed-text:768",
            "toModel": "openai:text-embedding-3-small:1536", "total": 120000, "done": 45000,
            "percent": 37.5, "ratePerMinute": 1900.0, "etaSeconds": 2368, "startedAt": "…"}
```

The backfill embeds every embeddable record again, so it costs one embedding per record (about
`total × average text tokens`; texts are capped at 2,000 characters). A restart resumes from where
it stopped. `DELETE /v1/search/reindex` cancels and discards the new slot. If the old provider is
deleted or loses the old model during the rebuild, searches answer from text (`degraded: true`)
until the swap.

### The `embed` flag

Each source has an `embed` setting (`PUT /v1/sources/{id}` with `{"embed": false}`), on by default
for sources that declare `embedFields`. Turning it off stops embedding the source and deletes its
documents. Turning it back on embeds new versions; run `POST /v1/search/reindex` to embed the
records it already has.

## Briefings

A briefing is a scheduled summary ("morning brief", "weekly maritime summary") delivered to your
channels. It needs no LLM: the default `digest` mode is deterministic markdown. With a chat provider
assigned, `llm` mode writes a cited narrative from the same data instead.

```json
POST /v1/briefings
{"name": "Morning brief", "schedule": "0 7 * * *", "timezone": "America/Chicago",
 "scope": {"kinds": ["earthquake"], "bbox": "-125,24,-66,50", "ruleIds": [], "includeHealth": true},
 "lookback": "24h", "mode": "digest", "channelIds": ["…"], "maxItemsPerKind": 5}
```

- **Schedule.** A standard 5-field cron (`minute hour day-of-month month day-of-week`) in the
  briefing's IANA `timezone` (default `UTC`), so `0 7 * * *` stays at 07:00 local time across DST
  changes. Briefings may run at most every 15 minutes. The scheduler checks every 30 s; runs missed
  while Foresignal was down run once when it is back, never once per missed slot.
- **Period.** `lookback` (1h to 7d) back from the run; without one, since the previous run (capped at
  7 days), or 24 hours for the first run.
- **Scope.** `sources`, `kinds` and `bbox` limit the records; `ruleIds` limits the alert section;
  `includeHealth` adds the source health section. Empty lists mean everything. `places` (prompt 056) adds
  "Weather at places" for those places and `weatherProducts` picks its products; see
  [Alert weather context](#alert-weather-context).
- **Channels.** The run is published as a `briefing` notification to `channelIds`, or to every
  channel subscribed to `briefing` when the list is empty. A channel that does not exist, is disabled
  or is not subscribed to `briefing` is reported in `warnings` when you save the briefing; it is not
  an error. The message links to the HTML rendition ("Read more").

### What a digest contains

- **Alerts** in the period, critical first, with counts per severity and state.
- **Activity**, per kind: versions that are `new` (first seen or reappeared), `changed` and
  `removed`, entities `active` in the period (including ones only re-observed, never re-versioned),
  and the delta against the previous period of the same length. Routine versions (samples,
  keyframes, heartbeats) are listed separately and never counted as change.
- **Notable** records per kind, ranked by the source's `rankField` (for example magnitude), else by
  time, each linked to `publicBaseUrl/v1/records/{source}/{externalId}`. Set `publicBaseUrl` in
  `PUT /v1/settings/server` so links work outside the box.
- **Source health**: sources currently degraded or failing (Foresignal does not keep a history of
  health changes yet, so this is the state at run time).
- A **"Counts incomplete (retention)"** note when the period reaches back past what retention kept.

Private sources are counted but their records are not listed, unless the briefing was created or
last saved with the admin token.

### LLM mode

`mode: "llm"` with `instructions` (focus, length, tone; up to 4,000 characters). The model gets the
digest statistics, the notable records and, when semantic search is available, a search on your
instructions within the scope, and must cite `[source:externalId@seq]`. Citations are verified as in
Ask; problems go to the run's `warnings`. If the model fails, refuses, or the LLM budget is spent,
the run falls back to the digest with status `fallback` and the reason in `error`. Records of private
sources never go to a non-local provider. Usage is recorded per run.

### Runs

- `POST /v1/briefings/{id}/run` runs now (agent token) and returns the run, waiting up to 120 s; a
  second run while one is going is `409 BRIEFING_RUNNING`. `POST /v1/briefings/preview` renders a
  definition without storing, delivering or scheduling anything.
- `GET /v1/briefings/{id}/runs` lists runs, newest first; `GET /v1/briefings/{id}/runs/latest` and
  `/runs/{runId}` return one as JSON, or as a self-contained HTML page with `Accept: text/html` or
  `?format=html`. The page is sanitized (no scripts, styles attributes or frames survive, even from
  record titles), follows the browser's light or dark theme, and is served with a strict
  Content-Security-Policy.
- Runs are kept for the `briefings` retention (default 180 days), even after the briefing is
  deleted, and are searchable with `docType=briefing` when semantic search is on. MCP agents use
  `list_briefings`, `get_briefing_run`, `create_briefing` and `run_briefing`.
- With a speech provider and `audio` set on the briefing, each run is also read aloud and listed in a podcast
  feed: see [Audio briefings and translation](#audio-briefings-and-translation-prompt-141).

## Alert enrichment

With a chat provider assigned, a rule can ask for a short summary of what Foresignal knows around each
of its alerts. Set `enrich: true` on the rule, optionally with `enrichRadiusKm` (1 to 1,000, default
100) and `enrichWindow` (10m to 7d, default 24h):

```json
PUT /v1/triggers/{ruleId}
{"name": "Big quakes", "sources": ["usgs-earthquakes"], "conditions": [{"path": "mag", "op": ">=", "value": 5}],
 "enrich": true, "enrichRadiusKm": 150, "enrichWindow": "12h"}
```

`enrich: true` is accepted without a provider: it is stored and enrichment is skipped until a chat
provider is assigned. `PUT /v1/llm/settings` with `"enrichmentEnabled": false` switches enrichment off
everywhere. `testTrigger` reports `"enrich": true` without calling the model.

**What the model reads.** Around the alert's observation time `T`, within the radius and `T ±
enrichWindow`: the record and up to 10 of its earlier versions, records of any kind nearby (also ones
that only moved, without a new version), up to 10 nearby alerts and, when semantic search is on, the
5 best search matches for the alert message. A record without a location gets its history and the
search matches only. The context is capped at 24 KB; the least relevant items are left out first.

**What it writes.** At most about 120 words for the operator: what happened, what is nearby or
related, and why it might matter, citing records as `[source:externalId@seq]` and alerts as
`[alert:id]`. Citations are checked against what the model was given and the ones it invented are
removed. "No notable related activity" means it found nothing relevant. The summary appears as
`summary`, `enrichedAt` and `summaryCitations` on `GET /v1/alerts/{id}` and in lists, in MCP
`get_alert`, and as an `alert.enriched` event on `/v1/alerts/stream` and the `alerts` topic of
`/v1/events/stream` (the full alert; treat it as an update of the alert with the same id).

**Waiting or editing, per channel.** Enrichment never holds a message back longer than the channel
allows:

| Channel | What happens |
|---|---|
| Any type with `waitForEnrichmentSeconds` > 0 (up to 60) | The alert waits up to that long. When the summary arrives the message is sent at once, with it; if it does not arrive in time, the message goes out without it. |
| `discord`, `telegram`, `matrix` with `editInPlace` on (the default) | A message already sent without the summary is edited to add it, if it is less than an hour old. |
| Every other type, wait 0 | The message goes out at once without the summary, and nothing more is sent: no follow-up message. |

`{{summary}}` is in the default body; a custom `bodyTemplate` must include it to show the summary.
Slack and Mattermost incoming webhooks cannot edit messages, so give those channels a wait (10 to 20
seconds is usually enough) if you want the summary in them.

**Cost per alert.** One call per enriched alert, never retried: up to about 6,000 input tokens (the
24 KB context plus a fixed system prompt that providers can cache) and at most 600 output tokens, at
effort `low`. A rule that fires often multiplies that; the LLM budget refuses calls once the day's
tokens are spent, and those alerts go out without a summary. Alerts older than 10 minutes (catch-up
after downtime) are not enriched. Usage shows under purpose `enrichment` in `GET /v1/llm/usage`, and
`foresignal.enrichment.succeeded`, `.failed{reason}`, `.skipped{reason}`, `.dropped` and `.duration`
are exported as metrics.

**What leaves the box.** The context goes to the chat provider. Records of private sources are left
out for a remote provider and sent only to a local one, and an alert on a private source is not
enriched at all by a remote provider. Record text is treated as data: instructions hidden in a record
are not followed, and the model is offered no tools, so it cannot change anything.

## Audio briefings and translation (prompt 141)

Both features are optional and stay inert until you configure them. Without a `tts` provider no briefing is
synthesized, and without a channel `language` nothing is translated. The broker behaves exactly as before.

### Audio briefings

A [briefing](#briefings) can be read aloud after each run. Foresignal sends the run's plain-text rendition to a speech
endpoint, stores the audio in the media store, attaches it to the delivery where the channel can carry audio, and
lists it in a podcast feed.

**1. Add a speech provider.** Any OpenAI-compatible `/v1/audio/speech` endpoint works: OpenAI itself, or a local
server such as Kokoro-FastAPI or openedai-speech. Create it as an `openai-compatible` provider. Its `baseUrl` ends
in `/v1`, as it does for chat, and Foresignal appends `/audio/speech`:

```http
POST /v1/llm/providers
{
  "id": "speech",
  "type": "openai-compatible",
  "baseUrl": "http://kokoro:8880/v1",
  "options": { "ttsModel": "kokoro", "ttsCharactersPerDay": 200000 }
}
```

- `options.ttsModel` is sent as `model`. The default is `tts-1`, OpenAI's model name.
- `options.ttsCharactersPerDay` is the daily budget, counted in characters per UTC day. The default is 100,000.
  Speech is priced and limited by input characters, so it does not use the provider's token budget. Each request
  still counts toward the provider's `requestsPerMinute`. The day's count is kept in memory, so a restart sets it back
  to zero.
- A cloud endpoint needs `apiKey`. A local server usually needs none.

**2. Assign the `tts` role:** `PUT /v1/llm/settings` with `"ttsProvider": "speech"`. Keep your other settings in
the body, because the PUT replaces them. The provider must be enabled and `openai-compatible`; Anthropic and
Ollama have no speech endpoint. While it holds the role it shows `"tts"` in `roles`, and it cannot be deleted.

**3. Turn audio on for a briefing:** set `"audio": {"voice": "af_bella", "format": "mp3"}` on create or replace.
The voice name belongs to the provider. OpenAI uses `alloy`, `nova` and so on; Kokoro uses `af_bella`, `am_adam`
and so on. The format is `mp3` (the default) or `opus`.

**What happens after a run:**

- The text is the run's notification title (the briefing's name and date) followed by the run rendered as plain
  text. Link targets and URLs are dropped, and table rows are read as comma-separated cells.
- One request carries at most 4,000 characters.
  - **MP3:** longer text is split at paragraph or sentence boundaries and the parts are joined into one file, up
    to 25 requests (about 100,000 characters).
  - **Opus:** Ogg streams cannot be joined, so the text is cut to a single request.
  - Text that is cut ends with a spoken note: "The audio ends here; the full briefing is in the text."
- The audio is stored as media with `role=audio` (`audio/mpeg` or `audio/ogg`). It expires with the briefing-run
  retention (`retention.briefings`, 180 days by default).
- It is signed like the run's markdown: a detached JWS in `audio.signature`, verifiable against
  `/.well-known/jwks.json` (see [Licenses and egress](#licenses-and-egress-prompt-109), "Signing").
- The run's `audio` object records the outcome:
  - `status`: `ok`, `failed` or `skipped`
  - `mediaId`, `format`, `voice`, `characters`, `truncated` and `error`
  - `bytes`, `contentType` and `signature`, only when the status is `ok`
- **A speech failure never holds back the text.** A provider error, a spent budget or a storage problem sets
  `status: failed` with the reason, and the briefing is delivered as usual. Without a `tts` provider, or with nothing
  to say, the status is `skipped`.

**Delivery:**

| Channel | Audio |
|---|---|
| Telegram | `sendAudio` for MP3, or `sendVoice` for Opus (shown as a voice message), with the briefing as the caption. When the text does not fit the caption, the caption is the title and the full text follows as a message. Up to 50 MB. |
| Matrix | The text event as usual, then the file is uploaded to the homeserver media repository and posted as an `m.audio` event. Up to 50 MB (Synapse's default `max_upload_size`). A failed upload or `m.audio` post is only logged; no link is sent then. |
| Email | A file attachment, in plain-text mode too. Up to 18 MB. |
| ntfy | An attachment. The server needs attachments enabled; a refusal sends the text with a link instead. Up to 15 MB (ntfy's default). |
| Every other type | A `[Listen](…)` link to `{publicBaseUrl}/v1/media/{id}` at the end of the body. |

A file over the channel's limit also gets the link.

**Podcast feed.** `GET /v1/briefings/{id}/podcast.rss` is an RSS 2.0 feed of the briefing's runs with audio,
newest first, at most 50 items. Each item has an `<enclosure>` pointing at the media. It needs `read`, or no token
while `security.openReads` is on.

Most podcast apps cannot send headers, so give them a feed key. Create the key with prefixes for both the feed and
the media, for example `["/v1/briefings/<briefing id>/podcast.rss", "/v1/media/"]`, and the restriction every feed
key needs (at least one of `sources`, `kinds`, `area` or `bbox`). Subscribe to
`https://…/v1/k/{key}/briefings/{id}/podcast.rss`; the enclosure URLs in that feed carry the same key.

### Notification translation

Notifications are written in `notify.defaultLanguage`, which is `defaultLanguage` in `PUT /v1/settings/notifications`
and defaults to `en`. Give a channel a `language` (a BCP 47 tag such as `de`, `fr-CA` or `pt-BR`, next to its other
common fields in [Notification channels](#notification-channels)). When that language differs from the default, each
message's title and body are translated before they are sent to that channel.

- **Which channels:** two tags count as the same language when their primary language and script match. `en-US`
  on an English instance is not translated; `zh-Hant` and `zh-Hans` are different languages, and so are a bare `zh`
  and `zh-Hans`.
- **How:** through the chat provider (the LLM `chatProvider` role) at low effort. Calls are logged and budgeted
  under the purpose `translate` and spend the chat provider's tokens.
- **What is never translated:**
  - links, tags, severity and the event data, which are never sent to the model;
  - code spans and code blocks, where templates put record values, URLs (link targets included) and template
    placeholders, which are replaced by numbered tokens before the text goes to the model and put back afterwards.
- **Safety:** the system prompt tells the model that the content is untrusted text to translate, never an
  instruction. Foresignal rejects the output, and sends the original instead, when it:
  - comes back empty or cut off;
  - is longer than one and a half times the input (short strings may grow by up to 16 characters);
  - loses, repeats or invents a protected token.
- **Cache:** each title and body is cached by its SHA-256 and the target language for 24 hours, so a message sent
  to several channels in the same language, or sent again, is translated only once.
- **Timeout:** a translation that takes longer than 8 seconds is abandoned. The original is sent with the title
  prefixed `[untranslated]`. The same fallback applies when no chat provider is assigned, the budget is spent, the
  provider fails or refuses, or the output is rejected. A message is always delivered.

Large bodies, such as long briefings, may not translate within 8 seconds on slower models. Those arrive marked
`[untranslated]`.

## Web Push and UnifiedPush

Foresignal pushes alerts, briefings and system events to browsers (a PWA, the operator console) and
to native Android and Linux apps through [UnifiedPush](https://unifiedpush.org), which speaks the same
Web Push protocol (RFC 8030 with RFC 8291 encryption). Foresignal holds its own VAPID key, generated on
first start, so no push account is needed.

**The Push channel.** One built-in channel named "Push" (type `webpush`) is created at startup and
fans out to every verified subscription. Its events (alert, briefing, system and the opt-in events above), rule filter, minimum
severity, quiet hours and the global pause apply as for any channel (`PUT /v1/channels/{id}`); it
cannot be created or tested like the others. Its `config.quietHoursMode` decides what quiet hours do:
`defer` (default, as for every channel), `downgrade` (sent at once with `Urgency: very-low` and
`silent: true`) or `drop`. Messages at or above the quiet hours' `bypassSeverity` (critical by
default) always go out. Each subscription adds its own filters (`events`, `ruleIds`, `minSeverity`,
`tags`).

**Subscribing.** A client needs a token (read scope is enough; a token is required even with open
reads). It fetches `GET /v1/push/vapid-public-key`, subscribes, and POSTs the subscription to
`/v1/push/subscriptions`. The new subscription is `pending`: Foresignal immediately sends it a
verification push `{"v":1,"event":"verify","subscriptionId":"…","code":"…"}`, and the client echoes the
code to `POST /v1/push/subscriptions/{id}/verify` within 10 minutes (five wrong codes delete it). Only
verified subscriptions receive notifications. Limits: 5 pending per token, 200 in total. Revoking the
token deletes its subscriptions. `GET /v1/push/subscriptions` (admin) lists them with their host,
status and failure count, never the endpoint.

A service worker for a browser client:

```js
// page: register the worker and subscribe
const reg = await navigator.serviceWorker.register('/sw.js');
const { publicKey } = await (await fetch(`${FS}/v1/push/vapid-public-key`)).json();
const sub = await reg.pushManager.subscribe({ userVisibleOnly: true, applicationServerKey: publicKey });
await fetch(`${FS}/v1/push/subscriptions`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${TOKEN}` },
  body: JSON.stringify({ name: 'Laptop', subscription: sub.toJSON(), filters: { minSeverity: 'warning' } }),
});

// sw.js
self.addEventListener('push', (event) => {
  const msg = event.data.json();
  if (msg.event === 'verify') {
    event.waitUntil(fetch(`${FS}/v1/push/subscriptions/${msg.subscriptionId}/verify`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${TOKEN}` },
      body: JSON.stringify({ code: msg.code }),
    }));
    return;
  }
  event.waitUntil(self.registration.showNotification(msg.title, {
    body: msg.body, tag: msg.tag, lang: msg.lang, silent: msg.silent, data: msg,
  }));
});
self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  const links = event.notification.data.links || {};
  event.waitUntil(clients.openWindow(links.web || links.api));
});
```

The payload is `{v, event, id, alertId, ruleId, severity, title, shortTitle, body, url, links {api,
app, web}, tags, lang, tag, silent, ts}` (absent members omitted), at most 3,993 bytes; long bodies
are cut first. `tag` is `foresignal-alert-<alertId>`, so a resolve replaces the fired notification.
`links.app` and `links.web` come from the server setting `clientLinks`. Safari 18.4 and later can use
`payloadFormat: "declarative"` (WebKit Declarative Web Push, no service worker needed); the
notification then opens `links.web`, or `links.api` when there is none.

A Flutter app with the [`unifiedpush`](https://pub.dev/packages/unifiedpush) package passes the
VAPID key to `registerApp` and gets an already decrypted message:

```dart
await UnifiedPush.initialize(
  onNewEndpoint: (PushEndpoint endpoint, String instance) async {
    final keys = endpoint.pubKeySet!;
    await api.post('/v1/push/subscriptions', {
      'name': 'Pixel', 'transportLabel': 'unifiedpush',
      'subscription': {'endpoint': endpoint.url, 'keys': {'p256dh': keys.pubKey, 'auth': keys.auth}},
    });
  },
  onMessage: (PushMessage message, String instance) async {
    final msg = jsonDecode(utf8.decode(message.content));
    if (msg['event'] == 'verify') {
      await api.post('/v1/push/subscriptions/${msg['subscriptionId']}/verify', {'code': msg['code']});
    } else {
      showLocalNotification(msg['title'], msg['body'], tag: msg['tag']);
    }
  },
);
final vapid = (await api.get('/v1/push/vapid-public-key'))['publicKey'];
if (await UnifiedPush.tryUseCurrentOrDefaultDistributor()) {
  await UnifiedPush.registerApp('default', vapid: vapid);
}
```

**A distributor on the LAN.** Endpoints must be `https` and every address the host resolves to must
be globally routable; loopback, private, CGNAT, link-local, unique-local, NAT64, 6to4 and metadata
addresses are refused. A self-hosted UnifiedPush distributor (for example ntfy) on the LAN is allowed
by listing it in the server setting `push.allowedPrivateHosts`, as an exact host name or a CIDR (IPv4
/8 or longer, IPv6 /32 or longer; no wildcards). It still needs https. The address is checked again
before every send and the connection goes to the checked address; three failed rechecks in a row
disable the subscription. `push.contact` (default `mailto:admin@localhost`) is the VAPID contact the
push services see, and `push.defaultLang` the payload language when a subscription sets none.

**Reach.** Web Push reaches Chrome, Edge and Firefox on desktop and Android while the browser runs,
and Safari when a site is added to the Home Screen. It does not reach the native iOS app, and it does
not wake a closed desktop browser on macOS or Windows. Use native push (APNs and FCM, below) or an
ntfy channel for those.

**Delivery.** Each subscription gets its own send and retries (10 s, 1 min, 5 min, 30 min, 2 h), so a
retry never repeats a push that already arrived. A push service answering 404 or 410 removes the
subscription; 429 and 503 are retried after `Retry-After`. At most 20 pushes per second go to one push
service host. The Push channel's delivery is delivered when at least one subscription received it.

## Native push (APNs and FCM)

The Flutter apps receive alerts on iOS, iPadOS and macOS through Apple Push Notification service
(APNs) and on Android through Firebase Cloud Messaging (FCM). Both are further targets of the
built-in Push channel, so its events, quiet-hours mode and retries apply unchanged.

**The self-hosting constraint.** An APNs key belongs to the Apple developer team that signs the app,
and a Firebase service account to the Firebase project compiled into the app. Foresignal can only
push to an app built and signed with your own team and Firebase project (or through a publisher's
relay, which Foresignal does not provide). Without that, use UnifiedPush (above) or an ntfy channel:
neither needs an account.

**Privacy.** A native push is a visible alert that carries only ids, the severity and a minimal
text; full titles and bodies never leave the server through Apple or Google. Per device,
`contentLevel` chooses the text:

| `contentLevel` | Title | Body |
|---|---|---|
| `minimal` (default) | Foresignal | "Critical alert", "Briefing ready" or "System notice" |
| `short` | Foresignal · critical | the alert's short title (at most 40 characters) |

`short` lets Apple or Google see the rule name; choose it only where that is acceptable. The app's
Notification Service Extension (iOS) or message handler (Android) replaces the text with the real
title and body, which it fetches from Foresignal itself (see `docs/Client-Guide.md`).

**Reachability for the rich text.** The extension fetches from your Foresignal over the phone's own
network: on the LAN, over a VPN, or through Tailscale. It has about five seconds; when Foresignal is
out of reach the minimal text is shown, and opening the notification fetches the details later.

### Create an APNs key

1. In your Apple Developer account, under Certificates, Identifiers & Profiles → Keys, create a key
   with Apple Push Notifications service enabled. Apple's keys are environment-specific: choose
   Sandbox (development builds) or Production (TestFlight and App Store); a team may hold two per
   environment. Older keys that serve both environments still work. Topic-specific keys are tied to
   one environment and a list of bundle ids.
2. Download the `.p8` file (you can download it once), and note the Key ID and your Team ID.
3. Configure the provider (the `.p8` text is the PEM; it is validated as an EC P-256 key, encrypted
   and never shown again):

```http
PUT /v1/push/providers/ios-apns
Authorization: Bearer <admin token>

{"type": "apns", "appIds": ["com.example.foresignal"],
 "config": {"teamId": "ABCDE12345", "keyId": "KEY1234567", "keyEnvironment": "production",
            "timeSensitive": false},
 "secrets": {"p8": "-----BEGIN PRIVATE KEY-----\n…\n-----END PRIVATE KEY-----\n"}}
```

`keyEnvironment` is `sandbox`, `production` or `both` (legacy keys). A device registers with its
own environment, and registration is refused (409 `PUSH_PROVIDER_NOT_CONFIGURED`) when no key
covers it: debug builds use `sandbox`, TestFlight and App Store builds `production`. Set
`timeSensitive` only when the app has the Time Sensitive Notifications capability; critical alerts
are then delivered as time-sensitive. Foresignal never sends Apple's `critical` level, which needs a
special entitlement.

### Create a Firebase service account

1. In the Firebase console of the project the Android app uses, open Project settings → Service
   accounts and generate a new private key. It downloads a JSON file.
2. Configure the provider with the whole JSON (only its `private_key` is stored, encrypted;
   `project_id` and `client_email` fill the config):

```http
PUT /v1/push/providers/android-fcm
Authorization: Bearer <admin token>

{"type": "fcm", "appIds": ["com.example.foresignal"], "config": {},
 "secrets": {"serviceAccountJson": "<the downloaded JSON, as a string>"}}
```

Foresignal uses the FCM HTTP v1 API with an OAuth 2.0 token it mints from the service account
(the legacy FCM APIs no longer exist). An iOS build that registers through FCM instead of APNs is
also served: the message then carries an APNs block.

### Test and monitor

`POST /v1/push/providers/{providerId}/test` mints an APNs provider token or an FCM access token and
reports whether it worked; with `{"deviceId": "…"}` it also sends that device a test notice. A
device's owner can send itself a test with `POST /v1/push/devices/{deviceId}/test` (five per hour).

When Apple or Google rejects the key (an invalid or revoked key, a key of the wrong environment, a
Firebase permission problem), the provider's `lastError` shows the reason code, one
`system.push_provider_failed` notification goes to your other channels (never to Push), and the
pushes are retried after 15 minutes. `lastError` clears on the next success. Responses, logs and
`lastError` never contain a key, a device token or alert text.

### Devices

The app registers each installation with `POST /v1/push/devices` using its own read token (a
paired token from `foresignal://pair`). `GET /v1/push/devices` lists every device for an admin and
only its own for a read token; device tokens are never shown, only their last six characters.
A device whose token Apple or Google reports as gone (APNs 410, `BadDeviceToken`; FCM
`UNREGISTERED`) is removed automatically, and revoking or expiring the API token that registered a
device removes it too. Deleting or disabling a provider stops pushes to its apps' devices until a
provider serves them again; the devices stay registered.

## Acting from notifications

A firing alert can be acknowledged, snoozed (1 h) or its watch muted (4 h) straight from the
notification. Each button or link is a **single-use capability** (`/v1/actions/eac_…`), valid for 12
hours and only for that alert; no API token is ever put on the phone. Opening a link (`GET`) only
shows a confirm page with one button, so link previews, mail scanners and a stray tap never act; the
action happens on `POST`. Acknowledging or resolving the alert any other way revokes the remaining
buttons, and every execution is in the audit log (actor kind `action`, the token shown as `eac_…`).
Acting on an alert that is already acknowledged or resolved answers "already acknowledged".

Per channel, under `config.actions` (`{"enabled": true, "list": ["ack", "snooze", "mute"], "links": true}`):

| Channel | How it acts | Default |
|---|---|---|
| ntfy | Two one-tap buttons (`http` POST, `clear=true`) and **Open** | on |
| Push (Web Push) | Up to two notification buttons; browsers without buttons (Safari) open the confirm page | on |
| Push (native apps) | Category `FORESIGNAL_ALERT_ACTIONABLE`; the app fetches its own buttons from `GET /v1/notifications/{id}`, so no capability goes through Apple or Google | on |
| Telegram | Inline buttons; the message is then edited ("Acknowledged by Ana") and the buttons removed | on |
| Matrix | Confirm-page links; with `"reactions": true`, a ✅ reaction from someone in `allowedSenders` acknowledges | on (links) |
| Pushover | Critical alerts are emergency priority; acknowledging in the Pushover app acknowledges in Foresignal (`pushover:<device>`); the URL opens the acknowledge confirm page | on |
| Webhook | An `actions` array (`action`, `label`, `url`, `expiresAt`) in the default JSON body | opt-in |
| Email, Slack, Discord, Mattermost, Teams, Google Chat, Gotify, … | Confirm-page links at the end of the body (`"links": true`) | opt-in |

The links use the `server` setting's `publicBaseUrl`, which must be reachable from the phone. Set
`"enabled": false` to send a channel no buttons, or narrow `list` (for example `["ack"]`).

**Telegram needs a dedicated bot.** Foresignal long-polls `getUpdates` for button presses, which
Telegram refuses while a webhook is set for the bot, and two programs polling one bot steal each
other's updates. Create a bot for Foresignal alone with @BotFather. `allowedUserIds` (Telegram user
ids) limits who may press; left empty, any member of the channel's chat may.

**Pushover receipts.** Foresignal polls each emergency receipt every 60 seconds (Pushover asks for no
more than every 5 seconds) for up to three hours, until it is acknowledged or expires.

**Why links only confirm.** A capability URL is a credential. Preview bots, antivirus scanners and
browsers prefetch links; if `GET` acted, they would acknowledge your alerts. The confirm page is
plain HTML without scripts or external assets, is never cached and is not indexed; posts from
another site are refused, and the endpoint takes at most 30 requests per minute per client address.

### Presence deferral

When you are at your desk, your phone need not buzz for what you are already looking at. Give the
phone-type channels (Push, ntfy, Pushover, Telegram, Gotify) `"deferWhileActiveSeconds": 120`. Your
desktop client reports `POST /v1/clients/presence {"kind":"desktop","state":"active","ttlSeconds":120}`
every minute or so. While a desktop is active, a non-critical alert waits 120 seconds on those
channels; if you acknowledge, snooze or resolve it, or read it on the desktop, in that time, the phone
copy is marked `suppressed` (visible in `/v1/channels/{id}/deliveries`) instead of sent. Critical
alerts and system events are never held, and presence that stops arriving expires, so everything goes
to the phone again. `GET /v1/clients` (admin) lists the current presence of every device.

### Escalation

An escalation policy reaches further people when nobody acknowledges:

```json
POST /v1/escalation-policies
{"name": "Wake me up",
 "steps": [{"afterMinutes": 10, "channelIds": ["<pager channel id>"], "includePush": true},
           {"afterMinutes": 30, "channelIds": ["<second person's channel id>"]}],
 "repeat": {"everyMinutes": 30, "maxTimes": 3},
 "stopOn": {"resolve": true, "snooze": true}}
```

Put its id in a watch's `escalationPolicyId`. Ten minutes after an alert of that watch fires, unless
it was acknowledged (always), resolved or snoozed (per `stopOn`), the pager channel and Push get
"[Escalation 1] …" with fresh buttons; at 30 minutes the second channel gets "[Escalation 2] …", and
then every 30 minutes up to three more times. Quiet hours hold escalations back except for critical
alerts. Acknowledging stops it at once, cancels escalation messages still waiting and cancels Pushover
emergency retries; muting the watch (`POST /v1/triggers/{id}/mute {"for":"4h"}`) stops escalation of
its open alerts too. `GET /v1/alerts/{id}/escalation` shows where an alert stands. A policy used by a
watch cannot be deleted (`409 POLICY_IN_USE`). Muted watches still record alerts and keep them in the
inbox, but notify nobody until the mute ends (1 hour to 7 days).

## Reference data and region tags

Foresignal gives every record a human place context without any online geocoder (prompt 040):
country, first-level region, US county, sea area, and the nearest town ("12 km NNE of Austin, TX").
Tags are stored beside the record (`record_region`), never in its payload, so they never create
versions or change hashes.

**Datasets.** On the first start, when `foresignal.reference.auto-load` is on (the default), the
default datasets load in the background about 30 s after startup. Loading never blocks startup or
readiness; the `reference` readiness detail and `GET /v1/reference` show progress and failures, and a
failed load raises a `system.reference_load_failed` notification.

| Dataset | Source | License | Download | Table |
|---|---|---|---|---|
| `ne-admin0` | Natural Earth 10m admin-0 countries v5.1.2 | Public domain | 13 MB | `ref_country` |
| `ne-admin1` | Natural Earth 10m admin-1 states and provinces | Public domain | 41 MB | `ref_admin1` |
| `ne-admin2-us` | Natural Earth 10m admin-2 (US counties) | Public domain | 8 MB | `ref_admin2` |
| `ne-marine` | Natural Earth 10m marine polygons (sea areas) | Public domain | 1.7 MB | `ref_sea` |
| `geonames-cities15000` | GeoNames, places over 15,000 people (34,160 rows on 2026-10-12) | CC BY 4.0 | 3.4 MB | `gazetteer_place` |
| `geonames-admin1` | GeoNames first-level region names | CC BY 4.0 | 0.2 MB | `gazetteer_admin1` |
| `geonames-countryinfo` | GeoNames country names | CC BY 4.0 | 32 KB | `gazetteer_country` |
| `geonames-cities1000`, `geonames-cities500` (optional) | GeoNames, places over 1,000 / 500 people (171,192 / 236,046 rows) | CC BY 4.0 | 11 / 14 MB | `gazetteer_place` (replaces) |

Reload or add datasets with `POST /v1/reference/load {"datasets": ["ne-admin1", "geonames-cities1000"]}`
(admin, 202). Only names from the bundled manifest are accepted, never URLs. Downloads go through the
same budgeted client as adapters (id `foresignal-reference`, 10 requests a minute, 100 a day,
Foresignal's User-Agent) into `${FORESIGNAL_DATA_DIR}/reference/`. Each file is checked against the
SHA-256 pinned in the manifest; a mismatch is `REFERENCE_CHECKSUM_MISMATCH` and the dataset is not
loaded. GeoNames regenerates its dumps every day, so its entries are verified only when you pin a
checksum (`foresignal.reference.checksums.geonames-cities15000: <sha256>`); otherwise the observed
SHA-256 is recorded in `GET /v1/reference`. Every load goes into a staging table and is swapped in
inside one transaction, so readers never see a half-loaded table.

**Disk use.** The default downloads take 65 MB in `${FORESIGNAL_DATA_DIR}/reference/` (measured
2026-10-12; you may delete them after a load). In PostgreSQL the boundaries are stored at full Natural
Earth resolution plus a simplified copy, with GiST indexes; check the real size with
`SELECT pg_size_pretty(sum(pg_total_relation_size(c.oid))) FROM pg_class c WHERE c.relname LIKE 'ref\_%'
OR c.relname LIKE 'gazetteer\_%'`.

**Offline installs.** Copy the files named in `src/main/resources/reference/manifest.json` into a
directory and set `foresignal.reference.offline-dir` (or `FORESIGNAL_REFERENCE_OFFLINEDIR`) to it;
the loader reads them instead of downloading and still verifies the checksums. Without any reference
data, tags still carry the country, from a simplified boundary set bundled with the application, and
`GET /v1/reference/countries` serves the bundled country table.

**Boundaries.** Natural Earth draws de facto boundaries (who controls the ground), which are not an
endorsement of any claim. `foresignal.reference.ne-admin0-variant: ind` (or `arg`, `chn`, `deu`,
`fra`, `gbr`, `rus`, `tur`, `twn`, `ukr`, `usa` … every Natural Earth point-of-view file is in the
manifest with its hash) loads a point-of-view file instead.

**Licenses.** Natural Earth is public domain. GeoNames is CC BY 4.0: the credit is in
`THIRD_PARTY_NOTICES.md`, in `GET /v1/reference`, and in every response that carries a `near`
(`RegionTags.attribution`, GeoJSON `properties.nearAttribution`). Keep it when you republish those
fields.

**How records are tagged.** Per source, `regionTagging` (`PUT /v1/sources/{id}`) is `eager` (default
for STANDARD sources: tagged as versions persist), `lazy` (default for TRACK sources: tagged when a
template renders, an alert fires, or a record is read with `include=region`) or `off`. A moving entity
is tagged again once it is more than 2 km (10 km for TRACK sources) from where it was last tagged. A
point outside every country but within 5 km of one gets that country; at sea the country is empty and
`sea` is set. After a reference load every current record of an eager source is tagged again.

**Using the tags.**
- `include=region` on `GET /v1/records` and `GET /v1/records/{source}/{id}` adds `region`; GeoJSON
  features gain `properties.near`.
- Filters `country=UA,PL`, `admin1=US-TX` and `county=48453` on records (JSON and GeoJSON), `/v1/search`,
  `/v1/alerts` (the alert's region at fire time), the stats reads (answered from raw rows) and briefing
  scopes. Unknown codes are `400 UNKNOWN_REGION`. Record filters see tagged records only, so a mover may
  lag by up to one retag threshold, and lazy sources match only once their records were tagged.
- `GET /v1/reference/reverse?lon=-97.7&lat=30.37` and the MCP tool `reverse_geocode` tag any point.
- Template examples:

```text
{{name}}: M{{payload.mag}} {{near.text}}            → Quake: M4.6 12 km NNE of Austin, TX
{{country.name}} / {{admin1.name}} / {{county}}     → United States of America / Texas / Travis
{{near.km}} km ({{near.mi}} mi) {{near.bearing}} of {{near.name}}
{{near.text|imperial}}                              → 8 mi NNE of Austin, TX
```

## Stats, glance and metrics

### Activity rollups

Foresignal keeps an `activity_rollup` table so clients can draw activity charts and maps without
paging raw history, and so the counts outlive retention. A job runs every 5 minutes and rolls up each
closed hour (and, once its 24 hours are done, each UTC day) from `record_version`. It works from a
durable cursor (`app_setting['stats.rollup.cursor']`) written in the same transaction as the rows, so a
crash never double-counts or skips an hour.

- **Buckets are on the transaction axis:** hour `H` holds the versions *ingested* in `[H, H + 1 h)`.
  Stats on the observed, effective or event time are served from raw history, inside retention.
- **Measures:** `versions` (HEARTBEAT and KEYFRAME excluded), `entities` (distinct entities),
  `firsts` (FIRST and REAPPEAR), `tombstones`, `edges` (TRIGGER_EDGE). Rows hold no entity ids.
- **Cells:** each version's centroid is mapped to an H3 cell at `foresignal.stats.cell-res` (default 4,
  about 1,770 km² per cell). Versions without a geometry go to cell 0.
- **First start:** the job backfills from the oldest retained version, at most
  `foresignal.stats.backfill` (default `30d`) back, 6 hours per run.
- **Retention:** the rollup cursor joins the consumer clamp (`min(trigger, embedding, rollup)`), so raw
  versions are never deleted before they are rolled up. Rollup rows have their own windows:
  `RetentionSettings.stats = {"hourly": "35d", "daily": "1825d"}`.
- **Which sources:** private sources are skipped unless `PUT /v1/sources/{id}` sets
  `statsIncludePrivate: true`; set `statsEnabled: false` to leave a noisy source out.
- **Rebuild:** `POST /v1/stats/rollup/rebuild {"from": "…", "to": "…"}` (admin) recomputes the closed
  buckets in the range, per source, only where that source's raw versions are all still retained.

**Storage estimate.** Rows ≈ hours × sources × kinds × occupied cells, plus one totals row per
source and kind per hour. Point sources in a few hundred cells cost little. **Global aircraft at
resolution 4 occupy about 20,000 cells an hour, about 0.5 million rows a day** (about 17 million rows in
the 35-day hourly window). If that is too much, lower `foresignal.stats.cell-res` to 3 (about 7 times
fewer cells) or set `statsEnabled: false` on the aircraft sources.

### Stats endpoints

| Endpoint | What |
|---|---|
| `GET /v1/stats/timeseries` | `bucket=1h\|1d\|auto` (≤ 2,000 buckets), `measure`, `groupBy=none\|source\|kind`, `timeField`. `backend` says `rollup`, `raw` or `mixed` |
| `GET /v1/stats/grid` | H3 cells as GeoJSON (`res` ≤ `cellRes`), or `mode=raw&sizeKm=` hexagons in EPSG:3857 (≤ 7 days); ≤ 10,000 cells |
| `GET /v1/stats/top` | Top sources, kinds, cells or entities (`by=entity` reads raw history inside retention) |
| `count=true` | On `/v1/records`, `/v1/history` and `/v1/search`: `{count, exact, watermarkSeq}`, exact up to 1,000,000 |

Every stats query has a 10-second statement timeout (`503 QUERY_TIMEOUT`). With a bbox, a restricted
area or a place, rollup rows are chosen by cell center, so edges are accurate to a cell, and
`entities` counts an entity once per cell it visited.

### Glance

`GET /v1/glance` is a status document of at most 2 KB for widgets, watches and e-ink displays:
`status`, open alerts per severity with the latest three, source tallies, the latest briefing,
versions in the last hour, `lastSeq` and deep links. `?format=text` (or `Accept: text/plain`) gives at
most 8 lines of 40 ASCII characters. `?place=<id>` narrows the alerts to a place (token required).
Answers are cached for 15 seconds and carry a strong ETag, so polling with `If-None-Match` is cheap.

Without a token (under `openReads`) only `status`, `serverTime`, the latest alerts and `lastSeq` are
shown. To show the counts too, set the app setting `glance.exposeCountsAnonymously` to `true`.

- **TRMNL:** the TRMNL cloud accepts at most 12 webhook updates an hour, so whatever forwards the
  glance (a small script or an automation fetching `/v1/glance` with a read token) should coalesce to
  one update every 10 minutes or more. Bring-your-own-server (BYOS) setups need a renderer such as
  Terminus that turns the JSON into the panel image.
- **MagicMirror:** any JSON or text module can poll `/v1/glance?format=text` every minute.
- **Home Assistant:** a REST sensor:

  ```yaml
  sensor:
    - platform: rest
      name: Foresignal
      resource: http://foresignal.local:8080/v1/glance
      headers:
        Authorization: !secret foresignal_read_token
      value_template: "{{ value_json.status }}"
      json_attributes: [alerts, sources, activity, lastSeq]
      scan_interval: 60
  ```

- **Garmin and Wear OS:** the watch platforms need native code (a Connect IQ widget, a Wear OS tile)
  that fetches the JSON glance through the phone; the 2 KB limit keeps it within their request
  budgets.

### Metric projections

`/v1/metrics` exposes payload fields of current records as Prometheus gauges, from a registry of its
own: it never appears on `/actuator/prometheus`, and it needs the `read` scope (Prometheus sends the
token as a bearer). Define projections with `PUT /v1/settings/metrics` (admin):

```json
{"projections": [{"name": "usgs_magnitude", "source": "usgs-earthquakes", "kind": "earthquake",
  "path": "mag", "unit": "magnitude", "help": "Magnitude of current USGS earthquakes", "maxSeries": 500}]}
```

Each projection becomes `foresignal_data_<name>{source, external_id}` (or the projection's own
`labels`, at most 4) plus `foresignal_data_<name>_observed_timestamp_seconds`. Values follow live state
(SAMPLED sources included); a series disappears when its record is tombstoned, expires or is purged.
Non-numeric values are skipped and counted in `foresignal.projection.coercion_failures{projection}`.
Each projection keeps at most `maxSeries` series and the whole endpoint at most 10,000; reaching that
raises a `system` notification once. A private source needs `allowPrivate: true`, and its series are
shown only to callers that pass the private gate (an agent or admin token).

A Prometheus scrape job:

```yaml
scrape_configs:
  - job_name: foresignal-data
    metrics_path: /v1/metrics
    scrape_interval: 60s
    authorization:
      type: Bearer
      credentials_file: /etc/prometheus/foresignal.token
    static_configs:
      - targets: ["foresignal.local:8080"]
```

**Grafana without Prometheus:** the Infinity data source can chart the stats API directly. Add a
query of type JSON, URL `http://foresignal.local:8080/v1/stats/timeseries?bucket=1h&measure=versions&groupBy=source&from=${__from:date:iso}&to=${__to:date:iso}`,
an `Authorization: Bearer …` header, root selector `series`, and columns `key` (string) and
`points` expanded to `t` (time) and `value` (number).

## Personal location and privacy

Prompt 039 makes it safe to feed family phones and personal trackers into Foresignal. A **private**
source is invisible on every read and egress path unless the caller is explicitly allowed; the four
common tracker apps push without glue; each phone gets a key that can write only its own entity; and
any single entity can be exported or erased.

### Create a personal-location source

```http
POST /v1/custom-sources
{"id": "custom-family", "name": "Family phones", "profile": "personal-location"}
```

The `personal-location` profile fills in what the request leaves out:

- `private: true`. It cannot be unset: `"private": false` with this profile is a 400.
- STANDARD history tier, kinds `tracker` (fixes) and `tracker-transition` (OwnTracks region events).
- `observedAtMode: UPSTREAM`, `ttlAnchor: OBSERVED`, `recordTtl: 24h`, default retention 7 days.
- Ingest policy SAMPLED: `minInterval` 30 s, keyframe 10 min, `minDeviation` 50 m,
  `significantFields: [motion, charging]`.
- No embedding (`embedFields` are refused) and no enrichment (the private gate below).
- No precision cap: the stored coordinates keep 7 decimals. It is your own data; set
  `privatePrecisionDecimals` yourself if you want the 008 cap.
- Field specs `speedMs` (m/s), `courseDeg` (deg), `altitudeM` (m), `battery` (%), `charging`, `motion`.

Explicit members win over the profile, except `private`.

### One key per phone

```http
POST /v1/custom-sources/custom-family/devices
{"externalId": "alex-phone", "name": "Alex's phone"}
```

The answer carries the device key (`eid_…`) **once**, plus ready-made settings for each app under
`setup`, all built from the `server` setting's `publicBaseUrl` (set it to the address the phones
reach). A device key:

- authenticates only for its source, and forces every record it pushes to its bound `externalId`. A
  payload that names another device (`id`, `deviceid`, `device_id`, `dev`) is refused with
  `403 DEVICE_ENTITY_MISMATCH` and nothing is written;
- is accepted as `X-Foresignal-Ingest-Key`, `Authorization: Bearer`, the Basic-auth password (any
  username), or in the path: `/v1/ingest/{sourceId}/k/{key}/{format}`;
- stops working the moment it is revoked (`DELETE …/devices/{deviceId}`) or rotated
  (`POST …/devices/{deviceId}/key/rotate`, which returns the new key and setup once). The device's
  records stay until retention removes them or you purge them.

`GET /v1/custom-sources/{id}/devices` lists the devices with their key prefix and `lastSeenAt`; key
values are never shown again.

### App setup

| App | Setting | Value |
|---|---|---|
| OwnTracks (HTTP mode) | Import `setup.owntracks` as a `.otrc` file, or set Mode HTTP, URL `<base>/v1/ingest/<source>/owntracks`, Authentication on, any username, password = the device key | Foresignal replies `200 []` |
| Overland (iOS) | Receiver endpoint `setup.overland.endpoint`, access token `setup.overland.token` (sent as `Authorization: Bearer`) | Foresignal replies `{"result":"ok"}` even when some points are rejected (rejections are counted, not retried) |
| Traccar Client, OsmAnd | Server URL `setup.traccar.serverUrl` (`<base>/v1/ingest/<source>/k/<key>/osmand`); query, form or JSON bodies are all read | `200` with an empty body |
| GPSLogger (Android) | Log to custom URL: `setup.gpslogger.urlTemplate`, method GET (POST with a form or JSON body works too; Basic or Bearer auth instead of the path key is accepted) | `200` |

The GPSLogger template is
`lat=%LAT&lon=%LON&time=%TIME&acc=%ACC&alt=%ALT&spd=%SPD&dir=%DIR&batt=%BATT&dev=%SER`; parameter names
are read case-insensitively. Check the placeholders against the app's own Parameters screen (in the
custom URL settings): its website names only `%LAT`, `%LON`, `%TIME`, `%SAT`, `%ACC` and `%ALL`, and
the full list lives in the app. Foresignal reads `spd` as m/s.

OwnTracks region events (`_type=transition`) become a separate `tracker-transition` record whose
externalId is the phone's plus `:transition`, so an event never replaces the position.

Every fix maps to kind `tracker` with `speedMs`, `courseDeg`, `altitudeM`, `battery`, `charging`,
`motion` and `quality.accuracyMeters`. Units are normalized: OwnTracks `vel` is km/h, OsmAnd query
`speed` is knots, everything else is m/s.

- **Accuracy filter:** a fix whose accuracy is worse than `maxAccuracyMeters` (a format option under
  `formats.<format>`, default 200 m) is rejected with reason `LOW_ACCURACY`. Fixes with `valid=false`
  (`INVALID_FIX`) and exact (0, 0) (`NULL_ISLAND`) are rejected too. Overland's `wifi` name is dropped
  unless `formats.overland.keepWifi` is true.
- **OwnTracks encryption** is not supported: encrypted payloads are acknowledged with `[]`, dropped
  and counted, and the source's health shows "OwnTracks payload encryption is not supported; disable
  it".
- **Path keys and GET.** A key in the path, a key in the query or a GET push is accepted only from
  the addresses in the source's `ingestAllowCidrs`, or, when that is unset, from private addresses
  (loopback, RFC 1918, link-local, ULA); anything else is `403 INGEST_SOURCE_DENIED`. A phone on
  mobile data reaching you through a VPN (WireGuard, Tailscale) arrives from a private address; for a
  public reverse proxy, list its address range. The source ingest key (`eik_`) is accepted in the
  path only with `allowPathKey: true` (otherwise `404`); device keys always are.

### Logs and reverse proxies

Foresignal never logs a query string or a path that carries a key: `/k/{key}/` is rewritten to
`/k/redacted/` before anything (the token filter, problem documents, the fetch log, the access log)
sees it, and `key=`, `auth=`, `token=`, `PASSWORD=` and `PASSKEY=` query values are redacted. Do the
same in front of it:

- never log `$request_uri` or the query string for `/v1/ingest/*/k/*` or anything carrying `key=`,
  `auth=`, `token=`, `PASSWORD=` or `PASSKEY=`;
- log the path with the `/k/[^/]+/` segment rewritten (nginx: a `map $uri` to `/k/redacted/`), or
  turn access logging off for `/v1/ingest/`;
- never log the `Authorization` or `X-Foresignal-Ingest-Key` headers.

Caddy's `log` directive can rewrite both the path key and the query values (the `filter` format's
`regexp` filter, RE2; checked against the Caddy documentation on 2026-10-13):

```caddy
foresignal.example.org {
	reverse_proxy foresignal:8080
	log {
		format filter {
			request>uri regexp "(/k/)[^/?]+|((?i:password|passkey|key|auth|token)=)[^&]*" "${1}${2}redacted"
		}
	}
}
```

### What `private` hides, and from whom

A source is private when its descriptor says so (built-in TAK, Home Assistant and mail sources,
`CustomSourceInput.private`, an EDP manifest's `private`) or when you tighten it with
`PUT /v1/sources/{id} {"private": true}`. You can make any source private, never a descriptor-private
source public (`"private": false` is a 400).

Private sources are visible only to:

- `admin` tokens;
- `read` and `agent` tokens, feed keys and pairing codes created with `privateAccess: true` (tickets
  inherit their token's grant). An `agent` token **without** the grant no longer sees them;
- never to anonymous callers, whatever `openReads` says.

For everyone else a private source does not exist:

| Path | Behavior for a caller without access |
|---|---|
| `/v1/records` (list, get, versions, track, asOf, `count=true`), `/v1/history`, `/v1/history/frames`, `/v1/timeline`, `/v1/changes`, `/v1/kinds` counts | absent from lists and counts; a direct read is `404`, never `403` |
| `/v1/sources` | absent; `GET /v1/sources/{id}` is `404` |
| Tiles | kind layers leave private sources out; a private source layer is `404`; TileJSON and the layer index omit them. A tile or TileJSON that can contain private data is sent `Cache-Control: private` only |
| SSE (`/v1/changes/stream`, `/v1/events/stream`, alerts, inbox) | filtered per client at the hub; revoking the token or feed key sends `reset` (reason `revoked`) and ends its streams |
| Search | text search leaves them out; they are never embedded (`embed` is forced off and existing embedding rows are deleted when a source becomes private) |
| MCP | every tool filters per token with the same rule |
| Alerts | alerts on private records (snapshot included) are hidden; see notifications below |
| Stats, glance, metrics | rollups skip private sources unless `statsIncludePrivate`; glance hides their alerts; metric projections need `allowPrivate` and a caller with access |

Every private-data read by a non-admin principal (a token or feed key holding the grant) is written to
the audit log (`targetType: private_read`), as is every export and purge.

### Notifications

A notification about a private-source alert carries only the rule name, the severity and a link,
unless the channel has `config.privatePayload: true` (an admin opt-in per channel). Web Push, APNs and
FCM payloads never carry it, whatever the channel says, because they cross third-party push services;
the app fetches the details over its own authenticated connection.

### LLM egress

Private records are never placed in a prompt for a provider classified as remote. A provider counts
as local only when it is Ollama or flagged `options.local` **and** its base URL resolves only to
loopback or private addresses. Even then, private records are included only when
`PUT /v1/settings/llm {"allowPrivateLocal": true}` (default false). This covers Ask, briefings,
alert enrichment, the analyst and hypothesis drafts (prompt 125); enrichment of an alert on a private record is skipped unless both conditions hold.

### Export and erase one entity

```http
GET /v1/records/custom-family/alex-phone/export?format=gpx       # or geojson, ndjson (admin)
POST /v1/records/custom-family/alex-phone/purge {"purgeAlerts": false}   # admin
POST /v1/custom-sources/custom-family/devices/{deviceId}/purge             # purge + revoke the key
```

- The export streams the current state, every retained version (with observers), the alerts that
  reference the entity and a manifest with counts and a SHA-256 of everything before it. GPX writes a
  `trk` for kind `tracker`.
- A purge writes a tombstone (qa flag `PURGED`, its change carries `purged: true` so SSE clients,
  `/v1/changes` readers and offline caches delete the entity), deletes every older version on both
  tiers, then deletes the entity's embeddings, redacts referencing alerts (`{"redacted": true}`; or
  deletes them with `purgeAlerts: true`), redacts stored notification bodies and drops its metric
  series. Ask messages and briefing runs that cite the entity are counted in the report, not edited.
  The head disappears with a later retention pass.
- **Messages already delivered to external channels (email, chat, push) cannot be recalled.**

### A note on data-protection law

Tracking your own and your household's devices on hardware you control is the kind of purely personal
or household activity that the EU GDPR (Article 2(2)(c)) and similar laws place outside their scope.
That exemption may not cover tracking people outside your household, or sharing the data further. This
is not legal advice: check the rules that apply to you, and get consent from the people you track.

## Retention

`GET /v1/settings/retention` shows every window; `PUT` replaces the whole object (every window at
least `1h`); `POST /v1/settings/retention/run` runs the job now and returns what it deleted. The job
also runs hourly, starting 5 minutes after boot.

```json
PUT /v1/settings/retention
{"default": "7d", "trackWindow": "3d", "provenance": "30d", "alerts": "90d", "usage": "30d",
 "notifications": "90d", "audit": "90d", "kinds": {"earthquake": "3650d"}, "sources": {"usgs-earthquakes": "1825d"},
 "stats": {"hourly": "35d", "daily": "1825d"}}
```

**Which window applies.** A TRACK source always uses `trackWindow`. A STANDARD record's window is the
first of:

1. `kinds.{kind}` (yours);
2. `sources.{id}` (yours; the same value as `PUT /v1/sources/{id}` `retention`, stored once);
3. the descriptor's per-kind default (`kindRetention`);
4. the descriptor's `defaultRetention` (adapters set it by analytic class: events 1825 days, series
   3650 days, bulk events 90 days; the derived intelligence sources `foresignal-intel`, `foresignal-analytics` and
   `foresignal-outlook` 1825 days);
5. `default` (7 days).

A `sources` entry for a TRACK source, or a `kinds` entry for a kind only TRACK sources emit, is
`400 RETENTION_TIER_FIXED`; unknown sources and kinds are listed in one `400`.

**What the job removes, in order.**

| Step | What |
|---|---|
| TRACK | Whole track partitions (`rv_t_*`) once they are older than `trackWindow`; never row by row |
| STANDARD | Versions older than their window, in transactions of 10,000 rows |
| Partitions | Standard partitions that are older than the largest window and already empty |
| Heads | Records whose every version is gone and that nobody has observed for a window, purged by the writer |
| Provenance | `fetch_log` rows that wrote nothing after 7 days, the others after `provenance` |
| `seq_clock` | Minutes older than the largest window plus a day |
| Other | Resolved or suppressed alerts after `alerts` (open alerts stay), usage minutes after `usage`, delivered or failed deliveries after 7 days, notifications after `notifications` |
| Audit | Audit log entries after `audit` (default 90 days) |
| Inbox | Notifications after `inbox` (default 30 days) once no delivery refers to them, read cursors of devices unseen for 90 days, expired presence and action capabilities expired for a week |
| Stats | Activity rollup rows: hourly after `stats.hourly` (default 35 days), daily after `stats.daily` (default 1825 days) |

**Current state is kept while it is current** (FR-17 as amended). Versions follow the window, but a
record that is still being observed keeps a version newer than a quarter of its window, so it never
disappears. A record purged after going quiet that comes back starts again at version 1.

**Durable consumers hold retention back.** The trigger engine's, the embedding worker's and the
activity rollup's cursors (and later the intelligence cursors) clamp every cutoff, so nothing is deleted before it was evaluated. If a cursor
falls more than two windows behind, retention stops waiting for it, counts
`foresignal.retention.consumer_lag` and sends a `system` notification; `/v1/system` then shows
`RETENTION_CONSUMER_LAG`.

**Clients.** A change-feed cursor below a purge gets `410 RESYNC_REQUIRED` (re-snapshot, then resume);
an SSE stream resumed below the retained floor gets `reset`. Routine version deletes never force a
resync.

### History downsampling

TRACK history (aircraft, vessels, other moving things) is kept at full detail for
`trackRawWindow` (24 hours by default). After that, the retention job thins one track partition per
hourly run, oldest first, until the partition reaches the end of `trackWindow` and is dropped.

**What is kept** in a thinned partition:

- every version that matters on its own: the first sighting, a reappearance, a significant change
  (callsign, squawk and the like), a tombstone and every trigger edge;
- the newest version of each entity in the partition;
- every version a current record or an alert points at;
- of the remaining positions, one per entity per `trackDownsampleInterval` (5 minutes by default).

So alerts, trigger edges and current records always resolve. What goes is the dense run of ordinary
position samples in between.

**Storage effect.** A thinned partition keeps at most one ordinary sample per entity per interval.
For an entity sampled once a minute at the default 5-minute interval, that is at most one ordinary
sample in five, plus the versions above. How much TRACK storage that saves depends on how many of
your versions are edges and keyframes. `/v1/system` `history.tiers[]` shows `downsampledLeaves` and
`downsampledRows`, and the `foresignal.history.downsample.*` metrics count the rows before and after.

**Reading thinned history.**

- `/versions` items inside a thinned partition say `downsampled: true`. Their `prevSeq` may name a
  version that downsampling removed.
- An asOf read over thinned history treats each surviving position as valid for
  `max(ttl, interval)`, so a stationary aircraft stays on the map between survivors. The answer says
  `approximate: true`.
- A trigger backtest (`POST /v1/triggers/{id}/test`) over thinned history warns `DOWNSAMPLED_RANGE`.
  Match-mode counts there are lower than they were live; edge rules are unaffected. Live triggers
  never see thinned data.

**Settings.**

```json
PUT /v1/settings/retention
{"default": "7d", "trackWindow": "3d", "trackRawWindow": "24h", "trackDownsampleInterval": "5m"}
```

- `trackRawWindow` must be shorter than `trackWindow`.
- `trackDownsampleInterval` must be between `30s` and `trackRawWindow`.
- A TRACK source can use its own interval: `PUT /v1/sources/{id}` with
  `{"downsampleInterval": "1m"}`, and `{"downsampleInterval": null}` to go back to the global one.
  A STANDARD source is refused with `400 VALIDATION_FAILED`, because its history is never thinned.

**How to disable it.** Set `"trackRawWindow": null`. Partitions that are already thinned stay as
they are until retention drops them; nothing new is thinned.

## Radar and satellite products

Prompt 046 adds the first two product sources. Like every free source they start enabled; disable them
with `PUT /v1/sources/{id}` and `{"enabled": false}`.

| Source | Products | Upstream | Poll | Default budget |
|---|---|---|---|---|
| `mrms` | `mrms-reflectivity`, `mrms-precip-rate` (band 2 is the PrecipFlag type code), `mrms-mesh-60m` | NOAA MRMS, bucket `noaa-mrms-pds`, anonymous | 2 min, every frame | 30/min, 12,000/day, 4,000 MB/day |
| `goes-east-abi` | `goes-east-ir` (ABI band 13, clean IR) | GOES-19 CMIPC, bucket `noaa-goes19`, anonymous | 5 min | 10/min, 2,000/day, 2,500 MB/day |

Expect about 1.7–3.4 GB a day of downloads for `mrms` and 1.0–1.1 GB for `goes-east-abi`, and a
rolling disk of about 2 GB and 1.2 GB under the default retention ladders (MRMS: every frame for 6 h,
then 10-minute frames to 48 h; GOES: 5-minute frames for 24 h, then 30-minute frames to 72 h). The
`mbPerDay` budget is charged with each object's listed size **before** it is downloaded, so a day
whose byte budget is spent stops fetching rather than overspending.

**Health.** A source degrades when its newest frame is older than the product's `maxGap` (6 min for
MRMS, 15 min for GOES), and when an upstream file arrives without the field or variable Foresignal
expects ("field MergedReflectivityQCComposite missing at level 500 m", "variable CMI missing"): no
frame is written for such a file, because an empty frame would read as clear weather.

**The decoder self-test.** At startup Foresignal decodes four small bundled files, one per decoding
path: GRIB2 complex packing (GFS), GRIB2 PNG (MRMS), GRIB2 CCSDS/AEC (ECMWF, through the native
libaec library) and NetCDF4 (GOES). If a path fails, only the products that need it are disabled:
their tile, image and frame endpoints answer `503 PRODUCT_UNAVAILABLE`, the adapter skips them, and
the source's health shows `disabled: decoder self-test failed (…)`. The log line
`Decoder self-test failed for …` names the cause. The usual one is the native library: JNA extracts
libaec into `jna.tmpdir`, which the container sets to `/data/tmp` on the data volume. That directory
must allow executables. A named Docker volume does; a bind mount from a `noexec` filesystem does
not. Restart after fixing it. `foresignal.raster.decoder-self-test.enabled=false` turns the test off
(products are then never disabled by it).

**Files.** Downloads are staged in `${FORESIGNAL_DATA_DIR}/rasters/tmp/` and deleted after decoding.
NetCDF-Java's caches live in `${FORESIGNAL_DATA_DIR}/cache/netcdf/` (normally empty). Foresignal
never opens a NetCDF or GRIB file outside `rasters/tmp` and `rasters/selftest`.

### Building when the Unidata repository is down

NetCDF-Java is the one dependency not on Maven Central: Gradle fetches `edu.ucar` and
`edu.ucar.unidata` only from `https://artifacts.unidata.ucar.edu/all/` (Architecture §2). A build
host that has built Foresignal before already has the artifacts in its Gradle cache. To build
without the Unidata host:

1. Create `gradle/vendor-repo/` as a Maven repository layout and copy into it, from a machine that
   has them, every `edu.ucar` and `edu.ucar.unidata` jar and pom listed in
   `gradle/verification-metadata.xml` (for example from
   `~/.gradle/caches/modules-2/files-2.1/edu.ucar/`, renamed to
   `edu/ucar/<name>/<version>/<name>-<version>.{jar,pom}`).
2. In `build.gradle.kts`, replace `maven("https://artifacts.unidata.ucar.edu/all/") { name = "unidata" }`
   with `maven(uri("gradle/vendor-repo")) { name = "unidata" }`. Keep the name and the
   `exclusiveContent` filter: `checkUnidataScope` and the checksums in
   `gradle/verification-metadata.xml` keep applying, so a tampered or wrong file fails the build.
3. Build. Revert the line once the host is back; the vendored files need not be committed.

After a NetCDF-Java upgrade, regenerate the checksums with
`./gradlew --write-verification-metadata sha256 help`, then trim the file back to the `edu.ucar`
components and the `<trusted-artifacts>` rule.

## Static maps

`GET /v1/maps/static` (read scope; prompt 055) draws a PNG from the bundled Natural Earth 1:50m basemap and
Foresignal's own stored frames, so it works offline and **never fetches third-party tiles**. Give a box
(`bbox`), a `center` with `radiusKm`, an `alert` id (the map centers on its record geometry, radius default
50 km) or a `place` (a token is required; adds a marker and range rings), plus `products` (at most 4) with
optional `style`s, `time` (`latest`, `now`, an ISO time or an offset), `width` (≤ 1280, default 800),
`height` (default width × 0.75), `theme=light|dark` and `units=metric|imperial`. Every map shows each
product's frame valid time (UTC, plus the place's local time), a legend, the attribution, a scale bar and
the footer "Automated — not an official warning". A product without a usable frame is drawn as a note
("MRMS reflectivity unavailable — not evidence of clear weather"), never left out silently. The MCP tool
`get_map_image` returns the same map (≤ 512 px, ≤ 200 KB) with a text caption, and map attachments of
alerts use it too.

The response is immutable only when every frame is pinned (an ISO `time`, a versioned `style` per product,
a pinned `run` for forecasts, no `place`); otherwise it gets a short max-age and an ETag. Renders run on
the render pool within 2 s; under load the answer is `503 RENDER_BUSY` with `Retry-After: 1`.

**Your own tile server (optional).** `FORESIGNAL_MAPS_BASEMAP_URL_TEMPLATE`, an XYZ template such as
`http://tiles.lan:8080/{z}/{x}/{y}.png`, replaces the Natural Earth fills with tiles from a server **on your
own network**: a host that does not resolve to a loopback or private address is refused (logged once per
map) and Natural Earth is used. Tiles are fetched only while a map is drawn (at most 16 per map, 0.8 s
each) and are never cached or passed on. Do **not** point it at `tile.openstreetmap.org`: the OpenStreetMap
tile usage policy forbids this kind of use, and Foresignal refuses public hosts anyway.

## Feeds, Google Earth and Cesium

Prompt 058 lets ordinary feed readers, Google Earth and Cesium read Foresignal with no custom code. All of
these are `read` operations: open while `openReads` is on, otherwise reached with a token or, for clients
that cannot send headers, a feed key in the path (`/v1/k/{key}/…`). Private sources appear only for a
token or feed key holding the private grant, and a URL that carries a key is never cached publicly.

**Feed readers (Atom and JSON Feed).**
- Alerts: `GET /v1/alerts/feed.atom` or `/v1/alerts/feed.json`, filtered by `rule`, `minSeverity`
  (`info|warning|critical`), `source`, `kind` and `state` (`firing|acknowledged|resolved`), `limit` 1–200
  (default 50), newest first by last change (fired, acknowledged or resolved).
- Briefing runs: `GET /v1/briefings/{id}/runs/feed.atom` or `.json`, with the whole run text.
- Entry links open your app when you set `clientLinks` in the server settings, else the API URL under
  `publicBaseUrl`, so set `publicBaseUrl` to a name the reader can reach. Content is the alert message
  (and its enrichment summary) as sanitized HTML: no scripts, images or styles, only `http`/`https`
  links. Atom entries carry GeoRSS (`georss:point`, or `georss:polygon` for a simple polygon). Readers that
  send `If-None-Match` or `If-Modified-Since` get `304` until something changes.
- With `openReads` off, mint a feed key for the reader, for example
  `POST /v1/feed-keys {"name":"reader","prefixes":["/v1/alerts/feed.atom"],"restrictions":{"kinds":["earthquake"]},"expiresAt":"2027-06-01T00:00:00Z"}`,
  and subscribe to `{publicBaseUrl}/v1/k/{key}/alerts/feed.atom`. A feed key never sees alerts of watchlist
  rules.
- RSS 2.0 and WebSub are not offered: every current reader understands Atom or JSON Feed, and readers poll.

**Google Earth.** Add a network link to `{publicBaseUrl}/v1/kml/networklink.kml?layers=usgs-earthquakes,nws-alerts&refresh=60`
(Add → Network Link in Google Earth Pro; or open the file). It holds one NetworkLink per layer to
`/v1/kml/records.kml?layer=…`, refreshed every `refresh` seconds (at least 30) and two seconds after the
view stops, with the view's bounding box. Each refresh returns at most 5,000 Placemarks per layer, newest
first; when more match, the document's snippet says `truncated: showing 5000 of N`, so zoom in. Names,
descriptions and values are escaped for HTML, so upstream text can never inject markup into a balloon.
Other exports:
- `GET /v1/kml/records.kml?source=…|kind=…&bbox=…&asOf=…&format=kmz` for a one-off file (KMZ bundles the
  icons);
- `GET /v1/kml/tracks.kml?sourceId=…&externalId=…&from=…&to=…` for one entity's `gx:Track` history
  (unsimplified, at most 5,000 points), or `?kind=…&bbox=…` for up to 100 entities;
- `GET /v1/triggers/{ruleId}.kml` for a trigger's geofence.

With `openReads` off, use a feed key whose prefixes include `/v1/kml/` and `/v1/map/` (the icons), and add
`{publicBaseUrl}/v1/k/{key}/kml/networklink.kml`: the generated links keep the same key, and the access
log shows only its first eight characters.

**Cesium.** Load the same NetworkLink document as a data source; Cesium needs the camera and canvas to fill
the view bounding box:

```js
viewer.dataSources.add(Cesium.KmlDataSource.load(
  `${base}/v1/kml/networklink.kml?layers=usgs-earthquakes`,
  { camera: viewer.scene.camera, canvas: viewer.scene.canvas }));
```

CesiumJS can also load tracks and history as CZML: see
[Exchange formats](#exchange-formats-czml-fdsnquakeml-stixtaxii-prompt-141).

**MapLibre style.** `GET /v1/map/style.json?layers=…&theme=light|dark` is an overlay style built from each
kind's display hints, with an SDF sprite (Maki and Temaki icons) and no glyphs; `docs/Frontend-Guide.md`
("Overlay style and sprite") shows how to add it over a basemap. Tile keys reach it when their prefixes
include `/v1/map/` and `/v1/tiles/`.

## Forecast models (GFS and HRRR)

Prompt 051 adds two forecast model sources. Both start enabled (free); disable them with
`PUT /v1/sources/{id}` and `{"enabled": false}`.

| Source | Products | Runs and steps | Poll | Default budget |
|---|---|---|---|---|
| `gfs` | `gfs` (10 m wind, u/v), `gfs-gust`, `gfs-t2m`, `gfs-apcp` (precipitation since the run started) | 4 runs a day; f000–f048 every 3 h, f054–f120 every 6 h | 15 min | 60/min, 3,000/day, 1,000 MB/day |
| `hrrr` | `hrrr` (10 m wind, u/v), `hrrr-refc`, `hrrr-gust`, `hrrr-smoke` | every 3 h, or every 6 h with `FORESIGNAL_PROFILE=small`; f00–f18 | 10 min | 60/min, 4,000/day, 2,000 MB/day |

Neither source downloads whole files (a GFS step is about 0.5 GB, an HRRR step about 150 MB). Each
step's `.idx` inventory names every message and its byte offset; Foresignal reads only the messages
it needs, with ranged requests, each charged to `mbPerDay` before it is sent. Expect about 0.4–0.5 GB
a day for `gfs`, and about 0.6 GB (small profile) or 1.1 GB (large) for `hrrr`, whose 10 m winds are
stored uncompressed upstream. At most 10 steps are read per poll, so a run completes over a few polls.
Disk: the current and the previous complete run are kept per product (about 29 steps × 4 products
for GFS), plus the GFS wind fields at 1° and 0.25° (about 6 MB compressed per step).

**What clients see.** A forecast product gets one `raster-frame` record version per **completed** run
(every configured step has a frame), not one per step; `{productId}:partial` reports a run in progress.
Tiles of a forecast frame need a run: a concrete time without `run=` answers `400 RUN_REQUIRED`
(`run=latest`, `previous` or the run time). The `gfs` product also serves God's Eye View wind fields
(`/v1/products/gfs/fields/…`) for web particle layers, and both wind products serve barb vector tiles
(`/v1/products/{id}/vectors/…`) and the `wind-speed@1` raster style. See the Frontend Guide.

**HRRR cadence by profile.** The small profile ingests every other HRRR run (00, 06, 12, 18z), which
halves the downloads; set the source's `runEvery` param to `3h` to ingest every run anyway, or `6h` on
a large host to save bandwidth. `runEvery` must be whole hours dividing 24.

**GFSv17.** NCEP plans GFS v17 (PNS 26-29/26-30). If its file layout changes, set the `gfs` source's
`pathTemplate` param instead of waiting for a release, for example
`{"params": {"pathTemplate": "gfs.{yyyyMMdd}/{HH}/atmos/gfs.t{HH}z.pgrb2.0p25.f{FFF}"}}` (the
default; tokens `{yyyyMMdd}`, `{HH}`, `{FFF}` three-digit step, `{FF}` two-digit step). Fields are
found by variable and level in the `.idx`, never by position, so new or reordered fields do not
matter. The `steps` param (for example `["0-48/3", "54-120/6"]`) and `availabilityLag` can be changed
the same way.

**Health.** A model source degrades when:
- its newest frame was ingested more than two runs ago (12 h for GFS, 6 h or 12 h for HRRR);
- the first `.idx` of the newest run is still missing 2 h after `availabilityLag` (3.5 h for GFS,
  50 min for HRRR): the reason names the missing object;
- an `.idx` lacks a field: "possible format change: GFS idx missing GUST:surface". No frame is written
  for that product (a missing wind field stops the whole step), so a format change never produces
  empty frames.

## Severe weather records

Prompt 047 adds four record sources (installed enabled, like every free source):

| Source | What it brings | Notes |
|---|---|---|
| `nws-alerts` | NWS watches, warnings and advisories | Every 60 s. Param `area` (state or marine codes, empty = national) and `minSeverity`. The NWS asks for a contact in the User-Agent: set `contactEmail` in the server settings (`PUT /v1/settings/server`), which Foresignal appends as `contact=…`. |
| `mrms-probsevere` | MRMS ProbSevere v3 storm objects | Every 2 min, newest file only, inside the places area of interest (no places: the Texas box, with a health warning). Automated guidance, never a warning. |
| `spc-outlooks` | SPC day 1–3 convective outlooks | Every 15 min, 17 conditional requests, mostly 304. A missing layer shows the source `degraded` with "SPC layer {file} missing". |
| `awc-metars` | Airport METARs | Every 5 min (AWC allows 1 request per minute per thread). Param `scope` (`aoi`, `conus`, `global`) or a `stations` allowlist. A report stays current for 3 hours. |

```json
PUT /v1/settings/server
{"publicBaseUrl": null, "contactEmail": "ops@example.org"}
```

None of these sources ever turns a failed fetch into an all-clear: a record is tombstoned only when a
complete, successful response no longer contains it. When a feed is down its records stay, expire on
their own, and the source shows `degraded`. Rule recipes are in the Agent Guide, "Weather alerts", and for the
derived place weather below, "Weather alerts".

## ECMWF IFS, forecast areas and storm tracking (prompt 105)

**`ecmwf-ifs`** adds the ECMWF IFS 0.25° open-data model as a third forecast alongside GFS and HRRR. It reads the
`ecmwf-forecasts` S3 mirror (falling back to `data.ecmwf.int/forecasts` by editing the `pathTemplate`), plans byte
ranges from each step's JSON `.index`, and costs about **0.5–1 GB/day** (20/min, 3,000/day, 1,200 MB/day; one
20-minute check, 00z and 12z, steps 0–144 by 3). Products: `ifs` (10 m wind), `ifs-gust`, `ifs-t2m`, `ifs-mslp`,
`ifs-precip-3h` and `ifs-precip-24h`. The whole real-time catalog is CC BY 4.0: **every derived output carries the
"Contains modified Copernicus/ECMWF data … changes made" attribution** (the `forecast-area` payload and the
place-weather `forecast.ifs` section carry it as `attribution`). The 00z/12z run appears about 7 h after its
reference time. A step is marked fetched only when every product that has it was committed; a step whose products
did not all commit (a missing reference message, a missing field) is retried on the next checks, at most 3 times.

**Run staleness is per model.** A derived weather section (place weather, forecast areas) calls a model's newest run
`stale` only once its successor should already be ingested: run cadence + availability lag + an ingest allowance.
Defaults: GFS 11.5 h, GFS-Wave 12 h, HRRR 7 h 50 min (the 6-hourly small-profile cadence), NBM 9 h, AIFS 16 h,
IFS 22 h. A slower cadence set by the operator makes sections go stale early, never late.

**`wx-forecast-areas`** (`forecast-area` records) thresholds a model field over the places AOI on each completed run:
the polygon where it is forecast to cross the threshold within the `thresholds` horizon (default 120 h, at most 120).
Defaults cover gust ≥ 20 and ≥ 25 m/s, 24-hour precipitation ≥ 50 mm and 2 m temperature ≥ 313.15 K (40 °C) and
≤ 263.15 K (−10 °C) for GFS, HRRR and IFS where the product exists. `validFrom` is the first exceeding valid time and
`validUntil` the last exceeding valid time plus the step, so a `forecast-area` rule can use `forecastHorizon`. A
typical recipe: "IFS gust ≥ 25 m/s within 48 h at home" is `kind=forecast-area`, `near{home}`, `forecastHorizon 48h`,
`enter`. A version is written only when the quantized payload changes or the polygon changes by more than 10%
(symmetric difference over union); `run` alone is volatile, so a newer run forecasting the same area writes nothing.
When a newer run drops the exceedance the area is **tombstoned** (an `exit`). The peak of a `<=` threshold is the
minimum.

**`wx-storm-tracks`** (`storm-track`, `storm-swath`) tracks MRMS storm cells with TITAN-lite: advected-polygon IoU
with a Hungarian assignment, merges and splits. Limits: it runs on the 2-minute MRMS frames inside the places AOI
(`scope=conus` on the large profile only), the nowcast is a straight **linear advection** of +15/+30/+60 min, and the
GLM lightning-jump flag (`2sigma-glm-v1`) is **advisory** — GLM's footprint over Texas is about 8–10 km and its
detection efficiency is below 100%. `storm-swath` is the union of the cells along the track, written every 10 min and
when the track ends (by a merge or by two missed frames); a restart extends the stored swath. A track keeps a
12-position `history` (volatile: a frame that only moves the history writes no version); its motion is the least
squares over the newest five positions. In a merge the largest track continues. Automated, not a warning.

**Annotators.** `outage-weather` adds `context.weather` (NWS warnings, radar ≥ 50 dBZ areas and storm tracks, and
gusts ≥ 25 m/s from `forecast-area` gust areas or METAR observations, within ±3 h and 25 km; read from the stored
versions in that window, so weather that has since ended still counts) to `infrastructure-disruption` and
`weather-impact` events as **context members** — background, never
supporting evidence, so they never raise confidence — and adds the alternative "weather-related damage". The
weather-impact recipes `dry-lightning-ignition`, `airport-weather` and `smoke-aq` create events from weather
co-occurrences.

## Extended weather products (prompt 106)

**Per-product enablement.** Every extended source has a `products` string-list parameter; a product not
listed is not fetched or stored. Left unset (it has no descriptor default), the memory profile
(`FORESIGNAL_PROFILE`) decides: the **small** profile turns on GOES-East C13/C08/C09, `rtma-ru`, `nbm`,
the OVATION aurora raster, TAF, HRRR smoke (surface and column), `wpc-heatrisk` and `epa-uv`; the **large**
profile also turns on C02, `goes-west-abi`, `gfs-wave`, the MRMS extras (lightning probability, VIL, echo
top), `sst-geopolar` and `aifs`. The mesoscale sectors are opt-in on both (list them in `goes-east-abi`'s
`products`), and `nexrad-l3` starts enabled like every free source. Set the parameter with
`PUT /v1/sources/{id}/params` (or the source settings API).

**New sources.** `goes-west-abi` mirrors the East clean IR on GOES-18; `goes-east-abi` adds the C08/C09
water-vapor channels, the C02 visible (daytime only, cropped to the AOI + 300 km) and the 1-minute
mesoscale sectors (AOI-gated, every second frame). `rtma-ru` is the 15-minute RTMA Rapid Update 2.5 km
CONUS analysis (observation; about 3.4 GB/day for all five products — set `analysisEvery` to `30m` or `60m`
to thin it); `nbm` is the National Blend of Models core; `gfs-wave` (HTSGW/PERPW/DIRPW) feeds the
place-weather `marine` section for places tagged `coastal`; `aifs` is the ECMWF AIFS machine-learning
forecast (CC BY 4.0). The `mrms` source gains lightning probability, VIL and echo top; the `hrrr` source
gains `hrrr-smoke-column` (COLMD); `swpc-aurora` gains the global `aurora-probability` raster; `sst-geopolar`
adds the daily Geo-Polar SST over an ocean bbox; `wpc-heatrisk` adds the **experimental** NWS HeatRisk
days 1–7 (place-weather `heat`); `epa-uv` adds the hourly UV index per place — set a place's `zip`
attribute (`PUT /v1/places/{id}` `{"attributes":{"zip":"78701"}}`) for it to be fetched; places without
one are named in the source's health note. **`nexrad-l3`** (enabled by default) ingests N0B reflectivity and N0G
velocity for the nearest WSR-88D sites (`sites`, or `siteCount` nearest the home), decoded with
NetCDF-Java's NIDS reader and resampled to 0.005° within 230 km.

**Products with a byte budget** set `mbPerDay`; when the projected day's download, at the rate seen so
far, exceeds **120%** of it, the source degrades with a `projected download … exceeds …` reason
(`BudgetOutlookHealthRule`, prompt 106), before the budget is actually refused.

**Field encodings.** Besides `.f32`, each global wind field (`gfs`, `ifs`, `aifs`) serves
`.velocity-json` (leaflet-velocity / wind-js, gzipped, ≤ 300,000 points per component) and `.png-uv`
(R = u, G = v, an 8-bit RGB PNG for WebGL particles); both are listed in the field manifest's
`encodings` block. **Position sampling:** `/v1/sample?record={sourceId}/{externalId}` samples at an
entity's live position (honoring `private`); a position older than 15 minutes marks the values
`stale-position`. `POST /v1/sample/track` samples each point of a track (given, or a record's 037
track) at its own time.

## Lightning and fire

Prompt 048 adds four more sources (installed enabled; all are free):

| Source | What it brings | Notes |
|---|---|---|
| `goes-glm` | GOES-19 GLM lightning | Every 30 s, every 20-second granule (about 1.1–1.9 GB a day). Flashes go to the flash store, not to records: a daily archive under `${FORESIGNAL_DATA_DIR}/rasters/glm/G19/{yyyymmdd}.bin` (16 bytes per flash, about 28 MB a day, kept 14 days, counted toward `FORESIGNAL_RASTER_DISK_QUOTA_GB`) and a 60-minute in-memory ring of the flashes inside the places area. Records are `lightning-cluster`. |
| `goes-fdc` | GOES-19 fire detections | Every 5 min, newest scan only. Param `scope` (`aoi` default, or `conus`). Records `fire-detection` (one per pixel) and `fire-cluster`. |
| `hms-smoke` | NOAA HMS analyst smoke polygons | Every 30 min, conditional requests; today's file, and yesterday's until 06:00 UTC. A 404 before the first analysis of the day is normal. |
| `hms-fire` | NOAA HMS analyst fire points | Same schedule and files as `hms-smoke`; records share `fire-detection` with `goes-fdc`. |

**The GLM ring scope.** `foresignal.raster.glm-ring-scope` (`FORESIGNAL_RASTER_GLM_RING_SCOPE`) is `aoi`
by default: the ring keeps the flashes inside the union of your places' buffers (no places: the
`weather.defaultAoi` box). The large profile (`FORESIGNAL_PROFILE=large`) may set `conus`; the small
profile logs a warning and stays on `aoi`. The archive always keeps the full field of view, and the ring
is rebuilt from it at startup and whenever a place changes. `GET /v1/system` reports
`rasters.glm{ringFlashes, archiveBytes, newestFileAt}`.

**Stale is not clear.** When the GLM or FDC feed stops, clusters and detections freeze: nothing is
tombstoned and no `exit` rule fires. The source shows `degraded` with "newest input file is …s old" once
the newest processed file is older than 2 minutes (GLM) or 15 minutes (FDC). An unreadable file or a
missing NetCDF variable also shows `degraded` with the reason, and the file is skipped. Rule recipes are
in the Agent Guide, "Lightning and fire".

### NASA FIRMS (`nasa-firms`, prompt 065)

`nasa-firms` brings NASA FIRMS near-real-time active-fire detections worldwide: VIIRS on NOAA-20, NOAA-21 and
Suomi-NPP, and MODIS, by default; Landsat (`LANDSAT_NRT`, US and Canada only) can be added. Records are
`fire-detection`, the kind `goes-fdc` and `hms-fire` write, so one "fire near home" rule covers all of them; fire
radiative power is `frpMw` in all three. Each detection stays current for 24 hours after its acquisition time and
is kept 7 days.

**Obtaining a MAP_KEY.** FIRMS needs your own free MAP_KEY, and the source stays disabled until one is stored
(enabling without it is a `409 CREDENTIALS_REQUIRED`, and no request is ever made without it):

1. Open <https://firms.modaps.eosdis.nasa.gov/api/area/>, choose **Get MAP_KEY** and enter your e-mail address.
   FIRMS e-mails the key.
2. Store it and enable the source:

   ```json
   PUT /v1/sources/nasa-firms/credentials
   {"mapKey": "…"}
   ```

   then `PUT /v1/sources/nasa-firms` with `{"enabled": true}`. A key FIRMS refuses makes every fetch fail with
   "FIRMS refused the MAP_KEY", which the source's health shows. The key is part of the request URL; Foresignal masks it as `***` in fetch
   traces, probes, the fetch log and errors.

A key allows 5,000 transactions per 10 minutes, and a large request may count as several. The default schedule
(every 30 minutes, four products one after another, plus yesterday's file for each product between 00:00 and
06:00 UTC) stays far below that.

**Setting `area` to reduce bytes.** By default each request asks for the whole world, and a busy day's VIIRS file
for the world runs to many megabytes. If you only care about one region, set `area` to
`west,south,east,north`, for example `PUT /v1/sources/nasa-firms` with `{"params": {"area": "-125,24,-66,50"}}` for
the contiguous United States.
The box must not cross the antimeridian. A small box can legitimately have no fires; this source does not treat
zero results as normal, so a quiet small area can show a warning. Other parameters: `sources` (the products, in
fetch order) and `window` (how old a detection may be when first emitted, 1h to 48h, default 24h).

**Incremental fetching.** FIRMS always returns the whole day, so each run emits only detections from three hours
before the newest one already ingested, per product; re-sent detections are touches, not new versions.

## Weather alerts

Prompt 052 adds three **derived** sources (installed enabled; all are free). They make no upstream request: they read
the frames and records the weather sources already stored, and write a record only when its rounded
content changes.

| Source | Kind | What a record is |
|---|---|---|
| `wx-places` | `place-weather` | one per place (`place:{id}`): radar, hail, rain rate, lightning, storm-cell ETA, smoke and GFS forecast sections, re-evaluated on every MRMS frame and every 60 s |
| `wx-areas` | `wx-area` | one per configured area (`refl:50:aoi`, `mesh:25:home`): the area above a threshold inside the places AOI or a place, every 6–10 min |
| `wx-cells` | `radar-cell` | one per tracked storm cell (`c-{ulid}`): maximum reflectivity, area, hail size, GLM flashes and centroid motion |

The inputs (`mrms`, `goes-glm`, `gfs`, `hrrr`) and the derived sources all start enabled; keep `wx-cells`
enabled while you use `wx-places` (the `cell` section needs it), and disable any you do not want with
`PUT /v1/sources/{id} {"enabled": false}`. `wx-areas` takes `areas` (a list of `field:threshold:aoi`, field `refl`, `mesh`, `precip`,
`rotation` (s⁻¹), `qpe1h` (mm) or `ffg` (% of flash-flood guidance))
and `interval` (6–10 min); `wx-cells` takes `scope` (`aoi`, or `conus` on the large profile). Each derived
source shows in `GET /v1/sources/{id}/health` like any other: every invocation (a frame, a change batch, a
tick) writes one `fetch_log` row with `principal: derived`.

**Unknown is not clear.** Every section has a `status`: `ok`, `stale` (MRMS older than 6 min, GLM older
than 2 min, model runs older than 12 h), `no-coverage` or `unavailable` (input disabled, failing or never
seen). A section that is not `ok` keeps its last values and adds `asOf`, the time of the last good data;
before any good data it carries only its status. "None within 100 km" is `999`, never null. So a stopped
feed can never look like clear weather, and a derived record is never tombstoned because its input is
stale; it is tombstoned only when its object disappears (a storm cell no longer present in a processed
frame) or its place is deleted. **Deleting a place tombstones its `place-weather` record, which fires
`exit` rules whose condition held** (a tombstone after a matching version is an exit).

**GLM limits.** GLM's footprint over Texas is about 8–10 km and it detects less than every flash. GLM-only
rules should use 15 km or more, and a lightning all-clear is advisory.

**Write derived rules with `enter` and `exit`** (or an explicit cooldown), never a bare `match`: a
place-weather record versions whenever any section changes, and `match` would fire on each version.
Conditions are AND-only, so all-clear rules use the **verified-clear helpers**:
`radar.verifiedNearest50Km10m` and `lightning.verifiedNearestKm30m` equal the metric while the section is
`ok` and `0` otherwise. A condition `<= X` on them is therefore true when a storm is near **or** the data is
unknown, and its `exit` fires only when the data is known **and** far.

**≥ 50 dBZ within 20 km:**

```json
{"name": "Heavy echoes near home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "radar.nearest50Km10m", "op": "<=", "value": 20}],
 "mode": "enter", "severity": "warning",
 "messageTemplate": "≥ 50 dBZ within {{payload.radar.nearest50Km10m}} km of {{payload.place}} (automated, not a warning)"}
```

**Echoes cleared (verified only).** P is "near or unknown"; `exit` fires when the previous version was near
or unknown and the current one is `ok` and far:

```json
{"name": "Echoes cleared at home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "radar.verifiedNearest50Km10m", "op": "<=", "value": 20}],
 "mode": "exit", "severity": "info",
 "messageTemplate": "No ≥ 50 dBZ within 20 km of {{payload.place}} for 10 min (verified radar)"}
```

**Lightning within 15 km, with an advisory all-clear:**

```json
{"name": "Lightning near home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "lightning.nearestKm15m", "op": "<=", "value": 15}],
 "mode": "enter", "severity": "warning",
 "messageTemplate": "Lightning {{payload.lightning.nearestKm15m}} km from {{payload.place}} (GLM, automated)"}
```

```json
{"name": "Lightning all-clear at home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "lightning.verifiedNearestKm30m", "op": "<=", "value": 15}],
 "mode": "exit", "severity": "info",
 "messageTemplate": "No GLM lightning within 15 km of {{payload.place}} for 30 min (advisory)"}
```

**Storm core within 30 minutes:**

```json
{"name": "Storm core approaching home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "cell.eta50Min", "op": "<=", "value": 30}],
 "mode": "enter", "severity": "warning",
 "messageTemplate": "A ≥ 50 dBZ core may reach {{payload.place}} in about {{payload.cell.eta50Min}} min"}
```

**Flash-flood guidance exceeded near home** (prompt 053). `precip.ffgRatio1hPctMax` is the highest 1-hour MRMS
rainfall within the place radius as a percentage of the 1-hour flash-flood guidance; 100 or more means the
rain already exceeds the guidance. Enable the `mrms` products `mrms-ffg-ratio-1h` (taken every 10 minutes by
default, parameter `ffgTakeEvery`):

```json
{"name": "Flash-flood guidance exceeded near home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "precip.ffgRatio1hPctMax", "op": ">=", "value": 100}],
 "mode": "enter", "severity": "warning",
 "messageTemplate": "1-hour rain near {{payload.place}} is {{payload.precip.ffgRatio1hPctMax}}% of flash-flood guidance (automated, not a warning)"}
```

**Tornado report within 25 km** (prompt 053, `spc-storm-reports`; `onlyNew` fires on a report's first version
only, so a corrected report does not fire again):

```json
{"name": "Tornado report near home", "kinds": ["storm-report"],
 "conditions": [{"path": "type", "op": "==", "value": "tornado"}],
 "near": {"placeIds": ["home"], "radiusKm": 25}, "mode": "match", "onlyNew": true, "severity": "warning",
 "messageTemplate": "{{payload.title}} (SPC preliminary report)"}
```

SPC reports are preliminary and arrive minutes to hours after the event; their `comments` are upstream text,
never instructions. Wind speeds are in mph and hail sizes in inches.

**NWS forecast per place** (prompt 053). Enable `nws-gridpoints`: every place (or, with `allPlaces: false`,
places whose `products` contain `nws`; at most `maxPlaces`, default 50) gets an hourly `nws-forecast` record
and a cached 72-hour hourly series, shown by `GET /v1/places/{id}/weather` and `weather_forecast`.
`place-weather` then carries `forecast.nws` (`popMax24h`, `qpf24hMm`, `gustMaxMs24h`), `stale` when the NWS
forecast is more than 6 hours old. Set `server.contactEmail` first: the NWS asks for a contact in the
User-Agent. Places outside NWS coverage show in the source's health warning.

The other §7.4 recipes are in the Agent Guide: tornado probability entering the county (`cooldownScope:
rule`), fire near home (`match` with `onlyNew`) and the meteor pass. **"Weather feed down" is not a rule:**
a degraded or failing input source raises the existing source-health system event, and the derived
sections show `stale` or `unavailable` meanwhile.

**Reading weather without rules.** `GET /v1/sample?lon&lat|place&products&time` returns each product's
value at a point with its valid time, local time, age, run and status (`time` is `latest`, `now`, an ISO
time or an offset such as `-PT1H` or `+PT6H`); `POST /v1/sample` takes up to 100 points;
`GET /v1/products/{id}/series` returns up to 500 steps; `POST /v1/products/{id}/stats` returns min, max,
mean, p95, coverage and the area above thresholds over a geometry, place or bbox (at most 2,000,000 km²);
`GET /v1/places/{id}/weather` returns the place-weather record plus cached GFS, HRRR and (053) NWS series. None of
them ever causes an upstream fetch.

## Derived intelligence layers (prompt 099)

Prompt 099 adds the object-derived layers. Like the weather ones they make no upstream request:
they read the stored and live records of the aircraft, vessel and event sources through the
observation feed, and write an ordinary record only when its rounded content changes. Every layer except
`derived-cpa` (disabled until you set its `aoi`) is installed **enabled** (free); every layer is a STANDARD source
with no TTL (the fused-aircraft layer is the one TRACK,
with a 5-minute TTL), and declares its inputs as source families, so the receiver slots you configure
(`local-adsb-*`, `ais-*`, `adsbfi-region-*`, …) are picked up without further setup.

| Source | Kind | Inputs | What it reports |
|---|---|---|---|
| `derived-gps-jamming` | `jamming-cell` | readsb-family aircraft (NACp/NIC; not OpenSky) | cells where ≥ 30% (medium) or ≥ 50% (high) of ≥ 3 airborne aircraft report a degraded solution |
| `derived-ais-gap` | `ais-gap` | AIS vessel sources | a vessel under way with no fix for 30 min, when ≥ 3 neighbors kept reporting nearby |
| `derived-loiter` | `loiter` | AIS vessel sources | a vessel holding within 0.5 nm of its centroid for ≥ 30 min |
| `derived-rendezvous` | `rendezvous` | AIS vessel sources | two slow vessels within 0.5 nm for ≥ 15 min, away from ports |
| `derived-deception` | `deception` | aircraft + vessel | duplicate MMSI, impossible vessel or aircraft speed |
| `derived-holding` | `holding-pattern` | aircraft | a slow aircraft turning through > 300° in 10 min in a level band |
| `derived-emergency-squawk` | `emergency-squawk` | aircraft | a 7500/7600/7700 squawk or a readsb emergency, confirmed twice |
| `derived-chokepoint` | `chokepoint-activity`, `chokepoint-uturn` | AIS vessel + PortWatch chokepoints | hourly banded transits, U-turns and stops per chokepoint |
| `derived-mil-naval` | `mil-naval-cooccurrence` | aircraft + vessel | military aircraft and naval vessels in one res-3 cell within 30 min |
| `derived-aircraft` | `aircraft` | every aircraft source | the freshest observation per hex, fused |
| `derived-corroboration` | `corroboration` | earthquakes, fires, conflict, outages | one record per cross-source event cluster with an Admiralty credibility |
| `derived-port-calls` | `port-call` | AIS vessel + World Port Index | arrivals and departures at port buffers |
| `derived-cpa` | `cpa` | AIS vessel (area) | vessel pairs with a close-quarters approach inside an area |
| `derived-felt-locally` | `felt-report-estimate` | earthquakes + places | estimated intensity at each place, by ShakeMap or IPE |
| `derived-cable-risk` | `cable-proximity-risk` | AIS vessel + submarine cables | slow or anchored vessels near cables outside ports |
| `derived-dark-vessel-match` | `dark-vessel-candidate` | AIS gaps + SAR detections | gaps joined to unmatched SAR detections (candidates only) |
| `derived-lightning-holdover` | `holdover-fire-risk` | GLM + FIRMS/FDC + QPE | flash clusters over dry cells, watched for a later fire |
| `derived-hf-blackout` | `hf-propagation-anomaly` | PSKReporter + SWPC X-ray | spot/SNR drops on sunlit paths against a 14-day baseline |

A layer reads only the inputs you have enabled: the free ones are on already, the keyed or configured ones (receiver
slots, `aisstream`, …) once you set them up. Disable a layer you do not want, for example
`PUT /v1/sources/derived-gps-jamming {"enabled": false}`. Each layer shows in
`GET /v1/sources/{id}/health` like any other and writes one `fetch_log` row with `principal: derived`
per invocation. Its tick is one minute by default (`derived-gps-jamming` five, others as noted in the
catalog).

**Absence of evidence is not evidence of absence.** Every layer is coverage-aware. An input that is
not `healthy`, or a window touched by a change-bus gap or a live drop, marks the record
`status: degraded-input` and suppresses absence-based events (an AIS gap, a cleared squawk, a port
departure). `derived-ais-gap` additionally requires three other vessels reported by the same source
within 50 km during the gap. A record is tombstoned only when its condition is genuinely gone, never
because input is stale; an uncertain record keeps its last values with `insufficient-data`.

**Rule recipes** (all `enter`/`exit` or an explicit cooldown, never a bare `match`):

```json
{"name": "GPS jamming cell goes high", "sources": ["derived-gps-jamming"], "kinds": ["jamming-cell"],
 "conditions": [{"path": "level", "op": "==", "value": "high"}], "mode": "enter", "severity": "warning",
 "messageTemplate": "High-rate GPS jamming in {{payload.cell}} (automated detection, not a warning)"}
```

```json
{"name": "Emergency squawk", "sources": ["derived-emergency-squawk"], "kinds": ["emergency-squawk"],
 "conditions": [{"path": "squawk", "op": "==", "value": "7700"}], "mode": "enter", "severity": "critical",
 "cooldown": "PT30M", "messageTemplate": "Aircraft {{title}} squawking 7700 (unverified)"}
```

```json
{"name": "AIS gap near me", "sources": ["derived-ais-gap"], "kinds": ["ais-gap"],
 "near": {"placeId": "home", "radiusKm": 100},
 "conditions": [{"path": "open", "op": "==", "value": true}], "mode": "enter", "severity": "warning",
 "cooldown": "PT1H", "messageTemplate": "Vessel {{title}} lost AIS inside 100 km of home"}
```

## Alert weather context

Prompt 056 gives every geolocated alert a deterministic weather block, `context.weather`, computed when the
alert fires and stored with it **before** it is dispatched, so the `alert.fired` event, every channel and the
enrichment see the same facts. It is computed from stored frames, the GLM flash ring and current records only
(never an upstream fetch) within a 500 ms budget; a part that misses the budget is `unavailable`, and a failure
stores `{"status": "unavailable"}` without delaying or blocking delivery.

| Section | What it holds |
|---|---|
| `radar` | MRMS reflectivity maximum within 10 km of the record's geometry (`maxDbz`), and the nearest `radar-cell` within 100 km (`nearestCell`: `id`, `distanceKm`, `motion {speedKmh, bearingDeg, method}`, `meshMaxMm`) |
| `lightning` | GOES GLM flashes within 20 km over the 15 minutes before the alert (`flashes20km15m`); `stale` when no GLM file ended within 2 minutes |
| `precip` | MRMS rain rate at the geometry (`rateMmH`), plus the 1-hour QPE (`qpe1hMm`) when `mrms-qpe-1h` is current |
| `warnings` | up to five active NWS alerts intersecting the geometry (`id`, `event`, `severity`, `expires`), each `untrusted: true` because its text is upstream data |
| `wind` | 10-m wind and gust of the newest HRRR run (else GFS) at the step nearest the alert (`productId`, `speedMs`, `gustMs`, `fromDeg`, `run`, `validTime`, `step`) |
| `smoke` | HRRR near-surface smoke at the nearest step (`pm25UgM3`, `run`, `validTime`, `step`) |
| `summary` | one sentence built from the sections, for example "Radar max 52 dBZ within 10 km; cell 8 km away moving ENE at 45 km/h; 14 GLM flashes within 20 km (15 min); NWS Severe Thunderstorm Warning in effect. Automated — not an official warning." |

Every section has `status` `ok`, `stale`, `no-coverage` or `unavailable`. **Unknown is not clear:** a non-ok
section carries `statusText` (and `asOf`, the time of its last good data, when known) and no values, and the
summary words it as unknown ("lightning data stale since 14:30 UTC", "radar data has no coverage here (not
evidence of clear weather)", "smoke data unavailable"), never as absence. A product whose source is disabled
gives an `unavailable` section, not a missing one. GLM's footprint over Texas is about 8–10 km and its detection
efficiency is below 100%, so a zero flash count is advisory.

- **Switching it off.** `PUT /v1/settings/weather` (admin) `{"alertContext": "off"}` turns it off everywhere;
  a rule's `"weatherContext": "off"` turns it off for that rule (default `auto`). It also stays off while no
  weather input (radar, rain, model or smoke product, GLM or NWS alerts) is enabled.
- **The same settings** hold `defaultAoi` (the area used while no places exist, 047) and `aoiBufferKm` (the
  minimum buffer around each place, 036); changing either recomputes the places area of interest. Until you
  store a value they come from the configuration properties `foresignal.weather.default-aoi` and
  `foresignal.weather.aoi-buffer-km`.
- **Reading it.** `GET /v1/alerts/{id}` returns the whole `context`; `GET /v1/alerts` lists carry only
  `context.weather.summary` unless you add `include=context`. Webhook bodies carry it in `data.context`.
- **Templates.** Rule templates and channel `titleTemplate`/`bodyTemplate` can use
  `{{context.weather.summary}}` and every field by its path: `{{context.weather.radar.maxDbz}}`,
  `{{context.weather.radar.nearestCell.motion.speedKmh}}`, `{{context.weather.lightning.flashes20km15m}}`,
  `{{context.weather.lightning.status}}`, `{{context.weather.wind.gustMs}}`,
  `{{context.weather.warnings.items.0.event}}` … A missing context or field prints nothing. The default body
  of `discord`, `telegram`, `email`, `ntfy`, `pushover` and `slack` appends the summary line when there is one,
  and the stored notification keeps it with the message.
- **Enrichment** receives the block as the labeled data section `alert.context.weather`, inside the untrusted
  data envelope, and is told that a section other than `ok` is unknown, not clear.

```json
POST /v1/triggers
{"name": "Storm cell near the farm", "sources": ["wx-cells"], "near": {"placeIds": ["farm"], "radiusKm": 30},
 "mode": "enter", "conditions": [{"path": "maxDbz", "op": ">=", "value": 55}],
 "messageTemplate": "{{payload.maxDbz}} dBZ cell near the farm. {{context.weather.summary}}"}
```

**Weather at places in briefings.** A briefing scope with `places` (and optionally `weatherProducts`, by default
the products behind place weather) adds a "Weather at places" table per place, in the place's timezone: **now**
(radar and lightning from the place's `place-weather` record, active NWS alerts), **the period** (maximum dBZ and
MESH, the GLM flash total within the place radius from the 14-day flash archive, and hazards that entered the
area; "partial period" when the window reaches past retention) and **the next 24–72 h** (the NWS gridpoint summary
and the GFS and HRRR maxima, each with its run and valid time). Stale or unavailable parts are labeled
("Lightning: data stale since 14:30"), never left out or shown as calm. `GET /v1/digest?places=home,farm` adds the
same section as `weatherAtPlaces` (it needs a token). The place-weather rule recipes are in
[Weather alerts](#weather-alerts).

## SatDump ground station

Prompt 049 makes your own [SatDump](https://github.com/SatDump/SatDump) station a source. Set
`FORESIGNAL_SATDUMP_STATIONS` (property `foresignal.satdump.stations`) to a comma list of station names,
for example `home`, and restart: each name becomes a `satdump-{station}` source, installed disabled until
you set its required `location`. A name is kebab-case and at most 56 characters.

SatDump is GPL-3.0 and stays a separate program that you install and run. Foresignal talks to it only
through HTTP (`/api`, `/polarplot.jpeg`) and the files it writes; no SatDump code is in Foresignal, and the
Foresignal image contains no SatDump binaries or SatDump-produced images.

### Station requirements (§4.0)

- **`/api` needs the headless legacy CLI.** Only `satdump legacy live …` and `satdump legacy autotrack …`
  serve it. In `live` mode pass `--http_server 0.0.0.0:8081` on the command line; in `autotrack` mode the
  web server and the other options come from the config file:
  `satdump legacy autotrack autotrack.json` with `"http_server": "0.0.0.0:8081"` in it. A station run from
  the GUI works in **products-only mode** (inbox only, no RF statistics).
- **`finish_processing: true`, and no server mode.** SatDump runs the offline pipeline (and writes
  `dataset.json`, the products and the composites) only when `finish_processing` is true and no
  `server_address`/`server_port` is set. Without it a pass is recorded but never decoded, and the source
  shows "Pass ended {t} but no dataset.json after 20 min. Is `finish_processing: true` set, and server_mode
  off?".
- **Pin a SatDump version.** The `/api` shape and the `product.cbor` keys differ between versions (1.2.2
  and 2.0.0-alpha differ). Foresignal keeps one fixture set per pinned version; a response that no longer
  matches shows "SatDump /api shape changed (version drift?)".
- Foresignal never requests `/fft.jpeg` (requesting it turns SatDump's FFT on) or `/schedule.jpeg`.

### Parameters

| Param | Type | Default | Meaning |
|---|---|---|---|
| `apiUrl` | LAN URL | — | SatDump's web server, for example `http://192.168.1.60:8081`. Local addresses only; resolved once and pinned. |
| `inboxPath` | inbox path | — | SatDump's output directory under `FORESIGNAL_INBOX_DIR`, for example `satdump/home`. |
| `location` | string | required | `lat,lon[,altM]` or a place id. |
| `satNames` | JSON object | — | Name or NORAD number to `satKey`, overriding the built-in table (`src/main/resources/satdump/sat-names.json`). |
| `apiInterval` | duration | `5s` | 2 s to 30 s. |
| `scanInterval` | duration | `30s` | 5 s to 10 min. Each full scan costs one budget unit. |
| `polarGlob` | glob | `**/*.tif` | Which files of a ready polar pass folder are read as GeoTIFF overlays, relative to the pass folder (`**` crosses directories, `*` and `?` do not; case is ignored). |

Credential (optional, `PUT /v1/sources/satdump-home/credentials`): `ingestKey`, a key you choose
(`eik_` followed by at least 32 random characters) that a remote station presents on the push endpoints
below. Without it only an admin token may push.

At least one of `apiUrl` and `inboxPath` is required (`400 VALIDATION_FAILED` otherwise). The budget is
60 a minute and 50,000 a day; every request stays on your LAN.

```json
PUT /v1/sources/satdump-home
{"enabled": true, "params": {"apiUrl": "http://192.168.1.60:8081", "inboxPath": "satdump/home", "location": "home"}}
```

### What it produces

- **`ground-station`** (`externalId` = the station name, kept 30 days): `state`
  (`idle | tracking | receiving | locked | unknown`), `currentTarget`, `frequencyHz`, `az`, `el`, `snrDb`,
  `nextAos`, `nextLos`, `rotatorEngaged`, `satdumpMode`. Azimuth and elevation are rounded to 1° and SNR to
  1 dB, so a version is written only when something changes. Without `apiUrl` there is one record in state
  `unknown`.
- **`satellite-pass`** (kept 365 days): `receiving` at AOS, `recorded` at LOS with the RF statistics
  (`rf.snrMaxDb`, `snrMeanDb`, `berMin`, `berMean`, `syncedFraction`, lock times and elevations, and an
  SNR-against-elevation series), then `decoded` when the pass folder's products arrive, or `failed` when
  none arrived 20 minutes after LOS although the station had lock. Each composite is stored as media with
  256 and 1024 px thumbnails (`products[].thumbMediaId`). The id is `{station}:{satKey}:{aosEpochMinute}`.
- **Geostationary frames**: GOES-19/18 HRIT and Elektro-L xRIT folders become frames of dynamic products
  `satdump-{station}-{satKey}-{region}-{channel}` (region `fd` when SatDump writes none), kept 72 hours and
  rendered through the normal tile path. A frame is `partial` as soon as one channel arrives and becomes
  complete after 120 s without changes or when the next timestamp folder appears. GK-2A needs KMA keys
  obtained legitimately. FY-4 and MSG HRV are refused ("FY-4/MSG HRV has no usable projection.").
- **Live polar plot** during an active autotrack pass: `GET /v1/ground-stations/satdump-home/polarplot.jpeg`
  (read scope; `404 NO_ACTIVE_PASS` otherwise). Fetched every 15 s by the source, held in memory only,
  never fetched because a client asked.

- **Polar overlays** (prompt 054): every file matching `polarGlob` in a ready pass folder is read as a
  GeoTIFF map overlay; see the next section.

The original images and the provenance files (`dataset.json`, `product.cbor`, any `.tif`) are copied to
`${FORESIGNAL_DATA_DIR}/rasters/satdump/{station}/{passId}/` and deleted after 30 days. `.cadu` and baseband
files are never copied; SatDump's own retention keeps them.

**Health.** The source is `failing` when `apiUrl` is set and `/api` has been unreachable for 3 polls in a
row, and `degraded` for the reasons above and the overlay reasons below. With no `apiUrl` it stays healthy
on the inbox loop alone.

### Polar overlays and footprints (prompt 054)

Foresignal only **reads** GeoTIFFs that SatDump wrote into a pass folder; it never reimplements SatDump's
projection, and no SatDump code is involved (SatDump's source was read for file-format facts only).

**The one producer configuration.** No SatDump version has been confirmed for your station yet
(Weather-Imagery-Design §1.5, question 1), so the documented producer is the default the design names:
**SatDump 1.2.2**, with a `project` block on each composite you want as an overlay, in the pipeline's
composite settings:

```json
"project": {"config": {"type": "equirec", "auto": true}, "img_format": ".tif", "draw_map_overlay": false}
```

SatDump 1.2.2 then writes `rgb_{composite}_projected.tif` next to the composite: an EPSG:4326 GeoTIFF
(one tie point, a pixel scale of about 0.016°, `GeographicTypeGeoKey` 4326). `"auto": true` is needed:
without it, or an explicit `width` and `height`, 1.2.2 logs "No width or height defined for projection!"
and writes nothing. **SatDump 2.0-alpha ignores a preset's `project` keys**, so a 2.0 station gets
thumbnails and approximate swaths only, until the 2.0 hook of prompt 107.

**What happens to each GeoTIFF:**

1. The header is checked first: a BigTIFF, more than 150 megapixels or a side over 12,000 px is refused
   before any pixel is decoded.
2. Only EPSG:4326 and EPSG:3857 are read (PixelIsArea and PixelIsPoint both; a rotated
   ModelTransformation is refused); 8-bit RGB/RGBA and 16-bit grayscale. A file without alpha is
   transparent where every band is zero (or equals `GDAL_NODATA`).
3. It becomes one frame of the dynamic product `satdump-{station}-{composite}` (the file stem without
   `_projected`): `rgba8`, `temporal.type` `pass`, coverage `swath`, style `rgb@1`, kept 30 days, tiled
   like every other product. Processing the same pass again replaces its frame.
4. Its footprint is the outline of the non-transparent pixels, simplified to about 0.02° and split at
   ±180°. The `satellite-pass` record's geometry becomes that footprint (`geometrySource: footprint`),
   and its `products[]` entry gains `productId`, `frameKey` and `tilesTemplate`.

**Approximate swath.** A pass without a usable GeoTIFF (a 2.0 station, a refused file, a failed decode) gets
an approximate swath: the SGP4 ground track (Orekit) from the TLE in `product.cbor`, between the first and
last product timestamps, widened by half the swath on each side (`corr_swath` from the product, else
MSU-MR 2,800 km, AVHRR 2,900 km, MHS 2,200 km). It is a geometry only (`geometrySource: swath`), never
tiles, and never replaces a footprint. Without a TLE the pass keeps the station point.

**Health reasons** (the source shows `degraded` until a later pass is clean):

| Reason | Fix |
|---|---|
| `GeoTIFF CRS EPSG:{n} not supported (build with WITH_GDAL=true)` | Use `"type": "equirec"` (or WebMercator) in the composite's `project`, or build with GDAL |
| `GeoTIFF ModelTransformation is rotated or sheared (not supported; build with WITH_GDAL=true)` | As above |
| `BigTIFF not supported.` | Keep overlays under 4 GB, or build with GDAL |
| `GeoTIFF … px has a side over the 12000 px limit` / `… over the 150 megapixel limit` | Raise `scale_x`/`scale_y` in the projection |

**Optional GDAL.** Build the image with `docker compose build --build-arg WITH_GDAL=true` (or
`WITH_GDAL=true docker compose build`): it installs `gdal-bin` only (no JNI, no AGPL drivers). Foresignal
detects `gdalwarp --version` at startup (`FORESIGNAL_RASTER_GDAL_ENABLED`, default `auto`; `false` turns it
off) and warps refused GeoTIFFs (another CRS, a rotation, BigTIFF) with
`gdalwarp -t_srs EPSG:4326 -of GTiff -co COMPRESS=DEFLATE`, a 120-second timeout and `GDAL_CACHEMAX=256`,
on the raster decode pool, with its files only under `rasters/tmp/`.

### Same host or remote Pi (§4.7)

- **Same host:** `docker compose --profile satdump up -d` runs a `satdump` service with
  `network_mode: host`, `/dev/bus/usb` passed through and the output volume `satdump-output`, which the
  broker mounts read-only at `/data/inbox/satdump/${SATDUMP_STATION:-home}`. Build the image yourself from
  SatDump's own Dockerfile and pin the commit:
  `git clone https://github.com/SatDump/SatDump && git -C SatDump checkout <commit> && docker build -t satdump:local SatDump`
  (or set `SATDUMP_IMAGE`). Write `docker/satdump/autotrack.json` with `"http_server": "0.0.0.0:8081"`,
  `"finish_processing": true` and `"output_folder": "/srv/output"`.
- **Remote Pi at the antenna (recommended in v1):** run SatDump on the Pi and copy its output directory into
  `${FORESIGNAL_INBOX_DIR}/satdump/{station}` with rsync or Syncthing; the 30-second rescan picks it up
  (inotify is not trusted, so a sync tool that renames into place works). Point `apiUrl` at the Pi if it
  is on your LAN.
- **Remote Pi that pushes (prompt 054):** when a sync tool is not an option, the Pi can push with the
  source's `ingestKey` credential (`X-Foresignal-Ingest-Key`, `Authorization: Bearer` or Basic) or an
  admin token:
  - `POST /v1/ingest/satdump-home/rasters`: a whole pass folder as `multipart/form-data`, a `meta` part
    `{"passFolder": "…", "files": ["dataset.json", "product.cbor", "…png", "…tif"], "product": "MSU-MR"}`
    and one `files` part per file. Each file at most 25 MB, the bundle at most 200 MB (`413
    MEDIA_TOO_LARGE`); file and folder names are single path segments. `dataset.json` goes to the pass
    folder and the rest to the `product` directory (default: the single product `dataset.json` lists).
    The bundle is written atomically to `${FORESIGNAL_DATA_DIR}/rasters/satdump/home/incoming/{uploadId}/`
    and processed by the next inbox scan exactly like a pass folder; it is deleted after 2 days. Send one
    bundle per product for a multi-instrument pass. Answers `202 {uploadId, passFolder}`.
  - `POST /v1/ingest/satdump-home/media`: one image (`file`, at most 25 MB) and
    `meta {"composite", "timestamp", "satellite", "passId"?}`; it is checked by the image guard, stored
    with thumbnails and attached to the matching pass (or a new one). Answers
    `202 {mediaId, sha256, passId}`; `404` while the source's stream is not running.

  Each push costs one unit of the source's budget (`429 RATE_LIMITED` with `Retry-After` when spent).
  Other sources answer both paths with `404`.

  ```sh
  curl -H "X-Foresignal-Ingest-Key: $KEY" -F 'meta={"passFolder":"2026-10-12_18-02_meteor","files":["dataset.json","product.cbor","rgb_MSU-MR_221_Composite_projected.tif"],"product":"MSU-MR"};type=application/json' \
       -F files=@dataset.json -F files=@MSU-MR/product.cbor -F files=@MSU-MR/rgb_MSU-MR_221_Composite_projected.tif \
       https://foresignal.example/v1/ingest/satdump-home/rasters
  ```

**Do not use SatDump's webhook plugin** as an ingest path: it posts plain text only.

### Texas targets

| Target | Frequency | Hardware |
|---|---|---|
| GOES-19 HRIT/EMWIN | 1694.1 MHz | about 1 m dish, LNA, LTE filter |
| Meteor-M2-3 / M2-4 LRPT | 137.9 / 137.1 MHz | QFH or V-dipole |
| Metop-B/C AHRPT (optional) | 1701.3 MHz | tracking dish |

Drop `noaa_apt`, `noaa_dsb` and `noaa_hrpt` from SatDump's defaults: NOAA-15, -18 and -19 were
decommissioned in 2025.

### Open owner questions (Weather-Imagery-Design §1.5) and the defaults used

| Question | Default until answered |
|---|---|
| SatDump version and mode at the station | 2.0.0-alpha headless `legacy autotrack` for `/api` and the inbox; fixtures are hand-written from that version's observed keys until a capture replaces them. The polar overlay producer is documented for 1.2.2 (054, the design's default when unanswered); a 2.0 station gets approximate swaths only |
| Bench server or remote Pi | Both work; the remote Pi with rsync/Syncthing is the documented default |
| L-band dish for GOES HRIT, 137 MHz antenna | Both decoders are built; whatever arrives is used |
| 32 GB or 64 GB host | `small` profile |
| Clients outside the LAN | Tile keys and the `web` profile as documented; nothing SatDump-specific |
| Home location and places | `location` names a place when one exists, otherwise coordinates |

### SatDump extras (prompt 107)

**DCS.** Enable `parse_dcs` in SatDump (GOES-R HRIT pipeline) and, at the source, set `tracked_addresses` to
cut volume. Then set `dcsEnabled` on the `satdump-{station}` source and a `dcsBbox` (or configure places, whose
AOI is the default; the built-in default box in force without places does not count): **DCS cannot be enabled
without an effective bbox**, and if places are later removed, no DCS message is processed and health says
so. Put a `dcp_locations.csv`
(`address,lat,lon,name,agency`) at `${FORESIGNAL_INBOX_DIR}/dcs/dcp_locations.csv`; it is re-read on change.
Messages with no location, outside the bbox or not allowlisted are dropped and counted. Over `dcsMaxPerScan`
(500) the newest per address are kept and health degrades with "DCS volume over cap" — tighten `dcsBbox` or
`dcsAddresses`. DCS is hemisphere-wide from a GOES antenna; expect high volume without a filter.

DCS and EMWIN files are read **as they settle**, on every inbox scan, wherever `dcsGlob`/`emwinGlob` match
below `inboxPath` (not only in finished pass folders), so continuous HRIT reception delivers warnings within
about two scan intervals. A file is read once it is unchanged since the previous scan, and once per size and
modification time; `IMAGES` trees are not searched.

**EMWIN.** Enable `emwinEnabled`; products in `emwinProducts` (AWIPS categories) become `emwin-product`
records and P-VTEC warnings with a polygon become `emwin-warning`, filtered to the places AOI. While
`nws-alerts` is healthy the warnings carry `duplicateOf`; when it is failing they do not, and this rule fires:

```json
{"name": "Warnings via satellite (internet down)", "source": "satdump-home", "kind": "emwin-warning",
 "conditions": [{"path": "duplicateOf", "op": "exists", "value": false}],
 "near": {"placeId": "home", "radiusKm": 100}, "mode": "enter", "severity": "warning"}
```

EMWIN is never more authoritative than NWS; messages render "Received via GOES HRIT EMWIN".

Products are applied in issue-time order (the WMO heading's time), and an older product never overrides a
newer one of the same warning, so a late copy of a `NEW` cannot revive a canceled warning. An
`emwin-product` id is `{wmoHeading}:{awipsId}:{issue time}` and its `observedAt` is the issue time.

**Local GLM (GRB).** A GOES GRB station (1686.6 MHz; a larger dish than HRIT) writes L2 LCFA NetCDF files;
set `FORESIGNAL_GLM_LOCAL_STATIONS` to the station names (use the same names as `FORESIGNAL_SATDUMP_STATIONS`)
and point `inboxPath` at the GLM output. Local flashes join the shared AOI ring and de-duplicate against the
internet `goes-glm` in both directions (whichever copy of a flash arrives second is dropped, within 1 s and
0.05°, inside the ring's scope); `lightning-cluster` records keep flowing from local flashes while the
internet feed is down, and the local clusters end as soon as it recovers. A GLM file is read once it has
been unmodified for 10 s; a young file that does not parse is retried. Health degrades if no file arrives for
5 minutes during a pass.

**Elements feed (TLE).** For SatDump, Gpredict and Look4Sat, point the TLE update at Foresignal:
`/compat/celestrak/NORAD/elements/{group}.txt` (groups `weather`, `noaa`, `goes`, `resource`, `amateur`,
`cubesat`, `active`, `stations`, …) or `/compat/celestrak/gp.php?GROUP=…&FORMAT=tle`. It is **served from the
cache only** (never fetched on request); `X-Foresignal-Elements-Age` gives the age and a `Warning` appears
past 3 days. Clients that cannot send headers use a feed key as `?key=fsf_…` (`POST /v1/tile-keys` with the
prefix `/compat/celestrak/`), or enable `openReads`.

**SatDump 2.0 hook.** If the station runs SatDump 2.0 (alpha only as of 2026-10; 1.2.2 is stable), set
`satdumpVersion` to `2.0` and install `contrib/satdump/foresignal_hook.as` (MIT, separate, experimental). It
writes equirectangular composites and a `foresignal-manifest.json` that Foresignal recognizes
version-agnostically: listed GeoTIFFs go through the overlay pipeline, and listed PNGs through the image
guard, georeferenced from the product's `bounds [w,s,e,n]` (`projection: equirec`). An unknown manifest
schema, or a PNG without valid bounds, is rejected with a health reason.

## Space sources (prompt 064)

`celestrak-gp`, `celestrak-socrates`, `satellite-passes` and `launch-library` start enabled (free); `spacetrack-tip`
stays disabled until its Space-Track account is stored. Disable any with `PUT /v1/sources/{id}`.

**`celestrak-gp` (element sets).** CelesTrak's usage policy of 2026-05-22 is strict, and Foresignal
follows it:
- It fetches `GROUP=active` as CSV every 2 h (the policy's own check interval), one request at a time.
  It then fetches at most two `memberGroups` whose membership is a day old, so each group about once a day.
- A repeat inside one CelesTrak update gets a 403 "GP data has not updated since your last successful
  download". That counts as not modified.
- **Any other status, a redirect, or a body that is not CSV/JSON stops the source for 24 hours.** Its
  health turns `failing` with "CelesTrak returned {status}; automatic retries stopped per CelesTrak usage
  policy — investigate, then use fetch-now", and a `system.source_halted` notification goes to every
  channel subscribed to `system`. More than 50 such errors in 2 h get an address firewalled.
- Find out why (the fetch log and `GET /v1/sources/celestrak-gp/health` show the status), then
  `POST /v1/sources/celestrak-gp/fetch`. A manual fetch releases the hold; an ordinary backoff still
  answers `409 IN_BACKOFF`.
- More than 60 MB in a day turns health `degraded` (CelesTrak may block above 100 MB a day).
- `celestrak-socrates` (about 23 MB, refreshed every 10–11 h) follows the same rules and the same
  CelesTrak error budget. It starts enabled; disable it if you do not need conjunctions.

**Feeding Gpredict, Look4Sat and SatDump.** Point clients at Foresignal instead of CelesTrak:
`/compat/celestrak/gp.php` (or `/compat/celestrak/NORAD/elements/gp.php`) serves the stored element
sets in CelesTrak's formats. It never calls CelesTrak. It needs `read` (open while `security.openReads`
is on). For a client that cannot send headers, create a feed key whose prefixes include
`/compat/celestrak/` and whose restriction names the source, and append it as `&key=`:

```http
POST /v1/feed-keys
{"name": "Gpredict", "prefixes": ["/compat/celestrak/"], "restrictions": {"sources": ["celestrak-gp"]}}
```

| Client | URL |
|---|---|
| Gpredict | `https://foresignal.example/compat/celestrak/gp.php?GROUP=stations&FORMAT=tle&key=fsf_…` (one URL per group) |
| Look4Sat | `https://foresignal.example/compat/celestrak/gp.php?GROUP=active&FORMAT=csv&key=fsf_…` |
| SatDump | `kepler_settings.urls_to_fetch`: `http://foresignal.lan:8080/compat/celestrak/gp.php?GROUP=active&FORMAT=omm` |

- Unknown `FORMAT` values (SatDump's `omm`) get CSV, as at CelesTrak. That is what SatDump needs: its
  OMM reader (`ccsdsOmmToKepler`, checked in the SatDump tree of 2026-10-12) splits each line into the
  17 CSV columns. `FORMAT=json` does **not** load in SatDump. SatDump's default source is orbicentral
  (`http://orbicentral.net/gp.php?GROUP=active&FORMAT=omm`).
- TLE formats leave out objects numbered 100000 and above (CelesTrak does not use alpha-5); use CSV or
  JSON to get them.
- `X-Foresignal-Elements-Age` gives the age of the newest element set in seconds. A `Warning` header
  appears when it is over 3 days old, meaning `celestrak-gp` has stopped.

**`satellite-passes`.** This source predicts passes of `norad` (default ISS 25544, METEOR-M2 3 57166 and
METEOR-M2 4 59051) over every non-sensitive point place for 48 h, with Orekit, from the element sets.
It needs `celestrak-gp` enabled.
- The observer height is the place's `altitudeM` attribute, else 0 m.
- A sensitive place is used only when you name it in `places`. Mark the source private if its passes
  should not reach read tokens.
- A SatDump station predicts its own `predictNorad` satellites (default 57166 and 59051, at
  `predictMinElevationDeg` 10). Its `predicted` versions become `receiving`/`recorded`/`decoded` under the
  same id when a session starts within 5 minutes of the predicted AOS. A predicted pass with no session
  by LOS + 20 minutes becomes `missed` while `/api` answered throughout the pass, and is removed otherwise.
- At startup a self-test predicts a bundled ISS pass. If Orekit's output drifts, the log says "Orekit
  self-test FAILED".

**`launch-library`.** It works anonymously: 15 requests an hour, polled every 15 minutes. With a Launch
Library 2 token (`PUT /v1/sources/launch-library/credentials {"values": {"token": "…"}}`) you may lower
the interval to 5 minutes. Do not lower it without a token: the upstream answers 429, and Foresignal
backs off 30 minutes.

**`spacetrack-tip`.** It needs your own Space-Track account (`identity` and `password`). The User
Agreement allows one account per person or entity, and the credentials must never be shared. The data
may not be passed to others without Space-Track's approval, so the source is `LOCAL_ONLY`: it is never
served to feeds, sinks, federation or share links. It queries the last hour's TIP messages hourly. Space-Track
limits TIP to once an hour, or every 10 minutes during a re-entry within 12 hours.

## Transit (prompt 070)

Foresignal ships seven reviewed GTFS-Realtime feeds (`docs/Source-Catalog.md`, "Reviewed transit feeds"): each has a
`transit-{id}` vehicle source and, where the operator publishes alerts, a `transit-alerts-{id}` source. All are
enabled by default (free; disable any with `PUT /v1/sources/{id}`). Vehicles are TRACK records with a 5-minute TTL; keep the
interval at or below 30 s (the default), or shorten the keyframe of the ingest policy with it (225 s at 45 s), because
`keyframe ≤ ttl − interval − 30 s` must hold. The policy validator checks that only against the default interval, so it
will not stop a slower interval on its own. The alerts source takes a `language` parameter (default `en`)
that picks the translation of the alert texts. Entur requires an identifying header; Foresignal sends
`ET-Client-Name: foresignal-{installId}` automatically.

### Adding a transit feed from the Mobility Database

More keyless feeds can be enabled by id from the vendored Mobility Database snapshot
(`src/main/resources/transit/mobility-gtfs-rt.csv`; the snapshot date is in the README beside it). Foresignal never
accepts a transit URL at runtime, so the steps are:

1. **Search.** `GET /v1/transit/catalog?country=FR&q=bus&entityType=vp` (admin token) lists matching rows with their
   `id`, provider, URL, license URL and whether each is already `enabled`. `entityType` is `vp` (vehicle positions),
   `sa` (service alerts) or `tu` (trip updates, which Foresignal does not ingest).
2. **Check the license.** Open the row's `licenseUrl`. When it is empty the catalog does not know the license, and the
   source's license reads "see provider terms": read the provider's own terms before you enable the feed. Catalog feeds
   are egress OPEN (their records may be re-served on every path); the per-source license override arrives with
   prompt 109, so until then do not enable a feed whose terms forbid redistribution.
3. **Set the property.** Add the ids, comma-separated, to `foresignal.transit.catalog-feeds` (environment variable
   `FORESIGNAL_TRANSIT_CATALOG_FEEDS`), for example `FORESIGNAL_TRANSIT_CATALOG_FEEDS=mdb-1602,mdb-1603`.
4. **Restart.** The family reads the property only at startup: a `vp` row becomes `transit-mdb-{id}` and an `sa` row
   `transit-alerts-mdb-{id}` (a row serving both registers both). The search result's `sourceIds` names them. Each URL
   must pass the outbound URL policy (public addresses only); a refused URL, an id missing from the snapshot and a
   trip-updates-only row are skipped with a warning in the log.
5. **Enable** the new sources with `PUT /v1/sources/{id}` like any other.

Why a restart: the set of transit URLs is fixed when Foresignal starts, and each one came from code or from the
reviewed snapshot. An API that accepted a feed URL would let any admin-token holder point Foresignal at an arbitrary
host, which is the open-proxy hole the static feed lists exist to close. Catalog feeds use the header timestamp as
their time base (their vehicle timestamps are unknown), send no extra headers (an Entur sub-feed listed in the
catalog may therefore be refused; use `entur-norway` instead), and the snapshot is refreshed only when a developer
reruns `scripts/update-mobility-catalog.py`.

## Bikeshare (prompt 071)

Foresignal ships 30 reviewed docked bikeshare systems, each a `gbfs-{id}` source of `bikeshare-station` records. All
are enabled by default (free; disable any with `PUT /v1/sources/{id}`):

| Operator family | Sources |
|---|---|
| Lyft-operated | `gbfs-nyc-citibike`, `gbfs-chicago-divvy`, `gbfs-dc-capital-bikeshare`, `gbfs-sf-bay-wheels` |
| Own hosts | `gbfs-boston-bluebikes`, `gbfs-portland-biketown`, `gbfs-buffalo-reddy` |
| publicbikesystem.net | `gbfs-austin-capmetro`, `gbfs-honolulu-biki`, `gbfs-chattanooga-bikechatt`, `gbfs-tucson-tugo`, `gbfs-fort-worth-trinity` |
| BCycle | `gbfs-philadelphia-indego`, `gbfs-la-metro-bike`, `gbfs-boulder-bcycle`, `gbfs-milwaukee-bublr`, `gbfs-madison-bcycle`, `gbfs-nashville-bcycle`, `gbfs-salt-lake-greenbike`, `gbfs-san-antonio-bcycle`, `gbfs-cincinnati-red-bike`, `gbfs-el-paso-bcycle`, `gbfs-indianapolis-pacers`, `gbfs-memphis-bcycle`, `gbfs-des-moines-bcycle`, `gbfs-omaha-heartland`, `gbfs-lincoln-bikelnk`, `gbfs-greenville-sc-bcycle`, `gbfs-las-vegas-rtc-bike-share`, `gbfs-santa-barbara-bcycle` |

Each station is one record: a new version is written when its service state changes (name, capacity, installed,
renting, returning, or `availability`: `out-of-service`, `empty`, `low` at 2 bikes or fewer, `full`, `ok`), while bike
and dock counts arrive as live updates. Rules on `availability` therefore fire on the transitions that matter without
storing a version for every bike taken. The default poll is 60 s; the source honors the feed's own `ttl` and does not
ask again while the last status is still valid. The `language` parameter (default `en`) picks the name of a GBFS 3.0
station where the operator publishes several. No reviewed feed declares a data license, so each source's license reads
"operator terms" and links to the operator's data page: read it before re-serving the data.

**Redirects are refused.** A system that starts answering with a redirect fails its runs with "redirect refused"
until the reviewed URL is updated in code; the redirect target is never requested.

### Proposing another system

Systems are added only by a code change to `GbfsSystems` (`src/main/java/ai/foresignal/source/adapters/gbfs/`),
reviewed like any other change; there is no property or API that adds a URL, and Foresignal never follows a feed's
`gbfs.json` to discover one. A proposal names the system's `station_information.json` and `station_status.json` URLs
(https, docked stations), the GBFS version, and the license from its `system_information.json` (`license_id` or
`license_url`, GBFS 2.3 and later) or, when it declares none, the operator's data terms page. The host allowlist is
derived from the list, so a new host needs nothing else. MobilityData's `systems.csv` is a good place to find
candidates.

## Operating the instance

### Probes and the compose healthcheck

| Endpoint | Includes | Use |
|---|---|---|
| `/actuator/health/liveness` | the application state only, never the database | restart decisions |
| `/actuator/health/readiness` | readiness, database, ingest writer (lock held, state loaded, clock in step), partition headroom | traffic and the compose healthcheck |
| `/actuator/health/catchup` | the catch-up signal (trigger lag over 10,000 seq, or the ingest queue above 80% for a minute) | dashboards; deliberately not part of readiness |

Details are never shown; `GET /v1/system` has them. The compose file checks readiness with
`curl -fsS http://localhost:8080/actuator/health/readiness` every 15 s after a 120 s start period.

### Watchdog and canary

A watchdog proves Foresignal is alive from somewhere else. Create a push check in Healthchecks.io
(or a push monitor in Uptime Kuma), then:

```json
PUT /v1/settings/watchdog
{"heartbeatUrl": "https://hc-ping.com/<uuid>", "failUrl": "https://hc-ping.com/<uuid>/fail",
 "interval": "60s", "method": "GET", "canary": {"enabled": true, "interval": "1h"}}
```

Every `interval` (at least 30 s) Foresignal pings `heartbeatUrl` while readiness is UP, and
`failUrl` (when set) while it is not. If the host dies the pings stop and the monitor alerts. Pings
time out after 5 s, never follow redirects, and their URLs are redacted in logs.

The opt-in **canary** submits one synthetic record to the built-in `system-canary` source every
`canary.interval` and requires its change on the change stream within 30 s
(`foresignal.canary.latency`). A failure sends a `system` notification and switches the watchdog to
`failUrl` until the canary passes again. `system-canary` keeps one day of history and never appears
in tiles.

### `/v1/system`

Anonymous callers get `version`, `apiVersion`, `startedAt`, `uptimeSeconds`, `status`
(`ok`, `degraded` or `down`), `features` and `limits`. A token of at least the `read` scope adds the
database, records, sources, scheduler, writer, streams, notifications, history per tier, catch-up
and `warnings`:

| Warning | Meaning |
|---|---|
| `OPEN_READS_EXPOSED` | Data reads need no token and `publicBaseUrl` or the published bind is not local (loopback, RFC 1918, 100.64/10 tailnets, link-local, unique-local or `.local`) |
| `OPEN_READS_ALL_INTERFACES` (info) | Data reads need no token and the port is published on `0.0.0.0` |
| `HISTORY_RATE_HIGH` | More than 20,000 versions per minute |
| `PARTITION_HEADROOM_LOW` | A tier has less than one partition block attached ahead |
| `RETENTION_CONSUMER_LAG` | The last retention run stopped waiting for a lagging consumer |
| `PUBLIC_BASE_URL_INSECURE` | `publicBaseUrl` is `http://` on a host that is not loopback, private, 100.64/10 or `.local` |
| `PUBLIC_BASE_URL_MISMATCH` | Requests arrive over HTTPS but `publicBaseUrl` is still `http://` |

Compose passes `FORESIGNAL_BIND` into the container so the warning knows the published bind.

### Logs

`FORESIGNAL_LOG_FORMAT` selects the console format: `ecs` (the default in the container), `gelf`,
`logstash`, or `text` (the default elsewhere). Lines logged while a request is served carry its
`traceId` and `spanId` from Micrometer Tracing (traces are never exported); an unexpected error's
`correlationId`, which the caller sees in the `500` body, is logged with its stack trace.

### Settings export and import

`GET /v1/settings/export` returns every setting (sources without credentials, retention, CORS,
notifications, the watchdog, `security`, `server`, trigger rules with their ids, channels without
secrets, LLM providers without their API keys, the LLM roles, and native push providers without their
keys). Tokens, tickets, pairing codes and push devices are never exported. An imported LLM provider
keeps a key it already has here; set the key of a new one with `PUT /v1/llm/providers/{id}`. An
imported push provider likewise keeps its key; a new one is imported disabled until you set its key
with `PUT /v1/push/providers/{providerId}` and enable it.
`PUT /v1/settings/import` applies such a bundle atomically: unknown sources are listed, a source
that needs credentials you have not stored is imported disabled with a warning, rules and channels
are upserted by id (channels keep their stored secrets), and any error rejects the whole bundle.
`POST /v1/system/admin-token/rotate` returns a new admin token once; the old one stops working at
once.

## Running the host

### Binds

Compose publishes the broker on `127.0.0.1:8080` and PostgreSQL on `127.0.0.1:5433`, so a fresh
install is reachable only from the host itself.

- **LAN access:** `FORESIGNAL_BIND=0.0.0.0 docker compose up -d`. Compose passes the bind into the
  container, and `GET /v1/system` then warns `OPEN_READS_ALL_INTERFACES` (and `OPEN_READS_EXPOSED`
  when the address is not private) while `openReads` is on. Read those warnings: open reads mean
  anyone who can reach the port can read every collected record.
- **The database** has its own `FORESIGNAL_DB_BIND`. Leave it on `127.0.0.1`; nothing outside the
  host needs PostgreSQL, and the compose password is a default.

### Remote access and TLS

**Recommended: Tailscale Serve.** It gives the broker a real HTTPS certificate on your tailnet name
(`https://foresignal.<tailnet>.ts.net`) without opening any port on your router.

- **Compose profile:** create an auth key in the Tailscale admin console, then
  `TS_AUTHKEY=tskey-auth-… docker compose --profile tailscale up -d`. The `tailscale` service (pinned
  `tailscale/tailscale:v1.102.5`) joins the tailnet as `foresignal`, keeps its identity in the
  `tailscale-state` volume, and loads `docker/tailscale/serve.json`, which proxies HTTPS 443 to
  `http://foresignal:8080` over the compose network. Funnel (public internet) is off. The broker's
  own port stays on `127.0.0.1`. Enable HTTPS certificates for your tailnet in the admin console
  first.
- **Or on the host:** with Tailscale installed on the host, `tailscale serve --bg 8080` does the same
  for the localhost port.

**Alternative: Caddy.** `docker/caddy/Caddyfile.example` reverse-proxies with automatic HTTPS (a
public name pointing at the host, ports 80 and 443 open) or `tls internal` for a LAN-only name. It
sets `flush_interval -1`, which SSE needs. Foresignal trusts its forwarded headers only when
`FORESIGNAL_TRUSTED_PROXIES` matches the proxy's address (see [Trusted proxies](#trusted-proxies)).

**For map clients: the `web` profile.** Map clients fetch many tiles at once and hold SSE streams open.
Over plain HTTP/1.1 a browser allows about six connections per host, so tiles queue behind each other
and every open stream costs a connection. The `web` profile puts Caddy in front of the broker with
HTTP/2 and HTTP/3 over TLS:

```sh
FORESIGNAL_HOSTNAME=foresignal.local \
FORESIGNAL_TRUSTED_PROXIES='172\.(1[6-9]|2[0-9]|3[01])\.\d{1,3}\.\d{1,3}' \
  docker compose --profile web up -d
```

- The `caddy` service (pinned `caddy:2.11.7-alpine`) publishes `80`, `443/tcp` and `443/udp` (HTTP/3)
  on every interface, and loads `deploy/caddy/Caddyfile`. Its state lives in the `caddy-data` and
  `caddy-config` volumes.
- `FORESIGNAL_HOSTNAME` is the site name (default `foresignal.local`); it must resolve to the host
  through mDNS, your DNS or the clients' hosts files.
- **Certificates:** `tls internal` by default: Caddy runs its own local certificate authority and
  issues the site certificate from it. Clients must trust that authority's root (below). For a
  publicly trusted certificate without opening port 80, use DNS-01: build a Caddy binary with your DNS
  provider's module (`xcaddy build --with github.com/caddy-dns/cloudflare`, or a Dockerfile `FROM
  caddy:2.11.7-builder-alpine` running that command), use that image, and replace `tls internal`
  with the commented `tls { dns … }` block in the Caddyfile. The stock image has no DNS modules.
- **Routes:** `/v1/*`, `/mcp*` and `/actuator/health*` go to the broker (with `flush_interval -1` for
  SSE); everything else is served from `./deploy/web` with a single-page-app fallback. Copy a separately
  built web client there: it is then same-origin with the API, so it makes no CORS preflights.
- **Compression:** zstd or gzip for JSON, vector tiles and text; PNG tiles and SSE are never
  re-encoded.
- **Trusted proxies:** Caddy reaches the broker from the compose network, so set
  `FORESIGNAL_TRUSTED_PROXIES` to that network as above (check it with
  `docker network inspect`; the compose network is named after the project, for example
  `foresignal_default`). Otherwise every client appears as Caddy's address in
  the audit log, the rate limits and the auth-failure backoff.
- Set `publicBaseUrl` to `https://<FORESIGNAL_HOSTNAME>` so feed-key and tile-key URLs, notification
  links and pairing codes use the HTTPS name.
- Plain `http://<host>:8080` keeps working for agents, scripts and tools.
- Caddy's access log (container stdout) keeps only the first eight characters of a path key and
  removes `?ticket=` values.

**Trusting Caddy's local CA.** Export the root certificate once:

```sh
docker compose cp caddy:/data/caddy/pki/authorities/local/root.crt ./foresignal-root.crt
```

Then install `foresignal-root.crt` on each client:

- **macOS:** open it in Keychain Access, add it to the System keychain, open the certificate, and set
  "When using this certificate" to **Always Trust** (or
  `sudo security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain foresignal-root.crt`).
- **Windows:** `certutil -addstore -f Root foresignal-root.crt` from an administrator prompt, or
  double-click it and import it into **Trusted Root Certification Authorities** for the local machine.
- **Linux:** Debian and Ubuntu: copy it to `/usr/local/share/ca-certificates/foresignal-root.crt` and
  run `sudo update-ca-certificates`; Fedora: copy it to `/etc/pki/ca-trust/source/anchors/` and run
  `sudo update-ca-trust`. Firefox keeps its own store: import it under Settings → Privacy & Security →
  Certificates, or enable `security.enterprise_roots.enabled`.
- **Android:** Settings → Security → Encryption & credentials → Install a certificate → CA certificate.
  Browsers then trust it, but **apps targeting API level 24 or later do not trust user-installed CAs**
  unless their `network_security_config` allows them. A Flutter client that talks to the `web` profile
  must ship that configuration, for example a `res/xml/network_security_config.xml` with
  `<trust-anchors><certificates src="system"/><certificates src="user"/></trust-anchors>`, referenced
  from `AndroidManifest.xml` with `android:networkSecurityConfig`.
- **iOS and iPadOS:** send the file to the device (AirDrop, Mail or a download), install the profile under
  Settings → General → VPN & Device Management, then enable full trust under Settings → General →
  About → Certificate Trust Settings. Apps then trust it too.

Whichever you use:

- set the `server` setting's `publicBaseUrl` to the HTTPS name with `PUT /v1/settings/server`, so
  notification links and pairing QR codes work from a phone (an `http://` URL on a public host
  answers with `PUBLIC_BASE_URL_INSECURE`);
- **never expose port 5433**;
- plain HTTP port forwarding from the internet is **unsupported**: the token would cross the network
  in clear text.

### mDNS

`docker/avahi/foresignal.service` lets LAN clients discover the broker. On a host running Avahi:

```sh
sudo cp docker/avahi/foresignal.service /etc/avahi/services/
```

It advertises `_http._tcp` and `_foresignal._tcp` on port 8080 with exactly two TXT records,
`path=/v1` and `api=v1`. It deliberately carries **no install id or other stable identifier**:
anyone on the network can read mDNS, and a stable value would let the device be recognized and
tracked wherever it goes. Clients ask `GET /v1/system` once connected. mDNS only helps when the broker
is bound to the LAN (`FORESIGNAL_BIND=0.0.0.0`).

### Clock and NTP

Every version's `ingested_at` comes from the host clock, and asOf, retention and the change cursor
depend on it never going backwards. **Sync time before Foresignal starts:** use chrony or
systemd-timesyncd and check with `timedatectl` (`System clock synchronized: yes`).

- If the clock steps **backwards** past the tolerance, the writer **holds ingest** (readiness goes down and
  `GET /v1/system` `ingestWriter` says why) until the clock catches up with the last stored time; small steps are absorbed.
- A **forward** jump is only counted and logged; it never blocks, but versions written during it carry
  the wrong time.

### Storage projection

At full load (every planned source, Core-Data-Model §5.3 budgets), expect about **10–15 GB/day of
history writes plus 15–20 GB/day of WAL**. The TRACK window (3 days) holds about **40 million rows,
roughly 20 GB including indexes**. Size the disk for the STANDARD windows on top (1825 days for events).

- `GET /v1/system` (with the token) reports `history` per tier: partitions, rows, bytes, headroom,
  window, plus `versionsPerMinute` and `projectedBytes`; above 20,000 versions per minute it warns
  `HISTORY_RATE_HIGH`.
- Each source's health has `storage.projectedBytes` (its 7-day average bytes per day times its
  window) and `persistRatio`.

The compose database runs with the flags Core-Data-Model §16 sets for sustained writes:
`shared_buffers=512MB`, `max_wal_size=8GB`, `checkpoint_timeout=15min`, `wal_compression=lz4`,
`max_locks_per_transaction=256` (many partitions) and `autovacuum_vacuum_cost_limit=2000`. A
standalone PostgreSQL should use the same. The broker also sets `reWriteBatchedInserts=true` on its
JDBC connection.

### Backups

A complete backup is **two things**: a database dump and the secret key file.

```sh
docker compose exec -T db pg_dump -U foresignal -Fc foresignal > foresignal-$(date +%F).dump
docker compose cp foresignal:/data/secret.key ./secret.key   # FORESIGNAL_DATA_DIR in the container
```

- `pg_dump -Fc` dumps and restores the partitioned history like any other table.
- **Source credentials and channel secrets cannot be decrypted without `secret.key`.** A dump without
  it restores everything except those secrets, which you would have to enter again. Keep the key with
  the dump, but not next to it on the same disk, and treat it like a password.
- The admin token's hash is in the database, so a restored instance accepts the token of the
  instance it came from.

**Restore** (stop Foresignal first, so the writer is not running):

```sh
docker compose stop foresignal
docker compose exec -T db dropdb -U foresignal foresignal
docker compose exec -T db createdb -U foresignal foresignal
docker compose exec -T db pg_restore -U foresignal -d foresignal --no-owner < foresignal-2026-10-11.dump
docker compose cp ./secret.key foresignal:/data/secret.key
docker compose start foresignal
```

**Drill it.** Restore into a scratch database now and then and check it, without touching the live
one:

```sh
docker compose exec -T db createdb -U foresignal restore_drill
docker compose exec -T db pg_restore -U foresignal -d restore_drill --no-owner < foresignal-2026-10-11.dump
docker compose exec -T db psql -U foresignal -d restore_drill -c "select count(*) from record_head"
docker compose exec -T db dropdb -U foresignal restore_drill
```

### Troubleshooting

| Symptom | Look at |
|---|---|
| A source shows no data | `GET /v1/sources/{id}/health` (`status`, `statusReason`, `lastError`), then `GET /v1/sources/{id}/fetches?limit=5` (HTTP status, error, `versionsWritten`), then `POST /v1/sources/{id}/probe` to see the raw upstream answer |
| Many sources unhappy at once | `GET /v1/sources/health?status=failing,degraded` |
| Fetches are `REFUSED` | The budget is spent: raise `budget` or lengthen `interval` |
| The container restarts or is unhealthy | `/actuator/health/readiness` (database, ingest writer, partitions; details are in `GET /v1/system`); `docker compose logs foresignal` |
| `GET /v1/system` `ingestWriter` says the clock is behind | The host clock went backwards: fix NTP (above) |
| Alerts or streams lag | `/actuator/health/catchup` and `GET /v1/system` `catchUp` (trigger lag, ingest queue) |
| Disk filling | `GET /v1/system` `history` and `warnings`, each source's `storage.projectedBytes`, then the retention windows |
| A smoke test against the instance | `FORESIGNAL_TOKEN=fsr_… scripts/smoke.sh` (it enables `usgs-earthquakes`) |

## Humanitarian source credentials (prompt 090)

- **ReliefWeb (`reliefweb-disasters`)** needs an **approved appname**, not just any string. Request one from the
  form at <https://apidoc.reliefweb.int/parameters#appname>, then set it as the source's `appname` credential.
  ReliefWeb answers HTTP 403 "You are not using an approved appname" without one; Foresignal reports that as
  `APPNAME_NOT_APPROVED` and does not retry until the credential changes.
- **HDX HAPI (the optional `hdx-hapi-*` family)** is not built in prompt 090; it would need an `appName` and
  `email` sent as `app_identifier = base64("<appName>:<email>")` (requests without it get HTTP 403 "Invalid app
  identifier"). If the family is added later, set both credentials on each HAPI source.
- The other prompt 090 sources (`nasa-eonet`, `unhcr-population`, `who-don`, `copernicus-ems`,
  `usgs-hans-volcanoes`, `geonet-volcanoes`, `openfema-declarations`, `travel-advisory-us`) are keyless.

## Sanctions lists and the shrink guard (prompt 091)

- **SEC 8-K:** the SEC requires a declared User-Agent with contact details. Set the `contactEmail` credential on
  `sec-8k` (`PUT /v1/sources/sec-8k/credentials`); the shared client appends it to Foresignal's own User-Agent as
  `; contact=<email>`. Until it is set the fetch is refused (`credentials rejected`), unless the optional
  `foresignal.adapters.sec-8k.contact-email` property supplies a fallback. Never replace the User-Agent with
  another product's.
- **OFAC and UN redirects:** both export URLs answer 302 to a pre-signed object (S3 in `us-gov-west-1`, Azure Blob
  `unsolprodfiles.blob.core.windows.net`). Only those hosts are allowed. The whole query string of a pre-signed
  URL (AWS `X-Amz-Signature`/`X-Amz-Credential`, Azure `sig`) is replaced by `***` wherever a URL is stored or
  logged (fetch log, traces, error messages). The UN blob is change-checked by its ETag (a conditional GET); the
  OFAC export by its publication path and the SHA-256 of the file. Nothing to configure.
- **The delisting shrink guard (`LIST_SHRINK_GUARD`):** a sanctions list normally only grows a little between
  runs. If a run's entry count is below 90 % of the previous run's, Foresignal writes **no** tombstones, keeps the
  previous run's id set as the baseline, marks the source degraded with a reason starting `LIST_SHRINK_GUARD`
  (naming the list and both counts), and raises a `system.sanctions_list_shrink` notification, because a sudden
  large shrink is almost always an upstream fault (a bad export, a truncated download). Running the fetch again
  does not accept the smaller list: the guard keeps tripping against the kept baseline.
- **Accepting a genuinely large delisting:** verify the upstream list first, then run one manual fetch with the
  override: `POST /v1/sources/{sourceId}/fetch?allowShrink=true` (the same one-off override as the complete-set
  reconciliation below). That run writes the tombstones, stores the smaller list as the new baseline and clears
  the degraded reason; the next ordinary run re-applies the guard. **Note:** the override reaches the sanctions
  adapters through the fetch context, which the fetch executor must populate from the request; until it does, the
  override has no effect on these three sources (see the 091 completion report).
- **The UK source changed:** `uk-sanctions-list` (the FCDO UK Sanctions List) replaced `uk-ofsi-sanctions`,
  whose list closed on 28 January 2026.

## Internet-measurement sources (prompt 092)

- **Cloudflare Radar (`cloudflare-radar-outages`, `cloudflare-radar-bgp`):** create an API token at
  <https://dash.cloudflare.com/profile/api-tokens> with **Radar read** access and set it as the `apiToken`
  credential. Cloudflare's global limit is 1,200 requests per 5 minutes per user; Foresignal's budget is far
  below that and honors `Retry-After` on 429.
- **`gpsjam-daily` starts enabled** like every free source (it needs no key or parameter). gpsjam.org publishes no
  license, so the source is local-only; disable it with `PUT /v1/sources/gpsjam-daily` if you do not want it.
- **RIPE Atlas:** `ripe-atlas-probes` never stores a probe's address or description; only status, prefixes,
  ASNs, country and tags. License: RIPE Atlas Service Terms v3.5.
- **Rule recipes:** BGP hijacks — enter on `bgp-hijack` when `confidence_score >= 8`; service status — enter on
  `service-status` when `indicator in [major, critical]`. Cloudflare outages pair with IODA (`ioda-outages`) in
  the 099 corroboration layer to distinguish a national outage from a cable cut or a hijack.
- **RIS Live (`ris-live`) is optional and stays disabled until you set `prefixes`.** Set `prefixes` (at most 50) to the prefixes you
  watch; Foresignal subscribes to those (with their more-specifics) and never to the full firehose. Optional:
  `expectedOrigins` (`prefix=ASN,ASN;prefix2=ASN`) to flag an unexpected origin, and `hosts` (RRC names such as
  `rrc00`) to narrow the subscription to those collectors. While the stream is down, records say
  `status: no-data` rather than reporting zero visibility.
- **RIPE Atlas intraday:** to refresh disconnected probes between daily dumps, set `intradayDisconnected: true`
  and the source's interval to 30 minutes; the dump is still checked only every 6 hours.

## Reference datasets (prompt 094)

- **They poll weekly on their own.** `wri-power-plants`, `ourairports`, `submarine-cables`, `gvp-volcanoes`,
  `world-port-index`, `nps-boundaries`, `spaceports`, `nuclear-reactors-us` and `nuclear-plants-wikidata` are
  enabled by default like every free source; disable the ones you do not want. Each keeps 730 days of history and
  emits one `dataset-release` record per run.
- **`allowShrink`.** A reference dataset normally changes slowly, so if a complete run would remove more than
  `reconcile.maxAbsentRatio` of it (20 % by default), Foresignal writes no tombstones and shows the source
  degraded ("dataset shrank by N% — reconciliation skipped"): a truncated download must never read as a mass
  deletion. The skipped reconciliation also leaves a `DISCARDED` row in the fetch log, carrying the run's
  `runId` and the reason. After you have verified the smaller upstream dataset is genuine, release the guard
  **once** with `POST /v1/sources/{sourceId}/fetch?allowShrink=true`: that run re-reads every file in full
  (it sends no stored `ETag`/`Last-Modified`, so it cannot be answered with a 304) and reconciles. The
  degradation clears as soon as a reconciliation runs, after the override or after an ordinary complete run
  whose dataset is back within the ratio. The next ordinary run re-applies the guard.
- **License flags you should read before enabling:**
  - `submarine-cables` is **non-commercial**: the public Submarine Cable Map is CC BY-SA 4.0, the geometry is
    stylized and TeleGeography's underlying database is subscriber-only. Egress is local-only for prompts
    109/110 and the attribution carries `nonCommercial: true`. Display it, attribute TeleGeography, do not
    redistribute.
  - `gvp-volcanoes` uses the Smithsonian Terms of Use (**non-commercial; cite the VOTW DOI**). The attribution
    names Volcanoes of the World v5.4.0 (2026) and `doi:10.5479/si.GVP.VOTW5-2026.5.4`.
- **Keys:** none. `nuclear-plants-wikidata` sends Foresignal's own User-Agent with contact; set `contactEmail`
  so Wikidata can reach you, and it honors `429`/`Retry-After`.
- **Excluded upstreams:** PeeringDB facilities (AUP forbids storage/reproduction — ask
  support@peeringdb.com first), IAEA PRIS (no open license), GEM trackers (request form, no stable file URL)
  and the NRC dataset pages (blocked for scripted clients) are noted, not built.

## Citizen science and signals (prompt 095)

- **`inat-observations` needs an area.** iNaturalist asks that its API be used for application development, not
  scraping, so the `aoi` parameter (a `west,south,east,north` box or a place id) is required and the source
  cannot be enabled without it. A box must not cross the antimeridian (split it into two sources) and may be at
  most 15° on a side. Each run reads one round of revisions (quality-grade changes, re-identifications) and new
  observations in the area, sending no more requests than the per-minute budget (2 by default: one page of each);
  a busy area catches up over the following runs from the stored cursor, and a run inside the upstream's
  `Cache-Control: max-age` sends nothing.
- **The privacy choices are deliberate.** No contributor usernames, descriptions or comments are ever stored.
  Obscured observations keep their randomized upstream coordinates with the upstream's own positional accuracy;
  `private` observations and observations without coordinates are skipped. iNaturalist photo links are kept only
  when the photo's license is open and its host is exactly the open-data bucket
  (`inaturalist-open-data.s3.amazonaws.com`); licenses are per record (see `licenseCode`).
- **`ebird-notable` is off by default and non-commercial.** It needs your own eBird key (`apiKey`, one per eBird
  account). Private locations are rounded to 0.05 degrees with a 5000 m accuracy, and neither their place name
  nor their eBird location id is stored. eBird's terms are non-commercial with attribution and the source is
  local-only for 109/110.
- **CDC wastewater.** `cdc-nwss-wval` reads site viral-activity levels and `cdc-nwss-detections` reads pathogen
  detections, both from the current datasets (the pre-September-2025 datasets are archived). Add an optional
  Socrata `appToken` to avoid the shared anonymous IP pool and its 429s. Each run sends at most its per-minute
  budget of requests (4), shared among the configured detection datasets in turn, and a weekly update larger
  than that is read over the following runs; only the latest week (or sample) of each site is kept current, so a
  re-dated older row never replaces a newer one. **A detection flip (`detected == true`)
  is the signal**; a rule with `detected == true` in `enter` catches it. All rows are public-domain US
  government data **except WastewaterSCAN rows, which are CC BY-NC 4.0**; those are dropped unless
  `includeWastewaterScan` is set, and then each carries `licenceOverride: "CC BY-NC 4.0 (WastewaterSCAN)"`, which
  109 honors.
- **`wiki-edit-surge` is free but starts disabled: you opt in.** It is the one free source held off by default
  (prompt 146), because it holds one connection to the Wikimedia EventStreams `recentchange` firehose and reports
  **the bytes it reads** against a 3 GB/day budget. `POST /v1/sources/enable-free` skips it; enable it with
  `PUT /v1/sources/wiki-edit-surge` and `{"enabled": true}`, then measure your own volume. It never stores usernames, edit comments
  or parsed comments; the page title is untrusted text and is the only embedded field. A useful rule:
  `phase == rising` in `enter`, with a **6 h cooldown** so a long surge does not repeat.

## Satellite scenes (prompt 096)

- **You must set an AOI.** `stac-sentinel2` and `stac-landsat` require the `aoi` parameter — a box
  `west,south,east,north` or a place id — so enabling either fails with `400 VALIDATION_FAILED` until you
  supply one. A box that crosses the antimeridian is split into two searches automatically. A place id searches
  the place's own area (its shape, radius or buffer), and `aoiCoverPct` is measured against that area.
- **Links only; no imagery is ever downloaded.** A `satellite-scene` record carries the footprint, the
  acquisition time, the cloud cover, the fraction of your AOI the footprint covers (`aoiCoverPct`), the STAC
  item link (`stacItemUrl`) and, only when the collection publishes a public one, an `https://` thumbnail.
  Landsat and Sentinel-1 thumbnails live in requester-pays S3 buckets (`s3://…`), so Foresignal drops them
  rather than rewriting or signing them; open `stacItemUrl` to get the full set of assets yourself.
- **Rules.** A rule is just `kind=satellite-scene` plus payload conditions, for example
  `cloudCover < 20 AND aoiCoverPct >= 80` to catch a clear scene over your area:
  ```json
  {"name": "Clear Sentinel-2 scene over home", "kinds": ["satellite-scene"],
   "sources": ["stac-sentinel2"], "match": "match", "onlyNew": true,
   "enter": "cloudCover < 20 AND aoiCoverPct >= 80"}
  ```
  Use `only_new` (`onlyNew`) so a reprocessed item that only moves `properties.updated` is a touch, not a new
  alert. `cloudCover` is percent (one decimal); `aoiCoverPct` is an integer 0–100.
- **Cloudy-season flood watching with Sentinel-1.** SAR sees through cloud, so enable `stac-sentinel1`
  (it too needs `aoi`) and watch `kind=satellite-scene`, `sources=["stac-sentinel1"]` with a
  `aoiCoverPct >= 90` rule: a new Sentinel-1 scene over your flood AOI is a fresh, cloud-independent look.
  Sentinel-1 has no `cloudCover`, so leave it out of the conditions.
- **Cadence and quota.** Each run searches your AOI hourly (min 15 minutes) at 4 requests/minute and 300/day;
  the first run looks `lookbackDays` (default 3) behind, later runs start two days behind the newest scene
  already seen. `maxPages` (default 5, at most 10) caps the pages of 100 read per run, and a run never sends more
  searches than the per-minute budget (4), shared by the parts of a split AOI; when that ends a search early the
  log says so. A cloudy week returns nothing, which is normal (no silent-break warning).

## Raster exports and offline packs (prompt 108)

Async raster exports and place offline packs write files under
`${FORESIGNAL_DATA_DIR}/exports/{exportId}/` and keep them for 48 h; they count against the raster disk
quota. Submit an export with `POST /v1/products/{id}/exports` (`format`: `mbtiles`, `pmtiles` or `cog`,
plus `frameKey`/`frameKeys`, `bbox` or `placeId`+`radiusKm`, `minZoom`, `maxZoom`, `style`, `tms`), poll
`GET /v1/exports/{id}`, and download via `GET /v1/exports/{id}/files/{name}`. Caps: 50,000 tiles per
tileset and 2 GB per file. `POST /v1/places/{id}/pack` builds a ZIP (≤ 50 MB) of place weather, forecast
series, nearby alerts, legends, a loop GIF and one PMTiles per product.

A sweep (every 15 minutes, `foresignal.raster.export.sweep-interval`) deletes jobs and their files once
`expiresAt` passes, finished or not, and fails jobs that can no longer finish: those still queued or
running when the process restarted (submit them again) and any stuck for more than 6 hours. MBTiles and
PMTiles hold WebMercatorQuad tiles only (`tms=WorldCRS84Quad` is 400); a COG follows `tms`
(WebMercatorQuad → EPSG:3857, WorldCRS84Quad → EPSG:4326). Every file embeds the product's attribution and
license (MBTiles/PMTiles `attribution` and `description`, the TIFF `Copyright` tag and `GDAL_METADATA`,
the pack's `manifest.json`), with a "changes made" note for CC BY and ECMWF products. A place pack only
holds products the requesting token may read, and the same request from the same token within 10 minutes
returns the pack already built.

WebP tiles need the bundled `webp-imageio` native library. A missing native (an unsupported platform)
disables WebP: `/v1/system` reports a `WEBP_UNAVAILABLE` warning and `.webp` answers `406
FORMAT_UNAVAILABLE`. Set `notifications.imageLinks` only on an instance with an https `publicBaseUrl`;
enabling it on a LAN-only instance exposes one media path per feed key and is refused without https.

## Licenses and egress (prompt 109)

Every source resolves to structured license terms (`ai.foresignal.provenance.LicenceTerms`, from
`provenance/licences.json`): `commercialUse`, `shareAlike`, `attributionRequired` and `redistribution`.
`UNKNOWN` is treated as `FORBIDDEN` (commercial) and `LOCAL_ONLY` (redistribution). Free-text descriptor
licenses resolve through catalog aliases; provider terms that state no license resolve to `provider-terms`,
`provider-terms-nc` or `unspecified` (redistribution `UNKNOWN`, so local use only). A DERIVED source takes
the most restrictive terms of its inputs (`derivedFrom`, else the sources its generator subscribes to). A
custom PUSH source may declare its own `licenceTerms`; each PUSH batch snapshots their id.

A version's license is the one snapshotted on its batch's `fetch_log` row, else the source's current terms
(`licenceBasis: descriptor`). A peer record's `_foresignal.originLicence` overrides the batch license, and an
MLAT position is downgraded to `LOCAL_ONLY` while keeping its license id and attribution. Fetch rows that wrote
versions are kept for `max(retention.provenance, the largest version window)`. `include=provenance` returns
`fetchId`, `adapterVersion`, the ingest `principal` (admins only), the Admiralty `reliability` and the
`credibility` (1–6, computed at read time), composed as a `grade` (e.g. `B2`).

**Reliability.** Unless a descriptor sets its own grade, official agencies (US federal public domain and the
open government licenses) get `B`, your own receivers and other built-in providers `C`, community and
news-derived feeds (GDELT, Wikipedia surge) `D`, and custom, peer and DERIVED sources `F`. Override it per
source with `PUT /v1/sources/{id}` `{"reliability": "A"}`; `null` reverts to the default. A news article used as
evidence takes the grade of its outlet's factuality instead (`media.reliabilityMap`, prompt 133).

**License-filtered reads.** Clients send `Foresignal-Licence-Policy: no-nc, no-sa, redistributable` (or
`licencePolicy=`). A token's `licencePolicy` is a floor a request may add to but never loosen; set it at
creation or replace it with `PATCH /v1/tokens/{tokenId}` `{"licencePolicy": [...]}` (admin; `[]` clears it).
JSON pages and GeoJSON collections carry `licenceFiltered` (records dropped) and aggregated `attributions`,
plus a `Foresignal-Attribution` header (ISO-8859-1, at most 1 KB, cut with `...`) and
`Link: <termsUrl>; rel="license"`. Record reads send `Vary: …, Foresignal-Licence-Policy`. Tiles, the SSE
change stream and `/v1/changes` prune excluded sources in SQL and send `Foresignal-Licence-Filtered: <n>`, the
number of *sources* the policy pruned (their records are never read, so they cannot be counted). A layer's
TileJSON `attribution` aggregates its sources.

**Egress.** `EgressPolicy` is the one component that decides whether a record may leave, on every path; its
license step (`LicenceEgressGuard`) is internal and is applied on every path, `EXPORT` and `ARCHIVE` included.
In order:

1. An unregistered source never leaves.
2. A private source is excluded on every path except `API_READ`, `TILE`, `SSE` and `MCP` for callers allowed
   to see it, and `ARCHIVE` (your own storage), whatever its license. Two explicit exceptions: an `EXPORT` by
   an admin with `includePrivate=true` (111), and a sink with `filter.includePrivate=true` that is a SERVE sink
   bound to loopback/private or a PUSH sink whose target resolves to a private address (110). Feeds, share
   links and federation never carry private records.
3. A source whose effective `egress` is `LOCAL_ONLY` is served to this instance's own clients, exports and the
   archive, and to a LAN-serving sink bound to loopback/private only; never pushed, shared, federated or put
   on a feed.
4. An MLAT position never leaves on a sink push, a share, a feed or federation, nor on a LAN sink that is not
   local.
5. The record's license goes through the table below. Turning a local receiver's egress to `OPEN` (only the
   `egressOverridable` families allow it) also lifts its `LOCAL_ONLY` license default; `FORBIDDEN` terms are
   never lifted.

Denials are counted as `foresignal.licence.egress.denied{path,licence}`.

| Redistribution | API_READ / TILE / SSE / MCP / LLM_* / NOTIFICATION / FEED / ARCHIVE | SINK_SERVE_LAN | SINK_PUSH, EXPORT | PUBLIC_SHARE, FEDERATION, SHARE |
|---|---|---|---|---|
| ALLOWED | yes | yes | yes | yes (NC terms: yes; attribution always attached) |
| LOCAL_ONLY | yes | yes, only if the sink binds loopback/private | no (EXPORT: only with `personalUse=true`) | no |
| FORBIDDEN | yes (own use) | no | no (EXPORT: only with `personalUse=true`, sidecar marks "do not redistribute") | no |

So `opensky-states`, `acled` and `aisstream` (`FORBIDDEN`) are exported only with `personalUse=true` and never
reach a sink, a share link or a peer.

**Signing.** The instance Ed25519 signing key is published at `GET /.well-known/jwks.json` (and
`GET /v1/system/jwks`), `Cache-Control: public, max-age=3600`; `POST /v1/system/signing-key/rotate` retires the
active key, activates a new one and writes a `signing_key` audit event. Retired keys stay published for
`signing.retainRetired` (365 days). A stored briefing run carries `signature`, a detached JWS (`b64:false`)
over its markdown; `GET /v1/briefings/{id}/runs/{runId}` sends it as `Foresignal-Signature`, and the HTML
rendition sends a signature over its own bytes. Verify it with any JOSE library, for example Python
`jwcrypto`:

```python
import json, urllib.request
from jwcrypto import jwk, jws
jwks = json.load(urllib.request.urlopen("https://host/.well-known/jwks.json"))
key = jwk.JWK(**jwks["keys"][0])
signed = jws.JWS(payload)          # payload = the exact response bytes
signed.deserialize(signature, key)  # signature = the Foresignal-Signature header
```

or without one, from the definition (RFC 7797: the signing input is the protected header, a dot and the
unencoded payload), with the `cryptography` package:

```python
import base64, json
from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PublicKey

def b64url(s): return base64.urlsafe_b64decode(s + "=" * (-len(s) % 4))

header_b64, _, signature_b64 = signature.split(".")   # "header..signature"
header = json.loads(b64url(header_b64))
assert header["alg"] in ("Ed25519", "EdDSA") and header["b64"] is False and header["crit"] == ["b64"]
jwk_ = next(k for k in jwks["keys"] if k["kid"] == header["kid"])
Ed25519PublicKey.from_public_bytes(b64url(jwk_["x"])).verify(
    b64url(signature_b64), header_b64.encode("ascii") + b"." + payload)   # raises if invalid
```

## Outbound sinks (prompt 110)

Outbound sinks let existing ecosystems consume Foresignal with no new client code. Each sink is created
disabled (`POST /v1/sinks` with `enabled=false` unless set true); enabling starts it on its own virtual
thread with a durable cursor under `sink`/`sink_cursor`. Sinks are an egress path like any other: every
record goes through `EgressPolicy` (the one egress decision, 109) on `SINK_SERVE_LAN` (SERVE sinks) or
`SINK_PUSH` (PUSH sinks), so the private flag, the descriptor's `egress` (with your overrides), the MLAT
rule and the license table all apply.

| Type | Kind | Mode | Client setup |
|---|---|---|---|
| `mqtt-state` | STATE_MIRROR | PUSH | Retained `{prefix}/state/{source}/{externalId}` (QoS 1); LWT `{prefix}/status` |
| `cot` | STATE_MIRROR | PUSH/SERVE | ATAK/iTAK/WinTAK: UDP multicast SA (default `239.2.3.1:6969`, LAN) or a TCP network input |
| `readsb-json` | STATE_MIRROR | SERVE | tar1090 / Virtual Radar Server: `http://host:port/data/aircraft.json` |
| `sbs` | STATE_MIRROR | SERVE | BaseStation client: TCP `host:port` |
| `nmea-aivdm` | STATE_MIRROR | SERVE | OpenCPN / Signal K: NMEA 0183 TCP provider `host:port` |
| `aprs-is-serve` | STATE_MIRROR | SERVE | Xastir / YAAC / APRSdroid: APRS-IS server `host:port` |
| `cloudevents` | CHANGE_FEED | PUSH | HTTP receiver; verify `X-Foresignal-Signature` |

**What never leaves.** OpenSky, ACLED and aisstream records are never re-served on any sink (their terms
forbid redistribution). `LOCAL_ONLY` records — sources whose descriptor `egress` is `LOCAL_ONLY` (for
example `eia-petroleum`, even though its license is public domain), the operator's own receivers, records
whose license is unknown, and every MLAT position — reach only SERVE sinks bound to loopback or a private
address, never a PUSH sink. Setting an overridable local receiver's egress to `OPEN`
(`PUT /v1/sources/{id}`) lets its non-MLAT records reach PUSH sinks too. A private source needs
`filter.includePrivate=true` **and** a local target (a SERVE sink, a PUSH target that resolves to a private
address, or the CoT LAN multicast group).

**Denied records.** A record the policy refuses is skipped and counted, never retried: a CHANGE_FEED cursor
moves past it, so one denied record cannot stall the sink or hold back retention. Denials are counted as
`foresignal.sink.denied{sink,reason}` (reasons `private`, `local-only`, `mlat`, `licence-forbidden`,
`licence-local-only`, `unregistered`) and shown, per reason and per source, under
`GET /v1/sinks/{id}/status` → `denied` (written to the cursor every few seconds).

**Filters.** `filter.sources`, `filter.kinds` and `filter.bbox` select records; `filter.placeId` keeps
records whose geometry intersects the place; `filter.watchlistId` keeps records whose watched identity
(icao24, registration, callsign, mmsi, imo, read from the payload field the source declares) is on the list,
or whose external id is on a `custom` list. A filter that names a deleted place or list matches nothing.

**Delivery and resync.**

- *CHANGE_FEED* (`cloudevents`): ordered by seq, at least once. The cursor is persisted after every
  acknowledged batch (`config.batchSize` 1–100; above 1 the body is `application/cloudevents-batch+json`). A
  new sink, a sink whose cursor fell below the purge horizon, and `POST /v1/sinks/{id}/resync` first replay
  current state as `ai.foresignal.record.snapshot` events, then continue with changes after the snapshot.
  A 2xx acknowledges; 429 and 5xx back off (honoring `Retry-After`), other 4xx mark the sink `FAILING`;
  neither advances the cursor. The URL is re-checked against the outbound URL policy on every delivery,
  and the request goes to the address that check resolved.
- *STATE_MIRROR*: a snapshot of current state, then persisted changes and live updates. An update older
  than the mirrored `observedAt` is ignored (counted as `dropped`); tombstones and records past
  `validUntil` are removed; a record that leaves the filter is removed. A live-bus overflow, a change-feed
  gap or `POST /v1/sinks/{id}/resync` drops the mirror and snapshots again. `mqtt-state` republishes its
  retained topics after a broker reconnect.
- A failing sink restarts with backoff (5 s doubling to 5 min) and raises one `system.sink_failing`
  notification after 10 minutes of failure.

**SERVE binding.** A SERVE sink must set `config.port` (there is no default). `config.bind` defaults to
`127.0.0.1`; only loopback, RFC 1918, link-local or ULA are accepted, `100.64.0.0/10` only with
`allowTailnet=true`, and `0.0.0.0` only with `allowAllInterfaces=true` (then every connecting peer must
pass the same check, for the TCP types and for `readsb-json` alike). `maxClients` defaults to 8; further
connections are refused (HTTP: `503`) and counted under `clients.rejected`. TCP clients that do not read
fast enough are disconnected rather than allowed to stall the sink, and a client line longer than 1 KiB
closes the connection. `aprs-is-serve` sends a `#` keepalive comment every 20 s and discards everything
clients send. CoT PUSH sends only to a LAN-scoped multicast group (`239.0.0.0/8`, `224.0.0.0/24`, or IPv6
link/site/organization scope) with TTL 1.

In Docker, publish the port, e.g.:

```yaml
services:
  foresignal:
    ports:
      - "${FORESIGNAL_SINK_BIND:-127.0.0.1}:8081:8081"
```

**Docker NAT and the peer check.** With Docker's default bridge network and port publishing, connections
reach the container from the bridge gateway (for example `172.17.0.1`), not from the real client, so the
per-peer check sees a private address for every client: `allowAllInterfaces` then admits anyone who can
reach the published port. Restrict exposure with the host side of the `ports:` entry
(`FORESIGNAL_SINK_BIND`) or a host firewall, or run with `network_mode: host` so the real peer address is
visible.

**Secrets.** Secrets (`secrets` on create and update) are write-only and encrypted at rest; responses list
only their names (`secretsSet`). An update merges them: a field with a value sets it, `null` removes it, and
an omitted field keeps its stored value.

## Exports, snapshots and holds (prompt 111)

**Bulk exports.** `POST /v1/exports` (agent, always authenticated) queues an export of records and answers
`202` with the job; poll `GET /v1/exports/{id}`, list your jobs with `GET /v1/exports` (admin sees every
job; `kind=`, `status=`, `cursor=`, `limit=`), download with `GET /v1/exports/{id}/download`, and cancel or
delete with `DELETE /v1/exports/{id}`. A job is visible only to the token that created it and to admin.

| `format` | File | Notes |
|---|---|---|
| `geoparquet` (default) | `{id}.parquet` | GeoParquet 1.1: WKB `geometry`, the `bbox` covering struct, `geo` metadata; ZSTD pages; row groups of 65,536 rows |
| `geopackage` | `{id}.gpkg` | OGC GeoPackage 1.4: one feature table per kind, RTree spatial index, the license summary in `gpkg_metadata` |
| `flatgeobuf` | `{id}.flatgeobuf` | Packed Hilbert R-tree (node size 16); written without an index when some record has no geometry |
| `gpx` | `{id}.gpx` | `mode=history` only: one `<trk>` per entity of a TRACK source, `<wpt>` for other point records |

The spec chooses `sources[]`, `kinds[]`, `bbox` or `placeId`, a `from`/`to` window on `timeField`
(`effective`, `observed`, `event`, `ingested`), and the `mode`: `current` (live state), `history` (every
version), `asOf` (the state known at an instant; the same horizon rules as `/v1/records?asOf=`) or `atSeq`
(the state at a seq; see snapshots). `fields[]` keeps only those payload keys; `flatten=true` adds one text
column per payload field (the `fields[]`, else every `fieldSpecs` key of the sources). Rows are read in
keyset pages of 10,000, so a cancel takes effect within a page.

Every finished job carries, next to its data file, `manifest.json` (each file's SHA-256, the spec, the
watermark and the license summary), `manifest.json.jws` (a detached Ed25519 JWS over `manifest.json`,
verifiable against `/.well-known/jwks.json` exactly as in "Licenses and egress" above), `licence.json`
(the license summary: per license the terms, sources and rows; the attributions; the exclusions) and
`ATTRIBUTION.txt`. `?bundle=zip` downloads all of them in one zip; `?file=` names one file. Downloads support
`Range` and `If-None-Match` (the `ETag` is the file's SHA-256), so `curl -C -` resumes:

```sh
curl -sS -H "Authorization: Bearer $AGENT" -H 'Content-Type: application/json' \
  -d '{"format":"geoparquet","sources":["usgs-earthquakes"],"mode":"history","from":"2026-10-01T00:00:00Z"}' \
  https://host/v1/exports
curl -C - -o quakes.zip -H "Authorization: Bearer $AGENT" "https://host/v1/exports/$ID/download?bundle=zip"
```

**License rules.** Sources whose terms forbid redistribution (`FORBIDDEN`, for example OpenSky and ACLED)
or restrict it to this instance (`LOCAL_ONLY`, including unknown terms) are left out and listed under
`excluded` in `licence.json`. `personalUse=true` includes them; the sidecar and `ATTRIBUTION.txt` then say
"Personal use only — do not redistribute" and list them. Private sources need an admin token and
`includePrivate=true` (`403` otherwise).

**Limits** (`foresignal.exports.*` in `application.yml`): `max-concurrent` (1: exports, snapshots and packs
share one worker pool), `max-rows` (5,000,000; a larger export fails), `disk-quota-gb` (20 GiB across
`${FORESIGNAL_DATA_DIR}/exports` and `/snapshots`; a new job is refused with `409 EXPORT_QUOTA`), `ttl`
(7 days; then the hourly retention run deletes the files and the job becomes `EXPIRED`, and downloads answer
`410 EXPORT_EXPIRED`), `page-size` (10,000) and `query-timeout` (5 min per page). A job a restart
interrupted is marked `FAILED`; submit it again. `/v1/system` shows `exports.queued`, `running` and `bytes`;
metrics `foresignal.export.rows` and `foresignal.export.duration` are tagged by format.

**Opening the files.** DuckDB reads GeoParquet with its spatial extension
(`INSTALL spatial; LOAD spatial; SELECT kind, count(*) FROM 'export.parquet' GROUP BY kind;`), and GDAL 3.8
or later with `ogrinfo export.parquet`. QGIS opens the `.gpkg` directly (Layer → Add Layer → Add Vector
Layer); the manual check is that each kind shows as a layer with its features in place and that
`ogrinfo -al -so export.gpkg` reports the spatial index. `ogrinfo export.flatgeobuf` reads FlatGeobuf, and
the GPX file loads in any GPX viewer.

**Snapshots.** `POST /v1/snapshots` (admin) writes the state at a seq S as GeoParquet with the same
sidecars, under `${FORESIGNAL_DATA_DIR}/snapshots/{id}/`. S is `atSeq`, else `S(asOf)`, else the current
watermark. The state is rebuilt from `record_version` per tier (the newest version of each entity at or
below S, tombstones dropped, validity judged at the ingestion time of S). A snapshot is an export job of kind
`SNAPSHOT`: poll and download it through `/v1/exports/{id}`. It never expires unless `expiresAt` is set.
When S lies before a source's retention horizon, the versions it needs are read from a hold's copies; with
no hold that covers the source, its kinds and the needed seq range since before those versions were
deleted, the request fails `400 SNAPSHOT_BEYOND_RETENTION` (with `sourceId` and `horizon`).

**Holds.** `POST /v1/holds` (admin) keeps the records of some sources and kinds, within an ingestion-time
range (`from`/`to`) or a seq range (`fromSeq`/`toSeq`), through retention: before each retention run
deletes STANDARD versions or drops TRACK partitions, it copies the rows about to go that fall inside an
active hold into `record_version_held`. Retention still drops partitions and `floorSeq` is unchanged; a hold
never blocks a drop. When a copy fails, retention keeps those rows for that run (it skips the STANDARD
source, or keeps the TRACK partition and every later one). At most 20 holds (`holds-max`); a hold lapses at
its `expiresAt`. `GET /v1/holds/{id}/versions` pages through the copies; `DELETE /v1/holds/{id}` deletes the
hold and its copies. `/v1/system.holds` reports the count, the copied rows and the table's size. Copies are
made only from the moment a hold exists: create the hold before the records age out.

## Cold archive (prompt 111)

The cold archive writes records out as GeoParquet before retention removes them, so history stays
queryable after the database lets it go. It is off by default; configure it with
`PUT /v1/settings/archive` (admin):

```json
{"enabled": true, "sources": [], "root": "", "target": "local", "keepLocal": true}
```

`sources` is an allowlist (empty: every non-private source); `root` defaults to
`${FORESIGNAL_DATA_DIR}/archive` and may be an absolute path (another disk) or a path relative to the data
directory. Each hourly retention run then writes, before its deletes and drops, exactly the rows it is about
to remove: for every TRACK partition about to be dropped, the rows of each archived TRACK source in it, and
for every STANDARD source, its rows below the run's cutoff. Files land in a hive layout:

```
archive/record_version/tier={std|trk}/source_id={id}/date={yyyy-mm-dd}/part-{fromSeq}-{toSeq}.parquet
archive/record_version/tier={std|trk}/source_id={id}/date={yyyy-mm-dd}/_manifest.json
```

`date` is the UTC date of `ingested_at`; each part is named after the first and last seq it holds, and the
partition's `_manifest.json` lists every part with its rows, SHA-256, upload state and the source's
license. A part is written under a temporary name, fsynced, and its row count is read back from the Parquet
footer before retention deletes anything; only then does the archive's watermark for that scope advance, so
a row is never archived twice, even when a delete fails after the write. When a write fails, retention keeps
the rows for that run (it skips the STANDARD source, or keeps the TRACK partition) and counts
`foresignal.archive.failed`. `foresignal.archive.bytes` counts the bytes written; `/v1/system.archive` shows
the last run, its bytes and failures. `GET /v1/archive/catalog?source=&from=&to=` lists the partitions and
their files. The archive is the `ARCHIVE` egress path (operator-owned storage), allowed for every license;
the manifests still record each license.

**S3 and MinIO.** Set `target` to `s3` and give the bucket and the credentials (write-only; the API only
lists their names under `secretsSet`, and they are stored encrypted):

```json
{"enabled": true, "target": "s3", "keepLocal": false,
 "s3": {"endpoint": "http://192.168.1.20:9000", "bucket": "foresignal-archive", "prefix": "prod",
        "region": "us-east-1", "pathStyle": true},
 "secrets": {"accessKey": "REPLACE", "secretKey": "REPLACE"}}
```

The endpoint must pass the outbound URL policy's LAN-allowed profile: a public endpoint, or one on
loopback, RFC 1918, unique-local or 100.64/10 addresses, over https or http (a refused endpoint is
`400 URL_NOT_ALLOWED`). MinIO needs `pathStyle: true`; AWS S3 works with either style. Each verified part
is uploaded after it is written, by one background thread: files up to 64 MiB with one `PutObject`, larger
ones as a multipart upload in 64 MiB parts, every request signed with AWS Signature Version 4 over the
body's SHA-256. The local copy is deleted only when `keepLocal` is false **and** the returned ETag matches
the file's MD5 (or the multipart MD5-of-MD5s). A MinIO bucket for the archive:

```sh
mc alias set lan http://192.168.1.20:9000 ADMIN_USER ADMIN_PASSWORD
mc mb lan/foresignal-archive
mc admin user add lan foresignal REPLACE_SECRET
mc admin policy attach lan readwrite --user foresignal
```

**Reading the archive.** DuckDB reads the whole tree with its partition columns:

```sql
INSTALL spatial; LOAD spatial;
SELECT source_id, date, count(*) AS rows
  FROM read_parquet('archive/record_version/**/*.parquet', hive_partitioning=true)
 GROUP BY ALL ORDER BY ALL;
```

From S3, `CREATE SECRET (TYPE s3, KEY_ID '…', SECRET '…', ENDPOINT '192.168.1.20:9000', URL_STYLE 'path', USE_SSL false);`
then `read_parquet('s3://foresignal-archive/prod/record_version/**/*.parquet', hive_partitioning=true)`.
GDAL reads a part with `ogrinfo part-….parquet` (GDAL 3.8 or later) and converts a partition with
`ogr2ogr -f GPKG partition.gpkg part-….parquet`.

## Offline packs (prompt 111)

`POST /v1/packs` (agent, always authenticated) builds PMTiles v3 files for phones and field laptops:

```json
{"name": "Austin field pack", "bbox": [-97.95, 30.1, -97.55, 30.5], "minZoom": 6, "maxZoom": 12,
 "vector": {"sources": ["usgs-earthquakes"], "kinds": [], "asOf": null},
 "raster": {"productId": "mrms-reflectivity", "frame": "latest"}}
```

`vector` produces `vector.pmtiles` (Mapbox Vector Tiles, gzip, one layer named `foresignal`, the same
features as `/v1/tiles`; `asOf` renders the state at that instant); `raster` produces `raster.pmtiles`
(PNG tiles of one product frame; `latest` is resolved when the pack is queued). A pack may cover at most
`foresignal.exports.pack-max-tiles` (200,000) tiles over its box and zooms, else
`413 PACK_TOO_LARGE` with the computed `tileCount`. Empty tiles are left out; identical tiles are stored
once. The metadata carries `name`, `vector_layers`, the aggregated `attribution`, `generated` and
`watermarkSeq`. A pack is an export job of kind `PACK` with the same license and private rules and sidecars:
download each file with `GET /v1/exports/{id}/download?file=vector.pmtiles` (resumable with `Range`). Packs
are files to copy to the device; Foresignal never proxies `pmtiles://` from an upstream.

**MapLibre GL JS** (the `pmtiles` npm package) opens a local file the user picks:

```js
import maplibregl from "maplibre-gl";
import { PMTiles, Protocol, FileSource } from "pmtiles";

const protocol = new Protocol();
maplibregl.addProtocol("pmtiles", protocol.tile);
const pack = new PMTiles(new FileSource(file)); // file: a File from <input type="file">
protocol.add(pack);
map.addSource("foresignal", { type: "vector", url: "pmtiles://" + pack.source.getKey() });
map.addLayer({ id: "records", type: "circle", source: "foresignal", "source-layer": "foresignal" });
```

**Flutter** (`flutter_map`): the raster pack with the `flutter_map_pmtiles` package and the vector pack with
`vector_map_tiles_pmtiles`, both reading a file on the device:

```dart
final rasterProvider = await PmTilesTileProvider.fromSource('/path/to/raster.pmtiles');
final vectorProvider = await PmTilesVectorTileProvider.fromSource('/path/to/vector.pmtiles');
```

**Checking a pack.** The manual check is the `pmtiles` command-line tool from protomaps/go-pmtiles:
`pmtiles verify vector.pmtiles` checks the header counts, the directories and the clustered order, and
`pmtiles show vector.pmtiles` prints the header and metadata. The header's zoom range is that of the tiles
actually present.

## Standards outputs (prompt 112)

Foresignal also speaks four standards that existing tools understand, for clients that can only fetch a URL.
Every one of them honors the caller's token scope, restricted tokens and feed keys, hides private sources and
applies the license rules (109).

### OGC API – Features (QGIS, GDAL, GeoPandas)

`/v1/ogc` is an OGC API – Features server (Part 1 core and GeoJSON, Part 3 filtering and queryables, basic CQL2 in
`cql2-text` and `cql2-json` with `S_INTERSECTS`). Each record kind is two collections: `{kind}` (current state)
and `{kind}-history` (every stored version; feature ids `{sourceId}:{externalId}:{version}`). The raster products
of 108 are listed next to them as coverage collections. JSON only; unknown query parameters are refused with
`400` so a typo never returns unfiltered data (`f=json` is tolerated).

- **QGIS:** Layer → Add Layer → Add WFS / OGC API – Features Layer → New, URL `https://foresignal.lan/v1/ogc`
  (with `openReads` off use a feed key whose prefix is `/v1/ogc`: `https://foresignal.lan/v1/k/fsf_…/ogc`).
  Connect, pick a collection, and use the filter button to write a CQL2 expression such as
  `mag > 5 AND S_INTERSECTS(geometry, BBOX(-98, 29, -96, 31))`. Manual check (not automated): the collections
  list, a layer loads, paging continues past 100 features and a filter narrows the layer.
- **GDAL/OGR:** `ogrinfo "OAPIF:https://foresignal.lan/v1/ogc" earthquake -so` and
  `ogr2ogr -f GPKG quakes.gpkg "OAPIF:https://foresignal.lan/v1/ogc" earthquake -where "mag > 5"`.
- **GeoPandas:**

  ```python
  import geopandas as gpd
  url = ("https://foresignal.lan/v1/ogc/collections/earthquake/items"
         "?limit=1000&filter=mag%20%3E%205")
  quakes = gpd.read_file(url)
  ```

`limit` is at most 10,000; `next` links carry an opaque cursor; `numberMatched` is returned with `count=true` up
to 100,000. `datetime` matches the effective time of current collections and the observed time of history.
`/v1/ogc/collections/{id}/queryables` lists the filterable properties (the kind's declared payload fields plus
`title`, `sourceId`, `observedAt`, `eventTime`, `version` and `geometry`). A filter error is
`400 FILTER_INVALID` with the `position` of the problem.

### CAP 1.2 for alerting systems

Every alert state change issues one CAP 1.2 message (`Alert` when it fires; `Update` on acknowledgement, snooze,
note or enrichment; `Update` with `responseType AllClear` when it resolves; never `Cancel`, which CAP reserves for
messages issued in error). Each message lists the earlier ones in `references`, is `scope Public`, and carries the
note "Generated automatically by a Foresignal instance from third-party data. Not an official warning."
Consumers must treat it as such: it is a relay of third-party data, not an authority's warning. For alerts on
official CAP sources (the `cap-*` and `meteoalarm-*` feeds, `tsunami-warnings`, `nws-alerts`) the message links
the upstream message in `web` and names the upstream sender in the description.

- `GET /v1/cap/alerts.atom` — the Atom index (filters `ruleId`, `minSeverity`, `tags`, `placeId`, `since`).
- `GET /v1/cap/alerts/{alertId}.cap` — an alert's latest message; `GET /v1/cap/messages/{identifier}.cap` — any
  stored message. Identifiers are `{instanceId}-{alertId}-{n}`.

The sender is the setting `cap.sender` (default `foresignal@{installId}`) and `senderName` is
"Foresignal ({instance.name})" (default the install id). Messages are deleted with their alert by retention.
Messages of alerts that fired before the upgrade start with the next state change.

### Calendars

`GET /v1/calendars/{feed}.ics` with `feed` one of `launches` (Launch Library NET and window), `tfr` (FAA TFR
effective to expiry; open-ended ones carry `X-FORESIGNAL-OPEN-ENDED:TRUE`), `nws` (NWS onset to end),
`briefings` (the next 30 days of each enabled briefing, one event per occurrence in the briefing's time zone) or
`custom?source=…&kind=…` (any kind whose descriptor declares `startField`/`endField`; otherwise
`400 ICAL_FIELDS_MISSING`). Filters `bbox`, `placeId`, `source`, `kind`. UIDs are stable
(`{sourceId}:{externalId}@{instanceId}`) and `SEQUENCE` is the record version, so a calendar app updates an event
in place; a canceled or expired record stays for 7 days as `STATUS:CANCELLED`. Calendars ask to be refreshed
every 15 minutes.

To subscribe from a phone or desktop calendar with `openReads` off, create a feed key whose prefix is
`/v1/calendars` and subscribe to `https://foresignal.lan/v1/k/fsf_…/calendars/launches.ics` (in Apple Calendar:
File → New Calendar Subscription; in Google Calendar: Other calendars → From URL, which needs a public URL).

### Share links

`POST /v1/share-links` (agent token) creates an expiring link (default 7 days, at most 30) to one `record`,
`alert`, `view`, `track`, `briefing-run` or `export` (a workspace is shared with `POST /v1/workspaces/{id}/share`,
see "Objects, workspaces and validation", and only while its TLP label is `CLEAR`; a published report with
`POST /v1/reports/{id}/share-link`, see "Reports and exchange"); the `fss_` key is returned once and only its hash is stored. The link works without a token, only at `GET /v1/k/{key}` (JSON, or GeoJSON with
`Accept: application/geo+json`), `/v1/k/{key}/geojson`, `/v1/k/{key}/tiles/{z}/{x}/{y}.mvt` (views) and
`/v1/k/{key}/download` (exports); it is never accepted as a bearer token. Uses are counted (`maxUses`), limited to
60 a minute per link, and audited (the first use and then at most one an hour); a revoked, expired or used-up link
answers `410 SHARE_EXPIRED`. With `clientLinks.share` set in the server settings (placeholder `{key}`),
`?open=1` redirects to your client.

Privacy defaults: a link renders within the creating token's restriction and never shows a private source.
Records whose license does not allow redistribution are removed, and a target that is such a record (or an
export or briefing run built from such sources) is refused with `409 SHARE_LICENCE_FORBIDDEN`; attribution is
always included. `fieldMask` keeps only the listed payload fields and `precisionM` coarsens coordinates. The
privacy mask for personal sources (precision capped to 1,000 m, observer, device and address-like fields removed,
the payload reduced to `title`, `kind` and `observedAt`) is built in, but a private target is refused even for an
admin token, because Foresignal's egress policy never lets a private record leave on a share link. Views over
places or watchlists cannot be shared (they reveal your places).

## Exchange formats: CZML, FDSN/QuakeML, STIX/TAXII (prompt 141)

All four are read-only views of data Foresignal already stores. None needs configuring, and none changes ingest.

**CZML for CesiumJS.**
- `GET /v1/history?since=…&until=…&format=czml` and `GET /v1/records/{sourceId}/{externalId}/track?format=czml`
  return a CZML document (`application/czml+json`).
- The first packet is the document packet. Its clock spans the requested range and its `description` carries the
  attribution. Then comes one packet per entity, with its positions as sampled `cartographicDegrees` (a constant
  position when the entity has a single sample), `availability`, the title as its label and the kind's `kindDisplay`
  color.
- History CZML is not paged. It reads about 200,000 versions at most (the cap is checked after each page) and 5,000
  entities. A capped document says so in its description and in the header `Foresignal-Truncated: true`.
- Load it in Cesium with `Cesium.CzmlDataSource.load(url)`. `load(url)` sends no headers, so either pass a
  `Cesium.Resource` that carries `Authorization: Bearer …`, or put a feed key in the URL (see
  [Exposing Foresignal](#exposing-foresignal)). The KML route for Cesium is in
  [Feeds, Google Earth and Cesium](#feeds-google-earth-and-cesium).
- The MCP tool `get_track` with `format: czml` stores the document in the media store, where it expires after 24
  hours, and returns a `media` object (`id`, `url` of `/v1/media/{id}`, `contentType`, `bytes`, `expiresAt`) with the
  track summary. The `url` is absolute when `publicBaseUrl` is set.

**FDSN event service (fdsnws-event 1.2).**
- `GET /fdsnws/event/1/query` serves the current `earthquake` records (of every source that writes that kind) to
  FDSN clients such as ObsPy (`Client(base_url="https://your-host")`), SeisComP or QGIS plugins. A query scans at
  most 200,000 current records.
- Parameters: `starttime`, `endtime`, a rectangle (`minlatitude`…`maxlongitude`), a circle (`latitude`,
  `longitude`, `minradius`, `maxradius` in degrees), `minmagnitude`, `maxmagnitude`, `orderby` (`time`,
  `time-asc`, `magnitude`, `magnitude-asc`), `limit` (at most 20,000), `format` (`xml` or `text`) and `nodata`
  (204 or 404). Short names (`start`, `minlat`, `minmag`, …) work too. Any other parameter is a 400, except a feed
  key's `key`.
- `xml` is QuakeML 1.2 BED, with publicIDs `smi:foresignal/{source}/{externalId}` and depth in meters. `text` is the
  FDSN pipe-separated format, with depth in km.
- No match returns `204` with no body. Errors use the FDSN plain-text body.
- `/fdsnws/event/1/version` returns `1.2.0`. `/fdsnws/event/1/application.wadl` describes the parameters.
- Access is `read`, so the service is open while `security.openReads` is on. Private sources and token restrictions
  apply as on `/v1/records`.

**STIX 2.1 and TAXII 2.1 (agent scope).**
- `GET /v1/export/stix?kind=&since=` returns a STIX bundle (`application/stix+json;version=2.1`; a client that asks
  only for `application/json` gets that type). `kind` may be repeated and takes `vulnerability`, `malware-url` or
  `malware-c2`:
  - KEV and NVD `vulnerability` records become `vulnerability` objects with a CVE external reference;
  - URLhaus `malware-url` records become `indicator` objects with the pattern `[url:value = '…']`;
  - Feodo `malware-c2` records become `indicator` objects with a `network-traffic` pattern (address and port), or an
    `ipv4-addr` or `ipv6-addr` pattern when no port is known.
- The bundle also holds an `identity` for Foresignal. Ids are UUIDv5 values of the record reference, so they are the
  same on every export.
- If more than 50,000 records match (counted before the egress filter), the answer is `400 RESULT_TOO_LARGE`; narrow
  `since` or use TAXII.
- The TAXII 2.1 server is read-only. Point a TAXII client (OpenCTI, MISP's taxii2 client, `taxii2-client`) at
  `https://your-host/taxii2/` with an agent token as `Authorization: Bearer …`. It serves two collections,
  *Vulnerabilities* and *Indicators*, under `/taxii2/foresignal/collections/{id}/objects/`. Objects are listed in
  ingest order, with `added_after`, `limit` (100 by default, at most 1,000) and `next` paging.
- **Egress:** both STIX and TAXII run every record through `EgressPolicy` on the `EXPORT` path, without
  `personalUse`. Private sources, MLAT positions and records whose license does not allow redistribution (`FORBIDDEN`,
  `LOCAL_ONLY` or `UNKNOWN` redistribution) are left out; when any were, their count is in
  `Foresignal-Licence-Filtered`.
  - By default this withholds every URLhaus indicator. `abusech-urlhaus` maps to the catalog entry
    `provider-terms-nc`, whose redistribution is `UNKNOWN`, so it counts as `LOCAL_ONLY`. Feodo Tracker and KEV
    (CC0) and NVD (`reuse-with-attribution`) pass. There is currently no operator setting that releases URLhaus
    records to STIX or TAXII.

## Federation and edge relay (prompt 113)

Foresignal instances can share data with each other. Every replicated record carries a provenance chain (the
origin instance, source, external id and seq, then one hop per instance that re-served it) and a globally unique
reference, `urn:foresignal:{instanceId}:{sourceId}:{externalId}` (`globalRef` on records, versions and changes;
the instance id is the install id).

### Topologies

- **Home hub ⇄ cabin edge.** A Raspberry Pi at the cabin runs `FORESIGNAL_MODE=edge` with its local receivers
  (ADS-B, AIS, mesh, decoders). It pushes to the hub whenever the link is up. On the hub, create a `RECEIVE` peer
  for the cabin and rotate its ingest key; on the edge, create a `federation-push` sink to the hub with that key.
  Set `sameOperator: true` on both when the hub is yours, so the cabin's `LOCAL_ONLY` receiver data may reach it.
- **Two friends' instances.** Each creates a read token for the other with `restrictions.federation: true`
  (optionally narrowed to sources, kinds or a bbox) and a `PULL` peer pointing at the other's base URL with that
  token. Each replicates the subset the other's token and its own `selection` allow.
- **A ring** (A → B → C → A) is safe: an item whose chain already names the receiving instance is dropped
  (`foresignal.federation.dropped{reason="loop"}`), and so is one whose chain has `maxHops` (default 3) or more
  entries. The same origin arriving over two paths becomes one local record: the first peer source to claim an
  origin owns it, later updates from either path land in that record, and an update older than the newest one
  applied is dropped as `dedup`.

### Serving peers

The feed is `GET /v1/federation/changes`, `/stream` (SSE with `change`, optional `live`, `heartbeat` and `reset`)
and `/snapshot`. Each needs a token with the read scope **and** `restrictions.federation: true`; the token's
source, kind, bbox and area restrictions still apply, and `includeExercise: true` adds exercise records. Below
the purge horizon the feed answers `410 RESYNC_REQUIRED` and the stream sends `reset`, and the peer re-snapshots.
`GET /v1/federation/info` (signed with the instance key, `Foresignal-Signature`) tells a peer who you are.

### What never leaves

Every record passes the one egress policy on the federation path before it is serialized:

- licenses that forbid redistribution: `opensky-states`, `acled`, `aisstream`;
- `LOCAL_ONLY` sources and licenses: the local receivers (`local-*`), and anything you have not opened;
- MLAT positions, from any source;
- private sources (personal location, mesh, ACARS, home sensors), always;
- exercise records, unless the token asks for them.

Exclusions count in `foresignal.federation.denied{reason}`. The one exception is an edge pushing to its
operator's own hub (`sameOperator: true` on the `federation-push` sink and on the hub's peer): `LOCAL_ONLY`
records may then travel, labeled `local-only-operator`, and the hub keeps them `LOCAL_ONLY`, so it never
re-federates them. Private records and MLAT positions never travel, even then.

### Consuming a peer

`POST /v1/peers` (admin) with `id`, `name`, `baseUrl` (checked by the outbound URL policy: LAN, Tailnet and public
addresses are allowed), `mode: PULL`, the remote `token` (stored encrypted, never returned) and a `selection`:
`sources`, `kinds`, `bbox` or `placeId`, `maxAge` (drop older items), `backfill` (`none`, the default: the current
snapshot only; or a duration of history to page from the remote change feed), `live`, `maxHops`, `trackTtl`.
The peer registers `peer-{id}` (STANDARD), `peer-{id}-trk` (TRACK, TTL `trackTtl`, sampled like other tracks) and
`peer-{id}-events` (compact messages). Each record keeps the origin's license in `_foresignal.originLicence`, a
volatile payload member, so an identical re-send makes no new version and the license follows the record. Peer
data has reliability `F` (cannot be judged) unless you set `reliability`.

**Key pinning.** On the first successful contact the remote signing key is pinned (trust on first use). If the
peer later signs with another key, replication stops with the health reason `PEER_KEY_MISMATCH`. Check with the
peer's operator, then `PUT /v1/peers/{id}` with `pinnedKid: null` to re-pin on the next contact (or the new kid).
`POST /v1/peers/{id}/test` fetches and verifies `/info` without pinning anything. Other health reasons:
`PEER_UNREACHABLE` (the stream reconnects from 5 s up to 5 min, and honors `Retry-After`) and `PEER_RESYNC`.
`POST /v1/peers/{id}/resync` re-snapshots and then tombstones the local records the peer no longer lists. Metrics:
`foresignal.federation.items{peer,result}` and `foresignal.federation.lag_seq{peer}`; MCP `list_peers` and
`peer_status` (admin).

### Edge mode

`FORESIGNAL_MODE=edge` keeps collection, the writer, triggers and notification channels, and leaves out
embeddings, LLM features (ask, briefings, the analyst), raster products and rendering, OGC, exports and the
federation feed; `GET /v1/system` reports `mode: edge` and those `features` off. Retention defaults to a 6 h track
window and a 1 d default window (only when you have stored no retention settings). Plan for a JVM heap of about
256 MB (`JAVA_TOOL_OPTIONS=-Xmx256m`) on a Raspberry Pi 4 or 5 with 2 GB or more.

The edge's `federation-push` sink (`config.url` = the hub's base URL, `config.peerId` = the edge's peer id on the
hub, `secrets.ingestKey` = the `fsp_` key the hub issued) sends signed, gzip-compressed batches of up to 1,000
items from a durable cursor. While the link is down the cursor holds and nothing is lost; when it comes back the
edge catches up in order (the usual sink rules on cursor clamps and resync apply). The hub pins the edge's key on
the first batch and refuses later batches signed by another key (`403 PEER_KEY_MISMATCH`).

### Compact messages over mesh and DTN

Fired and resolved alerts and intelligence events (never tracks or bulk records) are also queued as compact
CBOR messages of at most 160 bytes: the 084 mesh profile plus key 10 (this instance's 4-byte short id), key 11
(a message id) and key 12 (expiry in minutes). The queue sends critical messages first, newest first within a
severity, drops expired ones, and sends a message again only when its severity changes or it is resolved.

- **Mesh:** set `compactEvents: true` on a Meshtastic or MeshCore channel. Messages go out through the channel's
  `profile` (`cbor` or `text`) and its airtime governor, within `maxMessagesPerHour × 160` bytes an hour.
- **Store and forward:** `GET /v1/federation/bundle?since=` (a token with `federation: true`) returns the pending
  messages as one CBOR array, for sneakernet or DTN tooling such as a BPv7 bundle agent; available on an edge too.
  `POST /v1/federation/bundle` with a peer's ingest key accepts the same, as `remote-event` records on
  `peer-{id}-events`. Messages from this instance or already seen are dropped, and received messages are never
  sent on.

## Pattern of life (prompt 114)

Pattern of life learns where a tracked entity dwells and when, and reports deviations as records that rules can
match. **It is opt-in only**: nothing is analyzed until you create a subject.

```http
POST /v1/pol/subjects
{"subjectType": "record", "ref": "owntracks-phone/phone",
 "params": {"stayRadiusM": 100, "minDwell": "10m", "maxGap": "30m", "eps": 150, "minPts": 3, "lookback": "28d"}}
```

- **Subjects:** a record (`{sourceId}/{externalId}`), a 098 entity (every record it links) or a 036 watchlist
  (every record whose identity field holds a value on the list; at most 200 records per subject).
- **Stays:** consecutive positions within `stayRadiusM` of the stay's first fix, spanning at least `minDwell`; a gap
  longer than `maxGap` ends a stay. Positions are read incrementally from the stored versions, so a subject needs
  history in the TRACK window (downsampled ranges are fine: dwell uses observed-time gaps).
- **Places:** nightly at 03:00 in the server timezone (`PUT /v1/settings/server {"timezone": …}`), or on
  `POST /v1/pol/subjects/{id}/recompute`, the stays of the trailing `lookback` are clustered with DBSCAN (`eps`
  meters, `minPts` stays). Between runs a new stay joins the nearest place within its radius. Places keep their
  id and your label across runs; label one with `PUT /v1/pol/places/{id} {"label": "home"}`. Stays and places
  outlive the TRACK window; the `pol.retention` setting (default `180d`) removes them.
- **Records:** `pattern-of-life` publishes `pol-place` (one record per place: label, visits, dwell, typical hours)
  and `pol-deviation` with `deviationType`:
  - `NEW_PLACE`: a stay outside every place, once the subject has 14 days of history;
  - `UNUSUAL_TIME`: a stay at a known place arriving in an hour-of-week bucket that holds under 2% of the place's
    dwell;
  - `EXPECTED_ABSENT`: checked every 10 minutes; in a bucket where one place normally holds at least 60% of the
    subject's dwell, the subject has been elsewhere, or unseen, for more than an hour. Suppressed unless every
    member source is healthy, so an outage never reads as an absence.
- **Rule recipes:** `{"name": "Not home at night", "sources": ["pattern-of-life-private"], "kinds":
  ["pol-deviation"], "conditions": [{"path": "deviationType", "op": "==", "value": "EXPECTED_ABSENT"},
  {"path": "placeLabel", "op": "==", "value": "home"}], "mode": "match"}`; or a new place for a watched aircraft:
  `kinds: ["pol-deviation"]`, `deviationType == NEW_PLACE`.

**Privacy.** A subject with a member on a private source (039, such as an OwnTracks device) needs an admin token to
create, and is visible only to admin tokens. Its stays and places are stored at that source's precision cap, and
its records go to `pattern-of-life-private`, a private source: never served anonymously, never embedded, exported,
shared or federated. Deleting a subject deletes its stays and places and tombstones its records; purging an entity
(`POST /v1/records/{sourceId}/{externalId}/purge`) also deletes the subjects of that record and the stays other
subjects formed from its positions.

## Trips and commutes (prompt 114)

A trip watches the corridor along a route you supply, around a scheduled departure.

**Routing stays in your client.** Compute the route with your own router and send the line; Foresignal never calls a
routing service. To route around known hazards, ask a router that can exclude polygons: Valhalla accepts
`exclude_polygons` (for example the corridor hazards a dry-run check returned), OSRM cannot exclude areas. Then save
the resulting LineString (at most 5,000 vertices), or a 036 route place:

```http
POST /v1/trips
{"name": "Morning commute",
 "route": {"type": "LineString", "coordinates": [[-97.7431, 30.2672], [-97.6990, 30.3600]]},
 "bufferMeters": 2000,
 "schedule": {"type": "recurring", "days": ["MO","TU","WE","TH","FR"], "departTime": "07:45",
              "timezone": "America/Chicago"},
 "expectedDuration": "40m", "leadTime": "60m", "channelIds": ["<channel id>"],
 "anchor": "owntracks-phone/phone"}
```

- **Corridor:** the route buffered by `bufferMeters` on the ellipsoid (split at the antimeridian), stored on save.
- **Pre-trip briefing:** at `departAt − leadTime`, the hazards of `hazardKinds` (default weather alerts, road events,
  work zones, fire perimeters, cyclone cones, M4+ earthquakes, incidents) on the route, near it (within twice the
  buffer), and forecast hazards whose `validFrom` falls inside the trip, ordered along the route, are sent to the
  trip's channels (every channel subscribed to briefings when it names none). A clear route is sent too unless
  `notifyWhenClear` is false. `minSeverity` drops lower-rated hazards. `POST /v1/trips/{id}/check {"dryRun": true}`
  runs it now without sending; `GET /v1/trips/{id}/runs` lists past checks.
- **During the trip:** an ephemeral trigger rule tagged `trip-<trip id in lower case>` watches the corridor for new
  hazards (mode `enter`) from the check until `departAt + expectedDuration`, extended in 15-minute steps (up to twice
  the duration) while the anchor is still moving, then deleted. Its alerts follow your normal alert routing.
- **Anchor:** with a moving-anchor record, the briefing lists only hazards ahead of your position on the route,
  each with an ETA at your average speed over the last 10 minutes.
- Recurring departures keep their local time across daylight-saving changes.

## CPA/ETA and approach rules (prompt 114)

- `GET /v1/kinematics/cpa?a=aisstream/244660000&b=aisstream/244123000&horizon=1h`: the closest point of approach of
  two records under constant velocity (`dcpaMeters`, `tcpaSeconds` clamped to the horizon, vertical separation for
  aircraft). Speed and course come from the payload (`gs`/`track`, `sog`/`cog`, `speed`, `speedMs`/`courseDeg`),
  or from the last two stored positions. Pairs more than 500 km apart answer `400 CPA_RANGE`; states older than an
  hour `422 STATE_TOO_OLD`; `stale: true` marks a state older than twice its source TTL.
- `GET /v1/kinematics/eta?record=…&to=home` (or `to=-97.74,30.27`): approaching when the course is within 45° of the
  bearing at 0.5 m/s or more; then `etaSeconds = distance / (speed · cos Δ)`.
- `POST /v1/kinematics/cpa/batch`: one subject against up to 500 records of a filter (kinds, sources, bbox), the
  closest approaches first.
- **Approach rules:** a trigger condition `{"type": "approach", "target": {"placeId": "home"} | {"point": {...}} |
  {"record": "src/id"}, "withinMeters": 1852, "withinMinutes": 20}` fires `enter` when a watched record's CPA to the
  target is within the distance and the time. It is evaluated on each stored version of the rule's sources, so edge
  resolution is the source's poll or flush resolution; unknown speed, course or a stale target counts as not
  approaching. Approach conditions apply to record rules only and cannot be combined with `hysteresisMeters` or a
  moving anchor.

## Exercises and replay (prompt 115)

An exercise replays a past window of retained history through selected rules and delivers the matches as
clearly marked exercise alerts, so you can rehearse the whole alert chain (rule → notification → phone → action)
without touching real alert state.

**Rehearsal checklist**

1. Opt in the channels that should take part: set `acceptExercise: true` on them (`PUT /v1/channels/{id}`), or name
   them in the run's `channelIds`. Every other channel never receives an exercise alert. Good candidates are a test
   ntfy topic, your own phone's Web Push or native push, and a webhook pointed at the automation you want to
   rehearse.
2. Pick a window you remember (an evening with a known storm, a busy shipping morning) and the rules to replay
   (`ruleIds`, or a `tag`).
3. Project it first: `POST /v1/exercises` with `"dryRun": true` returns `stats.alertsByRule` and
   `stats.notificationsByChannel` without delivering anything.
4. Start it with a pace: `realtime`, `x10`, `x60` (the default) or `max`. The replay sleeps the scaled gap between
   the versions' ingest times, never more than 60 s ÷ the factor at once.
5. Watch `GET /v1/exercises/{id}`; stop it with `POST /v1/exercises/{id}/cancel` (it stops between matches).
6. Afterwards remove its alerts and notifications with `DELETE /v1/exercises/{id}/alerts`. They also expire on
   their own after `exercise.alertRetention` (7 days).

One exercise runs at a time (`409 EXERCISE_RUNNING`). Start, cancel and purge are audited, and channels subscribed
to system events get an `[EXERCISE]` start and finish notification.

**How an exercise is marked**

| Where | Marking |
|---|---|
| Every notification | title prefixed `[EXERCISE] `; body footer "This is an exercise replay of data from {from}–{to}. No action required." |
| Webhook | header `X-Foresignal-Exercise: true`, payload field `exercise: true` |
| Web Push, APNs, FCM | `exercise: true`; urgency `normal`, APNs `active`, FCM `NORMAL`, never time-sensitive or critical |
| CAP output | `status` `Exercise` |
| API, SSE and MCP | `exercise: true` and `exerciseRunId` on the alert; hidden unless `includeExercise=true` |

**What an exercise never touches**: real cooldowns, `maxAlertsPerHour` counters and storm state, real alerts'
lifecycle, escalation policies, proposed actions, federation, digests and briefings, rollups and statistics,
glance counts, and alert-quality statistics. Enrichment runs only when the run sets `enrich: true`.

**Replay limits**

- The window is at most `exercise.maxWindow` (7 days) and must lie inside retained history: a window that starts
  below any involved source's floor is refused with `400 REPLAY_BEYOND_RETENTION`, naming the source and its
  earliest replayable time. TRACK sources keep only `retention.trackWindow` (3 days) of raw history.
- Cooldowns, the hourly cap and active windows are simulated in run-local state, from the start of the window.
- Enter and exit come from each version's predecessor. A version whose predecessor lies before the window and was
  already removed by retention fires nothing; `stats.caveats` says so (`PREDECESSOR_OUTSIDE_WINDOW`).
- Count and absence rules replay at record level (`WINDOW_APPROXIMATE`); anomaly rules are not replayed and are
  listed in `stats.skippedRules`.
- Replays create no records, so trip briefings and pattern-of-life analyses are not triggered by an exercise.

## Tuning alert quality (prompt 115)

**Giving feedback.** Every alert takes one of `useful`, `noise`, `late` or `false_positive`:

- REST: `PUT /v1/alerts/{id}/feedback`;
- notification buttons: `feedback:useful` and `feedback:noise` (Telegram and Matrix show them by default; on ntfy
  and Web Push, which show two buttons, list them in `config.actions.list`, for example
  `["ack", "feedback:noise"]`);
- chat: `/feedback <alertId> <value>` (agent chats);
- MCP: `set_alert_feedback`; Ask: the model proposes it (`propose_feedback`) and you confirm.

**Implicit signals.** Without any feedback, Foresignal also records `ACK_FAST` (acknowledged within 15 minutes),
`SNOOZED` (the alert, or its rule while the alert was open, was snoozed), `FLAP` (resolved by the inverse edge within
10 minutes) and `UNOPENED` (its notification was not read within 24 hours; only when a device uses the inbox read
cursors). They are never stored as feedback. `signalScore` weighs them (+0.5, −0.5, −0.5, −0.25) per alert.

**Reading the quality view.** `GET /v1/triggers/{id}/quality?since=` shows feedback precision (with a Wilson
interval once five alerts are labeled), the signal counts and `signalScore` next to it (never mixed in), and
`daily[]`: per-day alerts, feedback counts, signals, median time to acknowledge, flaps and matches suppressed by
the cooldown or the rate cap. A nightly job (after 02:00 UTC) writes these rows and keeps them 400 days. Exercise
alerts never count. A briefing can include a weekly quality section with `scope.includeAlertQuality: true`.

**Suggestions.** `GET /v1/triggers/{id}/suggestions` generates and stores deterministic suggestions (tuning 1.1.0):
thresholds, cooldowns, enter mode, a smaller geofence, anomaly sensitivity, an `ACTIVE_WINDOW` when most negative
alerts fall in hours with few positives, `SEVERITY_UP` when many labels say `late`, and `SEVERITY_DOWN` for a
critical rule labeled mostly noise. `status=OPEN` lists the stored ones; `explain=true` adds a short plain-language
summary from the chat provider (budgeted; needs a token). Nothing changes a rule on its own.

- **Apply:** `POST /v1/triggers/{id}/suggestions/{suggestionId}/apply` with an optional `note` applies the patch as
  an ordinary rule update (the same validation and events as `PUT /v1/triggers/{id}`) and writes a tuning log
  entry with the rule before and after. A suggestion generated before the rule last changed is `STALE` and refused
  with `409 SUGGESTION_STALE`; generate suggestions again.
- **Dismiss:** `POST …/dismiss` with an optional `reason`.
- **Undo:** `POST /v1/triggers/{id}/tuning-log/{logId}/undo` restores the earlier definition while the rule is
  unchanged since the change (`409` otherwise). `GET /v1/triggers/{id}/tuning-log` lists the changes.


## Evidence and observability (prompt 116)

**Why independence matters.** One earthquake can arrive four times: from USGS, from EMSC, from GDACS and from a
news report. Four records are not four confirmations. GDACS republishes USGS for earthquakes, and the news report
cites one of them. Foresignal therefore separates the real-world event from the observations that describe it:

- every source maps to an **originator** (`intel/originators.json`, extended by the `intel.originators` setting or a
  descriptor's own `originator`): an agency, sensor network, aggregator, outlet, crowd, your own receiver, or
  Foresignal itself. Originators in one **independence group** (for example the community ADS-B networks, which share
  feeders) are never independent of each other, and an aggregator lists the originators it **republishes**;
- the evidence resolver links event-class observations (earthquakes, fires, conflict events, outages, disasters,
  weather alerts; `intel/canonical-rules.json`) to **canonical events**, using the corroboration tolerances
  (earthquakes 60 s, 100 km, 0.8 magnitude; fires 1 km, 1 h; conflict events the same 0.1° cell and UTC day; outages
  the same country or network with overlapping times) and, ahead of them, the upstream's own event id (a GLIDE
  number, the CAP identifier). Each link shows its confidence and why it was made; links below
  `evidence.minLinkConfidence` (0.5) are not made;
- observations that trace back to one originator form one **evidence group**. A news item that cites its source is
  judged by what it cites. Only evidence groups count as independent: in the example above, USGS and GDACS are one
  group and EMSC another, so the event has two independent confirmations, not four.

`GET /v1/evidence/events` lists canonical events (filter by `family`, `region`, `since` and `flags`);
`GET /v1/evidence/events/{id}` shows every observation, its originator, its link reason and the groups;
`GET /v1/evidence/of?source=&externalId=` shows where one record went. The `evidence` stream topic announces
`canonical.created`, `canonical.merged` and `canonical.flagged`. The corroboration layer (`derived-corroboration`)
is now one record per canonical event, and the intelligence-event scorer (1.1.0) counts signal families from
distinct evidence groups, so repeated reports never raise confidence or evidence quality.

**Quality flags** describe the shape of the evidence; they never say a report is false:

- `ECHO`: five or more observations of one evidence group within two hours: many re-reports of one origin;
- `SINGLE_ORIGINATOR`: only one evidence group;
- `TIMESTAMP_CONFLICT`: the observations disagree about when it happened by more than three times the family's
  tolerance;
- `LATE_REVISION`: a source moved the event's time or place after first publishing it.

**Detection probabilities.** A healthy source is not the same as a source that could have seen something. For each
source, family, place and threshold band Foresignal keeps time-versioned estimates of the probability of
detection (`p`, with an interval):

- **capture–recapture** for families with independent sources: of the events independent sources reported in the
  last year, the share this source also reported (at least 20 reference events; per region and, for earthquakes,
  per magnitude band such as `M4.5-5`);
- **presence proxy** for aircraft and vessels: how often a map cell had traffic in the source's active hours over
  the last 28 days, relative to its busiest cells (always low confidence); your own receivers get their
  **measured range** instead;
- **declared** footprints otherwise (`intel/observability.json` or the descriptor's `observability`): inside the
  declared area and above the declared threshold, for example USGS reports magnitude 2.5 in the United States but
  4.5 elsewhere. A source that declares nothing is assumed to observe everywhere; declare a footprint to restrict it.

The estimator runs weekly (Mondays from 04:30 UTC). Combined detection is `1 − Π(1 − p)` across independence
groups, never across sources of one group. A question about the past always uses the estimate that was valid then.
`GET /v1/observability?family=seismic&region=TR` (or `&bbox=` for map cells, `&at=` for a past time,
`&include=estimates` for the per-source numbers) answers how well instrumented a place is, and
`GET /v1/observability/tiles/{z}/{x}/{y}.mvt?family=` draws it as a map layer.

**Why some absences say `unknown`.** "Nothing happened" needs two things: every involved source healthy over the
window, and a combined detection probability of at least `observability.absenceMinDetect` (0.8). Otherwise an
absence rule, or a downward anomaly, reports `unknown` with reason `LOW_DETECTABILITY`, and never alerts: the
sources could not have seen it, so silence says nothing.

**Manifests and replay.** Every analytic output (anomaly evaluations, intelligence-event scores, recipe runs,
analyst runs, backtest jobs, observability estimates) points to an analysis manifest: the component and version,
the build, the configuration and its hash, the hashes of the registry files used, `asOfSeq`, the inputs and, for
LLM steps, the provider, model, prompt hash and prompt template. Identical manifests are stored once.
`GET /v1/manifests/{id}` shows one (agent), and `POST /v1/manifests/{id}/replay` queues a job (poll
`GET /v1/jobs/{jobId}`) that re-runs a deterministic component with exactly those inputs and reports `identical`,
`differs` (with what changed) or `not-replayable` (inputs purged by retention, a component that no longer exists,
or LLM output: "inputs recorded; model output not reproducible"). Every LLM role's system prompt is a versioned
template; `GET /v1/prompt-templates/{id}` shows exactly what the model was asked.

## Markets and attention (prompt 117)

Five sources add the market and attention signals the intelligence layer correlates with events. All of them write
`series-point` records (one per period) and one `series-latest` per series, so `/v1/series`, `get_series`, rules
and briefings work on them like on the energy series of 093. Every catalog entry now says what kind of number it is:

- `family`: `price`, `volume`, `open-interest`, `position`, `rate`, `attention`, `tone`, `count`, `index` or
  `structural`;
- `transform`: the hint for analysis (`log-return` for prices, `log-level` for counts and attention, `difference`
  for positions and storage, `level` otherwise);
- `releaseLagDays`: how long after its period a value is normally published. The point's `knownAt` is when
  Foresignal received it; its period is never back-dated, so analyses only use what was known at the time;
- `egress`: `OPEN` or `LOCAL_ONLY`; `regionCode` and `topicId` where they apply; `attribution`, the credit line.

**What each source gives.**

| Source | What | Lag | Needs |
|---|---|---|---|
| `cftc-cot` | Weekly futures positioning from the CFTC Commitments of Traders: open interest and the net position of each trader category (managed money, producers, swap dealers, asset managers, leveraged funds…) for twelve default contracts (`contracts`), from the disaggregated, legacy and TFF reports (`reports`; `futuresOnly=false` reads the futures-and-options reports). Series `cot.{code}.{report}.{measure}`, for example `cot.067651.disaggregated.mm_net` (WTI managed money net). | Positions as of Tuesday, published Friday 15:30 ET: 3 days | nothing; an optional Socrata `appToken` raises the rate limit |
| `fred-series` | Rates and prices from FRED: 10- and 2-year Treasury yields, the 10y−2y spread, the effective federal funds rate, the broad dollar index, WTI and Henry Hub spot prices (`series`). Series `fred.{id}`. | about 1 day | a free FRED API key (`PUT /v1/sources/fred-series/credentials` with `apiKey`) |
| `wikimedia-pageviews` | Daily Wikipedia pageviews of your attention topics' articles (human readers), and each project's daily top 100 articles (`attention-top`). Series `wiki.{project}.{article}`. | 1 day (read once a day after 02:30 UTC) | attention topics with articles |
| `gdelt-timelines` | News volume (matching articles per 15 minutes, with `extra.share` of everything GDELT monitored) and average tone per attention topic. Series `gdelt.{topicId}.vol` and `gdelt.{topicId}.tone`. | none | attention topics with a GDELT query |
| `worldbank-indicators` | Slow structural covariates per country and year: population, GDP per capita, infant mortality, urbanization, trade share, electricity access (`indicators`, from `fromYear`). Series `wb.{indicator}.{iso2}`. | about 180 days | nothing |

**Daily and weekly data cannot show intraday trading.** COT positions are a weekly snapshot and FRED prices are
daily closes: they can show that positioning moved before an event, never when within a day it moved. Market
findings say "unusual positioning before the event", never more.

**The FRED key and notice.** FRED is free with a key from <https://fred.stlouisfed.org/docs/api/api_key.html>.
FRED's terms require this notice wherever its data appears: "This product uses the FRED® API but is not endorsed or
certified by the Federal Reserve Bank of St. Louis." Foresignal carries it, with the series' own source line, in the
`attribution` of every FRED series and in the source's attribution.

**Copyrighted series and `LOCAL_ONLY`.** FRED republishes series owned by others (Cboe's VIX and OVX, ICE's Brent
price…). The whole `fred-series` source is `LOCAL_ONLY`: its raw values are served to this instance's own callers
allowed local egress and never leave through sinks, federation, share links or feeds; derived, standardized
statistics may. A series whose FRED notes mark it as copyrighted is skipped (the source shows `degraded` with the
skipped ids) until you list it in `allowCopyrighted`, which records that you accept it for personal use:

```
PUT /v1/sources/fred-series
{"params": {"series": ["DGS10", "VIXCLS"], "allowCopyrighted": ["VIXCLS"]}}
```

**Attention topics.** A topic names what you want to watch: `GET /v1/attention/topics` lists them, `POST` creates
one, `PUT /v1/attention/topics/{id}` replaces it and `DELETE` removes it (stored series stay). Enabled topics are
written into the `topics` parameter of `wikimedia-pageviews`, `gdelt-timelines` and, since prompt 133,
`gdelt-articles` on every change, so enable the ones you want as well. Five examples ship disabled: `red-sea-shipping`, `strait-of-hormuz`,
`gulf-energy-infrastructure`, `taiwan-strait` and `ukraine-front`; enable one with a `PUT` that sets
`"enabled": true`. A topic has an id (`a-z`, `0-9` and `-`, 3 to 48 characters), a name, up to 20 Wikipedia articles
(`{"project": "en.wikipedia", "article": "Strait_of_Hormuz"}`), up to 30 keywords (the situation linker uses them),
040 region codes, themes and, since prompt 135, a `social` block (see
[Social signals](#social-signals-prompt-135)).

**Writing a GDELT query** (at most 250 characters): quote phrases (`"Strait of Hormuz"`), and put alternatives joined
with `OR` in parentheses (`("Red Sea" OR "Bab el-Mandeb") (tanker OR Houthi)`); terms side by side must all match.
Foresignal checks the characters (balanced quotes and parentheses, no control characters) and, when you create a
topic while `gdelt-timelines` is enabled, makes one budgeted test call; a query GDELT does not answer with a
timeline is refused with `400 TOPIC_INVALID`. GDELT allows one request every 5 seconds, so a run covers at most five
topics (two requests each, 5 seconds apart) and takes the rest in turn on the next runs. Since prompt 133
`gdelt-timelines` and `gdelt-articles` both declare a 5-second `hostPacing`, so they share one gate on
`api.gdeltproject.org` and together never start more than one request every 5 seconds; a request that cannot start
within 30 s is skipped (`host-pacing`) and retried on a later run (see
[News coverage and media bias](#news-coverage-and-media-bias-prompt-133)).

**Wikipedia's User-Agent policy** asks for contact details. Foresignal's User-Agent always carries the project URL;
set `contactEmail` with `PUT /v1/settings/server` and it is appended as `; contact=…` for every upstream. If
Wikimedia answers 403, the source shows `failing` with that reason.

**Backfilling COT history.** Live polling reads the last year. To load older reports:

```
POST /v1/sources/cftc-cot/backfill
{"from": "2010-01-01"}
```

The source must be enabled. The answer (202) is the job; the backfill reads older pages during the source's runs
and a tick every two minutes, at most five pages a run and never past the source's budget, so a history back to 2010 takes
some minutes. An interrupted backfill continues where its last successful run stopped, and running it again changes
nothing that is already stored (`409 BACKFILL_RUNNING` while one is open).

**Not built:** ICE's own weekly COT for Brent and gasoil (its website terms forbid automated collection), CME and ICE
end-of-day prices, UKMTO advisories and Metaculus. An external provider (EDP, prompt 024) can supply any of them
from your own subscription.

## Situations (prompt 118)

A **situation** is a storyline that lasts days to months ("Red Sea shipping disruption"). It groups intelligence
events, findings, alerts, anomalies, records and investigations, has a lifecycle (`emerging`, `active`, `dormant`,
`closed`), a severity, regions, a geometry (the convex hull of its members buffered by 25 km), a deterministic
summary and an optional LLM brief. `GET /v1/situations` lists them by latest activity; `GET /v1/situations/{id}`
returns the evidence first (members grouped by signal family, each with its join reason and corroboration status),
then the inference (status, severity, summary), then the brief labeled as speculation.

**What links, and why.** The linker (`situation-linker 1.0.0`) reads, from its durable cursor `situation.cursor`:
intelligence events that are `emerging` or `active`, analytic findings of severity notable or higher, alerts whose
rule carries the tag `situation` or whose severity is critical, and `activity-up` / `activity-down` anomalies. For
each input it scores the open situations that pass a cheap prefilter (a shared region code, the geometry within the
distance limit, a shared entity or a matching topic keyword):

| Component | Contribution |
|---|---|
| Shared first-level region (`US-TX`) | +0.30 |
| Shared country, when no first-level region is shared | +0.20 |
| Distance to the situation geometry | +0.25 × max(0, 1 − d / 300 km); 500 km when the input or the situation is maritime |
| Shared 098 entity | +0.25 |
| The input's text matches a keyword of one of the situation's 117 attention topics | +0.15 |
| Text similarity of the 027 embeddings (input record vs. the situation's record) | +0.20 × max(0, (cos − 0.70) / 0.30) |
| Incompatible theme (`mixed` is compatible with all) | −0.20 |
| Recency: the sum, clamped to [0, 1], is multiplied by | 0.5 + 0.5 × exp(−hours since the situation's last activity / 72) |

The best situation joins when its score is at least `linkThreshold` (0.45). Otherwise an `active` intelligence event
of severity warning or higher, a significant finding or a critical alert seeds a new `emerging` situation titled
"{type} — {region}"; anything else is not linked. An input already in a situation is never linked again (move it
with `POST /v1/situations/{id}/members` and `moveFromSituationId`). Every member stores its `joinReason` (score,
components, linker version), so `POST /v1/situations/linker/test {"fromSeq", "toSeq"}` reproduces the decisions of
any seq range without storing anything.

**Lifecycle.** `emerging` becomes `active` when the core members come from at least two signal families **and** at
least two independent evidence groups (116: re-reports of one originator never count twice), or when you set
`"status": "active"`. A situation without a new core member for `dormancyWindow` (7 days) goes `dormant`; a new core
member within `reopenWindow` (30 days) makes it `active` again; 90 days dormant closes it. `closed` is final: a new
situation starts instead. Severity is the highest core-member severity unless you override it
(`"severityOverride": "critical"`, `null` clears it). Anomalies, and since prompt 133 news stories, join as `context`
members: they never drive status or severity.

**Tuning.** The settings are app settings without their own endpoint (like 116's): `situation.linkThreshold`
(0.45; lower links more, higher splits storylines), `situation.dormancyWindow` (`7d`) and `situation.reopenWindow`
(`30d`). Raise the threshold first when unrelated events end up together; give situations topics
(`PATCH {"topicIds": [...]}`) so news and alerts that name the storyline join it.

**Themes.** Each input gets a theme from `intel/themes.json` (intelligence event types, finding types, alert and
anomaly rule tags, record kinds, then keyword rules on the text); add rows with the setting `intel.themes` (same
shape, merged over the file). Untyped text records that no rule matches stay without a theme unless you set
`intel.themes.llmClassify=true`: then the chat provider picks one theme from the fixed list (effort low, budgeted,
cached by the text's hash, labeled `themeSource: llm`). Off by default.

**What changed since.** A job at 00:10 UTC writes one snapshot per open situation for the previous day.
`GET /v1/situations/{id}/changes?since=2026-10-10T00:00:00Z` compares that day's snapshot with now: status and
severity before and after, new members by family and type, area change, regions added and removed. No LLM is used.

**Briefs and their cost.** With a chat provider and `situation.briefs.enabled=true` (off by default), a brief is
rewritten when a situation activates, when its severity changes, or when three new core members arrived since the
last brief, at most once per `situation.briefMinInterval` (`30m`) per situation and at most
`situation.briefs.maxPerDay` (50) attempts per day across all situations. Each brief is one budgeted tool loop
(effort low, up to 6 tool calls) whose claims are typed and whose citations must name a member or a tool result; a
private situation is never sent to a remote provider. A failure or budget refusal keeps the previous brief.
`POST /v1/situations/{id}/brief` asks for one now (202; 409 `LLM_NOT_CONFIGURED` when briefs are off). The brief never
changes members, status or severity.

**Shaping situations.** `POST /v1/situations` creates one by hand (members verified by `seq`); `PATCH` renames,
changes the theme, overrides severity, activates, reopens or closes; `POST .../members` and `DELETE
.../members/{memberId}` add and remove members; `POST .../merge {"intoId"}` keeps the older situation and closes the
other; `POST .../split {"memberIds", "title"}` moves members into a new one. Every change is in the situation log
with the token that made it (`GET .../timeline`). A stale `version` is `409 SITUATION_VERSION_CONFLICT`; changing a
closed situation is `409 SITUATION_CLOSED`.

**Records, SSE and notifications.** Every public situation is a `foresignal-intel` record of kind `situation`
(title, theme, status, severity, memberCount, signalFamilies, regionCodes, corroborationShare), so an ordinary rule
can alert on it, for example `kind=situation` and `severity == critical`. Private situations (any member from a
private source) are never published as records and are visible only with private access. The `situations` topic of
`/v1/events/stream` sends `situation.created`, `.updated`, `.activated`, `.escalated`, `.dormant`, `.closed` and
`.merged`. The notification event types `situation.activated` and `situation.escalated` are off by default; to
receive them on a channel, read it with `GET /v1/channels/{channelId}`, add them to its `events` and send the whole
channel back with `PUT /v1/channels/{channelId}` (a `PUT` replaces the channel), for example:

```
"events": ["alert", "situation.activated", "situation.escalated"]
```

## Discovery (prompt 119)

Discovery finds patterns nobody wrote a rule for and says how often each would turn up by chance. Three detectors run
(and, since prompt 126, the `forecast` detector of "Outlooks and indices", whose exceedances are findings too; since
prompt 127 the impact projector of "Impact projection" writes `projected-impact` findings; since prompt 133 the coverage
job of "News coverage and media bias" writes `coverage-blindspot` and `coverage-divergence` findings; since prompt 135
the social jobs write `social-velocity` findings, and since prompt 136 the narrative jobs of "Narratives and claims"
write `media-reuse` and `social-coordination` findings, and since prompt 137 the attention-gap detector of "Social
versus physical" writes `attention-gap` findings)
on their own schedule, deterministically and without an LLM; what they find is an **analytic finding**
(`GET /v1/analytics/findings`), also published as a record of the DERIVED source `foresignal-analytics` (kind
`analytic-finding`), so ordinary rules can alert on findings (`kind=analytic-finding`, `severity == significant`) and
the situation linker (118) attaches them to storylines.

**What feeds it.** Every detector reads series through one series reader, by reference:
`series:{sourceId}/{seriesId}` (the market, energy and attention series of 093 and 117, and
`series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits), `rollup:{hour|day}/{sourceId}/{kind}/{region}`
(activity counts by 040 region) and `rule:{ruleId}/{groupKey}` (an anomaly rule's samples). `GET
/v1/analytics/series/data?ref=…` shows exactly what a detector sees: each point with `knownAt` (when Foresignal knew it)
and `coverageOk`, and the gaps, which are unknown periods, never zero.

**Region rollups.** The activity rollup now also counts by region: region-hour rows (kept 1095 days,
`RetentionSettings.stats.regionHourly`) and region-day rows (3650 days, `stats.regionDaily`). A record's region is its
040 country, or its first-level region (`US-TX`) for sources you list in the app setting `stats.regionLevel`
(`{"acled": "admin1"}`); untagged records count in `ZZ`. Source `*` in a rollup reference is every public source
together with re-reports of one event counted once (116 canonical events), which is what the detectors use.
`/v1/stats/timeseries?groupBy=region` and `region=` and `/v1/stats/top?by=region` read these rows.

**The registry.** `GET /v1/analytics/series` lists the series the detectors run on. Every night at 02:00 UTC
Foresignal registers busy region-day rollups (30 non-zero days in the last 90), every daily, weekly or hourly market
and attention series, the PortWatch chokepoints and anomaly rule groups with eight weeks of samples, keeping at most
`analysis.series.max` (2,000) automatic rows, busiest first. Automatic rows list the change-point detectors; since
prompt 126 they also list `forecast` for region-day rollups with at least 365 days of history, the market and attention
series and the PortWatch chokepoints. `PUT /v1/analytics/series` adds or changes a row as yours; automatic registration
never removes your rows.

**Change points** (03:00 UTC on daily and weekly series; hourly series you register run at :20). A CUSUM detector
watches each series against its own recent baseline (the same weekday over the previous eight weeks for counts) and
flags a sustained shift of at least 50%; its alarm threshold is set by simulation so that it would false-alarm about
once a year on a steady series. The shift must persist for three covered buckets. A second, Bayesian detector (BOCPD)
confirms it: posterior 0.8 or more makes the finding `significant`, otherwise it is `notable`. A gap in coverage after
the change makes it `unknown`, and a fall in counts is reported only when 116 observability says the sources could have
seen it (`LOW_DETECTABILITY` otherwise).

**Event studies.** `POST /v1/analytics/event-studies` asks "did this series move unusually around these events?" as a
103 job (read it with `GET /v1/backtests/{id}`): an event selector (an intelligence event type, an alert rule or tag,
an anomaly rule, situations turning active, change points by family, or explicit times), a target series and a range.
The answer is the cumulative abnormal movement (CAR, in standard deviations) before (`[−5, −1]` days or `[−2, −1]`
weeks) and at each event, averaged across events (CAAR), with a standardized test, a rank test and a placebo test.

**Live pre-event signals and unexplained moves.** When an intelligence event of warning or higher becomes active, a
situation turns active or a significant change point appears, Foresignal checks the **watch pairs** of the registry:
series paired with that signal family (and regions). Only data known before the event counts. A move of at least
`minEffect` (2 SD) that happens before fewer than 1% of comparable windows without an event becomes a
`pre-event-signal`. Weekly series published with a delay are re-checked after `releaseLagDays` and say
`LAGGED_PUBLICATION`. Every day at 04:00 UTC price, volume, positioning, attention and tone series are scanned for moves
of 3 SD or more with no qualifying event in their regions or topic within two days (`unexplained-move`); an event within
the next seven days is linked to the move. The defaults (`intel/watch-pairs.json`) pair Middle East and Gulf conflict
with WTI positioning (COT managed money), FRED and EIA oil prices and the Gulf, Red Sea and Hormuz topics' news volume,
tone and pageviews; maritime behavior with chokepoint transits and WTI positioning; and outages with each topic
region's news volume. Missing series are skipped. Edit a series' pairs with `PUT /v1/analytics/series`.

**Co-occurrence** (05:00 UTC). For every region and pair of signal families, Foresignal asks whether both ran above
their own day-of-week baselines on the same day, combines the two p-values (Fisher) and controls the false-discovery
rate across all of the day's tests (Benjamini–Hochberg, 5%). Survivors are `co-occurrence` findings; `significant`
needs an adjusted p of 0.001 or less and a lift of 3 or more over what independence would predict.

**Reading a finding.** `inference.baseRate` says how often a finding this strong happens by chance: for change points
the alarms the same settings raised on this series' own history per year; for pre-event signals the placebo share
("moves this large occur before about 0.6% of comparable windows with no event"); for co-occurrences the joint days a
year independence predicts. A placebo p-value of 0.01 means one comparable window in a hundred with no event moved as
much. `caveats` list what the finding cannot say. **`DAILY_RESOLUTION_ONLY`** on every market finding means Foresignal's
market data is daily or weekly: moves that happen within a trading day, as in licensed minute data, are invisible.
A pre-event signal is unusual movement before an event, never evidence of wrongdoing. `PATCH
/v1/analytics/findings/{id}` with `{"status": "dismissed", "reason": "…"}` dismisses one (its record is tombstoned).
Findings over `LOCAL_ONLY` series (FRED, LSEG-sourced EIA prices) keep their raw levels from callers the egress policy
refuses and never put them on records.

**Notifications and streams.** Add `finding.significant` to a channel's `events` to be told about significant
findings (off by default; private findings send none). The `analytics` topic of `/v1/events/stream` (cursor key `f`)
carries `finding.created`, `finding.updated` and `finding.dismissed`, plus the precursor events of prompt 120,
`outlook.published`, `index.changed` and `index.weights_proposed` of prompt 126, and `impact.projected` and
`impact.superseded` of prompt 127.

**Tuning and cost.** Everything is deterministic database work, no LLM. The nightly runs read up to 400 days per daily
series; the series cap (`analysis.series.max`) bounds the work. `analysis.findings.publishInfo=true` also publishes
`info` findings as records (off: they are stored and listed only). Operator rows of the signal-family registry go in
the app setting `intel.signalFamilies` (the shape of `intel/signal-families.json`).

## Precursors (prompt 120)

A precursor is a pattern where one kind of event, or one series' movement, tended to come before another in stored
data: "GNSS interference alerts in Yemen were followed by maritime disruption events within three days in 12 of 30
cases, against 11% expected by chance". Foresignal looks for these on its own every night, keeps the ones that hold on
later data in a **precursor library** (`GET /v1/precursors`), and tells you when a validated precursor's first half
happens. Everything is deterministic database work without an LLM, and every precursor says what it is: an
**association in stored data, not causation**.

**Where candidates come from.** Each night at 01:30 UTC a mining run (a 103 job of type `precursor-mining`; `POST
/v1/precursors/mine` runs one now) builds a batch of candidate pairs:

- **Your pairs** (`POST /v1/precursors/candidates`): an antecedent and a consequent (event selectors, as in event
  studies, or two series references), a spatial rule (`sameRegion`, `withinKm`, `sameEntity` or `none`) and lag bounds.
  Every run includes them.
- **Automatic event pairs:** in each country, the 12 busiest event families over the discovery window (intel event
  types, alert rule tags, anomaly classifications per rule, change points and unexplained moves per signal family,
  situations turning active per theme), every ordered pair of two of them, with lags up to 1, 3, 7 and 14 days, in the
  same country (or the same first-level region when both families carry one).
- **Automatic series pairs:** enabled analysis series that share a region, a family or a topic, with lags of 1 to 14
  days (daily) or 1 to 8 weeks (weekly).

A run tests at most `analysis.precursors.maxTestsPerRun` (500) candidates: yours first, then the busiest antecedents.
The batch (`GET /v1/precursors/batches`) says how many were generated, tested, cut by the cap (`capped`) and skipped as
`insufficient`.

**What "validated" means.** The history of each pair (the last `analysis.precursors.lookbackDays`, 365, where both
sides have data) is split by time: the oldest two thirds (`analysis.precursors.discoveryFraction`) is the **discovery**
window and the newest third the **validation** window. A candidate is tested on discovery only. Those that pass are
tested again, with exactly the same definition, on the validation window, which the first test never saw. Only a
pattern that holds there too becomes `validated`:

- event pairs: again `notable` (p ≤ 0.05 and a lift of 2 or more against the circular-shift baseline), a lift of at
  least 1.5, and the hit rate's 95% interval entirely above the rate expected by chance;
- series pairs: the same direction (positive or negative correlation) and p ≤ 0.05.

**Why most candidates fail.** That is the point. With hundreds of tests a night, some will look striking by luck alone.
Foresignal counts every test of a batch and adjusts the p-values for that number (Benjamini–Hochberg at a false-discovery
rate of 10%, or the stricter Benjamini–Yekutieli when series pairs share a series), so a test has to beat the whole
batch, not just chance. Then it has to hold again on newer data. A pattern that passes the first step but not the second
stays a `candidate` with the reason (`statusReason`), and you can see both windows' numbers (`discovery`, `validation`:
raw and adjusted p, hit rate, base rate, lift, verdict and the reverse test). Short histories (under 60 days), coverage
gaps over 30% and fewer than 10 antecedents are `insufficient`, not failures.

**Honest counting.** Re-reports of one event by several sources count once (116 canonical events). An antecedent with
no consequent counts as a miss only when the sources that would have seen the consequent could have seen it (116
detectability); otherwise it is left out and counted under `LOW_DETECTABILITY`. The reverse direction (does the
consequent precede the antecedent?) is always tested; when it is as strong, the precursor says "co-occurrence, not
precedence". Mining uses non-private data only, so precursors are never private.

**The monthly re-test.** `POST /v1/precursors/retest` (and the monthly maintenance, see "Analysis cycles") tests every
validated and stale precursor on the newest third of its range. A validated precursor whose lift falls below 1.2, or
whose hit-rate interval now includes the chance rate, becomes `stale`; stale twice in a row, it is `retired`; a stale
precursor that validates again returns to `validated`. Each change is announced on the `analytics` topic of
`/v1/events/stream` (`precursor.validated`, `precursor.stale`, `precursor.retired`) and logged in the precursor's
`history`. Only one mining or re-test run happens at a time (409 `PRECURSOR_MINING_IN_PROGRESS`).

**Your judgment.** `PATCH /v1/precursors/{id}` with `{"status": "retired", "reason": "…"}` retires a pattern you know to
be an artifact; `{"status": "validated", "reason": "…"}` pins one as operator-validated, which the re-test then leaves
alone. Both are logged with your token in `history`, and a pinned precursor carries the `OPERATOR_VALIDATED` caveat.

**Live watch.** Every minute Foresignal checks new intel events, situations turning active, change points, unexplained
moves, alerts and anomalies against the antecedents of validated event precursors (and, daily at 06:15 UTC, whether a
validated series precursor's antecedent moved 2 or more robust standard deviations). A match writes a
`precursor-active` finding with the expected window for the consequent and a probability: the precursor's hit rate,
`(hits + 1) / (n + 2)`, pulled toward the chance rate by its sample size (`n / (n + 20)`), so a pattern seen ten times
says less than one seen a hundred times. Prompt 122 turns these into forecast questions. Its position is the durable
cursor `precursor.cursor`, which retention respects.

## Base rates and analogs (prompt 121)

**Why the outside view matters.** Before asking "what is special about this case?", ask "how often does this kind of
thing happen at all?". A forecaster who starts from the base rate (the reference class: battles in Yemen per 28 days,
M6 earthquakes in Indonesia per week) and then adjusts for what is special is right far more often than one who starts
from the story. Every probability Foresignal produces starts from a stated base rate, and analysts and agents can ask
for one directly (`POST /v1/base-rates/query`, MCP `get_base_rate`).

**Where the history comes from.** Foresignal's own history starts the day you install it, which is too short for rates
of rare events. So you can import decades of reference history, kept only as weekly counts per family and region
(`reference_history`), never as raw rows:

| Dataset | What to do | License |
|---|---|---|
| UCDP GED (conflict, 1989–) | Download the GED CSV from `https://ucdp.uu.se/downloads/` (for example `ged261-csv.zip`), put it under `${FORESIGNAL_DATA_DIR}/imports/`, then `POST /v1/reference-imports {"dataset": "ucdp-ged", "params": {"file": "ged261-csv.zip"}}`. The ZIP or the CSV inside it both work. With the `ucdp-ged` source's token set you can use the API instead: `{"useApi": true, "version": "26.1", "yearFrom": 1989}` (it spends that source's budget). | CC BY 4.0: credit UCDP when you share results |
| USGS ComCat (earthquakes, decades) | `POST /v1/reference-imports {"dataset": "usgs-comcat", "params": {"yearFrom": 1973, "minMagnitude": 4.5}}`. One request per year (split when a year has more than 20,000 events). | Public domain |
| GDACS (disasters, 2000–) | `{"dataset": "gdacs", "params": {"yearFrom": 2000}}`: orange and red alerts per hazard type. | Open, with attribution to GDACS |
| ACLED (conflict and protest, by country) | Set the `acled` source's `email` and `password` credentials first, then `{"dataset": "acled", "params": {"countries": ["YE", "SD"], "yearFrom": 2018}}` (up to 30 countries). Without credentials the import is recorded as `skipped` with the reason `credentials absent`. | ACLED's terms forbid redistribution: rows are `LOCAL_ONLY`, used for computation only |

Import UCDP GED and USGS ComCat first: together they give conflict and seismic base rates for every country before
Foresignal has much history of its own. Each import is a 103 job (`GET /v1/reference-imports/{id}` shows progress, rows
read and written, the period and the license). Asking for the same dataset and parameters again resumes or re-runs the
same import: finished years are skipped and a re-run writes exactly the same counts, so nothing is ever double counted.
`DELETE /v1/reference-imports/{id}` removes the import and its rows; rates that used them change at the next refresh.

Every night at 02:15 UTC Foresignal folds its own history into the same table (`source = foresignal`): canonical events
(re-reports of one event count once), per family, region and complete week, plus the M4.5, M5 and M6 bands for
earthquakes. So the reference set keeps growing after the imports end.

**Families.** Reference families are a signal family plus a subtype: `conflict:battles`,
`conflict:explosions-remote`, `conflict:violence-civilians`, `conflict:protests`, `conflict:riots` (ACLED event types;
UCDP state-based and non-state violence count as battles, one-sided violence as violence against civilians),
`seismic:m4.5`, `seismic:m5`, `seismic:m6` (an M6.2 counts in all three), `disaster:gdacs-orange+`,
`disaster:gdacs-red` and `disaster:gdacs-{eq|tc|fl|vo|dr|wf}-orange+`. Foresignal's own history adds the bare families
(`conflict`, `fire`, `outage`, `disaster`, `weather-alert`, `seismic`).

**What a base rate says.** A definition names a family, regions (040 codes, or a region class such as `continent:AF`
or `income:low`, or neither for worldwide), a window (`P7D`, `P28D` or `P90D`, the last being 13 whole weeks) and,
optionally, a condition. The answer gives:

- `ratePerWindow`: mean events per window;
- `pAtLeastOne`: the share of windows with at least one event, with a 95% **Jeffreys interval** (`interval`). Read the
  interval, not just the number: 3 of 10 windows and 300 of 1,000 are both 30%, but the first could easily be 10% or
  60%;
- `nWindows`, `nEvents`, `periodFrom`, `periodTo`: how much history stands behind it;
- `sourceMix` and `licences`: which sources contributed how many events. Each region and week uses one source only
  (ACLED before UCDP for conflict, USGS for earthquakes, GDACS for disasters, then Foresignal's own history), so the
  same events are never counted twice;
- `caveats`: `SMALL_SAMPLE` (fewer than 30 windows), `MIXED_SOURCES` (different sources for different regions or
  periods; their coding rules differ), `LOW_REPORTING_DENSITY` (the region is among the 10% least reported countries
  for its population size: the true rate is probably higher) and `LOW_DETECTABILITY` (Foresignal's own history was
  used where its sources would miss many events);
- a conditional rate (`condition: {family, withinDays}`) keeps only the windows that followed the antecedent within
  that many days, and reports the unconditional rate alongside (`unconditional`), so you can see whether the
  antecedent really changes the odds.

**Reporting density.** Media coverage differs by two orders of magnitude between countries. Each refresh computes, per
country, its weekly count of text-reported conflict events over the last ten years divided by the median of countries
of similar population (`regionProfiles` on each rate). The bottom decile gets `LOW_REPORTING_DENSITY`.

**Automatic and custom rates.** Automatic rates exist for every family and region with at least 52 weeks of coverage,
for 7- and 28-day windows. They are recomputed by the monthly maintenance (see "Analysis cycles"), after each finished
import, and on `POST /v1/base-rates/refresh` (one refresh at a time). Agents and operators can store their own definitions (`POST
/v1/base-rates`) and delete them; automatic rates cannot be deleted. Ad-hoc queries are computed on the spot and cached
for 24 hours.

**Analogs.** For a situation, `GET /v1/situations/{id}/analogs` finds past situations that looked like it at the same
age: the same families in similar numbers in their first days (scaled by how variable each family is), a similar
escalation slope, theme, region class, entity types and severity path, and a similar deterministic summary (027
embeddings, when an embedding model is set up; otherwise the result says `feature-only`). Situations that overlap it in
time and its own merges are never analogs. Each analog shows what happened next: duration, peak severity and the
families active in its regions within 7 and 30 days. Add `outcome=conflict:battles/P30D` (or `critical`) to get how
often that happened among the analogs, pulled toward the base rate by `k ÷ (k + 10)`: ten analogs carry half the weight,
so a handful of look-alikes never overrides decades of history. `POST /v1/analogs/query` does the same for a region's
recent weeks without a situation, against two-week periods of the same continent's countries over the previous ten
years. Signatures are computed when a situation becomes active and daily for its first 30 days; outcomes once it is
dormant, closed or a month old.

## Forecast ledger (prompt 122)

The ledger answers "were we right?". It holds **questions** with mechanical resolution, **forecasts** from many
forecasters, and their **scores**. Probabilities are stored as numbers; the ICD 203 words (`almost no chance`, `very
unlikely`, `unlikely`, `roughly even chance`, `likely`, `very likely`, `almost certain`) are derived from them and never
the other way round.

**Writing a resolvable question.** `POST /v1/forecasts/questions` (agent) takes a title, a type (`binary`, `multiple`,
`ordered`, `numeric`), resolution criteria in plain words (shown verbatim to every forecaster) and a machine resolver:

- `event-exists {selector, area, window}`: yes as soon as the selector finds an occurrence; it resolves early;
- `count-threshold {selector | ref, area, window, op?, threshold?}`: the number of occurrences, or the sum of a
  `rollup:` series, compared with the threshold; without `op` and `threshold` (prompt 126) the count itself answers a
  numeric question, and a `referenceFamily` selector then counts the family's events in the reference history (one
  source per region), waiting until the source covers every week of the window;
- `series-value {ref, period | window, aggregate, op?, threshold?}`: binary with a threshold, numeric without;
- `alert-fired {ruleId, window, area?}`, `situation-status {situationId, status?, minSeverity?}`;
- `hypothesis-outcome {setId}` (a hypothesis set's question, resolved by hand with the hypothesis that happened through
  `POST …/resolve`, like a manual question; see "Rolling hypotheses") and `manual`.

Good questions are specific (a place, a window, a threshold), use a `referenceFamily` selector with `area.regionCodes`
where they can (so the base-rate forecaster and the detectability check apply), and avoid `manual` unless nothing in
Foresignal could settle them. Criteria and resolver can change until the first forecast; after that only the title,
tags and closing time can (`QUESTION_IMMUTABLE`). A `dedupKey` stops the same question being opened twice.

**Resolution.** The resolution job runs hourly. It closes questions at `closes_at`, resolves an event question early
when the event happens, and otherwise waits until `resolve_by` plus the sources' publication lag (7 days for weekly
reference history, the series' own lag, at most 14 days; past that it resolves with a `LATE_DATA` caveat). A "no"
answer needs the 116 detectability check over the question's area and window: without it the question is annulled
`LOW_DETECTABILITY`, because "nothing seen" is not "nothing happened". If the resolver's sources were failing for more
than 20% of the window, a "no" or numeric answer is annulled `COVERAGE_GAP`. Annulled questions are never scored.
Manual questions raise `forecast.resolution_needed` in the inbox at `resolve_by` and are listed at
`GET /v1/forecasts/questions?status=closed&resolver=manual`; resolve them with `POST …/resolve` and at least one
evidence ref. Selectors without an observability family (intel events, situations, anomalies) can resolve "yes" but a
"no" is annulled, so prefer reference families, change points or alert rules for questions that may well resolve "no".

**Forecasters.** The baselines forecast every new open question they can, and again at most daily when their inputs
change: `base-rate` (the 121 rate of the question's family, regions and window, scaled to the window; a Poisson count
for thresholds; conditional when a precursor is linked), `climatology` (numeric: the same season in past years, of a
series or, since prompt 126, of a reference-history count),
`persistence` (the last value or state carried forward), `precursor` (questions opened from `precursor-active`
findings, which also open their own questions) and `analog` (121 analogs); `indicator-bayes` (prompt 125) records
the probabilities of a linked hypothesis set at every revision and joins the ensemble like any other forecaster, and
`statistical` (prompt 126) records Foresignal's series forecasts and regional outlooks, and `impact` (prompt 127) records
the probability of each projected impact on the question it opens. When one cannot forecast a question it says
why in `baselineNotes`. You are a forecaster too: `POST …/forecasts` with a read token records your forecast as
`operator:{your handle}`, and so does `/forecast <questionId> <probability>` in an interactive chat (`/questions` lists
the open ones). Forecasts are never edited: a new one supersedes the old, and `madeAt` and `asOfSeq` are set by the
server. `GET …/history` gives each forecaster's standing forecast per day for trend lines.

**The ensemble** pools the latest forecasts (older than 7 days are dropped once the question is older than that) by a
weighted mean of log-odds. Weights follow each forecaster's Brier score relative to the per-question median over the
last 100 resolved questions in the category, shrunk toward equal weights until it has a record (20 questions carry
half the weight). LLM forecasts are clamped to [0.03, 0.97] first, because language models hedge and lean toward
"yes", and once a forecaster has 50 resolved forecasts in a category its probabilities are **Platt-recalibrated**
(`p' = σ(a · logit p + b)`) before pooling. Extremizing (pushing the pool away from 50%) stays off unless the monthly
refit (`POST /v1/forecasts/ensemble/refit`) finds at least five forecasters of different types, 100 resolutions and an
out-of-sample gain.

**Reading the scores** (`GET /v1/forecasts/scorecard`, `/calibration`, `/compare`):

- **Brier**: the mean squared error of the probability (0 perfect; always saying 50% scores 0.25 on yes/no
  questions; multi-category questions run 0–2). **Time-averaged Brier** scores the standing forecast every day the
  question was open, so a late correct guess does not count as much as an early one; days before a forecaster's first
  forecast use the base rate's.
- **Log score**: `−ln p` of what happened, clipped at 1% and 99%. It punishes confident misses hard.
- **RPS** (ordered questions) rewards being close on the scale; **CRPS** (numeric) is the error of the whole
  distribution, from the pinball losses of its quantiles; interval coverage shows how often the outcome fell inside the
  50% and 90% ranges (they should be about 50% and 90%).
- **Skill** is `1 − S / S_ref` against the base-rate forecaster (climatology for numeric) on the same questions, with a
  95% paired bootstrap interval: above 0 beats the outside view, below 0 does worse. An interval that spans 0 means
  "not shown yet".
- **Calibration**: the CORP curve (isotonic regression) plots what actually happened against what was forecast; a
  well-calibrated forecaster sits on the diagonal. Its decomposition splits the score into miscalibration (fixable
  by recalibration), discrimination (telling events apart) and uncertainty (how hard the questions were). Under 50
  resolved questions you get a 10-bin table with counts instead. The Murphy decomposition on the scorecard is the
  binned version.
- **Bias**: mean forecast minus observed frequency (positive says "yes" too often), the share of forecasts between 40%
  and 60% (hedging) and the mean forecast on questions that resolved "no".
- Fewer than 20 resolved questions are labeled `insufficient`: read them as anecdotes.

**Operator versus machine.** `GET /v1/forecasts/compare?a=operator:{you}&b=ensemble` compares two forecasters on the
questions both answered, with the mean Brier difference and its paired bootstrap interval. A negative difference
favors `a`.

**Leakage and retrospective evaluation.** `POST /v1/forecasts/evaluations` (agent) replays one forecaster over past
resolved questions, as of each question's opening time, and scores it: a scorecard marked `retrospective`. The
baselines read only what was known then, so they are fully valid (analogs cannot be replayed and are skipped). A
language model may already know how a question ended if it opened before the model's training cutoff, so an LLM
forecaster needs its provider's `knowledgeCutoff` (`PUT /v1/llm/providers/{id}`); without it the evaluation is refused
(`KNOWLEDGE_CUTOFF_REQUIRED`), and earlier questions are skipped and counted under `LEAKAGE_RISK`.

## Benchmarks (prompt 123)

Software tests prove that the code does what it says; they do not prove that the analysis is any good. The benchmark
harness measures that. It runs every intelligence component in its pure or isolated mode over **deterministic
synthetic scenarios with planted truth** and over a **historical corpus** of past episodes, and grades each component
`pass`, `warn` or `fail` against documented thresholds. It never writes findings, events, situations, forecasts or
hypotheses: only `benchmark_run` and `benchmark_case` rows (plus its 103 job row and a 116 manifest).

**What each scenario tests** (`bench-scenarios` 1.0.0; the same seed always gives the same data and results):

| Scenario | What it plants | What a good system does |
|---|---|---|
| `null-seasonal` | seasonal counts with noise, no signal | change points and anomalies at about their configured false-alarm rate; co-occurrence discoveries within q + 0.05; no validated precursor; base-rate forecasts calibrated within ±0.05 per populated bin; no merged situations |
| `step-change` | a 2 or 3 SD level shift at a known day | the change point found in the right direction, with a short delay |
| `spike` | a two-day (or three-hour) burst; joint family spikes; five planted storylines | anomalies fire; no change point; co-occurrence finds the joint spikes; the situation linker keeps each storyline together and apart from the others |
| `coverage-outage` | true events during a source outage or thin reporting | `unknown` / `LOW_DETECTABILITY`, never a downward finding or `activity-down` |
| `duplicate-echo` | one report re-published ten times by aggregators; multi-source seismic events | one evidence group, unchanged confidence and evidence quality; observations of one event linked into one canonical event, neighbors kept apart |
| `late-revision` | values revised 30 days after first publication | decisions use only what was known at the time |
| `common-driver` | two series (and two event streams) sharing a season and trend, no lead–lag | no validated precursor, or one flagged co-movement |
| `true-precursor` | X followed by Y 48 h later 60% of the time | the precursor validated, lag and hit rate recovered; decoys in the same batch not validated; the precursor forecaster beats the base rate |
| `base-rate-drift` | each region's event rate doubles or halves halfway | base-rate, analog and ensemble forecasts adapt and keep their skill against naive references |
| `pre-event-move` | an abnormal market move before six events, plus six placebo events | a `pre-event-signal` before the planted events, none before the placebos, placebo p near 0.5 on placebo events |

Skill is always relative to a naive reference: pooled climatology for the base-rate forecaster, the base-rate forecaster
for the analog and ensemble forecasters, and the circular-shift base rate for the precursor forecaster.

**Runs.** The synthetic suite runs every Sunday at 03:00 UTC and at startup whenever a component's version differs from
the version last benchmarked. Each month the maintenance (see "Analysis cycles") samples 20 null region-weeks into
the corpus and runs the historical suite. Start one yourself with
`POST /v1/benchmarks/runs {"suite": "synthetic" | "historical" | "all", "components": [...], "seed": n}` (agent, 202);
only one run at a time (`BENCHMARK_IN_PROGRESS`). A full synthetic run takes from tens of seconds to a few minutes.

**Reading the report.** `GET /v1/benchmarks/runs/{id}` gives, per component, its version, status, headline metrics,
every threshold check (`metric`, `comparator`, the pass `threshold`, the `warn` threshold, `observed`, `status`) and
per-scenario results with the planted truth and notes. A metric that cannot be measured is a warning, not a pass.
`GET /v1/benchmarks/status` (read) shows the latest status of each component, whether its version changed since, and
what it gates. Notable metrics: `falseAlarmRatio` (null-data false alarms over the configured rate), `recall`,
`precision`, `delayMedianHours`, `correctUnknownShare`, `duplicateInflation` (must be 0), `linkingPrecision` and
`linkingRecall`, `spuriousMerges`, `falseValidationRate`, `lagRecoveredShare`, `rateRecoveredShare`, `brier`,
`logScore`, `reliabilityMaxDeviation` and `skill`. For event studies, `recall` and `placeboFalseSignalRate` measure the
live pre-event check exactly as it runs; the `engine*` metrics measure the same engine over all the history known at
the event.

**Thresholds** live in `intel/benchmark-thresholds.json` (for example: change-point false alarms at most 1.5 times the
configured rate, co-occurrence FDR at most q + 0.05, duplicate inflation 0, correct `unknown` under outages at least
95%, precursor false validation on `common-driver` at most 5%, step-change recall at least 80% for effects of 2 SD or
more, base-rate reliability within ±0.05 per populated bin). Override them with the setting `benchmark.thresholds`
(same shape, through the settings import): its `suite` values (seed, replicates, minimum bin size) replace the
defaults, a component it lists replaces that component's checks, and its `gating` lists replace the defaults'.

**Adding historical cases.** `POST /v1/benchmarks/cases` (agent; source `operator` for admin tokens) describes a known
past episode: a window, region codes and `expectations`:

- `findings`: `{findingType, present, seriesRef?, regionCodes?, from?, to?, minSeverity?}`. `change-point`
  expectations replay the change-point detector over the `seriesRef` (else the enabled analysis-series rows of the
  regions) with `asOf` stepping a day at a time from the window start, so later revisions are never seen; other
  finding types are listed as skipped until they can be replayed.
- `precursors`: `{precursorId, expected: validated | not-validated}`, compared with the precursor's lifecycle status.
- `questions`: binary questions with a known `outcome`; one that carries a `baseRate` definition is forecast by the
  base-rate calculator as of the window start and scored.

Sampled null cases (source `auto`) expect no change point in a region-week without any intel event at warning or above
and without situation activity. Delete a case with `DELETE /v1/benchmarks/cases/{id}`; past runs keep their copy.

**What gating does.** Autonomy follows the benchmark. 128's proposer may auto-execute only while every component it
depends on is `pass` or `warn`, and LLM forecasters carry weight in the ensemble only while the forecaster components
are `pass` or `warn` (a component never benchmarked counts as neither, so autonomy waits for the first run). On `fail`
they degrade: proposals stay pending for you, and LLM forecasts are left out of the ensemble (they are still recorded
and scored). Detectors keep running, but their new findings carry the caveat `BENCHMARK_FAIL` until a later run passes
or warns. The gate's dependency lists are the `gating` section of the thresholds file.

## Rolling hypotheses (prompt 125)

A **hypothesis set** frames one key question and its competing answers, and keeps the judgment current as evidence
arrives. Each answer carries a probability that moves only through recorded **revisions**, a separate analytic
confidence, and the evidence and assumptions behind it. A set can be linked to a forecast-ledger question; every
revision is then also a scored forecast. A set marked `private` (and everything in it) is visible only to callers with
private access.

**Key questions and competing hypotheses.** `POST /v1/hypotheses/sets` (agent) takes a `question` (at most 300
characters), a `kind`, a review `cadence` (`daily`, `weekly`, `monthly`), an optional `horizon` (the date the question
resolves by), `regionCodes`, `themes` and a `situationId`. An **exclusive** set lists answers that cannot all be true: its
priors sum to 1 and it has exactly one hypothesis of type `other` ("other / none of these"), which takes the mass the
named answers do not explain. An **independent** set judges each hypothesis on its own. A hypothesis has a short
`label`, a `statement` and a `type` (`outcome`, `explanation`, `speculative-link` or `other`). Add one later with
`POST /v1/hypotheses/sets/{id}/hypotheses`; an exclusive set is renormalized and the change is a revision.

**Priors from the outside view.** Every prior names its basis in `priorBasis`: a 121 base rate (`type: base-rate` with
the base-rate id), an analog set (`type: analogs` with its reference), or your judgment (`type: operator` with a
rationale). Start from `get_base_rate` and `find_analogs`, then adjust. When the base rate behind a prior later moves by
more than 20% relative (the daily refresh at 02:50 UTC checks), a `base-rate-refresh` revision shifts that hypothesis by
the change in the prior and keeps every evidence contribution.

**Indicators and LR classes.** An indicator is an observable signal with a **likelihood ratio** per hypothesis it bears
on: how much more often you would see it if the hypothesis were true than if it were not. Pick a class rather than a
number: `high` (5.0 when it supports, 0.2 when it weakens), `medium` (2.5 or 0.4), `low` (1.4 or 0.7) or `neutral`
(1.0). An explicit `value` (0.05 to 20) needs a `rationale`. Each indicator also has a 119 `signalFamily`, a credibility
and a relevance (H, M, L) and an optional `expectedBy`. Its `signal` says what to watch:

- `rule` (an inline trigger rule), `series` (a threshold on a `series:` value, a count or anomaly rule on a `rollup:`,
  or `changePoint: true` for 119 change-point findings on the reference), `intelEvent` (an event type, optionally in
  regions, a place or a box) and `situation` (a status or minimum severity of one situation) **watch for themselves**:
  Foresignal creates one trigger rule per indicator, tagged `indicator`, `origin-hypothesis` and `ind-{id}`. These
  rules notify no channel unless a channel names the rule in its `ruleIds`. Editing the indicator replaces the rule;
  once the indicator is observed, absent or retired the rule is disabled.
- `precursor` follows 120 `precursor-active` findings, and `question` follows the resolution of another ledger
  question.
- `manual` waits for you: `PATCH /v1/indicators/{id}` with `state: observed` (and `observedEvidence`) or `absent`.

An optional `emerging: {rule}` adds a first-stage rule whose alert marks the indicator `emerging` without moving the
probabilities. When the watch fires, the indicator becomes `observed`, the evidence is recorded, and the updater
revises the set (`indicator-observed`).

**Absence.** An indicator that has not appeared by `expectedBy` counts against the hypotheses it supports only if
Foresignal could have seen it. When the 116 detectability check (`canSeeAbsence`) holds for the indicator's family, area and window, it
becomes `absent` and the updater applies its absence LRs. By default an absence LR is the reciprocal of the observed LR
moved halfway toward 1 on the log scale, that is the square root of the reciprocal (a high supporting indicator's absence is 1/√5, about 0.45), or set your own in `absenceLr`. It
is then scaled by the detection probability, `1 + (absenceLr − 1) × pDetect`, so a barely observable indicator barely
moves the set. Without that coverage the indicator becomes `unknown-coverage` and nothing changes: not seeing something
you could not have seen is not evidence.

**How the updater moves the numbers** (`indicator-bayes` 1.0.0, deterministic and versioned):

- each driver adds `ln(LR)` to the log-odds of each hypothesis it bears on; an exclusive set is then renormalized;
- observations in one 116 evidence group are one piece of evidence: an echoed report never updates twice;
- within one signal family only the strongest |ln LR| per hypothesis per UTC day counts; the others are recorded as
  suppressed;
- **damping**: the evidence can move a hypothesis by at most 1.5 logits per UTC day, so a burst of reporting cannot
  swing the set from 10% to 90% in an afternoon;
- while the set is active, probabilities stay between 2% and 98%.

Confidence is recomputed with every revision by rule (`ConfidenceAssessor` 1.2.0). It is `high` only with two or more
credible (M or H) observed indicators from two or more families, sources graded A or B by 109, and no region of thin
reporting (`LOW_REPORTING_DENSITY`). It is `moderate` with at least one credible observed indicator, and `low`
otherwise. A broken linchpin assumption lowers it one step. Since prompt 133, supporting news evidence whose coverage is skewed
(`SKEWED_COVERAGE`) and has no non-news corroboration lowers it one step more. Since prompt 134, indicators observed through
products of one origin count once, and the basis reports circular support (`circularReporting`, `circularSupports`; see
[Chronicle network](#chronicle-network-prompt-134)). Confidence rates the evidence; it says nothing about which
answer is right.

**The ACH matrix is an audit trail.** `GET /v1/hypotheses/sets/{id}/matrix` lists the evidence against the hypotheses,
rated `CC`, `C`, `N`, `NA`, `I` or `II`. Observed indicators add a row automatically, rated from their LRs (4 or more
`CC`, 1.5 or more `C`, 0.25 or less `II`, 0.67 or less `I`, otherwise `N`; `NA` where the indicator does not bear on
the hypothesis). Add your own with `POST …/evidence`: a record, finding,
intel event or alert reference, or `manual` evidence you rate yourself. A re-report of the same evidence group attaches
to the existing row instead of adding weight. Since prompt 134 a report, received material (`remote:…`) or an evidence
item that cites them is grouped by the origin of its provenance chain, and a row whose support came back through other
analysts, or that a second support of the same origin attached to, carries `CIRCULAR_REPORTING`. Each hypothesis gets an inconsistency score, `−Σ weight × inconsistency`
(I counts 1, II counts 2, and the weight is credibility × relevance with L 0.5, M 1, H 2). The least inconsistent
hypothesis is shown, and rows rated the same for every hypothesis are flagged non-diagnostic: they cannot tell the
answers apart. The probabilities come from the updater and are scored in the ledger, not from the matrix. Manual
evidence rated `CC`, `C`, `I` or `II` for some hypothesis also records an `evidence-added` revision (with the LR of
the high or medium class those ratings stand for), and removing it reverses exactly what
it applied.

**Key assumptions.** List the assumptions the judgment rests on (`POST …/assumptions`): the text, whether it is a
**linchpin**, the rationale, what would make it wrong, its status (`solid`, `caveated`, `unsupported`, `broken`) and
the indicators that would test it. Marking an assumption `broken` emits `assumption.broken`. A broken linchpin flags the
set for review and lowers its confidence one step.

**Speculative links, and why they are capped.** A `speculative-link` hypothesis, such as "the pre-strike positioning
reflected advance knowledge", must name at least one 119 finding (`linkedFindingIds`), the coincidence baseline from the
event study's placebo distribution (`coincidenceBaseline.placeboRate` and its method), and at least two alternatives in
the same set (for example "scheduled news or inventory data", "contract roll or rebalancing", "coincidence at the base
rate"). Its probability can never exceed its **cap**: the prior updated by the evidence's own likelihood ratio,
`(1 − placeboRate) ÷ placeboRate` bounded to 1–20, and never above 98%. An unusual move that happens by chance one time
in five can support a link only so far, however many reports repeat the story. You can lift the cap in a review or a
hypothesis patch with `exceedCap: true`; the override is logged on the hypothesis (`capOverride`) and the revision is
marked `override`. Foresignal words these as associations, never accusations.

**Reading revisions.** `GET /v1/hypotheses/sets/{id}/revisions` lists the record, newest first. Each revision gives its
`cause` (`created`, `indicator-observed`, `indicator-absent`, `evidence-added`, `evidence-removed`, `base-rate-refresh`,
`review`, `operator`), the actor, `asOfSeq`, every hypothesis before and after, the drivers with their LRs, the log-odds
each one applied and why any was suppressed, damped or capped, citations, and the rationale. Automatic revisions explain
themselves in a deterministic text that names what moved and why, for example "'within 72 h' was unlikely (31%). It is
now roughly even chance (49%). Drivers: airspace restrictions observed (high diagnosticity)." All generated text passes
the estimative-language lint, and so must yours: a review rationale with a term that disagrees with its number, or a
likelihood term and a confidence level in one sentence, is refused (`LINT_FAILED`).

**Reviews.** `nextReviewAt` follows the cadence; enabled analysis cycles record the routine reviews of the sets in
their scope (see "Analysis cycles"), and you or an agent can record one at any time.
`POST /v1/hypotheses/sets/{id}/review` (agent) records your review with a rationale and may change LRs, assumption
statuses or probabilities directly, always as a revision with cause `review` (a routine reassessment, the default) or `operator` (a deliberate override
of the updater). A set past its horizon
without a ledger question is archived by its next review.

**Ledger link and resolution.** Give a set a `horizon` and a `ledgerQuestion` and Foresignal opens the forecast
question for you: with a `hypothesis-outcome` (or `manual`) resolver an exclusive set gets a multiple-choice question
over its hypotheses; a machine resolver such as `event-exists` gets a yes/no question, and you map the hypotheses that
mean "yes" with `ledgerOutcome: yes`. Every revision then records an `indicator-bayes` forecast on it, scored like any
other forecaster. An independent set gets a yes/no question whatever the resolver. When the question resolves, the hypotheses mapped to the outcome are `confirmed`, the others
`refuted`, the set `resolved` and its waiting indicators retired. Resolve a `hypothesis-outcome` question with
`POST /v1/forecasts/questions/{id}/resolve` and the outcome that happened.

**Information value and collection gaps.** `GET /v1/hypotheses/sets/{id}/information-value` ranks the pending
indicators by **expected information gain**: how much the answer would sharpen if each one appeared or failed to
appear, counting how likely Foresignal is to detect it. An indicator that would rank in the top three if it were fully
observable, but whose detection probability is below 0.5 or whose sources are down, is a **collection gap**
(`GET /v1/collection-gaps`, refreshed hourly). Each gap lists recommendations: enable or repair a cataloged source, add
a local receiver or sensor (prompts 069, 078, 086), or "no cataloged source observes this". Recommendations never
change your configuration.

**Drafting with the LLM.** `POST /v1/hypotheses/sets/draft {text, situationId?, regionCodes?}` (agent) asks the configured chat model to draft a set from
plain language ("watch whether the Red Sea situation spreads to Hormuz"). It must look up base rates and analogs before
it proposes priors. The draft comes back with warnings (structure, rule specs, the lint, the number of indicators); it
is never created until you post it to `POST /v1/hypotheses/sets`. A draft spends LLM budget and always needs a token.
Its tools read under the chat provider's egress rules, so private data reaches only a provider allowed to receive it.

**Notifications and the stream.** `hypothesis.band_changed` tells you when a hypothesis moves into a different ICD 203
term. Sets with a daily or weekly cadence send it to the inbox, and to channels whose `events` list it, at most once
per set every 6 hours (a later change waits and the latest state is sent); monthly sets send none. `indicator.observed`
and `assumption.broken` reach only channels whose `events` list them (`PUT /v1/channels/{id}`). The
SSE topic `hypotheses` carries `set.created`, `set.revised`, `set.band_changed`, `set.resolved`, `indicator.observed`,
`indicator.absent` and `assumption.broken`; private sets reach only callers with private access. Public sets are also
published as `foresignal-intel` records of kind `hypothesis-set`, so ordinary trigger rules can watch them.

## Outlooks and indices (prompt 126)

Foresignal makes three kinds of statistical forecast of its own, and holds each one to account: **series forecasts**
(quantiles of a series 1, 7 and 28 days ahead), **regional outlooks** (event counts per region for the next six
4-week windows) and **regional indices** (transparent 0–100 composites whose weights are fitted to outcomes). Every
series forecast on a configured series and every outlook window is a forecast-ledger question (prompt 122), forecast
by the `statistical` forecaster and scored automatically when it resolves. No LLM is involved.

### Series forecasts and method selection

A series gets forecasts when its analysis-series row (prompt 119) lists the `forecast` detector. Automatic rows get it
by default for region-day rollups with at least 365 days of history, for the 117 market and attention series
(`cftc-cot`, `fred-series`, `wikimedia-pageviews`, `gdelt-timelines`, and any series of a market family) and for the
PortWatch chokepoints; add or remove it with `PUT /v1/analytics/series` (`"detectors": [..., "forecast"]`). Daily,
weekly and hourly series are forecast (hourly ones as days).

**Methods** (`series-forecast` 1.0.0):

- `seasonal-naive`: the last value of the same weekday (the benchmark every other method must beat);
- `ets-ann`: simple exponential smoothing, ETS(A,N,N);
- `ets-aaa`: exponential smoothing with additive trend and weekly season, ETS(A,A,A);
- `nb-glm` (counts only): a negative-binomial regression on trend, weekday, two annual cycles and the count a week
  earlier, with a parametric bootstrap for parameter uncertainty;
- `damped-trend` (continuous series only): ETS(A,Ad,N), a trend that flattens out.

The ETS methods fit their parameters by maximum likelihood; counts are modeled on `log1p(x)` and the quantiles
transformed back. **The method is chosen per series by a rolling-origin backtest**: at each of the last 8 weekly
origins every applicable method is fitted only on what was known by that origin and forecasts 1, 7 and 28 days ahead;
the method with the lowest mean pinball loss at 7 days wins. The choice is redone monthly. See it, with every method's
scores, at `GET /v1/analytics/series/forecast/model?ref=` (409 `FORECAST_MODEL_NOT_READY` until the first selection).

**Forecasts** run nightly at 03:30 UTC (after the 03:00 change-point run): quantiles p05, p10, p25, p50, p75, p90 and
p95 for each horizon, read at `GET /v1/analytics/series/forecast?ref=&horizon=`. Each run stores its method, the seq it
read up to and a 116 manifest.

**Ledger questions.** The series in the app setting `analysis.forecasts.questionSeries` (an array of series
references; by default the PortWatch Bab el-Mandeb, Hormuz and Suez transits and COT WTI managed-money net) get one
numeric `series-value` question per horizon and target day, category `markets` or `activity`. The `statistical`
forecast is recorded on it at once, and the 122 `climatology` and `persistence` baselines forecast it automatically,
so the scorecard compares Foresignal's model with both. The setting is an app setting without its own endpoint (like
119's `analysis.series.max`); its value is a JSON array such as
`["series:imf-portwatch-chokepoints/chokepoint4", "series:cftc-cot/cot.067651.disaggregated.mm_net"]`. A listed series
needs the `forecast` detector too.

**Exceedances.** When a day's actual value falls outside the 1-day p05–p95 band, Foresignal records a
`forecast-exceedance` finding (`notable`; `significant` outside p01–p99 of the same distribution). A count that falls
below the band where the sources could not have seen a fall is recorded as `info` with `LOW_DETECTABILITY` instead.

**Licensed series.** A series of a `LOCAL_ONLY` source (FRED, for example) is forecast like any other; a caller that the
egress policy refuses raw values to gets the quantiles in standardized units (`units: "standardised"`, standard
deviations from the mean of the last year) and the model without its fitted parameters.

### Reading quantiles and pinball loss

A quantile forecast is a range, not a point. `p50` is the median: half the outcomes should fall below it. `p05`–`p95`
is a 90% interval: about one outcome in ten should land outside it, so an occasional exceedance is expected and a
calibrated forecaster has them about 10% of the time. Narrow intervals that are rarely missed are the goal.

The **pinball loss** of a quantile `q` at level `τ` for an outcome `y` is `τ·(y − q)` when the outcome is above it and
`(1 − τ)·(q − y)` when it is below. Averaged over the seven levels it rewards both a good median and honest spread, in
the series' own units (lower is better). Twice the mean pinball loss is the CRPS the ledger reports. Compare methods on
the same series only.

### Regional outlooks (CAST-style)

Every Monday at 06:00 UTC Foresignal publishes, for each eligible region and **outlook family**, quantiles of the event
count in each of the next six 4-week windows, the first starting the following Monday, in the style of ACLED's CAST.
The families are in `intel/outlook-families.json`:

| Family | Counts from |
|---|---|
| `conflict:battles`, `conflict:explosions-remote`, `conflict:violence-civilians`, `conflict:protests` | ACLED where imported, UCDP otherwise (prompt 121 reference history) |
| `outage` | Foresignal's canonical outage events (IODA and Cloudflare Radar) |
| `weather-severe` | Foresignal's canonical weather-alert events (NWS and CAP warnings) |
| `seismic:m4.5` | USGS ComCat, then Foresignal's canonical seismic events |

Each region uses one source for its whole history, never a mix. **Regions** are countries with at least 52 weeks of
history and an event in the last two years, and admin-1 regions with at least 52 weeks of 5 or more events.

**The model** (`regional-outlook` 1.0.0) is a negative-binomial regression of weekly counts on the counts 1, 2 and 4
weeks earlier, a 13-week average, an annual cycle, the neighboring regions' counts (adjacency from the 040 boundaries)
and, when an enabled index targets the family, the region's index value. Each region's baseline level is pooled toward
the mean of its continent, so a region with a short or noisy history borrows strength from similar ones. Regions with
too little history fall back to the mean and spread of the last 52 weeks and say so (`SMALL_SAMPLE`). The model is
refitted monthly, and each fit is evaluated on the last 26 weeks, refitting at every origin, against the seasonal-naive
window (the same four weeks a year earlier) and climatology.

**Each window is a ledger question**: a numeric `count-threshold` question over the same reference family and region,
forecast by `statistical` (and by `climatology`), resolved from reference history 7 days after the window ends once the
source covers every week (annulled `COVERAGE_GAP` after another 14 days without). So the accuracy is measured by the
same ledger as every other forecaster, and **travels with the outlook**: `skill` gives the number of resolved windows,
the mean pinball loss and CRPS, the skill against climatology on the same questions, the p10–p90 coverage of resolved
windows, and the skill against seasonal naive from the monthly evaluation. Below 20 resolved windows the label is
`insufficient`; quote the numbers only with that label.

Read them at `GET /v1/outlooks?family=&region=` (the latest outlook per region and family) and
`GET /v1/outlooks/{region}/{family}` (the six windows, past windows with their actual counts, and the skill). A window
that resolves above its p95, or below its p05 where detectability is adequate, becomes an `outlook-exceedance` finding.

**Reporting density.** Event data rest on reporting, and media coverage differs by orders of magnitude between regions.
An outlook for a country in the bottom decile of reporting density (prompt 121) carries `lowReportingDensity: true` and
the caveat `LOW_REPORTING_DENSITY`: its counts, and so its quantiles, may understate what happens. An outlook built on
Foresignal's own sources where their detection probability is below `absenceMinDetect` carries `LOW_DETECTABILITY`, and
a low count there is never read as a quiet period. An outlook whose reference history ends weeks before the first
window (UCDP is published yearly) says so (`LAGGED_PUBLICATION`), and its quantiles are wider.

### Regional indices

A regional index is a **transparent composite**, not a probability: each **component** is a series template with
`{region}` (for example `rollup:day/*/conflict-event/{region}`), a window (`P7D`, `P28D` or `P90D`), a transform and a
direction. Every day at 03:45 UTC Foresignal sums each component over its window per region, normalizes it into
0–100 against the pooled history of the region's continent over the past year (`pctrank`, the percentile, by default;
`zscore`, `log1p` and `level` rescale instead), reverses it when the direction is −1, and adds the components with
their weights. **Floors** then raise the value where a rule holds, for example "a UCDP war (1,000 or more battle deaths)
in the last 12 months keeps the index at 70 or more". Every value comes with its decomposition: each component's raw
window value, normalized value, weight and contribution (the contributions sum to the value before floors), the floors
applied, and the change over 24 hours and 7 days.

Three built-ins are seeded **disabled**: `instability` (conflict events, internet outages, travel-advisory activity,
sanctions activity; target `conflict:battles`), `maritime-risk` (loitering, vessel encounters, maritime warnings, GNSS
interference; target change points in maritime activity) and `infrastructure-stress` (internet and power outages,
grid-frequency excursions, NWS warnings; target `outage`). Enable one, or define your own, with
`PUT /v1/indices/{id}` (agent); `ready` says whether its component series exist yet. The first computation backfills
two years of Mondays so the index can be fitted. A move of 5 points or more emits `index.changed`.

**Why the weights are fitted, and only proposed.** Hand-set weights encode someone's guess about what matters.
`POST /v1/indices/{id}/fit` (agent; a 103 job, also run by the monthly maintenance) asks the data instead: a
regularized negative-binomial regression of the next four weeks' target count on the components, across all regions,
with the penalty chosen by rolling-origin validation and the last part of the history held out. The result reports
the **out-of-sample skill** of the index's implied count forecast against the seasonal-naive outlook (pinball loss and
log score), each component's coefficient and significance, and the **proposed** weights with a `weightsVersion`
(`index.weights_proposed` on the stream). Nothing changes until you, or an agent you allow, apply them:

```json
POST /v1/indices/instability/weights
{"source": "fitted", "version": 2, "rationale": "Beat the seasonal-naive outlook out of sample."}
```

`source: operator` applies your own weights and `source: default` restores the definition's; every change is logged on
the definition and bumps `weightsVersion`. Set `autoApplyFittedWeights: true` on an index to apply fitted weights
automatically. Every index response includes `weightsOrigin` (`default`, `fitted` or `operator`) and the latest
out-of-sample `skill`; an index whose fit cannot beat seasonal naive tells you so.

### Records, the stream and MCP

Outlook windows and index values are also records of the DERIVED source `foresignal-outlook`: kind `regional-outlook`
(`externalId` `{region}:{family}:{windowStart}`, carrying the quantiles, the ledger question, the skill and coverage,
and the flags) and kind `regional-index` (`externalId` `{indexId}:{region}`, re-emitted when the value moves by a point
or more). Ordinary trigger rules can watch them, for example an alert when an outlook's p50 for battles in Yemen
exceeds a threshold. The `analytics` SSE topic carries `outlook.published`, `index.changed` and
`index.weights_proposed`. MCP offers `get_series_forecast`, `get_outlook` and `get_index` (read) and `fit_index`
(agent).

## Impact projection (prompt 127)

The impact projector turns the hazard forecasts Foresignal already ingests into probabilistic impact statements that are
scored like any other forecast: "Tropical-storm-force winds: 40% chance in Texas; given that, an internet outage is
reported in the same admin1 region within 48 h in 55% of comparable cases, so a 22% chance of an outage report by
Thursday 18:00Z." Each statement is a `projected-impact` finding with its inputs, exposed assets and dependency paths,
and, when it is likely enough, a ledger question that Foresignal resolves from its own sources. **Every projection is
labeled "modelled; heuristic links may be wrong; not an official warning".** Official warnings come from the agencies;
the projector never replaces them. No LLM is involved.

### What the numbers mean

`P(impact) = P(hazard) × P(impact | hazard class)`.

- **`pHazard`** is the producer's published probability that the hazard occurs at an exposed asset or somewhere in the
  admin1 region: the highest published band that intersects the region or contains an exposed asset. The band's lower
  bound is used, and its upper bound is the top of the interval.
- **`pConditional`** is how often the impact family was reported in the same admin1 region after the hazard class, in
  Foresignal's own history of the last 1825 days, with a 95% Jeffreys interval and `n`, the number of hazard
  occurrences (admin1 region × UTC day). Fewer than 20 occurrences is `insufficient`: the pair is skipped for that
  region class and listed in the projection's `skipped`. An unknown rate gives no projection, never a guess.
- **`pImpact`** is the product. Its `interval` is the product of the interval bounds, so it carries both the producer's
  band and the history's sampling uncertainty.

Region classes are the continent classes of base rates (121): an admin1 region belongs to its country's continent
(`continent:NA` for `US-TX`). Rates are computed on demand and cached for a day.

### Which products carry real probabilities

| Hazard type | Source | Probability used |
|---|---|---|
| `tropical-wind-34`, `-50`, `-64` | `nhc-wind-probs` (new, below) | NHC's published cumulative wind-speed probability bands (5–10% … >90%) over the 120 hours after the advisory |
| `tropical-track` | `nhc-cyclone-cones` | **none**: context and timing only |
| `tornado`, `wind`, `hail` | `spc-outlooks`, days 1–2 | SPC's published point probability: `torn` 0.10 means a 10% chance of a tornado within 25 miles of a point |
| `severe-categorical` | `spc-outlooks` | **none**: categorical risk is context only |
| `excessive-rain` | `wpc-ero-d1` to `-d3` | WPC's published range per category (marginal ≥ 5%, slight ≥ 15%, moderate ≥ 40%, high ≥ 70%): the lower bound, with the range stored |
| `river-flood` | `nwps-gauges` | the gauge's forecast category (else its current category, under persistence), calibrated by the gauge's own history: the share of past days at that basis that verified at the flood-relevant category (moderate) within the horizon; fewer than 10 such days gives no probability |
| `storm-cell` | `wx-storm-tracks` | **none** today: the 105 tracker publishes no position error |

**Why a cone is not a probability.** NHC sizes the cone so that the storm's center stays inside it about two thirds of
the time, using historical track errors. It says nothing about winds away from the center, about intensity or about
impacts, and it is the same width for every storm in a season. The wind-speed probabilities are the product that
answers "how likely are damaging winds here"; the cone appears only as context (the storm, its advisory and its timing)
on tropical wind projections. In the same way an SPC "moderate risk" or a gauge's deterministic forecast category is a
label, not a probability, unless the producer publishes the mapping (WPC does) or Foresignal calibrates it from history
(gauges).

**New source `nhc-wind-probs`** (enabled by default, like every free source). It reads the layers "Probabilistic Winds 34
kts", "50 kts" and "64 kts" of NOAA's `NHC_tropical_weather_summary` MapServer (ids 30, 31 and 32 on 2026-10-17,
resolved by name each day) every 15 minutes (at most every 5) with the ArcGIS kit: one `wind-probability` record per published band, ids
such as `34kt:40-50`, valid 120 hours from the advisory, retention 1825 days. Keep it enabled alongside `nhc-cyclone-cones`
during hurricane season.

### Exposure and dependencies

For each probabilistic hazard the projector finds the admin1 regions its bands touch (040 reference regions: load the
reference data first, or nothing is projected), the 104 infrastructure facilities inside the area (published polygons
count within `impact.assetBufferKm`, 5 km, of a facility) with their dependency paths two hops downstream and each
path's weakest-edge label (`confirmed`, `likely`, `possible`, `speculative`), your 036 places that intersect it, and
the 101 exposure summary (facility counts, places and population) of the hazard's part of each region. Without an
infrastructure build, or for an area too large to traverse, facilities are left out with the caveat
`INFRASTRUCTURE_NOT_EVALUATED`; the regional projection still stands.

### When it runs and what it writes

The projector follows the record stream with the durable cursor `impact.cursor` (in retention's consumer clamp). A new
version of a hazard record schedules its hazard 10 minutes later, so the bands of one advisory are projected once.
Each run:

- keeps one projection per impact family and admin1 region, drops those below `impact.minProbability` (0.05) and keeps
  the 20 most likely per hazard;
- writes one `projected-impact` finding per (hazard, impact family, admin1 region) with `stats` `{pHazard,
  pConditional, pImpact, interval, n}`, the exposed assets, the dependency paths, the inputs (the hazard band, the rate,
  the pair, the exposure summary and, for tropical winds, the cones), the skipped pairs and the label, plus a 116
  manifest. Severity is `significant` at 50% or more, `notable` at 20% or more, else `info`;
- supersedes the hazard's earlier projections: the next advisory's projection for the same family and region replaces
  the old one (`supersededBy`), and a projection the next run no longer produces is superseded without a successor;
- announces `impact.projected` and `impact.superseded` on the `analytics` topic.

The built-in pairs live in `intel/impact-pairs.json`:

| Hazard class (one occurrence: a matching record in the region on a UTC day) | Impact family | Lag |
|---|---|---|
| `tropical-wind-34` (34-kt band ≥ 50%) | `outage`, `ground-stop` | 48 h |
| `tropical-wind-50`, `tropical-wind-64` (band ≥ 50%) | `outage` | 48 h |
| `tornado` (SPC ≥ 10%) | `tornado-warning`, `outage` | 12 h |
| `wind` (SPC ≥ 15%) | `outage` | 12 h |
| `hail` (SPC ≥ 15%) | `ground-stop` | 12 h |
| `excessive-rain` (WPC moderate or high) | `river-flood` | 72 h |
| `river-flood` (a gauge at moderate or major) | `outage` | 48 h |

Impact families: `outage` (IODA region-level outages, 068; Cloudflare Radar outages name countries, not regions, so
they cannot resolve a regional question), `ground-stop` (FAA NAS status, 087), `tornado-warning` (NWS alerts, 047) and
`river-flood` (NWPS gauges at moderate or major flood, 079). The hazard classes of tropical winds, SPC outlooks and
WPC are forecast thresholds, so their conditional rate is the rate after a likely forecast, not after observed winds:
the product then tends to understate, never overstate, the chance of an impact. Rates for SPC and WPC need history
those sources keep only briefly by default (14 and 90 days); raise their retention if you want those pairs to reach
20 occurrences.

### How projections are scored

A projection at or above `impact.questionMinProbability` (0.10) opens a binary ledger question (122), category
`weather-impact`: "Will an internet outage be reported in Texas (US-TX) between {validFrom} and {validTo + lag}?". Its
`event-exists` resolver selects the alerts of the impact family's trigger rule in that region. The rules ("Impact
family: …", tagged `impact` and `origin-impact`) are created on first use; their alerts reach no notification channel
unless a channel names the rule, but they stay in the inbox and on the streams. The question is answered `yes` as soon
as the rule fires in the region inside the window, `no` after the window only if the rule's sources were healthy and
could have seen a report (otherwise it is annulled, never scored), so a quiet sensor never counts as "no impact". The
forecaster `impact` records `pImpact`, the baselines add theirs, and the ledger scores them all: the impact
forecaster's Brier score and calibration on `GET /v1/forecasts/scorecard` tell you how far to trust the projections.
One question is opened per advisory, impact family and region; the next advisory opens its own.

### Reading them

- `GET /v1/analytics/impacts` (read): the current projections, highest probability first within each hazard; filters
  `hazardType`, `region` (a country or admin1 code), `since`, `minProbability`.
- `GET /v1/analytics/impacts/{findingId}` (read): one projection with the hazard's bands, the inputs, the exposed
  assets, the dependency paths, the skipped pairs, the label and the ledger question; superseded ones too.
  404 `UNKNOWN_IMPACT`.
- `GET /v1/analytics/impact-rates?hazardClass&impactFamily&regionClass` (read): the conditional rates with intervals,
  `n` and `status`.
- MCP: `list_projected_impacts` and `get_impact_rate` (read).

Settings: `impact.minProbability` (0.05), `impact.questionMinProbability` (0.10) and `impact.assetBufferKm` (5).

## Analysis cycles (prompt 128)

Analysis cycles give Foresignal a rhythm. A cycle runs on a schedule (daily, weekly or monthly), looks at what
changed in its scope since its previous run, and publishes an **assessment**: a structured report that builds on the
previous assessment of the same cycle and says what moved and why. Without an LLM the assessment is built from the
deterministic packet alone (`digest-only`); with one, four budgeted roles add key judgments, a red-team challenge,
scored forecasts and proposals.

### Enabling the defaults

The first start creates three cycles, all **disabled**, in the timezone of the `server` setting (UTC when unset):

| Cycle | Cadence | Schedule | Notification priority |
|---|---|---|---|
| Daily brief | daily | 06:00 every day | normal |
| Weekly assessment | weekly | Monday 07:00 | high |
| Monthly outlook | monthly | the 1st, 08:00 | high |

`GET /v1/analysis/cycles` lists them. To turn one on, replace it with `PUT /v1/analysis/cycles/{id}` (agent) with
`"enabled": true` and the rest of its definition; the next run time (`nextRunAt`) is recomputed from now. A cycle you
create with `POST /v1/analysis/cycles` (agent) takes:

- `name`, `cadence` (`daily`, `weekly`, `monthly`) and `schedule` (`{cron, timezone}`: a 5-field cron and an IANA
  timezone, the same rules as briefings);
- `scope`: `regionCodes` (ISO 3166-1 alpha-2 or ISO 3166-2), `themes`, `situationIds`, `hypothesisSetIds` (a list, or
  `"cadence-match"` for every set whose review cadence matches the cycle's), `sourceIds` and question `categories`.
  An empty scope covers everything. A filter applies where an object has a value for it (a finding without regions
  passes the region filter);
- `analystProfileId`: the 102 analyst profile that supplies the model, the effort cap, the per-loop limits, the
  instructions, `allowActions` and the owner. Without a profile the roles use the assigned chat provider with the 102
  default limits, and the proposer does not run (proposals belong to a profile);
- `roles` (any of `analyst`, `red-team`, `forecaster`, `proposer`; empty for a digest-only cycle),
  `forecasterProviders` (provider ids; default the profile's provider), `samples` (forecaster samples per question,
  1–7, default 3), `maxQuestions` (default 15 daily, 40 weekly, 60 monthly) and `maxTokensPerCycle` (default 300,000
  daily, 1.2 million weekly, 2 million monthly);
- `channelIds` (empty: every channel subscribed to `assessment.published`) and `notifyMinPriority`.

Scheduling follows the briefing and analyst pattern: a 30-second poll claims due cycles, at most one run per cycle and
two in total run at a time, and a run that was missed while the server was down runs once (trigger `missed`), never
once per missed slot. `POST /v1/analysis/cycles/{id}/run` (agent, always authenticated, 202) runs a cycle now; it is
refused with 409 `CYCLE_RUN_IN_PROGRESS` while the cycle runs, and with 409 `LLM_NOT_CONFIGURED` when the cycle has
roles and no chat provider is configured (a cycle without roles never needs one; a scheduled run without a provider
publishes a digest-only assessment instead). `GET /v1/analysis/cycles/{id}/runs` and `GET /v1/analysis/runs/{runId}`
(agent) show each run with its packet, the outcome and token use of every role, and its warnings. Deleting a cycle
keeps its runs and assessments.

### What a run does

1. **The period.** From the end of the previous successful run of the same cycle; the first run looks back over the
   cadence's natural window (1 day, 7 days or 30 days).
2. **The packet** (`cycle-packet` 1.0.0), always and without an LLM: situations that were new, activated, escalated,
   went dormant or closed (with before and after); hypothesis revisions with their drivers, band changes, indicators
   observed, absent or of unknown coverage, assumptions broken or questioned, and sets due for review; findings with
   their base rates and caveats; ledger questions opened, due within the horizon (7 days daily, 30 weekly, 90 monthly)
   and resolved with every forecaster's score, and scorecard changes; regional index moves of 5 points or more and
   outlook windows that resolved outside their p10–p90 range; failing and degraded sources and regions of low
   reporting density; and the 125 collection gaps. Each section is ranked. The stored packet is complete; the LLM sees
   the 30 best-ranked items per section and is told how many were cut (`truncatedForLlm`). Private items reach the
   model only when the provider's egress allows private data.
3. **Routine reviews.** Every in-scope set due for review whose cadence the cycle covers (a daily cycle reviews daily
   sets, a weekly one daily and weekly sets, a monthly one all) gets a review revision that changes no probability and
   moves its next review date; a set past its horizon without a ledger question is archived by it.
4. **The LLM roles**, in order, each a tool loop over the read catalog with the packet as untrusted input:
   - **Analyst**: 3 to 7 key judgments, each cited. A judgment with a probability must name the ledger question or
     hypothesis set it is about; the ICD 203 term is derived from the number, and confidence is a separate sentence.
   - **Red team**: for up to three active sets (those that moved most, then those linked to the most severe
     situations), the strongest case for the runner-up hypothesis, disconfirming evidence it searched for, the
     assumptions most likely to be wrong and a premortem ("assume the leading hypothesis proved wrong; why?"). Where a
     set has a ledger question, its probability is recorded as forecaster `llm-redteam:{profile}`.
   - **Forecaster**: the open questions in scope with the highest value of information (the linked situation's stakes,
     critical 3, warning 2, otherwise 1, times the ensemble's uncertainty `4·p·(1−p)`), up to `maxQuestions`. For each
     question and sample it first takes the outside view (resolution criteria quoted verbatim, `get_base_rate`,
     `find_analogs`, the reference class and its rate), then the inside view (cited adjustments). The samples are
     combined by the median of their log-odds and recorded as forecaster `llm:{profile}:{provider}`, one forecaster per
     configured provider.
   - **Proposer**: hypothesis sets for situations without one, indicators, watches, ledger questions and situation
     merges, as 102 proposals. They stay pending unless the profile allows actions, the type is in its
     `allowedActions`, and the benchmark gate (123) passes.
5. **The assessment**, always (see below), then delivery: the SSE topic `assessments`, the notification event
   `assessment.published` (in the inbox; to channels when the cadence's priority, `normal` daily and `high` weekly and
   monthly, reaches the cycle's `notifyMinPriority`), and the embedding index for semantic search.
6. **Monthly maintenance** after a monthly cycle's assessment (see below).

A role that hits the cycle's token budget, the provider's 026 budget or a refusal is recorded `skipped` with the
reason, a role that errors is `failed`, and the assessment is still written. The run's status, and the assessment's,
is `ok` when every configured role succeeded, `partial` when some did, `digest-only` when none did (or none were
configured) and `failed` when the run itself failed (it then emits `cycle.run_failed`).

### Choosing models, effort and cost

The effort is `medium` for daily cycles and `high` for weekly and monthly ones, capped by the profile's `effort` when
that is lower. The system prompts are the versioned templates `cycle-analyst-system`, `cycle-red-team-system`,
`cycle-forecaster-system` and `cycle-proposer-system` (`GET /v1/prompt-templates/{id}`), sent unchanged with the tools
in a fixed order so the provider can cache them.

**Estimating cost.** Each run records its tokens per role (`inputTokens`, `outputTokens`, `cachedTokens`) and in
total (`tokensUsed`). The forecaster dominates: it makes two tool loops per question and sample, for each provider, so
its cost grows as `maxQuestions × samples × providers`. A first estimate is the run's `tokensUsed` multiplied by your
provider's price per token (cached input is cheaper with providers that discount it); `maxTokensPerCycle` is the hard
ceiling across all roles, and the provider's 026 daily budget applies on top. Start with the analyst and red team, add
the forecaster with one provider, and raise `samples` or add providers only once the ledger shows the forecasts earn
their place.

### Reading an assessment

`GET /v1/assessments/latest?cadence=weekly`, `GET /v1/assessments/{id}` (`format=md` or `format=html` for the rendered
page, JSON by default) and `GET /v1/assessments?cadence&since` (read). A cycle assessment has these sections:

1. **Bottom line up front**: the key judgments, or, without an LLM, the period's top deterministic changes.
2. **What changed** since the previous assessment of this cycle (linked): judgments added, dropped or re-estimated,
   hypothesis probabilities that moved (with band changes) and situations that appeared, closed or changed.
3. **Situations**: status, severity and what happened to each this period.
4. **Hypotheses**: every set with each hypothesis's probability, term, change since the last assessment and top
   drivers, followed by the red team's notes.
5. **Indicators to watch**: pending indicators expected in the next period, plus this run's proposals.
6. **Forecasts**: questions opened, due and resolved (with scores); weekly and monthly add the 90-day scorecard per
   forecaster, operator next to machine.
7. **Speculation**, kept apart: speculative-link hypotheses with their alternatives and coincidence baselines, and any
   judgment that lost its citations.
8. **Sources, coverage and caveats**: failing and degraded sources, low-density regions, finding caveats, what the
   LLM did not see, and the outcome of every role.

Since prompt 133 a deterministic **Coverage** section (id `news-coverage`) follows Forecasts when a situation in scope
has news coverage, and the assessment's top-level `coverage` lists each situation's coverage statistics, the stories
with a blindspot and the coverage-divergence findings (see "News coverage and media bias").

"What we cannot see" lists the collection gaps, with the analyst's comment when there is one. Every text says where it
came from (`textOrigin`: `deterministic`, `llm` or `mixed`), every probability is a number with its derived term, and
the footer reads: "Automated analysis. Probabilities use ICD 203 terms; evidence is cited; speculation is labeled.
Verify before acting." `lintWarnings` lists the wording problems found and how they were handled: failing text is
regenerated once, then repaired (terms derived from the numbers, confidence moved out of the sentence) or dropped.
Each assessment names its model, effort, prompt templates and manifest, so a client can show "view prompt".
`GET /v1/assessments/{id}/bundle` exports the cited findings, base rates, questions with their forecasts and scores,
forecasts and hypothesis sets with their recent revisions, with provenance and manifests; `format=geoparquet` returns
a ZIP of GeoParquet files. `POST /v1/assessments/{id}/feedback` records `useful`, `not_useful` or `wrong`.

**On-demand situation assessments.** `POST /v1/situations/{id}/assessment` (agent, always authenticated, 202) runs the
same machinery focused on one situation (its members' findings, linked hypothesis sets and questions, coverage), by
default with the analyst and red team (`{"roles": [...], "effort": "low" | "medium" | "high"}`). The assessment has
the sections bottom line, key insights, what changed since the previous assessment of the situation, outlook, risks,
speculation and sources; `GET /v1/situations/{id}/assessments` lists them.

### How the ledger keeps the LLM honest

LLM forecasts are just forecasts. They are written to the 122 ledger under their own forecaster ids, clamped to
[0.03, 0.97], Platt-recalibrated once they have a record, scored on every resolved question exactly like the baselines,
and weighted in the ensemble by their earned skill (and only while the forecaster benchmarks pass). They never change
a hypothesis probability or another forecaster's number. Each forecast stores its rationale as typed claims and its
inputs: the samples, the model, the effort and a prompt hash. The scorecard and `GET /v1/forecasts/compare` show
whether the model beats the base rate and you; with the provider's `knowledgeCutoff` set, the monthly maintenance
also replays the LLM forecasters over the past month's resolved questions (questions opened before the cutoff are
skipped as `LEAKAGE_RISK`; a retrospective forecast is made without tools, because the read tools cannot be wound back
to a past date).

### Monthly maintenance

The monthly cycle, after its assessment, runs these steps in order, recording each one's job id, result or error on a
maintenance run; a failing step never stops the later ones:

1. observability re-estimation (116);
2. null-case sampling and the historical benchmark suite (123);
3. the base-rate refresh (121);
4. the precursor re-test (120);
5. the Platt refits, relative-Brier weights and extremizing gate of the ensemble refit (122);
6. an ensemble refresh with the new weights (122);
7. the extremizing check (122);
8. index weight fits, as proposals, and regional outlook refits (126);
9. series-method re-selection (126);
10. archival proposals: active hypothesis sets past their horizon are flagged for review, and their next review
    archives them;
11. with the profile's provider `knowledgeCutoff` set, the retrospective evaluation of every `llm` forecaster over the
    past month (skipped otherwise).

Because the defaults are disabled, the maintenance also runs on the first of each month at 03:30 UTC whenever no
monthly cycle is enabled, so base rates, precursors, ensemble weights, index weights and series methods stay current.
`POST /v1/analysis/maintenance/run` (agent, always authenticated, 202) runs it now; one maintenance runs at a time
(`MAINTENANCE_IN_PROGRESS`). The cycle run's `maintenance` field shows the steps.

### The intel board

`GET /v1/intel/board` (read; at most 8 KB; ETag and `Cache-Control: max-age=60`) is the small summary for widgets and
dashboards: the top five situations by severity and recency, hypothesis band changes and observed indicators of the
last 24 hours, questions due in the next 7 days with the ensemble probability and its term, the latest assessment of
each cadence with its headline, the ensemble's 90-day Brier skill and calibration next to yours (once you have 20
resolved forecasts), and the latest benchmark status per component. Private objects appear only for callers allowed
private data; the board carries no raw series values. When the content would exceed 8 KB it shortens texts first,
then drops the oldest observed indicators, band changes, due questions and lower-ranked situations, and lists what it
cut in `truncated`.

### Records, the stream and MCP

The SSE topic `assessments` carries `assessment.published` and `cycle.run_failed` (cursor key `m`; a reconnecting
client replays what it missed). MCP: `get_assessment`, `list_assessments` and `get_intel_board` (read, also in the
analyst's and the cycles' read catalog) and `run_cycle` (agent, cost-bearing).

## Objects, workspaces and validation (prompt 129)

Prompt 129 gives every analytic object one address, links objects into a graph, lets analysts and agents record
verdicts on what Foresignal produced, and lets several clients (a globe, a map, an analyst console, an agent) share one
working context.

### Object references

Every object has a reference of the form `{type}:{id}`: `record:{sourceId}/{externalId}`, `situation:{id}`,
`finding:{id}`, `intel-event:{id}`, `canonical-event:{id}`, `entity:{id}`, `precursor:{id}`, `base-rate:{id}`,
`hypothesis-set:{id}`, `hypothesis:{id}`, `indicator:{id}`, `question:{id}`, `forecast:{id}`, `assessment:{id}`,
`alert:{id}`, `investigation:{id}`, `place:{id}`, `view:{id}`, `watchlist:{id}`, `source:{id}`,
`sensor:{kind}:{id}`, `workspace:{id}` and `annotation:{id}`, and since prompt 130 `principal:{id or handle}`,
`group:{id}`, `tag:{id}`, `attachment:{id}`, `evidence-item:{id}` and `saved-search:{id}`. `GET /v1/contract` (read)
lists the types, the relation types, the confidence labels and the client kinds with the contract version (`1.5`
since prompt 136 added `narrative:{id}` (its id is the narrative's canonical event id) and `claim:{id}`; `1.4` since
prompt 133 added `news-story:{id}`, whose id is the story's canonical event id, and `outlet:{id}`; `1.3` added
`report:{id}` and `remote:{instanceId}/{type}:{id}` in prompt 132; `1.2` added `chronicle:{id}` in prompt 131).

In a URL path the reference is one segment, so its `/` is written `%2F`
(`GET /v1/objects/record:usgs-quakes%2Fus7000abcd`). Foresignal's Tomcat connector passes encoded slashes through for
this; a reverse proxy in front of it must forward them still encoded. An invalid reference answers 400 `OBJECT_REF_INVALID`, an unknown or
invisible one 404 `UNKNOWN_OBJECT`.

### Envelopes, dossiers and context summaries

`GET /v1/objects/{ref}` (read) returns the object's **envelope**: type, title, time, geometry (simplified to at most
500 vertices), severity or status, `private`, the provenance it came from and the **actions** the caller may take on
it (open, annotate, share, add to a workspace, and so on, each with the method and path). `?asOf=` reads the object as
it was: records from their version history (`record:{source}/{id}@{seq}` names one version), situations from their
snapshots; types without history answer only for their current state. `POST /v1/objects/resolve` turns up to 500
references into envelopes in one call; unknown ones come back in `missing`.

`GET /v1/objects/{ref}/dossier` (read; at most 64 KB; ETag and 304) gathers what a client needs to open an object in
one call: an overview, current imagery (raster frames and satellite scenes covering it), official forecasts (NHC),
projected impacts (127), related objects, a timeline, evidence and analogs. `include=` picks sections. Imagery uses the
current scene and frame heads only, and official forecasts cover NHC only.

`POST /v1/context/summary` (read; at most 32 KB; cached 30 seconds) summarizes an area and time range for a side panel:
record counts per layer (current state), the latest activity (findings, intel events, alerts and situation changes;
`asOf` bounds the activity only), up to 15 insights and up to 10 related objects. It writes no narrative; the
`narrative` field is null. `GET /v1/workspaces/{id}/summary` does the same for a workspace's context (409
`WORKSPACE_CONTEXT_INCOMPLETE` when the context lacks a viewport or a time range).

### Relations and the projector

Relations are typed, dated edges between references: `member-of`, `observed-by`, `same-as`, `located-in`, `near`,
`depends-on`, `affects`, `derived-from`, `evidence-for`, `evidence-against`, `about`, `mentions`, `precedes`, `causes`
and `related-to`, each with a confidence label (`confirmed`, `likely`, `possible`, `speculative`) and a validity
interval.

Most relations are **projected** from what Foresignal already knows, by the `relation-projector` (1.0.0):

- incrementally from the record stream for record-backed owners (situations, intel events, findings and the other
  `foresignal-intel` and `foresignal-analytics` records), with the durable cursor `relation.cursor`, which retention's
  consumer clamp respects like every other cursor;
- nightly at 03:40 UTC (UTC, `foresignal.relations.reconcile-cron`) for the rest: entity links, infrastructure
  dependency edges (owner `infra:graph`), canonical events, questions and investigations. A first reconcile also runs
  when no projected relation exists yet.

`foresignal.relations.auto-start` (default: the same as `foresignal.analytics.scheduled`) turns the projector off for
tests or a replica. `located-in` is computed on demand from your 036 places, not stored. `precedes` and `affects` are
not projected, and neither is `causes`.

Reading the graph: `GET /v1/objects/{ref}/related` (read; `types`, `direction`, `asOf`, `minConfidence`, `limit`),
`POST /v1/graph` (read; up to 50 seed references, at most 2,000 edges) and `POST /v1/graph/common-dependencies` (read;
up to 20 references; the infrastructure the objects share, through the 104 facilities that directly affect them).
Operators and agents add their own edges with `POST /v1/relations` (agent, 201) and retire them with
`DELETE /v1/relations/{id}` (204); projected edges cannot be retired by hand (409 `RELATION_NOT_EDITABLE`). A
`causes` edge is labeled at most `possible` unless an admin token asserts it: Foresignal never claims causation by
itself.

### Annotations, verdicts and precision

`POST /v1/objects/{ref}/annotations` (read token for a note, agent for a verdict) attaches a `note` (a body of at most
10,000 characters and up to 20 tags) or a `verdict`: `confirmed`, `rejected`, `needs-review` or `duplicate-of` (with
`duplicateOfRef`). `GET` lists them (`thread` for replies), `PATCH /v1/annotations/{id}` (JSON merge patch) edits one
and `DELETE` removes it; 404 `UNKNOWN_ANNOTATION`. A note tagged `propose-watch` is how a client suggests a watch; it is
an annotation, not a 102 proposal. A private annotation is visible only to callers with private access. Prompt 130 makes
notes comment threads with replies, `@mentions` and resolving (see
[People, sharing and TLP](#people-sharing-and-tlp-prompt-130)).

Verdicts do three things:

- **Precision.** `GET /v1/analytics/precision?component=&since=` (read) gives, per producing component (detectors,
  the situation linker, the impact projector and so on), the share of labeled objects confirmed, with a 95% Wilson
  interval. Only the latest live verdict per object counts, and `needs-review` is not a label. Read the interval: five
  confirmations out of six is not the same evidence as fifty out of sixty.
- **Status, only when asked.** A `rejected` verdict dismisses a finding or an intel event only when its agent-scope
  author says so (`"applyStatus": true`); otherwise the object stays as it was and the verdict is recorded beside it.
- **Benchmark cases.** A verdict on an object with a time window proposes a historical benchmark case (123), stored
  as a pending proposal for an operator; nothing is added to the corpus by itself. The 128 cycle packet lists the
  period's verdicts.

### Workspaces

A workspace is a shared context several clients follow: `POST /v1/workspaces` (any authenticated token) creates one,
`GET /v1/workspaces` lists those you can see, and `GET`, `PATCH` (merge patch) and `DELETE /v1/workspaces/{id}` read,
change and remove one (404 `UNKNOWN_WORKSPACE`, 403 `WORKSPACE_FORBIDDEN`). Its **context** holds `selectedRefs`,
`focusRef`, `timeRange`, `asOf`, `playback`, `viewport` (a bbox, or a center and radius), `activeSituationId`,
`activeHypothesisSetId`, `filters` and `layers`; its **layout** (`PUT …/layout`, at most 64 KB) is free-form client
state.

Roles: the owner, and members (at most 200) as `editor` or `viewer`; since prompt 130 the access list adds principal,
group and instance grants, and ownership moves through it. Owners and editors change the context and layout;
the owner, or an editor with an agent token, changes the name, members, saved views and the private flag; the owner or
an admin deletes it; an admin may read any workspace. A private workspace is visible only to callers with private
access.

**Keeping clients in step.** `PATCH /v1/workspaces/{id}/context` takes a JSON merge patch with `If-Match` set to the
context version you hold; a stale version answers 412 `WORKSPACE_CONTEXT_STALE` with the current `contextVersion`, so
the client re-reads and re-applies. Send `Foresignal-Client-Id` so other clients can tell your changes from theirs. The
SSE topic `workspace` of `/v1/events/stream` (cursor key `w`; filter `workspaceId`, optional `clientId` and
`clientKind`; always authenticated) carries `workspace.context` on every change and `workspace.presence` when the set
of connected clients changes (checked every 2 seconds).

**Deep links and sharing.** `GET /v1/workspaces/{id}/context-link` and `POST /v1/context-links` encode a context as a
`#fs=` fragment (at most 2,048 bytes) a client can open without a server round trip. `POST /v1/workspaces/{id}/share`
(agent) creates an expiring share link to a read-only snapshot of the workspace, with the same rules as other share links.
A share link is public, so the workspace's TLP label must be `CLEAR` first (`PUT /v1/objects/workspace:{id}/acl` with
`"tlp": "CLEAR"`); otherwise it answers `403 TLP_VIOLATION`.

### Sensor discovery

`GET /v1/sensors?bbox=` (or `placeId=`) with `from`, `to` (at most 7 days) and `kinds` answers "what could observe this
area?": enabled sources whose 116 detection probability there is above 0.2, local receivers whose last position is
within 400 km, stations within 2,500 km, curated Earth-observation satellites with predicted passes at 10° elevation or
more (from current CelesTrak elements), STAC scenes and raster products already in Foresignal. Every answer says
"Foresignal cannot task public satellites or third-party sensors": it tells you what may already be looking, not what
you can order.

The satellite list is `intel/satellite-capabilities.json` (39 satellites: operator, sensor type, nominal resolution and
swath, and the NORAD catalog number, checked against CelesTrak on 2026-10-17; geostationary satellites are left out).
Point `foresignal.sensors.capabilities-file` at a JSON file of the same shape to add satellites or replace bundled
entries with the same NORAD number. The figures are nominal, not per-pass.

### MCP

`get_object`, `get_related` and `get_workspace_context` (read; `get_object` and `get_related` are also in the
analyst's read catalog), `annotate` (read for notes, agent for verdicts) and `set_workspace_focus` (agent).

## People, sharing and TLP (prompt 130)

Foresignal can serve several named people and agents on one instance without becoming a user directory. Everyone
works through a **principal** that holds scoped tokens; there are no passwords. Records and analytic objects stay
readable per scope as before; **work products** (investigations, workspaces, saved searches, notes, attachments,
evidence items, shared tags, chronicles (prompt 131) and reports (prompt 132)) get an owner, an access list and
a TLP label.

### Principals and their tokens

| Call | What it does |
|---|---|
| `GET /v1/me` | The calling principal in full, the token used, the scopes it may call, its groups, its notification preferences and the instance's default TLP |
| `GET /v1/principals` | Handles, display names, kinds and avatars of every principal (`q`, `kind`); any token |
| `POST /v1/principals` (admin) | Create a `person`, `agent` or `service`: `handle` (`^[a-z0-9][a-z0-9_.-]{1,31}$`, unique, `409 PRINCIPAL_HANDLE_TAKEN`), `displayName`, and optionally `email`, `avatarMediaId`, `timezone` and `organization` |
| `PATCH /v1/principals/{id}` (admin, merge patch) | Change the profile; `"disabled": true` stops every token of the principal at once (the bootstrap admin token always works). The handle never changes |
| `POST /v1/principals/{id}/tokens` (admin) | Issue a token for that principal (an `ApiTokenInput`; the scope is your choice); shown once |

```json
POST /v1/principals
{"handle": "jsmith", "displayName": "Jo Smith", "kind": "person", "email": "jo@example.org",
 "timezone": "Europe/London", "organization": "Example Analysis Ltd"}

POST /v1/principals/jsmith/tokens
{"name": "jsmith-laptop", "scope": "read"}
```

Pairing codes (`POST /v1/pairing-codes` with `principalId`) give a person's phone its own token under their principal.
The bootstrap admin token belongs to the principal `owner`; every token created before prompt 130 already has a
principal (023). `email` is used only for that principal's own email notifications and is shown only to admins and the
principal itself. **`organization` matters**: it draws the TLP `AMBER` boundaries below. Principals without an
organization count as one organization, so a single-organization instance needs none.

### Groups

`POST /v1/groups` (agent) creates a group; its creator becomes a `manager`. `POST /v1/groups/{id}/members`
(`{"members": [{"principalId": "jsmith", "role": "member"}]}`) and `DELETE /v1/groups/{id}/members?principalId=` add and
remove members; managers and admins change and delete groups (deleting one removes its grants). Any token reads
`GET /v1/groups` (`member=` filters by principal) and `GET /v1/groups/{id}` with its members.

### What is shareable and who sees it

A work product has a `visibility`: `private` (its owner and admins only), `shared` (the owner and the access list) or
`instance` (also every principal as a viewer). Its **access list** grants `viewer`, `commenter`, `editor` or `owner` to
`principal:{id}`, `group:{id}` or `instance`, optionally until `expiresAt`. Viewers read, commenters also comment,
editors also edit and share, owners also delete and transfer ownership. Admin tokens see everything.

| Work product | Default when created |
|---|---|
| Investigation | `instance` (investigations were instance-wide before prompt 130); pass `"visibility": "private"` to keep one to yourself |
| Workspace | its owner and its 129 members (`private` without members, `shared` with them) |
| Saved search, attachment, evidence item | `private` |
| Tag | personal tags are visible only to their owner; shared tags default to `instance` |
| Note (annotation) | visible to whoever can see the object it is on |

Without access a work product is **invisible**: every API, envelope, listing, search, saved search, SSE topic and export
answers as if it did not exist (`404`), so ids do not leak. Workspace members keep their roles; investigations, saved
searches and the rest follow the access list alone.

- `GET /v1/objects/{ref}/acl` (owner or editor): owner, visibility, TLP and grants.
- `PUT /v1/objects/{ref}/acl` (agent; owner or editor): replace the grants, the visibility and the TLP label; only
  owners grant `owner` or transfer ownership (`"owner": "jsmith"`).
- `POST /v1/objects/{ref}/share` (agent; owner or editor): add grants and notify the people reached (`share.received`),
  with an optional `message` and `expiresAt`. Sharing a private object makes it `shared`.
- `GET /v1/shared-with-me` and `GET /v1/my` (`type`, `cursor`) list what reached you and what you own;
  `GET /v1/investigations?scope=mine|shared|all&status=` does the same for investigations. Display ids
  (`INV-2026-0042`) work in every investigation path and object reference.

Every share and access-list change is written to the audit log (`GET /v1/audit`, target type `object_acl`, with the
grants, visibility, TLP and any ownership transfer); principal and group changes are audited too.

### TLP rules

Every work product carries a Traffic Light Protocol 2.0 label, `tlp`: `CLEAR`, `GREEN`, `AMBER` (the default,
`foresignal.collab.default-tlp`), `AMBER+STRICT` or `RED`. The label bounds who may receive the object outside its
owner; `EgressPolicy` checks every grant, visibility and share link against it and refuses with `403 TLP_VIOLATION`,
naming the rule (`rule`):

| Label | Principals named in the access list | Groups and `instance` | Public share links | Federated peers | Named recipients on other instances |
|---|---|---|---|---|---|
| `RED` | yes | no | no | no | yes |
| `AMBER+STRICT` | yes | only when everyone reached is in the owner's organization | no | no | only of the owner's organization |
| `AMBER` | yes | only when everyone reached is in the owner's organization | no | no | yes, with their organization |
| `GREEN` | yes | yes | no | yes | yes |
| `CLEAR` | yes | yes | yes | yes | yes |

- **Derived products inherit the strictest label of their inputs**: an evidence item with a `RED` attachment is `RED`,
  and asking for a weaker label is refused; an investigation that receives a `RED` item becomes `RED` (its grants must
  allow it); a comment on a work product carries the product's label.
- **Renditions print the label**: investigation exports start with `TLP:<label>` (and send `Foresignal-TLP`), personal
  notifications start with `[TLP:<label>]`, and envelopes carry `tlp`.
- A workspace share link (112) needs `CLEAR`.

### Tags, comments and mentions

- **Tags** (`/v1/tags`): `POST /v1/tags {"name": "red-sea", "namespace": "personal" | "shared"}`; assign one to up to
  500 references at once with `POST /v1/tags/{id}/assign {"refs": [...]}` (and `/unassign`); references you cannot see
  are skipped and listed. `GET /v1/tags/{id}/objects` and `GET /v1/objects/{ref}/tags` read them, and every envelope
  carries the names of the caller's visible tags.
- **Comments**: a note is a thread; `POST /v1/annotations/{id}/replies {"body": "…"}` replies,
  `POST /v1/annotations/{id}/resolve {"resolved": true}` resolves (or reopens), and
  `GET /v1/objects/{ref}/annotations?thread=true` lists threads. On a work product commenting needs the `commenter`
  role. `@handle` notifies that principal only if it can see the object; otherwise the mention is dropped and returned
  to the author in `droppedMentions`. A reply notifies the thread's earlier participants.

### Uploads and their limits

`POST /v1/attachments` (multipart `file`, optional `tlp` and `visibility`; any token) stores a document or image:

- **Allowed types**, decided from the bytes, never from the client: PNG and JPEG (checked by the 043 ImageGuard), PDF,
  and UTF-8 plain text, CSV, JSON, GeoJSON, KML and Markdown. Anything else is `415 UPLOAD_TYPE_NOT_ALLOWED`.
- **Size**: at most `foresignal.collab.max-upload-mb` (50) MB, `413 UPLOAD_TOO_LARGE` above; keep it under
  `spring.servlet.multipart.max-file-size` (200 MB), which Tomcat enforces first.
- **Images lose their EXIF, XMP and IPTC metadata** (including GPS positions) unless `foresignal.collab.keep-exif` is
  true; pixels are untouched.
- **Text** is extracted for search (PDF through Apache PDFBox, KML with its markup stripped), at most 1 MB.
- **Storage** is content-addressed under `${FORESIGNAL_DATA_DIR}/attachments/{ab}/{sha256}.{ext}`; identical uploads
  share one file, which goes when the last attachment using it is deleted. Include that directory in your backups.
- **Serving**: `GET /v1/attachments/{id}/content` sends the stored safe content type with
  `Content-Disposition: attachment` (inline only for images), `X-Content-Type-Options: nosniff` and a sandboxing
  Content-Security-Policy. Files are data, never code: nothing uploaded is executed or rendered as active content.
- An attachment cited by an evidence item cannot be deleted (`409 ATTACHMENT_IN_USE`).

### Evidence items and saved searches

- **Evidence items** (`/v1/evidence-items`) turn documents, images and links into graded evidence: a title,
  description, attachments, a link (stored as metadata, never fetched), the Admiralty grade (source reliability `A`–`F`
  and information credibility `1`–`6`, default `F6`), an optional cited cataloged source, cited objects, geometry,
  event time and tags. They can be investigation items, ACH evidence (`"source": "evidence-item"`) and relation
  endpoints. For 116 independence an evidence item's originator is `operator:{handle}` unless it cites a cataloged
  source. Only `CLEAR` and `GREEN` items are embedded for semantic search (the text would otherwise reach an embedding
  provider); the others match by text.
- **Saved searches** (`/v1/saved-searches`) store a query (text, object types, filters, area, time window, tags);
  `GET /v1/saved-searches/{id}/results` runs it over the envelopes the caller can see. With `"notify": true` new
  matches reach the owner's inbox at most hourly (`saved-search.matches`; checked every 15 minutes,
  `foresignal.collab.saved-search-scheduled`).

### Notifications for people

`GET /v1/inbox` is the caller's personal inbox (mentions, shares, replies, saved-search matches), newest first;
listing an item marks it read. The `me` topic on `/v1/events/stream` streams the same items
(`share.received`, `mention`, `comment.reply`, `saved-search.matches`; since prompt 132 the report and package events and
since prompt 134 the chronicle network events) and resumes by id. To reach a person elsewhere,
they set `PUT /v1/me/notification-preferences`:

```json
{"items": [{"eventTypes": ["mention", "share", "comment-reply"],
            "channel": {"type": "ntfy", "config": {"topic": "jsmith-foresignal"}},
            "quietHours": {"start": "22:00", "end": "07:00"}}]}
```

Each preference owns a personal 018 channel (validated like any channel, including the outbound URL policy) delivered
by the same worker, with the principal's timezone and the preference's quiet hours. An `email` channel without `to`
goes to the principal's own email. Personal channels never receive the instance's alerts, briefings or system events,
and personal notifications never appear in the device inbox or on the `notifications` topic.

### MCP

`whoami`, `list_shared_with_me`, `tag_objects` and `comment` (read) and `share_object` (agent). `annotate` follows the
same comment rules, and `create_investigation` and `add_to_investigation` respect ownership and the editor role.

## Chronicles (prompt 131)

A **chronicle** is a named monitoring thread that records its own history: "Middle East air and sea, from today". It
has **layers** (what to capture), a chronological **thread** (what happened, with an unread count per person) and a
**replay** of exactly what it captured. Global retention never removes a chronicle's copy: only the chronicle's own
`retention` or its deletion does. A chronicle is a 130 work product: it has an owner, an access list and a TLP label,
and without a role it answers `404 UNKNOWN_CHRONICLE`. Each chronicle owns a 129 workspace for the client's layout;
access to the chronicle gives the same access to the workspace.

### Creating one

`POST /v1/chronicles` (any authenticated token) takes `name`, `description`, `visibility` (default `private`), `tlp`
(default the instance's), `area` (a place or a geometry; it bounds records, saved views, watchlists, findings, raster
products and alerts, and applies to entities, situations and series only where a layer sets its own), `retention`
(`forever`, the default, or an ISO-8601 duration of at least `PT1H`), `sizeCapBytes`, links to situations,
hypothesis sets, investigations and attention topics, `fromWorkspaceId` (take the area and layers from a 129
workspace) and up to 100 `layers`. A new chronicle inherits the strictest TLP of its linked investigations and of the
workspace it was made from. It starts `proposed`; nothing is captured until `POST /v1/chronicles/{id}/activate`.
`POST /v1/chronicles/suggest` drafts a chronicle (layers, capture modes and its storage estimate) from a `situationId`,
or from an `area` and a `topic`, deterministically and without an LLM; it creates nothing. Display ids (`CHR-2026-0007`) work in every chronicle path.

A layer has a `selector` and a `capture` mode. Selectors: `records` (sources, kinds and payload conditions),
`saved-view`, `watchlist`, `entities`, `situations`, `findings`, `series`, `raster-products` and `alerts`, and since
prompt 138 `external-stream` (one stream of an EDP provider, see
[Binding external streams](#binding-external-streams-prompt-138)). Add, change
and remove layers with `POST`, `PATCH` and `DELETE /v1/chronicles/{id}/layers[/{layerId}]` (editor). A layer applies
from the seq at which it was added or changed; it never rewrites what was already captured, and every edit writes a
`layer-edit` thread item.

### Capture modes

| Mode | What is kept |
|---|---|
| `full` | every matching version |
| `downsampled` | one version per entity per `interval` (default 60 s), plus every event version |
| `events-only` | versions persisted as FIRST, SIGNIFICANT, REAPPEAR, TOMBSTONE or TRIGGER_EDGE |
| `latest-per-interval` | the latest version per entity per `interval` bucket |
| `rollup-only` | counts per region and hour, no records |
| `frames` | every `everyNthFrame`-th raster frame, by reference; the first frame is always kept |

Intervals run from 1 second to 31 days. Choose `events-only` or `downsampled` for busy TRACK sources (aircraft,
vessels): they keep the story (first sightings, significant changes, reappearances, edges) at a fraction of the rows.

**How capture works.** An active chronicle's capture loop wakes on the change bus (polling every `poll-interval`, 2 s)
and always reads `record_version` by seq, in chunks of `chunk-seqs` (10,000), so a burst is never lost. Its position
is the chronicle's own cursor, `chronicle.cursor.{id}`, which joins retention's consumer clamp like every durable
consumer: global retention waits for an active chronicle until it has captured, and a stopped or paused chronicle
holds nothing back. A chronicle that falls more than two windows behind is released like any lagging consumer; what
retention removed before capture becomes a `capture-gap` thread item, so a gap is shown, never silently skipped.
Versions are copied into the chronicle's own partition of `chronicle_version`; frames are pinned so raster retention
keeps them while the chronicle needs them.

**Backfill.** `POST /v1/chronicles/{id}/activate {"backfill": {"from": "2026-10-01T00:00:00Z"}}` also copies the
matching history that global retention still holds, with each layer's capture mode, marked `backfilled`. A
`backfill` thread item reports the counts and the caveats: what retention had already removed, and ranges that
overlap downsampled track history.

### Storage estimates and the size cap

`GET /v1/chronicles/{id}/estimate` projects each layer's rows and bytes per day from the last 24 hours of global
history (alerts layers from the last 7 days of their rules' alerts), applying the capture mode's share. The area and
payload conditions are not applied, so the figure is an upper bound and says so in its basis. Read it before
activating a chronicle with busy TRACK layers in `full` mode.

Every chronicle has a size cap: `sizeCapBytes`, else the app setting `chronicle.defaultCapGb` (20 GB). Bytes are
counted per captured row. At 90% the chronicle's `capState` becomes `warning`, a `cap-warning` thread item is written
and the owner gets `chronicle.cap_warning` in the inbox. At 100% it becomes `exceeded`, a `cap-fallback` item is
written, and `full` and `downsampled` layers fall back to `events-only` (shown as each layer's `effectiveCapture`)
until you raise the cap or remove layers. Capture never stops silently.

### Stop, archive or delete

| Action | What happens | Reversible |
|---|---|---|
| `POST …/pause` / `…/resume` | capture stops and restarts; the paused span is a visible gap | yes |
| `POST …/stop` | capture ends at the current watermark (`captureToSeq`); the history stays readable and replayable | no (create a new chronicle to continue) |
| `POST …/archive` (202) | a stopped chronicle is written to a cold package under `${FORESIGNAL_DATA_DIR}/archive/chronicles/` (`manifest.json` and gzip JSON Lines of versions, series and rollups), verified, and its database partition dropped; the thread stays | yes: `POST …/restore` (202) reloads every row and returns it to `stopped` |
| `DELETE /v1/chronicles/{id}?confirm={displayId}` | drops the captured history, the thread, the layers, the workspace and the access list (owner only) | no |

Any other transition answers `409 CHRONICLE_STATE_INVALID`; deleting without the display id answers
`409 CHRONICLE_CONFIRMATION_REQUIRED`. Every transition writes a `status-change` thread item. The chronicle's own
retention (hourly, `retention-scheduled`) removes a stopped chronicle's captured history once `retention` has passed
since it stopped, and records `historyExpiredAt`; the thread and the definition stay. The archive package is JSON
Lines rather than GeoParquet; use `POST …/export` when you want GeoParquet.

### The thread

`GET /v1/chronicles/{id}/thread` (`types`, `after`, `cursor`, `limit`) lists, oldest first, alerts, findings, situation
changes, hypothesis band changes, forecast resolutions, first appearances (capped at `first-appearances-per-hour`, 20,
per layer per hour), layer edits, capture gaps, cap warnings and fallbacks, status changes and backfills, each with
its object envelope. `unreadCount` is the caller's own; `POST …/thread/read` marks items read. The SSE topic
`chronicle` of `/v1/events/stream` carries `chronicle.item` and `chronicle.status` under the same access rules.
Private data is captured, but only callers with private access see it in the thread and the replay.

### Replay

- `GET …/state?at=` : per layer, the latest captured version of each entity at that moment, with caveats (capture
  gaps, archived history, the cap fallback);
- `GET …/tiles/{z}/{x}/{y}.mvt?at=` : the same state as vector tiles;
- `GET …/frames` : the pinned raster frames; `GET …/tracks/{sourceId}/{externalId}` : one entity's captured track;
- `GET …/series` : the captured series points (sampled every `series-sample-interval`, 15 min, for computed series);
- `GET …/summary` : layer counts over time, top entities, links, storage and gaps.

A replay shows what the chronicle **captured**, not everything Foresignal knew: a `downsampled` layer replays the
kept versions only.

### Proposals

Foresignal proposes a chronicle when a situation turns `active` with severity warning or above, or when a hypothesis
set opens on a theme no set of the last 30 days had; the 128 proposer can also propose one (`propose_chronicle`). A
proposal creates a chronicle in status `proposed` (no capture), owned by the analyst profile's owner (else the
instance owner), who gets `chronicle.proposed` in the inbox; accept it with `POST …/activate`. When the profile's
`allowedActions` include `propose_chronicle` and the benchmark gate passes, the cycles may activate proposals on their
own: at most `chronicle.autoActivatePerDay` (2) per UTC day, each with its cap lowered to
`chronicle.autoActivateCapGb` (5 GB). Beyond that the chronicle stays proposed for a person to accept. A situation is
never proposed twice.

### Sharing, export and MCP

Share a chronicle like any work product (`POST /v1/objects/chronicle:{id}/share`); a viewer reads its thread, replay
and workspace and may export, but not change it. `POST /v1/chronicles/{id}/export` (`format`: `geoparquet`, `gpkg` or
`json-bundle`; optional `layers`, `from`, `to`) queues a 111 export job of the captured history (202, with
`Foresignal-TLP`); sources the caller may not read are left out and `LOCAL_ONLY` data never leaves. MCP:
`list_chronicles`, `get_chronicle_thread` and `get_chronicle_state` (read), and `create_chronicle` and
`add_chronicle_layer` (agent). Whether other analysts may learn that a chronicle exists is its owner's discoverability
setting (see [Chronicle network](#chronicle-network-prompt-134)).

**Settings.** App settings `chronicle.defaultCapGb` (20), `chronicle.autoActivatePerDay` (2) and
`chronicle.autoActivateCapGb` (5); deployment properties under `foresignal.chronicles`: `auto-start`, `poll-interval`
(2 s), `chunk-seqs` (10,000), `first-appearances-per-hour` (20), `series-sample-interval` (15 min),
`retention-scheduled` and `async-jobs`.

## Reports and exchange (prompt 132)

A **report** is a finished intelligence product: a titled, TLP-labeled document of ordered **blocks**, drafted from
what Foresignal already holds, edited by people, published as a frozen revision and sent to principals on this
instance or, as a signed **package**, to analysts on other instances. A report is a 130 work product: it has an
owner, an access list and a TLP label, and without a role it answers `404 UNKNOWN_REPORT`. Display ids
(`RPT-2026-0012`) work in every report path. MCP: `list_reports`, `get_report`, `draft_report` and `send_report`.

### Drafting

`POST /v1/reports/draft` (any authenticated token) builds a draft deterministically from a `source` and a
`template`; `POST /v1/reports` creates one from your own blocks.

| Template | Typical source | What the draft holds |
|---|---|---|
| `situation-brief` | `situation` | bottom line, members, map, timeline, linked hypotheses and forecasts, coverage |
| `investigation-summary` | `investigation` | the items, evidence, timeline and coverage |
| `chronicle-recap` | `chronicle` | the thread over the time window, capture gaps and cap fallbacks as caveats |
| `weekly-assessment` | `assessmentId` (a 128 assessment) | the assessment's key judgments, sections and citations |
| `custom` | `selection`, `tag` or `workspace` | the objects, a map and a timeline |

Block types: `markdown`, `bluf`, `objects`, `map`, `timeline`, `hypotheses`, `forecasts`, `evidence`, `coverage`,
`attachments` and `sources`. Every block says where its text came from (`author`, `deterministic` or `llm`), and a
probability in a judgment carries its ICD 203 term. The `coverage` block lists caveats (`CAPTURE_GAP` from a
chronicle, `UNRESOLVED_REFERENCES` for objects that no longer resolve) and, since prompt 133, the news coverage
statistics of the situations in scope with their caveats (`COVERAGE_BLINDSPOT`, `SKEWED_COVERAGE`,
`MOSTLY_UNRATED_OUTLETS`): `NO_NEWS_COVERAGE` when none of those situations has news, and `MEDIA_COVERAGE_UNAVAILABLE`
only when the news layer is absent (see [News coverage and media bias](#news-coverage-and-media-bias-prompt-133)). `"narrative": true` adds a labeled LLM narrative block, whose citations are verified against the
report's objects; it needs an agent token (`403 INSUFFICIENT_SCOPE` otherwise) and a chat provider
(`409 LLM_NOT_CONFIGURED`). Edit with `PATCH /v1/reports/{id}` (merge patch) and `POST`, `PATCH` and
`DELETE /v1/reports/{id}/blocks[/{blockId}]`; send the version you hold in `If-Match` (or `version`), and a stale one
answers `409 REPORT_VERSION_CONFLICT`. Comments go on a block as notes on `report:{id}#{blockId}`.

### Publishing and retracting

`POST /v1/reports/{id}/publish` freezes a revision: the object references are pinned to the versions read (records as
`@seq`), the map is rendered to an image (or a caveat when rendering is unavailable), the `sources` block is
regenerated from the cited objects, and a 116 manifest is stored with the bundle's SHA-256.
`GET /v1/reports/{id}/rendition?format=md|html|json` serves the latest revision (a draft is a labeled preview); the
HTML prints the TLP label at the top and the bottom and the footer "Automated analysis inputs; verify before acting".
`GET /v1/reports/{id}/revisions` lists the revisions. `POST /v1/reports/{id}/retract` with a `reason` withdraws the
report: its key links stop working and every recipient gets `report.retracted`.

### Sending on this instance

`POST /v1/reports/{id}/send` (agent) takes `principals`, `groups`, optional `channels` and a `message`. Each recipient
is checked against the TLP rules of 130, given viewer access, and told with `report.received` in the inbox, on the
`me` topic and on their own notification preferences (category `report`). A published `CLEAR` report can also get a
public read-only link (`POST /v1/reports/{id}/share-link`, agent), served as HTML at `GET /v1/k/{key}/report`;
`GREEN` reports get one only with the app setting `exchange.allowGreenLinks` set to `true`, and `AMBER`, `AMBER+STRICT`
and `RED` never do (`403 TLP_VIOLATION`). A report made stricter, returned to draft or retracted stops being served
through its links.

### Exchange packages

`POST /v1/exchange/packages` (agent) builds a ZIP package of the object named by `objectRef`: a report, an
investigation, an evidence item, a chronicle (a `chronicle-extract`, with `extract: {from, to, layers}`), or a received
`remote:` reference to forward. `recipients` is `"*"` or a
list of `{instanceId, principals}` (`"*"` among principals for anyone there); the 130 TLP table's last column decides
what each label allows, with a peer's `sameOperator` standing for the owner's organization. The package carries
`manifest.json` (type, title, sender, authors, TLP, recipients, content hashes, message and provenance chain; since
prompt 134 a report's chain starts with the chains of the reports, received material and evidence items it cites), a
detached Ed25519 JWS over it signed with the 109 instance key, and the content with its renditions. Data the egress
policy keeps at home (private sources, `LOCAL_ONLY` licenses) is reduced to counts. A package larger than
`exchange.maxPackageMb` (default 200) is refused with `413 PACKAGE_TOO_LARGE`. With `"deliver": "peer"` it is posted to
each recipient instance that is a configured peer (`"file"` only keeps it); `GET /v1/exchange/packages/{id}/download` gives the ZIP to hand
over by other means, and `GET /v1/exchange/sent` lists what you sent with the receipts that came back.

**Setting up a peer for exchange.** Each side creates a 113 peer for the other and rotates its ingest key
(`POST /v1/peers/{id}/ingest-key`), then gives that `fsp_` key to the other operator. Each stores the key it received
as the peer's `exchangeKey` (`PUT /v1/peers/{id}` with `"exchangeKey": "fsp_…"`; stored encrypted, never returned,
`exchangeKeySet` shows it is there). Deliveries go to `/v1/exchange/inbox` and receipts to `/v1/exchange/receipts`
with that key.

### Receiving

A delivered or imported package is verified before anything is stored: the signature against the sender's keys (a
JWKS pinned at import, then a sender key stored earlier, then the peer's pinned key, then the peer's JWKS), every
content hash, and the recipients (`422 PACKAGE_RECIPIENT_UNKNOWN` when a named principal does not exist here).
A failing signature is `400 PACKAGE_SIGNATURE_INVALID`; a package already imported is `409 PACKAGE_ALREADY_IMPORTED`; a
package whose own product already passed through this instance (its origin, a forwarder or an importer of the same
product in the provenance chain) is `409 PACKAGE_LOOP`. Since prompt 134 the hops of material a new product only cites
do not count, so a report written elsewhere about this instance's work can arrive; used as support, it is marked
`CIRCULAR_REPORTING` (see [Chronicle network](#chronicle-network-prompt-134)). An admin imports a package from a
file with `POST /v1/exchange/import` (multipart `file`, and `jwks` with the sender's published JWKS to pin its keys on
first contact).

`GET /v1/exchange/received` lists what reached you (named recipients, or everyone for `"*"`; admins see every package
except `RED` ones not addressed to them). `GET /v1/exchange/received/{id}/rendition` renders it here from the
package's content, sanitized and always marked with its origin; the sender's own HTML is never served.
`POST /v1/exchange/received/{id}/acknowledge` records your acknowledgement and sends a signed receipt back. Received
objects are addressed as `remote:{instanceId}/{type}:{id}`: they are read-only, can be added to an investigation
(audited as a copy of a received item), and can be forwarded only within their TLP. Recipients get
`package.received`.

**Settings.** App settings `exchange.maxPackageMb` (200) and `exchange.allowGreenLinks` (`false`). Sent and received
packages are stored under `${FORESIGNAL_DATA_DIR}/exchange/`; include it in your backups.

## News coverage and media bias (prompt 133)

Foresignal collects news article **metadata** (never an article body, a byline or an image), groups the articles that
report one event into a **story**, and counts how that story was covered: by how many independent evidence groups,
from outlets of which lean, factuality, country, type and ownership. That shows where coverage is lopsided, where only
one side of the press reports something, and where a hypothesis rests on reporting from one corner. Two rules hold
throughout:

- **Ratings are imported, never generated (NEWS-1).** An outlet's lean, factuality, state affiliation and ownership
  come only from declared rating providers, each with its license; no model or heuristic writes a rating.
- **Coverage describes reporting, never truth.** A story covered only by the right is not false, and one covered by
  everyone is not true. Whether something happened is decided by non-news evidence (see "Corroboration" below).

### Where articles come from

**`gdelt-articles`** (polling, enabled by default like every free source) reads the GDELT DOC 2.0 API for your 117
attention topics. Enabled topics are written into its `topics` parameter on every change, exactly as for
`gdelt-timelines` and `wikimedia-pageviews`, so a topic with a GDELT query is all it needs. For each topic a run sends
one request:

```text
GET https://api.gdeltproject.org/api/v2/doc/doc?query={the topic's GDELT query}&mode=ArtList&format=json
    &maxrecords=250&sort=DateDesc&timespan={time since the topic's last run}
```

The timespan is at least 15 minutes and at most one day (the first run asks for one day). A run reads at most 10
topics and takes the rest round-robin on later runs. The default interval is 30 minutes (at least 15), the budget 10
requests a minute and 1,000 a day. **Pacing:** GDELT allows one request every 5 seconds per client, and answers a
faster client with a plain-text HTTP 429 ("Please limit requests to one every 5 seconds…"). `gdelt-articles` and
`gdelt-timelines` both declare a 5-second `hostPacing`, so the two share one gate on `api.gdeltproject.org` and
together never start more than one request every 5 seconds; a request that cannot start within 30 s is skipped
(reason `host-pacing`) and the topic is read on a later run. Enabling both sources therefore slows each of them down.

Only the ArtList answer is fetched (title, link, domain, language, source country, the time GDELT saw the article);
the article itself and its image are never requested. An article found for several topics in one run is one record
with every topic. The records are kept 90 days (the bulk-event class). GDELT's terms ask for citation and a link
(the source's `attribution`); the source is `OPEN`, and its originator is `gdelt`.

**Outlet feeds (`news-feed-*`).** No outlet feed is bundled, because outlets' terms differ. You add the RSS or Atom
feeds of outlets whose terms you have read through the 073 feed-kit operator overlay. Turn the overlay on with the
deployment property `foresignal.kits.operator-specs.enabled=true` (environment `FORESIGNAL_KITS_OPERATORSPECS_ENABLED=true`),
then put a JSON file holding an array of specs in `${FORESIGNAL_DATA_DIR}/kits/feeds/` (every `*.json` file there is
read at startup, in name order; specs are never accepted over the API). An outlet feed's id starts with
`op-news-feed-`, and it must name the reviewed item mapper `news-article` (only these feeds may use it):

```json
[
  {"id": "op-news-feed-example-world", "name": "Example Times: world news",
   "url": "https://www.example-times.com/world/rss.xml", "itemMapper": "news-article",
   "observedAt": "published", "language": "en", "interval": "15m",
   "budget": {"perMinute": 2, "perDay": 200},
   "license": "Example Times terms: headlines and links for personal use",
   "attribution": "Example Times", "homepage": "https://www.example-times.com/",
   "termsUrl": "https://www.example-times.com/terms"}
]
```

Restart, then enable it like any source (`PUT /v1/sources/op-news-feed-example-world {"enabled": true}`). The startup
check lists every problem with its field (the id pattern `^op-[a-z0-9]+(-[a-z0-9]+)*$`, https only, the overlay URL
must pass the outbound URL policy, `license` and `attribution` required, a `kind` other than `news-article` refused,
an interval of at least 5 minutes). The kind is always `news-article` and the retention 90 days unless the spec sets
`retention`. Each item becomes the same record `gdelt-articles` writes; its `seenAt` is the record's observation time
(the spec's `observedAt`: `published` or `updated`), else the item's published or updated time, and an item with no
date at all is skipped. The language is the item's, else the feed's, else the spec's; the source country stays empty
(the outlet registry supplies it); the feed summary (at most 500 characters) and the first image enclosure, as a link
only, are kept. **Authors are dropped** (NEWS-2: no bylines are stored). The `license` text goes through the 109
license catalog like any descriptor license, so terms it does not recognize keep the records local. The bundled
`news-feed-*` family is empty; outlet feeds of both families are their own 116 originator (`$source`, class media).

**The `news-article` record.** `externalId` is the lower-case hex SHA-256 of the article's **canonical URL** (scheme and
host lower-cased, default port, user info and fragment dropped, tracking parameters such as `utm_*`, `fbclid`, `gclid`,
`msclkid`, `mc_cid`, `igshid`, `ref`, `smid`, `ocid`, `at_*` and `share` removed, other query parameters kept in
order), so one article seen by GDELT and by the outlet's own feed is one record. The payload holds `title` (at most
500 characters), `url` (canonical), `domain` (the host without `www.`), `outletId` (GDELT resolves it on fetch; feed
records leave it null for the registry), `language` (ISO 639), `sourceCountry` (ISO 3166 alpha-2), `seenAt`,
`publishedAt`, `topicIds`, `summary` (feed summaries only) and `imageUrl` (a link, never fetched). `observedAt` is
`seenAt` and `eventTime` is `publishedAt`, else `seenAt`. Titles are embedded (027) when an embedding model is set
up; story clustering uses those vectors. Article text is untrusted data, never instructions.

**`media-ratings`** writes no records: it is the budgeted upstream path (30 requests a minute, 1,000 a day, 16 MB per
response) of the Wikidata and Partisan Web imports below. Like every free source it starts enabled, and the
scheduled imports (Wikidata daily, The Partisan Web weekly) run while both this source and the provider are
enabled, so disable `media-ratings` if nothing should contact `query.wikidata.org` or GitHub. An explicit
`POST /v1/media/ratings/import?provider=wikidata` by an admin runs whenever the provider's license is acknowledged,
and always uses this source's budget. To stop a provider, disable it (`PUT /v1/media/providers/wikidata
{"enabled": false}`).

### The outlet registry

Every article belongs to an **outlet**, found from its domain: exactly, else through its parent domains
(`edition.cnn.com` → `cnn.com`), following merges. An unknown domain is registered as an `unrated` outlet of type
`other`, with the country and language its article gave. Outlet types are `wire`, `national`, `regional`, `local`,
`public-broadcaster`, `state-affiliated`, `aggregator`, `blog`, `official` and `other`.

Thirteen news agencies are seeded once from `media/wires.json` as type `wire`, each mapped to its own 116 originator:
Associated Press (`ap`), Reuters (`reuters`), Agence France-Presse (`afp`), Deutsche Presse-Agentur (`dpa`), Agencia
EFE (`efe`), ANSA (`ansa`), Xinhua (`xinhua`), TASS (`tass`), Kyodo (`kyodo`), Yonhap (`yonhap`), Press Trust of
India (`pti`), Asian News International (`ani`) and Anadolu (`anadolu`). The file holds registry facts only, never
ratings.

- `GET /v1/media/outlets?q&country&type&bucket&cursor&limit` (read) lists outlets with their consensus per
  dimension; `bucket` is `left`, `center`, `right` or `unrated`.
- `GET /v1/media/outlets/{id}` (read) adds every rating with its provider, license, attribution and date;
  404 `UNKNOWN_OUTLET`.
- `PATCH /v1/media/outlets/{id}` (admin, JSON merge patch) changes `name`, `type`, `ownerOrg`, `parentOrg`,
  `wikidataQid`, `domains`, `aggregates` and `mergeOutletIds`. Ratings are not patchable: any other member is a 400,
  because ratings come only from providers.

**`aggregates`** lists the wires (their originator ids) an outlet syndicates; empty means any wire. It decides which
reprints count as the wire's story (below). **Merging** duplicates (`"mergeOutletIds": ["01J…"]`) moves their domains
and ratings to the outlet you patch; where the survivor already has a rating from the same provider and dimension,
the survivor's is kept. A merged id keeps resolving to its survivor.

```json
PATCH /v1/media/outlets/01JB8Q6Z3V9K2M4N7P1R5T8W0X
{"type": "regional", "aggregates": ["ap", "reuters"], "mergeOutletIds": ["01JB8Q70A1B2C3D4E5F6G7H8J9"]}
```

### What the ratings mean

| Dimension | Scale | Meaning |
|---|---|---|
| `lean` | −2 … +2 | −2 left, −1 lean-left, 0 center, +1 lean-right, +2 right |
| `factuality` | 1 … 5 | how reliably the outlet reports facts (5 highest) |
| `reliability` | as the provider gives it | a provider's overall reliability score |
| `state-affiliation` | 1 | 1 marks state media |
| `ownership-type` | label | for example `public-broadcaster`, `state-media`, `news-agency` |
| `{provider}:{axis}` | as published | a provider's own axis, such as `partisan-web:governmental`; never forced into `lean` |

**Consensus.** For each outlet and dimension Foresignal stores the **median** of the enabled providers' values,
`nProviders` (providers with a rating, label-only ones included), `disagreement` (the largest value minus the
smallest) and, for `lean`, a **bucket**: `center` while |median| < 0.75, else `left` (−0.75 and below) or `right`
(+0.75 and above); an outlet without a lean consensus is `unrated`. The consensus is recomputed after every import and
whenever a provider is enabled or disabled or its egress changes. Every response carries the per-provider breakdown
next to the median, so a disagreement of 2 or more (one provider says center, another right) is visible rather than
averaged away; read it before trusting a bucket. Factuality bands for coverage counts are the median rounded to 1–5.

**Local-only ratings.** A `LOCAL_ONLY` rating (AllSides without your recorded permission, MBFC always, operator rows
under restrictive terms) is shown on this instance's API, MCP, SSE, tiles, notifications, LLM, exports and archive.
On paths that may not carry local-only data (share links, feeds, sinks, federation) its value is withheld and the
consensus is recomputed from the `OPEN` providers only. Coverage counts use every enabled provider, local-only ones
included, because they are derived counts, never the ratings themselves.

### Rating providers and their licenses

| Provider | License | Egress | Default |
|---|---|---|---|
| `wikidata` | CC0 1.0 | `OPEN` | enabled, license acknowledged |
| `partisan-web` | CC BY 4.0 (the `LICENSE` file of `github.com/stockHuman/tpw-data`, checked 2026-10-17; its README badge says CC BY 4.0 but links CC BY-SA 4.0) | `OPEN` | enabled, license acknowledged |
| `allsides` | reported as CC BY-NC 4.0, **not confirmed**: allsides.com refused automated reads (HTTP 403) on 2026-10-17; confirm it yourself | `LOCAL_ONLY` unless you record permission | disabled |
| `mbfc` | Media Bias/Fact Check API terms (paid, through RapidAPI with your own key; the terms exclude redistribution) | always `LOCAL_ONLY` | disabled |
| `operator` | declared on each row you import | `OPEN` for a recognizable open license (CC0, CC BY, CC BY-SA, ODbL, PDDL, public domain, US government), `LOCAL_ONLY` otherwise (NC, ND, proprietary, unknown) | enabled, license acknowledged |

`GET /v1/media/providers` and `GET /v1/media/providers/{id}` (admin) show each provider's state: `enabled`,
`licence`, `licenceUrl`, `attribution`, `licenceAcknowledged` with `acknowledgedBy` and `acknowledgedAt`, `egress`,
`permissionRecorded`, `credentialSet`, the last import and `ratingsCount`. Enabling a provider, or importing for it,
before an admin has acknowledged its license is `409 PROVIDER_LICENCE_NOT_ACKNOWLEDGED`; withdrawing the
acknowledgement disables the provider.

- **Wikidata** (daily at 03:40 UTC while enabled, or `POST /v1/media/ratings/import?provider=wikidata`): registry
  outlets not asked in the last 30 days, at most 200 per run and 20 domains per SPARQL query to
  `https://query.wikidata.org/sparql`, are looked up by their official website (P856). Where several items match, a
  news class wins (news agency, newspaper, daily or online newspaper, news website, public broadcaster, state media,
  periodical), then the item with most facts, then the lowest QID. Registry facts: the QID, the country (when still
  unknown), owner (P127) and parent organization (P749) names, and the type (news agency → `wire`, public
  broadcaster → `public-broadcaster`, state media → `state-affiliated`) while it is still `other`. Ratings: `lean` from
  political alignment (P1387: far-left and left-wing −2, center-left −1, centrism and radical centrism 0, center-right
  +1, right-wing, far-right and right-wing extremism +2; an unmapped alignment is kept as a label without a value;
  several give their median), `state-affiliation` 1 for state media, and `ownership-type`.
- **The Partisan Web** (weekly, Monday 04:10 UTC while enabled, or `?provider=partisan-web`): the repository's
  organization files, read from `raw.githubusercontent.com`. `bias_social` (−10…10) becomes `lean` (value ÷ 5);
  `fact_sourcing` (0…10) becomes `factuality` (1 + 4 × value ÷ 10); `bias_governmental` (authoritarian–libertarian,
  relative to the country) becomes `partisan-web:governmental` as published, and the editorial, pseudoscience,
  conspiracy, message and ideological fields become their own `partisan-web:` axes; the tags `state-owned` and
  `state-sponsored` give `state-affiliation` 1. A file still at the template's defaults gives no rating, and the
  `ownership` field is never imported (it may name people). The data was last pushed on 2021-10-09 (about 70 files):
  read its ratings as five years old.

### Enabling AllSides or MBFC with your own rights

Foresignal never downloads AllSides or MBFC data. If you hold the rights to use their ratings, you import them
yourself:

1. **Acknowledge the license and enable the provider** (admin):

   ```json
   PUT /v1/media/providers/allsides
   {"licenceAcknowledged": true, "enabled": true}
   ```

2. **AllSides only, when you have its permission to redistribute:** `{"permissionRecorded": true}` makes its ratings
   `OPEN`. Without it they stay `LOCAL_ONLY`. Recording permission is your statement, not Foresignal's check.

3. **MBFC only:** store your RapidAPI key with `{"apiKey": "…"}` (write-only and encrypted; the provider then shows
   `credentialSet: true`; a blank value removes it). **The live MBFC API is not called.** Its endpoint could not be
   verified while this was built (the RapidAPI listing could not be read), and Foresignal does not call an API it
   cannot verify. The key is kept for a future verified client; until then MBFC ratings come from the export you
   retrieve with your own subscription, imported as below. MBFC ratings are always `LOCAL_ONLY`.

4. **Import your rows** (admin) as CSV with the header `domain,dimension,value,label,licence,attribution` (`license`
   is accepted for `licence`; `url` is optional), as JSON `{"rows": [...]}` with the same members, or as a multipart
   `file`:

   ```sh
   curl -X POST "http://foresignal.local:8080/v1/media/ratings/import?provider=allsides" \
     -H "Authorization: Bearer $ADMIN" -F file=@allsides-ratings.csv
   ```

   ```text
   domain,dimension,value,label,licence,attribution
   apnews.com,lean,,Center,,
   foxnews.com,lean,,Right,,
   ```

   A number is the normalized value (`lean` −2…2 and `factuality` 1…5 are checked). Labels are mapped: AllSides
   Left −2, Lean Left −1, Center 0, Lean Right +1, Right +2 (Mixed is kept as a label); MBFC Extreme Left and Left −2,
   Left-Center −1, Least Biased and Center 0, Right-Center +1, Right and Extreme Right +2; factual reporting Very High
   5, High 4, Mostly Factual 3.5, Mixed 2.5, Low 2, Very Low 1. MBFC's Pro-Science, Conspiracy-Pseudoscience,
   Questionable and Satire are not left–right positions and go to `mbfc:category`. At most 50,000 rows and 20 MB.
   The answer counts `imported`, `updated`, `rejected` and `outletsCreated`, and lists up to 100 refused rows with
   their row number and reason; an import without one usable row is `400 RATINGS_IMPORT_INVALID`.

**Your own ratings** go through the `operator` provider (the default of `POST /v1/media/ratings/import`), and every
row must declare its `licence` and `attribution`; the license decides the row's egress as in the table above.

### Why ratings are never generated

A rating is a judgment about an outlet. A model asked to rate outlets would reproduce whatever its training data
believed, with no license, no date and no one accountable for it, and a heuristic tuned on coverage would rate outlets
by what they report, which makes the coverage statistics circular. So nothing in Foresignal writes a rating: not the
LLM roles, not the analyst, not the coverage job. An outlet no provider rates stays `unrated`, and coverage built
mostly on such outlets says so (`MOSTLY_UNRATED_OUTLETS`). To rate an outlet yourself, import an `operator` row with
your own license and attribution: it is then your rating, labeled as yours.

### Stories and syndication

Articles are clustered into **stories** by the 116 evidence resolver's `news-story` family (`intel/canonical-rules.json`
1.1.0): a story is a canonical event and each article an observation of it.

- An article joins a story when its title embedding has a cosine of at least 0.82 with the story's centroid (the mean
  of its members' vectors, same model), or, without vectors, when its normalized title shares at least 60% of its
  tokens (Jaccard) with the story's title or one of its latest 50 member titles. Candidate stories had an article in
  the last 48 hours and share a topic or a region with the article (the regions are the attention topics' region
  codes); an article with neither may join any story in the window. The best score wins, ties go to the older story.
- The title is the most central article's (highest mean similarity to the others; ties to the earliest).
- **Evidence groups, not articles.** Near-identical titles (Jaccard 0.9 or more) from different outlets are reprints.
  A reprint cluster that includes the wire's own article becomes the group `wire:{originator}` with every reprint from
  an outlet that syndicates that wire (its `aggregates` names it or is empty), flagged `syndicated`; without the wire's
  own article, outlets that name a common wire form that wire's group; otherwise the cluster is one group of unknown
  origin. Every other article is its own outlet's group (`outlet:{id}`). **Thirty newspapers printing one AP story are
  one evidence group**, and the story is flagged `ECHO`; a story with one group is `SINGLE_ORIGINATOR`.
- **Corroboration** (every 10 minutes, stories active in the last 3 days): a story is `corroborated` when a non-news
  canonical event shares one of its regions within 24 hours of its articles and has an observation from a
  sensor-network or agency originator. Coverage never decides corroboration: an earthquake is corroborated by
  seismometers, not by how many outlets reported it.

Stories are published as `foresignal-intel` records of kind `news-story` carrying derived counts only (title, topics,
regions, counts, coverage balance, blindspot, state-affiliated share, corroborated), never a rating, so ordinary rules
can watch them. Article rows are pruned after 90 days; stories keep their aggregates.

- `GET /v1/news/stories?topic&region&situationId&since&blindspot&cursor&limit` (read; `blindspot` is `left`, `right`
  or `any`; `region` a 040 code), newest activity first.
- `GET /v1/news/stories/{id}` (read; 404 `UNKNOWN_STORY`): the articles grouped by outlet with each outlet's ratings
  and consensus, the coverage statistics, and the linked situations and canonical events.
- `GET /v1/news/articles?story&outlet&since&cursor&limit` (read), newest first.

A restricted token sees no articles of sources outside its `sources` restriction, and a token restricted by `bbox` or
`area`, or by `kinds` that leave out `news-article`, sees no articles at all; story counts are derived and stay
visible.

### Reading blindspots

`CoverageAnalyzer` 1.0.0 counts each story's (and each topic's, situation's and hypothesis's) **evidence groups** by
lean bucket, country, outlet type and factuality band, with their shares (`coverageBalance`), the share from
state-affiliated outlets (type `state-affiliated` or a state-affiliation consensus of 0.5 or more), `nRated` and the
article counts for reference. Every share and threshold uses evidence groups, never articles, so syndication cannot
manufacture a blindspot.

- **`blindspot: right`** means at least 75% of the **rated** evidence groups come from right-leaning outlets: a story
  the left barely covers. `left` is the mirror image. The value names the side the coverage comes from.
- **At least 8 rated groups** are needed before a blindspot is called. Seven outlets from one side say little about
  the other side's silence, and unrated outlets are left out of the denominator.
- **`skewed`** (caveat `SKEWED_COVERAGE`) needs only 3 groups: at least 75% of the rated groups from one bucket
  (center included), or at least 75% of all groups from state-affiliated outlets.
- **`MOSTLY_UNRATED_OUTLETS`** when more than half the groups come from outlets no enabled provider rates: the buckets
  then describe the minority of the coverage.
- **`corroborated`** comes from non-news evidence only (above), never from the coverage counts.

A blindspot describes **who reported**, not **what is true**. It may mean one side ignores a real event, that the
other side's reporting of it is the error, or that the providers rate few outlets in that country. Read it with the
per-provider disagreement and `nRated`, and check corroboration before drawing anything from it.

### Coverage in analysis

- **Situations (118).** News stories join situations as `context` members of family `attention`: they show a
  situation's coverage but never seed, activate or escalate it. `GET /v1/situations/{id}` carries `coverage` (the
  coverage statistics of its stories, with `corroboratedShare`, the share of them corroborated), null when it has no
  news members.
- **Dossiers (129).** `GET /v1/objects/{ref}/dossier` fills the top-level `coverage` (`status` `ok` or `unavailable`,
  and the statistics) for `situation:` and `news-story:` references, and leaves it null for other types; `coverage` is part of the
  default `include`, so name it when you narrow the sections.
- **Hypotheses (125).** `newsCoverage` on each hypothesis gives the statistics of the live ACH rows rated `C` or `CC`
  for it whose references are news articles (one row per evidence group), with the `SKEWED_COVERAGE` caveat when
  skewed. `ConfidenceAssessor` (1.1.0 and later) lowers the set's confidence **one step** when that support is skewed **and** has
  no non-news corroboration (no supporting non-news row and no corroborated story); `basis.newsCoverage` says why.
  Skewed but corroborated support changes nothing. An indicator can watch a `rollup:news/…` series with
  `changePoint: true`.
- **Assessments (128).** A deterministic **Coverage** section (id `news-coverage`) follows Forecasts when a situation
  in scope has news, and the assessment's top-level `coverage` lists each situation's statistics, the stories with a
  blindspot (`blindspotStoryIds`) and the divergence findings (`divergenceFindingIds`).
- **Reports (132).** The `coverage` block carries the situations' statistics and their caveats; `NO_NEWS_COVERAGE`
  when none of the situations in scope has news, and `MEDIA_COVERAGE_UNAVAILABLE` only when the news layer is absent.
- **Reliability (109).** When a news article is evidence (intelligence-event scoring, indicator observations), its
  reliability grade follows its outlet's factuality band through the app setting **`media.reliabilityMap`**, a JSON
  object from band to grade. The default is `{"5": "A", "4": "B", "3": "C", "2": "D", "1": "E", "unrated": "F"}`;
  your entries are merged over it and invalid ones ignored. The event scorer weighs the grades A 0.9, B 0.75, C 0.6,
  D 0.45, E 0.3, and F at its default.

### Findings and series

The coverage job (every 10 minutes, recomputing the last 3 days) writes, into the 119 findings table, with the caveat
`COVERAGE_NOT_TRUTH` on both:

- **`coverage-blindspot`**, once per story with a blindspot (`notable`; members: the canonical event and up to 20
  article records);
- **`coverage-divergence`** (detector `coverage-divergence` 1.0.0): on the last complete UTC day one lean bucket of a
  topic's coverage rose at least 3 standard deviations above its 28-day baseline (at least 14 baseline days, a
  standard deviation of at least 1, at least 3 groups that day) while the other buckets stayed below 3. One side of
  the press suddenly covering a topic the others do not is worth a look; it is not evidence about the topic.

Both are published like other findings and reach the `analytics` SSE topic (`finding.created`), signal family
`attention`, theme `mixed`. The daily series `rollup:news/{topicId|situationId}/{bucket}` (buckets `left`, `center`,
`right`, `unrated` and `state`: evidence groups first seen that UTC day) are registered in the 119 analysis-series
registry once they have 14 days of rows, so the change-point detectors and event studies can use them.

### MCP and problem codes

MCP read tools `list_news_stories`, `get_news_story` and `get_outlet` (also in the analyst's and the cycles' read
catalog). Contract 1.4 adds the object types `news-story` (its id is the story's canonical event id) and `outlet`.

| Code | When |
|---|---|
| `404 UNKNOWN_STORY` | no such story |
| `404 UNKNOWN_OUTLET` | no such outlet |
| `409 PROVIDER_LICENCE_NOT_ACKNOWLEDGED` | enabling a provider, or importing for it, before an admin acknowledged its license |
| `400 RATINGS_IMPORT_INVALID` | an import with an unknown provider, a malformed body or no usable row |

## Chronicle network (prompt 134)

Chronicles that follow the same events find each other. Foresignal compares **fingerprints** of what each chronicle
captured, never the captured data: on this instance (other analysts' private chronicles included) and, when you turn
it on, across federated instances that agreed a shared salt. What an analyst learns about a related chronicle depends
on whether they may see it, on its owner's **discoverability** setting and on its TLP label. Analysts can ask the
owner for access without revealing their own work, shared evidence is counted once, and an interpretation that comes
back through other analysts is never mistaken for independent confirmation.

### What is shared: fingerprints, never content

- **Global evidence keys.** From each chronicle's own captured rows (`chronicle_version`), Foresignal derives text keys
  that any instance capturing the same upstream content derives identically: `ev:{family}:{originator}:{upstreamId}` for
  a 116 canonical event with an upstream id, otherwise `cell:{family}:{H3 resolution-5 cell}:{UTC hour}` of the
  canonical event; `ent:{keyType}:{value}:{UTC day}` for ICAO hex (`icao24`), MMSI, IMO and NORAD keys;
  `at:{H3 resolution-4 cell}:{UTC day}` for every located row; and `topic:{hash of the normalized keyword set}` for the
  chronicle's attention topics and the topics of captured news articles. Rows of private sources (039) give no keys.
- **Keyed hashes.** Every key is hashed with HMAC-SHA256 under a salt and truncated to 64 bits. The **instance salt**
  (local discovery) is 32 random bytes generated on first use and rotated yearly; a rotation rebuilds every key set
  under the new salt. A **network salt** keys federated discovery (below). Salts are stored encrypted with the
  instance's secret key (`secret.key`, like source credentials), never exported and never returned by the API.
- **Sketches.** Per chronicle, key type and salt Foresignal keeps a MinHash signature of 128 seeded, deterministic
  permutations and the exact set size (`chronicle_fingerprint`). The hashed key sets themselves (`chronicle_key`) stay
  on this instance; they are what lets full links list the shared canonical events exactly. Sketches are updated
  incrementally from what was captured since the last pass, every hour and when capture stops.

### Discoverability and TLP

`PUT /v1/chronicles/{id}/discoverability` (owner, agent scope) takes `discoverability` (`none`, the default, `instance`
or `network`) and `revealTitle` and `revealOwner` (both false by default). The TLP label caps it:

| Label | Allowed discoverability |
|---|---|
| `RED` | `none` only: never discoverable |
| `AMBER+STRICT` | `none` or `instance` |
| `AMBER`, `GREEN`, `CLEAR` | any |

Asking for more is `409 DISCOVERABILITY_NOT_ALLOWED` with the `rule`. The response (and the chronicle's own
`discoverability`) is the effective setting, with `maxAllowed` and `tlp`. When a stricter label is set later (through
the chronicle or `PUT /v1/objects/chronicle:{id}/acl`), discoverability is lowered automatically, the change is logged
and the owner gets `chronicle.discoverability_lowered` in the inbox. Lowering it from `network` withdraws the chronicle's
sketch from peers at the next exchange.

### How links are found

The `ChronicleDiscoveryJob` runs nightly (03:30 UTC) and, every hour after the sketch refresh, whenever some chronicle's
sketch changed by more than 10% since its last discovery:

- **Candidates** come from LSH banding over the signatures (16 bands of 8 rows, per key type): two sketches that agree
  on every row of a band are compared.
- **Scores** per key type are the Jaccard similarity and the containment of the smaller set. Local pairs are scored
  **exactly** from both key sets; remote pairs are estimated from the signatures and the peer's log2 size bands.
- **A link exists** when, for at least one key type, the shared keys reach `network.minSharedKeys` (app setting,
  default 5) **and** the containment of the smaller chronicle reaches 0.05. The key types that pass are its
  `dimensions`; its **strength** comes from the largest Jaccard among them: `low` below 0.1, `medium` below 0.3, `high`
  from 0.3. The `temporal` relation says which chronicle saw the shared keys first (more than an hour earlier for at
  least twice as many of them; else `concurrent`), and `timeOverlap` compares the capture spans.
- **Clusters** are connected components of medium-or-stronger active links (`chronicle_cluster`); a cluster keeps its
  id while it changes.
- A link that no longer passes is removed; a dismissed link stays dismissed. Deleting a chronicle removes its keys,
  sketches and links.

### What each analyst sees

`GET /v1/chronicles/{id}/related` (read; MCP `get_related_chronicles`) returns each link of a chronicle the caller can
see at its disclosure level:

- **full**, when the caller can also see the other chronicle (owner, access list or admin): the other envelope, the
  per-dimension scores, `sharedCanonicalEvents` and `sharedEntities` (at most 100 each, computed exactly),
  `sharedKeyCount`, `sharedSituations`, `temporal` and `timeOverlap`;
- **opaque**, when the other chronicle is discoverable to the caller (`instance` or `network` after the TLP cap) or is a
  peer's sketch (`remote: true`): only the strength band, the dimensions, the time overlap and, if its owner revealed
  them, the title and the owner's handle. No count finer than the band is ever given;
- **nothing**, otherwise.

Each new link notifies the owners of its local chronicles with `chronicle.link.found` on their inbox, `me` topic and
personal channels, at their own disclosure level. `POST /v1/chronicle-network/links/{linkId}/dismiss` (the owner of
either side) dismisses a link; `includeDismissed=true` still lists it.

**Clusters and the combined timeline.** `GET /v1/chronicle-network/clusters` and `/{id}` show clusters holding a
chronicle the caller can see, with visible members in full and directly linked discoverable members as opaque members
(no id). `GET /v1/chronicle-network/clusters/{id}/timeline?from&to` merges the visible members' captured evidence and
thread items, **deduplicated by global evidence key**, and lists for each item which visible chronicles hold it
(`heldBy`). Opaque members contribute nothing.

### Anti-probing

- Discovery runs only on chronicles, never on ad-hoc queries, and only for chronicles that captured at least 50 global
  evidence keys.
- A principal is shown at most 20 new opaque links a day (a link shown once stays visible); the rest wait for later
  days.
- Churn buys nothing: when an owner deleted a chronicle less than 24 hours after creating it, within the last 24 hours,
  that owner's chronicles younger than 24 hours sit out discovery until they are a day old.
- Access requests are limited to 10 per principal per day.

### Federated discovery and network salts

Federation needs both sides' consent. On each instance an admin sets the app setting `network.federated` to `true`
(default `false`) and installs the same salt, agreed out of band (a password manager, a phone call), for a network id:

```json
PUT /v1/chronicle-network/salts
{"networkId": "gulf-analysts", "saltRef": "<the agreed secret, at least 16 characters>"}
```

The answer is the salt's public status (`networkId`, `saltId`, `createdAt`, `rotatesAt`), never its value. A salt is
used for 90 days; after that federated discovery for that network stops until the admins agree and install a new one,
so long-term linkage needs continued consent. Installing a new salt retires the old one and forgets the sketches
received under it; chronicles then get new random sketch ids.

Every day at 04:30 UTC Foresignal sends, for each current network salt, a signed `ChronicleSketchBatch` to every
enabled 113 peer that holds an exchange key (the same `fsp_` key as package exchange, see "Reports and exchange"):
`POST {peer}/v1/federation/chronicle-sketches`, the body signed with the 109 instance key in `Foresignal-Signature`. It
carries, for each chronicle that is `network`-discoverable and has at least 50 keys, a sketch package: a random sketch
id (per chronicle per salt), the key types, the MinHash slots, log2 size bands, the capture span in ISO weeks, the TLP
label, and the title or owner handle only if revealed; and the sketch ids withdrawn in the last 30 days. Nothing else
leaves.

A received batch is accepted only with a valid signature (the peer's pinned key, else its JWKS; otherwise
`400 SKETCH_BATCH_INVALID`), while `network.federated` is on (`409 FEDERATED_DISCOVERY_DISABLED`) and for a network
with a current salt here (`409 NETWORK_SALT_UNKNOWN`). Sketches whose label does not allow network discoverability are
rejected one by one. Received sketches are stored per peer, expire after 30 days unless refreshed, and take part in
discovery like local ones, but only ever yield opaque links. A peer with a different salt simply produces nothing in
common.

### Access requests

`POST /v1/chronicle-network/links/{linkId}/request-access {"message": "…", "offerShare": "<one of your chronicles>"}`
(read) asks the owner of the other chronicle for access. The owner sees only the requester's handle (and instance),
the message and, if the requester chose, which of the requester's own chronicles it offers to share; never the
requester's chronicle in the link. Remote owners receive it through the peer channel. `GET /v1/chronicle-network/requests`
lists the caller's incoming and outgoing requests.

- **Grant** (`POST /v1/chronicle-network/requests/{id}/grant {"role": "viewer" | "commenter", "expiresAt", "deliverAs",
  "message"}`, owner, agent scope): on this instance a 130 access-list grant, checked against TLP
  (`403 TLP_VIOLATION`), and the offered chronicle shared back with the owner as viewer; for a remote requester a 132
  signed package addressed to its handle, a `chronicle-extract` (the default) or a `report` (a chronicle recap drafted
  and published for the purpose).
- **Decline** (`…/decline`) or ignore. The requester is told only `granted` or `declined`
  (`chronicle.access.granted` or `.declined`); a decided request is `409 ACCESS_REQUEST_DECIDED`.
- Repeats to an owner who declined the same requester are stored but suppressed for 30 days: the owner never sees them.
  A request on a link whose other side the caller already sees is `409 CHRONICLE_ALREADY_VISIBLE`; more than 10 a day
  is `429 REQUEST_RATE_LIMITED`.

### Circular reporting

Analytic products that cross analysts or instances carry a **provenance chain**, the ordered hops from origin to now
(`instanceId`, `principal`, `productRef`, `manifestId`, `role`): a received package keeps the chain of its manifest, a
local report starts with the chains of the reports, received material and evidence items it cites and ends with its
owner, and a report package now carries that chain. When a report, received material or an evidence item citing them
is added to a hypothesis set as support (`POST /v1/hypotheses/sets/{id}/evidence`, MCP `add_evidence`, or an indicator
observation), `CircularReportingDetector` takes the chain's origin as the 116 evidence group, so:

- support whose chain names the consuming analyst (this instance and principal) before its last hop, importer hops
  aside, is that analyst's own judgment coming back and is marked `CIRCULAR_REPORTING`;
- two supports that share an origin fall into one ACH row (the second one marks it `CIRCULAR_REPORTING`), and the 125
  updater applies its weight once;
- the confidence assessor (1.2.0) counts indicators observed through one origin once and reports `circularReporting`
  and `circularSupports` in the confidence basis, and a report drafted over such a set gets a `CIRCULAR_REPORTING`
  coverage caveat.

The row shows its `provenanceChain` and `caveats`. Package loop prevention (132) counts only the hops of the package's
own product, so a new report from elsewhere that cites your work can arrive; it is then marked, never counted as
independent confirmation.

### Settings, schedule and problem codes

App settings `network.minSharedKeys` (5) and `network.federated` (`false`). The deployment property
`foresignal.chronicles.network-scheduled` (true; off in tests) runs the hourly sketch refresh, the nightly discovery
(03:30 UTC) and the daily expiry and peer exchange (04:30 UTC). AccessPolicy: related links, clusters, timelines, the
request list, dismissing and requesting are read-scope operations held to a token; discoverability and decisions are
agent operations for the owner; salts are admin; the peer inbox takes a peer ingest key.

| Code | When |
|---|---|
| `404 UNKNOWN_LINK` | no such link, or one you may not learn of |
| `404 UNKNOWN_CLUSTER` | no such cluster holding a chronicle you can see |
| `404 UNKNOWN_ACCESS_REQUEST` | no such request addressed to or sent by you |
| `409 DISCOVERABILITY_NOT_ALLOWED` | the TLP label does not allow the requested discoverability (with `rule`) |
| `409 ACCESS_REQUEST_DECIDED` | the request was already granted or declined |
| `409 CHRONICLE_ALREADY_VISIBLE` | an access request on a link whose other chronicle you can already see |
| `409 FEDERATED_DISCOVERY_DISABLED` | a salt or sketch batch while `network.federated` is false |
| `409 NETWORK_SALT_UNKNOWN` | a sketch batch for a network with no current salt here |
| `400 SKETCH_BATCH_INVALID` | a malformed sketch batch or a signature that does not verify |
| `429 REQUEST_RATE_LIMITED` | more than 10 access requests in a day |

## Social signals (prompt 135)

Foresignal can watch what people post about the places, topics and entities you track, on the platforms you choose,
with your own accounts, budgets and judgment. Each platform is an adapter (see the Source Catalog, "Social platforms"):
the keyed platforms collect nothing until you enable them and store their credentials. Bluesky's two adapters need no
key, so their sources are free and start enabled like every free source (see "Sources"): the open Jetstream firehose
(its platform row is also switched on when first created, `foresignal.social.default-enable`, true by default), and
`bluesky-search`, which searches the social terms of the attention topics that apply to Bluesky on every run although
its platform row reads `disabled` until you change it. Disable either with `PUT /v1/social/platforms/{platformId}`
and `{"enabled": false}`, which also disables its source; your choice stands afterwards. Read each platform's terms before enabling it: the platform view carries a summary and the link,
but the terms are the platform's and they change.

### Platforms, credentials and budgets

| Call | What it does |
|---|---|
| `GET /v1/social/platforms` (read) | Every platform with its state (`disabled`, `credentials-required`, `budget-exhausted`, `unavailable`, `degraded` or `enabled`), its collection paths, quotas, spend, deletion sync and terms |
| `GET /v1/social/platforms/{platformId}` (read) | One platform, adding the stream's cursor, lag and gaps, and the monthly estimate of X standing counts; 404 `UNKNOWN_PLATFORM` |
| `PUT /v1/social/platforms/{platformId}` (admin) | Replace its settings (below) |
| `PUT /v1/sources/{id}/credentials` (admin) | Store the platform's credentials; the platform id is the source id |

```json
PUT /v1/social/platforms/x
{"enabled": true, "mode": "counts", "monthlyCapUsd": 25, "deletionSync": true, "postRetention": "30d",
 "channels": [], "options": {"standingCounts": true, "fullArchive": false, "maxResults": 100}}
```

`enabled` is required; `mode` is for `x` only (`counts`, `search` or `counts+search`); `channels` (at most 200) are
platform channels to follow (subreddits for Reddit, channel ids for YouTube, `#tags` for Mastodon and Instagram);
`deletionSync` and `postRetention` (at most 3650 days) are described below; `budget` (`perMinute`, `perDay`) changes
the source's request budget; `prices` and `pricesAsOf` replace the money table when a platform changes its prices;
`options` carries the per-platform switches: `instances` (Mastodon), `replay` (Bluesky Network
Replay), `standingCounts`, `fullArchive`, `maxResults` and `fieldsParam` (X), `comments` (YouTube; comments are not
read yet). Enabling a platform whose credentials are missing leaves it `credentials-required`; storing them later
turns it on.

| Platform | Credentials | Notes |
|---|---|---|
| `bluesky-jetstream` | none (`apiToken` only for Network Replay) | on by default; `endpoint` `us-west` uses the west instance |
| `bluesky-search` | none, or `identifier` and `appPassword` (an app password from Bluesky's settings) | source on by default (free); runs the standing topics' terms every run |
| `mastodon` | `accessToken`, or per instance `instanceTokens` as `host=token` pairs | a **user** token with `read:statuses` (streams need it since Mastodon 4.2) and `read:accounts` for followed accounts; list the instances in `options.instances` |
| `reddit` | `clientId`, `clientSecret`, `contactEmail` | register a script app at `reddit.com/prefs/apps`; Reddit approves Data API access by hand, for non-commercial use |
| `youtube` | `apiKey` | a Google Cloud project with the YouTube Data API v3 enabled |
| `x` | `bearerToken` | a pay-per-use developer account; see "The X spend cap" |
| `threads` | `accessToken` | a Threads app with `threads_basic` and `threads_keyword_search` (the latter needs Meta's app review) |
| `instagram-hashtags` | `accessToken`, `userId` | a professional account and Instagram Public Content Access (Meta's app review) |

**Quotas are kept locally.** Every platform's published limits (requests per window, YouTube's 10,000 units a day and
its 100 searches a day, Threads' 2,200 queries per rolling day, Instagram's 30 hashtags per rolling week) are counted
before each call, rolling windows included; a call that would exceed one is not sent, and the platform shows
`budget-exhausted`. A `429` or a platform's own quota error backs the source off as for any
source.

### The X spend cap

X bills every read. Before each call Foresignal estimates its cost from the price table (a search: `max_results` ×
the post-read price; counts: the request price; a deletion check: the ids × the post-read price) and refuses it when
this month's spend plus the estimate would pass `monthlyCapUsd` (default $25; `null` removes the cap). A refused call
is counted on the platform, shown in its health, and answers `BUDGET_EXCEEDED` with `monthlyCapUsd`, `spentThisMonth`
and `estimateUsd` on the API path. What a call actually cost is charged from the answer, and the month's spend is
`SourceHealth.spentUsdThisMonth` and the platform's `spend`. Hourly standing counts cost one request per standing topic
an hour (about $3.60 a topic a month at $0.005): the platform shows `standingCountsEstimateMonthly` for the topics you
have before you switch them on. The prices were read on 2026-10-17; update `prices` and `pricesAsOf` when X changes
them.

### What to watch: the attention topic's `social` block

Social collection follows your 117 attention topics. Add a `social` block to a topic (`PUT /v1/attention/topics/{id}`):

```json
"social": {"mode": "standing",
           "terms": {"en": ["Strait of Hormuz", "Hormuz tanker"], "fa": ["تنگه هرمز"]},
           "hashtags": ["Hormuz"], "exclude": ["hormuz restaurant"],
           "accounts": [{"platform": "bluesky", "handle": "example.bsky.social"}],
           "channels": [{"type": "subreddit", "id": "geopolitics"}],
           "platforms": [], "placeIds": [], "regionCodes": ["IR", "OM"], "entityRefs": []}
```

- `mode` `standing` collects and counts continuously; `cue-only` collects only the targeted searches (cues) of prompt 137.
- `terms` per language (at most 50 in total) match case- and diacritic-insensitively on word boundaries (Arabic and
  Hebrew prefixes such as و and ה are allowed; Chinese and Japanese match anywhere); `hashtags` (at most 30) match
  tags; `exclude` terms drop a post that would otherwise match. `accounts` (at most 100, `{platform, handle}`) and
  `channels` (at most 100, `{type, id}` with type `subreddit`, `mastodon-instance`, `mastodon-hashtag` or
  `youtube-channel`) also feed the adapters' follow lists; `platforms` limits the topic to some networks (`bluesky`,
  `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`; empty: all).
- `placeIds`, `regionCodes` and `entityRefs` make the topic's posts count for those places, regions and 098 entities
  too, so `GET /v1/social/volume?place=` works without a separate topic.

Editing a topic rebuilds the matcher at once. Every adapter matches every post against the same index; a post that
matches is stored as a `social-post` record, and on Jetstream every post, matching or not, is counted once for its
platform and language (the **denominator**). `social.languages` (default `en, ar, he, fa, ru, uk, fr, es, de, tr, zh`)
lists the languages Foresignal tells apart when a post carries no language tag; anything else counts as `other`.

### What is stored, and for how long

A `social-post` record holds the post's text (at most 5,000 characters), language, creation time, links, hashtags,
mentions, reply and quote references, engagement counts, media links (never downloaded), the platform's own geotag
when it has one, the matched topics and the collection path. The author is kept as handle and display name plus an
`authorKey`: an HMAC-SHA256 of the platform and account id under a random salt that exists only on this instance
(`social_salt`, encrypted with `secret.key`, never exported, never rotated), so counts of distinct authors stay
stable without any cross-instance identifier. Posts are kept 30 days by default (`postRetention` per platform), as the
analytic class `social`. Hourly counts (`social_volume_hourly`, `social_denominator_hourly`, `social_coverage`) carry
no post or author and are kept with the series retention.

**Deletion sync.** With `deletionSync` on (by default for Bluesky, X and Threads, where the terms or the
firehose call for it), a post deleted upstream gets a tombstoned version: the text, links and media links are removed
and `deleted: true` is set, while counts, topics and the author key stay. Jetstream applies delete events as they
arrive; Bluesky search, X, Reddit and Threads check their stored posts once a day (X's check is billed and counts
against the cap). X's Developer Policy asks for deleted content to be removed within 24 hours.

**Where posts may go.** The Bluesky and Mastodon records are `LOCAL_ONLY`: your own API, tiles, streams, MCP and
notifications see them; sinks, federation, share links, feeds and exports without `personalUse` do not. X, Reddit,
YouTube, Threads and Instagram records are `FORBIDDEN` for redistribution: your own use only (109's table). Every post
is evidence of class `social`, with the originator `social:{platform}:{authorKey}`; reposts and quotes keep the
original's originator, so a thousand reposts are one evidence group, never a thousand confirmations. Prompt 136 groups
posts into narratives and extracts their claims (see [Narratives and claims](#narratives-and-claims-prompt-136)).

### Reading volume and coverage

- `GET /v1/social/volume?topic=|place=|entity=&platform&language&metric&interval&since&until` (read): one hourly (or
  daily, `interval=P1D`) series with each point's baseline, `velocityRatio` and robust z, its collection type and
  whether its hour was complete. `metric` is `posts`, `uniqueAuthors`, `reposts` or `engagement`. The same series are
  119 analysis series (`social:topic:{id}/{platform}/posts`, `social:all/{platform}/denominator`, …), registered once
  they have a week of hours, so change points, event studies and the precursor miner use them.
- `GET /v1/social/coverage?platform&region&since&until` (read): per platform, region and hour, the posts attributed to
  the region and whether the hour was complete.
- `GET /v1/social/posts?topic&platform&q&cueId&since&until` and `GET /v1/social/posts/{platform}/{postId}` (read):
  the stored posts, newest first; restricted tokens see only their sources, and spatial restrictions see none.
- MCP: `get_social_volume` and `search_social_posts` (read; also in the analyst's read catalog).

**Baselines and velocity.** `SocialBaseline` 1.0.0 compares each hour with the same hour of the week over the last
eight weeks (at least four collected samples). Where the platform has a denominator, each sample is a share of
everything posted on the platform in that language, so platform growth, an outage or a quiet holiday changes the
expectation instead of looking like attention. When an hour closes, a scope whose posts reach `minRatio` (5) times the
baseline from at least `minUniqueAuthors` (20) distinct authors becomes a `social-velocity` finding (`notable`;
`significant` at a robust z of 8), at most once per scope and platform every `dedupHours` (6); set the three in the app
setting `social.velocity`. Sampled paths (ranked search, YouTube search, X search) are stored and searchable but never
baselined: a ranked search shows what the platform chose to show, not how much was posted.

### Health, gaps and observability

A stream gap (from the drop to the reconnect) is recorded with whether it was replayed: Jetstream resumes from its
cursor when the outage is shorter than 35 hours, otherwise it reconnects live and the gap stays unreplayed. The
platform view and `GET /v1/sources/{id}/health` show the stream's cursor, lag and recent gaps. **Bluesky Network
Replay** (`options.replay` with an `apiToken`) is only planned: Foresignal asks for a snapshot plan and reports it in
the gap, but does not apply replay segments, whose format could not be verified.

Silence on social media means something only where the platform was watched and is busy. `canSeeAbsence` for the
family `social-attention` (method `platform-volume`) holds for a region and window only when a complete stream was
healthy for the whole window and the region's trailing 8-week mean is at least `social.minRegionBaseline` posts an hour
(20); otherwise the answer is `unknown` with `LOW_SOCIAL_COVERAGE` or `SOCIAL_STREAM_GAP`, and absence rules, indicator
absence and downward findings stay silent.

**Streams.** The `social` topic of `/v1/events/stream` (cursor key `o`) carries `social.velocity` and
`social.platform.state`, and since prompt 136 the narrative events (see
[Narratives and claims](#narratives-and-claims-prompt-136)). Problem codes: `404 UNKNOWN_PLATFORM`, `BUDGET_EXCEEDED` (the X cap) and `409 PLATFORM_DISABLED`,
raised by `POST /v1/social/cues` (prompt 137) when no requested platform is enabled and search-capable.

**Settings.** App settings `social.languages`, `social.minRegionBaseline` (20) and `social.velocity`
(`{"minRatio": 5, "minUniqueAuthors": 20, "dedupHours": 6}`); deployment properties `foresignal.social.scheduled`
(true; the hourly close, velocity, flush and daily deletion jobs) and `foresignal.social.default-enable` (true).

## Narratives and claims (prompt 136)

Posts become the units analysts reason with: **narratives** (posts that say the same thing), structured **claims**
(what, where, when and who), shared **media fingerprints**, the **propagation** of a narrative across reposts,
platforms and news, and **coordination indicators**. Four rules hold throughout:

- **Claims are reports, not facts.** A claim's status describes how it relates to independent evidence (prompt 137
  sets it, see [Social versus physical](#social-versus-physical-prompt-137)); nothing here marks a claim true or
  false. Until 137 compares a narrative, its status and its claims' statuses are `unassessed`.
- **Earliest observed, not originator.** Foresignal records the earliest post it observed on each platform; it never
  says who started a narrative.
- **Indicators, not labels.** Coordination is a set of statistics on a narrative against null models, never a label on
  an account ("bot", "troll", "state actor").
- **Deterministic first.** Clustering, places, entities, fingerprints and propagation are deterministic; the optional
  LLM extraction proposes claims for large narratives but never sets a status or a count.

### Reading narratives

A narrative is a 116 canonical event of the family `social-narrative` (`intel/canonical-rules.json`), and each member
post is an observation of it. The `NarrativeClusterer` 1.0.0 runs on the evidence resolver's cursor:

- a post that **reposts or quotes** a member joins that member's narrative;
- otherwise, among narratives whose latest member is within 72 hours of the post, it joins one where a member **links
  the same URL** (canonical URL, tracking parameters removed), then one where a member uses the **same image** (the
  same image URL, or a media fingerprint), then the most similar by **text** among those sharing an attention topic, a
  resolved place or region, or an entity: the post's text embedding (027) against the narrative's centroid at a cosine
  of 0.85 when both exist under one model, else the token MinHash Jaccard against the central member and the latest
  50 members at 0.6;
- otherwise it starts a narrative.

A post collected by two sources joins once. Each member keeps why it joined (`linkReason`: `seed`, `repost`, `quote`,
`text`, `link` or `media`), its evidence group and its centrality. A narrative's title is, deterministically, the most
central member's text cut to 140 characters, and once two or more claims exist a summary of the three most supported
claim types and the top claim's place ("Reported explosion and fire in Aden").

**Origin counts.** The evidence groups of a narrative count **independent origins**: members that repost, quote or link
one original post, link one external URL, or share a media fingerprint first seen in one post are one group, rooted at
that origin; posts of one account are one group; every other author posting their own words is a group of their own.
So 50,000 posts that descend from three reports count as three. Next to `postCount` and `uniqueAuthors`, a narrative
shows `evidenceGroupCount` and **`originConcentration`**, the share of posts in the three largest groups: near 1, the
volume traces back to a handful of origins; low, many authors are saying it in their own words.

**State and velocity.** `velocity` is the posts in the last closed hour over the median hourly posts of the 24 hours
before it (null for a narrative younger than two hours). `state` is `emerging` while the narrative is younger than six
hours or its velocity is 5 or more, `active` while posting continues, `fading` after 24 hours of silence or a velocity
under 0.5, and `dormant` after 72 hours of silence; the hourly pass moves states and records every change in the
history. Narrative merges and splits follow the situation rules (118): `POST /v1/social/narratives/{id}/merge
{"narrativeIds": [...], "reason"}` (agent) moves members, evidence links, claims and edges into the surviving
narrative and marks the others `mergedInto`; `POST /v1/social/narratives/{id}/split {"postRefs": [...], "reason"}`
(agent, 201) moves the chosen member posts, with the reposts and quotes of them, into a new narrative; both histories
record it. A one-post narrative whose image turns out to be identical to one in an active narrative is merged into it
automatically (`linkReason` `media`).

**Linked news and situations.** A narrative links the 133 news stories whose articles a member links, and stories in
its window sharing a topic or region whose title tokens overlap its title (Jaccard 0.5); its situations are those
stories' situations. Text-derived regions of every post are counted into 135's `social_coverage`; they never become a
post's geometry.

| Call | What it does |
|---|---|
| `GET /v1/social/narratives?topic&situationId&place&status&state&platform&since&cursor&limit` (read) | Narratives newest activity first (`place` is a 036 place id or a 040 region code) |
| `GET /v1/social/narratives/{id}` (read) | The narrative with its envelope, claims, media fingerprints, coordination block, linked stories and situations, and history; 404 `UNKNOWN_NARRATIVE` |
| `GET /v1/social/narratives/{id}/posts?platform&evidenceGroup` (read) | Member posts newest first; restricted tokens see only their sources. A post whose record expired or was deleted comes back without text (`deleted: true`): the narrative outlives its posts |
| `GET /v1/social/narratives/{id}/propagation` (read) | The propagation graph (below) |
| `GET /v1/social/narratives/{id}/timeline?interval` (read) | Posts and unique authors per platform per `PT5M`, `PT15M`, `PT1H` or `P1D` |

Narratives (and claims) are 129 objects (contract 1.5): `narrative:{id}` and `claim:{id}` resolve to envelopes,
take relations, notes and verdicts, and can be added to workspaces and chronicles. Narratives are public content,
labeled `CLEAR`.

### Claims

The deterministic `ClaimExtractor` 1.0.0 reads every member post:

- **What:** the event terms of `intel/social/claim-lexicon.json` (23 types: explosion, strike, drone, missile, gunfire,
  fire, smoke, flood, earthquake, outage, blackout, internet-down, closure, blockade, seizure, evacuation, protest,
  collapse, spill, crash, gnss-jamming, cyberattack, outbreak), in English, Arabic, Hebrew, Persian, Russian,
  Ukrainian, French, Spanish, German and Turkish, each mapped to a 119 signal family. Terms match on folded text and
  word boundaries (Arabic proclitics and Hebrew prefix letters allowed); a term ending in `*` matches inflected forms
  (`взрыв*`).
- **Polarity:** `denied` when a denial marker ("denied", "fake", "نفى", "опроверг…") appears or a negation stands
  within three words before the term; `questioned` with a question marker or a closing question mark; else
  `asserted`. `hedged` with "reportedly", "unconfirmed", "якобы" and the like.
- **Where:** place names from your 036 places (with their aliases), 040 countries and admin-1 regions, populated places
  of the 040 gazetteer with at least 15,000 inhabitants, and the lexicon's aliases in other scripts, longest match
  first. An operator place wins; among gazetteer candidates the most populous wins, with a confidence that falls with
  ambiguity (`0.9 ×` its share of the candidates' population, at least 0.2; 0.9 for an unambiguous name). The place
  carries its `precision` (`point`, `locality`, `admin1`, `country`), confidence and region code.
- **Who:** 098 entities by multi-word name or alias and by IMO and MMSI numbers, named 104 facilities, and the
  organization named as the actor.
- **When:** "just now", "N minutes/hours ago", "last night", "this morning", "today", "yesterday" and their
  equivalents, relative to the post's creation time (in UTC: the poster's timezone is unknown).

A narrative's claims aggregate its posts' claims by type, polarity and place: `supportCount` (posts),
`evidenceGroupCount` (independent origins among them), `firstObservedAt`, the earliest event time, the most named
actor, and `confidence`, which is **extraction confidence only** (how clearly the text states it), never a probability
that it happened. `GET /v1/social/claims?narrativeId&situationId&type&status&bbox&since&cursor&limit` and
`GET /v1/social/claims/{id}` (read; 404 `UNKNOWN_CLAIM`) list them; MCP `get_claims`. Draw a claim's place in a
"reported" style, never as an observed event.

**Claims as evidence.** Each claim is a 125 evidence item with an Admiralty grade. Its source reliability is that of
the account or channel of its earliest supporting post: `F` (cannot be judged) by default, `E` once that account or
channel has at least 10 claims assessed by 137 with at least 70% corroborated, `D` at 85% or more (the three numbers
are the `trackRecord` member of `PUT /v1/settings/social`, app setting `social.trackRecord`). An operator
override wins: the app setting `social.sourceReliability` is a JSON object from `account:{platform}:{authorKey}` (the
author key shown on the post) or `channel:{platform}:{channel}` to a letter A–F. Information credibility follows the
claim's status: 6 while unassessed, pending or without coverage, 2 corroborated, 5 unobserved, 4 contradicted. Add a
claim to a hypothesis set as evidence with `"source": "social-claim"` and `"ref": "claim:{id}"`; its provenance chain
starts at the narrative, so the same narrative coming back through a report or another analyst counts once (134).

**Optional LLM extraction.** With a chat provider, a narrative of at least `social.llmClaimMinPosts` (50) posts or 5
evidence groups, and again once it has doubled, is read by the `claim-extraction` role (template
`claim-extraction-system@1.0.0`, `GET /v1/prompt-templates/claim-extraction-system`): the 20 most central member texts
go to the model as data, never as instructions (sources the provider may not receive are left out), and it answers
claim tuples in a JSON schema. Types outside the lexicon are dropped, places are resolved through the gazetteer (a place
it does not know is kept as a name without geometry), and the counts are deterministic: the model never sets a status,
a count or a grade. These claims carry `extraction: llm` and a manifest naming the template. At most
`social.llmClaimsPerDay` (20) narratives a UTC day are read, on top of the provider's own token budget; a refusal skips
the run.

### Media fingerprints and what reuse findings can show

For each post image (and video thumbnail) Foresignal fetches the image over HTTPS through the outbound URL policy
(public addresses only, the address pinned, redirects refused, Foresignal's own User-Agent), inspects and decodes it
through the 043 ImageGuard (PNG, JPEG or TIFF; size and pixel limits), computes its **PDQ** hash (Meta's open
perceptual hash: 256 bits and a 0–100 quality) and discards the image, unless the app setting `social.keepMedia` is on
(off by default; the image is then stored content-addressed in the 043 media store under the post's retention). A
hash within Hamming distance 31 of a stored one (PDQ's recommended threshold) is another use of that fingerprint;
hashes of quality below 50 are kept but never matched. Fingerprinting runs from a queue every minute, outside the
evidence cursor, and gives up an image after three failed fetches.

A **`media-reuse`** finding (119) is written when a fingerprint attached to a narrative was first seen at least seven
days before the narrative began (`earlier`), or was attached to an earlier narrative in a different place (`elsewhere`:
both have regions and they do not overlap). It names both narratives, their dates and places, and emits `media.reuse`.

What a reuse finding **can** show: this picture, or one PDQ cannot tell from it, was posted earlier, or somewhere
else, among the posts Foresignal fingerprinted. What it **cannot** show: where or when the picture was taken, whether it
was edited, or that it is new when no match exists. Foresignal only knows media it has fingerprinted, since the store's
start; every finding and every match carries the `REFERENCE_WINDOW` caveat with that date. PDQ also matches near
copies of different scenes with the same composition (a city skyline at night), so read the earlier post before
drawing anything from a match.

- `GET /v1/social/media/{fingerprintId}` (read; 404 `UNKNOWN_FINGERPRINT`): the hash, quality, first sighting and
  post, uses (the latest 100), platforms, narratives and reuse findings.
- `POST /v1/social/media/match` (read, token required): a multipart `file` (checked by ImageGuard; `415
  IMAGE_REJECTED` for a type it refuses, `413` when it is too large; the upload is not stored) or `{"pdq": "…"}`, with
  `maxDistance` (0–63, default 31) and `limit`; MCP `match_media` takes a hash.

### Propagation, origin concentration and descent share

`PropagationTracer` 1.0.0 builds each narrative's graph whenever it is refreshed and keeps the full edge list for the
narrative's lifetime. Nodes are posts, linked 133 news articles and agency records that posts link; edges are
`repost`, `quote`, `reply`, `link`, `text-match` (to the member a post's text matched when it joined), `media-match`
and `cross-platform` (to each platform's first 20 posts from the earliest earlier post on another platform sharing a
URL, a fingerprint or near-identical text, MinHash Jaccard 0.8), each with its time delta.

The graph reports the **earliest observed post per platform**, the platforms in first-observed order, the **first news
pickup** (the earliest article of a linked story, with its delay after the first post; negative when the news came
first), the **median repost delay** and the **descent share**: the share of posts and articles reachable from the
members of the three largest evidence groups. Read the two shares together: a descent share near 1 with a high origin
concentration means the narrative is mostly amplification of a few reports, however many posts it has; a low descent
share means it grew from many independent starts. A graph of more than 500 nodes is summarized (`summarised: true`)
by collapsing each evidence group into one node with its member count and merging edges with counts; the response
gives the totals. Its provenance chain starts at the narrative.

### Coordination indicators and their limits

Every day at 05:10 UTC `CoordinationDetector` 1.1.0 looks at each narrative active in the last 24 hours (its latest
2,000 posts) and computes four statistics against null models:

- **synchrony**: the share of distinct authors who posted near-identical text (MinHash Jaccard 0.9) within 60 seconds
  of another author, against the same narrative with each author's posts shifted by one random offset within the
  window, wrapping past its end, 200 times (authors keep their own rhythm but stop lining up with each other);
- **text duplication**: the share of distinct authors in the largest near-duplicate cluster, against the day's other
  narratives (their median and robust spread);
- **account-age skew**: the share of authors whose account was created within 30 days of their post, where the
  platform provides creation dates, against the platform's share among authors of posts on the same topics over the
  last 30 days (exact binomial tail);
- **co-repost bursts**: the share of reposting authors in a pair that reposted the same five or more posts within 10
  minutes of each other, against a degree-preserving null (repost targets permuted across repost events, 200 times).

Each indicator gives its value, null expectation, p-value (from a normal approximation to the shuffled distribution,
so values smaller than one in 200 are representable), the Benjamini–Hochberg q-value across all of the day's
narratives and `available` (false when the platform gives no creation dates or there are too few authors or reposts).
When at least two indicators have q below 0.01, a **`social-coordination`** finding says "coordinated-amplification
indicators present" and `coordination.detected` is emitted; the narrative's `coordination` block and
`coordinationIndicatorsPresent` show it.

What the indicators **cannot** say: which accounts coordinated, whether they are automated, or who directed them. Real
events produce bursts too (many people posting their own words at once is not synchrony), shared templates and
quote-tweets of a viral post raise duplication, a platform's new users skew account ages, and fan communities repost
together. The finding is a reason to look at the narrative, never a verdict on any account, and Foresignal never
labels one.

### API, events and settings

- MCP: `list_narratives`, `get_narrative`, `get_claims` and `match_media` (read; also in the analyst's read catalog).
- SSE topic `social` of `/v1/events/stream` (cursor key `o`): `narrative.emerged`, `narrative.updated` (when a state,
  status or count crosses a band, or on a merge or split), `claim.extracted`, `media.reuse` and
  `coordination.detected`, next to 135's `social.velocity` and `social.platform.state`.
- AccessPolicy: the reads are read scope, image matching needs a token, merge and split are agent operations.
- Problem codes: `404 UNKNOWN_NARRATIVE`, `404 UNKNOWN_CLAIM`, `404 UNKNOWN_FINGERPRINT`, `415 IMAGE_REJECTED`.
- App settings: `social.keepMedia` (false), `social.llmClaimMinPosts` (50), `social.llmClaimsPerDay` (20) and
  `social.sourceReliability` (`{}`). Deployment property `foresignal.social.narratives-scheduled` (true; off in tests)
  runs the minute job (refreshing narratives that grew past 100 posts, which refresh in batches rather than on every
  post, and draining the image queue), the hourly state and LLM passes and the daily coordination pass.
- Tables (Hibernate, captured by prompt 144): `social_narrative`, `social_narrative_member`, `social_narrative_log`,
  `social_claim`, `media_fingerprint`, `media_use`, `social_media_queue` and `social_propagation_edge`. Narratives,
  claims and fingerprints are intelligence tables: they are kept when the posts behind them expire.

## Social versus physical (prompt 137)

Prompt 137 compares what people say with what Foresignal observes. Significant claims are tested against independent
physical evidence in the same place and window, with sensor coverage taken into account; significant physical events
that drew far less discussion than similar past events are reported; physical findings cue targeted, budgeted social
searches; and the predictive value of social signals is measured out of sample. Four rules hold throughout:

- **Two worlds, tracked separately.** A narrative never corroborates itself and never counts as an independent
  evidence group for the physical event it describes. Situations count narratives as social evidence groups, apart from
  their physical corroboration, and a hypothesis indicator is `observed` only from physical evidence.
- **Absence needs detectability.** "No corresponding change observed" is said only where the 116 `canSeeAbsence` check
  holds for the claim's family, place and window; otherwise the answer is `insufficient-coverage`. An attention gap
  likewise needs adequate social coverage of the region (135).
- **Lead times are measured, not assumed.** A kind of social signal is said to lead the physical world only from
  out-of-sample precursor tests (120) and ledger scores (122). A single case is an observed lead time, never
  predictive value.
- **No accusations.** Social activity before an event is reported as timing ("discussion accelerated 47 minutes before
  the first AIS anomaly"), never as foreknowledge.

### Comparisons and what their statuses mean

`SocialPhysicalComparator` 1.0.0 compares the claims of **significant narratives**: those with at least
`social.compare.minGroups` (3) evidence groups, or a `social-velocity` finding on one of their topics or places. A claim
is compared when it is asserted (denied and questioned claims are not), has a signal family other than `attention`, and
names a place of `point` or `locality` precision (`admin1` too for the large-area families: seismic, severe weather,
outages, internet measurement, GNSS interference, cyber and health).

For each such claim a **comparison** holds the claim's place and family, a **window** around the claim time (the
claim's own event time when it states one, else its earliest supporting post): 6 hours before to 24 hours after by
default, per family in `intel/social/social-compare.json` (earthquakes 2 h before and 12 h after, severe weather 12 h
and 36 h, health 7 and 14 days), the matching **physical evidence**, the **detectability**, the **lead times** and the
manifest. Physical evidence is every canonical event (116) of the family's canonical families and every non-social
finding (119) of its signal families, with a non-social originator, inside the window and within the family's distance
tolerance of the claim's place (conflict 25 km, fires 15 km, earthquakes 150 km, outages 75 km and so on; evidence
without a geometry, and evidence of a large-area family, match by region).

| Status | Means | Does not mean |
|---|---|---|
| `corroborated` | at least one non-social evidence group matches the claim in place and window | that every detail of the claim is right |
| `unobserved` | nothing matched, the window has closed, and coverage could have seen it (`canSeeAbsence` ok) | that the claim is false: sensors miss things, and a claim may be about something no family observes |
| `contradicted` | an agency or sensor record explicitly reports normal conditions for the place and time (the family's negating records, for example a status page reporting normal service), or the claim's image is a `media-reuse` finding | that anyone lied |
| `insufficient-coverage` | nothing matched and coverage could not have seen it | anything about the claim |
| `pending` | the window is still open and nothing matched yet | anything yet |

A narrative's status follows its comparisons: `pending` while none is final, the agreeing final status, otherwise
`mixed` (an `insufficient-coverage` claim does not split a narrative whose other claims agree); a claim's information
credibility follows its status (136). Comparisons are re-evaluated every 15 minutes as late evidence arrives, until the
window has closed plus the family's publication lag (24 hours by default, 7 days for conflict, 14 days for health);
every status change, and every change of the matching evidence, is a revision with its reason and manifest. A
corroborated comparison carries the signed lead of the first post over the first physical, official (agency) and news
observation, in seconds; positive means social came first.

| Call | What it does |
|---|---|
| `GET /v1/social/comparisons?status&situationId&narrativeId&claimId&family&region&since&cursor&limit` (read) | Comparisons, newest evaluation first |
| `GET /v1/social/comparisons/{id}` (read) | One comparison with every revision; 404 `UNKNOWN_COMPARISON` |

A comparison opened as `pending` from an organically found narrative also opens a binary **ledger question** (122),
category `social`: "Independent confirmation of {claim type} at {place} within {window}?". Its `manual` resolver is
the comparison itself: `corroborated` resolves it yes, `unobserved` or `contradicted` no, and `insufficient-coverage`
annuls it (`COVERAGE_GAP`). The **`social-claim`** forecaster 1.0.0 forecasts it when it opens, from the base rate of
corroboration among decided comparisons of the same claim type, platform mix, region and evidence-group band under a
Jeffreys Beta(½, ½) prior, backing off to coarser cells below five decided comparisons. Its scorecard shows how far
claims of each kind can be trusted. Cue-found claims open no question: they were found by looking for the event that
would confirm them.

### Attention gaps

`AttentionGapDetector` 1.0.0 runs hourly on `significant` non-social findings (or `notable`, with
`social.attentionGap.minSeverity`), situation escalations and critical alerts. Their **attention** is the posts
attributed to their region on the complete-stream platforms in the `windowHours` (6) after them, normalized by the
region's trailing 8-week baseline for those hours. The **expected attention** comes from past events of the same family
and severity band in the trailing `lookbackDays` (365): a log-normal fit when at least five exist, else the empirical
distribution of the region's past events of that family, with its median as the expectation. When the attention is
below the `percentile` (10th) of that distribution **and** social observability is ok for the region and window, an
`attention-gap` finding records the expected and observed posts and the platforms covered, and `attention.gap` is
emitted. Without adequate coverage nothing is recorded (the log says `LOW_SOCIAL_COVERAGE`): silence where nobody is
listening says nothing. An attention gap describes discussion, not importance.

### Cues and their costs

A **cue** is a targeted search around a physical event: the place's names and aliases (036 places, 040 regions, the
claim lexicon's aliases in other scripts), nearby facilities (104) and entities (098), the event terms of its families
from the claim lexicon in the region's languages (`intel/social/region-languages.json`; English always), and your extra
terms. One query per language and platform is run on the enabled search-capable platforms (`bluesky-search`, `reddit`,
`x`, `threads`), each in that platform's syntax; the cue records every exact query.

- **Automatic cues** (`social.cues.triggers`): a situation activated or escalated, a `significant` finding of a listed
  type (by default change points in outage, internet, vessel, flight and fire series), an anomaly, a critical alert,
  and a canonical event of a listed family with at least two evidence groups. A trigger is cued at most once a day.
- **Manual cues:** `POST /v1/social/cues` (agent, always authenticated) with a `placeId`, `regionCode`, `bbox`,
  `entityRef` or `situationId`, optional `terms`, `families` and `platforms`, a `window` and a `maxCostUsd`; it answers
  202 with the queued cue. `409 PLATFORM_DISABLED` when no requested platform is enabled and search-capable,
  `429 CUE_LIMIT_REACHED` (with `Retry-After`) at the daily limit, `429 BUDGET_EXCEEDED` when the cost cap or the X
  monthly cap leaves no room. MCP `run_social_cue`.
- `GET /v1/social/cues?status&since` and `GET /v1/social/cues/{id}` (read; 404 `UNKNOWN_CUE`) show the trigger, the
  queries, the status (`queued`, `running`, `done`, `failed`, `budget-refused`), the posts found per platform, the
  narratives they joined and the cost; `cue.completed` is emitted when a cue ends.

**Costs.** At most `social.cues.maxPerDay` (50) cues run per UTC day, automatic and manual together. Bluesky, Reddit and
Threads cost nothing but their own quotas (Threads' 2,200 queries a day included). X bills every post read: each X query
is estimated before it is sent (50 results × the post-read price, $0.25 at $0.005 a post), must fit the
cue's cap (`maxCostUsdPerCue`, $1.00) and the platform's monthly cap, and is charged from the answer. The optional
**`xai-x-search`** backend (`social.cues.xaiXSearch.enabled`, off by default) asks the 026 LLM provider with id `xai`
(an `openai-compatible` provider with base URL `https://api.x.ai/v1` and your xAI key) through `POST /responses` with
the `x_search` tool limited to the cue's dates; it keeps only the cited post links, never the model's text, spends the
provider's token budget and is billed per post fetched ($5 per 1,000 as of 2026-09-21).

**What cue results are.** The adapters store what a cue finds as posts with `collection = cue` and the cue id. They
join narratives and are compared like any posts, but they are a **targeted sample**: they are counted as sampled
collection, so they never feed baselines or region coverage, their comparisons record `foundBy: cue`, and lead-time
statistics keep them apart from organic narratives. **A cue never corroborates its own trigger**: a cue-found claim whose
only match is the event that cued the search carries the caveat `CUE_TRIGGER_MATCH`, opens no ledger question, and
nothing a cue finds is ever counted as evidence for the trigger. Cue-found narratives never raise cues of their own.

### Reading lead times

`GET /v1/social/lead-times?claimType&platform&region&foundBy&since` (read; MCP `get_social_lead_times`) groups the
corroborated comparisons by claim type, platform, region and `foundBy` and gives, per group, the median and
interquartile lead of the first post over physical, news and official first observation, with counts. The **training
cut-off** splits the comparisons by evaluation time at the 120 discovery fraction (two thirds): only comparisons
evaluated after it are out of sample, and only `outOfSampleOverPhysical` supports a statement that a kind of social
signal leads. Assessments state a lead only for groups with at least five out-of-sample comparisons.

Social volume series (135) are registered in the 119 analysis-series registry with family `social` and their region
(a topic's first region, a place's country), so the 120 miner pairs them, as antecedents only, with the physical series
of their region. Benjamini–Hochberg control and later-period validation apply unchanged, and survivors appear in the
precursor library with `family = social`.

### In analysis

- **Situations (118).** `GET /v1/situations/{id}` carries a `social` section: the top narratives linked to the
  situation (status, velocity, origin concentration, observed lead time, `foundBy`), their evidence groups counted
  apart from physical corroboration, the attention gap, and whether social coverage of its regions is adequate.
  Narratives link to situations by place, family and time: an open situation sharing a region (or its country) with
  the narrative's claims, carrying one of their signal families and active within a day of the narrative.
- **Hypotheses (125).** A compared claim marks a waiting indicator of the same family in a set linked to the
  narrative's situation (or sharing its region) `reported`: no probability moves, and the indicator keeps waiting. When
  the comparison is corroborated the indicator becomes `observed` from the matching physical evidence; when it is
  `unobserved` under adequate coverage the indicator becomes `absent`, weighted by the detection probability, so
  hypotheses that require the claimed event lose support.
- **Assessments (128).** Packets and assessments carry `social` ("What people are saying versus what we observe",
  section id `social-versus-observed`): narratives by status, notable narratives, attention gaps, out-of-sample lead
  times only, media reuse and coordination findings. The section is deterministic and passes the ICD 203 lint.
- **Chronicles (131).** Layer `social-narratives` (topics, places, regions, situations, statuses, `keepPostText`)
  writes `narrative-change` and `comparison-change` thread items whose snapshots keep the narrative, its claim
  summaries and the comparison, so the history survives the posts; post text is kept only with `keepPostText`. Layer
  `social-volume` (scopes, platforms, metrics) samples the `social:` series like computed series.
- **Notifications (018).** `social.divergence` (a comparison of a narrative at or above the velocity threshold became
  `unobserved` or `contradicted`) and `social.attention_gap`, off by default: add them to a channel's `events`.

### Settings, events and problem codes

`GET /v1/settings/social` and `PUT /v1/settings/social` (admin) read and replace one `SocialSettings` document over the
`social.*` app settings: `languages`, `minRegionBaseline`, `velocity`, `llmClaimMinPosts` (`llmClaimMinEvidenceGroups`
is fixed at 5), `keepMedia`, `trackRecord` (`minAssessed` 10, `gradeEShare` 0.7, `gradeDShare` 0.85, and the
overrides, keyed by platform and author key or channel), `compare` (`minGroups`, `defaultWindow`, `familyWindows` with
`before`, `after`, `toleranceKm` and `publicationLag`), `attentionGap` and `cues`. Members left out return to their
defaults; changes apply to the next evaluation, and final comparisons keep their status.

- SSE topic `social`: `comparison.updated`, `cue.completed` and `attention.gap`, next to the 135 and 136 events.
- MCP: `compare_claim` and `get_social_lead_times` (read; also in the analyst's read catalog), `run_social_cue`
  (agent, cost-bearing).
- Problem codes: `404 UNKNOWN_COMPARISON`, `404 UNKNOWN_CUE`, `429 CUE_LIMIT_REACHED`, `429 BUDGET_EXCEEDED`,
  `409 PLATFORM_DISABLED`.
- Deployment properties `foresignal.social.correlation-scheduled` (true; off in tests: the 15-minute comparisons and
  cue passes, the hourly attention-gap and canonical cue passes, and the cue triggers on the situation, finding and
  alert buses) and `foresignal.social.cues-async` (true: a manual cue runs at once on a virtual thread).
- Tables (Hibernate, captured by prompt 144): `social_comparison`, `social_comparison_revision` and `social_cue`;
  assessments gain the column `social`.
- The benchmark component `social-physical` covers six scenarios: a planted narrative with no physical event under
  adequate and thin coverage, a planted event with organic discussion and with silence, a reused image, and a cue loop.
