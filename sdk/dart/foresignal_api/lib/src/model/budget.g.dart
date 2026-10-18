// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'budget.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BudgetCWProxy {
  Budget perMinute(int perMinute);

  Budget perDay(int perDay);

  Budget monthlyCapUsd(num? monthlyCapUsd);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Budget(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Budget(...).copyWith(id: 12, name: "My name")
  /// ```
  Budget call({int perMinute, int perDay, num? monthlyCapUsd});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBudget.copyWith(...)` or call `instanceOfBudget.copyWith.fieldName(value)` for a single field.
class _$BudgetCWProxyImpl implements _$BudgetCWProxy {
  const _$BudgetCWProxyImpl(this._value);

  final Budget _value;

  @override
  Budget perMinute(int perMinute) => call(perMinute: perMinute);

  @override
  Budget perDay(int perDay) => call(perDay: perDay);

  @override
  Budget monthlyCapUsd(num? monthlyCapUsd) =>
      call(monthlyCapUsd: monthlyCapUsd);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Budget(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Budget(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Budget call({
    Object? perMinute = const $CopyWithPlaceholder(),
    Object? perDay = const $CopyWithPlaceholder(),
    Object? monthlyCapUsd = const $CopyWithPlaceholder(),
  }) {
    return Budget(
      perMinute: perMinute == const $CopyWithPlaceholder() || perMinute == null
          ? _value.perMinute
          // ignore: cast_nullable_to_non_nullable
          : perMinute as int,
      perDay: perDay == const $CopyWithPlaceholder() || perDay == null
          ? _value.perDay
          // ignore: cast_nullable_to_non_nullable
          : perDay as int,
      monthlyCapUsd: monthlyCapUsd == const $CopyWithPlaceholder()
          ? _value.monthlyCapUsd
          // ignore: cast_nullable_to_non_nullable
          : monthlyCapUsd as num?,
    );
  }
}

extension $BudgetCopyWith on Budget {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBudget.copyWith(...)` or `instanceOfBudget.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BudgetCWProxy get copyWith => _$BudgetCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Budget _$BudgetFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Budget', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['perMinute', 'perDay']);
      final val = Budget(
        perMinute: $checkedConvert('perMinute', (v) => (v as num).toInt()),
        perDay: $checkedConvert('perDay', (v) => (v as num).toInt()),
        monthlyCapUsd: $checkedConvert('monthlyCapUsd', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$BudgetToJson(Budget instance) => <String, dynamic>{
  'perMinute': instance.perMinute,
  'perDay': instance.perDay,
  'monthlyCapUsd': ?instance.monthlyCapUsd,
};
