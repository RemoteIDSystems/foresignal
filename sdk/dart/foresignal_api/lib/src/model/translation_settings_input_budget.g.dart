// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'translation_settings_input_budget.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TranslationSettingsInputBudgetCWProxy {
  TranslationSettingsInputBudget window(
    TranslationSettingsInputBudgetWindowEnum? window,
  );

  TranslationSettingsInputBudget capUsd(num? capUsd);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TranslationSettingsInputBudget(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TranslationSettingsInputBudget(...).copyWith(id: 12, name: "My name")
  /// ```
  TranslationSettingsInputBudget call({
    TranslationSettingsInputBudgetWindowEnum? window,
    num? capUsd,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTranslationSettingsInputBudget.copyWith(...)` or call `instanceOfTranslationSettingsInputBudget.copyWith.fieldName(value)` for a single field.
class _$TranslationSettingsInputBudgetCWProxyImpl
    implements _$TranslationSettingsInputBudgetCWProxy {
  const _$TranslationSettingsInputBudgetCWProxyImpl(this._value);

  final TranslationSettingsInputBudget _value;

  @override
  TranslationSettingsInputBudget window(
    TranslationSettingsInputBudgetWindowEnum? window,
  ) => call(window: window);

  @override
  TranslationSettingsInputBudget capUsd(num? capUsd) => call(capUsd: capUsd);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TranslationSettingsInputBudget(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TranslationSettingsInputBudget(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TranslationSettingsInputBudget call({
    Object? window = const $CopyWithPlaceholder(),
    Object? capUsd = const $CopyWithPlaceholder(),
  }) {
    return TranslationSettingsInputBudget(
      window: window == const $CopyWithPlaceholder()
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as TranslationSettingsInputBudgetWindowEnum?,
      capUsd: capUsd == const $CopyWithPlaceholder()
          ? _value.capUsd
          // ignore: cast_nullable_to_non_nullable
          : capUsd as num?,
    );
  }
}

extension $TranslationSettingsInputBudgetCopyWith
    on TranslationSettingsInputBudget {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTranslationSettingsInputBudget.copyWith(...)` or `instanceOfTranslationSettingsInputBudget.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TranslationSettingsInputBudgetCWProxy get copyWith =>
      _$TranslationSettingsInputBudgetCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TranslationSettingsInputBudget _$TranslationSettingsInputBudgetFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TranslationSettingsInputBudget', json, ($checkedConvert) {
  final val = TranslationSettingsInputBudget(
    window: $checkedConvert(
      'window',
      (v) => $enumDecodeNullable(
        _$TranslationSettingsInputBudgetWindowEnumEnumMap,
        v,
      ),
    ),
    capUsd: $checkedConvert('capUsd', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$TranslationSettingsInputBudgetToJson(
  TranslationSettingsInputBudget instance,
) => <String, dynamic>{
  'window': ?_$TranslationSettingsInputBudgetWindowEnumEnumMap[instance.window],
  'capUsd': ?instance.capUsd,
};

const _$TranslationSettingsInputBudgetWindowEnumEnumMap = {
  TranslationSettingsInputBudgetWindowEnum.hour: 'hour',
  TranslationSettingsInputBudgetWindowEnum.day: 'day',
  TranslationSettingsInputBudgetWindowEnum.month: 'month',
};
