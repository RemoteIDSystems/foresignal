/// Cursor iterators (`Stream`s) over Foresignal's paged operations. Every listing that takes `cursor` answers
/// `{items, nextCursor}` (two older ones say `next`); `GET /v1/changes` pages by seq (`after`, `lastSeq`,
/// `hasMore`) and refuses a cursor below the purge horizon with `410 RESYNC_REQUIRED`, surfaced as
/// [ResyncRequiredException].
library;

import '../errors.dart';
import '../http.dart';

export '../errors.dart' show ResyncRequiredException;

/// Fetches one page given the cursor (null for the first page).
typedef PageFetcher = Future<Map<String, Object?>> Function(String? cursor);

/// The next cursor of a page (`nextCursor`, else `next`); null on the last page.
String? nextCursorOf(Map<String, Object?> page) {
  final cursor = page['nextCursor'] ?? page['next'];
  return cursor is String && cursor.isNotEmpty ? cursor : null;
}

/// Pages from any fetcher (for example a generated-client call), following the cursor.
Stream<Map<String, Object?>> paginatePagesWith(PageFetcher fetchPage, {int? maxPages}) async* {
  String? cursor;
  var pages = 0;
  do {
    final page = await fetchPage(cursor);
    pages++;
    yield page;
    cursor = nextCursorOf(page);
  } while (cursor != null && (maxPages == null || pages < maxPages));
}

/// The items of every page from any fetcher.
Stream<Object?> paginateWith(PageFetcher fetchPage, {int? maxPages}) async* {
  await for (final page in paginatePagesWith(fetchPage, maxPages: maxPages)) {
    for (final item in (page['items'] as List?) ?? const []) {
      yield item;
    }
  }
}

/// The pages of a cursor-paged `GET` operation, e.g. `paginatePages(t, '/v1/alerts', query: {'status': 'firing'})`.
Stream<Map<String, Object?>> paginatePages(HttpTransport transport, String path,
    {Map<String, Object?>? query, int? pageSize, int? maxPages}) {
  final base = <String, Object?>{...?query, 'limit': ?pageSize};
  return paginatePagesWith((cursor) async {
    final response = await transport.request('GET', path, query: cursor == null ? base : {...base, 'cursor': cursor});
    return response.json;
  }, maxPages: maxPages);
}

/// The items of a cursor-paged `GET` operation.
Stream<Object?> paginate(HttpTransport transport, String path,
    {Map<String, Object?>? query, int? pageSize, int? maxPages}) async* {
  await for (final page in paginatePages(transport, path, query: query, pageSize: pageSize, maxPages: maxPages)) {
    for (final item in (page['items'] as List?) ?? const []) {
      yield item;
    }
  }
}

/// The changes of `GET /v1/changes` from [after] until `hasMore` is false; [onPage] receives each page (with its
/// `lastSeq`, the cursor to persist).
///
/// The stream fails with [ResyncRequiredException] when [after] is below the purge horizon.
Stream<Map<String, Object?>> iterateChanges(HttpTransport transport,
    {int after = 0, Map<String, Object?>? filters, int? limit, void Function(Map<String, Object?> page)? onPage}) async* {
  var cursor = after;
  while (true) {
    final response = await transport.request('GET', '/v1/changes',
        query: {...?filters, 'after': cursor, 'limit': ?limit});
    final page = response.json;
    onPage?.call(page);
    for (final item in (page['items'] as List?) ?? const []) {
      if (item is Map<String, Object?>) {
        yield item;
      }
    }
    final last = page['lastSeq'];
    if (last is int) {
      cursor = last;
    }
    if (page['hasMore'] != true) {
      return;
    }
  }
}
