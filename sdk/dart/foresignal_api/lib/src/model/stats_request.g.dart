// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stats_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StatsRequestCWProxy {
  StatsRequest geometry(Geometry? geometry);

  StatsRequest place(String? place);

  StatsRequest bbox(String? bbox);

  StatsRequest time(String? time);

  StatsRequest run(String? run);

  StatsRequest thresholds(List<num>? thresholds);

  StatsRequest units(StatsRequestUnitsEnum? units);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  StatsRequest call({
    Geometry? geometry,
    String? place,
    String? bbox,
    String? time,
    String? run,
    List<num>? thresholds,
    StatsRequestUnitsEnum? units,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStatsRequest.copyWith(...)` or call `instanceOfStatsRequest.copyWith.fieldName(value)` for a single field.
class _$StatsRequestCWProxyImpl implements _$StatsRequestCWProxy {
  const _$StatsRequestCWProxyImpl(this._value);

  final StatsRequest _value;

  @override
  StatsRequest geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  StatsRequest place(String? place) => call(place: place);

  @override
  StatsRequest bbox(String? bbox) => call(bbox: bbox);

  @override
  StatsRequest time(String? time) => call(time: time);

  @override
  StatsRequest run(String? run) => call(run: run);

  @override
  StatsRequest thresholds(List<num>? thresholds) =>
      call(thresholds: thresholds);

  @override
  StatsRequest units(StatsRequestUnitsEnum? units) => call(units: units);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StatsRequest call({
    Object? geometry = const $CopyWithPlaceholder(),
    Object? place = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? thresholds = const $CopyWithPlaceholder(),
    Object? units = const $CopyWithPlaceholder(),
  }) {
    return StatsRequest(
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      place: place == const $CopyWithPlaceholder()
          ? _value.place
          // ignore: cast_nullable_to_non_nullable
          : place as String?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as String?,
      time: time == const $CopyWithPlaceholder()
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as String?,
      run: run == const $CopyWithPlaceholder()
          ? _value.run
          // ignore: cast_nullable_to_non_nullable
          : run as String?,
      thresholds: thresholds == const $CopyWithPlaceholder()
          ? _value.thresholds
          // ignore: cast_nullable_to_non_nullable
          : thresholds as List<num>?,
      units: units == const $CopyWithPlaceholder()
          ? _value.units
          // ignore: cast_nullable_to_non_nullable
          : units as StatsRequestUnitsEnum?,
    );
  }
}

extension $StatsRequestCopyWith on StatsRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStatsRequest.copyWith(...)` or `instanceOfStatsRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StatsRequestCWProxy get copyWith => _$StatsRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StatsRequest _$StatsRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StatsRequest', json, ($checkedConvert) {
      final val = StatsRequest(
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        place: $checkedConvert('place', (v) => v as String?),
        bbox: $checkedConvert('bbox', (v) => v as String?),
        time: $checkedConvert('time', (v) => v as String? ?? 'latest'),
        run: $checkedConvert('run', (v) => v as String?),
        thresholds: $checkedConvert(
          'thresholds',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        units: $checkedConvert(
          'units',
          (v) => $enumDecodeNullable(_$StatsRequestUnitsEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$StatsRequestToJson(StatsRequest instance) =>
    <String, dynamic>{
      'geometry': ?instance.geometry?.toJson(),
      'place': ?instance.place,
      'bbox': ?instance.bbox,
      'time': ?instance.time,
      'run': ?instance.run,
      'thresholds': ?instance.thresholds,
      'units': ?_$StatsRequestUnitsEnumEnumMap[instance.units],
    };

const _$StatsRequestUnitsEnumEnumMap = {
  StatsRequestUnitsEnum.metric: 'metric',
  StatsRequestUnitsEnum.imperial: 'imperial',
};
