// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_budget_provider.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmBudgetProviderCWProxy {
  LlmBudgetProvider providerId(String providerId);

  LlmBudgetProvider name(String name);

  LlmBudgetProvider shared(bool shared);

  LlmBudgetProvider capUsd(num? capUsd);

  LlmBudgetProvider spentUsd(num spentUsd);

  LlmBudgetProvider remainingUsd(num? remainingUsd);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmBudgetProvider(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmBudgetProvider(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmBudgetProvider call({
    String providerId,
    String name,
    bool shared,
    num? capUsd,
    num spentUsd,
    num? remainingUsd,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmBudgetProvider.copyWith(...)` or call `instanceOfLlmBudgetProvider.copyWith.fieldName(value)` for a single field.
class _$LlmBudgetProviderCWProxyImpl implements _$LlmBudgetProviderCWProxy {
  const _$LlmBudgetProviderCWProxyImpl(this._value);

  final LlmBudgetProvider _value;

  @override
  LlmBudgetProvider providerId(String providerId) =>
      call(providerId: providerId);

  @override
  LlmBudgetProvider name(String name) => call(name: name);

  @override
  LlmBudgetProvider shared(bool shared) => call(shared: shared);

  @override
  LlmBudgetProvider capUsd(num? capUsd) => call(capUsd: capUsd);

  @override
  LlmBudgetProvider spentUsd(num spentUsd) => call(spentUsd: spentUsd);

  @override
  LlmBudgetProvider remainingUsd(num? remainingUsd) =>
      call(remainingUsd: remainingUsd);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmBudgetProvider(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmBudgetProvider(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmBudgetProvider call({
    Object? providerId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? shared = const $CopyWithPlaceholder(),
    Object? capUsd = const $CopyWithPlaceholder(),
    Object? spentUsd = const $CopyWithPlaceholder(),
    Object? remainingUsd = const $CopyWithPlaceholder(),
  }) {
    return LlmBudgetProvider(
      providerId:
          providerId == const $CopyWithPlaceholder() || providerId == null
          ? _value.providerId
          // ignore: cast_nullable_to_non_nullable
          : providerId as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      shared: shared == const $CopyWithPlaceholder() || shared == null
          ? _value.shared
          // ignore: cast_nullable_to_non_nullable
          : shared as bool,
      capUsd: capUsd == const $CopyWithPlaceholder()
          ? _value.capUsd
          // ignore: cast_nullable_to_non_nullable
          : capUsd as num?,
      spentUsd: spentUsd == const $CopyWithPlaceholder() || spentUsd == null
          ? _value.spentUsd
          // ignore: cast_nullable_to_non_nullable
          : spentUsd as num,
      remainingUsd: remainingUsd == const $CopyWithPlaceholder()
          ? _value.remainingUsd
          // ignore: cast_nullable_to_non_nullable
          : remainingUsd as num?,
    );
  }
}

extension $LlmBudgetProviderCopyWith on LlmBudgetProvider {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmBudgetProvider.copyWith(...)` or `instanceOfLlmBudgetProvider.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmBudgetProviderCWProxy get copyWith =>
      _$LlmBudgetProviderCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmBudgetProvider _$LlmBudgetProviderFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LlmBudgetProvider', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['providerId', 'name', 'shared', 'spentUsd'],
      );
      final val = LlmBudgetProvider(
        providerId: $checkedConvert('providerId', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String),
        shared: $checkedConvert('shared', (v) => v as bool),
        capUsd: $checkedConvert('capUsd', (v) => v as num?),
        spentUsd: $checkedConvert('spentUsd', (v) => v as num),
        remainingUsd: $checkedConvert('remainingUsd', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$LlmBudgetProviderToJson(LlmBudgetProvider instance) =>
    <String, dynamic>{
      'providerId': instance.providerId,
      'name': instance.name,
      'shared': instance.shared,
      'capUsd': ?instance.capUsd,
      'spentUsd': instance.spentUsd,
      'remainingUsd': ?instance.remainingUsd,
    };
