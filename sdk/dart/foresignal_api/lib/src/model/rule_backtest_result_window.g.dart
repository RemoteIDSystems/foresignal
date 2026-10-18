// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rule_backtest_result_window.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RuleBacktestResultWindowCWProxy {
  RuleBacktestResultWindow steps(int? steps);

  RuleBacktestResultWindow unknownSteps(int? unknownSteps);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResultWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResultWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  RuleBacktestResultWindow call({int? steps, int? unknownSteps});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRuleBacktestResultWindow.copyWith(...)` or call `instanceOfRuleBacktestResultWindow.copyWith.fieldName(value)` for a single field.
class _$RuleBacktestResultWindowCWProxyImpl
    implements _$RuleBacktestResultWindowCWProxy {
  const _$RuleBacktestResultWindowCWProxyImpl(this._value);

  final RuleBacktestResultWindow _value;

  @override
  RuleBacktestResultWindow steps(int? steps) => call(steps: steps);

  @override
  RuleBacktestResultWindow unknownSteps(int? unknownSteps) =>
      call(unknownSteps: unknownSteps);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResultWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResultWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RuleBacktestResultWindow call({
    Object? steps = const $CopyWithPlaceholder(),
    Object? unknownSteps = const $CopyWithPlaceholder(),
  }) {
    return RuleBacktestResultWindow(
      steps: steps == const $CopyWithPlaceholder()
          ? _value.steps
          // ignore: cast_nullable_to_non_nullable
          : steps as int?,
      unknownSteps: unknownSteps == const $CopyWithPlaceholder()
          ? _value.unknownSteps
          // ignore: cast_nullable_to_non_nullable
          : unknownSteps as int?,
    );
  }
}

extension $RuleBacktestResultWindowCopyWith on RuleBacktestResultWindow {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRuleBacktestResultWindow.copyWith(...)` or `instanceOfRuleBacktestResultWindow.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RuleBacktestResultWindowCWProxy get copyWith =>
      _$RuleBacktestResultWindowCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RuleBacktestResultWindow _$RuleBacktestResultWindowFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RuleBacktestResultWindow', json, ($checkedConvert) {
  final val = RuleBacktestResultWindow(
    steps: $checkedConvert('steps', (v) => (v as num?)?.toInt()),
    unknownSteps: $checkedConvert('unknownSteps', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$RuleBacktestResultWindowToJson(
  RuleBacktestResultWindow instance,
) => <String, dynamic>{
  'steps': ?instance.steps,
  'unknownSteps': ?instance.unknownSteps,
};
