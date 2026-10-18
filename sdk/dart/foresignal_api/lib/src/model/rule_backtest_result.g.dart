// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rule_backtest_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RuleBacktestResultCWProxy {
  RuleBacktestResult ruleType(RuleBacktestResultRuleTypeEnum ruleType);

  RuleBacktestResult wouldFire(int wouldFire);

  RuleBacktestResult perDay(List<RuleBacktestResultPerDayInner>? perDay);

  RuleBacktestResult topEntities(
    List<RuleBacktestResultTopEntitiesInner>? topEntities,
  );

  RuleBacktestResult samples(List<RuleBacktestResultSamplesInner>? samples);

  RuleBacktestResult actual(RuleBacktestResultActual? actual);

  RuleBacktestResult overlap(RuleBacktestResultOverlap? overlap);

  RuleBacktestResult window(RuleBacktestResultWindow? window);

  RuleBacktestResult anomaly(RuleBacktestResultAnomaly? anomaly);

  RuleBacktestResult variants(List<RuleBacktestResultVariantsInner>? variants);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  RuleBacktestResult call({
    RuleBacktestResultRuleTypeEnum ruleType,
    int wouldFire,
    List<RuleBacktestResultPerDayInner>? perDay,
    List<RuleBacktestResultTopEntitiesInner>? topEntities,
    List<RuleBacktestResultSamplesInner>? samples,
    RuleBacktestResultActual? actual,
    RuleBacktestResultOverlap? overlap,
    RuleBacktestResultWindow? window,
    RuleBacktestResultAnomaly? anomaly,
    List<RuleBacktestResultVariantsInner>? variants,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRuleBacktestResult.copyWith(...)` or call `instanceOfRuleBacktestResult.copyWith.fieldName(value)` for a single field.
class _$RuleBacktestResultCWProxyImpl implements _$RuleBacktestResultCWProxy {
  const _$RuleBacktestResultCWProxyImpl(this._value);

  final RuleBacktestResult _value;

  @override
  RuleBacktestResult ruleType(RuleBacktestResultRuleTypeEnum ruleType) =>
      call(ruleType: ruleType);

  @override
  RuleBacktestResult wouldFire(int wouldFire) => call(wouldFire: wouldFire);

  @override
  RuleBacktestResult perDay(List<RuleBacktestResultPerDayInner>? perDay) =>
      call(perDay: perDay);

  @override
  RuleBacktestResult topEntities(
    List<RuleBacktestResultTopEntitiesInner>? topEntities,
  ) => call(topEntities: topEntities);

  @override
  RuleBacktestResult samples(List<RuleBacktestResultSamplesInner>? samples) =>
      call(samples: samples);

  @override
  RuleBacktestResult actual(RuleBacktestResultActual? actual) =>
      call(actual: actual);

  @override
  RuleBacktestResult overlap(RuleBacktestResultOverlap? overlap) =>
      call(overlap: overlap);

  @override
  RuleBacktestResult window(RuleBacktestResultWindow? window) =>
      call(window: window);

  @override
  RuleBacktestResult anomaly(RuleBacktestResultAnomaly? anomaly) =>
      call(anomaly: anomaly);

  @override
  RuleBacktestResult variants(
    List<RuleBacktestResultVariantsInner>? variants,
  ) => call(variants: variants);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RuleBacktestResult call({
    Object? ruleType = const $CopyWithPlaceholder(),
    Object? wouldFire = const $CopyWithPlaceholder(),
    Object? perDay = const $CopyWithPlaceholder(),
    Object? topEntities = const $CopyWithPlaceholder(),
    Object? samples = const $CopyWithPlaceholder(),
    Object? actual = const $CopyWithPlaceholder(),
    Object? overlap = const $CopyWithPlaceholder(),
    Object? window = const $CopyWithPlaceholder(),
    Object? anomaly = const $CopyWithPlaceholder(),
    Object? variants = const $CopyWithPlaceholder(),
  }) {
    return RuleBacktestResult(
      ruleType: ruleType == const $CopyWithPlaceholder() || ruleType == null
          ? _value.ruleType
          // ignore: cast_nullable_to_non_nullable
          : ruleType as RuleBacktestResultRuleTypeEnum,
      wouldFire: wouldFire == const $CopyWithPlaceholder() || wouldFire == null
          ? _value.wouldFire
          // ignore: cast_nullable_to_non_nullable
          : wouldFire as int,
      perDay: perDay == const $CopyWithPlaceholder()
          ? _value.perDay
          // ignore: cast_nullable_to_non_nullable
          : perDay as List<RuleBacktestResultPerDayInner>?,
      topEntities: topEntities == const $CopyWithPlaceholder()
          ? _value.topEntities
          // ignore: cast_nullable_to_non_nullable
          : topEntities as List<RuleBacktestResultTopEntitiesInner>?,
      samples: samples == const $CopyWithPlaceholder()
          ? _value.samples
          // ignore: cast_nullable_to_non_nullable
          : samples as List<RuleBacktestResultSamplesInner>?,
      actual: actual == const $CopyWithPlaceholder()
          ? _value.actual
          // ignore: cast_nullable_to_non_nullable
          : actual as RuleBacktestResultActual?,
      overlap: overlap == const $CopyWithPlaceholder()
          ? _value.overlap
          // ignore: cast_nullable_to_non_nullable
          : overlap as RuleBacktestResultOverlap?,
      window: window == const $CopyWithPlaceholder()
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as RuleBacktestResultWindow?,
      anomaly: anomaly == const $CopyWithPlaceholder()
          ? _value.anomaly
          // ignore: cast_nullable_to_non_nullable
          : anomaly as RuleBacktestResultAnomaly?,
      variants: variants == const $CopyWithPlaceholder()
          ? _value.variants
          // ignore: cast_nullable_to_non_nullable
          : variants as List<RuleBacktestResultVariantsInner>?,
    );
  }
}

extension $RuleBacktestResultCopyWith on RuleBacktestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRuleBacktestResult.copyWith(...)` or `instanceOfRuleBacktestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RuleBacktestResultCWProxy get copyWith =>
      _$RuleBacktestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RuleBacktestResult _$RuleBacktestResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RuleBacktestResult', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['ruleType', 'wouldFire']);
  final val = RuleBacktestResult(
    ruleType: $checkedConvert(
      'ruleType',
      (v) => $enumDecode(_$RuleBacktestResultRuleTypeEnumEnumMap, v),
    ),
    wouldFire: $checkedConvert('wouldFire', (v) => (v as num).toInt()),
    perDay: $checkedConvert(
      'perDay',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => RuleBacktestResultPerDayInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    topEntities: $checkedConvert(
      'topEntities',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => RuleBacktestResultTopEntitiesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    samples: $checkedConvert(
      'samples',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => RuleBacktestResultSamplesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    actual: $checkedConvert(
      'actual',
      (v) => v == null
          ? null
          : RuleBacktestResultActual.fromJson(v as Map<String, dynamic>),
    ),
    overlap: $checkedConvert(
      'overlap',
      (v) => v == null
          ? null
          : RuleBacktestResultOverlap.fromJson(v as Map<String, dynamic>),
    ),
    window: $checkedConvert(
      'window',
      (v) => v == null
          ? null
          : RuleBacktestResultWindow.fromJson(v as Map<String, dynamic>),
    ),
    anomaly: $checkedConvert(
      'anomaly',
      (v) => v == null
          ? null
          : RuleBacktestResultAnomaly.fromJson(v as Map<String, dynamic>),
    ),
    variants: $checkedConvert(
      'variants',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => RuleBacktestResultVariantsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$RuleBacktestResultToJson(RuleBacktestResult instance) =>
    <String, dynamic>{
      'ruleType': _$RuleBacktestResultRuleTypeEnumEnumMap[instance.ruleType]!,
      'wouldFire': instance.wouldFire,
      'perDay': ?instance.perDay?.map((e) => e.toJson()).toList(),
      'topEntities': ?instance.topEntities?.map((e) => e.toJson()).toList(),
      'samples': ?instance.samples?.map((e) => e.toJson()).toList(),
      'actual': ?instance.actual?.toJson(),
      'overlap': ?instance.overlap?.toJson(),
      'window': ?instance.window?.toJson(),
      'anomaly': ?instance.anomaly?.toJson(),
      'variants': ?instance.variants?.map((e) => e.toJson()).toList(),
    };

const _$RuleBacktestResultRuleTypeEnumEnumMap = {
  RuleBacktestResultRuleTypeEnum.record: 'record',
  RuleBacktestResultRuleTypeEnum.count: 'count',
  RuleBacktestResultRuleTypeEnum.absence: 'absence',
  RuleBacktestResultRuleTypeEnum.anomaly: 'anomaly',
};
