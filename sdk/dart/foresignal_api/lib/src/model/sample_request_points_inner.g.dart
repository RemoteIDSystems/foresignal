// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sample_request_points_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SampleRequestPointsInnerCWProxy {
  SampleRequestPointsInner lon(num lon);

  SampleRequestPointsInner lat(num lat);

  SampleRequestPointsInner place(String place);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleRequestPointsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleRequestPointsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SampleRequestPointsInner call({num lon, num lat, String place});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSampleRequestPointsInner.copyWith(...)` or call `instanceOfSampleRequestPointsInner.copyWith.fieldName(value)` for a single field.
class _$SampleRequestPointsInnerCWProxyImpl
    implements _$SampleRequestPointsInnerCWProxy {
  const _$SampleRequestPointsInnerCWProxyImpl(this._value);

  final SampleRequestPointsInner _value;

  @override
  SampleRequestPointsInner lon(num lon) => call(lon: lon);

  @override
  SampleRequestPointsInner lat(num lat) => call(lat: lat);

  @override
  SampleRequestPointsInner place(String place) => call(place: place);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleRequestPointsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleRequestPointsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SampleRequestPointsInner call({
    Object? lon = const $CopyWithPlaceholder(),
    Object? lat = const $CopyWithPlaceholder(),
    Object? place = const $CopyWithPlaceholder(),
  }) {
    return SampleRequestPointsInner(
      lon: lon == const $CopyWithPlaceholder() || lon == null
          ? _value.lon
          // ignore: cast_nullable_to_non_nullable
          : lon as num,
      lat: lat == const $CopyWithPlaceholder() || lat == null
          ? _value.lat
          // ignore: cast_nullable_to_non_nullable
          : lat as num,
      place: place == const $CopyWithPlaceholder() || place == null
          ? _value.place
          // ignore: cast_nullable_to_non_nullable
          : place as String,
    );
  }
}

extension $SampleRequestPointsInnerCopyWith on SampleRequestPointsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSampleRequestPointsInner.copyWith(...)` or `instanceOfSampleRequestPointsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SampleRequestPointsInnerCWProxy get copyWith =>
      _$SampleRequestPointsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SampleRequestPointsInner _$SampleRequestPointsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SampleRequestPointsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['lon', 'lat', 'place']);
  final val = SampleRequestPointsInner(
    lon: $checkedConvert('lon', (v) => v as num),
    lat: $checkedConvert('lat', (v) => v as num),
    place: $checkedConvert('place', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$SampleRequestPointsInnerToJson(
  SampleRequestPointsInner instance,
) => <String, dynamic>{
  'lon': instance.lon,
  'lat': instance.lat,
  'place': instance.place,
};
