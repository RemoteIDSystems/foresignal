/// The object envelope (docs/Client-Contract.md §3), resolve results and contract versions, written by hand so
/// the contract helpers do not depend on the generated types.
library;

/// The context schema version this SDK writes and validates.
const contextSchemaVersion = 1;

/// The deep link payload version this SDK encodes and decodes.
const deepLinkVersion = 1;

/// The contract major version this SDK was built for (within `/v1` changes are additive).
const contractMajorVersion = 1;

/// The uniform summary of any Foresignal object; unknown members stay available in [json].
class ObjectEnvelope {
  /// Wraps a decoded envelope.
  ObjectEnvelope(this.json);

  /// The envelope as decoded JSON.
  final Map<String, Object?> json;

  /// The canonical reference.
  String get ref => json['ref'] as String;

  /// The object type.
  String get type => json['type'] as String;

  /// The title.
  String get title => json['title'] as String;

  /// At most 280 characters, or null.
  String? get summary => json['summary'] as String?;

  /// `deterministic` or `llm`.
  String? get textOrigin => json['textOrigin'] as String?;

  /// The type-specific status, or null.
  String? get status => json['status'] as String?;

  /// The severity, or null.
  String? get severity => json['severity'] as String?;

  /// `{start, end, observedAt}`.
  Map<String, Object?>? get time => json['time'] as Map<String, Object?>?;

  /// A GeoJSON geometry (simplified to at most 500 vertices), or null.
  Map<String, Object?>? get geometry => json['geometry'] as Map<String, Object?>?;

  /// The unsimplified extent `[minLon, minLat, maxLon, maxLat]`, or null.
  List<num>? get bbox => (json['bbox'] as List?)?.cast<num>();

  /// Whether the object is private.
  bool get isPrivate => json['private'] == true;

  /// The TLP label of a work product, or null.
  String? get tlp => json['tlp'] as String?;

  /// The caller's visible tag names.
  List<String> get tags => ((json['tags'] as List?) ?? const []).cast<String>();

  /// `{sourceIds, manifestId, asOfSeq}`.
  Map<String, Object?>? get provenance => json['provenance'] as Map<String, Object?>?;

  /// `{self, related}`.
  Map<String, Object?>? get links => json['links'] as Map<String, Object?>?;

  /// What the calling token may do with the object.
  List<Map<String, Object?>> get actions =>
      ((json['actions'] as List?) ?? const []).cast<Map<String, Object?>>();
}

/// Envelopes for the resolvable refs, and the refs that are missing or invisible.
class ResolveResult {
  /// Creates the result.
  ResolveResult(this.items, this.missing, this.asOf);

  /// The envelopes, in request order.
  final List<ObjectEnvelope> items;

  /// The refs that do not exist or are invisible to the caller.
  final List<String> missing;

  /// The echo of `asOf`, or null.
  final String? asOf;
}

/// The reasons a server's `GET /v1/contract` answer is not one this SDK understands (empty when compatible).
List<String> contractProblems(Map<String, Object?> contract) {
  final problems = <String>[];
  final version = '${contract['contractVersion']}';
  final major = int.tryParse(version.split('.').first);
  if (major != contractMajorVersion) {
    problems.add('contract version $version (SDK supports $contractMajorVersion.x)');
  }
  if (contract['contextSchemaVersion'] != contextSchemaVersion) {
    problems.add('context schema version ${contract['contextSchemaVersion']} (SDK supports $contextSchemaVersion)');
  }
  if (contract['deepLinkVersion'] != deepLinkVersion) {
    problems.add('deep link version ${contract['deepLinkVersion']} (SDK supports $deepLinkVersion)');
  }
  return problems;
}
