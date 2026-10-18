// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sample_track_response_points_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SampleTrackResponsePointsInnerCWProxy {
  SampleTrackResponsePointsInner lon(num lon);

  SampleTrackResponsePointsInner lat(num lat);

  SampleTrackResponsePointsInner time(DateTime time);

  SampleTrackResponsePointsInner status(SampleStatus status);

  SampleTrackResponsePointsInner values(List<SampleValue> values);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleTrackResponsePointsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleTrackResponsePointsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SampleTrackResponsePointsInner call({
    num lon,
    num lat,
    DateTime time,
    SampleStatus status,
    List<SampleValue> values,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSampleTrackResponsePointsInner.copyWith(...)` or call `instanceOfSampleTrackResponsePointsInner.copyWith.fieldName(value)` for a single field.
class _$SampleTrackResponsePointsInnerCWProxyImpl
    implements _$SampleTrackResponsePointsInnerCWProxy {
  const _$SampleTrackResponsePointsInnerCWProxyImpl(this._value);

  final SampleTrackResponsePointsInner _value;

  @override
  SampleTrackResponsePointsInner lon(num lon) => call(lon: lon);

  @override
  SampleTrackResponsePointsInner lat(num lat) => call(lat: lat);

  @override
  SampleTrackResponsePointsInner time(DateTime time) => call(time: time);

  @override
  SampleTrackResponsePointsInner status(SampleStatus status) =>
      call(status: status);

  @override
  SampleTrackResponsePointsInner values(List<SampleValue> values) =>
      call(values: values);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleTrackResponsePointsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleTrackResponsePointsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SampleTrackResponsePointsInner call({
    Object? lon = const $CopyWithPlaceholder(),
    Object? lat = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? values = const $CopyWithPlaceholder(),
  }) {
    return SampleTrackResponsePointsInner(
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
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SampleStatus,
      values: values == const $CopyWithPlaceholder() || values == null
          ? _value.values
          // ignore: cast_nullable_to_non_nullable
          : values as List<SampleValue>,
    );
  }
}

extension $SampleTrackResponsePointsInnerCopyWith
    on SampleTrackResponsePointsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSampleTrackResponsePointsInner.copyWith(...)` or `instanceOfSampleTrackResponsePointsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SampleTrackResponsePointsInnerCWProxy get copyWith =>
      _$SampleTrackResponsePointsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SampleTrackResponsePointsInner _$SampleTrackResponsePointsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SampleTrackResponsePointsInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['lon', 'lat', 'time', 'status', 'values'],
  );
  final val = SampleTrackResponsePointsInner(
    lon: $checkedConvert('lon', (v) => v as num),
    lat: $checkedConvert('lat', (v) => v as num),
    time: $checkedConvert('time', (v) => DateTime.parse(v as String)),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$SampleStatusEnumMap, v),
    ),
    values: $checkedConvert(
      'values',
      (v) => (v as List<dynamic>)
          .map((e) => SampleValue.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$SampleTrackResponsePointsInnerToJson(
  SampleTrackResponsePointsInner instance,
) => <String, dynamic>{
  'lon': instance.lon,
  'lat': instance.lat,
  'time': instance.time.toIso8601String(),
  'status': _$SampleStatusEnumMap[instance.status]!,
  'values': instance.values.map((e) => e.toJson()).toList(),
};

const _$SampleStatusEnumMap = {
  SampleStatus.ok: 'ok',
  SampleStatus.noCoverage: 'no-coverage',
  SampleStatus.noData: 'no-data',
  SampleStatus.stale: 'stale',
  SampleStatus.unavailable: 'unavailable',
  SampleStatus.stalePosition: 'stale-position',
};
