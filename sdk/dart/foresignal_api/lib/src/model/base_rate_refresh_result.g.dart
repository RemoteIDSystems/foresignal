// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'base_rate_refresh_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BaseRateRefreshResultCWProxy {
  BaseRateRefreshResult profiles(int profiles);

  BaseRateRefreshResult rates(int rates);

  BaseRateRefreshResult skipped(int? skipped);

  BaseRateRefreshResult removed(int? removed);

  BaseRateRefreshResult families(List<String> families);

  BaseRateRefreshResult regions(int? regions);

  BaseRateRefreshResult computer(DetectorRef? computer);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaseRateRefreshResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaseRateRefreshResult(...).copyWith(id: 12, name: "My name")
  /// ```
  BaseRateRefreshResult call({
    int profiles,
    int rates,
    int? skipped,
    int? removed,
    List<String> families,
    int? regions,
    DetectorRef? computer,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBaseRateRefreshResult.copyWith(...)` or call `instanceOfBaseRateRefreshResult.copyWith.fieldName(value)` for a single field.
class _$BaseRateRefreshResultCWProxyImpl
    implements _$BaseRateRefreshResultCWProxy {
  const _$BaseRateRefreshResultCWProxyImpl(this._value);

  final BaseRateRefreshResult _value;

  @override
  BaseRateRefreshResult profiles(int profiles) => call(profiles: profiles);

  @override
  BaseRateRefreshResult rates(int rates) => call(rates: rates);

  @override
  BaseRateRefreshResult skipped(int? skipped) => call(skipped: skipped);

  @override
  BaseRateRefreshResult removed(int? removed) => call(removed: removed);

  @override
  BaseRateRefreshResult families(List<String> families) =>
      call(families: families);

  @override
  BaseRateRefreshResult regions(int? regions) => call(regions: regions);

  @override
  BaseRateRefreshResult computer(DetectorRef? computer) =>
      call(computer: computer);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaseRateRefreshResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaseRateRefreshResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BaseRateRefreshResult call({
    Object? profiles = const $CopyWithPlaceholder(),
    Object? rates = const $CopyWithPlaceholder(),
    Object? skipped = const $CopyWithPlaceholder(),
    Object? removed = const $CopyWithPlaceholder(),
    Object? families = const $CopyWithPlaceholder(),
    Object? regions = const $CopyWithPlaceholder(),
    Object? computer = const $CopyWithPlaceholder(),
  }) {
    return BaseRateRefreshResult(
      profiles: profiles == const $CopyWithPlaceholder() || profiles == null
          ? _value.profiles
          // ignore: cast_nullable_to_non_nullable
          : profiles as int,
      rates: rates == const $CopyWithPlaceholder() || rates == null
          ? _value.rates
          // ignore: cast_nullable_to_non_nullable
          : rates as int,
      skipped: skipped == const $CopyWithPlaceholder()
          ? _value.skipped
          // ignore: cast_nullable_to_non_nullable
          : skipped as int?,
      removed: removed == const $CopyWithPlaceholder()
          ? _value.removed
          // ignore: cast_nullable_to_non_nullable
          : removed as int?,
      families: families == const $CopyWithPlaceholder() || families == null
          ? _value.families
          // ignore: cast_nullable_to_non_nullable
          : families as List<String>,
      regions: regions == const $CopyWithPlaceholder()
          ? _value.regions
          // ignore: cast_nullable_to_non_nullable
          : regions as int?,
      computer: computer == const $CopyWithPlaceholder()
          ? _value.computer
          // ignore: cast_nullable_to_non_nullable
          : computer as DetectorRef?,
    );
  }
}

extension $BaseRateRefreshResultCopyWith on BaseRateRefreshResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBaseRateRefreshResult.copyWith(...)` or `instanceOfBaseRateRefreshResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BaseRateRefreshResultCWProxy get copyWith =>
      _$BaseRateRefreshResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BaseRateRefreshResult _$BaseRateRefreshResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('BaseRateRefreshResult', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['profiles', 'rates', 'families']);
  final val = BaseRateRefreshResult(
    profiles: $checkedConvert('profiles', (v) => (v as num).toInt()),
    rates: $checkedConvert('rates', (v) => (v as num).toInt()),
    skipped: $checkedConvert('skipped', (v) => (v as num?)?.toInt()),
    removed: $checkedConvert('removed', (v) => (v as num?)?.toInt()),
    families: $checkedConvert(
      'families',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    regions: $checkedConvert('regions', (v) => (v as num?)?.toInt()),
    computer: $checkedConvert(
      'computer',
      (v) => v == null ? null : DetectorRef.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$BaseRateRefreshResultToJson(
  BaseRateRefreshResult instance,
) => <String, dynamic>{
  'profiles': instance.profiles,
  'rates': instance.rates,
  'skipped': ?instance.skipped,
  'removed': ?instance.removed,
  'families': instance.families,
  'regions': ?instance.regions,
  'computer': ?instance.computer?.toJson(),
};
