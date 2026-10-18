// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rendezvous_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RendezvousPayloadCWProxy {
  RendezvousPayload automated(bool automated);

  RendezvousPayload notAWarning(bool notAWarning);

  RendezvousPayload detector(DerivedPayloadCommonDetector detector);

  RendezvousPayload confidence(num? confidence);

  RendezvousPayload status(RendezvousPayloadStatusEnum status);

  RendezvousPayload evidence(List<DerivedEvidenceRef>? evidence);

  RendezvousPayload vessels(List<String> vessels);

  RendezvousPayload distanceNm(num? distanceNm);

  RendezvousPayload startedAt(DateTime startedAt);

  RendezvousPayload durationMin(num? durationMin);

  RendezvousPayload nearestPortKm(num? nearestPortKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RendezvousPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RendezvousPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  RendezvousPayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    RendezvousPayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    List<String> vessels,
    num? distanceNm,
    DateTime startedAt,
    num? durationMin,
    num? nearestPortKm,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRendezvousPayload.copyWith(...)` or call `instanceOfRendezvousPayload.copyWith.fieldName(value)` for a single field.
class _$RendezvousPayloadCWProxyImpl implements _$RendezvousPayloadCWProxy {
  const _$RendezvousPayloadCWProxyImpl(this._value);

  final RendezvousPayload _value;

  @override
  RendezvousPayload automated(bool automated) => call(automated: automated);

  @override
  RendezvousPayload notAWarning(bool notAWarning) =>
      call(notAWarning: notAWarning);

  @override
  RendezvousPayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  RendezvousPayload confidence(num? confidence) => call(confidence: confidence);

  @override
  RendezvousPayload status(RendezvousPayloadStatusEnum status) =>
      call(status: status);

  @override
  RendezvousPayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  RendezvousPayload vessels(List<String> vessels) => call(vessels: vessels);

  @override
  RendezvousPayload distanceNm(num? distanceNm) => call(distanceNm: distanceNm);

  @override
  RendezvousPayload startedAt(DateTime startedAt) => call(startedAt: startedAt);

  @override
  RendezvousPayload durationMin(num? durationMin) =>
      call(durationMin: durationMin);

  @override
  RendezvousPayload nearestPortKm(num? nearestPortKm) =>
      call(nearestPortKm: nearestPortKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RendezvousPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RendezvousPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RendezvousPayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? vessels = const $CopyWithPlaceholder(),
    Object? distanceNm = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? durationMin = const $CopyWithPlaceholder(),
    Object? nearestPortKm = const $CopyWithPlaceholder(),
  }) {
    return RendezvousPayload(
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
          : status as RendezvousPayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      vessels: vessels == const $CopyWithPlaceholder() || vessels == null
          ? _value.vessels
          // ignore: cast_nullable_to_non_nullable
          : vessels as List<String>,
      distanceNm: distanceNm == const $CopyWithPlaceholder()
          ? _value.distanceNm
          // ignore: cast_nullable_to_non_nullable
          : distanceNm as num?,
      startedAt: startedAt == const $CopyWithPlaceholder() || startedAt == null
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime,
      durationMin: durationMin == const $CopyWithPlaceholder()
          ? _value.durationMin
          // ignore: cast_nullable_to_non_nullable
          : durationMin as num?,
      nearestPortKm: nearestPortKm == const $CopyWithPlaceholder()
          ? _value.nearestPortKm
          // ignore: cast_nullable_to_non_nullable
          : nearestPortKm as num?,
    );
  }
}

extension $RendezvousPayloadCopyWith on RendezvousPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRendezvousPayload.copyWith(...)` or `instanceOfRendezvousPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RendezvousPayloadCWProxy get copyWith =>
      _$RendezvousPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RendezvousPayload _$RendezvousPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RendezvousPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'automated',
      'notAWarning',
      'detector',
      'status',
      'vessels',
      'startedAt',
    ],
  );
  final val = RendezvousPayload(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$RendezvousPayloadStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    vessels: $checkedConvert(
      'vessels',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    distanceNm: $checkedConvert('distanceNm', (v) => v as num?),
    startedAt: $checkedConvert('startedAt', (v) => DateTime.parse(v as String)),
    durationMin: $checkedConvert('durationMin', (v) => v as num?),
    nearestPortKm: $checkedConvert('nearestPortKm', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$RendezvousPayloadToJson(RendezvousPayload instance) =>
    <String, dynamic>{
      'automated': instance.automated,
      'notAWarning': instance.notAWarning,
      'detector': instance.detector.toJson(),
      'confidence': ?instance.confidence,
      'status': _$RendezvousPayloadStatusEnumEnumMap[instance.status]!,
      'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
      'vessels': instance.vessels,
      'distanceNm': ?instance.distanceNm,
      'startedAt': instance.startedAt.toIso8601String(),
      'durationMin': ?instance.durationMin,
      'nearestPortKm': ?instance.nearestPortKm,
    };

const _$RendezvousPayloadStatusEnumEnumMap = {
  RendezvousPayloadStatusEnum.ok: 'ok',
  RendezvousPayloadStatusEnum.insufficientData: 'insufficient-data',
  RendezvousPayloadStatusEnum.noCoverage: 'no-coverage',
  RendezvousPayloadStatusEnum.degradedInput: 'degraded-input',
};
