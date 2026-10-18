// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'watchdog_settings_canary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WatchdogSettingsCanaryCWProxy {
  WatchdogSettingsCanary enabled(bool? enabled);

  WatchdogSettingsCanary interval(String? interval);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchdogSettingsCanary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchdogSettingsCanary(...).copyWith(id: 12, name: "My name")
  /// ```
  WatchdogSettingsCanary call({bool? enabled, String? interval});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWatchdogSettingsCanary.copyWith(...)` or call `instanceOfWatchdogSettingsCanary.copyWith.fieldName(value)` for a single field.
class _$WatchdogSettingsCanaryCWProxyImpl
    implements _$WatchdogSettingsCanaryCWProxy {
  const _$WatchdogSettingsCanaryCWProxyImpl(this._value);

  final WatchdogSettingsCanary _value;

  @override
  WatchdogSettingsCanary enabled(bool? enabled) => call(enabled: enabled);

  @override
  WatchdogSettingsCanary interval(String? interval) => call(interval: interval);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchdogSettingsCanary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchdogSettingsCanary(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WatchdogSettingsCanary call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? interval = const $CopyWithPlaceholder(),
  }) {
    return WatchdogSettingsCanary(
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      interval: interval == const $CopyWithPlaceholder()
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as String?,
    );
  }
}

extension $WatchdogSettingsCanaryCopyWith on WatchdogSettingsCanary {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWatchdogSettingsCanary.copyWith(...)` or `instanceOfWatchdogSettingsCanary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WatchdogSettingsCanaryCWProxy get copyWith =>
      _$WatchdogSettingsCanaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WatchdogSettingsCanary _$WatchdogSettingsCanaryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('WatchdogSettingsCanary', json, ($checkedConvert) {
  final val = WatchdogSettingsCanary(
    enabled: $checkedConvert('enabled', (v) => v as bool? ?? false),
    interval: $checkedConvert('interval', (v) => v as String? ?? '1h'),
  );
  return val;
});

Map<String, dynamic> _$WatchdogSettingsCanaryToJson(
  WatchdogSettingsCanary instance,
) => <String, dynamic>{
  'enabled': ?instance.enabled,
  'interval': ?instance.interval,
};
