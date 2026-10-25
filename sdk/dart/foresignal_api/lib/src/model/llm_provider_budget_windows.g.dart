// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_provider_budget_windows.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmProviderBudgetWindowsCWProxy {
  LlmProviderBudgetWindows hour(LlmSpendWindow hour);

  LlmProviderBudgetWindows day(LlmSpendWindow day);

  LlmProviderBudgetWindows month(LlmSpendWindow month);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmProviderBudgetWindows(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmProviderBudgetWindows(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmProviderBudgetWindows call({
    LlmSpendWindow hour,
    LlmSpendWindow day,
    LlmSpendWindow month,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmProviderBudgetWindows.copyWith(...)` or call `instanceOfLlmProviderBudgetWindows.copyWith.fieldName(value)` for a single field.
class _$LlmProviderBudgetWindowsCWProxyImpl
    implements _$LlmProviderBudgetWindowsCWProxy {
  const _$LlmProviderBudgetWindowsCWProxyImpl(this._value);

  final LlmProviderBudgetWindows _value;

  @override
  LlmProviderBudgetWindows hour(LlmSpendWindow hour) => call(hour: hour);

  @override
  LlmProviderBudgetWindows day(LlmSpendWindow day) => call(day: day);

  @override
  LlmProviderBudgetWindows month(LlmSpendWindow month) => call(month: month);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmProviderBudgetWindows(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmProviderBudgetWindows(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmProviderBudgetWindows call({
    Object? hour = const $CopyWithPlaceholder(),
    Object? day = const $CopyWithPlaceholder(),
    Object? month = const $CopyWithPlaceholder(),
  }) {
    return LlmProviderBudgetWindows(
      hour: hour == const $CopyWithPlaceholder() || hour == null
          ? _value.hour
          // ignore: cast_nullable_to_non_nullable
          : hour as LlmSpendWindow,
      day: day == const $CopyWithPlaceholder() || day == null
          ? _value.day
          // ignore: cast_nullable_to_non_nullable
          : day as LlmSpendWindow,
      month: month == const $CopyWithPlaceholder() || month == null
          ? _value.month
          // ignore: cast_nullable_to_non_nullable
          : month as LlmSpendWindow,
    );
  }
}

extension $LlmProviderBudgetWindowsCopyWith on LlmProviderBudgetWindows {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmProviderBudgetWindows.copyWith(...)` or `instanceOfLlmProviderBudgetWindows.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmProviderBudgetWindowsCWProxy get copyWith =>
      _$LlmProviderBudgetWindowsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmProviderBudgetWindows _$LlmProviderBudgetWindowsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('LlmProviderBudgetWindows', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['hour', 'day', 'month']);
  final val = LlmProviderBudgetWindows(
    hour: $checkedConvert(
      'hour',
      (v) => LlmSpendWindow.fromJson(v as Map<String, dynamic>),
    ),
    day: $checkedConvert(
      'day',
      (v) => LlmSpendWindow.fromJson(v as Map<String, dynamic>),
    ),
    month: $checkedConvert(
      'month',
      (v) => LlmSpendWindow.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$LlmProviderBudgetWindowsToJson(
  LlmProviderBudgetWindows instance,
) => <String, dynamic>{
  'hour': instance.hour.toJson(),
  'day': instance.day.toJson(),
  'month': instance.month.toJson(),
};
