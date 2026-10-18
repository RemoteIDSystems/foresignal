// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'retention_settings_stats.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RetentionSettingsStatsCWProxy {
  RetentionSettingsStats hourly(String? hourly);

  RetentionSettingsStats daily(String? daily);

  RetentionSettingsStats regionHourly(String? regionHourly);

  RetentionSettingsStats regionDaily(String? regionDaily);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionSettingsStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionSettingsStats(...).copyWith(id: 12, name: "My name")
  /// ```
  RetentionSettingsStats call({
    String? hourly,
    String? daily,
    String? regionHourly,
    String? regionDaily,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRetentionSettingsStats.copyWith(...)` or call `instanceOfRetentionSettingsStats.copyWith.fieldName(value)` for a single field.
class _$RetentionSettingsStatsCWProxyImpl
    implements _$RetentionSettingsStatsCWProxy {
  const _$RetentionSettingsStatsCWProxyImpl(this._value);

  final RetentionSettingsStats _value;

  @override
  RetentionSettingsStats hourly(String? hourly) => call(hourly: hourly);

  @override
  RetentionSettingsStats daily(String? daily) => call(daily: daily);

  @override
  RetentionSettingsStats regionHourly(String? regionHourly) =>
      call(regionHourly: regionHourly);

  @override
  RetentionSettingsStats regionDaily(String? regionDaily) =>
      call(regionDaily: regionDaily);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionSettingsStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionSettingsStats(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RetentionSettingsStats call({
    Object? hourly = const $CopyWithPlaceholder(),
    Object? daily = const $CopyWithPlaceholder(),
    Object? regionHourly = const $CopyWithPlaceholder(),
    Object? regionDaily = const $CopyWithPlaceholder(),
  }) {
    return RetentionSettingsStats(
      hourly: hourly == const $CopyWithPlaceholder()
          ? _value.hourly
          // ignore: cast_nullable_to_non_nullable
          : hourly as String?,
      daily: daily == const $CopyWithPlaceholder()
          ? _value.daily
          // ignore: cast_nullable_to_non_nullable
          : daily as String?,
      regionHourly: regionHourly == const $CopyWithPlaceholder()
          ? _value.regionHourly
          // ignore: cast_nullable_to_non_nullable
          : regionHourly as String?,
      regionDaily: regionDaily == const $CopyWithPlaceholder()
          ? _value.regionDaily
          // ignore: cast_nullable_to_non_nullable
          : regionDaily as String?,
    );
  }
}

extension $RetentionSettingsStatsCopyWith on RetentionSettingsStats {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRetentionSettingsStats.copyWith(...)` or `instanceOfRetentionSettingsStats.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RetentionSettingsStatsCWProxy get copyWith =>
      _$RetentionSettingsStatsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RetentionSettingsStats _$RetentionSettingsStatsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RetentionSettingsStats', json, ($checkedConvert) {
  final val = RetentionSettingsStats(
    hourly: $checkedConvert('hourly', (v) => v as String? ?? '35d'),
    daily: $checkedConvert('daily', (v) => v as String? ?? '1825d'),
    regionHourly: $checkedConvert(
      'regionHourly',
      (v) => v as String? ?? '1095d',
    ),
    regionDaily: $checkedConvert('regionDaily', (v) => v as String? ?? '3650d'),
  );
  return val;
});

Map<String, dynamic> _$RetentionSettingsStatsToJson(
  RetentionSettingsStats instance,
) => <String, dynamic>{
  'hourly': ?instance.hourly,
  'daily': ?instance.daily,
  'regionHourly': ?instance.regionHourly,
  'regionDaily': ?instance.regionDaily,
};
