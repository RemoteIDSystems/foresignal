/// Object references `{type}:{id}` (docs/Client-Contract.md §2), parsed and formatted with the same rules as the
/// server's `ObjectRef`: record ids are `{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded
/// (`/` → `%2F`, `@` → `%40`, `%` → `%25`, whitespace and control characters as their UTF-8 bytes), sensor ids
/// are `{kind}:{id}`, remote ids are `{instanceId}/{type}:{id}`. Ids are otherwise opaque and references compare
/// as strings. Types are not checked against a fixed list; pass `knownTypes` (from `GET /v1/contract`) to
/// reject types the server does not know.
library;

import 'dart:convert';

import '../errors.dart';

/// Longest accepted reference, in characters.
const maxObjectRefLength = 1024;

/// Sensor kinds accepted in `sensor:{kind}:{id}`.
const sensorKinds = ['source', 'receiver', 'station', 'satellite', 'scene', 'raster-product'];

final _typePattern = RegExp(r'^[a-z][a-z-]*$');
final _hex2 = RegExp(r'^[0-9A-Fa-f]{2}$');

/// The parts of a `record:` reference.
class RecordRefParts {
  /// Creates the parts.
  const RecordRefParts(this.sourceId, this.externalId, this.seq);

  /// The source id.
  final String sourceId;

  /// The decoded external id.
  final String externalId;

  /// The pinned version, or null for the current one.
  final int? seq;
}

/// A parsed reference.
class ObjectRef {
  const ObjectRef._(this.ref, this.type, this.id,
      {this.record, this.sensorKind, this.sensorId, this.remoteInstanceId, this.remoteRef});

  /// Parses [text]; throws [ObjectRefException] when it is malformed.
  factory ObjectRef.parse(String text, {List<String>? knownTypes}) => parseObjectRef(text, knownTypes: knownTypes);

  /// The canonical text `{type}:{id}`.
  final String ref;

  /// The object type.
  final String type;

  /// The id exactly as it appears after the first colon.
  final String id;

  /// Present for `record:` references.
  final RecordRefParts? record;

  /// The sensor kind of a `sensor:` reference.
  final String? sensorKind;

  /// The kind-specific id of a `sensor:` reference.
  final String? sensorId;

  /// The sending instance of a `remote:` reference.
  final String? remoteInstanceId;

  /// The reference on the sending instance of a `remote:` reference.
  final String? remoteRef;

  @override
  String toString() => ref;

  @override
  bool operator ==(Object other) => other is ObjectRef && other.ref == ref;

  @override
  int get hashCode => ref.hashCode;
}

/// Whitespace as Java's `Character.isWhitespace` defines it (the server's rule).
bool _isJavaWhitespace(int cp) =>
    (cp >= 0x09 && cp <= 0x0D) ||
    (cp >= 0x1C && cp <= 0x20) ||
    cp == 0x1680 ||
    (cp >= 0x2000 && cp <= 0x2006) ||
    (cp >= 0x2008 && cp <= 0x200A) ||
    cp == 0x2028 ||
    cp == 0x2029 ||
    cp == 0x205F ||
    cp == 0x3000;

bool _isControl(int cp) => cp <= 0x1F || (cp >= 0x7F && cp <= 0x9F);

/// Percent-encodes `%`, `/`, `@`, whitespace and control characters of an external id.
String encodeExternalId(String externalId) {
  final out = StringBuffer();
  for (final cp in externalId.runes) {
    final ch = String.fromCharCode(cp);
    if (ch == '%' || ch == '/' || ch == '@' || _isJavaWhitespace(cp) || _isControl(cp)) {
      for (final b in utf8.encode(ch)) {
        out.write('%${b.toRadixString(16).toUpperCase().padLeft(2, '0')}');
      }
    } else {
      out.write(ch);
    }
  }
  return out.toString();
}

/// Decodes a percent-encoded external id (UTF-8); throws [FormatException] on a bad escape.
String decodeExternalId(String encoded) {
  if (!encoded.contains('%')) {
    return encoded;
  }
  final bytes = <int>[];
  var i = 0;
  while (i < encoded.length) {
    final c = encoded[i];
    if (c == '%') {
      final hex = encoded.length >= i + 3 ? encoded.substring(i + 1, i + 3) : encoded.substring(i + 1);
      if (hex.length < 2 || !_hex2.hasMatch(hex)) {
        throw FormatException(hex.length < 2 ? 'truncated escape' : 'bad escape', encoded, i);
      }
      bytes.add(int.parse(hex, radix: 16));
      i += 3;
    } else {
      final cp = encoded.codeUnitAt(i);
      if (cp >= 0xD800 && cp <= 0xDBFF && i + 1 < encoded.length) {
        bytes.addAll(utf8.encode(encoded.substring(i, i + 2)));
        i += 2;
      } else {
        bytes.addAll(utf8.encode(c));
        i += 1;
      }
    }
  }
  return utf8.decode(bytes);
}

