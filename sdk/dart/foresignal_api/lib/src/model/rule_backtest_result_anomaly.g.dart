// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rule_backtest_result_anomaly.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RuleBacktestResultAnomalyCWProxy {
  RuleBacktestResultAnomaly warmupConsumed(String? warmupConsumed);

  RuleBacktestResultAnomaly byClassification(
    Map<String, int>? byClassification,
  );

  RuleBacktestResultAnomaly coverageDrops(int? coverageDrops);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResultAnomaly(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResultAnomaly(...).copyWith(id: 12, name: "My name")
  /// ```
  RuleBacktestResultAnomaly call({
    String? warmupConsumed,
    Map<String, int>? byClassification,
    int? coverageDrops,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRuleBacktestResultAnomaly.copyWith(...)` or call `instanceOfRuleBacktestResultAnomaly.copyWith.fieldName(value)` for a single field.
class _$RuleBacktestResultAnomalyCWProxyImpl
    implements _$RuleBacktestResultAnomalyCWProxy {
  const _$RuleBacktestResultAnomalyCWProxyImpl(this._value);

  final RuleBacktestResultAnomaly _value;

  @override
  RuleBacktestResultAnomaly warmupConsumed(String? warmupConsumed) =>
      call(warmupConsumed: warmupConsumed);

  @override
  RuleBacktestResultAnomaly byClassification(
    Map<String, int>? byClassification,
  ) => call(byClassification: byClassification);

  @override
  RuleBacktestResultAnomaly coverageDrops(int? coverageDrops) =>
      call(coverageDrops: coverageDrops);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResultAnomaly(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResultAnomaly(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RuleBacktestResultAnomaly call({
    Object? warmupConsumed = const $CopyWithPlaceholder(),
    Object? byClassification = const $CopyWithPlaceholder(),
    Object? coverageDrops = const $CopyWithPlaceholder(),
  }) {
    return RuleBacktestResultAnomaly(
      warmupConsumed: warmupConsumed == const $CopyWithPlaceholder()
          ? _value.warmupConsumed
          // ignore: cast_nullable_to_non_nullable
          : warmupConsumed as String?,
      byClassification: byClassification == const $CopyWithPlaceholder()
          ? _value.byClassification
          // ignore: cast_nullable_to_non_nullable
          : byClassification as Map<String, int>?,
      coverageDrops: coverageDrops == const $CopyWithPlaceholder()
          ? _value.coverageDrops
          // ignore: cast_nullable_to_non_nullable
          : coverageDrops as int?,
    );
  }
}

extension $RuleBacktestResultAnomalyCopyWith on RuleBacktestResultAnomaly {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRuleBacktestResultAnomaly.copyWith(...)` or `instanceOfRuleBacktestResultAnomaly.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RuleBacktestResultAnomalyCWProxy get copyWith =>
      _$RuleBacktestResultAnomalyCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RuleBacktestResultAnomaly _$RuleBacktestResultAnomalyFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RuleBacktestResultAnomaly', json, ($checkedConvert) {
  final val = RuleBacktestResultAnomaly(
    warmupConsumed: $checkedConvert('warmupConsumed', (v) => v as String?),
    byClassification: $checkedConvert(
      'byClassification',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, (e as num).toInt()),
      ),
    ),
    coverageDrops: $checkedConvert(
      'coverageDrops',
      (v) => (v as num?)?.toInt(),
    ),
  );
  return val;
});

Map<String, dynamic> _$RuleBacktestResultAnomalyToJson(
  RuleBacktestResultAnomaly instance,
) => <String, dynamic>{
  'warmupConsumed': ?instance.warmupConsumed,
  'byClassification': ?instance.byClassification,
  'coverageDrops': ?instance.coverageDrops,
};
