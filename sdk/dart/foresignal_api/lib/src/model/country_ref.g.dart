// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'country_ref.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CountryRefCWProxy {
  CountryRef iso2(String iso2);

  CountryRef iso3(String iso3);

  CountryRef isoNumeric(String? isoNumeric);

  CountryRef fips(String? fips);

  CountryRef name(String name);

  CountryRef nameLong(String? nameLong);

  CountryRef continent(String? continent);

  CountryRef labelLon(num labelLon);

  CountryRef labelLat(num labelLat);

  CountryRef aliases(List<String>? aliases);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CountryRef(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CountryRef(...).copyWith(id: 12, name: "My name")
  /// ```
  CountryRef call({
    String iso2,
    String iso3,
    String? isoNumeric,
    String? fips,
    String name,
    String? nameLong,
    String? continent,
    num labelLon,
    num labelLat,
    List<String>? aliases,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCountryRef.copyWith(...)` or call `instanceOfCountryRef.copyWith.fieldName(value)` for a single field.
class _$CountryRefCWProxyImpl implements _$CountryRefCWProxy {
  const _$CountryRefCWProxyImpl(this._value);

  final CountryRef _value;

  @override
  CountryRef iso2(String iso2) => call(iso2: iso2);

  @override
  CountryRef iso3(String iso3) => call(iso3: iso3);

  @override
  CountryRef isoNumeric(String? isoNumeric) => call(isoNumeric: isoNumeric);

  @override
  CountryRef fips(String? fips) => call(fips: fips);

  @override
  CountryRef name(String name) => call(name: name);

  @override
  CountryRef nameLong(String? nameLong) => call(nameLong: nameLong);

  @override
  CountryRef continent(String? continent) => call(continent: continent);

  @override
  CountryRef labelLon(num labelLon) => call(labelLon: labelLon);

  @override
  CountryRef labelLat(num labelLat) => call(labelLat: labelLat);

  @override
  CountryRef aliases(List<String>? aliases) => call(aliases: aliases);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CountryRef(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CountryRef(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CountryRef call({
    Object? iso2 = const $CopyWithPlaceholder(),
    Object? iso3 = const $CopyWithPlaceholder(),
    Object? isoNumeric = const $CopyWithPlaceholder(),
    Object? fips = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? nameLong = const $CopyWithPlaceholder(),
    Object? continent = const $CopyWithPlaceholder(),
    Object? labelLon = const $CopyWithPlaceholder(),
    Object? labelLat = const $CopyWithPlaceholder(),
    Object? aliases = const $CopyWithPlaceholder(),
  }) {
    return CountryRef(
      iso2: iso2 == const $CopyWithPlaceholder() || iso2 == null
          ? _value.iso2
          // ignore: cast_nullable_to_non_nullable
          : iso2 as String,
      iso3: iso3 == const $CopyWithPlaceholder() || iso3 == null
          ? _value.iso3
          // ignore: cast_nullable_to_non_nullable
          : iso3 as String,
      isoNumeric: isoNumeric == const $CopyWithPlaceholder()
          ? _value.isoNumeric
          // ignore: cast_nullable_to_non_nullable
          : isoNumeric as String?,
      fips: fips == const $CopyWithPlaceholder()
          ? _value.fips
          // ignore: cast_nullable_to_non_nullable
          : fips as String?,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      nameLong: nameLong == const $CopyWithPlaceholder()
          ? _value.nameLong
          // ignore: cast_nullable_to_non_nullable
          : nameLong as String?,
      continent: continent == const $CopyWithPlaceholder()
          ? _value.continent
          // ignore: cast_nullable_to_non_nullable
          : continent as String?,
      labelLon: labelLon == const $CopyWithPlaceholder() || labelLon == null
          ? _value.labelLon
          // ignore: cast_nullable_to_non_nullable
          : labelLon as num,
      labelLat: labelLat == const $CopyWithPlaceholder() || labelLat == null
          ? _value.labelLat
          // ignore: cast_nullable_to_non_nullable
          : labelLat as num,
      aliases: aliases == const $CopyWithPlaceholder()
          ? _value.aliases
          // ignore: cast_nullable_to_non_nullable
          : aliases as List<String>?,
    );
  }
}

extension $CountryRefCopyWith on CountryRef {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCountryRef.copyWith(...)` or `instanceOfCountryRef.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CountryRefCWProxy get copyWith => _$CountryRefCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CountryRef _$CountryRefFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CountryRef', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['iso2', 'iso3', 'name', 'labelLon', 'labelLat'],
      );
      final val = CountryRef(
        iso2: $checkedConvert('iso2', (v) => v as String),
        iso3: $checkedConvert('iso3', (v) => v as String),
        isoNumeric: $checkedConvert('isoNumeric', (v) => v as String?),
        fips: $checkedConvert('fips', (v) => v as String?),
        name: $checkedConvert('name', (v) => v as String),
        nameLong: $checkedConvert('nameLong', (v) => v as String?),
        continent: $checkedConvert('continent', (v) => v as String?),
        labelLon: $checkedConvert('labelLon', (v) => v as num),
        labelLat: $checkedConvert('labelLat', (v) => v as num),
        aliases: $checkedConvert(
          'aliases',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CountryRefToJson(CountryRef instance) =>
    <String, dynamic>{
      'iso2': instance.iso2,
      'iso3': instance.iso3,
      'isoNumeric': ?instance.isoNumeric,
      'fips': ?instance.fips,
      'name': instance.name,
      'nameLong': ?instance.nameLong,
      'continent': ?instance.continent,
      'labelLon': instance.labelLon,
      'labelLat': instance.labelLat,
      'aliases': ?instance.aliases,
    };
