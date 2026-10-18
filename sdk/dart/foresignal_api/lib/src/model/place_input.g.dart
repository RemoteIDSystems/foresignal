// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceInputCWProxy {
  PlaceInput id(String id);

  PlaceInput name(String name);

  PlaceInput shape(PlaceInputShapeEnum shape);

  PlaceInput geometry(Geometry geometry);

  PlaceInput radiusKm(num? radiusKm);

  PlaceInput bufferM(num? bufferM);

  PlaceInput timezone(String? timezone);

  PlaceInput kind(PlaceInputKindEnum? kind);

  PlaceInput tags(List<String>? tags);

  PlaceInput products(List<String>? products);

  PlaceInput sensitive(bool? sensitive);

  PlaceInput attributes(PlaceInputAttributes? attributes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceInput(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceInput call({
    String id,
    String name,
    PlaceInputShapeEnum shape,
    Geometry geometry,
    num? radiusKm,
    num? bufferM,
    String? timezone,
    PlaceInputKindEnum? kind,
    List<String>? tags,
    List<String>? products,
    bool? sensitive,
    PlaceInputAttributes? attributes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceInput.copyWith(...)` or call `instanceOfPlaceInput.copyWith.fieldName(value)` for a single field.
class _$PlaceInputCWProxyImpl implements _$PlaceInputCWProxy {
  const _$PlaceInputCWProxyImpl(this._value);

  final PlaceInput _value;

  @override
  PlaceInput id(String id) => call(id: id);

  @override
  PlaceInput name(String name) => call(name: name);

  @override
  PlaceInput shape(PlaceInputShapeEnum shape) => call(shape: shape);

  @override
  PlaceInput geometry(Geometry geometry) => call(geometry: geometry);

  @override
  PlaceInput radiusKm(num? radiusKm) => call(radiusKm: radiusKm);

  @override
  PlaceInput bufferM(num? bufferM) => call(bufferM: bufferM);

  @override
  PlaceInput timezone(String? timezone) => call(timezone: timezone);

  @override
  PlaceInput kind(PlaceInputKindEnum? kind) => call(kind: kind);

  @override
  PlaceInput tags(List<String>? tags) => call(tags: tags);

  @override
  PlaceInput products(List<String>? products) => call(products: products);

  @override
  PlaceInput sensitive(bool? sensitive) => call(sensitive: sensitive);

  @override
  PlaceInput attributes(PlaceInputAttributes? attributes) =>
      call(attributes: attributes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceInput call({
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
  }) {
    return PlaceInput(
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
          : shape as PlaceInputShapeEnum,
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
          : kind as PlaceInputKindEnum?,
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
    );
  }
}

extension $PlaceInputCopyWith on PlaceInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceInput.copyWith(...)` or `instanceOfPlaceInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceInputCWProxy get copyWith => _$PlaceInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceInput _$PlaceInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PlaceInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'name', 'shape', 'geometry']);
      final val = PlaceInput(
        id: $checkedConvert('id', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String),
        shape: $checkedConvert(
          'shape',
          (v) => $enumDecode(_$PlaceInputShapeEnumEnumMap, v),
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
          (v) => $enumDecodeNullable(_$PlaceInputKindEnumEnumMap, v),
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
      );
      return val;
    });

Map<String, dynamic> _$PlaceInputToJson(PlaceInput instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'shape': _$PlaceInputShapeEnumEnumMap[instance.shape]!,
      'geometry': instance.geometry.toJson(),
      'radiusKm': ?instance.radiusKm,
      'bufferM': ?instance.bufferM,
      'timezone': ?instance.timezone,
      'kind': ?_$PlaceInputKindEnumEnumMap[instance.kind],
      'tags': ?instance.tags,
      'products': ?instance.products,
      'sensitive': ?instance.sensitive,
      'attributes': ?instance.attributes?.toJson(),
    };

const _$PlaceInputShapeEnumEnumMap = {
  PlaceInputShapeEnum.point: 'point',
  PlaceInputShapeEnum.polygon: 'polygon',
  PlaceInputShapeEnum.route: 'route',
};

const _$PlaceInputKindEnumEnumMap = {
  PlaceInputKindEnum.user: 'user',
  PlaceInputKindEnum.airport: 'airport',
  PlaceInputKindEnum.asset: 'asset',
};
