// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'weather_context_radar.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WeatherContextRadarCWProxy {
  WeatherContextRadar status(WeatherSectionStatus status);

  WeatherContextRadar asOf(DateTime? asOf);

  WeatherContextRadar statusText(String? statusText);

  WeatherContextRadar maxDbz(num? maxDbz);

  WeatherContextRadar nearestCell(WeatherContextRadarNearestCell? nearestCell);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextRadar(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextRadar(...).copyWith(id: 12, name: "My name")
  /// ```
  WeatherContextRadar call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    num? maxDbz,
    WeatherContextRadarNearestCell? nearestCell,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWeatherContextRadar.copyWith(...)` or call `instanceOfWeatherContextRadar.copyWith.fieldName(value)` for a single field.
class _$WeatherContextRadarCWProxyImpl implements _$WeatherContextRadarCWProxy {
  const _$WeatherContextRadarCWProxyImpl(this._value);

  final WeatherContextRadar _value;

  @override
  WeatherContextRadar status(WeatherSectionStatus status) =>
      call(status: status);

  @override
  WeatherContextRadar asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  WeatherContextRadar statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  WeatherContextRadar maxDbz(num? maxDbz) => call(maxDbz: maxDbz);

  @override
  WeatherContextRadar nearestCell(
    WeatherContextRadarNearestCell? nearestCell,
  ) => call(nearestCell: nearestCell);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextRadar(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextRadar(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WeatherContextRadar call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? maxDbz = const $CopyWithPlaceholder(),
    Object? nearestCell = const $CopyWithPlaceholder(),
  }) {
    return WeatherContextRadar(
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
      maxDbz: maxDbz == const $CopyWithPlaceholder()
          ? _value.maxDbz
          // ignore: cast_nullable_to_non_nullable
          : maxDbz as num?,
      nearestCell: nearestCell == const $CopyWithPlaceholder()
          ? _value.nearestCell
          // ignore: cast_nullable_to_non_nullable
          : nearestCell as WeatherContextRadarNearestCell?,
    );
  }
}

extension $WeatherContextRadarCopyWith on WeatherContextRadar {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWeatherContextRadar.copyWith(...)` or `instanceOfWeatherContextRadar.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WeatherContextRadarCWProxy get copyWith =>
      _$WeatherContextRadarCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WeatherContextRadar _$WeatherContextRadarFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WeatherContextRadar', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = WeatherContextRadar(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$WeatherSectionStatusEnumMap, v),
        ),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        statusText: $checkedConvert('statusText', (v) => v as String?),
        maxDbz: $checkedConvert('maxDbz', (v) => v as num?),
        nearestCell: $checkedConvert(
          'nearestCell',
          (v) => v == null
              ? null
              : WeatherContextRadarNearestCell.fromJson(
                  v as Map<String, dynamic>,
                ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$WeatherContextRadarToJson(
  WeatherContextRadar instance,
) => <String, dynamic>{
  'status': _$WeatherSectionStatusEnumMap[instance.status]!,
  'asOf': ?instance.asOf?.toIso8601String(),
  'statusText': ?instance.statusText,
  'maxDbz': ?instance.maxDbz,
  'nearestCell': ?instance.nearestCell?.toJson(),
};

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
