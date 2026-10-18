// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'anomaly_baseline.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnomalyBaselineCWProxy {
  AnomalyBaseline ruleId(String ruleId);

  AnomalyBaseline slotMode(AnomalyBaselineSlotModeEnum slotMode);

  AnomalyBaseline bucket(AnomalyBaselineBucketEnum bucket);

  AnomalyBaseline weeksAvailable(num weeksAvailable);

  AnomalyBaseline minWeeks(int? minWeeks);

  AnomalyBaseline learning(bool learning);

  AnomalyBaseline learningUntil(DateTime? learningUntil);

  AnomalyBaseline lowConfidence(bool? lowConfidence);

  AnomalyBaseline samplePath(AnomalyBaselineSamplePathEnum? samplePath);

  AnomalyBaseline slots(List<AnomalyBaselineSlot> slots);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyBaseline(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyBaseline(...).copyWith(id: 12, name: "My name")
  /// ```
  AnomalyBaseline call({
    String ruleId,
    AnomalyBaselineSlotModeEnum slotMode,
    AnomalyBaselineBucketEnum bucket,
    num weeksAvailable,
    int? minWeeks,
    bool learning,
    DateTime? learningUntil,
    bool? lowConfidence,
    AnomalyBaselineSamplePathEnum? samplePath,
    List<AnomalyBaselineSlot> slots,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnomalyBaseline.copyWith(...)` or call `instanceOfAnomalyBaseline.copyWith.fieldName(value)` for a single field.
class _$AnomalyBaselineCWProxyImpl implements _$AnomalyBaselineCWProxy {
  const _$AnomalyBaselineCWProxyImpl(this._value);

  final AnomalyBaseline _value;

  @override
  AnomalyBaseline ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  AnomalyBaseline slotMode(AnomalyBaselineSlotModeEnum slotMode) =>
      call(slotMode: slotMode);

  @override
  AnomalyBaseline bucket(AnomalyBaselineBucketEnum bucket) =>
      call(bucket: bucket);

  @override
  AnomalyBaseline weeksAvailable(num weeksAvailable) =>
      call(weeksAvailable: weeksAvailable);

  @override
  AnomalyBaseline minWeeks(int? minWeeks) => call(minWeeks: minWeeks);

  @override
  AnomalyBaseline learning(bool learning) => call(learning: learning);

  @override
  AnomalyBaseline learningUntil(DateTime? learningUntil) =>
      call(learningUntil: learningUntil);

  @override
  AnomalyBaseline lowConfidence(bool? lowConfidence) =>
      call(lowConfidence: lowConfidence);

  @override
  AnomalyBaseline samplePath(AnomalyBaselineSamplePathEnum? samplePath) =>
      call(samplePath: samplePath);

  @override
  AnomalyBaseline slots(List<AnomalyBaselineSlot> slots) => call(slots: slots);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyBaseline(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyBaseline(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnomalyBaseline call({
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? slotMode = const $CopyWithPlaceholder(),
    Object? bucket = const $CopyWithPlaceholder(),
    Object? weeksAvailable = const $CopyWithPlaceholder(),
    Object? minWeeks = const $CopyWithPlaceholder(),
    Object? learning = const $CopyWithPlaceholder(),
    Object? learningUntil = const $CopyWithPlaceholder(),
    Object? lowConfidence = const $CopyWithPlaceholder(),
    Object? samplePath = const $CopyWithPlaceholder(),
    Object? slots = const $CopyWithPlaceholder(),
  }) {
    return AnomalyBaseline(
      ruleId: ruleId == const $CopyWithPlaceholder() || ruleId == null
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String,
      slotMode: slotMode == const $CopyWithPlaceholder() || slotMode == null
          ? _value.slotMode
          // ignore: cast_nullable_to_non_nullable
          : slotMode as AnomalyBaselineSlotModeEnum,
      bucket: bucket == const $CopyWithPlaceholder() || bucket == null
          ? _value.bucket
          // ignore: cast_nullable_to_non_nullable
          : bucket as AnomalyBaselineBucketEnum,
      weeksAvailable:
          weeksAvailable == const $CopyWithPlaceholder() ||
              weeksAvailable == null
          ? _value.weeksAvailable
          // ignore: cast_nullable_to_non_nullable
          : weeksAvailable as num,
      minWeeks: minWeeks == const $CopyWithPlaceholder()
          ? _value.minWeeks
          // ignore: cast_nullable_to_non_nullable
          : minWeeks as int?,
      learning: learning == const $CopyWithPlaceholder() || learning == null
          ? _value.learning
          // ignore: cast_nullable_to_non_nullable
          : learning as bool,
      learningUntil: learningUntil == const $CopyWithPlaceholder()
          ? _value.learningUntil
          // ignore: cast_nullable_to_non_nullable
          : learningUntil as DateTime?,
      lowConfidence: lowConfidence == const $CopyWithPlaceholder()
          ? _value.lowConfidence
          // ignore: cast_nullable_to_non_nullable
          : lowConfidence as bool?,
      samplePath: samplePath == const $CopyWithPlaceholder()
          ? _value.samplePath
          // ignore: cast_nullable_to_non_nullable
          : samplePath as AnomalyBaselineSamplePathEnum?,
      slots: slots == const $CopyWithPlaceholder() || slots == null
          ? _value.slots
          // ignore: cast_nullable_to_non_nullable
          : slots as List<AnomalyBaselineSlot>,
    );
  }
}

extension $AnomalyBaselineCopyWith on AnomalyBaseline {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnomalyBaseline.copyWith(...)` or `instanceOfAnomalyBaseline.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnomalyBaselineCWProxy get copyWith => _$AnomalyBaselineCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnomalyBaseline _$AnomalyBaselineFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnomalyBaseline', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'ruleId',
          'slotMode',
          'bucket',
          'weeksAvailable',
          'learning',
          'slots',
        ],
      );
      final val = AnomalyBaseline(
        ruleId: $checkedConvert('ruleId', (v) => v as String),
        slotMode: $checkedConvert(
          'slotMode',
          (v) => $enumDecode(_$AnomalyBaselineSlotModeEnumEnumMap, v),
        ),
        bucket: $checkedConvert(
          'bucket',
          (v) => $enumDecode(_$AnomalyBaselineBucketEnumEnumMap, v),
        ),
        weeksAvailable: $checkedConvert('weeksAvailable', (v) => v as num),
        minWeeks: $checkedConvert('minWeeks', (v) => (v as num?)?.toInt()),
        learning: $checkedConvert('learning', (v) => v as bool),
        learningUntil: $checkedConvert(
          'learningUntil',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        lowConfidence: $checkedConvert('lowConfidence', (v) => v as bool?),
        samplePath: $checkedConvert(
          'samplePath',
          (v) => $enumDecodeNullable(_$AnomalyBaselineSamplePathEnumEnumMap, v),
        ),
        slots: $checkedConvert(
          'slots',
          (v) => (v as List<dynamic>)
              .map(
                (e) => AnomalyBaselineSlot.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AnomalyBaselineToJson(
  AnomalyBaseline instance,
) => <String, dynamic>{
  'ruleId': instance.ruleId,
  'slotMode': _$AnomalyBaselineSlotModeEnumEnumMap[instance.slotMode]!,
  'bucket': _$AnomalyBaselineBucketEnumEnumMap[instance.bucket]!,
  'weeksAvailable': instance.weeksAvailable,
  'minWeeks': ?instance.minWeeks,
  'learning': instance.learning,
  'learningUntil': ?instance.learningUntil?.toIso8601String(),
  'lowConfidence': ?instance.lowConfidence,
  'samplePath': ?_$AnomalyBaselineSamplePathEnumEnumMap[instance.samplePath],
  'slots': instance.slots.map((e) => e.toJson()).toList(),
};

const _$AnomalyBaselineSlotModeEnumEnumMap = {
  AnomalyBaselineSlotModeEnum.hourOfWeek: 'hour-of-week',
  AnomalyBaselineSlotModeEnum.hourOfDay: 'hour-of-day',
};

const _$AnomalyBaselineBucketEnumEnumMap = {
  AnomalyBaselineBucketEnum.n15m: '15m',
  AnomalyBaselineBucketEnum.n1h: '1h',
};

const _$AnomalyBaselineSamplePathEnumEnumMap = {
  AnomalyBaselineSamplePathEnum.activitySample: 'activity_sample',
  AnomalyBaselineSamplePathEnum.activityRollup: 'activity_rollup',
};
