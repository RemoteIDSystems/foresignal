# foresignal_sdk

Hand-written Dart helpers for the Foresignal API, with the same semantics as the TypeScript and Python SDKs:

- `ChangeStream` — resuming reader of `/v1/changes/stream` and `/v1/events/stream`: `Last-Event-ID` resume (or
  stream tickets with `useTicket: true`), snapshot paging on `reset {resync_required}` and `resync`, the
  `observedAt` guard for `live`/`touch`, a heartbeat watchdog (2.5 × the interval) and jittered backoff
  (1 s → 60 s) that honors `retry:` and `Retry-After`;
- cursor `Stream`s over paged operations (`paginate`, `paginatePages`, `paginateWith`) and `iterateChanges`,
  which fails with `ResyncRequiredException` on `410 RESYNC_REQUIRED`;
- client contract: `ObjectRef` (`parseObjectRef`, `recordRef`, `objectRefPathSegment`), `ObjectEnvelope`,
  `resolveObjects` (batches of at most 500), `contextProblems` and `applyMergePatch`, deep links
  (`encodeDeepLink`, `decodeDeepLink`) and `WorkspaceSync`.

It depends only on `dio` (the client the generated [`foresignal_api`](../foresignal_api) uses) and works with or
without the generated package.

## Install

```yaml
dependencies:
  foresignal_sdk:
    path: ../foresignal/sdk/dart/foresignal_sdk        # or, from git:
  # foresignal_sdk:
  #   git:
  #     url: https://gitlab.com/adam.allard/foresignal.git
  #     path: sdk/dart/foresignal_sdk
```

## Use

```dart
import 'package:foresignal_sdk/foresignal_sdk.dart';

final transport = HttpTransport(baseUrl: 'https://foresignal.example.org', token: 'fs_…');
final stream = ChangeStream(transport: transport, filters: {'kind': ['aircraft'], 'touches': true});
await for (final signal in stream.signals()) {
  switch (signal.type) {
    case SignalType.change || SignalType.live || SignalType.touch:
      apply(signal.json);
    case SignalType.reset:
      clearLocalState();
    case SignalType.snapshot:
      signal.items!.forEach(applyRecord);
    default:
      break;
  }
}

final sync = WorkspaceSync(transport: transport, workspaceId: '01JAB…', clientKind: 'map');
sync.changes.listen((change) => render(change.context));
await sync.start();
await sync.updateWith((ctx) => {'focusRef': 'situation:01JA8…'});
```

## Tests

`dart test` (an in-process `HttpServer` plays the SSE server; the contract tests read
[`sdk/contract-fixtures`](../../contract-fixtures)).

## Versioning and publishing

The package version is the API's `info.version` (`1.5.0`); dependency versions are pinned in
[`sdk/versions.properties`](../../versions.properties). Publishing is a separate release step (`publish_to: none`).
