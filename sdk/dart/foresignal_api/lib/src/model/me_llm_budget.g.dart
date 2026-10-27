// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'me_llm_budget.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MeLlmBudgetCWProxy {
  MeLlmBudget providers(List<LlmBudgetProvider> providers);

  MeLlmBudget totalSpentUsd(num totalSpentUsd);

  MeLlmBudget asOf(DateTime? asOf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MeLlmBudget(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MeLlmBudget(...).copyWith(id: 12, name: "My name")
  /// ```
  MeLlmBudget call({
    List<LlmBudgetProvider> providers,
    num totalSpentUsd,
    DateTime? asOf,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMeLlmBudget.copyWith(...)` or call `instanceOfMeLlmBudget.copyWith.fieldName(value)` for a single field.
class _$MeLlmBudgetCWProxyImpl implements _$MeLlmBudgetCWProxy {
  const _$MeLlmBudgetCWProxyImpl(this._value);

  final MeLlmBudget _value;

  @override
  MeLlmBudget providers(List<LlmBudgetProvider> providers) =>
      call(providers: providers);

  @override
  MeLlmBudget totalSpentUsd(num totalSpentUsd) =>
      call(totalSpentUsd: totalSpentUsd);

  @override
  MeLlmBudget asOf(DateTime? asOf) => call(asOf: asOf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MeLlmBudget(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MeLlmBudget(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MeLlmBudget call({
    Object? providers = const $CopyWithPlaceholder(),
    Object? totalSpentUsd = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
  }) {
    return MeLlmBudget(
      providers: providers == const $CopyWithPlaceholder() || providers == null
          ? _value.providers
          // ignore: cast_nullable_to_non_nullable
          : providers as List<LlmBudgetProvider>,
      totalSpentUsd:
          totalSpentUsd == const $CopyWithPlaceholder() || totalSpentUsd == null
          ? _value.totalSpentUsd
          // ignore: cast_nullable_to_non_nullable
          : totalSpentUsd as num,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
    );
  }
}

extension $MeLlmBudgetCopyWith on MeLlmBudget {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMeLlmBudget.copyWith(...)` or `instanceOfMeLlmBudget.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MeLlmBudgetCWProxy get copyWith => _$MeLlmBudgetCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MeLlmBudget _$MeLlmBudgetFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MeLlmBudget', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['providers', 'totalSpentUsd']);
      final val = MeLlmBudget(
        providers: $checkedConvert(
          'providers',
          (v) => (v as List<dynamic>)
              .map((e) => LlmBudgetProvider.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        totalSpentUsd: $checkedConvert('totalSpentUsd', (v) => v as num),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$MeLlmBudgetToJson(MeLlmBudget instance) =>
    <String, dynamic>{
      'providers': instance.providers.map((e) => e.toJson()).toList(),
      'totalSpentUsd': instance.totalSpentUsd,
      'asOf': ?instance.asOf?.toIso8601String(),
    };
