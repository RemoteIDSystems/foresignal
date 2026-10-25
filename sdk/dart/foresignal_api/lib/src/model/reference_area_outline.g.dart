// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'reference_area_outline.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReferenceAreaOutlineCWProxy {
  ReferenceAreaOutline kind(ReferenceAreaOutlineKindEnum kind);

  ReferenceAreaOutline code(String code);

  ReferenceAreaOutline name(String name);

  ReferenceAreaOutline countryIso2(String? countryIso2);

  ReferenceAreaOutline countryName(String? countryName);

  ReferenceAreaOutline bbox(List<num> bbox);

  ReferenceAreaOutline geometry(Geometry geometry);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceAreaOutline(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceAreaOutline(...).copyWith(id: 12, name: "My name")
  /// ```
  ReferenceAreaOutline call({
    ReferenceAreaOutlineKindEnum kind,
    String code,
    String name,
    String? countryIso2,
    String? countryName,
    List<num> bbox,
    Geometry geometry,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReferenceAreaOutline.copyWith(...)` or call `instanceOfReferenceAreaOutline.copyWith.fieldName(value)` for a single field.
class _$ReferenceAreaOutlineCWProxyImpl
    implements _$ReferenceAreaOutlineCWProxy {
  const _$ReferenceAreaOutlineCWProxyImpl(this._value);

  final ReferenceAreaOutline _value;

  @override
  ReferenceAreaOutline kind(ReferenceAreaOutlineKindEnum kind) =>
      call(kind: kind);

  @override
  ReferenceAreaOutline code(String code) => call(code: code);

  @override
  ReferenceAreaOutline name(String name) => call(name: name);

  @override
  ReferenceAreaOutline countryIso2(String? countryIso2) =>
      call(countryIso2: countryIso2);

  @override
  ReferenceAreaOutline countryName(String? countryName) =>
      call(countryName: countryName);

  @override
  ReferenceAreaOutline bbox(List<num> bbox) => call(bbox: bbox);

  @override
  ReferenceAreaOutline geometry(Geometry geometry) => call(geometry: geometry);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceAreaOutline(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceAreaOutline(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReferenceAreaOutline call({
    Object? kind = const $CopyWithPlaceholder(),
    Object? code = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? countryIso2 = const $CopyWithPlaceholder(),
    Object? countryName = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
  }) {
    return ReferenceAreaOutline(
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as ReferenceAreaOutlineKindEnum,
      code: code == const $CopyWithPlaceholder() || code == null
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      countryIso2: countryIso2 == const $CopyWithPlaceholder()
          ? _value.countryIso2
          // ignore: cast_nullable_to_non_nullable
          : countryIso2 as String?,
      countryName: countryName == const $CopyWithPlaceholder()
          ? _value.countryName
          // ignore: cast_nullable_to_non_nullable
          : countryName as String?,
      bbox: bbox == const $CopyWithPlaceholder() || bbox == null
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>,
      geometry: geometry == const $CopyWithPlaceholder() || geometry == null
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry,
    );
  }
}

extension $ReferenceAreaOutlineCopyWith on ReferenceAreaOutline {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReferenceAreaOutline.copyWith(...)` or `instanceOfReferenceAreaOutline.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReferenceAreaOutlineCWProxy get copyWith =>
      _$ReferenceAreaOutlineCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReferenceAreaOutline _$ReferenceAreaOutlineFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReferenceAreaOutline', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['kind', 'code', 'name', 'bbox', 'geometry'],
  );
  final val = ReferenceAreaOutline(
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecode(_$ReferenceAreaOutlineKindEnumEnumMap, v),
    ),
    code: $checkedConvert('code', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    countryIso2: $checkedConvert('countryIso2', (v) => v as String?),
    countryName: $checkedConvert('countryName', (v) => v as String?),
    bbox: $checkedConvert(
      'bbox',
      (v) => (v as List<dynamic>).map((e) => e as num).toList(),
    ),
    geometry: $checkedConvert(
      'geometry',
      (v) => Geometry.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$ReferenceAreaOutlineToJson(
  ReferenceAreaOutline instance,
) => <String, dynamic>{
  'kind': _$ReferenceAreaOutlineKindEnumEnumMap[instance.kind]!,
  'code': instance.code,
  'name': instance.name,
  'countryIso2': ?instance.countryIso2,
  'countryName': ?instance.countryName,
  'bbox': instance.bbox,
  'geometry': instance.geometry.toJson(),
};

const _$ReferenceAreaOutlineKindEnumEnumMap = {
  ReferenceAreaOutlineKindEnum.country: 'country',
  ReferenceAreaOutlineKindEnum.admin1: 'admin1',
};
