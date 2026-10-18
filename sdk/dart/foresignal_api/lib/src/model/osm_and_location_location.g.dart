// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'osm_and_location_location.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OsmAndLocationLocationCWProxy {
  OsmAndLocationLocation timestamp(String? timestamp);

  OsmAndLocationLocation coords(OsmAndLocationLocationCoords coords);

  OsmAndLocationLocation battery(OsmAndLocationLocationBattery? battery);

  OsmAndLocationLocation isMoving(bool? isMoving);

  OsmAndLocationLocation event(String? event);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OsmAndLocationLocation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OsmAndLocationLocation(...).copyWith(id: 12, name: "My name")
  /// ```
  OsmAndLocationLocation call({
    String? timestamp,
    OsmAndLocationLocationCoords coords,
    OsmAndLocationLocationBattery? battery,
    bool? isMoving,
    String? event,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOsmAndLocationLocation.copyWith(...)` or call `instanceOfOsmAndLocationLocation.copyWith.fieldName(value)` for a single field.
class _$OsmAndLocationLocationCWProxyImpl
    implements _$OsmAndLocationLocationCWProxy {
  const _$OsmAndLocationLocationCWProxyImpl(this._value);

  final OsmAndLocationLocation _value;

  @override
  OsmAndLocationLocation timestamp(String? timestamp) =>
      call(timestamp: timestamp);

  @override
  OsmAndLocationLocation coords(OsmAndLocationLocationCoords coords) =>
      call(coords: coords);

  @override
  OsmAndLocationLocation battery(OsmAndLocationLocationBattery? battery) =>
      call(battery: battery);

  @override
  OsmAndLocationLocation isMoving(bool? isMoving) => call(isMoving: isMoving);

  @override
  OsmAndLocationLocation event(String? event) => call(event: event);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OsmAndLocationLocation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OsmAndLocationLocation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OsmAndLocationLocation call({
    Object? timestamp = const $CopyWithPlaceholder(),
    Object? coords = const $CopyWithPlaceholder(),
    Object? battery = const $CopyWithPlaceholder(),
    Object? isMoving = const $CopyWithPlaceholder(),
    Object? event = const $CopyWithPlaceholder(),
  }) {
    return OsmAndLocationLocation(
      timestamp: timestamp == const $CopyWithPlaceholder()
          ? _value.timestamp
          // ignore: cast_nullable_to_non_nullable
          : timestamp as String?,
      coords: coords == const $CopyWithPlaceholder() || coords == null
          ? _value.coords
          // ignore: cast_nullable_to_non_nullable
          : coords as OsmAndLocationLocationCoords,
      battery: battery == const $CopyWithPlaceholder()
          ? _value.battery
          // ignore: cast_nullable_to_non_nullable
          : battery as OsmAndLocationLocationBattery?,
      isMoving: isMoving == const $CopyWithPlaceholder()
          ? _value.isMoving
          // ignore: cast_nullable_to_non_nullable
          : isMoving as bool?,
      event: event == const $CopyWithPlaceholder()
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as String?,
    );
  }
}

extension $OsmAndLocationLocationCopyWith on OsmAndLocationLocation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOsmAndLocationLocation.copyWith(...)` or `instanceOfOsmAndLocationLocation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OsmAndLocationLocationCWProxy get copyWith =>
      _$OsmAndLocationLocationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OsmAndLocationLocation _$OsmAndLocationLocationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OsmAndLocationLocation', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['coords']);
  final val = OsmAndLocationLocation(
    timestamp: $checkedConvert('timestamp', (v) => v as String?),
    coords: $checkedConvert(
      'coords',
      (v) => OsmAndLocationLocationCoords.fromJson(v as Map<String, dynamic>),
    ),
    battery: $checkedConvert(
      'battery',
      (v) => v == null
          ? null
          : OsmAndLocationLocationBattery.fromJson(v as Map<String, dynamic>),
    ),
    isMoving: $checkedConvert('is_moving', (v) => v as bool?),
    event: $checkedConvert('event', (v) => v as String?),
  );
  return val;
}, fieldKeyMap: const {'isMoving': 'is_moving'});

Map<String, dynamic> _$OsmAndLocationLocationToJson(
  OsmAndLocationLocation instance,
) => <String, dynamic>{
  'timestamp': ?instance.timestamp,
  'coords': instance.coords.toJson(),
  'battery': ?instance.battery?.toJson(),
  'is_moving': ?instance.isMoving,
  'event': ?instance.event,
};
