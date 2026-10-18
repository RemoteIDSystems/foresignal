// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_hail.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherHailCWProxy {
  PlaceWeatherHail status(WeatherSectionStatus status);

  PlaceWeatherHail asOf(DateTime? asOf);

  PlaceWeatherHail statusText(String? statusText);

  PlaceWeatherHail meshMaxMmInRadius(num? meshMaxMmInRadius);

  PlaceWeatherHail meshNearest25Km(num? meshNearest25Km);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherHail(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherHail(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherHail call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    num? meshMaxMmInRadius,
    num? meshNearest25Km,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherHail.copyWith(...)` or call `instanceOfPlaceWeatherHail.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherHailCWProxyImpl implements _$PlaceWeatherHailCWProxy {
  const _$PlaceWeatherHailCWProxyImpl(this._value);

  final PlaceWeatherHail _value;

  @override
  PlaceWeatherHail status(WeatherSectionStatus status) => call(status: status);

  @override
  PlaceWeatherHail asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  PlaceWeatherHail statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  PlaceWeatherHail meshMaxMmInRadius(num? meshMaxMmInRadius) =>
      call(meshMaxMmInRadius: meshMaxMmInRadius);

  @override
  PlaceWeatherHail meshNearest25Km(num? meshNearest25Km) =>
      call(meshNearest25Km: meshNearest25Km);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherHail(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherHail(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherHail call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? meshMaxMmInRadius = const $CopyWithPlaceholder(),
    Object? meshNearest25Km = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherHail(
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
      meshMaxMmInRadius: meshMaxMmInRadius == const $CopyWithPlaceholder()
          ? _value.meshMaxMmInRadius
          // ignore: cast_nullable_to_non_nullable
          : meshMaxMmInRadius as num?,
      meshNearest25Km: meshNearest25Km == const $CopyWithPlaceholder()
          ? _value.meshNearest25Km
          // ignore: cast_nullable_to_non_nullable
          : meshNearest25Km as num?,
    );
  }
}

extension $PlaceWeatherHailCopyWith on PlaceWeatherHail {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherHail.copyWith(...)` or `instanceOfPlaceWeatherHail.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherHailCWProxy get copyWith => _$PlaceWeatherHailCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherHail _$PlaceWeatherHailFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PlaceWeatherHail', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = PlaceWeatherHail(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$WeatherSectionStatusEnumMap, v),
        ),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        statusText: $checkedConvert('statusText', (v) => v as String?),
        meshMaxMmInRadius: $checkedConvert(
          'meshMaxMmInRadius',
          (v) => v as num?,
        ),
        meshNearest25Km: $checkedConvert('meshNearest25Km', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$PlaceWeatherHailToJson(PlaceWeatherHail instance) =>
    <String, dynamic>{
      'status': _$WeatherSectionStatusEnumMap[instance.status]!,
      'asOf': ?instance.asOf?.toIso8601String(),
      'statusText': ?instance.statusText,
      'meshMaxMmInRadius': ?instance.meshMaxMmInRadius,
      'meshNearest25Km': ?instance.meshNearest25Km,
    };

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
