// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'reference_area.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReferenceAreaCWProxy {
  ReferenceArea kind(ReferenceAreaKindEnum kind);

  ReferenceArea code(String? code);

  ReferenceArea name(String name);

  ReferenceArea label(String label);

  ReferenceArea countryIso2(String? countryIso2);

  ReferenceArea countryName(String? countryName);

  ReferenceArea admin1(String? admin1);

  ReferenceArea admin1Name(String? admin1Name);

  ReferenceArea lon(num lon);

  ReferenceArea lat(num lat);

  ReferenceArea placeId(int? placeId);

  ReferenceArea population(int? population);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceArea(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceArea(...).copyWith(id: 12, name: "My name")
  /// ```
  ReferenceArea call({
    ReferenceAreaKindEnum kind,
    String? code,
    String name,
    String label,
    String? countryIso2,
    String? countryName,
    String? admin1,
    String? admin1Name,
    num lon,
    num lat,
    int? placeId,
    int? population,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReferenceArea.copyWith(...)` or call `instanceOfReferenceArea.copyWith.fieldName(value)` for a single field.
class _$ReferenceAreaCWProxyImpl implements _$ReferenceAreaCWProxy {
  const _$ReferenceAreaCWProxyImpl(this._value);

  final ReferenceArea _value;

  @override
  ReferenceArea kind(ReferenceAreaKindEnum kind) => call(kind: kind);

  @override
  ReferenceArea code(String? code) => call(code: code);

  @override
  ReferenceArea name(String name) => call(name: name);

  @override
  ReferenceArea label(String label) => call(label: label);

  @override
  ReferenceArea countryIso2(String? countryIso2) =>
      call(countryIso2: countryIso2);

  @override
  ReferenceArea countryName(String? countryName) =>
      call(countryName: countryName);

  @override
  ReferenceArea admin1(String? admin1) => call(admin1: admin1);

  @override
  ReferenceArea admin1Name(String? admin1Name) => call(admin1Name: admin1Name);

  @override
  ReferenceArea lon(num lon) => call(lon: lon);

  @override
  ReferenceArea lat(num lat) => call(lat: lat);

  @override
  ReferenceArea placeId(int? placeId) => call(placeId: placeId);

  @override
  ReferenceArea population(int? population) => call(population: population);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceArea(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceArea(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReferenceArea call({
    Object? kind = const $CopyWithPlaceholder(),
    Object? code = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? countryIso2 = const $CopyWithPlaceholder(),
    Object? countryName = const $CopyWithPlaceholder(),
    Object? admin1 = const $CopyWithPlaceholder(),
    Object? admin1Name = const $CopyWithPlaceholder(),
    Object? lon = const $CopyWithPlaceholder(),
    Object? lat = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
    Object? population = const $CopyWithPlaceholder(),
  }) {
    return ReferenceArea(
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as ReferenceAreaKindEnum,
      code: code == const $CopyWithPlaceholder()
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as String?,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String,
      countryIso2: countryIso2 == const $CopyWithPlaceholder()
          ? _value.countryIso2
          // ignore: cast_nullable_to_non_nullable
          : countryIso2 as String?,
      countryName: countryName == const $CopyWithPlaceholder()
          ? _value.countryName
          // ignore: cast_nullable_to_non_nullable
          : countryName as String?,
      admin1: admin1 == const $CopyWithPlaceholder()
          ? _value.admin1
          // ignore: cast_nullable_to_non_nullable
          : admin1 as String?,
      admin1Name: admin1Name == const $CopyWithPlaceholder()
          ? _value.admin1Name
          // ignore: cast_nullable_to_non_nullable
          : admin1Name as String?,
      lon: lon == const $CopyWithPlaceholder() || lon == null
          ? _value.lon
          // ignore: cast_nullable_to_non_nullable
          : lon as num,
      lat: lat == const $CopyWithPlaceholder() || lat == null
          ? _value.lat
          // ignore: cast_nullable_to_non_nullable
          : lat as num,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as int?,
      population: population == const $CopyWithPlaceholder()
          ? _value.population
          // ignore: cast_nullable_to_non_nullable
          : population as int?,
    );
  }
}

extension $ReferenceAreaCopyWith on ReferenceArea {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReferenceArea.copyWith(...)` or `instanceOfReferenceArea.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReferenceAreaCWProxy get copyWith => _$ReferenceAreaCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReferenceArea _$ReferenceAreaFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReferenceArea', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['kind', 'name', 'label', 'lon', 'lat'],
      );
      final val = ReferenceArea(
        kind: $checkedConvert(
          'kind',
          (v) => $enumDecode(_$ReferenceAreaKindEnumEnumMap, v),
        ),
        code: $checkedConvert('code', (v) => v as String?),
        name: $checkedConvert('name', (v) => v as String),
        label: $checkedConvert('label', (v) => v as String),
        countryIso2: $checkedConvert('countryIso2', (v) => v as String?),
        countryName: $checkedConvert('countryName', (v) => v as String?),
        admin1: $checkedConvert('admin1', (v) => v as String?),
        admin1Name: $checkedConvert('admin1Name', (v) => v as String?),
        lon: $checkedConvert('lon', (v) => v as num),
        lat: $checkedConvert('lat', (v) => v as num),
        placeId: $checkedConvert('placeId', (v) => (v as num?)?.toInt()),
        population: $checkedConvert('population', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$ReferenceAreaToJson(ReferenceArea instance) =>
    <String, dynamic>{
      'kind': _$ReferenceAreaKindEnumEnumMap[instance.kind]!,
      'code': ?instance.code,
      'name': instance.name,
      'label': instance.label,
      'countryIso2': ?instance.countryIso2,
      'countryName': ?instance.countryName,
      'admin1': ?instance.admin1,
      'admin1Name': ?instance.admin1Name,
      'lon': instance.lon,
      'lat': instance.lat,
      'placeId': ?instance.placeId,
      'population': ?instance.population,
    };

const _$ReferenceAreaKindEnumEnumMap = {
  ReferenceAreaKindEnum.country: 'country',
  ReferenceAreaKindEnum.admin1: 'admin1',
  ReferenceAreaKindEnum.place: 'place',
};
