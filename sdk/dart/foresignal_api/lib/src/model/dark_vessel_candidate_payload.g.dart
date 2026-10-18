// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dark_vessel_candidate_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DarkVesselCandidatePayloadCWProxy {
  DarkVesselCandidatePayload automated(bool automated);

  DarkVesselCandidatePayload notAWarning(bool notAWarning);

  DarkVesselCandidatePayload detector(DerivedPayloadCommonDetector detector);

  DarkVesselCandidatePayload confidence(num? confidence);

  DarkVesselCandidatePayload status(
    DarkVesselCandidatePayloadStatusEnum status,
  );

  DarkVesselCandidatePayload evidence(List<DerivedEvidenceRef>? evidence);

  DarkVesselCandidatePayload mmsi(String mmsi);

  DarkVesselCandidatePayload gapStart(DateTime gapStart);

  DarkVesselCandidatePayload detectionId(String detectionId);

  DarkVesselCandidatePayload distanceKm(num? distanceKm);

  DarkVesselCandidatePayload timeOffsetMin(num? timeOffsetMin);

  DarkVesselCandidatePayload detectionLengthM(num? detectionLengthM);

  DarkVesselCandidatePayload matchScore(num matchScore);

  DarkVesselCandidatePayload label(DarkVesselCandidatePayloadLabelEnum? label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DarkVesselCandidatePayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DarkVesselCandidatePayload(...).copyWith(id: 12, name: "My name")
  /// ```
  DarkVesselCandidatePayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    DarkVesselCandidatePayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    String mmsi,
    DateTime gapStart,
    String detectionId,
    num? distanceKm,
    num? timeOffsetMin,
    num? detectionLengthM,
    num matchScore,
    DarkVesselCandidatePayloadLabelEnum? label,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDarkVesselCandidatePayload.copyWith(...)` or call `instanceOfDarkVesselCandidatePayload.copyWith.fieldName(value)` for a single field.
class _$DarkVesselCandidatePayloadCWProxyImpl
    implements _$DarkVesselCandidatePayloadCWProxy {
  const _$DarkVesselCandidatePayloadCWProxyImpl(this._value);

  final DarkVesselCandidatePayload _value;

  @override
  DarkVesselCandidatePayload automated(bool automated) =>
      call(automated: automated);

  @override
  DarkVesselCandidatePayload notAWarning(bool notAWarning) =>
      call(notAWarning: notAWarning);

  @override
  DarkVesselCandidatePayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  DarkVesselCandidatePayload confidence(num? confidence) =>
      call(confidence: confidence);

  @override
  DarkVesselCandidatePayload status(
    DarkVesselCandidatePayloadStatusEnum status,
  ) => call(status: status);

  @override
  DarkVesselCandidatePayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  DarkVesselCandidatePayload mmsi(String mmsi) => call(mmsi: mmsi);

  @override
  DarkVesselCandidatePayload gapStart(DateTime gapStart) =>
      call(gapStart: gapStart);

  @override
  DarkVesselCandidatePayload detectionId(String detectionId) =>
      call(detectionId: detectionId);

  @override
  DarkVesselCandidatePayload distanceKm(num? distanceKm) =>
      call(distanceKm: distanceKm);

  @override
  DarkVesselCandidatePayload timeOffsetMin(num? timeOffsetMin) =>
      call(timeOffsetMin: timeOffsetMin);

  @override
  DarkVesselCandidatePayload detectionLengthM(num? detectionLengthM) =>
      call(detectionLengthM: detectionLengthM);

  @override
  DarkVesselCandidatePayload matchScore(num matchScore) =>
      call(matchScore: matchScore);

  @override
  DarkVesselCandidatePayload label(
    DarkVesselCandidatePayloadLabelEnum? label,
  ) => call(label: label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DarkVesselCandidatePayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DarkVesselCandidatePayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DarkVesselCandidatePayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? mmsi = const $CopyWithPlaceholder(),
    Object? gapStart = const $CopyWithPlaceholder(),
    Object? detectionId = const $CopyWithPlaceholder(),
    Object? distanceKm = const $CopyWithPlaceholder(),
    Object? timeOffsetMin = const $CopyWithPlaceholder(),
    Object? detectionLengthM = const $CopyWithPlaceholder(),
    Object? matchScore = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
  }) {
    return DarkVesselCandidatePayload(
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
          : status as DarkVesselCandidatePayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      mmsi: mmsi == const $CopyWithPlaceholder() || mmsi == null
          ? _value.mmsi
          // ignore: cast_nullable_to_non_nullable
          : mmsi as String,
      gapStart: gapStart == const $CopyWithPlaceholder() || gapStart == null
          ? _value.gapStart
          // ignore: cast_nullable_to_non_nullable
          : gapStart as DateTime,
      detectionId:
          detectionId == const $CopyWithPlaceholder() || detectionId == null
          ? _value.detectionId
          // ignore: cast_nullable_to_non_nullable
          : detectionId as String,
      distanceKm: distanceKm == const $CopyWithPlaceholder()
          ? _value.distanceKm
          // ignore: cast_nullable_to_non_nullable
          : distanceKm as num?,
      timeOffsetMin: timeOffsetMin == const $CopyWithPlaceholder()
          ? _value.timeOffsetMin
          // ignore: cast_nullable_to_non_nullable
          : timeOffsetMin as num?,
      detectionLengthM: detectionLengthM == const $CopyWithPlaceholder()
          ? _value.detectionLengthM
          // ignore: cast_nullable_to_non_nullable
          : detectionLengthM as num?,
      matchScore:
          matchScore == const $CopyWithPlaceholder() || matchScore == null
          ? _value.matchScore
          // ignore: cast_nullable_to_non_nullable
          : matchScore as num,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as DarkVesselCandidatePayloadLabelEnum?,
    );
  }
}

extension $DarkVesselCandidatePayloadCopyWith on DarkVesselCandidatePayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDarkVesselCandidatePayload.copyWith(...)` or `instanceOfDarkVesselCandidatePayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DarkVesselCandidatePayloadCWProxy get copyWith =>
      _$DarkVesselCandidatePayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DarkVesselCandidatePayload _$DarkVesselCandidatePayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DarkVesselCandidatePayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'automated',
      'notAWarning',
      'detector',
      'status',
      'mmsi',
      'gapStart',
      'detectionId',
      'matchScore',
    ],
  );
  final val = DarkVesselCandidatePayload(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$DarkVesselCandidatePayloadStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    mmsi: $checkedConvert('mmsi', (v) => v as String),
    gapStart: $checkedConvert('gapStart', (v) => DateTime.parse(v as String)),
    detectionId: $checkedConvert('detectionId', (v) => v as String),
    distanceKm: $checkedConvert('distanceKm', (v) => v as num?),
    timeOffsetMin: $checkedConvert('timeOffsetMin', (v) => v as num?),
    detectionLengthM: $checkedConvert('detectionLengthM', (v) => v as num?),
    matchScore: $checkedConvert('matchScore', (v) => v as num),
    label: $checkedConvert(
      'label',
      (v) =>
          $enumDecodeNullable(_$DarkVesselCandidatePayloadLabelEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$DarkVesselCandidatePayloadToJson(
  DarkVesselCandidatePayload instance,
) => <String, dynamic>{
  'automated': instance.automated,
  'notAWarning': instance.notAWarning,
  'detector': instance.detector.toJson(),
  'confidence': ?instance.confidence,
  'status': _$DarkVesselCandidatePayloadStatusEnumEnumMap[instance.status]!,
  'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
  'mmsi': instance.mmsi,
  'gapStart': instance.gapStart.toIso8601String(),
  'detectionId': instance.detectionId,
  'distanceKm': ?instance.distanceKm,
  'timeOffsetMin': ?instance.timeOffsetMin,
  'detectionLengthM': ?instance.detectionLengthM,
  'matchScore': instance.matchScore,
  'label': ?_$DarkVesselCandidatePayloadLabelEnumEnumMap[instance.label],
};

const _$DarkVesselCandidatePayloadStatusEnumEnumMap = {
  DarkVesselCandidatePayloadStatusEnum.ok: 'ok',
  DarkVesselCandidatePayloadStatusEnum.insufficientData: 'insufficient-data',
  DarkVesselCandidatePayloadStatusEnum.noCoverage: 'no-coverage',
  DarkVesselCandidatePayloadStatusEnum.degradedInput: 'degraded-input',
};

const _$DarkVesselCandidatePayloadLabelEnumEnumMap = {
  DarkVesselCandidatePayloadLabelEnum
          .candidateSemicolonSARDetectionsAreNotIdentifications:
      'candidate; SAR detections are not identifications',
};
