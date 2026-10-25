// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_provider_balance_amounts_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmProviderBalanceAmountsInnerCWProxy {
  LlmProviderBalanceAmountsInner amount(num amount);

  LlmProviderBalanceAmountsInner currency(String currency);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmProviderBalanceAmountsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmProviderBalanceAmountsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmProviderBalanceAmountsInner call({num amount, String currency});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmProviderBalanceAmountsInner.copyWith(...)` or call `instanceOfLlmProviderBalanceAmountsInner.copyWith.fieldName(value)` for a single field.
class _$LlmProviderBalanceAmountsInnerCWProxyImpl
    implements _$LlmProviderBalanceAmountsInnerCWProxy {
  const _$LlmProviderBalanceAmountsInnerCWProxyImpl(this._value);

  final LlmProviderBalanceAmountsInner _value;

  @override
  LlmProviderBalanceAmountsInner amount(num amount) => call(amount: amount);

  @override
  LlmProviderBalanceAmountsInner currency(String currency) =>
      call(currency: currency);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmProviderBalanceAmountsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmProviderBalanceAmountsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmProviderBalanceAmountsInner call({
    Object? amount = const $CopyWithPlaceholder(),
    Object? currency = const $CopyWithPlaceholder(),
  }) {
    return LlmProviderBalanceAmountsInner(
      amount: amount == const $CopyWithPlaceholder() || amount == null
          ? _value.amount
          // ignore: cast_nullable_to_non_nullable
          : amount as num,
      currency: currency == const $CopyWithPlaceholder() || currency == null
          ? _value.currency
          // ignore: cast_nullable_to_non_nullable
          : currency as String,
    );
  }
}

extension $LlmProviderBalanceAmountsInnerCopyWith
    on LlmProviderBalanceAmountsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmProviderBalanceAmountsInner.copyWith(...)` or `instanceOfLlmProviderBalanceAmountsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmProviderBalanceAmountsInnerCWProxy get copyWith =>
      _$LlmProviderBalanceAmountsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmProviderBalanceAmountsInner _$LlmProviderBalanceAmountsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('LlmProviderBalanceAmountsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['amount', 'currency']);
  final val = LlmProviderBalanceAmountsInner(
    amount: $checkedConvert('amount', (v) => v as num),
    currency: $checkedConvert('currency', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$LlmProviderBalanceAmountsInnerToJson(
  LlmProviderBalanceAmountsInner instance,
) => <String, dynamic>{
  'amount': instance.amount,
  'currency': instance.currency,
};
