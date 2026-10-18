// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rule_backtest_result_actual.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RuleBacktestResultActualCWProxy {
  RuleBacktestResultActual alerts(int? alerts);

  RuleBacktestResultActual comparable(bool? comparable);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResultActual(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResultActual(...).copyWith(id: 12, name: "My name")
  /// ```
  RuleBacktestResultActual call({int? alerts, bool? comparable});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRuleBacktestResultActual.copyWith(...)` or call `instanceOfRuleBacktestResultActual.copyWith.fieldName(value)` for a single field.
class _$RuleBacktestResultActualCWProxyImpl
    implements _$RuleBacktestResultActualCWProxy {
  const _$RuleBacktestResultActualCWProxyImpl(this._value);

  final RuleBacktestResultActual _value;

  @override
  RuleBacktestResultActual alerts(int? alerts) => call(alerts: alerts);

  @override
  RuleBacktestResultActual comparable(bool? comparable) =>
      call(comparable: comparable);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResultActual(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResultActual(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RuleBacktestResultActual call({
    Object? alerts = const $CopyWithPlaceholder(),
    Object? comparable = const $CopyWithPlaceholder(),
  }) {
    return RuleBacktestResultActual(
      alerts: alerts == const $CopyWithPlaceholder()
          ? _value.alerts
          // ignore: cast_nullable_to_non_nullable
          : alerts as int?,
      comparable: comparable == const $CopyWithPlaceholder()
          ? _value.comparable
          // ignore: cast_nullable_to_non_nullable
          : comparable as bool?,
    );
  }
}

extension $RuleBacktestResultActualCopyWith on RuleBacktestResultActual {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRuleBacktestResultActual.copyWith(...)` or `instanceOfRuleBacktestResultActual.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RuleBacktestResultActualCWProxy get copyWith =>
      _$RuleBacktestResultActualCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RuleBacktestResultActual _$RuleBacktestResultActualFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RuleBacktestResultActual', json, ($checkedConvert) {
  final val = RuleBacktestResultActual(
    alerts: $checkedConvert('alerts', (v) => (v as num?)?.toInt()),
    comparable: $checkedConvert('comparable', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$RuleBacktestResultActualToJson(
  RuleBacktestResultActual instance,
) => <String, dynamic>{
  'alerts': ?instance.alerts,
  'comparable': ?instance.comparable,
};