/// Parses a reference; throws [ObjectRefException] when it is malformed. [knownTypes] (from `GET /v1/contract`)
/// rejects types the server does not know.
ObjectRef parseObjectRef(String text, {List<String>? knownTypes}) {
  if (text.trim().isEmpty) {
    throw ObjectRefException(text, 'is required');
  }
  if (text.length > maxObjectRefLength) {
    throw ObjectRefException(text, 'is longer than $maxObjectRefLength characters');
  }
  final colon = text.indexOf(':');
  if (colon <= 0 || colon == text.length - 1) {
    throw ObjectRefException(text, 'must be {type}:{id}');
  }
  final type = text.substring(0, colon);
  final id = text.substring(colon + 1);
  if (!_typePattern.hasMatch(type)) {
    throw ObjectRefException(text, "has an invalid object type '$type'");
  }
  if (knownTypes != null && !knownTypes.contains(type)) {
    throw ObjectRefException(text, "has unknown object type '$type'");
  }
  for (final cp in id.runes) {
    if (_isJavaWhitespace(cp) || _isControl(cp)) {
      throw ObjectRefException(text, 'must not contain whitespace or control characters');
    }
  }
  switch (type) {
    case 'record':
      return ObjectRef._(text, type, id, record: _parseRecordId(text, id));
    case 'sensor':
      final c = id.indexOf(':');
      if (c <= 0 || c == id.length - 1 || !sensorKinds.contains(id.substring(0, c))) {
        throw ObjectRefException(text, 'sensor refs are sensor:{kind}:{id} with kind one of ${sensorKinds.join(', ')}');
      }
      return ObjectRef._(text, type, id, sensorKind: id.substring(0, c), sensorId: id.substring(c + 1));
    case 'remote':
      final slash = id.indexOf('/');
      final inner = slash > 0 ? id.substring(slash + 1) : '';
      final innerColon = inner.indexOf(':');
      if (slash <= 0 || inner.isEmpty || innerColon <= 0 || innerColon == inner.length - 1) {
        throw ObjectRefException(text, 'remote refs are remote:{instanceId}/{type}:{id}');
      }
      final innerType = inner.substring(0, innerColon);
      if (!_typePattern.hasMatch(innerType) || innerType == 'remote') {
        throw ObjectRefException(text, 'a remote ref names an object type other than remote');
      }
      return ObjectRef._(text, type, id, remoteInstanceId: id.substring(0, slash), remoteRef: inner);
    default:
      return ObjectRef._(text, type, id);
  }
}

RecordRefParts _parseRecordId(String text, String id) {
  final slash = id.indexOf('/');
  if (slash <= 0 || slash == id.length - 1) {
    throw ObjectRefException(text, 'record refs are record:{sourceId}/{externalId}[@{seq}]');
  }
  final rest = id.substring(slash + 1);
  final at = rest.indexOf('@');
  if (at == 0 || (at > 0 && rest.indexOf('@', at + 1) >= 0)) {
    throw ObjectRefException(text, "a record ref has at most one '@' (encode '@' in the external id as %40)");
  }
  int? seq;
  if (at > 0) {
    final digits = rest.substring(at + 1);
    if (digits.isEmpty || digits.length > 19 || !RegExp(r'^\d+$').hasMatch(digits)) {
      throw ObjectRefException(text, "the version after '@' must be a non-negative integer seq");
    }
    seq = int.tryParse(digits);
    if (seq == null) {
      throw ObjectRefException(text, "the version after '@' must be a non-negative integer seq");
    }
  }
  final String externalId;
  try {
    externalId = decodeExternalId(at < 0 ? rest : rest.substring(0, at));
  } on FormatException {
    throw ObjectRefException(text, 'has a malformed percent-encoding in the external id');
  }
  return RecordRefParts(id.substring(0, slash), externalId, seq);
}

/// True when [text] parses.
bool isValidObjectRef(String text, {List<String>? knownTypes}) {
  try {
    parseObjectRef(text, knownTypes: knownTypes);
    return true;
  } on ObjectRefException {
    return false;
  }
}

/// Formats `{type}:{id}` (validated).
String formatObjectRef(String type, String id) => parseObjectRef('$type:$id').ref;

/// Formats a record reference from its parts, encoding the external id.
String recordRef(String sourceId, String externalId, [int? seq]) =>
    parseObjectRef('record:$sourceId/${encodeExternalId(externalId)}${seq == null ? '' : '@$seq'}').ref;

/// Formats a remote reference `remote:{instanceId}/{ref}`.
String remoteRef(String instanceId, String ref) => parseObjectRef('remote:$instanceId/$ref').ref;

/// Encodes a reference as one URL path segment: `%` → `%25`, `/` → `%2F`, `#` → `%23`, `?` → `%3F`.
String objectRefPathSegment(String ref) =>
    ref.replaceAll('%', '%25').replaceAll('/', '%2F').replaceAll('#', '%23').replaceAll('?', '%3F');
