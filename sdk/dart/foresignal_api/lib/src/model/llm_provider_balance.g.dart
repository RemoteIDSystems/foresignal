// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_provider_balance.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmProviderBalanceCWProxy {
  LlmProviderBalance provider(String provider);

  LlmProviderBalance kind(String kind);

  LlmProviderBalance amounts(List<LlmProviderBalanceAmountsInner> amounts);

  LlmProviderBalance fetchedAt(DateTime fetchedAt);

  LlmProviderBalance error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmProviderBalance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmProviderBalance(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmProviderBalance call({
    String provider,
    String kind,
    List<LlmProviderBalanceAmountsInner> amounts,
    DateTime fetchedAt,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmProviderBalance.copyWith(...)` or call `instanceOfLlmProviderBalance.copyWith.fieldName(value)` for a single field.
class _$LlmProviderBalanceCWProxyImpl implements _$LlmProviderBalanceCWProxy {
  const _$LlmProviderBalanceCWProxyImpl(this._value);

  final LlmProviderBalance _value;

  @override
  LlmProviderBalance provider(String provider) => call(provider: provider);

  @override
  LlmProviderBalance kind(String kind) => call(kind: kind);

  @override
  LlmProviderBalance amounts(List<LlmProviderBalanceAmountsInner> amounts) =>
      call(amounts: amounts);

  @override
  LlmProviderBalance fetchedAt(DateTime fetchedAt) =>
      call(fetchedAt: fetchedAt);

  @override
  LlmProviderBalance error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmProviderBalance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmProviderBalance(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmProviderBalance call({
    Object? provider = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? amounts = const $CopyWithPlaceholder(),
    Object? fetchedAt = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return LlmProviderBalance(
      provider: provider == const $CopyWithPlaceholder() || provider == null
          ? _value.provider
          // ignore: cast_nullable_to_non_nullable
          : provider as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String,
      amounts: amounts == const $CopyWithPlaceholder() || amounts == null
          ? _value.amounts
          // ignore: cast_nullable_to_non_nullable
          : amounts as List<LlmProviderBalanceAmountsInner>,
      fetchedAt: fetchedAt == const $CopyWithPlaceholder() || fetchedAt == null
          ? _value.fetchedAt
          // ignore: cast_nullable_to_non_nullable
          : fetchedAt as DateTime,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $LlmProviderBalanceCopyWith on LlmProviderBalance {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmProviderBalance.copyWith(...)` or `instanceOfLlmProviderBalance.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmProviderBalanceCWProxy get copyWith =>
      _$LlmProviderBalanceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmProviderBalance _$LlmProviderBalanceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('LlmProviderBalance', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['provider', 'kind', 'amounts', 'fetchedAt'],
  );
  final val = LlmProviderBalance(
    provider: $checkedConvert('provider', (v) => v as String),
    kind: $checkedConvert('kind', (v) => v as String),
    amounts: $checkedConvert(
      'amounts',
      (v) => (v as List<dynamic>)
          .map(
            (e) => LlmProviderBalanceAmountsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    fetchedAt: $checkedConvert('fetchedAt', (v) => DateTime.parse(v as String)),
    error: $checkedConvert('error', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$LlmProviderBalanceToJson(LlmProviderBalance instance) =>
    <String, dynamic>{
      'provider': instance.provider,
      'kind': instance.kind,
      'amounts': instance.amounts.map((e) => e.toJson()).toList(),
      'fetchedAt': instance.fetchedAt.toIso8601String(),
      'error': ?instance.error,
    };
