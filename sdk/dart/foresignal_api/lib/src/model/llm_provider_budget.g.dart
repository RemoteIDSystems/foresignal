// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_provider_budget.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmProviderBudgetCWProxy {
  LlmProviderBudget providerId(String providerId);

  LlmProviderBudget currency(LlmProviderBudgetCurrencyEnum currency);

  LlmProviderBudget priced(bool priced);

  LlmProviderBudget pricing(LlmPricing pricing);

  LlmProviderBudget windows(LlmProviderBudgetWindows windows);

  LlmProviderBudget rateLimits(LlmProviderBudgetRateLimits rateLimits);

  LlmProviderBudget providerBalance(LlmProviderBalance? providerBalance);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmProviderBudget(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmProviderBudget(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmProviderBudget call({
    String providerId,
    LlmProviderBudgetCurrencyEnum currency,
    bool priced,
    LlmPricing pricing,
    LlmProviderBudgetWindows windows,
    LlmProviderBudgetRateLimits rateLimits,
    LlmProviderBalance? providerBalance,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmProviderBudget.copyWith(...)` or call `instanceOfLlmProviderBudget.copyWith.fieldName(value)` for a single field.
class _$LlmProviderBudgetCWProxyImpl implements _$LlmProviderBudgetCWProxy {
  const _$LlmProviderBudgetCWProxyImpl(this._value);

  final LlmProviderBudget _value;

  @override
  LlmProviderBudget providerId(String providerId) =>
      call(providerId: providerId);

  @override
  LlmProviderBudget currency(LlmProviderBudgetCurrencyEnum currency) =>
      call(currency: currency);

  @override
  LlmProviderBudget priced(bool priced) => call(priced: priced);

  @override
  LlmProviderBudget pricing(LlmPricing pricing) => call(pricing: pricing);

  @override
  LlmProviderBudget windows(LlmProviderBudgetWindows windows) =>
      call(windows: windows);

  @override
  LlmProviderBudget rateLimits(LlmProviderBudgetRateLimits rateLimits) =>
      call(rateLimits: rateLimits);

  @override
  LlmProviderBudget providerBalance(LlmProviderBalance? providerBalance) =>
      call(providerBalance: providerBalance);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmProviderBudget(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmProviderBudget(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmProviderBudget call({
    Object? providerId = const $CopyWithPlaceholder(),
    Object? currency = const $CopyWithPlaceholder(),
    Object? priced = const $CopyWithPlaceholder(),
    Object? pricing = const $CopyWithPlaceholder(),
    Object? windows = const $CopyWithPlaceholder(),
    Object? rateLimits = const $CopyWithPlaceholder(),
    Object? providerBalance = const $CopyWithPlaceholder(),
  }) {
    return LlmProviderBudget(
      providerId:
          providerId == const $CopyWithPlaceholder() || providerId == null
          ? _value.providerId
          // ignore: cast_nullable_to_non_nullable
          : providerId as String,
      currency: currency == const $CopyWithPlaceholder() || currency == null
          ? _value.currency
          // ignore: cast_nullable_to_non_nullable
          : currency as LlmProviderBudgetCurrencyEnum,
      priced: priced == const $CopyWithPlaceholder() || priced == null
          ? _value.priced
          // ignore: cast_nullable_to_non_nullable
          : priced as bool,
      pricing: pricing == const $CopyWithPlaceholder() || pricing == null
          ? _value.pricing
          // ignore: cast_nullable_to_non_nullable
          : pricing as LlmPricing,
      windows: windows == const $CopyWithPlaceholder() || windows == null
          ? _value.windows
          // ignore: cast_nullable_to_non_nullable
          : windows as LlmProviderBudgetWindows,
      rateLimits:
          rateLimits == const $CopyWithPlaceholder() || rateLimits == null
          ? _value.rateLimits
          // ignore: cast_nullable_to_non_nullable
          : rateLimits as LlmProviderBudgetRateLimits,
      providerBalance: providerBalance == const $CopyWithPlaceholder()
          ? _value.providerBalance
          // ignore: cast_nullable_to_non_nullable
          : providerBalance as LlmProviderBalance?,
    );
  }
}

extension $LlmProviderBudgetCopyWith on LlmProviderBudget {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmProviderBudget.copyWith(...)` or `instanceOfLlmProviderBudget.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmProviderBudgetCWProxy get copyWith =>
      _$LlmProviderBudgetCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmProviderBudget _$LlmProviderBudgetFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LlmProviderBudget', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'providerId',
          'currency',
          'priced',
          'pricing',
          'windows',
          'rateLimits',
          'providerBalance',
        ],
      );
      final val = LlmProviderBudget(
        providerId: $checkedConvert('providerId', (v) => v as String),
        currency: $checkedConvert(
          'currency',
          (v) => $enumDecode(_$LlmProviderBudgetCurrencyEnumEnumMap, v),
        ),
        priced: $checkedConvert('priced', (v) => v as bool),
        pricing: $checkedConvert(
          'pricing',
          (v) => LlmPricing.fromJson(v as Map<String, dynamic>),
        ),
        windows: $checkedConvert(
          'windows',
          (v) => LlmProviderBudgetWindows.fromJson(v as Map<String, dynamic>),
        ),
        rateLimits: $checkedConvert(
          'rateLimits',
          (v) =>
              LlmProviderBudgetRateLimits.fromJson(v as Map<String, dynamic>),
        ),
        providerBalance: $checkedConvert(
          'providerBalance',
          (v) => v == null
              ? null
              : LlmProviderBalance.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$LlmProviderBudgetToJson(LlmProviderBudget instance) =>
    <String, dynamic>{
      'providerId': instance.providerId,
      'currency': _$LlmProviderBudgetCurrencyEnumEnumMap[instance.currency]!,
      'priced': instance.priced,
      'pricing': instance.pricing.toJson(),
      'windows': instance.windows.toJson(),
      'rateLimits': instance.rateLimits.toJson(),
      'providerBalance': instance.providerBalance?.toJson(),
    };

const _$LlmProviderBudgetCurrencyEnumEnumMap = {
  LlmProviderBudgetCurrencyEnum.USD: 'USD',
};
