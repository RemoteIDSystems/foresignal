// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'weather_context_lightning.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WeatherContextLightningCWProxy {
  WeatherContextLightning status(WeatherSectionStatus status);

  WeatherContextLightning asOf(DateTime? asOf);

  WeatherContextLightning statusText(String? statusText);

  WeatherContextLightning flashes20km15m(int? flashes20km15m);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextLightning(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextLightning(...).copyWith(id: 12, name: "My name")
  /// ```
  WeatherContextLightning call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    int? flashes20km15m,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWeatherContextLightning.copyWith(...)` or call `instanceOfWeatherContextLightning.copyWith.fieldName(value)` for a single field.
class _$WeatherContextLightningCWProxyImpl
    implements _$WeatherContextLightningCWProxy {
  const _$WeatherContextLightningCWProxyImpl(this._value);

  final WeatherContextLightning _value;

  @override
  WeatherContextLightning status(WeatherSectionStatus status) =>
      call(status: status);

  @override
  WeatherContextLightning asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  WeatherContextLightning statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  WeatherContextLightning flashes20km15m(int? flashes20km15m) =>
      call(flashes20km15m: flashes20km15m);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextLightning(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextLightning(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WeatherContextLightning call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? flashes20km15m = const $CopyWithPlaceholder(),
  }) {
    return WeatherContextLightning(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as WeatherSectionStatus,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      statusText: statusText == const $CopyWithPlaceholder()
          ? _value.statusText
          // ignore: cast_nullable_to_non_nullable
          : statusText as String?,
      flashes20km15m: flashes20km15m == const $CopyWithPlaceholder()
          ? _value.flashes20km15m
          // ignore: cast_nullable_to_non_nullable
          : flashes20km15m as int?,
    );
  }
}

extension $WeatherContextLightningCopyWith on WeatherContextLightning {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWeatherContextLightning.copyWith(...)` or `instanceOfWeatherContextLightning.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WeatherContextLightningCWProxy get copyWith =>
      _$WeatherContextLightningCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WeatherContextLightning _$WeatherContextLightningFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('WeatherContextLightning', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = WeatherContextLightning(
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$WeatherSectionStatusEnumMap, v),
    ),
    asOf: $checkedConvert(
      'asOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    statusText: $checkedConvert('statusText', (v) => v as String?),
    flashes20km15m: $checkedConvert(
      'flashes20km15m',
      (v) => (v as num?)?.toInt(),
    ),
  );
  return val;
});

Map<String, dynamic> _$WeatherContextLightningToJson(
  WeatherContextLightning instance,
) => <String, dynamic>{
  'status': _$WeatherSectionStatusEnumMap[instance.status]!,
  'asOf': ?instance.asOf?.toIso8601String(),
  'statusText': ?instance.statusText,
  'flashes20km15m': ?instance.flashes20km15m,
};

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
