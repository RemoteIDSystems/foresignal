// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_cell.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherCellCWProxy {
  PlaceWeatherCell status(WeatherSectionStatus status);

  PlaceWeatherCell asOf(DateTime? asOf);

  PlaceWeatherCell statusText(String? statusText);

  PlaceWeatherCell eta50Min(int? eta50Min);

  PlaceWeatherCell etaCellId(String? etaCellId);

  PlaceWeatherCell etaSpeedKmh(num? etaSpeedKmh);

  PlaceWeatherCell etaBearingDeg(int? etaBearingDeg);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherCell(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherCell(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherCell call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    int? eta50Min,
    String? etaCellId,
    num? etaSpeedKmh,
    int? etaBearingDeg,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherCell.copyWith(...)` or call `instanceOfPlaceWeatherCell.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherCellCWProxyImpl implements _$PlaceWeatherCellCWProxy {
  const _$PlaceWeatherCellCWProxyImpl(this._value);

  final PlaceWeatherCell _value;

  @override
  PlaceWeatherCell status(WeatherSectionStatus status) => call(status: status);

  @override
  PlaceWeatherCell asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  PlaceWeatherCell statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  PlaceWeatherCell eta50Min(int? eta50Min) => call(eta50Min: eta50Min);

  @override
  PlaceWeatherCell etaCellId(String? etaCellId) => call(etaCellId: etaCellId);

  @override
  PlaceWeatherCell etaSpeedKmh(num? etaSpeedKmh) =>
      call(etaSpeedKmh: etaSpeedKmh);

  @override
  PlaceWeatherCell etaBearingDeg(int? etaBearingDeg) =>
      call(etaBearingDeg: etaBearingDeg);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherCell(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherCell(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherCell call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? eta50Min = const $CopyWithPlaceholder(),
    Object? etaCellId = const $CopyWithPlaceholder(),
    Object? etaSpeedKmh = const $CopyWithPlaceholder(),
    Object? etaBearingDeg = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherCell(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as WeatherSectionStatus,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      statusText: statusText == const $CopyWithPlaceholder()
          ? _value.statusText
          // ignore: cast_nullable_to_non_nullable
          : statusText as String?,
      eta50Min: eta50Min == const $CopyWithPlaceholder()
          ? _value.eta50Min
          // ignore: cast_nullable_to_non_nullable
          : eta50Min as int?,
      etaCellId: etaCellId == const $CopyWithPlaceholder()
          ? _value.etaCellId
          // ignore: cast_nullable_to_non_nullable
          : etaCellId as String?,
      etaSpeedKmh: etaSpeedKmh == const $CopyWithPlaceholder()
          ? _value.etaSpeedKmh
          // ignore: cast_nullable_to_non_nullable
          : etaSpeedKmh as num?,
      etaBearingDeg: etaBearingDeg == const $CopyWithPlaceholder()
          ? _value.etaBearingDeg
          // ignore: cast_nullable_to_non_nullable
          : etaBearingDeg as int?,
    );
  }
}

extension $PlaceWeatherCellCopyWith on PlaceWeatherCell {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherCell.copyWith(...)` or `instanceOfPlaceWeatherCell.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherCellCWProxy get copyWith => _$PlaceWeatherCellCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherCell _$PlaceWeatherCellFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PlaceWeatherCell', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = PlaceWeatherCell(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$WeatherSectionStatusEnumMap, v),
        ),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        statusText: $checkedConvert('statusText', (v) => v as String?),
        eta50Min: $checkedConvert('eta50Min', (v) => (v as num?)?.toInt()),
        etaCellId: $checkedConvert('etaCellId', (v) => v as String?),
        etaSpeedKmh: $checkedConvert('etaSpeedKmh', (v) => v as num?),
        etaBearingDeg: $checkedConvert(
          'etaBearingDeg',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PlaceWeatherCellToJson(PlaceWeatherCell instance) =>
    <String, dynamic>{
      'status': _$WeatherSectionStatusEnumMap[instance.status]!,
      'asOf': ?instance.asOf?.toIso8601String(),
      'statusText': ?instance.statusText,
      'eta50Min': ?instance.eta50Min,
      'etaCellId': ?instance.etaCellId,
      'etaSpeedKmh': ?instance.etaSpeedKmh,
      'etaBearingDeg': ?instance.etaBearingDeg,
    };

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
