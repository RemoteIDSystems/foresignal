// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analog_result_outcome_distribution.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalogResultOutcomeDistributionCWProxy {
  AnalogResultOutcomeDistribution outcome(String outcome);

  AnalogResultOutcomeDistribution analogCount(int analogCount);

  AnalogResultOutcomeDistribution hits(int hits);

  AnalogResultOutcomeDistribution frequency(num? frequency);

  AnalogResultOutcomeDistribution baseRate(num? baseRate);

  AnalogResultOutcomeDistribution baseRateId(String? baseRateId);

  AnalogResultOutcomeDistribution probability(num probability);

  AnalogResultOutcomeDistribution interval(StatInterval interval);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalogResultOutcomeDistribution(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalogResultOutcomeDistribution(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalogResultOutcomeDistribution call({
    String outcome,
    int analogCount,
    int hits,
    num? frequency,
    num? baseRate,
    String? baseRateId,
    num probability,
    StatInterval interval,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalogResultOutcomeDistribution.copyWith(...)` or call `instanceOfAnalogResultOutcomeDistribution.copyWith.fieldName(value)` for a single field.
class _$AnalogResultOutcomeDistributionCWProxyImpl
    implements _$AnalogResultOutcomeDistributionCWProxy {
  const _$AnalogResultOutcomeDistributionCWProxyImpl(this._value);

  final AnalogResultOutcomeDistribution _value;

  @override
  AnalogResultOutcomeDistribution outcome(String outcome) =>
      call(outcome: outcome);

  @override
  AnalogResultOutcomeDistribution analogCount(int analogCount) =>
      call(analogCount: analogCount);

  @override
  AnalogResultOutcomeDistribution hits(int hits) => call(hits: hits);

  @override
  AnalogResultOutcomeDistribution frequency(num? frequency) =>
      call(frequency: frequency);

  @override
  AnalogResultOutcomeDistribution baseRate(num? baseRate) =>
      call(baseRate: baseRate);

  @override
  AnalogResultOutcomeDistribution baseRateId(String? baseRateId) =>
      call(baseRateId: baseRateId);

  @override
  AnalogResultOutcomeDistribution probability(num probability) =>
      call(probability: probability);

  @override
  AnalogResultOutcomeDistribution interval(StatInterval interval) =>
      call(interval: interval);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalogResultOutcomeDistribution(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalogResultOutcomeDistribution(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalogResultOutcomeDistribution call({
    Object? outcome = const $CopyWithPlaceholder(),
    Object? analogCount = const $CopyWithPlaceholder(),
    Object? hits = const $CopyWithPlaceholder(),
    Object? frequency = const $CopyWithPlaceholder(),
    Object? baseRate = const $CopyWithPlaceholder(),
    Object? baseRateId = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
    Object? interval = const $CopyWithPlaceholder(),
  }) {
    return AnalogResultOutcomeDistribution(
      outcome: outcome == const $CopyWithPlaceholder() || outcome == null
          ? _value.outcome
          // ignore: cast_nullable_to_non_nullable
          : outcome as String,
      analogCount:
          analogCount == const $CopyWithPlaceholder() || analogCount == null
          ? _value.analogCount
          // ignore: cast_nullable_to_non_nullable
          : analogCount as int,
      hits: hits == const $CopyWithPlaceholder() || hits == null
          ? _value.hits
          // ignore: cast_nullable_to_non_nullable
          : hits as int,
      frequency: frequency == const $CopyWithPlaceholder()
          ? _value.frequency
          // ignore: cast_nullable_to_non_nullable
          : frequency as num?,
      baseRate: baseRate == const $CopyWithPlaceholder()
          ? _value.baseRate
          // ignore: cast_nullable_to_non_nullable
          : baseRate as num?,
      baseRateId: baseRateId == const $CopyWithPlaceholder()
          ? _value.baseRateId
          // ignore: cast_nullable_to_non_nullable
          : baseRateId as String?,
      probability:
          probability == const $CopyWithPlaceholder() || probability == null
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num,
      interval: interval == const $CopyWithPlaceholder() || interval == null
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as StatInterval,
    );
  }
}

extension $AnalogResultOutcomeDistributionCopyWith
    on AnalogResultOutcomeDistribution {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalogResultOutcomeDistribution.copyWith(...)` or `instanceOfAnalogResultOutcomeDistribution.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalogResultOutcomeDistributionCWProxy get copyWith =>
      _$AnalogResultOutcomeDistributionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalogResultOutcomeDistribution _$AnalogResultOutcomeDistributionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalogResultOutcomeDistribution', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'outcome',
      'analogCount',
      'hits',
      'probability',
      'interval',
    ],
  );
  final val = AnalogResultOutcomeDistribution(
    outcome: $checkedConvert('outcome', (v) => v as String),
    analogCount: $checkedConvert('analogCount', (v) => (v as num).toInt()),
    hits: $checkedConvert('hits', (v) => (v as num).toInt()),
    frequency: $checkedConvert('frequency', (v) => v as num?),
    baseRate: $checkedConvert('baseRate', (v) => v as num?),
    baseRateId: $checkedConvert('baseRateId', (v) => v as String?),
    probability: $checkedConvert('probability', (v) => v as num),
    interval: $checkedConvert(
      'interval',
      (v) => StatInterval.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$AnalogResultOutcomeDistributionToJson(
  AnalogResultOutcomeDistribution instance,
) => <String, dynamic>{
  'outcome': instance.outcome,
  'analogCount': instance.analogCount,
  'hits': instance.hits,
  'frequency': ?instance.frequency,
  'baseRate': ?instance.baseRate,
  'baseRateId': ?instance.baseRateId,
  'probability': instance.probability,
  'interval': instance.interval.toJson(),
};
