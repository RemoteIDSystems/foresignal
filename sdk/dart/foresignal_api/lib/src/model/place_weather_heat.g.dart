// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_heat.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherHeatCWProxy {
  PlaceWeatherHeat status(SampleStatus? status);

  PlaceWeatherHeat riskMaxDay1(int? riskMaxDay1);

  PlaceWeatherHeat riskMax7d(int? riskMax7d);

  PlaceWeatherHeat peakDay(DateTime? peakDay);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherHeat(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherHeat(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherHeat call({
    SampleStatus? status,
    int? riskMaxDay1,
    int? riskMax7d,
    DateTime? peakDay,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherHeat.copyWith(...)` or call `instanceOfPlaceWeatherHeat.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherHeatCWProxyImpl implements _$PlaceWeatherHeatCWProxy {
  const _$PlaceWeatherHeatCWProxyImpl(this._value);

  final PlaceWeatherHeat _value;

  @override
  PlaceWeatherHeat status(SampleStatus? status) => call(status: status);

  @override
  PlaceWeatherHeat riskMaxDay1(int? riskMaxDay1) =>
      call(riskMaxDay1: riskMaxDay1);

  @override
  PlaceWeatherHeat riskMax7d(int? riskMax7d) => call(riskMax7d: riskMax7d);

  @override
  PlaceWeatherHeat peakDay(DateTime? peakDay) => call(peakDay: peakDay);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherHeat(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherHeat(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherHeat call({
    Object? status = const $CopyWithPlaceholder(),
    Object? riskMaxDay1 = const $CopyWithPlaceholder(),
    Object? riskMax7d = const $CopyWithPlaceholder(),
    Object? peakDay = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherHeat(
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SampleStatus?,
      riskMaxDay1: riskMaxDay1 == const $CopyWithPlaceholder()
          ? _value.riskMaxDay1
          // ignore: cast_nullable_to_non_nullable
          : riskMaxDay1 as int?,
      riskMax7d: riskMax7d == const $CopyWithPlaceholder()
          ? _value.riskMax7d
          // ignore: cast_nullable_to_non_nullable
          : riskMax7d as int?,
      peakDay: peakDay == const $CopyWithPlaceholder()
          ? _value.peakDay
          // ignore: cast_nullable_to_non_nullable
          : peakDay as DateTime?,
    );
  }
}

extension $PlaceWeatherHeatCopyWith on PlaceWeatherHeat {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherHeat.copyWith(...)` or `instanceOfPlaceWeatherHeat.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherHeatCWProxy get copyWith => _$PlaceWeatherHeatCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherHeat _$PlaceWeatherHeatFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PlaceWeatherHeat', json, ($checkedConvert) {
      final val = PlaceWeatherHeat(
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(_$SampleStatusEnumMap, v),
        ),
        riskMaxDay1: $checkedConvert(
          'riskMaxDay1',
          (v) => (v as num?)?.toInt(),
        ),
        riskMax7d: $checkedConvert('riskMax7d', (v) => (v as num?)?.toInt()),
        peakDay: $checkedConvert(
          'peakDay',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PlaceWeatherHeatToJson(PlaceWeatherHeat instance) =>
    <String, dynamic>{
      'status': ?_$SampleStatusEnumMap[instance.status],
      'riskMaxDay1': ?instance.riskMaxDay1,
      'riskMax7d': ?instance.riskMax7d,
      'peakDay': ?instance.peakDay?.toIso8601String(),
    };

const _$SampleStatusEnumMap = {
  SampleStatus.ok: 'ok',
  SampleStatus.noCoverage: 'no-coverage',
  SampleStatus.noData: 'no-data',
  SampleStatus.stale: 'stale',
  SampleStatus.unavailable: 'unavailable',
  SampleStatus.stalePosition: 'stale-position',
};
