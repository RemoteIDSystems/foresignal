// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cable_proximity_risk_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CableProximityRiskPayloadCWProxy {
  CableProximityRiskPayload automated(bool automated);

  CableProximityRiskPayload notAWarning(bool notAWarning);

  CableProximityRiskPayload detector(DerivedPayloadCommonDetector detector);

  CableProximityRiskPayload confidence(num? confidence);

  CableProximityRiskPayload status(CableProximityRiskPayloadStatusEnum status);

  CableProximityRiskPayload evidence(List<DerivedEvidenceRef>? evidence);

  CableProximityRiskPayload mmsi(String mmsi);

  CableProximityRiskPayload cableId(String cableId);

  CableProximityRiskPayload cableName(String? cableName);

  CableProximityRiskPayload startedAt(DateTime startedAt);

  CableProximityRiskPayload durationMin(num? durationMin);

  CableProximityRiskPayload cablesWithin2Km(int? cablesWithin2Km);

  CableProximityRiskPayload possibleAnchorDrag(bool? possibleAnchorDrag);

  CableProximityRiskPayload aisGapNearCable(bool? aisGapNearCable);

  CableProximityRiskPayload severity(Severity? severity);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CableProximityRiskPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CableProximityRiskPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  CableProximityRiskPayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    CableProximityRiskPayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    String mmsi,
    String cableId,
    String? cableName,
    DateTime startedAt,
    num? durationMin,
    int? cablesWithin2Km,
    bool? possibleAnchorDrag,
    bool? aisGapNearCable,
    Severity? severity,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCableProximityRiskPayload.copyWith(...)` or call `instanceOfCableProximityRiskPayload.copyWith.fieldName(value)` for a single field.
class _$CableProximityRiskPayloadCWProxyImpl
    implements _$CableProximityRiskPayloadCWProxy {
  const _$CableProximityRiskPayloadCWProxyImpl(this._value);

  final CableProximityRiskPayload _value;

  @override
  CableProximityRiskPayload automated(bool automated) =>
      call(automated: automated);

  @override
  CableProximityRiskPayload notAWarning(bool notAWarning) =>
      call(notAWarning: notAWarning);

  @override
  CableProximityRiskPayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  CableProximityRiskPayload confidence(num? confidence) =>
      call(confidence: confidence);

  @override
  CableProximityRiskPayload status(
    CableProximityRiskPayloadStatusEnum status,
  ) => call(status: status);

  @override
  CableProximityRiskPayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  CableProximityRiskPayload mmsi(String mmsi) => call(mmsi: mmsi);

  @override
  CableProximityRiskPayload cableId(String cableId) => call(cableId: cableId);

  @override
  CableProximityRiskPayload cableName(String? cableName) =>
      call(cableName: cableName);

  @override
  CableProximityRiskPayload startedAt(DateTime startedAt) =>
      call(startedAt: startedAt);

  @override
  CableProximityRiskPayload durationMin(num? durationMin) =>
      call(durationMin: durationMin);

  @override
  CableProximityRiskPayload cablesWithin2Km(int? cablesWithin2Km) =>
      call(cablesWithin2Km: cablesWithin2Km);

  @override
  CableProximityRiskPayload possibleAnchorDrag(bool? possibleAnchorDrag) =>
      call(possibleAnchorDrag: possibleAnchorDrag);

  @override
  CableProximityRiskPayload aisGapNearCable(bool? aisGapNearCable) =>
      call(aisGapNearCable: aisGapNearCable);

  @override
  CableProximityRiskPayload severity(Severity? severity) =>
      call(severity: severity);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CableProximityRiskPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CableProximityRiskPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CableProximityRiskPayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? mmsi = const $CopyWithPlaceholder(),
    Object? cableId = const $CopyWithPlaceholder(),
    Object? cableName = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? durationMin = const $CopyWithPlaceholder(),
    Object? cablesWithin2Km = const $CopyWithPlaceholder(),
    Object? possibleAnchorDrag = const $CopyWithPlaceholder(),
    Object? aisGapNearCable = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
  }) {
    return CableProximityRiskPayload(
      automated: automated == const $CopyWithPlaceholder() || automated == null
          ? _value.automated
          // ignore: cast_nullable_to_non_nullable
          : automated as bool,
      notAWarning:
          notAWarning == const $CopyWithPlaceholder() || notAWarning == null
          ? _value.notAWarning
          // ignore: cast_nullable_to_non_nullable
          : notAWarning as bool,
      detector: detector == const $CopyWithPlaceholder() || detector == null
          ? _value.detector
          // ignore: cast_nullable_to_non_nullable
          : detector as DerivedPayloadCommonDetector,
      confidence: confidence == const $CopyWithPlaceholder()
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as num?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as CableProximityRiskPayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      mmsi: mmsi == const $CopyWithPlaceholder() || mmsi == null
          ? _value.mmsi
          // ignore: cast_nullable_to_non_nullable
          : mmsi as String,
      cableId: cableId == const $CopyWithPlaceholder() || cableId == null
          ? _value.cableId
          // ignore: cast_nullable_to_non_nullable
          : cableId as String,
      cableName: cableName == const $CopyWithPlaceholder()
          ? _value.cableName
          // ignore: cast_nullable_to_non_nullable
          : cableName as String?,
      startedAt: startedAt == const $CopyWithPlaceholder() || startedAt == null
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime,
      durationMin: durationMin == const $CopyWithPlaceholder()
          ? _value.durationMin
          // ignore: cast_nullable_to_non_nullable
          : durationMin as num?,
      cablesWithin2Km: cablesWithin2Km == const $CopyWithPlaceholder()
          ? _value.cablesWithin2Km
          // ignore: cast_nullable_to_non_nullable
          : cablesWithin2Km as int?,
      possibleAnchorDrag: possibleAnchorDrag == const $CopyWithPlaceholder()
          ? _value.possibleAnchorDrag
          // ignore: cast_nullable_to_non_nullable
          : possibleAnchorDrag as bool?,
      aisGapNearCable: aisGapNearCable == const $CopyWithPlaceholder()
          ? _value.aisGapNearCable
          // ignore: cast_nullable_to_non_nullable
          : aisGapNearCable as bool?,
      severity: severity == const $CopyWithPlaceholder()
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as Severity?,
    );
  }
}

extension $CableProximityRiskPayloadCopyWith on CableProximityRiskPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCableProximityRiskPayload.copyWith(...)` or `instanceOfCableProximityRiskPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CableProximityRiskPayloadCWProxy get copyWith =>
      _$CableProximityRiskPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CableProximityRiskPayload _$CableProximityRiskPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CableProximityRiskPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'automated',
      'notAWarning',
      'detector',
      'status',
      'mmsi',
      'cableId',
      'startedAt',
    ],
  );
  final val = CableProximityRiskPayload(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$CableProximityRiskPayloadStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    mmsi: $checkedConvert('mmsi', (v) => v as String),
    cableId: $checkedConvert('cableId', (v) => v as String),
    cableName: $checkedConvert('cableName', (v) => v as String?),
    startedAt: $checkedConvert('startedAt', (v) => DateTime.parse(v as String)),
    durationMin: $checkedConvert('durationMin', (v) => v as num?),
    cablesWithin2Km: $checkedConvert(
      'cablesWithin2Km',
      (v) => (v as num?)?.toInt(),
    ),
    possibleAnchorDrag: $checkedConvert(
      'possibleAnchorDrag',
      (v) => v as bool?,
    ),
    aisGapNearCable: $checkedConvert('aisGapNearCable', (v) => v as bool?),
    severity: $checkedConvert(
      'severity',
      (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$CableProximityRiskPayloadToJson(
  CableProximityRiskPayload instance,
) => <String, dynamic>{
  'automated': instance.automated,
  'notAWarning': instance.notAWarning,
  'detector': instance.detector.toJson(),
  'confidence': ?instance.confidence,
  'status': _$CableProximityRiskPayloadStatusEnumEnumMap[instance.status]!,
  'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
  'mmsi': instance.mmsi,
  'cableId': instance.cableId,
  'cableName': ?instance.cableName,
  'startedAt': instance.startedAt.toIso8601String(),
  'durationMin': ?instance.durationMin,
  'cablesWithin2Km': ?instance.cablesWithin2Km,
  'possibleAnchorDrag': ?instance.possibleAnchorDrag,
  'aisGapNearCable': ?instance.aisGapNearCable,
  'severity': ?_$SeverityEnumMap[instance.severity],
};

const _$CableProximityRiskPayloadStatusEnumEnumMap = {
  CableProximityRiskPayloadStatusEnum.ok: 'ok',
  CableProximityRiskPayloadStatusEnum.insufficientData: 'insufficient-data',
  CableProximityRiskPayloadStatusEnum.noCoverage: 'no-coverage',
  CableProximityRiskPayloadStatusEnum.degradedInput: 'degraded-input',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};
