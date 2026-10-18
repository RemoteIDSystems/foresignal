// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_aurora.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherAuroraCWProxy {
  PlaceWeatherAurora status(SampleStatus? status);

  PlaceWeatherAurora probMaxNearby(num? probMaxNearby);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherAurora(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherAurora(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherAurora call({SampleStatus? status, num? probMaxNearby});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherAurora.copyWith(...)` or call `instanceOfPlaceWeatherAurora.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherAuroraCWProxyImpl implements _$PlaceWeatherAuroraCWProxy {
  const _$PlaceWeatherAuroraCWProxyImpl(this._value);

  final PlaceWeatherAurora _value;

  @override
  PlaceWeatherAurora status(SampleStatus? status) => call(status: status);

  @override
  PlaceWeatherAurora probMaxNearby(num? probMaxNearby) =>
      call(probMaxNearby: probMaxNearby);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherAurora(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherAurora(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherAurora call({
    Object? status = const $CopyWithPlaceholder(),
    Object? probMaxNearby = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherAurora(
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SampleStatus?,
      probMaxNearby: probMaxNearby == const $CopyWithPlaceholder()
          ? _value.probMaxNearby
          // ignore: cast_nullable_to_non_nullable
          : probMaxNearby as num?,
    );
  }
}

extension $PlaceWeatherAuroraCopyWith on PlaceWeatherAurora {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherAurora.copyWith(...)` or `instanceOfPlaceWeatherAurora.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherAuroraCWProxy get copyWith =>
      _$PlaceWeatherAuroraCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherAurora _$PlaceWeatherAuroraFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PlaceWeatherAurora', json, ($checkedConvert) {
      final val = PlaceWeatherAurora(
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(_$SampleStatusEnumMap, v),
        ),
        probMaxNearby: $checkedConvert('probMaxNearby', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$PlaceWeatherAuroraToJson(PlaceWeatherAurora instance) =>
    <String, dynamic>{
      'status': ?_$SampleStatusEnumMap[instance.status],
      'probMaxNearby': ?instance.probMaxNearby,
    };

const _$SampleStatusEnumMap = {
  SampleStatus.ok: 'ok',
  SampleStatus.noCoverage: 'no-coverage',
  SampleStatus.noData: 'no-data',
  SampleStatus.stale: 'stale',
  SampleStatus.unavailable: 'unavailable',
  SampleStatus.stalePosition: 'stale-position',
};
