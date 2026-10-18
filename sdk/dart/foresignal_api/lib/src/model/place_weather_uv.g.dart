// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_uv.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherUvCWProxy {
  PlaceWeatherUv status(SampleStatus? status);

  PlaceWeatherUv maxToday(num? maxToday);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherUv(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherUv(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherUv call({SampleStatus? status, num? maxToday});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherUv.copyWith(...)` or call `instanceOfPlaceWeatherUv.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherUvCWProxyImpl implements _$PlaceWeatherUvCWProxy {
  const _$PlaceWeatherUvCWProxyImpl(this._value);

  final PlaceWeatherUv _value;

  @override
  PlaceWeatherUv status(SampleStatus? status) => call(status: status);

  @override
  PlaceWeatherUv maxToday(num? maxToday) => call(maxToday: maxToday);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherUv(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherUv(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherUv call({
    Object? status = const $CopyWithPlaceholder(),
    Object? maxToday = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherUv(
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SampleStatus?,
      maxToday: maxToday == const $CopyWithPlaceholder()
          ? _value.maxToday
          // ignore: cast_nullable_to_non_nullable
          : maxToday as num?,
    );
  }
}

extension $PlaceWeatherUvCopyWith on PlaceWeatherUv {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherUv.copyWith(...)` or `instanceOfPlaceWeatherUv.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherUvCWProxy get copyWith => _$PlaceWeatherUvCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherUv _$PlaceWeatherUvFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PlaceWeatherUv', json, ($checkedConvert) {
      final val = PlaceWeatherUv(
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(_$SampleStatusEnumMap, v),
        ),
        maxToday: $checkedConvert('maxToday', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$PlaceWeatherUvToJson(PlaceWeatherUv instance) =>
    <String, dynamic>{
      'status': ?_$SampleStatusEnumMap[instance.status],
      'maxToday': ?instance.maxToday,
    };

const _$SampleStatusEnumMap = {
  SampleStatus.ok: 'ok',
  SampleStatus.noCoverage: 'no-coverage',
  SampleStatus.noData: 'no-data',
  SampleStatus.stale: 'stale',
  SampleStatus.unavailable: 'unavailable',
  SampleStatus.stalePosition: 'stale-position',
};
