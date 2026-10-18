// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rule_backtest_result_overlap.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RuleBacktestResultOverlapCWProxy {
  RuleBacktestResultOverlap both(int? both);

  RuleBacktestResultOverlap onlyBacktest(int? onlyBacktest);

  RuleBacktestResultOverlap onlyActual(int? onlyActual);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResultOverlap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResultOverlap(...).copyWith(id: 12, name: "My name")
  /// ```
  RuleBacktestResultOverlap call({
    int? both,
    int? onlyBacktest,
    int? onlyActual,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRuleBacktestResultOverlap.copyWith(...)` or call `instanceOfRuleBacktestResultOverlap.copyWith.fieldName(value)` for a single field.
class _$RuleBacktestResultOverlapCWProxyImpl
    implements _$RuleBacktestResultOverlapCWProxy {
  const _$RuleBacktestResultOverlapCWProxyImpl(this._value);

  final RuleBacktestResultOverlap _value;

  @override
  RuleBacktestResultOverlap both(int? both) => call(both: both);

  @override
  RuleBacktestResultOverlap onlyBacktest(int? onlyBacktest) =>
      call(onlyBacktest: onlyBacktest);

  @override
  RuleBacktestResultOverlap onlyActual(int? onlyActual) =>
      call(onlyActual: onlyActual);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResultOverlap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResultOverlap(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RuleBacktestResultOverlap call({
    Object? both = const $CopyWithPlaceholder(),
    Object? onlyBacktest = const $CopyWithPlaceholder(),
    Object? onlyActual = const $CopyWithPlaceholder(),
  }) {
    return RuleBacktestResultOverlap(
      both: both == const $CopyWithPlaceholder()
          ? _value.both
          // ignore: cast_nullable_to_non_nullable
          : both as int?,
      onlyBacktest: onlyBacktest == const $CopyWithPlaceholder()
          ? _value.onlyBacktest
          // ignore: cast_nullable_to_non_nullable
          : onlyBacktest as int?,
      onlyActual: onlyActual == const $CopyWithPlaceholder()
          ? _value.onlyActual
          // ignore: cast_nullable_to_non_nullable
          : onlyActual as int?,
    );
  }
}

extension $RuleBacktestResultOverlapCopyWith on RuleBacktestResultOverlap {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRuleBacktestResultOverlap.copyWith(...)` or `instanceOfRuleBacktestResultOverlap.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RuleBacktestResultOverlapCWProxy get copyWith =>
      _$RuleBacktestResultOverlapCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RuleBacktestResultOverlap _$RuleBacktestResultOverlapFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RuleBacktestResultOverlap', json, ($checkedConvert) {
  final val = RuleBacktestResultOverlap(
    both: $checkedConvert('both', (v) => (v as num?)?.toInt()),
    onlyBacktest: $checkedConvert('onlyBacktest', (v) => (v as num?)?.toInt()),
    onlyActual: $checkedConvert('onlyActual', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$RuleBacktestResultOverlapToJson(
  RuleBacktestResultOverlap instance,
) => <String, dynamic>{
  'both': ?instance.both,
  'onlyBacktest': ?instance.onlyBacktest,
  'onlyActual': ?instance.onlyActual,
};
