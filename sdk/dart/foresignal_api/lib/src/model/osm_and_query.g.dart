// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'osm_and_query.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OsmAndQueryCWProxy {
  OsmAndQuery id(String? id);

  OsmAndQuery deviceid(String? deviceid);

  OsmAndQuery lat(num? lat);

  OsmAndQuery lon(num? lon);

  OsmAndQuery location(String? location);

  OsmAndQuery timestamp(String? timestamp);

  OsmAndQuery speed(num? speed);

  OsmAndQuery bearing(num? bearing);

  OsmAndQuery heading(num? heading);

  OsmAndQuery altitude(num? altitude);

  OsmAndQuery accuracy(num? accuracy);

  OsmAndQuery batt(num? batt);

  OsmAndQuery charge(bool? charge);

  OsmAndQuery valid(bool? valid);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OsmAndQuery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OsmAndQuery(...).copyWith(id: 12, name: "My name")
  /// ```
  OsmAndQuery call({
    String? id,
    String? deviceid,
    num? lat,
    num? lon,
    String? location,
    String? timestamp,
    num? speed,
    num? bearing,
    num? heading,
    num? altitude,
    num? accuracy,
    num? batt,
    bool? charge,
    bool? valid,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOsmAndQuery.copyWith(...)` or call `instanceOfOsmAndQuery.copyWith.fieldName(value)` for a single field.
class _$OsmAndQueryCWProxyImpl implements _$OsmAndQueryCWProxy {
  const _$OsmAndQueryCWProxyImpl(this._value);

  final OsmAndQuery _value;

  @override
  OsmAndQuery id(String? id) => call(id: id);

  @override
  OsmAndQuery deviceid(String? deviceid) => call(deviceid: deviceid);

  @override
  OsmAndQuery lat(num? lat) => call(lat: lat);

  @override
  OsmAndQuery lon(num? lon) => call(lon: lon);

  @override
  OsmAndQuery location(String? location) => call(location: location);

  @override
  OsmAndQuery timestamp(String? timestamp) => call(timestamp: timestamp);

  @override
  OsmAndQuery speed(num? speed) => call(speed: speed);

  @override
  OsmAndQuery bearing(num? bearing) => call(bearing: bearing);

  @override
  OsmAndQuery heading(num? heading) => call(heading: heading);

  @override
  OsmAndQuery altitude(num? altitude) => call(altitude: altitude);

  @override
  OsmAndQuery accuracy(num? accuracy) => call(accuracy: accuracy);

  @override
  OsmAndQuery batt(num? batt) => call(batt: batt);

  @override
  OsmAndQuery charge(bool? charge) => call(charge: charge);

  @override
  OsmAndQuery valid(bool? valid) => call(valid: valid);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OsmAndQuery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OsmAndQuery(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OsmAndQuery call({
    Object? id = const $CopyWithPlaceholder(),
    Object? deviceid = const $CopyWithPlaceholder(),
    Object? lat = const $CopyWithPlaceholder(),
    Object? lon = const $CopyWithPlaceholder(),
    Object? location = const $CopyWithPlaceholder(),
    Object? timestamp = const $CopyWithPlaceholder(),
    Object? speed = const $CopyWithPlaceholder(),
    Object? bearing = const $CopyWithPlaceholder(),
    Object? heading = const $CopyWithPlaceholder(),
    Object? altitude = const $CopyWithPlaceholder(),
    Object? accuracy = const $CopyWithPlaceholder(),
    Object? batt = const $CopyWithPlaceholder(),
    Object? charge = const $CopyWithPlaceholder(),
    Object? valid = const $CopyWithPlaceholder(),
  }) {
    return OsmAndQuery(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      deviceid: deviceid == const $CopyWithPlaceholder()
          ? _value.deviceid
          // ignore: cast_nullable_to_non_nullable
          : deviceid as String?,
      lat: lat == const $CopyWithPlaceholder()
          ? _value.lat
          // ignore: cast_nullable_to_non_nullable
          : lat as num?,
      lon: lon == const $CopyWithPlaceholder()
          ? _value.lon
          // ignore: cast_nullable_to_non_nullable
          : lon as num?,
      location: location == const $CopyWithPlaceholder()
          ? _value.location
          // ignore: cast_nullable_to_non_nullable
          : location as String?,
      timestamp: timestamp == const $CopyWithPlaceholder()
          ? _value.timestamp
          // ignore: cast_nullable_to_non_nullable
          : timestamp as String?,
      speed: speed == const $CopyWithPlaceholder()
          ? _value.speed
          // ignore: cast_nullable_to_non_nullable
          : speed as num?,
      bearing: bearing == const $CopyWithPlaceholder()
          ? _value.bearing
          // ignore: cast_nullable_to_non_nullable
          : bearing as num?,
      heading: heading == const $CopyWithPlaceholder()
          ? _value.heading
          // ignore: cast_nullable_to_non_nullable
          : heading as num?,
      altitude: altitude == const $CopyWithPlaceholder()
          ? _value.altitude
          // ignore: cast_nullable_to_non_nullable
          : altitude as num?,
      accuracy: accuracy == const $CopyWithPlaceholder()
          ? _value.accuracy
          // ignore: cast_nullable_to_non_nullable
          : accuracy as num?,
      batt: batt == const $CopyWithPlaceholder()
          ? _value.batt
          // ignore: cast_nullable_to_non_nullable
          : batt as num?,
      charge: charge == const $CopyWithPlaceholder()
          ? _value.charge
          // ignore: cast_nullable_to_non_nullable
          : charge as bool?,
      valid: valid == const $CopyWithPlaceholder()
          ? _value.valid
          // ignore: cast_nullable_to_non_nullable
          : valid as bool?,
    );
  }
}

extension $OsmAndQueryCopyWith on OsmAndQuery {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOsmAndQuery.copyWith(...)` or `instanceOfOsmAndQuery.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OsmAndQueryCWProxy get copyWith => _$OsmAndQueryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OsmAndQuery _$OsmAndQueryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OsmAndQuery', json, ($checkedConvert) {
      final val = OsmAndQuery(
        id: $checkedConvert('id', (v) => v as String?),
        deviceid: $checkedConvert('deviceid', (v) => v as String?),
        lat: $checkedConvert('lat', (v) => v as num?),
        lon: $checkedConvert('lon', (v) => v as num?),
        location: $checkedConvert('location', (v) => v as String?),
        timestamp: $checkedConvert('timestamp', (v) => v as String?),
        speed: $checkedConvert('speed', (v) => v as num?),
        bearing: $checkedConvert('bearing', (v) => v as num?),
        heading: $checkedConvert('heading', (v) => v as num?),
        altitude: $checkedConvert('altitude', (v) => v as num?),
        accuracy: $checkedConvert('accuracy', (v) => v as num?),
        batt: $checkedConvert('batt', (v) => v as num?),
        charge: $checkedConvert('charge', (v) => v as bool?),
        valid: $checkedConvert('valid', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$OsmAndQueryToJson(OsmAndQuery instance) =>
    <String, dynamic>{
      'id': ?instance.id,
      'deviceid': ?instance.deviceid,
      'lat': ?instance.lat,
      'lon': ?instance.lon,
      'location': ?instance.location,
      'timestamp': ?instance.timestamp,
      'speed': ?instance.speed,
      'bearing': ?instance.bearing,
      'heading': ?instance.heading,
      'altitude': ?instance.altitude,
      'accuracy': ?instance.accuracy,
      'batt': ?instance.batt,
      'charge': ?instance.charge,
      'valid': ?instance.valid,
    };
