// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'osm_and_location_location_coords.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OsmAndLocationLocationCoordsCWProxy {
  OsmAndLocationLocationCoords latitude(num latitude);

  OsmAndLocationLocationCoords longitude(num longitude);

  OsmAndLocationLocationCoords speed(num? speed);

  OsmAndLocationLocationCoords heading(num? heading);

  OsmAndLocationLocationCoords accuracy(num? accuracy);

  OsmAndLocationLocationCoords altitude(num? altitude);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OsmAndLocationLocationCoords(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OsmAndLocationLocationCoords(...).copyWith(id: 12, name: "My name")
  /// ```
  OsmAndLocationLocationCoords call({
    num latitude,
    num longitude,
    num? speed,
    num? heading,
    num? accuracy,
    num? altitude,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOsmAndLocationLocationCoords.copyWith(...)` or call `instanceOfOsmAndLocationLocationCoords.copyWith.fieldName(value)` for a single field.
class _$OsmAndLocationLocationCoordsCWProxyImpl
    implements _$OsmAndLocationLocationCoordsCWProxy {
  const _$OsmAndLocationLocationCoordsCWProxyImpl(this._value);

  final OsmAndLocationLocationCoords _value;

  @override
  OsmAndLocationLocationCoords latitude(num latitude) =>
      call(latitude: latitude);

  @override
  OsmAndLocationLocationCoords longitude(num longitude) =>
      call(longitude: longitude);

  @override
  OsmAndLocationLocationCoords speed(num? speed) => call(speed: speed);

  @override
  OsmAndLocationLocationCoords heading(num? heading) => call(heading: heading);

  @override
  OsmAndLocationLocationCoords accuracy(num? accuracy) =>
      call(accuracy: accuracy);

  @override
  OsmAndLocationLocationCoords altitude(num? altitude) =>
      call(altitude: altitude);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OsmAndLocationLocationCoords(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OsmAndLocationLocationCoords(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OsmAndLocationLocationCoords call({
    Object? latitude = const $CopyWithPlaceholder(),
    Object? longitude = const $CopyWithPlaceholder(),
    Object? speed = const $CopyWithPlaceholder(),
    Object? heading = const $CopyWithPlaceholder(),
    Object? accuracy = const $CopyWithPlaceholder(),
    Object? altitude = const $CopyWithPlaceholder(),
  }) {
    return OsmAndLocationLocationCoords(
      latitude: latitude == const $CopyWithPlaceholder() || latitude == null
          ? _value.latitude
          // ignore: cast_nullable_to_non_nullable
          : latitude as num,
      longitude: longitude == const $CopyWithPlaceholder() || longitude == null
          ? _value.longitude
          // ignore: cast_nullable_to_non_nullable
          : longitude as num,
      speed: speed == const $CopyWithPlaceholder()
          ? _value.speed
          // ignore: cast_nullable_to_non_nullable
          : speed as num?,
      heading: heading == const $CopyWithPlaceholder()
          ? _value.heading
          // ignore: cast_nullable_to_non_nullable
          : heading as num?,
      accuracy: accuracy == const $CopyWithPlaceholder()
          ? _value.accuracy
          // ignore: cast_nullable_to_non_nullable
          : accuracy as num?,
      altitude: altitude == const $CopyWithPlaceholder()
          ? _value.altitude
          // ignore: cast_nullable_to_non_nullable
          : altitude as num?,
    );
  }
}

extension $OsmAndLocationLocationCoordsCopyWith
    on OsmAndLocationLocationCoords {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOsmAndLocationLocationCoords.copyWith(...)` or `instanceOfOsmAndLocationLocationCoords.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OsmAndLocationLocationCoordsCWProxy get copyWith =>
      _$OsmAndLocationLocationCoordsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OsmAndLocationLocationCoords _$OsmAndLocationLocationCoordsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OsmAndLocationLocationCoords', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['latitude', 'longitude']);
  final val = OsmAndLocationLocationCoords(
    latitude: $checkedConvert('latitude', (v) => v as num),
    longitude: $checkedConvert('longitude', (v) => v as num),
    speed: $checkedConvert('speed', (v) => v as num?),
    heading: $checkedConvert('heading', (v) => v as num?),
    accuracy: $checkedConvert('accuracy', (v) => v as num?),
    altitude: $checkedConvert('altitude', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$OsmAndLocationLocationCoordsToJson(
  OsmAndLocationLocationCoords instance,
) => <String, dynamic>{
  'latitude': instance.latitude,
  'longitude': instance.longitude,
  'speed': ?instance.speed,
  'heading': ?instance.heading,
  'accuracy': ?instance.accuracy,
  'altitude': ?instance.altitude,
};
