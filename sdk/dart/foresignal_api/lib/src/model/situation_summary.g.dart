// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationSummaryCWProxy {
  SituationSummary id(String id);

  SituationSummary title(String title);

  SituationSummary theme(SituationTheme theme);

  SituationSummary status(SituationStatus status);

  SituationSummary severity(Severity severity);

  SituationSummary severityOverridden(bool? severityOverridden);

  SituationSummary regionCodes(List<String> regionCodes);

  SituationSummary bbox(List<num>? bbox);

  SituationSummary centroid(Geometry? centroid);

  SituationSummary startedAt(DateTime startedAt);

  SituationSummary lastActivityAt(DateTime lastActivityAt);

  SituationSummary closedAt(DateTime? closedAt);

  SituationSummary memberCount(int memberCount);

  SituationSummary signalFamilies(List<String> signalFamilies);

  SituationSummary corroborationShare(num? corroborationShare);

  SituationSummary summary(String summary);

  SituationSummary origin(SituationSummaryOriginEnum origin);

  SituationSummary mergedInto(String? mergedInto);

  SituationSummary private(bool private);

  SituationSummary version(int version);

  SituationSummary updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationSummary call({
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
    SituationSummaryOriginEnum origin,
    String? mergedInto,
    bool private,
    int version,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationSummary.copyWith(...)` or call `instanceOfSituationSummary.copyWith.fieldName(value)` for a single field.
class _$SituationSummaryCWProxyImpl implements _$SituationSummaryCWProxy {
  const _$SituationSummaryCWProxyImpl(this._value);

  final SituationSummary _value;

  @override
  SituationSummary id(String id) => call(id: id);

  @override
  SituationSummary title(String title) => call(title: title);

  @override
  SituationSummary theme(SituationTheme theme) => call(theme: theme);

  @override
  SituationSummary status(SituationStatus status) => call(status: status);

  @override
  SituationSummary severity(Severity severity) => call(severity: severity);

  @override
  SituationSummary severityOverridden(bool? severityOverridden) =>
      call(severityOverridden: severityOverridden);

  @override
  SituationSummary regionCodes(List<String> regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  SituationSummary bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  SituationSummary centroid(Geometry? centroid) => call(centroid: centroid);

  @override
  SituationSummary startedAt(DateTime startedAt) => call(startedAt: startedAt);

  @override
  SituationSummary lastActivityAt(DateTime lastActivityAt) =>
      call(lastActivityAt: lastActivityAt);

  @override
  SituationSummary closedAt(DateTime? closedAt) => call(closedAt: closedAt);

  @override
  SituationSummary memberCount(int memberCount) =>
      call(memberCount: memberCount);

  @override
  SituationSummary signalFamilies(List<String> signalFamilies) =>
      call(signalFamilies: signalFamilies);

  @override
  SituationSummary corroborationShare(num? corroborationShare) =>
      call(corroborationShare: corroborationShare);

  @override
  SituationSummary summary(String summary) => call(summary: summary);

  @override
  SituationSummary origin(SituationSummaryOriginEnum origin) =>
      call(origin: origin);

  @override
  SituationSummary mergedInto(String? mergedInto) =>
      call(mergedInto: mergedInto);

  @override
  SituationSummary private(bool private) => call(private: private);

  @override
  SituationSummary version(int version) => call(version: version);

  @override
  SituationSummary updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationSummary call({
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
  }) {
    return SituationSummary(
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
          : origin as SituationSummaryOriginEnum,
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
    );
  }
}

extension $SituationSummaryCopyWith on SituationSummary {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationSummary.copyWith(...)` or `instanceOfSituationSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationSummaryCWProxy get copyWith => _$SituationSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationSummary _$SituationSummaryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationSummary', json, ($checkedConvert) {
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
    ],
  );
  final val = SituationSummary(
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
      (v) => $enumDecode(_$SituationSummaryOriginEnumEnumMap, v),
    ),
    mergedInto: $checkedConvert('mergedInto', (v) => v as String?),
    private: $checkedConvert('private', (v) => v as bool),
    version: $checkedConvert('version', (v) => (v as num).toInt()),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$SituationSummaryToJson(SituationSummary instance) =>
    <String, dynamic>{
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
      'origin': _$SituationSummaryOriginEnumEnumMap[instance.origin]!,
      'mergedInto': ?instance.mergedInto,
      'private': instance.private,
      'version': instance.version,
      'updatedAt': ?instance.updatedAt?.toIso8601String(),
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

const _$SituationSummaryOriginEnumEnumMap = {
  SituationSummaryOriginEnum.auto: 'auto',
  SituationSummaryOriginEnum.operator_: 'operator',
  SituationSummaryOriginEnum.agent: 'agent',
  SituationSummaryOriginEnum.analyst: 'analyst',
};
