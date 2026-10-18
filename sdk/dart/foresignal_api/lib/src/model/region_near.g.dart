// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'region_near.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegionNearCWProxy {
  RegionNear geonameid(int geonameid);

  RegionNear name(String name);

  RegionNear iso2(String? iso2);

  RegionNear admin1(String? admin1);

  RegionNear distanceKm(num distanceKm);

  RegionNear bearingDeg(num? bearingDeg);

  RegionNear compass16(RegionNearCompass16Enum compass16);

  RegionNear population(int? population);

  RegionNear text(String? text);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionNear(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionNear(...).copyWith(id: 12, name: "My name")
  /// ```
  RegionNear call({
    int geonameid,
    String name,
    String? iso2,
    String? admin1,
    num distanceKm,
    num? bearingDeg,
    RegionNearCompass16Enum compass16,
    int? population,
    String? text,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegionNear.copyWith(...)` or call `instanceOfRegionNear.copyWith.fieldName(value)` for a single field.
class _$RegionNearCWProxyImpl implements _$RegionNearCWProxy {
  const _$RegionNearCWProxyImpl(this._value);

  final RegionNear _value;

  @override
  RegionNear geonameid(int geonameid) => call(geonameid: geonameid);

  @override
  RegionNear name(String name) => call(name: name);

  @override
  RegionNear iso2(String? iso2) => call(iso2: iso2);

  @override
  RegionNear admin1(String? admin1) => call(admin1: admin1);

  @override
  RegionNear distanceKm(num distanceKm) => call(distanceKm: distanceKm);

  @override
  RegionNear bearingDeg(num? bearingDeg) => call(bearingDeg: bearingDeg);

  @override
  RegionNear compass16(RegionNearCompass16Enum compass16) =>
      call(compass16: compass16);

  @override
  RegionNear population(int? population) => call(population: population);

  @override
  RegionNear text(String? text) => call(text: text);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionNear(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionNear(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegionNear call({
    Object? geonameid = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? iso2 = const $CopyWithPlaceholder(),
    Object? admin1 = const $CopyWithPlaceholder(),
    Object? distanceKm = const $CopyWithPlaceholder(),
    Object? bearingDeg = const $CopyWithPlaceholder(),
    Object? compass16 = const $CopyWithPlaceholder(),
    Object? population = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
  }) {
    return RegionNear(
      geonameid: geonameid == const $CopyWithPlaceholder() || geonameid == null
          ? _value.geonameid
          // ignore: cast_nullable_to_non_nullable
          : geonameid as int,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      iso2: iso2 == const $CopyWithPlaceholder()
          ? _value.iso2
          // ignore: cast_nullable_to_non_nullable
          : iso2 as String?,
      admin1: admin1 == const $CopyWithPlaceholder()
          ? _value.admin1
          // ignore: cast_nullable_to_non_nullable
          : admin1 as String?,
      distanceKm:
          distanceKm == const $CopyWithPlaceholder() || distanceKm == null
          ? _value.distanceKm
          // ignore: cast_nullable_to_non_nullable
          : distanceKm as num,
      bearingDeg: bearingDeg == const $CopyWithPlaceholder()
          ? _value.bearingDeg
          // ignore: cast_nullable_to_non_nullable
          : bearingDeg as num?,
      compass16: compass16 == const $CopyWithPlaceholder() || compass16 == null
          ? _value.compass16
          // ignore: cast_nullable_to_non_nullable
          : compass16 as RegionNearCompass16Enum,
      population: population == const $CopyWithPlaceholder()
          ? _value.population
          // ignore: cast_nullable_to_non_nullable
          : population as int?,
      text: text == const $CopyWithPlaceholder()
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String?,
    );
  }
}

extension $RegionNearCopyWith on RegionNear {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegionNear.copyWith(...)` or `instanceOfRegionNear.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegionNearCWProxy get copyWith => _$RegionNearCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegionNear _$RegionNearFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RegionNear', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['geonameid', 'name', 'distanceKm', 'compass16'],
      );
      final val = RegionNear(
        geonameid: $checkedConvert('geonameid', (v) => (v as num).toInt()),
        name: $checkedConvert('name', (v) => v as String),
        iso2: $checkedConvert('iso2', (v) => v as String?),
        admin1: $checkedConvert('admin1', (v) => v as String?),
        distanceKm: $checkedConvert('distanceKm', (v) => v as num),
        bearingDeg: $checkedConvert('bearingDeg', (v) => v as num?),
        compass16: $checkedConvert(
          'compass16',
          (v) => $enumDecode(_$RegionNearCompass16EnumEnumMap, v),
        ),
        population: $checkedConvert('population', (v) => (v as num?)?.toInt()),
        text: $checkedConvert('text', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$RegionNearToJson(RegionNear instance) =>
    <String, dynamic>{
      'geonameid': instance.geonameid,
      'name': instance.name,
      'iso2': ?instance.iso2,
      'admin1': ?instance.admin1,
      'distanceKm': instance.distanceKm,
      'bearingDeg': ?instance.bearingDeg,
      'compass16': _$RegionNearCompass16EnumEnumMap[instance.compass16]!,
      'population': ?instance.population,
      'text': ?instance.text,
    };

const _$RegionNearCompass16EnumEnumMap = {
  RegionNearCompass16Enum.N: 'N',
  RegionNearCompass16Enum.NNE: 'NNE',
  RegionNearCompass16Enum.NE: 'NE',
  RegionNearCompass16Enum.ENE: 'ENE',
  RegionNearCompass16Enum.E: 'E',
  RegionNearCompass16Enum.ESE: 'ESE',
  RegionNearCompass16Enum.SE: 'SE',
  RegionNearCompass16Enum.SSE: 'SSE',
  RegionNearCompass16Enum.S: 'S',
  RegionNearCompass16Enum.SSW: 'SSW',
  RegionNearCompass16Enum.SW: 'SW',
  RegionNearCompass16Enum.WSW: 'WSW',
  RegionNearCompass16Enum.W: 'W',
  RegionNearCompass16Enum.WNW: 'WNW',
  RegionNearCompass16Enum.NW: 'NW',
  RegionNearCompass16Enum.NNW: 'NNW',
};
