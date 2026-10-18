// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_spend.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialSpendCWProxy {
  SocialSpend currency(SocialSpendCurrencyEnum currency);

  SocialSpend spentThisMonth(num spentThisMonth);

  SocialSpend monthlyCapUsd(num? monthlyCapUsd);

  SocialSpend projectedThisMonth(num? projectedThisMonth);

  SocialSpend standingCountsEstimateMonthly(num? standingCountsEstimateMonthly);

  SocialSpend pricesAsOf(DateTime? pricesAsOf);

  SocialSpend refusedThisMonth(int? refusedThisMonth);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSpend(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSpend(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialSpend call({
    SocialSpendCurrencyEnum currency,
    num spentThisMonth,
    num? monthlyCapUsd,
    num? projectedThisMonth,
    num? standingCountsEstimateMonthly,
    DateTime? pricesAsOf,
    int? refusedThisMonth,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialSpend.copyWith(...)` or call `instanceOfSocialSpend.copyWith.fieldName(value)` for a single field.
class _$SocialSpendCWProxyImpl implements _$SocialSpendCWProxy {
  const _$SocialSpendCWProxyImpl(this._value);

  final SocialSpend _value;

  @override
  SocialSpend currency(SocialSpendCurrencyEnum currency) =>
      call(currency: currency);

  @override
  SocialSpend spentThisMonth(num spentThisMonth) =>
      call(spentThisMonth: spentThisMonth);

  @override
  SocialSpend monthlyCapUsd(num? monthlyCapUsd) =>
      call(monthlyCapUsd: monthlyCapUsd);

  @override
  SocialSpend projectedThisMonth(num? projectedThisMonth) =>
      call(projectedThisMonth: projectedThisMonth);

  @override
  SocialSpend standingCountsEstimateMonthly(
    num? standingCountsEstimateMonthly,
  ) => call(standingCountsEstimateMonthly: standingCountsEstimateMonthly);

  @override
  SocialSpend pricesAsOf(DateTime? pricesAsOf) => call(pricesAsOf: pricesAsOf);

  @override
  SocialSpend refusedThisMonth(int? refusedThisMonth) =>
      call(refusedThisMonth: refusedThisMonth);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSpend(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSpend(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialSpend call({
    Object? currency = const $CopyWithPlaceholder(),
    Object? spentThisMonth = const $CopyWithPlaceholder(),
    Object? monthlyCapUsd = const $CopyWithPlaceholder(),
    Object? projectedThisMonth = const $CopyWithPlaceholder(),
    Object? standingCountsEstimateMonthly = const $CopyWithPlaceholder(),
    Object? pricesAsOf = const $CopyWithPlaceholder(),
    Object? refusedThisMonth = const $CopyWithPlaceholder(),
  }) {
    return SocialSpend(
      currency: currency == const $CopyWithPlaceholder() || currency == null
          ? _value.currency
          // ignore: cast_nullable_to_non_nullable
          : currency as SocialSpendCurrencyEnum,
      spentThisMonth:
          spentThisMonth == const $CopyWithPlaceholder() ||
              spentThisMonth == null
          ? _value.spentThisMonth
          // ignore: cast_nullable_to_non_nullable
          : spentThisMonth as num,
      monthlyCapUsd: monthlyCapUsd == const $CopyWithPlaceholder()
          ? _value.monthlyCapUsd
          // ignore: cast_nullable_to_non_nullable
          : monthlyCapUsd as num?,
      projectedThisMonth: projectedThisMonth == const $CopyWithPlaceholder()
          ? _value.projectedThisMonth
          // ignore: cast_nullable_to_non_nullable
          : projectedThisMonth as num?,
      standingCountsEstimateMonthly:
          standingCountsEstimateMonthly == const $CopyWithPlaceholder()
          ? _value.standingCountsEstimateMonthly
          // ignore: cast_nullable_to_non_nullable
          : standingCountsEstimateMonthly as num?,
      pricesAsOf: pricesAsOf == const $CopyWithPlaceholder()
          ? _value.pricesAsOf
          // ignore: cast_nullable_to_non_nullable
          : pricesAsOf as DateTime?,
      refusedThisMonth: refusedThisMonth == const $CopyWithPlaceholder()
          ? _value.refusedThisMonth
          // ignore: cast_nullable_to_non_nullable
          : refusedThisMonth as int?,
    );
  }
}

extension $SocialSpendCopyWith on SocialSpend {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialSpend.copyWith(...)` or `instanceOfSocialSpend.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialSpendCWProxy get copyWith => _$SocialSpendCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialSpend _$SocialSpendFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialSpend', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['currency', 'spentThisMonth']);
      final val = SocialSpend(
        currency: $checkedConvert(
          'currency',
          (v) => $enumDecode(_$SocialSpendCurrencyEnumEnumMap, v),
        ),
        spentThisMonth: $checkedConvert('spentThisMonth', (v) => v as num),
        monthlyCapUsd: $checkedConvert('monthlyCapUsd', (v) => v as num?),
        projectedThisMonth: $checkedConvert(
          'projectedThisMonth',
          (v) => v as num?,
        ),
        standingCountsEstimateMonthly: $checkedConvert(
          'standingCountsEstimateMonthly',
          (v) => v as num?,
        ),
        pricesAsOf: $checkedConvert(
          'pricesAsOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        refusedThisMonth: $checkedConvert(
          'refusedThisMonth',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SocialSpendToJson(SocialSpend instance) =>
    <String, dynamic>{
      'currency': _$SocialSpendCurrencyEnumEnumMap[instance.currency]!,
      'spentThisMonth': instance.spentThisMonth,
      'monthlyCapUsd': ?instance.monthlyCapUsd,
      'projectedThisMonth': ?instance.projectedThisMonth,
      'standingCountsEstimateMonthly': ?instance.standingCountsEstimateMonthly,
      'pricesAsOf': ?instance.pricesAsOf?.toIso8601String(),
      'refusedThisMonth': ?instance.refusedThisMonth,
    };

const _$SocialSpendCurrencyEnumEnumMap = {SocialSpendCurrencyEnum.USD: 'USD'};
