// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_event_record_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelEventRecordPayloadCWProxy {
  IntelEventRecordPayload type(IntelEventRecordPayloadTypeEnum type);

  IntelEventRecordPayload severity(Severity severity);

  IntelEventRecordPayload status(IntelEventRecordPayloadStatusEnum status);

  IntelEventRecordPayload confidence(num confidence);

  IntelEventRecordPayload label(IntelEventRecordPayloadLabelEnum? label);

  IntelEventRecordPayload evidenceQuality(
    IntelEventRecordPayloadEvidenceQualityEnum? evidenceQuality,
  );

  IntelEventRecordPayload memberCount(int? memberCount);

  IntelEventRecordPayload signalFamilies(List<String>? signalFamilies);

  IntelEventRecordPayload title(String title);

  IntelEventRecordPayload automated(bool automated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventRecordPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventRecordPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelEventRecordPayload call({
    IntelEventRecordPayloadTypeEnum type,
    Severity severity,
    IntelEventRecordPayloadStatusEnum status,
    num confidence,
    IntelEventRecordPayloadLabelEnum? label,
    IntelEventRecordPayloadEvidenceQualityEnum? evidenceQuality,
    int? memberCount,
    List<String>? signalFamilies,
    String title,
    bool automated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelEventRecordPayload.copyWith(...)` or call `instanceOfIntelEventRecordPayload.copyWith.fieldName(value)` for a single field.
class _$IntelEventRecordPayloadCWProxyImpl
    implements _$IntelEventRecordPayloadCWProxy {
  const _$IntelEventRecordPayloadCWProxyImpl(this._value);

  final IntelEventRecordPayload _value;

  @override
  IntelEventRecordPayload type(IntelEventRecordPayloadTypeEnum type) =>
      call(type: type);

  @override
  IntelEventRecordPayload severity(Severity severity) =>
      call(severity: severity);

  @override
  IntelEventRecordPayload status(IntelEventRecordPayloadStatusEnum status) =>
      call(status: status);

  @override
  IntelEventRecordPayload confidence(num confidence) =>
      call(confidence: confidence);

  @override
  IntelEventRecordPayload label(IntelEventRecordPayloadLabelEnum? label) =>
      call(label: label);

  @override
  IntelEventRecordPayload evidenceQuality(
    IntelEventRecordPayloadEvidenceQualityEnum? evidenceQuality,
  ) => call(evidenceQuality: evidenceQuality);

  @override
  IntelEventRecordPayload memberCount(int? memberCount) =>
      call(memberCount: memberCount);

  @override
  IntelEventRecordPayload signalFamilies(List<String>? signalFamilies) =>
      call(signalFamilies: signalFamilies);

  @override
  IntelEventRecordPayload title(String title) => call(title: title);

  @override
  IntelEventRecordPayload automated(bool automated) =>
      call(automated: automated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventRecordPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventRecordPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelEventRecordPayload call({
    Object? type = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? evidenceQuality = const $CopyWithPlaceholder(),
    Object? memberCount = const $CopyWithPlaceholder(),
    Object? signalFamilies = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? automated = const $CopyWithPlaceholder(),
  }) {
    return IntelEventRecordPayload(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as IntelEventRecordPayloadTypeEnum,
      severity: severity == const $CopyWithPlaceholder() || severity == null
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as Severity,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as IntelEventRecordPayloadStatusEnum,
      confidence:
          confidence == const $CopyWithPlaceholder() || confidence == null
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as num,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as IntelEventRecordPayloadLabelEnum?,
      evidenceQuality: evidenceQuality == const $CopyWithPlaceholder()
          ? _value.evidenceQuality
          // ignore: cast_nullable_to_non_nullable
          : evidenceQuality as IntelEventRecordPayloadEvidenceQualityEnum?,
      memberCount: memberCount == const $CopyWithPlaceholder()
          ? _value.memberCount
          // ignore: cast_nullable_to_non_nullable
          : memberCount as int?,
      signalFamilies: signalFamilies == const $CopyWithPlaceholder()
          ? _value.signalFamilies
          // ignore: cast_nullable_to_non_nullable
          : signalFamilies as List<String>?,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      automated: automated == const $CopyWithPlaceholder() || automated == null
          ? _value.automated
          // ignore: cast_nullable_to_non_nullable
          : automated as bool,
    );
  }
}

extension $IntelEventRecordPayloadCopyWith on IntelEventRecordPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelEventRecordPayload.copyWith(...)` or `instanceOfIntelEventRecordPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelEventRecordPayloadCWProxy get copyWith =>
      _$IntelEventRecordPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelEventRecordPayload _$IntelEventRecordPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelEventRecordPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'type',
      'severity',
      'status',
      'confidence',
      'title',
      'automated',
    ],
  );
  final val = IntelEventRecordPayload(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$IntelEventRecordPayloadTypeEnumEnumMap, v),
    ),
    severity: $checkedConvert(
      'severity',
      (v) => $enumDecode(_$SeverityEnumMap, v),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$IntelEventRecordPayloadStatusEnumEnumMap, v),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num),
    label: $checkedConvert(
      'label',
      (v) => $enumDecodeNullable(_$IntelEventRecordPayloadLabelEnumEnumMap, v),
    ),
    evidenceQuality: $checkedConvert(
      'evidenceQuality',
      (v) => $enumDecodeNullable(
        _$IntelEventRecordPayloadEvidenceQualityEnumEnumMap,
        v,
      ),
    ),
    memberCount: $checkedConvert('memberCount', (v) => (v as num?)?.toInt()),
    signalFamilies: $checkedConvert(
      'signalFamilies',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    title: $checkedConvert('title', (v) => v as String),
    automated: $checkedConvert('automated', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$IntelEventRecordPayloadToJson(
  IntelEventRecordPayload instance,
) => <String, dynamic>{
  'type': _$IntelEventRecordPayloadTypeEnumEnumMap[instance.type]!,
  'severity': _$SeverityEnumMap[instance.severity]!,
  'status': _$IntelEventRecordPayloadStatusEnumEnumMap[instance.status]!,
  'confidence': instance.confidence,
  'label': ?_$IntelEventRecordPayloadLabelEnumEnumMap[instance.label],
  'evidenceQuality':
      ?_$IntelEventRecordPayloadEvidenceQualityEnumEnumMap[instance
          .evidenceQuality],
  'memberCount': ?instance.memberCount,
  'signalFamilies': ?instance.signalFamilies,
  'title': instance.title,
  'automated': instance.automated,
};

const _$IntelEventRecordPayloadTypeEnumEnumMap = {
  IntelEventRecordPayloadTypeEnum.maritimeDisruption: 'maritime-disruption',
  IntelEventRecordPayloadTypeEnum.aviationAnomaly: 'aviation-anomaly',
  IntelEventRecordPayloadTypeEnum.infrastructureDisruption:
      'infrastructure-disruption',
  IntelEventRecordPayloadTypeEnum.weatherImpact: 'weather-impact',
  IntelEventRecordPayloadTypeEnum.seismic: 'seismic',
  IntelEventRecordPayloadTypeEnum.custom: 'custom',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};

const _$IntelEventRecordPayloadStatusEnumEnumMap = {
  IntelEventRecordPayloadStatusEnum.emerging: 'emerging',
  IntelEventRecordPayloadStatusEnum.active: 'active',
  IntelEventRecordPayloadStatusEnum.resolved: 'resolved',
  IntelEventRecordPayloadStatusEnum.dismissed: 'dismissed',
};

const _$IntelEventRecordPayloadLabelEnumEnumMap = {
  IntelEventRecordPayloadLabelEnum.low: 'low',
  IntelEventRecordPayloadLabelEnum.moderate: 'moderate',
  IntelEventRecordPayloadLabelEnum.high: 'high',
};

const _$IntelEventRecordPayloadEvidenceQualityEnumEnumMap = {
  IntelEventRecordPayloadEvidenceQualityEnum.strong: 'strong',
  IntelEventRecordPayloadEvidenceQualityEnum.moderate: 'moderate',
  IntelEventRecordPayloadEvidenceQualityEnum.weak: 'weak',
  IntelEventRecordPayloadEvidenceQualityEnum.insufficient: 'insufficient',
};
