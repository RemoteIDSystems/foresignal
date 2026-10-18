/// Deep links (docs/Client-Contract.md §6): a context travels as `{"v":1,"ws":…,"ctx":{…}}`, compact JSON with
/// the context's null members left out, base64url-encoded without padding, at most 2,048 bytes, carried in a
/// `#fs=` URL fragment. Encoding gives the same payload as the server's `ContextLinks`; decoding checks the
/// payload version and validates the context.
library;

import 'dart:convert';

import '../errors.dart';
import 'context.dart';
import 'envelope.dart';

/// Largest encoded payload, in bytes.
const maxDeepLinkBytes = 2048;

/// The URL fragment prefix.
const deepLinkFragment = '#fs=';

final _base64Url = RegExp(r'^[A-Za-z0-9_-]*$');

/// An encoded deep link (the shape of the server's `ContextLink`).
class EncodedDeepLink {
  /// Creates the link.
  const EncodedDeepLink(this.version, this.workspaceId, this.payload, this.fragment, this.bytes);

  /// The payload version.
  final int version;

  /// The workspace, or null for an ad-hoc context.
  final String? workspaceId;

  /// The base64url payload.
  final String payload;

  /// `#fs=` + payload.
  final String fragment;

  /// The payload length in bytes.
  final int bytes;
}

/// A decoded deep link.
class DecodedDeepLink {
  /// Creates the link.
  const DecodedDeepLink(this.version, this.workspaceId, this.context);

  /// The payload version.
  final int version;

  /// The workspace, or null for an ad-hoc context.
  final String? workspaceId;

  /// The context.
  final Map<String, Object?> context;
}

/// Encodes a context as a deep link; throws [DeepLinkException] (`context` or `tooLarge`).
EncodedDeepLink encodeDeepLink(String? workspaceId, Map<String, Object?> context) {
  final problems = contextProblems(context);
  if (problems.isNotEmpty) {
    throw DeepLinkException(DeepLinkErrorKind.context, problems.join('; '));
  }
  final json = jsonEncode({'v': deepLinkVersion, 'ws': workspaceId, 'ctx': stripNullMembers(context)});
  final payload = base64Url.encode(utf8.encode(json)).replaceAll('=', '');
  if (payload.length > maxDeepLinkBytes) {
    throw DeepLinkException(DeepLinkErrorKind.tooLarge,
        'The encoded context is ${payload.length} bytes; a deep link holds at most $maxDeepLinkBytes.');
  }
  return EncodedDeepLink(deepLinkVersion, workspaceId, payload, '$deepLinkFragment$payload', payload.length);
}

/// Decodes a payload, with or without the `#fs=` prefix (a full URL with that fragment works too); throws
/// [DeepLinkException] (`malformed`, `version`, `context` or `tooLarge`).
DecodedDeepLink decodeDeepLink(String payloadOrFragment) {
  var text = payloadOrFragment;
  final at = text.indexOf(deepLinkFragment);
  if (at >= 0) {
    text = text.substring(at + deepLinkFragment.length);
  }
  if (text.trim().isEmpty) {
    throw DeepLinkException(DeepLinkErrorKind.malformed, 'The deep link payload is empty.');
  }
  if (text.length > maxDeepLinkBytes) {
    throw DeepLinkException(DeepLinkErrorKind.tooLarge, 'The payload is longer than $maxDeepLinkBytes bytes.');
  }
  Object? doc;
  try {
    if (!_base64Url.hasMatch(text) || text.length % 4 == 1) {
      throw const FormatException('not base64url');
    }
    doc = jsonDecode(utf8.decode(base64Url.decode(base64Url.normalize(text))));
  } on FormatException {
    throw DeepLinkException(DeepLinkErrorKind.malformed, 'The deep link payload is not base64url JSON.');
  }
  if (doc is! Map<String, Object?> || doc['v'] is! int) {
    throw DeepLinkException(DeepLinkErrorKind.malformed, 'The deep link payload must be {"v":1,"ws":…,"ctx":{…}}.');
  }
  final version = doc['v'] as int;
  if (version != deepLinkVersion) {
    throw DeepLinkException(
        DeepLinkErrorKind.version, 'Unsupported deep link version $version (this SDK reads $deepLinkVersion).');
  }
  final ctx = doc['ctx'] ?? <String, Object?>{};
  final problems = contextProblems(ctx, prefix: 'ctx');
  if (problems.isNotEmpty) {
    throw DeepLinkException(DeepLinkErrorKind.context, problems.join('; '));
  }
  final ws = doc['ws'];
  return DecodedDeepLink(version, ws is String ? ws : null, Map<String, Object?>.from(ctx as Map));
}
