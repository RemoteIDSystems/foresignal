// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'weather_context_radar_nearest_cell.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WeatherContextRadarNearestCellCWProxy {
  WeatherContextRadarNearestCell id(String id);

  WeatherContextRadarNearestCell distanceKm(num? distanceKm);

  WeatherContextRadarNearestCell motion(
    WeatherContextRadarNearestCellMotion? motion,
  );

  WeatherContextRadarNearestCell meshMaxMm(num? meshMaxMm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextRadarNearestCell(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextRadarNearestCell(...).copyWith(id: 12, name: "My name")
  /// ```
  WeatherContextRadarNearestCell call({
    String id,
    num? distanceKm,
    WeatherContextRadarNearestCellMotion? motion,
    num? meshMaxMm,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWeatherContextRadarNearestCell.copyWith(...)` or call `instanceOfWeatherContextRadarNearestCell.copyWith.fieldName(value)` for a single field.
class _$WeatherContextRadarNearestCellCWProxyImpl
    implements _$WeatherContextRadarNearestCellCWProxy {
  const _$WeatherContextRadarNearestCellCWProxyImpl(this._value);

  final WeatherContextRadarNearestCell _value;

  @override
  WeatherContextRadarNearestCell id(String id) => call(id: id);

  @override
  WeatherContextRadarNearestCell distanceKm(num? distanceKm) =>
      call(distanceKm: distanceKm);

  @override
  WeatherContextRadarNearestCell motion(
    WeatherContextRadarNearestCellMotion? motion,
  ) => call(motion: motion);

  @override
  WeatherContextRadarNearestCell meshMaxMm(num? meshMaxMm) =>
      call(meshMaxMm: meshMaxMm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextRadarNearestCell(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextRadarNearestCell(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WeatherContextRadarNearestCell call({
    Object? id = const $CopyWithPlaceholder(),
    Object? distanceKm = const $CopyWithPlaceholder(),
    Object? motion = const $CopyWithPlaceholder(),
    Object? meshMaxMm = const $CopyWithPlaceholder(),
  }) {
    return WeatherContextRadarNearestCell(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      distanceKm: distanceKm == const $CopyWithPlaceholder()
          ? _value.distanceKm
          // ignore: cast_nullable_to_non_nullable
          : distanceKm as num?,
      motion: motion == const $CopyWithPlaceholder()
          ? _value.motion
          // ignore: cast_nullable_to_non_nullable
          : motion as WeatherContextRadarNearestCellMotion?,
      meshMaxMm: meshMaxMm == const $CopyWithPlaceholder()
          ? _value.meshMaxMm
          // ignore: cast_nullable_to_non_nullable
          : meshMaxMm as num?,
    );
  }
}

extension $WeatherContextRadarNearestCellCopyWith
    on WeatherContextRadarNearestCell {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWeatherContextRadarNearestCell.copyWith(...)` or `instanceOfWeatherContextRadarNearestCell.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WeatherContextRadarNearestCellCWProxy get copyWith =>
      _$WeatherContextRadarNearestCellCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WeatherContextRadarNearestCell _$WeatherContextRadarNearestCellFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('WeatherContextRadarNearestCell', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id']);
  final val = WeatherContextRadarNearestCell(
    id: $checkedConvert('id', (v) => v as String),
    distanceKm: $checkedConvert('distanceKm', (v) => v as num?),
    motion: $checkedConvert(
      'motion',
      (v) => v == null
          ? null
          : WeatherContextRadarNearestCellMotion.fromJson(
              v as Map<String, dynamic>,
            ),
    ),
    meshMaxMm: $checkedConvert('meshMaxMm', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$WeatherContextRadarNearestCellToJson(
  WeatherContextRadarNearestCell instance,
) => <String, dynamic>{
  'id': instance.id,
  'distanceKm': ?instance.distanceKm,
  'motion': ?instance.motion?.toJson(),
  'meshMaxMm': ?instance.meshMaxMm,
};
