// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_collection.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcCollectionCWProxy {
  OgcCollection id(String id);

  OgcCollection title(String? title);

  OgcCollection description(String? description);

  OgcCollection itemType(OgcCollectionItemTypeEnum itemType);

  OgcCollection crs(List<String>? crs);

  OgcCollection attribution(String? attribution);

  OgcCollection extent(OgcCollectionExtent? extent);

  OgcCollection links(List<OgcLink> links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCollection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCollection(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcCollection call({
    String id,
    String? title,
    String? description,
    OgcCollectionItemTypeEnum itemType,
    List<String>? crs,
    String? attribution,
    OgcCollectionExtent? extent,
    List<OgcLink> links,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcCollection.copyWith(...)` or call `instanceOfOgcCollection.copyWith.fieldName(value)` for a single field.
class _$OgcCollectionCWProxyImpl implements _$OgcCollectionCWProxy {
  const _$OgcCollectionCWProxyImpl(this._value);

  final OgcCollection _value;

  @override
  OgcCollection id(String id) => call(id: id);

  @override
  OgcCollection title(String? title) => call(title: title);

  @override
  OgcCollection description(String? description) =>
      call(description: description);

  @override
  OgcCollection itemType(OgcCollectionItemTypeEnum itemType) =>
      call(itemType: itemType);

  @override
  OgcCollection crs(List<String>? crs) => call(crs: crs);

  @override
  OgcCollection attribution(String? attribution) =>
      call(attribution: attribution);

  @override
  OgcCollection extent(OgcCollectionExtent? extent) => call(extent: extent);

  @override
  OgcCollection links(List<OgcLink> links) => call(links: links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCollection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCollection(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcCollection call({
    Object? id = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? itemType = const $CopyWithPlaceholder(),
    Object? crs = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? extent = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
  }) {
    return OgcCollection(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      itemType: itemType == const $CopyWithPlaceholder() || itemType == null
          ? _value.itemType
          // ignore: cast_nullable_to_non_nullable
          : itemType as OgcCollectionItemTypeEnum,
      crs: crs == const $CopyWithPlaceholder()
          ? _value.crs
          // ignore: cast_nullable_to_non_nullable
          : crs as List<String>?,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as String?,
      extent: extent == const $CopyWithPlaceholder()
          ? _value.extent
          // ignore: cast_nullable_to_non_nullable
          : extent as OgcCollectionExtent?,
      links: links == const $CopyWithPlaceholder() || links == null
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as List<OgcLink>,
    );
  }
}

extension $OgcCollectionCopyWith on OgcCollection {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcCollection.copyWith(...)` or `instanceOfOgcCollection.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcCollectionCWProxy get copyWith => _$OgcCollectionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcCollection _$OgcCollectionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OgcCollection', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'itemType', 'links']);
      final val = OgcCollection(
        id: $checkedConvert('id', (v) => v as String),
        title: $checkedConvert('title', (v) => v as String?),
        description: $checkedConvert('description', (v) => v as String?),
        itemType: $checkedConvert(
          'itemType',
          (v) => $enumDecode(_$OgcCollectionItemTypeEnumEnumMap, v),
        ),
        crs: $checkedConvert(
          'crs',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        attribution: $checkedConvert('attribution', (v) => v as String?),
        extent: $checkedConvert(
          'extent',
          (v) => v == null
              ? null
              : OgcCollectionExtent.fromJson(v as Map<String, dynamic>),
        ),
        links: $checkedConvert(
          'links',
          (v) => (v as List<dynamic>)
              .map((e) => OgcLink.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OgcCollectionToJson(OgcCollection instance) =>
    <String, dynamic>{
      'id': instance.id,
      'title': ?instance.title,
      'description': ?instance.description,
      'itemType': _$OgcCollectionItemTypeEnumEnumMap[instance.itemType]!,
      'crs': ?instance.crs,
      'attribution': ?instance.attribution,
      'extent': ?instance.extent?.toJson(),
      'links': instance.links.map((e) => e.toJson()).toList(),
    };

const _$OgcCollectionItemTypeEnumEnumMap = {
  OgcCollectionItemTypeEnum.feature: 'feature',
};
