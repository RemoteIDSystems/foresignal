// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'anomaly_observation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnomalyObservationCWProxy {
  AnomalyObservation id(int id);

  AnomalyObservation ruleId(String ruleId);

  AnomalyObservation groupKey(String groupKey);

  AnomalyObservation bucketStart(DateTime bucketStart);

  AnomalyObservation bucketEnd(DateTime bucketEnd);

  AnomalyObservation observed(int observed);

  AnomalyObservation expected(num expected);

  AnomalyObservation exposure(num? exposure);

  AnomalyObservation pValue(num? pValue);

  AnomalyObservation score(num? score);

  AnomalyObservation direction(AnomalyObservationDirectionEnum? direction);

  AnomalyObservation classification(
    AnomalyObservationClassificationEnum classification,
  );

  AnomalyObservation lowConfidence(bool? lowConfidence);

  AnomalyObservation baseline(AnomalyObservationBaseline baseline);

  AnomalyObservation detector(DetectorRef detector);

  AnomalyObservation evidence(AnomalyObservationEvidence? evidence);

  AnomalyObservation watermarkSeq(int watermarkSeq);

  AnomalyObservation alertId(int? alertId);

  AnomalyObservation manifestId(String? manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyObservation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyObservation(...).copyWith(id: 12, name: "My name")
  /// ```
  AnomalyObservation call({
    int id,
    String ruleId,
    String groupKey,
    DateTime bucketStart,
    DateTime bucketEnd,
    int observed,
    num expected,
    num? exposure,
    num? pValue,
    num? score,
    AnomalyObservationDirectionEnum? direction,
    AnomalyObservationClassificationEnum classification,
    bool? lowConfidence,
    AnomalyObservationBaseline baseline,
    DetectorRef detector,
    AnomalyObservationEvidence? evidence,
    int watermarkSeq,
    int? alertId,
    String? manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnomalyObservation.copyWith(...)` or call `instanceOfAnomalyObservation.copyWith.fieldName(value)` for a single field.
class _$AnomalyObservationCWProxyImpl implements _$AnomalyObservationCWProxy {
  const _$AnomalyObservationCWProxyImpl(this._value);

  final AnomalyObservation _value;

  @override
  AnomalyObservation id(int id) => call(id: id);

  @override
  AnomalyObservation ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  AnomalyObservation groupKey(String groupKey) => call(groupKey: groupKey);

  @override
  AnomalyObservation bucketStart(DateTime bucketStart) =>
      call(bucketStart: bucketStart);

  @override
  AnomalyObservation bucketEnd(DateTime bucketEnd) =>
      call(bucketEnd: bucketEnd);

  @override
  AnomalyObservation observed(int observed) => call(observed: observed);

  @override
  AnomalyObservation expected(num expected) => call(expected: expected);

  @override
  AnomalyObservation exposure(num? exposure) => call(exposure: exposure);

  @override
  AnomalyObservation pValue(num? pValue) => call(pValue: pValue);

  @override
  AnomalyObservation score(num? score) => call(score: score);

  @override
  AnomalyObservation direction(AnomalyObservationDirectionEnum? direction) =>
      call(direction: direction);

  @override
  AnomalyObservation classification(
    AnomalyObservationClassificationEnum classification,
  ) => call(classification: classification);

  @override
  AnomalyObservation lowConfidence(bool? lowConfidence) =>
      call(lowConfidence: lowConfidence);

  @override
  AnomalyObservation baseline(AnomalyObservationBaseline baseline) =>
      call(baseline: baseline);

  @override
  AnomalyObservation detector(DetectorRef detector) => call(detector: detector);

  @override
  AnomalyObservation evidence(AnomalyObservationEvidence? evidence) =>
      call(evidence: evidence);

  @override
  AnomalyObservation watermarkSeq(int watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  @override
  AnomalyObservation alertId(int? alertId) => call(alertId: alertId);

  @override
  AnomalyObservation manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyObservation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyObservation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnomalyObservation call({
    Object? id = const $CopyWithPlaceholder(),
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? groupKey = const $CopyWithPlaceholder(),
    Object? bucketStart = const $CopyWithPlaceholder(),
    Object? bucketEnd = const $CopyWithPlaceholder(),
    Object? observed = const $CopyWithPlaceholder(),
    Object? expected = const $CopyWithPlaceholder(),
    Object? exposure = const $CopyWithPlaceholder(),
    Object? pValue = const $CopyWithPlaceholder(),
    Object? score = const $CopyWithPlaceholder(),
    Object? direction = const $CopyWithPlaceholder(),
    Object? classification = const $CopyWithPlaceholder(),
    Object? lowConfidence = const $CopyWithPlaceholder(),
    Object? baseline = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
    Object? alertId = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return AnomalyObservation(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      ruleId: ruleId == const $CopyWithPlaceholder() || ruleId == null
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String,
      groupKey: groupKey == const $CopyWithPlaceholder() || groupKey == null
          ? _value.groupKey
          // ignore: cast_nullable_to_non_nullable
          : groupKey as String,
      bucketStart:
          bucketStart == const $CopyWithPlaceholder() || bucketStart == null
          ? _value.bucketStart
          // ignore: cast_nullable_to_non_nullable
          : bucketStart as DateTime,
      bucketEnd: bucketEnd == const $CopyWithPlaceholder() || bucketEnd == null
          ? _value.bucketEnd
          // ignore: cast_nullable_to_non_nullable
          : bucketEnd as DateTime,
      observed: observed == const $CopyWithPlaceholder() || observed == null
          ? _value.observed
          // ignore: cast_nullable_to_non_nullable
          : observed as int,
      expected: expected == const $CopyWithPlaceholder() || expected == null
          ? _value.expected
          // ignore: cast_nullable_to_non_nullable
          : expected as num,
      exposure: exposure == const $CopyWithPlaceholder()
          ? _value.exposure
          // ignore: cast_nullable_to_non_nullable
          : exposure as num?,
      pValue: pValue == const $CopyWithPlaceholder()
          ? _value.pValue
          // ignore: cast_nullable_to_non_nullable
          : pValue as num?,
      score: score == const $CopyWithPlaceholder()
          ? _value.score
          // ignore: cast_nullable_to_non_nullable
          : score as num?,
      direction: direction == const $CopyWithPlaceholder()
          ? _value.direction
          // ignore: cast_nullable_to_non_nullable
          : direction as AnomalyObservationDirectionEnum?,
      classification:
          classification == const $CopyWithPlaceholder() ||
              classification == null
          ? _value.classification
          // ignore: cast_nullable_to_non_nullable
          : classification as AnomalyObservationClassificationEnum,
      lowConfidence: lowConfidence == const $CopyWithPlaceholder()
          ? _value.lowConfidence
          // ignore: cast_nullable_to_non_nullable
          : lowConfidence as bool?,
      baseline: baseline == const $CopyWithPlaceholder() || baseline == null
          ? _value.baseline
          // ignore: cast_nullable_to_non_nullable
          : baseline as AnomalyObservationBaseline,
      detector: detector == const $CopyWithPlaceholder() || detector == null
          ? _value.detector
          // ignore: cast_nullable_to_non_nullable
          : detector as DetectorRef,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as AnomalyObservationEvidence?,
      watermarkSeq:
          watermarkSeq == const $CopyWithPlaceholder() || watermarkSeq == null
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int,
      alertId: alertId == const $CopyWithPlaceholder()
          ? _value.alertId
          // ignore: cast_nullable_to_non_nullable
          : alertId as int?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
    );
  }
}

extension $AnomalyObservationCopyWith on AnomalyObservation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnomalyObservation.copyWith(...)` or `instanceOfAnomalyObservation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnomalyObservationCWProxy get copyWith =>
      _$AnomalyObservationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnomalyObservation _$AnomalyObservationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnomalyObservation', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'ruleId',
      'groupKey',
      'bucketStart',
      'bucketEnd',
      'observed',
      'expected',
      'classification',
      'baseline',
      'detector',
      'watermarkSeq',
    ],
  );
  final val = AnomalyObservation(
    id: $checkedConvert('id', (v) => (v as num).toInt()),
    ruleId: $checkedConvert('ruleId', (v) => v as String),
    groupKey: $checkedConvert('groupKey', (v) => v as String),
    bucketStart: $checkedConvert(
      'bucketStart',
      (v) => DateTime.parse(v as String),
    ),
    bucketEnd: $checkedConvert('bucketEnd', (v) => DateTime.parse(v as String)),
    observed: $checkedConvert('observed', (v) => (v as num).toInt()),
    expected: $checkedConvert('expected', (v) => v as num),
    exposure: $checkedConvert('exposure', (v) => v as num?),
    pValue: $checkedConvert('pValue', (v) => v as num?),
    score: $checkedConvert('score', (v) => v as num?),
    direction: $checkedConvert(
      'direction',
      (v) => $enumDecodeNullable(_$AnomalyObservationDirectionEnumEnumMap, v),
    ),
    classification: $checkedConvert(
      'classification',
      (v) => $enumDecode(_$AnomalyObservationClassificationEnumEnumMap, v),
    ),
    lowConfidence: $checkedConvert('lowConfidence', (v) => v as bool?),
    baseline: $checkedConvert(
      'baseline',
      (v) => AnomalyObservationBaseline.fromJson(v as Map<String, dynamic>),
    ),
    detector: $checkedConvert(
      'detector',
      (v) => DetectorRef.fromJson(v as Map<String, dynamic>),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => v == null
          ? null
          : AnomalyObservationEvidence.fromJson(v as Map<String, dynamic>),
    ),
    watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num).toInt()),
    alertId: $checkedConvert('alertId', (v) => (v as num?)?.toInt()),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$AnomalyObservationToJson(
  AnomalyObservation instance,
) => <String, dynamic>{
  'id': instance.id,
  'ruleId': instance.ruleId,
  'groupKey': instance.groupKey,
  'bucketStart': instance.bucketStart.toIso8601String(),
  'bucketEnd': instance.bucketEnd.toIso8601String(),
  'observed': instance.observed,
  'expected': instance.expected,
  'exposure': ?instance.exposure,
  'pValue': ?instance.pValue,
  'score': ?instance.score,
  'direction': ?_$AnomalyObservationDirectionEnumEnumMap[instance.direction],
  'classification':
      _$AnomalyObservationClassificationEnumEnumMap[instance.classification]!,
  'lowConfidence': ?instance.lowConfidence,
  'baseline': instance.baseline.toJson(),
  'detector': instance.detector.toJson(),
  'evidence': ?instance.evidence?.toJson(),
  'watermarkSeq': instance.watermarkSeq,
  'alertId': ?instance.alertId,
  'manifestId': ?instance.manifestId,
};

const _$AnomalyObservationDirectionEnumEnumMap = {
  AnomalyObservationDirectionEnum.up: 'up',
  AnomalyObservationDirectionEnum.down: 'down',
};

const _$AnomalyObservationClassificationEnumEnumMap = {
  AnomalyObservationClassificationEnum.activityUp: 'activity-up',
  AnomalyObservationClassificationEnum.activityDown: 'activity-down',
  AnomalyObservationClassificationEnum.coverageDrop: 'coverage-drop',
  AnomalyObservationClassificationEnum.insufficientCoverage:
      'insufficient-coverage',
  AnomalyObservationClassificationEnum.learning: 'learning',
  AnomalyObservationClassificationEnum.normal: 'normal',
};
