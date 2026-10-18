// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rule_backtest_result_per_day_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RuleBacktestResultPerDayInnerCWProxy {
  RuleBacktestResultPerDayInner date(DateTime date);

  RuleBacktestResultPerDayInner count(int count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResultPerDayInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResultPerDayInner(...).copyWith(id: 12, name: "My name")
  /// ```
  RuleBacktestResultPerDayInner call({DateTime date, int count});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRuleBacktestResultPerDayInner.copyWith(...)` or call `instanceOfRuleBacktestResultPerDayInner.copyWith.fieldName(value)` for a single field.
class _$RuleBacktestResultPerDayInnerCWProxyImpl
    implements _$RuleBacktestResultPerDayInnerCWProxy {
  const _$RuleBacktestResultPerDayInnerCWProxyImpl(this._value);

  final RuleBacktestResultPerDayInner _value;

  @override
  RuleBacktestResultPerDayInner date(DateTime date) => call(date: date);

  @override
  RuleBacktestResultPerDayInner count(int count) => call(count: count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResultPerDayInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResultPerDayInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RuleBacktestResultPerDayInner call({
    Object? date = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
  }) {
    return RuleBacktestResultPerDayInner(
      date: date == const $CopyWithPlaceholder() || date == null
          ? _value.date
          // ignore: cast_nullable_to_non_nullable
          : date as DateTime,
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
    );
  }
}

extension $RuleBacktestResultPerDayInnerCopyWith
    on RuleBacktestResultPerDayInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRuleBacktestResultPerDayInner.copyWith(...)` or `instanceOfRuleBacktestResultPerDayInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RuleBacktestResultPerDayInnerCWProxy get copyWith =>
      _$RuleBacktestResultPerDayInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RuleBacktestResultPerDayInner _$RuleBacktestResultPerDayInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RuleBacktestResultPerDayInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['date', 'count']);
  final val = RuleBacktestResultPerDayInner(
    date: $checkedConvert('date', (v) => DateTime.parse(v as String)),
    count: $checkedConvert('count', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$RuleBacktestResultPerDayInnerToJson(
  RuleBacktestResultPerDayInner instance,
) => <String, dynamic>{
  'date': instance.date.toIso8601String(),
  'count': instance.count,
};
