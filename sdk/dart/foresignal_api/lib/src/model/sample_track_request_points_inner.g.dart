// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sample_track_request_points_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SampleTrackRequestPointsInnerCWProxy {
  SampleTrackRequestPointsInner lon(num lon);

  SampleTrackRequestPointsInner lat(num lat);

  SampleTrackRequestPointsInner time(DateTime time);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleTrackRequestPointsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleTrackRequestPointsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SampleTrackRequestPointsInner call({num lon, num lat, DateTime time});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSampleTrackRequestPointsInner.copyWith(...)` or call `instanceOfSampleTrackRequestPointsInner.copyWith.fieldName(value)` for a single field.
class _$SampleTrackRequestPointsInnerCWProxyImpl
    implements _$SampleTrackRequestPointsInnerCWProxy {
  const _$SampleTrackRequestPointsInnerCWProxyImpl(this._value);

  final SampleTrackRequestPointsInner _value;

  @override
  SampleTrackRequestPointsInner lon(num lon) => call(lon: lon);

  @override
  SampleTrackRequestPointsInner lat(num lat) => call(lat: lat);

  @override
  SampleTrackRequestPointsInner time(DateTime time) => call(time: time);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleTrackRequestPointsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleTrackRequestPointsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SampleTrackRequestPointsInner call({
    Object? lon = const $CopyWithPlaceholder(),
    Object? lat = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
  }) {
    return SampleTrackRequestPointsInner(
      lon: lon == const $CopyWithPlaceholder() || lon == null
          ? _value.lon
          // ignore: cast_nullable_to_non_nullable
          : lon as num,
      lat: lat == const $CopyWithPlaceholder() || lat == null
          ? _value.lat
          // ignore: cast_nullable_to_non_nullable
          : lat as num,
      time: time == const $CopyWithPlaceholder() || time == null
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as DateTime,
    );
  }
}

extension $SampleTrackRequestPointsInnerCopyWith
    on SampleTrackRequestPointsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSampleTrackRequestPointsInner.copyWith(...)` or `instanceOfSampleTrackRequestPointsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SampleTrackRequestPointsInnerCWProxy get copyWith =>
      _$SampleTrackRequestPointsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SampleTrackRequestPointsInner _$SampleTrackRequestPointsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SampleTrackRequestPointsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['lon', 'lat', 'time']);
  final val = SampleTrackRequestPointsInner(
    lon: $checkedConvert('lon', (v) => v as num),
    lat: $checkedConvert('lat', (v) => v as num),
    time: $checkedConvert('time', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$SampleTrackRequestPointsInnerToJson(
  SampleTrackRequestPointsInner instance,
) => <String, dynamic>{
  'lon': instance.lon,
  'lat': instance.lat,
  'time': instance.time.toIso8601String(),
};
