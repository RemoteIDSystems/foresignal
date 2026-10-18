/// Error types of the hand-written SDK layer.
library;

import 'dart:convert';

/// A non-2xx answer from the Foresignal API.
class ApiException implements Exception {
  /// Creates the exception from the HTTP status, the problem document and a `Retry-After` delay.
  ApiException(this.status, this.problem, {this.retryAfter, String? message})
      : code = problem?['code'] is String ? problem!['code'] as String : null,
        message = message ??
            (problem?['detail'] as String?) ??
            (problem?['title'] as String?) ??
            'HTTP $status';

  /// HTTP status.
  final int status;

  /// The problem's `code`, when the body was a problem document.
  final String? code;

  /// The parsed problem document, when there was one.
  final Map<String, Object?>? problem;

  /// The `Retry-After` delay, when present.
  final Duration? retryAfter;

  /// A human-readable message.
  final String message;

  @override
  String toString() => 'ApiException($status${code == null ? '' : ' $code'}): $message';
}

/// `410 RESYNC_REQUIRED` from `GET /v1/changes`: the cursor is below the purge horizon. Re-snapshot with
/// `/v1/records?order=id` and continue from its `watermarkSeq`.
class ResyncRequiredException extends ApiException {
  /// Creates the exception from the 410 problem document.
  ResyncRequiredException(Map<String, Object?>? problem, {Duration? retryAfter})
      : purgeHorizon = problem?['purgeHorizon'] is int ? problem!['purgeHorizon'] as int : null,
        watermarkSeq = problem?['watermarkSeq'] is int ? problem!['watermarkSeq'] as int : null,
        super(410, problem,
            retryAfter: retryAfter,
            message: (problem?['detail'] as String?) ?? 'Resync required: the cursor is below the purge horizon');

  /// The purge horizon seq the cursor fell below.
  final int? purgeHorizon;

  /// The watermark at the time of the refusal.
  final int? watermarkSeq;
}

/// A malformed object reference (docs/Client-Contract.md §2).
class ObjectRefException implements Exception {
  /// Creates the exception for [ref].
  ObjectRefException(this.ref, this.reason);

  /// The rejected text.
  final String ref;

  /// Why it was rejected.
  final String reason;

  @override
  String toString() {
    final shown = ref.length > 80 ? '${ref.substring(0, 80)}…' : ref;
    return 'ObjectRefException: invalid object reference "$shown": $reason';
  }
}

/// One problem found in a workspace context.
class ContextFieldError {
  /// Creates the error.
  const ContextFieldError(this.field, this.message);

  /// The offending member, e.g. `context.viewport.bbox`.
  final String field;

  /// What is wrong with it.
  final String message;

  @override
  String toString() => '$field $message';
}

/// A workspace context that breaks the schema version 1 rules (docs/Client-Contract.md §5).
class ContextValidationException implements Exception {
  /// Creates the exception from its field errors.
  ContextValidationException(this.errors);

  /// Every problem found.
  final List<ContextFieldError> errors;

  @override
  String toString() => 'ContextValidationException: ${errors.join('; ')}';
}

/// Why a deep link was refused.
enum DeepLinkErrorKind {
  /// An unsupported payload version.
  version,

  /// Not base64url JSON of the expected shape.
  malformed,

  /// The context breaks the schema rules.
  context,

  /// Over 2,048 bytes.
  tooLarge,
}

/// A deep link that cannot be encoded or decoded (docs/Client-Contract.md §6).
class DeepLinkException implements Exception {
  /// Creates the exception.
  DeepLinkException(this.kind, this.message);

  /// The kind of failure.
  final DeepLinkErrorKind kind;

  /// A human-readable message.
  final String message;

  @override
  String toString() => 'DeepLinkException(${kind.name}): $message';
}

/// No event or heartbeat arrived within the watchdog timeout.
class HeartbeatTimeoutException implements Exception {
  /// Creates the exception.
  HeartbeatTimeoutException(this.timeout);

  /// The watchdog timeout.
  final Duration timeout;

  @override
  String toString() => 'HeartbeatTimeoutException: no event or heartbeat within ${timeout.inMilliseconds} ms';
}

/// Parses a `Retry-After` header (delta seconds or an HTTP date).
Duration? parseRetryAfter(String? value, {DateTime? now}) {
  if (value == null || value.trim().isEmpty) {
    return null;
  }
  final text = value.trim();
  final seconds = int.tryParse(text);
  if (seconds != null && seconds >= 0) {
    return Duration(seconds: seconds);
  }
  try {
    final at = _parseHttpDate(text);
    final delta = at.difference(now ?? DateTime.now().toUtc());
    return delta.isNegative ? Duration.zero : delta;
  } on FormatException {
    return null;
  }
}

const _months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

/// Parses an IMF-fixdate (`Sun, 06 Nov 1994 08:49:37 GMT`).
DateTime _parseHttpDate(String text) {
  final m = RegExp(r'^[A-Za-z]{3}, (\d{2}) ([A-Za-z]{3}) (\d{4}) (\d{2}):(\d{2}):(\d{2}) GMT$').firstMatch(text);
  if (m == null) {
    throw FormatException('not an HTTP date', text);
  }
  final month = _months.indexOf(m.group(2)!) + 1;
  if (month == 0) {
    throw FormatException('not an HTTP date', text);
  }
  return DateTime.utc(int.parse(m.group(3)!), month, int.parse(m.group(1)!), int.parse(m.group(4)!),
      int.parse(m.group(5)!), int.parse(m.group(6)!));
}

/// Builds the exception for a non-2xx response body: [ResyncRequiredException] for `410 RESYNC_REQUIRED`,
/// else [ApiException].
ApiException apiExceptionFrom(int status, String? body, String? retryAfterHeader) {
  Map<String, Object?>? problem;
  if (body != null && body.isNotEmpty) {
    try {
      final parsed = jsonDecode(body);
      if (parsed is Map<String, Object?>) {
        problem = parsed;
      }
    } on FormatException {
      problem = null;
    }
  }
  final retryAfter = parseRetryAfter(retryAfterHeader);
  if (status == 410 && problem?['code'] == 'RESYNC_REQUIRED') {
    return ResyncRequiredException(problem, retryAfter: retryAfter);
  }
  return ApiException(status, problem, retryAfter: retryAfter);
}
