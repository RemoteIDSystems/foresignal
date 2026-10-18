// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceCWProxy {
  Place id(String id);

  Place name(String name);

  Place shape(PlaceShapeEnum shape);

  Place geometry(Geometry geometry);

  Place radiusKm(num? radiusKm);

  Place bufferM(num? bufferM);

  Place timezone(String? timezone);

  Place kind(PlaceKindEnum? kind);

  Place tags(List<String>? tags);

  Place products(List<String>? products);

  Place sensitive(bool? sensitive);

  Place attributes(PlaceInputAttributes? attributes);

  Place area(Geometry? area);

  Place version(int version);

  Place createdAt(DateTime createdAt);

  Place updatedAt(DateTime updatedAt);

  Place external_(Map<String, Object>? external_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Place(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Place(...).copyWith(id: 12, name: "My name")
  /// ```
  Place call({
    String id,
    String name,
    PlaceShapeEnum shape,
    Geometry geometry,
    num? radiusKm,
    num? bufferM,
    String? timezone,
    PlaceKindEnum? kind,
    List<String>? tags,
    List<String>? products,
    bool? sensitive,
    PlaceInputAttributes? attributes,
    Geometry? area,
    int version,
    DateTime createdAt,
    DateTime updatedAt,
    Map<String, Object>? external_,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlace.copyWith(...)` or call `instanceOfPlace.copyWith.fieldName(value)` for a single field.
class _$PlaceCWProxyImpl implements _$PlaceCWProxy {
  const _$PlaceCWProxyImpl(this._value);

  final Place _value;

  @override
  Place id(String id) => call(id: id);

  @override
  Place name(String name) => call(name: name);

  @override
  Place shape(PlaceShapeEnum shape) => call(shape: shape);

  @override
  Place geometry(Geometry geometry) => call(geometry: geometry);

  @override
  Place radiusKm(num? radiusKm) => call(radiusKm: radiusKm);

  @override
  Place bufferM(num? bufferM) => call(bufferM: bufferM);

  @override
  Place timezone(String? timezone) => call(timezone: timezone);

  @override
  Place kind(PlaceKindEnum? kind) => call(kind: kind);

  @override
  Place tags(List<String>? tags) => call(tags: tags);

  @override
  Place products(List<String>? products) => call(products: products);

  @override
  Place sensitive(bool? sensitive) => call(sensitive: sensitive);

  @override
  Place attributes(PlaceInputAttributes? attributes) =>
      call(attributes: attributes);

  @override
  Place area(Geometry? area) => call(area: area);

  @override
  Place version(int version) => call(version: version);

  @override
  Place createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Place updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  @override
  Place external_(Map<String, Object>? external_) => call(external_: external_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Place(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Place(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Place call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? shape = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? radiusKm = const $CopyWithPlaceholder(),
    Object? bufferM = const $CopyWithPlaceholder(),
    Object? timezone = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? products = const $CopyWithPlaceholder(),
    Object? sensitive = const $CopyWithPlaceholder(),
    Object? attributes = const $CopyWithPlaceholder(),
    Object? area = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? external_ = const $CopyWithPlaceholder(),
  }) {
    return Place(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      shape: shape == const $CopyWithPlaceholder() || shape == null
          ? _value.shape
          // ignore: cast_nullable_to_non_nullable
          : shape as PlaceShapeEnum,
      geometry: geometry == const $CopyWithPlaceholder() || geometry == null
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry,
      radiusKm: radiusKm == const $CopyWithPlaceholder()
          ? _value.radiusKm
          // ignore: cast_nullable_to_non_nullable
          : radiusKm as num?,
      bufferM: bufferM == const $CopyWithPlaceholder()
          ? _value.bufferM
          // ignore: cast_nullable_to_non_nullable
          : bufferM as num?,
      timezone: timezone == const $CopyWithPlaceholder()
          ? _value.timezone
          // ignore: cast_nullable_to_non_nullable
          : timezone as String?,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as PlaceKindEnum?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      products: products == const $CopyWithPlaceholder()
          ? _value.products
          // ignore: cast_nullable_to_non_nullable
          : products as List<String>?,
      sensitive: sensitive == const $CopyWithPlaceholder()
          ? _value.sensitive
          // ignore: cast_nullable_to_non_nullable
          : sensitive as bool?,
      attributes: attributes == const $CopyWithPlaceholder()
          ? _value.attributes
          // ignore: cast_nullable_to_non_nullable
          : attributes as PlaceInputAttributes?,
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as Geometry?,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
      external_: external_ == const $CopyWithPlaceholder()
          ? _value.external_
          // ignore: cast_nullable_to_non_nullable
          : external_ as Map<String, Object>?,
    );
  }
}

extension $PlaceCopyWith on Place {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlace.copyWith(...)` or `instanceOfPlace.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceCWProxy get copyWith => _$PlaceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Place _$PlaceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Place', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'name',
      'shape',
      'geometry',
      'version',
      'createdAt',
      'updatedAt',
    ],
  );
  final val = Place(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    shape: $checkedConvert(
      'shape',
      (v) => $enumDecode(_$PlaceShapeEnumEnumMap, v),
    ),
    geometry: $checkedConvert(
      'geometry',
      (v) => Geometry.fromJson(v as Map<String, dynamic>),
    ),
    radiusKm: $checkedConvert('radiusKm', (v) => v as num? ?? 20),
    bufferM: $checkedConvert('bufferM', (v) => v as num?),
    timezone: $checkedConvert('timezone', (v) => v as String?),
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecodeNullable(_$PlaceKindEnumEnumMap, v),
    ),
    tags: $checkedConvert(
      'tags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    products: $checkedConvert(
      'products',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    sensitive: $checkedConvert('sensitive', (v) => v as bool? ?? true),
    attributes: $checkedConvert(
      'attributes',
      (v) => v == null
          ? null
          : PlaceInputAttributes.fromJson(v as Map<String, dynamic>),
    ),
    area: $checkedConvert(
      'area',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    version: $checkedConvert('version', (v) => (v as num).toInt()),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
    external_: $checkedConvert(
      'external',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
  );
  return val;
}, fieldKeyMap: const {'external_': 'external'});

Map<String, dynamic> _$PlaceToJson(Place instance) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
  'shape': _$PlaceShapeEnumEnumMap[instance.shape]!,
  'geometry': instance.geometry.toJson(),
  'radiusKm': ?instance.radiusKm,
  'bufferM': ?instance.bufferM,
  'timezone': ?instance.timezone,
  'kind': ?_$PlaceKindEnumEnumMap[instance.kind],
  'tags': ?instance.tags,
  'products': ?instance.products,
  'sensitive': ?instance.sensitive,
  'attributes': ?instance.attributes?.toJson(),
  'area': ?instance.area?.toJson(),
  'version': instance.version,
  'createdAt': instance.createdAt.toIso8601String(),
  'updatedAt': instance.updatedAt.toIso8601String(),
  'external': ?instance.external_,
};

const _$PlaceShapeEnumEnumMap = {
  PlaceShapeEnum.point: 'point',
  PlaceShapeEnum.polygon: 'polygon',
  PlaceShapeEnum.route: 'route',
};

const _$PlaceKindEnumEnumMap = {
  PlaceKindEnum.user: 'user',
  PlaceKindEnum.airport: 'airport',
  PlaceKindEnum.asset: 'asset',
};
