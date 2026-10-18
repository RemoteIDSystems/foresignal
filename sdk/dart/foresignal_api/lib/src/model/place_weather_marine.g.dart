// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_marine.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherMarineCWProxy {
  PlaceWeatherMarine status(SampleStatus? status);

  PlaceWeatherMarine waveHeightMaxM24h(num? waveHeightMaxM24h);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherMarine(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherMarine(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherMarine call({SampleStatus? status, num? waveHeightMaxM24h});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherMarine.copyWith(...)` or call `instanceOfPlaceWeatherMarine.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherMarineCWProxyImpl implements _$PlaceWeatherMarineCWProxy {
  const _$PlaceWeatherMarineCWProxyImpl(this._value);

  final PlaceWeatherMarine _value;

  @override
  PlaceWeatherMarine status(SampleStatus? status) => call(status: status);

  @override
  PlaceWeatherMarine waveHeightMaxM24h(num? waveHeightMaxM24h) =>
      call(waveHeightMaxM24h: waveHeightMaxM24h);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherMarine(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherMarine(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherMarine call({
    Object? status = const $CopyWithPlaceholder(),
    Object? waveHeightMaxM24h = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherMarine(
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SampleStatus?,
      waveHeightMaxM24h: waveHeightMaxM24h == const $CopyWithPlaceholder()
          ? _value.waveHeightMaxM24h
          // ignore: cast_nullable_to_non_nullable
          : waveHeightMaxM24h as num?,
    );
  }
}

extension $PlaceWeatherMarineCopyWith on PlaceWeatherMarine {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherMarine.copyWith(...)` or `instanceOfPlaceWeatherMarine.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherMarineCWProxy get copyWith =>
      _$PlaceWeatherMarineCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherMarine _$PlaceWeatherMarineFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PlaceWeatherMarine', json, ($checkedConvert) {
      final val = PlaceWeatherMarine(
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(_$SampleStatusEnumMap, v),
        ),
        waveHeightMaxM24h: $checkedConvert(
          'waveHeightMaxM24h',
          (v) => v as num?,
        ),
      );
      return val;
    });

Map<String, dynamic> _$PlaceWeatherMarineToJson(PlaceWeatherMarine instance) =>
    <String, dynamic>{
      'status': ?_$SampleStatusEnumMap[instance.status],
      'waveHeightMaxM24h': ?instance.waveHeightMaxM24h,
    };

const _$SampleStatusEnumMap = {
  SampleStatus.ok: 'ok',
  SampleStatus.noCoverage: 'no-coverage',
  SampleStatus.noData: 'no-data',
  SampleStatus.stale: 'stale',
  SampleStatus.unavailable: 'unavailable',
  SampleStatus.stalePosition: 'stale-position',
};
