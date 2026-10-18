// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'storm_track_payload_history_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StormTrackPayloadHistoryInnerCWProxy {
  StormTrackPayloadHistoryInner time(DateTime time);

  StormTrackPayloadHistoryInner lon(num lon);

  StormTrackPayloadHistoryInner lat(num lat);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormTrackPayloadHistoryInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormTrackPayloadHistoryInner(...).copyWith(id: 12, name: "My name")
  /// ```
  StormTrackPayloadHistoryInner call({DateTime time, num lon, num lat});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStormTrackPayloadHistoryInner.copyWith(...)` or call `instanceOfStormTrackPayloadHistoryInner.copyWith.fieldName(value)` for a single field.
class _$StormTrackPayloadHistoryInnerCWProxyImpl
    implements _$StormTrackPayloadHistoryInnerCWProxy {
  const _$StormTrackPayloadHistoryInnerCWProxyImpl(this._value);

  final StormTrackPayloadHistoryInner _value;

  @override
  StormTrackPayloadHistoryInner time(DateTime time) => call(time: time);

  @override
  StormTrackPayloadHistoryInner lon(num lon) => call(lon: lon);

  @override
  StormTrackPayloadHistoryInner lat(num lat) => call(lat: lat);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormTrackPayloadHistoryInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormTrackPayloadHistoryInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StormTrackPayloadHistoryInner call({
    Object? time = const $CopyWithPlaceholder(),
    Object? lon = const $CopyWithPlaceholder(),
    Object? lat = const $CopyWithPlaceholder(),
  }) {
    return StormTrackPayloadHistoryInner(
      time: time == const $CopyWithPlaceholder() || time == null
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as DateTime,
      lon: lon == const $CopyWithPlaceholder() || lon == null
          ? _value.lon
          // ignore: cast_nullable_to_non_nullable
          : lon as num,
      lat: lat == const $CopyWithPlaceholder() || lat == null
          ? _value.lat
          // ignore: cast_nullable_to_non_nullable
          : lat as num,
    );
  }
}

extension $StormTrackPayloadHistoryInnerCopyWith
    on StormTrackPayloadHistoryInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStormTrackPayloadHistoryInner.copyWith(...)` or `instanceOfStormTrackPayloadHistoryInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StormTrackPayloadHistoryInnerCWProxy get copyWith =>
      _$StormTrackPayloadHistoryInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StormTrackPayloadHistoryInner _$StormTrackPayloadHistoryInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StormTrackPayloadHistoryInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['time', 'lon', 'lat']);
  final val = StormTrackPayloadHistoryInner(
    time: $checkedConvert('time', (v) => DateTime.parse(v as String)),
    lon: $checkedConvert('lon', (v) => v as num),
    lat: $checkedConvert('lat', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$StormTrackPayloadHistoryInnerToJson(
  StormTrackPayloadHistoryInner instance,
) => <String, dynamic>{
  'time': instance.time.toIso8601String(),
  'lon': instance.lon,
  'lat': instance.lat,
};
