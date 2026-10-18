// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationCWProxy {
  Situation id(String id);

  Situation title(String title);

  Situation theme(SituationTheme theme);

  Situation status(SituationStatus status);

  Situation severity(Severity severity);

  Situation severityOverridden(bool? severityOverridden);

  Situation regionCodes(List<String> regionCodes);

  Situation bbox(List<num>? bbox);

  Situation centroid(Geometry? centroid);

  Situation startedAt(DateTime startedAt);

  Situation lastActivityAt(DateTime lastActivityAt);

  Situation closedAt(DateTime? closedAt);

  Situation memberCount(int memberCount);

  Situation signalFamilies(List<String> signalFamilies);

  Situation corroborationShare(num? corroborationShare);

  Situation summary(String summary);

  Situation origin(SituationOriginEnum origin);

  Situation mergedInto(String? mergedInto);

  Situation private(bool private);

  Situation version(int version);

  Situation updatedAt(DateTime? updatedAt);

  Situation severityOverride(Severity? severityOverride);

  Situation geometry(Geometry? geometry);

  Situation topicIds(List<String>? topicIds);

  Situation entityIds(List<String>? entityIds);

  Situation investigationIds(List<String>? investigationIds);

  Situation linkerVersion(String? linkerVersion);

  Situation createdAt(DateTime createdAt);

  Situation evidence(SituationEvidence evidence);

  Situation inference(SituationInference inference);

  Situation speculation(SituationSpeculation speculation);

  Situation caveats(List<Caveat>? caveats);

  Situation coverage(CoverageStats? coverage);

  Situation social(SituationSocialSection? social);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Situation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Situation(...).copyWith(id: 12, name: "My name")
  /// ```
  Situation call({
    String id,
    String title,
    SituationTheme theme,
    SituationStatus status,
    Severity severity,
    bool? severityOverridden,
    List<String> regionCodes,
    List<num>? bbox,
    Geometry? centroid,
    DateTime startedAt,
    DateTime lastActivityAt,
    DateTime? closedAt,
    int memberCount,
    List<String> signalFamilies,
    num? corroborationShare,
    String summary,
    SituationOriginEnum origin,
    String? mergedInto,
    bool private,
    int version,
    DateTime? updatedAt,
    Severity? severityOverride,
    Geometry? geometry,
    List<String>? topicIds,
    List<String>? entityIds,
    List<String>? investigationIds,
    String? linkerVersion,
    DateTime createdAt,
    SituationEvidence evidence,
    SituationInference inference,
    SituationSpeculation speculation,
    List<Caveat>? caveats,
    CoverageStats? coverage,
    SituationSocialSection? social,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituation.copyWith(...)` or call `instanceOfSituation.copyWith.fieldName(value)` for a single field.
class _$SituationCWProxyImpl implements _$SituationCWProxy {
  const _$SituationCWProxyImpl(this._value);

  final Situation _value;

  @override
  Situation id(String id) => call(id: id);

  @override
  Situation title(String title) => call(title: title);

  @override
  Situation theme(SituationTheme theme) => call(theme: theme);

  @override
  Situation status(SituationStatus status) => call(status: status);

  @override
  Situation severity(Severity severity) => call(severity: severity);

  @override
  Situation severityOverridden(bool? severityOverridden) =>
      call(severityOverridden: severityOverridden);

  @override
  Situation regionCodes(List<String> regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  Situation bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  Situation centroid(Geometry? centroid) => call(centroid: centroid);

  @override
  Situation startedAt(DateTime startedAt) => call(startedAt: startedAt);

  @override
  Situation lastActivityAt(DateTime lastActivityAt) =>
      call(lastActivityAt: lastActivityAt);

  @override
  Situation closedAt(DateTime? closedAt) => call(closedAt: closedAt);

  @override
  Situation memberCount(int memberCount) => call(memberCount: memberCount);

  @override
  Situation signalFamilies(List<String> signalFamilies) =>
      call(signalFamilies: signalFamilies);

  @override
  Situation corroborationShare(num? corroborationShare) =>
      call(corroborationShare: corroborationShare);

  @override
  Situation summary(String summary) => call(summary: summary);

  @override
  Situation origin(SituationOriginEnum origin) => call(origin: origin);

  @override
  Situation mergedInto(String? mergedInto) => call(mergedInto: mergedInto);

  @override
  Situation private(bool private) => call(private: private);

  @override
  Situation version(int version) => call(version: version);

  @override
  Situation updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  @override
  Situation severityOverride(Severity? severityOverride) =>
      call(severityOverride: severityOverride);

  @override
  Situation geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  Situation topicIds(List<String>? topicIds) => call(topicIds: topicIds);

  @override
  Situation entityIds(List<String>? entityIds) => call(entityIds: entityIds);

  @override
  Situation investigationIds(List<String>? investigationIds) =>
      call(investigationIds: investigationIds);

  @override
  Situation linkerVersion(String? linkerVersion) =>
      call(linkerVersion: linkerVersion);

  @override
  Situation createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Situation evidence(SituationEvidence evidence) => call(evidence: evidence);

  @override
  Situation inference(SituationInference inference) =>
      call(inference: inference);

  @override
  Situation speculation(SituationSpeculation speculation) =>
      call(speculation: speculation);

  @override
  Situation caveats(List<Caveat>? caveats) => call(caveats: caveats);

  @override
  Situation coverage(CoverageStats? coverage) => call(coverage: coverage);

  @override
  Situation social(SituationSocialSection? social) => call(social: social);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Situation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Situation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Situation call({
    Object? id = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? theme = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? severityOverridden = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? centroid = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? lastActivityAt = const $CopyWithPlaceholder(),
    Object? closedAt = const $CopyWithPlaceholder(),
    Object? memberCount = const $CopyWithPlaceholder(),
    Object? signalFamilies = const $CopyWithPlaceholder(),
    Object? corroborationShare = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
    Object? mergedInto = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? severityOverride = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? topicIds = const $CopyWithPlaceholder(),
    Object? entityIds = const $CopyWithPlaceholder(),
    Object? investigationIds = const $CopyWithPlaceholder(),
    Object? linkerVersion = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? inference = const $CopyWithPlaceholder(),
    Object? speculation = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
    Object? coverage = const $CopyWithPlaceholder(),
    Object? social = const $CopyWithPlaceholder(),
  }) {
    return Situation(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      theme: theme == const $CopyWithPlaceholder() || theme == null
          ? _value.theme
          // ignore: cast_nullable_to_non_nullable
          : theme as SituationTheme,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SituationStatus,
      severity: severity == const $CopyWithPlaceholder() || severity == null
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as Severity,
      severityOverridden: severityOverridden == const $CopyWithPlaceholder()
          ? _value.severityOverridden
          // ignore: cast_nullable_to_non_nullable
          : severityOverridden as bool?,
      regionCodes:
          regionCodes == const $CopyWithPlaceholder() || regionCodes == null
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      centroid: centroid == const $CopyWithPlaceholder()
          ? _value.centroid
          // ignore: cast_nullable_to_non_nullable
          : centroid as Geometry?,
      startedAt: startedAt == const $CopyWithPlaceholder() || startedAt == null
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime,
      lastActivityAt:
          lastActivityAt == const $CopyWithPlaceholder() ||
              lastActivityAt == null
          ? _value.lastActivityAt
          // ignore: cast_nullable_to_non_nullable
          : lastActivityAt as DateTime,
      closedAt: closedAt == const $CopyWithPlaceholder()
          ? _value.closedAt
          // ignore: cast_nullable_to_non_nullable
          : closedAt as DateTime?,
      memberCount:
          memberCount == const $CopyWithPlaceholder() || memberCount == null
          ? _value.memberCount
          // ignore: cast_nullable_to_non_nullable
          : memberCount as int,
      signalFamilies:
          signalFamilies == const $CopyWithPlaceholder() ||
              signalFamilies == null
          ? _value.signalFamilies
          // ignore: cast_nullable_to_non_nullable
          : signalFamilies as List<String>,
      corroborationShare: corroborationShare == const $CopyWithPlaceholder()
          ? _value.corroborationShare
          // ignore: cast_nullable_to_non_nullable
          : corroborationShare as num?,
      summary: summary == const $CopyWithPlaceholder() || summary == null
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as String,
      origin: origin == const $CopyWithPlaceholder() || origin == null
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as SituationOriginEnum,
      mergedInto: mergedInto == const $CopyWithPlaceholder()
          ? _value.mergedInto
          // ignore: cast_nullable_to_non_nullable
          : mergedInto as String?,
      private: private == const $CopyWithPlaceholder() || private == null
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      severityOverride: severityOverride == const $CopyWithPlaceholder()
          ? _value.severityOverride
          // ignore: cast_nullable_to_non_nullable
          : severityOverride as Severity?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      topicIds: topicIds == const $CopyWithPlaceholder()
          ? _value.topicIds
          // ignore: cast_nullable_to_non_nullable
          : topicIds as List<String>?,
      entityIds: entityIds == const $CopyWithPlaceholder()
          ? _value.entityIds
          // ignore: cast_nullable_to_non_nullable
          : entityIds as List<String>?,
      investigationIds: investigationIds == const $CopyWithPlaceholder()
          ? _value.investigationIds
          // ignore: cast_nullable_to_non_nullable
          : investigationIds as List<String>?,
      linkerVersion: linkerVersion == const $CopyWithPlaceholder()
          ? _value.linkerVersion
          // ignore: cast_nullable_to_non_nullable
          : linkerVersion as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      evidence: evidence == const $CopyWithPlaceholder() || evidence == null
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as SituationEvidence,
      inference: inference == const $CopyWithPlaceholder() || inference == null
          ? _value.inference
          // ignore: cast_nullable_to_non_nullable
          : inference as SituationInference,
      speculation:
          speculation == const $CopyWithPlaceholder() || speculation == null
          ? _value.speculation
          // ignore: cast_nullable_to_non_nullable
          : speculation as SituationSpeculation,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
      coverage: coverage == const $CopyWithPlaceholder()
          ? _value.coverage
          // ignore: cast_nullable_to_non_nullable
          : coverage as CoverageStats?,
      social: social == const $CopyWithPlaceholder()
          ? _value.social
          // ignore: cast_nullable_to_non_nullable
          : social as SituationSocialSection?,
    );
  }
}

extension $SituationCopyWith on Situation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituation.copyWith(...)` or `instanceOfSituation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationCWProxy get copyWith => _$SituationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Situation _$SituationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Situation', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'title',
      'theme',
      'status',
      'severity',
      'regionCodes',
      'startedAt',
      'lastActivityAt',
      'memberCount',
      'signalFamilies',
      'summary',
      'origin',
      'private',
      'version',
      'geometry',
      'createdAt',
      'evidence',
      'inference',
      'speculation',
    ],
  );
  final val = Situation(
    id: $checkedConvert('id', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String),
    theme: $checkedConvert(
      'theme',
      (v) => $enumDecode(_$SituationThemeEnumMap, v),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$SituationStatusEnumMap, v),
    ),
    severity: $checkedConvert(
      'severity',
      (v) => $enumDecode(_$SeverityEnumMap, v),
    ),
    severityOverridden: $checkedConvert(
      'severityOverridden',
      (v) => v as bool?,
    ),
    regionCodes: $checkedConvert(
      'regionCodes',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    bbox: $checkedConvert(
      'bbox',
      (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
    ),
    centroid: $checkedConvert(
      'centroid',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    startedAt: $checkedConvert('startedAt', (v) => DateTime.parse(v as String)),
    lastActivityAt: $checkedConvert(
      'lastActivityAt',
      (v) => DateTime.parse(v as String),
    ),
    closedAt: $checkedConvert(
      'closedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    memberCount: $checkedConvert('memberCount', (v) => (v as num).toInt()),
    signalFamilies: $checkedConvert(
      'signalFamilies',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    corroborationShare: $checkedConvert('corroborationShare', (v) => v as num?),
    summary: $checkedConvert('summary', (v) => v as String),
    origin: $checkedConvert(
      'origin',
      (v) => $enumDecode(_$SituationOriginEnumEnumMap, v),
    ),
    mergedInto: $checkedConvert('mergedInto', (v) => v as String?),
    private: $checkedConvert('private', (v) => v as bool),
    version: $checkedConvert('version', (v) => (v as num).toInt()),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    severityOverride: $checkedConvert(
      'severityOverride',
      (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
    ),
    geometry: $checkedConvert(
      'geometry',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    topicIds: $checkedConvert(
      'topicIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    entityIds: $checkedConvert(
      'entityIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    investigationIds: $checkedConvert(
      'investigationIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    linkerVersion: $checkedConvert('linkerVersion', (v) => v as String?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    evidence: $checkedConvert(
      'evidence',
      (v) => SituationEvidence.fromJson(v as Map<String, dynamic>),
    ),
    inference: $checkedConvert(
      'inference',
      (v) => SituationInference.fromJson(v as Map<String, dynamic>),
    ),
    speculation: $checkedConvert(
      'speculation',
      (v) => SituationSpeculation.fromJson(v as Map<String, dynamic>),
    ),
    caveats: $checkedConvert(
      'caveats',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    coverage: $checkedConvert(
      'coverage',
      (v) =>
          v == null ? null : CoverageStats.fromJson(v as Map<String, dynamic>),
    ),
    social: $checkedConvert(
      'social',
      (v) => v == null
          ? null
          : SituationSocialSection.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$SituationToJson(Situation instance) => <String, dynamic>{
  'id': instance.id,
  'title': instance.title,
  'theme': _$SituationThemeEnumMap[instance.theme]!,
  'status': _$SituationStatusEnumMap[instance.status]!,
  'severity': _$SeverityEnumMap[instance.severity]!,
  'severityOverridden': ?instance.severityOverridden,
  'regionCodes': instance.regionCodes,
  'bbox': ?instance.bbox,
  'centroid': ?instance.centroid?.toJson(),
  'startedAt': instance.startedAt.toIso8601String(),
  'lastActivityAt': instance.lastActivityAt.toIso8601String(),
  'closedAt': ?instance.closedAt?.toIso8601String(),
  'memberCount': instance.memberCount,
  'signalFamilies': instance.signalFamilies,
  'corroborationShare': ?instance.corroborationShare,
  'summary': instance.summary,
  'origin': _$SituationOriginEnumEnumMap[instance.origin]!,
  'mergedInto': ?instance.mergedInto,
  'private': instance.private,
  'version': instance.version,
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'severityOverride': ?_$SeverityEnumMap[instance.severityOverride],
  'geometry': instance.geometry?.toJson(),
  'topicIds': ?instance.topicIds,
  'entityIds': ?instance.entityIds,
  'investigationIds': ?instance.investigationIds,
  'linkerVersion': ?instance.linkerVersion,
  'createdAt': instance.createdAt.toIso8601String(),
  'evidence': instance.evidence.toJson(),
  'inference': instance.inference.toJson(),
  'speculation': instance.speculation.toJson(),
  'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
  'coverage': ?instance.coverage?.toJson(),
  'social': ?instance.social?.toJson(),
};

const _$SituationThemeEnumMap = {
  SituationTheme.maritime: 'maritime',
  SituationTheme.aviation: 'aviation',
  SituationTheme.conflict: 'conflict',
  SituationTheme.infrastructure: 'infrastructure',
  SituationTheme.weather: 'weather',
  SituationTheme.cyber: 'cyber',
  SituationTheme.markets: 'markets',
  SituationTheme.health: 'health',
  SituationTheme.space: 'space',
  SituationTheme.mixed: 'mixed',
};

const _$SituationStatusEnumMap = {
  SituationStatus.emerging: 'emerging',
  SituationStatus.active: 'active',
  SituationStatus.dormant: 'dormant',
  SituationStatus.closed: 'closed',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};

const _$SituationOriginEnumEnumMap = {
  SituationOriginEnum.auto: 'auto',
  SituationOriginEnum.operator_: 'operator',
  SituationOriginEnum.agent: 'agent',
  SituationOriginEnum.analyst: 'analyst',
};
