// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'watchdog_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WatchdogSettingsCWProxy {
  WatchdogSettings heartbeatUrl(String? heartbeatUrl);

  WatchdogSettings failUrl(String? failUrl);

  WatchdogSettings interval(String? interval);

  WatchdogSettings method(WatchdogSettingsMethodEnum? method);

  WatchdogSettings canary(WatchdogSettingsCanary? canary);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchdogSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchdogSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  WatchdogSettings call({
    String? heartbeatUrl,
    String? failUrl,
    String? interval,
    WatchdogSettingsMethodEnum? method,
    WatchdogSettingsCanary? canary,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWatchdogSettings.copyWith(...)` or call `instanceOfWatchdogSettings.copyWith.fieldName(value)` for a single field.
class _$WatchdogSettingsCWProxyImpl implements _$WatchdogSettingsCWProxy {
  const _$WatchdogSettingsCWProxyImpl(this._value);

  final WatchdogSettings _value;

  @override
  WatchdogSettings heartbeatUrl(String? heartbeatUrl) =>
      call(heartbeatUrl: heartbeatUrl);

  @override
  WatchdogSettings failUrl(String? failUrl) => call(failUrl: failUrl);

  @override
  WatchdogSettings interval(String? interval) => call(interval: interval);

  @override
  WatchdogSettings method(WatchdogSettingsMethodEnum? method) =>
      call(method: method);

  @override
  WatchdogSettings canary(WatchdogSettingsCanary? canary) =>
      call(canary: canary);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchdogSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchdogSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WatchdogSettings call({
    Object? heartbeatUrl = const $CopyWithPlaceholder(),
    Object? failUrl = const $CopyWithPlaceholder(),
    Object? interval = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? canary = const $CopyWithPlaceholder(),
  }) {
    return WatchdogSettings(
      heartbeatUrl: heartbeatUrl == const $CopyWithPlaceholder()
          ? _value.heartbeatUrl
          // ignore: cast_nullable_to_non_nullable
          : heartbeatUrl as String?,
      failUrl: failUrl == const $CopyWithPlaceholder()
          ? _value.failUrl
          // ignore: cast_nullable_to_non_nullable
          : failUrl as String?,
      interval: interval == const $CopyWithPlaceholder()
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as String?,
      method: method == const $CopyWithPlaceholder()
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as WatchdogSettingsMethodEnum?,
      canary: canary == const $CopyWithPlaceholder()
          ? _value.canary
          // ignore: cast_nullable_to_non_nullable
          : canary as WatchdogSettingsCanary?,
    );
  }
}

extension $WatchdogSettingsCopyWith on WatchdogSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWatchdogSettings.copyWith(...)` or `instanceOfWatchdogSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WatchdogSettingsCWProxy get copyWith => _$WatchdogSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WatchdogSettings _$WatchdogSettingsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WatchdogSettings', json, ($checkedConvert) {
      final val = WatchdogSettings(
        heartbeatUrl: $checkedConvert('heartbeatUrl', (v) => v as String?),
        failUrl: $checkedConvert('failUrl', (v) => v as String?),
        interval: $checkedConvert('interval', (v) => v as String? ?? '60s'),
        method: $checkedConvert(
          'method',
          (v) => $enumDecodeNullable(_$WatchdogSettingsMethodEnumEnumMap, v),
        ),
        canary: $checkedConvert(
          'canary',
          (v) => v == null
              ? null
              : WatchdogSettingsCanary.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$WatchdogSettingsToJson(WatchdogSettings instance) =>
    <String, dynamic>{
      'heartbeatUrl': ?instance.heartbeatUrl,
      'failUrl': ?instance.failUrl,
      'interval': ?instance.interval,
      'method': ?_$WatchdogSettingsMethodEnumEnumMap[instance.method],
      'canary': ?instance.canary?.toJson(),
    };

const _$WatchdogSettingsMethodEnumEnumMap = {
  WatchdogSettingsMethodEnum.GET: 'GET',
  WatchdogSettingsMethodEnum.POST: 'POST',
};
