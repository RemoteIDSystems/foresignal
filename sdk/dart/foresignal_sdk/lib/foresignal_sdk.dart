/// Foresignal Dart SDK, hand-written layer (prompt 141): the resuming `ChangeStream`, cursor streams over paged
/// operations, and the client-contract helpers (`ObjectRef`, envelopes, `resolveObjects`, `WorkspaceSync`,
/// deep links). It uses dio only, so it works alongside the generated `foresignal_api` package (share one `Dio`)
/// or on its own.
library;

export 'src/contract/context.dart';
export 'src/contract/deep_link.dart';
export 'src/contract/envelope.dart';
export 'src/contract/object_ref.dart';
export 'src/contract/resolve.dart';
export 'src/contract/workspace_sync.dart';
export 'src/errors.dart';
export 'src/http.dart';
export 'src/paging/paginate.dart';
export 'src/realtime/backoff.dart';
export 'src/realtime/change_stream.dart';
export 'src/realtime/sse.dart';
