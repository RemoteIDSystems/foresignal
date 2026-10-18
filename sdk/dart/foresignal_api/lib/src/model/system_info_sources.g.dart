// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_sources.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoSourcesCWProxy {
  SystemInfoSources total(int? total);

  SystemInfoSources enabled(int? enabled);

  SystemInfoSources healthy(int? healthy);

  SystemInfoSources degraded(int? degraded);

  SystemInfoSources failing(int? failing);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoSources(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoSources(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoSources call({
    int? total,
    int? enabled,
    int? healthy,
    int? degraded,
    int? failing,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoSources.copyWith(...)` or call `instanceOfSystemInfoSources.copyWith.fieldName(value)` for a single field.
class _$SystemInfoSourcesCWProxyImpl implements _$SystemInfoSourcesCWProxy {
  const _$SystemInfoSourcesCWProxyImpl(this._value);

  final SystemInfoSources _value;

  @override
  SystemInfoSources total(int? total) => call(total: total);

  @override
  SystemInfoSources enabled(int? enabled) => call(enabled: enabled);

  @override
  SystemInfoSources healthy(int? healthy) => call(healthy: healthy);

  @override
  SystemInfoSources degraded(int? degraded) => call(degraded: degraded);

  @override
  SystemInfoSources failing(int? failing) => call(failing: failing);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoSources(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoSources(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoSources call({
    Object? total = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? healthy = const $CopyWithPlaceholder(),
    Object? degraded = const $CopyWithPlaceholder(),
    Object? failing = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoSources(
      total: total == const $CopyWithPlaceholder()
          ? _value.total
          // ignore: cast_nullable_to_non_nullable
          : total as int?,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as int?,
      healthy: healthy == const $CopyWithPlaceholder()
          ? _value.healthy
          // ignore: cast_nullable_to_non_nullable
          : healthy as int?,
      degraded: degraded == const $CopyWithPlaceholder()
          ? _value.degraded
          // ignore: cast_nullable_to_non_nullable
          : degraded as int?,
      failing: failing == const $CopyWithPlaceholder()
          ? _value.failing
          // ignore: cast_nullable_to_non_nullable
          : failing as int?,
    );
  }
}

extension $SystemInfoSourcesCopyWith on SystemInfoSources {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoSources.copyWith(...)` or `instanceOfSystemInfoSources.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoSourcesCWProxy get copyWith =>
      _$SystemInfoSourcesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoSources _$SystemInfoSourcesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SystemInfoSources', json, ($checkedConvert) {
      final val = SystemInfoSources(
        total: $checkedConvert('total', (v) => (v as num?)?.toInt()),
        enabled: $checkedConvert('enabled', (v) => (v as num?)?.toInt()),
        healthy: $checkedConvert('healthy', (v) => (v as num?)?.toInt()),
        degraded: $checkedConvert('degraded', (v) => (v as num?)?.toInt()),
        failing: $checkedConvert('failing', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$SystemInfoSourcesToJson(SystemInfoSources instance) =>
    <String, dynamic>{
      'total': ?instance.total,
      'enabled': ?instance.enabled,
      'healthy': ?instance.healthy,
      'degraded': ?instance.degraded,
      'failing': ?instance.failing,
    };
