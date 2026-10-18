// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'osm_and_location.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OsmAndLocationCWProxy {
  OsmAndLocation deviceId(String deviceId);

  OsmAndLocation location(OsmAndLocationLocation location);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OsmAndLocation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OsmAndLocation(...).copyWith(id: 12, name: "My name")
  /// ```
  OsmAndLocation call({String deviceId, OsmAndLocationLocation location});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOsmAndLocation.copyWith(...)` or call `instanceOfOsmAndLocation.copyWith.fieldName(value)` for a single field.
class _$OsmAndLocationCWProxyImpl implements _$OsmAndLocationCWProxy {
  const _$OsmAndLocationCWProxyImpl(this._value);

  final OsmAndLocation _value;

  @override
  OsmAndLocation deviceId(String deviceId) => call(deviceId: deviceId);

  @override
  OsmAndLocation location(OsmAndLocationLocation location) =>
      call(location: location);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OsmAndLocation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OsmAndLocation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OsmAndLocation call({
    Object? deviceId = const $CopyWithPlaceholder(),
    Object? location = const $CopyWithPlaceholder(),
  }) {
    return OsmAndLocation(
      deviceId: deviceId == const $CopyWithPlaceholder() || deviceId == null
          ? _value.deviceId
          // ignore: cast_nullable_to_non_nullable
          : deviceId as String,
      location: location == const $CopyWithPlaceholder() || location == null
          ? _value.location
          // ignore: cast_nullable_to_non_nullable
          : location as OsmAndLocationLocation,
    );
  }
}

extension $OsmAndLocationCopyWith on OsmAndLocation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOsmAndLocation.copyWith(...)` or `instanceOfOsmAndLocation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OsmAndLocationCWProxy get copyWith => _$OsmAndLocationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OsmAndLocation _$OsmAndLocationFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OsmAndLocation', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['device_id', 'location']);
      final val = OsmAndLocation(
        deviceId: $checkedConvert('device_id', (v) => v as String),
        location: $checkedConvert(
          'location',
          (v) => OsmAndLocationLocation.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    }, fieldKeyMap: const {'deviceId': 'device_id'});

Map<String, dynamic> _$OsmAndLocationToJson(OsmAndLocation instance) =>
    <String, dynamic>{
      'device_id': instance.deviceId,
      'location': instance.location.toJson(),
    };
