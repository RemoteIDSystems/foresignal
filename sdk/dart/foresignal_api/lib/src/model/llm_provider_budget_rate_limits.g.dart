// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_provider_budget_rate_limits.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmProviderBudgetRateLimitsCWProxy {
  LlmProviderBudgetRateLimits requestsPerMinute(int requestsPerMinute);

  LlmProviderBudgetRateLimits tokensPerDay(int tokensPerDay);

  LlmProviderBudgetRateLimits tokensToday(int tokensToday);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmProviderBudgetRateLimits(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmProviderBudgetRateLimits(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmProviderBudgetRateLimits call({
    int requestsPerMinute,
    int tokensPerDay,
    int tokensToday,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmProviderBudgetRateLimits.copyWith(...)` or call `instanceOfLlmProviderBudgetRateLimits.copyWith.fieldName(value)` for a single field.
class _$LlmProviderBudgetRateLimitsCWProxyImpl
    implements _$LlmProviderBudgetRateLimitsCWProxy {
  const _$LlmProviderBudgetRateLimitsCWProxyImpl(this._value);

  final LlmProviderBudgetRateLimits _value;

  @override
  LlmProviderBudgetRateLimits requestsPerMinute(int requestsPerMinute) =>
      call(requestsPerMinute: requestsPerMinute);

  @override
  LlmProviderBudgetRateLimits tokensPerDay(int tokensPerDay) =>
      call(tokensPerDay: tokensPerDay);

  @override
  LlmProviderBudgetRateLimits tokensToday(int tokensToday) =>
      call(tokensToday: tokensToday);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmProviderBudgetRateLimits(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmProviderBudgetRateLimits(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmProviderBudgetRateLimits call({
    Object? requestsPerMinute = const $CopyWithPlaceholder(),
    Object? tokensPerDay = const $CopyWithPlaceholder(),
    Object? tokensToday = const $CopyWithPlaceholder(),
  }) {
    return LlmProviderBudgetRateLimits(
      requestsPerMinute:
          requestsPerMinute == const $CopyWithPlaceholder() ||
              requestsPerMinute == null
          ? _value.requestsPerMinute
          // ignore: cast_nullable_to_non_nullable
          : requestsPerMinute as int,
      tokensPerDay:
          tokensPerDay == const $CopyWithPlaceholder() || tokensPerDay == null
          ? _value.tokensPerDay
          // ignore: cast_nullable_to_non_nullable
          : tokensPerDay as int,
      tokensToday:
          tokensToday == const $CopyWithPlaceholder() || tokensToday == null
          ? _value.tokensToday
          // ignore: cast_nullable_to_non_nullable
          : tokensToday as int,
    );
  }
}

extension $LlmProviderBudgetRateLimitsCopyWith on LlmProviderBudgetRateLimits {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmProviderBudgetRateLimits.copyWith(...)` or `instanceOfLlmProviderBudgetRateLimits.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmProviderBudgetRateLimitsCWProxy get copyWith =>
      _$LlmProviderBudgetRateLimitsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmProviderBudgetRateLimits _$LlmProviderBudgetRateLimitsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('LlmProviderBudgetRateLimits', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['requestsPerMinute', 'tokensPerDay', 'tokensToday'],
  );
  final val = LlmProviderBudgetRateLimits(
    requestsPerMinute: $checkedConvert(
      'requestsPerMinute',
      (v) => (v as num).toInt(),
    ),
    tokensPerDay: $checkedConvert('tokensPerDay', (v) => (v as num).toInt()),
    tokensToday: $checkedConvert('tokensToday', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$LlmProviderBudgetRateLimitsToJson(
  LlmProviderBudgetRateLimits instance,
) => <String, dynamic>{
  'requestsPerMinute': instance.requestsPerMinute,
  'tokensPerDay': instance.tokensPerDay,
  'tokensToday': instance.tokensToday,
};
