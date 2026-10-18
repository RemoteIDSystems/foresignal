// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'region_tags.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegionTagsCWProxy {
  RegionTags iso2(String? iso2);

  RegionTags admin1(String? admin1);

  RegionTags admin2(String? admin2);

  RegionTags sea(String? sea);

  RegionTags near(RegionNear? near);

  RegionTags countryName(String? countryName);

  RegionTags admin1Name(String? admin1Name);

  RegionTags countyName(String? countyName);

  RegionTags attribution(String? attribution);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionTags(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionTags(...).copyWith(id: 12, name: "My name")
  /// ```
  RegionTags call({
    String? iso2,
    String? admin1,
    String? admin2,
    String? sea,
    RegionNear? near,
    String? countryName,
    String? admin1Name,
    String? countyName,
    String? attribution,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegionTags.copyWith(...)` or call `instanceOfRegionTags.copyWith.fieldName(value)` for a single field.
class _$RegionTagsCWProxyImpl implements _$RegionTagsCWProxy {
  const _$RegionTagsCWProxyImpl(this._value);

  final RegionTags _value;

  @override
  RegionTags iso2(String? iso2) => call(iso2: iso2);

  @override
  RegionTags admin1(String? admin1) => call(admin1: admin1);

  @override
  RegionTags admin2(String? admin2) => call(admin2: admin2);

  @override
  RegionTags sea(String? sea) => call(sea: sea);

  @override
  RegionTags near(RegionNear? near) => call(near: near);

  @override
  RegionTags countryName(String? countryName) => call(countryName: countryName);

  @override
  RegionTags admin1Name(String? admin1Name) => call(admin1Name: admin1Name);

  @override
  RegionTags countyName(String? countyName) => call(countyName: countyName);

  @override
  RegionTags attribution(String? attribution) => call(attribution: attribution);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionTags(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionTags(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegionTags call({
    Object? iso2 = const $CopyWithPlaceholder(),
    Object? admin1 = const $CopyWithPlaceholder(),
    Object? admin2 = const $CopyWithPlaceholder(),
    Object? sea = const $CopyWithPlaceholder(),
    Object? near = const $CopyWithPlaceholder(),
    Object? countryName = const $CopyWithPlaceholder(),
    Object? admin1Name = const $CopyWithPlaceholder(),
    Object? countyName = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
  }) {
    return RegionTags(
      iso2: iso2 == const $CopyWithPlaceholder()
          ? _value.iso2
          // ignore: cast_nullable_to_non_nullable
          : iso2 as String?,
      admin1: admin1 == const $CopyWithPlaceholder()
          ? _value.admin1
          // ignore: cast_nullable_to_non_nullable
          : admin1 as String?,
      admin2: admin2 == const $CopyWithPlaceholder()
          ? _value.admin2
          // ignore: cast_nullable_to_non_nullable
          : admin2 as String?,
      sea: sea == const $CopyWithPlaceholder()
          ? _value.sea
          // ignore: cast_nullable_to_non_nullable
          : sea as String?,
      near: near == const $CopyWithPlaceholder()
          ? _value.near
          // ignore: cast_nullable_to_non_nullable
          : near as RegionNear?,
      countryName: countryName == const $CopyWithPlaceholder()
          ? _value.countryName
          // ignore: cast_nullable_to_non_nullable
          : countryName as String?,
      admin1Name: admin1Name == const $CopyWithPlaceholder()
          ? _value.admin1Name
          // ignore: cast_nullable_to_non_nullable
          : admin1Name as String?,
      countyName: countyName == const $CopyWithPlaceholder()
          ? _value.countyName
          // ignore: cast_nullable_to_non_nullable
          : countyName as String?,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as String?,
    );
  }
}

extension $RegionTagsCopyWith on RegionTags {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegionTags.copyWith(...)` or `instanceOfRegionTags.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegionTagsCWProxy get copyWith => _$RegionTagsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegionTags _$RegionTagsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RegionTags', json, ($checkedConvert) {
      final val = RegionTags(
        iso2: $checkedConvert('iso2', (v) => v as String?),
        admin1: $checkedConvert('admin1', (v) => v as String?),
        admin2: $checkedConvert('admin2', (v) => v as String?),
        sea: $checkedConvert('sea', (v) => v as String?),
        near: $checkedConvert(
          'near',
          (v) =>
              v == null ? null : RegionNear.fromJson(v as Map<String, dynamic>),
        ),
        countryName: $checkedConvert('countryName', (v) => v as String?),
        admin1Name: $checkedConvert('admin1Name', (v) => v as String?),
        countyName: $checkedConvert('countyName', (v) => v as String?),
        attribution: $checkedConvert('attribution', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$RegionTagsToJson(RegionTags instance) =>
    <String, dynamic>{
      'iso2': ?instance.iso2,
      'admin1': ?instance.admin1,
      'admin2': ?instance.admin2,
      'sea': ?instance.sea,
      'near': ?instance.near?.toJson(),
      'countryName': ?instance.countryName,
      'admin1Name': ?instance.admin1Name,
      'countyName': ?instance.countyName,
      'attribution': ?instance.attribution,
    };
