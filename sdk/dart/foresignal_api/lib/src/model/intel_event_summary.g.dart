// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_event_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelEventSummaryCWProxy {
  IntelEventSummary id(String id);

  IntelEventSummary type(IntelEventSummaryTypeEnum type);

  IntelEventSummary title(String title);

  IntelEventSummary status(IntelEventSummaryStatusEnum status);

  IntelEventSummary severity(Severity severity);

  IntelEventSummary confidence(num confidence);

  IntelEventSummary rawConfidence(num? rawConfidence);

  IntelEventSummary confidenceLabel(
    IntelEventSummaryConfidenceLabelEnum confidenceLabel,
  );

  IntelEventSummary confidenceMethod(String? confidenceMethod);

  IntelEventSummary calibrated(bool? calibrated);

  IntelEventSummary evidenceQuality(
    IntelEventSummaryEvidenceQualityEnum evidenceQuality,
  );

  IntelEventSummary memberCount(int memberCount);

  IntelEventSummary signalFamilies(List<String>? signalFamilies);

  IntelEventSummary startTime(DateTime startTime);

  IntelEventSummary endTime(DateTime? endTime);

  IntelEventSummary lastEvidenceAt(DateTime? lastEvidenceAt);

  IntelEventSummary centroid(Geometry? centroid);

  IntelEventSummary regionTags(List<String>? regionTags);

  IntelEventSummary private(bool? private);

  IntelEventSummary origin(IntelEventSummaryOriginEnum? origin);

  IntelEventSummary mergedInto(String? mergedInto);

  IntelEventSummary version(int version);

  IntelEventSummary updatedAt(DateTime updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelEventSummary call({
    String id,
    IntelEventSummaryTypeEnum type,
    String title,
    IntelEventSummaryStatusEnum status,
    Severity severity,
    num confidence,
    num? rawConfidence,
    IntelEventSummaryConfidenceLabelEnum confidenceLabel,
    String? confidenceMethod,
    bool? calibrated,
    IntelEventSummaryEvidenceQualityEnum evidenceQuality,
    int memberCount,
    List<String>? signalFamilies,
    DateTime startTime,
    DateTime? endTime,
    DateTime? lastEvidenceAt,
    Geometry? centroid,
    List<String>? regionTags,
    bool? private,
    IntelEventSummaryOriginEnum? origin,
    String? mergedInto,
    int version,
    DateTime updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelEventSummary.copyWith(...)` or call `instanceOfIntelEventSummary.copyWith.fieldName(value)` for a single field.
class _$IntelEventSummaryCWProxyImpl implements _$IntelEventSummaryCWProxy {
  const _$IntelEventSummaryCWProxyImpl(this._value);

  final IntelEventSummary _value;

  @override
  IntelEventSummary id(String id) => call(id: id);

  @override
  IntelEventSummary type(IntelEventSummaryTypeEnum type) => call(type: type);

  @override
  IntelEventSummary title(String title) => call(title: title);

  @override
  IntelEventSummary status(IntelEventSummaryStatusEnum status) =>
      call(status: status);

  @override
  IntelEventSummary severity(Severity severity) => call(severity: severity);

  @override
  IntelEventSummary confidence(num confidence) => call(confidence: confidence);

  @override
  IntelEventSummary rawConfidence(num? rawConfidence) =>
      call(rawConfidence: rawConfidence);

  @override
  IntelEventSummary confidenceLabel(
    IntelEventSummaryConfidenceLabelEnum confidenceLabel,
  ) => call(confidenceLabel: confidenceLabel);

  @override
  IntelEventSummary confidenceMethod(String? confidenceMethod) =>
      call(confidenceMethod: confidenceMethod);

  @override
  IntelEventSummary calibrated(bool? calibrated) =>
      call(calibrated: calibrated);

  @override
  IntelEventSummary evidenceQuality(
    IntelEventSummaryEvidenceQualityEnum evidenceQuality,
  ) => call(evidenceQuality: evidenceQuality);

  @override
  IntelEventSummary memberCount(int memberCount) =>
      call(memberCount: memberCount);

  @override
  IntelEventSummary signalFamilies(List<String>? signalFamilies) =>
      call(signalFamilies: signalFamilies);

  @override
  IntelEventSummary startTime(DateTime startTime) => call(startTime: startTime);

  @override
  IntelEventSummary endTime(DateTime? endTime) => call(endTime: endTime);

  @override
  IntelEventSummary lastEvidenceAt(DateTime? lastEvidenceAt) =>
      call(lastEvidenceAt: lastEvidenceAt);

  @override
  IntelEventSummary centroid(Geometry? centroid) => call(centroid: centroid);

  @override
  IntelEventSummary regionTags(List<String>? regionTags) =>
      call(regionTags: regionTags);

  @override
  IntelEventSummary private(bool? private) => call(private: private);

  @override
  IntelEventSummary origin(IntelEventSummaryOriginEnum? origin) =>
      call(origin: origin);

  @override
  IntelEventSummary mergedInto(String? mergedInto) =>
      call(mergedInto: mergedInto);

  @override
  IntelEventSummary version(int version) => call(version: version);

  @override
  IntelEventSummary updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelEventSummary call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? rawConfidence = const $CopyWithPlaceholder(),
    Object? confidenceLabel = const $CopyWithPlaceholder(),
    Object? confidenceMethod = const $CopyWithPlaceholder(),
    Object? calibrated = const $CopyWithPlaceholder(),
    Object? evidenceQuality = const $CopyWithPlaceholder(),
    Object? memberCount = const $CopyWithPlaceholder(),
    Object? signalFamilies = const $CopyWithPlaceholder(),
    Object? startTime = const $CopyWithPlaceholder(),
    Object? endTime = const $CopyWithPlaceholder(),
    Object? lastEvidenceAt = const $CopyWithPlaceholder(),
    Object? centroid = const $CopyWithPlaceholder(),
    Object? regionTags = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
    Object? mergedInto = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return IntelEventSummary(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as IntelEventSummaryTypeEnum,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as IntelEventSummaryStatusEnum,
      severity: severity == const $CopyWithPlaceholder() || severity == null
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as Severity,
      confidence:
          confidence == const $CopyWithPlaceholder() || confidence == null
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as num,
      rawConfidence: rawConfidence == const $CopyWithPlaceholder()
          ? _value.rawConfidence
          // ignore: cast_nullable_to_non_nullable
          : rawConfidence as num?,
      confidenceLabel:
          confidenceLabel == const $CopyWithPlaceholder() ||
              confidenceLabel == null
          ? _value.confidenceLabel
          // ignore: cast_nullable_to_non_nullable
          : confidenceLabel as IntelEventSummaryConfidenceLabelEnum,
      confidenceMethod: confidenceMethod == const $CopyWithPlaceholder()
          ? _value.confidenceMethod
          // ignore: cast_nullable_to_non_nullable
          : confidenceMethod as String?,
      calibrated: calibrated == const $CopyWithPlaceholder()
          ? _value.calibrated
          // ignore: cast_nullable_to_non_nullable
          : calibrated as bool?,
      evidenceQuality:
          evidenceQuality == const $CopyWithPlaceholder() ||
              evidenceQuality == null
          ? _value.evidenceQuality
          // ignore: cast_nullable_to_non_nullable
          : evidenceQuality as IntelEventSummaryEvidenceQualityEnum,
      memberCount:
          memberCount == const $CopyWithPlaceholder() || memberCount == null
          ? _value.memberCount
          // ignore: cast_nullable_to_non_nullable
          : memberCount as int,
      signalFamilies: signalFamilies == const $CopyWithPlaceholder()
          ? _value.signalFamilies
          // ignore: cast_nullable_to_non_nullable
          : signalFamilies as List<String>?,
      startTime: startTime == const $CopyWithPlaceholder() || startTime == null
          ? _value.startTime
          // ignore: cast_nullable_to_non_nullable
          : startTime as DateTime,
      endTime: endTime == const $CopyWithPlaceholder()
          ? _value.endTime
          // ignore: cast_nullable_to_non_nullable
          : endTime as DateTime?,
      lastEvidenceAt: lastEvidenceAt == const $CopyWithPlaceholder()
          ? _value.lastEvidenceAt
          // ignore: cast_nullable_to_non_nullable
          : lastEvidenceAt as DateTime?,
      centroid: centroid == const $CopyWithPlaceholder()
          ? _value.centroid
          // ignore: cast_nullable_to_non_nullable
          : centroid as Geometry?,
      regionTags: regionTags == const $CopyWithPlaceholder()
          ? _value.regionTags
          // ignore: cast_nullable_to_non_nullable
          : regionTags as List<String>?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
      origin: origin == const $CopyWithPlaceholder()
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as IntelEventSummaryOriginEnum?,
      mergedInto: mergedInto == const $CopyWithPlaceholder()
          ? _value.mergedInto
          // ignore: cast_nullable_to_non_nullable
          : mergedInto as String?,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
    );
  }
}

extension $IntelEventSummaryCopyWith on IntelEventSummary {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelEventSummary.copyWith(...)` or `instanceOfIntelEventSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelEventSummaryCWProxy get copyWith =>
      _$IntelEventSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelEventSummary _$IntelEventSummaryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelEventSummary', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'type',
      'title',
      'status',
      'severity',
      'confidence',
      'confidenceLabel',
      'evidenceQuality',
      'memberCount',
      'startTime',
      'version',
      'updatedAt',
    ],
  );
  final val = IntelEventSummary(
    id: $checkedConvert('id', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$IntelEventSummaryTypeEnumEnumMap, v),
    ),
    title: $checkedConvert('title', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$IntelEventSummaryStatusEnumEnumMap, v),
    ),
    severity: $checkedConvert(
      'severity',
      (v) => $enumDecode(_$SeverityEnumMap, v),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num),
    rawConfidence: $checkedConvert('rawConfidence', (v) => v as num?),
    confidenceLabel: $checkedConvert(
      'confidenceLabel',
      (v) => $enumDecode(_$IntelEventSummaryConfidenceLabelEnumEnumMap, v),
    ),
    confidenceMethod: $checkedConvert('confidenceMethod', (v) => v as String?),
    calibrated: $checkedConvert('calibrated', (v) => v as bool?),
    evidenceQuality: $checkedConvert(
      'evidenceQuality',
      (v) => $enumDecode(_$IntelEventSummaryEvidenceQualityEnumEnumMap, v),
    ),
    memberCount: $checkedConvert('memberCount', (v) => (v as num).toInt()),
    signalFamilies: $checkedConvert(
      'signalFamilies',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    startTime: $checkedConvert('startTime', (v) => DateTime.parse(v as String)),
    endTime: $checkedConvert(
      'endTime',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastEvidenceAt: $checkedConvert(
      'lastEvidenceAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    centroid: $checkedConvert(
      'centroid',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    regionTags: $checkedConvert(
      'regionTags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    private: $checkedConvert('private', (v) => v as bool?),
    origin: $checkedConvert(
      'origin',
      (v) => $enumDecodeNullable(_$IntelEventSummaryOriginEnumEnumMap, v),
    ),
    mergedInto: $checkedConvert('mergedInto', (v) => v as String?),
    version: $checkedConvert('version', (v) => (v as num).toInt()),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$IntelEventSummaryToJson(
  IntelEventSummary instance,
) => <String, dynamic>{
  'id': instance.id,
  'type': _$IntelEventSummaryTypeEnumEnumMap[instance.type]!,
  'title': instance.title,
  'status': _$IntelEventSummaryStatusEnumEnumMap[instance.status]!,
  'severity': _$SeverityEnumMap[instance.severity]!,
  'confidence': instance.confidence,
  'rawConfidence': ?instance.rawConfidence,
  'confidenceLabel':
      _$IntelEventSummaryConfidenceLabelEnumEnumMap[instance.confidenceLabel]!,
  'confidenceMethod': ?instance.confidenceMethod,
  'calibrated': ?instance.calibrated,
  'evidenceQuality':
      _$IntelEventSummaryEvidenceQualityEnumEnumMap[instance.evidenceQuality]!,
  'memberCount': instance.memberCount,
  'signalFamilies': ?instance.signalFamilies,
  'startTime': instance.startTime.toIso8601String(),
  'endTime': ?instance.endTime?.toIso8601String(),
  'lastEvidenceAt': ?instance.lastEvidenceAt?.toIso8601String(),
  'centroid': ?instance.centroid?.toJson(),
  'regionTags': ?instance.regionTags,
  'private': ?instance.private,
  'origin': ?_$IntelEventSummaryOriginEnumEnumMap[instance.origin],
  'mergedInto': ?instance.mergedInto,
  'version': instance.version,
  'updatedAt': instance.updatedAt.toIso8601String(),
};

const _$IntelEventSummaryTypeEnumEnumMap = {
  IntelEventSummaryTypeEnum.maritimeDisruption: 'maritime-disruption',
  IntelEventSummaryTypeEnum.aviationAnomaly: 'aviation-anomaly',
  IntelEventSummaryTypeEnum.infrastructureDisruption:
      'infrastructure-disruption',
  IntelEventSummaryTypeEnum.weatherImpact: 'weather-impact',
  IntelEventSummaryTypeEnum.seismic: 'seismic',
  IntelEventSummaryTypeEnum.custom: 'custom',
};

const _$IntelEventSummaryStatusEnumEnumMap = {
  IntelEventSummaryStatusEnum.emerging: 'emerging',
  IntelEventSummaryStatusEnum.active: 'active',
  IntelEventSummaryStatusEnum.resolved: 'resolved',
  IntelEventSummaryStatusEnum.dismissed: 'dismissed',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};

const _$IntelEventSummaryConfidenceLabelEnumEnumMap = {
  IntelEventSummaryConfidenceLabelEnum.low: 'low',
  IntelEventSummaryConfidenceLabelEnum.moderate: 'moderate',
  IntelEventSummaryConfidenceLabelEnum.high: 'high',
};

const _$IntelEventSummaryEvidenceQualityEnumEnumMap = {
  IntelEventSummaryEvidenceQualityEnum.strong: 'strong',
  IntelEventSummaryEvidenceQualityEnum.moderate: 'moderate',
  IntelEventSummaryEvidenceQualityEnum.weak: 'weak',
  IntelEventSummaryEvidenceQualityEnum.insufficient: 'insufficient',
};

const _$IntelEventSummaryOriginEnumEnumMap = {
  IntelEventSummaryOriginEnum.recipe: 'recipe',
  IntelEventSummaryOriginEnum.analyst: 'analyst',
  IntelEventSummaryOriginEnum.agent: 'agent',
  IntelEventSummaryOriginEnum.operator_: 'operator',
};
