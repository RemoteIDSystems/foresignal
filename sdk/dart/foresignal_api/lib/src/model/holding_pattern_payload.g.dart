// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'holding_pattern_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HoldingPatternPayloadCWProxy {
  HoldingPatternPayload automated(bool automated);

  HoldingPatternPayload notAWarning(bool notAWarning);

  HoldingPatternPayload detector(DerivedPayloadCommonDetector detector);

  HoldingPatternPayload confidence(num? confidence);

  HoldingPatternPayload status(HoldingPatternPayloadStatusEnum status);

  HoldingPatternPayload evidence(List<DerivedEvidenceRef>? evidence);

  HoldingPatternPayload hex(String hex);

  HoldingPatternPayload turnsDeg(num turnsDeg);

  HoldingPatternPayload durationMin(num? durationMin);

  HoldingPatternPayload altitudeFt(num? altitudeFt);

  HoldingPatternPayload nearestAirport(
    HoldingPatternPayloadAllOfNearestAirport? nearestAirport,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HoldingPatternPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HoldingPatternPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  HoldingPatternPayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    HoldingPatternPayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    String hex,
    num turnsDeg,
    num? durationMin,
    num? altitudeFt,
    HoldingPatternPayloadAllOfNearestAirport? nearestAirport,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHoldingPatternPayload.copyWith(...)` or call `instanceOfHoldingPatternPayload.copyWith.fieldName(value)` for a single field.
class _$HoldingPatternPayloadCWProxyImpl
    implements _$HoldingPatternPayloadCWProxy {
  const _$HoldingPatternPayloadCWProxyImpl(this._value);

  final HoldingPatternPayload _value;

  @override
  HoldingPatternPayload automated(bool automated) => call(automated: automated);

  @override
  HoldingPatternPayload notAWarning(bool notAWarning) =>
      call(notAWarning: notAWarning);

  @override
  HoldingPatternPayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  HoldingPatternPayload confidence(num? confidence) =>
      call(confidence: confidence);

  @override
  HoldingPatternPayload status(HoldingPatternPayloadStatusEnum status) =>
      call(status: status);

  @override
  HoldingPatternPayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  HoldingPatternPayload hex(String hex) => call(hex: hex);

  @override
  HoldingPatternPayload turnsDeg(num turnsDeg) => call(turnsDeg: turnsDeg);

  @override
  HoldingPatternPayload durationMin(num? durationMin) =>
      call(durationMin: durationMin);

  @override
  HoldingPatternPayload altitudeFt(num? altitudeFt) =>
      call(altitudeFt: altitudeFt);

  @override
  HoldingPatternPayload nearestAirport(
    HoldingPatternPayloadAllOfNearestAirport? nearestAirport,
  ) => call(nearestAirport: nearestAirport);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HoldingPatternPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HoldingPatternPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HoldingPatternPayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? hex = const $CopyWithPlaceholder(),
    Object? turnsDeg = const $CopyWithPlaceholder(),
    Object? durationMin = const $CopyWithPlaceholder(),
    Object? altitudeFt = const $CopyWithPlaceholder(),
    Object? nearestAirport = const $CopyWithPlaceholder(),
  }) {
    return HoldingPatternPayload(
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
          : status as HoldingPatternPayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      hex: hex == const $CopyWithPlaceholder() || hex == null
          ? _value.hex
          // ignore: cast_nullable_to_non_nullable
          : hex as String,
      turnsDeg: turnsDeg == const $CopyWithPlaceholder() || turnsDeg == null
          ? _value.turnsDeg
          // ignore: cast_nullable_to_non_nullable
          : turnsDeg as num,
      durationMin: durationMin == const $CopyWithPlaceholder()
          ? _value.durationMin
          // ignore: cast_nullable_to_non_nullable
          : durationMin as num?,
      altitudeFt: altitudeFt == const $CopyWithPlaceholder()
          ? _value.altitudeFt
          // ignore: cast_nullable_to_non_nullable
          : altitudeFt as num?,
      nearestAirport: nearestAirport == const $CopyWithPlaceholder()
          ? _value.nearestAirport
          // ignore: cast_nullable_to_non_nullable
          : nearestAirport as HoldingPatternPayloadAllOfNearestAirport?,
    );
  }
}

extension $HoldingPatternPayloadCopyWith on HoldingPatternPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHoldingPatternPayload.copyWith(...)` or `instanceOfHoldingPatternPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HoldingPatternPayloadCWProxy get copyWith =>
      _$HoldingPatternPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HoldingPatternPayload _$HoldingPatternPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HoldingPatternPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'automated',
      'notAWarning',
      'detector',
      'status',
      'hex',
      'turnsDeg',
    ],
  );
  final val = HoldingPatternPayload(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$HoldingPatternPayloadStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    hex: $checkedConvert('hex', (v) => v as String),
    turnsDeg: $checkedConvert('turnsDeg', (v) => v as num),
    durationMin: $checkedConvert('durationMin', (v) => v as num?),
    altitudeFt: $checkedConvert('altitudeFt', (v) => v as num?),
    nearestAirport: $checkedConvert(
      'nearestAirport',
      (v) => v == null
          ? null
          : HoldingPatternPayloadAllOfNearestAirport.fromJson(
              v as Map<String, dynamic>,
            ),
    ),
  );
  return val;
});

Map<String, dynamic> _$HoldingPatternPayloadToJson(
  HoldingPatternPayload instance,
) => <String, dynamic>{
  'automated': instance.automated,
  'notAWarning': instance.notAWarning,
  'detector': instance.detector.toJson(),
  'confidence': ?instance.confidence,
  'status': _$HoldingPatternPayloadStatusEnumEnumMap[instance.status]!,
  'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
  'hex': instance.hex,
  'turnsDeg': instance.turnsDeg,
  'durationMin': ?instance.durationMin,
  'altitudeFt': ?instance.altitudeFt,
  'nearestAirport': ?instance.nearestAirport?.toJson(),
};

const _$HoldingPatternPayloadStatusEnumEnumMap = {
  HoldingPatternPayloadStatusEnum.ok: 'ok',
  HoldingPatternPayloadStatusEnum.insufficientData: 'insufficient-data',
  HoldingPatternPayloadStatusEnum.noCoverage: 'no-coverage',
  HoldingPatternPayloadStatusEnum.degradedInput: 'degraded-input',
};
