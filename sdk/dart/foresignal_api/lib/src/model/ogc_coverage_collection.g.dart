// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_coverage_collection.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcCoverageCollectionCWProxy {
  OgcCoverageCollection id(String id);

  OgcCoverageCollection title(String? title);

  OgcCoverageCollection description(String? description);

  OgcCoverageCollection itemType(OgcCoverageCollectionItemTypeEnum itemType);

  OgcCoverageCollection extent(OgcCoverageCollectionExtent? extent);

  OgcCoverageCollection dataQueries(
    OgcCoverageCollectionDataQueries? dataQueries,
  );

  OgcCoverageCollection parameterNames(
    Map<String, Map<String, Object>>? parameterNames,
  );

  OgcCoverageCollection outputFormats(List<String>? outputFormats);

  OgcCoverageCollection crs(List<String>? crs);

  OgcCoverageCollection attribution(String? attribution);

  OgcCoverageCollection links(List<OgcLink> links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCoverageCollection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCoverageCollection(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcCoverageCollection call({
    String id,
    String? title,
    String? description,
    OgcCoverageCollectionItemTypeEnum itemType,
    OgcCoverageCollectionExtent? extent,
    OgcCoverageCollectionDataQueries? dataQueries,
    Map<String, Map<String, Object>>? parameterNames,
    List<String>? outputFormats,
    List<String>? crs,
    String? attribution,
    List<OgcLink> links,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcCoverageCollection.copyWith(...)` or call `instanceOfOgcCoverageCollection.copyWith.fieldName(value)` for a single field.
class _$OgcCoverageCollectionCWProxyImpl
    implements _$OgcCoverageCollectionCWProxy {
  const _$OgcCoverageCollectionCWProxyImpl(this._value);

  final OgcCoverageCollection _value;

  @override
  OgcCoverageCollection id(String id) => call(id: id);

  @override
  OgcCoverageCollection title(String? title) => call(title: title);

  @override
  OgcCoverageCollection description(String? description) =>
      call(description: description);

  @override
  OgcCoverageCollection itemType(OgcCoverageCollectionItemTypeEnum itemType) =>
      call(itemType: itemType);

  @override
  OgcCoverageCollection extent(OgcCoverageCollectionExtent? extent) =>
      call(extent: extent);

  @override
  OgcCoverageCollection dataQueries(
    OgcCoverageCollectionDataQueries? dataQueries,
  ) => call(dataQueries: dataQueries);

  @override
  OgcCoverageCollection parameterNames(
    Map<String, Map<String, Object>>? parameterNames,
  ) => call(parameterNames: parameterNames);

  @override
  OgcCoverageCollection outputFormats(List<String>? outputFormats) =>
      call(outputFormats: outputFormats);

  @override
  OgcCoverageCollection crs(List<String>? crs) => call(crs: crs);

  @override
  OgcCoverageCollection attribution(String? attribution) =>
      call(attribution: attribution);

  @override
  OgcCoverageCollection links(List<OgcLink> links) => call(links: links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCoverageCollection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCoverageCollection(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcCoverageCollection call({
    Object? id = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? itemType = const $CopyWithPlaceholder(),
    Object? extent = const $CopyWithPlaceholder(),
    Object? dataQueries = const $CopyWithPlaceholder(),
    Object? parameterNames = const $CopyWithPlaceholder(),
    Object? outputFormats = const $CopyWithPlaceholder(),
    Object? crs = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
  }) {
    return OgcCoverageCollection(
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
          : itemType as OgcCoverageCollectionItemTypeEnum,
      extent: extent == const $CopyWithPlaceholder()
          ? _value.extent
          // ignore: cast_nullable_to_non_nullable
          : extent as OgcCoverageCollectionExtent?,
      dataQueries: dataQueries == const $CopyWithPlaceholder()
          ? _value.dataQueries
          // ignore: cast_nullable_to_non_nullable
          : dataQueries as OgcCoverageCollectionDataQueries?,
      parameterNames: parameterNames == const $CopyWithPlaceholder()
          ? _value.parameterNames
          // ignore: cast_nullable_to_non_nullable
          : parameterNames as Map<String, Map<String, Object>>?,
      outputFormats: outputFormats == const $CopyWithPlaceholder()
          ? _value.outputFormats
          // ignore: cast_nullable_to_non_nullable
          : outputFormats as List<String>?,
      crs: crs == const $CopyWithPlaceholder()
          ? _value.crs
          // ignore: cast_nullable_to_non_nullable
          : crs as List<String>?,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as String?,
      links: links == const $CopyWithPlaceholder() || links == null
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as List<OgcLink>,
    );
  }
}

extension $OgcCoverageCollectionCopyWith on OgcCoverageCollection {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcCoverageCollection.copyWith(...)` or `instanceOfOgcCoverageCollection.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcCoverageCollectionCWProxy get copyWith =>
      _$OgcCoverageCollectionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcCoverageCollection _$OgcCoverageCollectionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate(
  'OgcCoverageCollection',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['id', 'itemType', 'links']);
    final val = OgcCoverageCollection(
      id: $checkedConvert('id', (v) => v as String),
      title: $checkedConvert('title', (v) => v as String?),
      description: $checkedConvert('description', (v) => v as String?),
      itemType: $checkedConvert(
        'itemType',
        (v) => $enumDecode(_$OgcCoverageCollectionItemTypeEnumEnumMap, v),
      ),
      extent: $checkedConvert(
        'extent',
        (v) => v == null
            ? null
            : OgcCoverageCollectionExtent.fromJson(v as Map<String, dynamic>),
      ),
      dataQueries: $checkedConvert(
        'data_queries',
        (v) => v == null
            ? null
            : OgcCoverageCollectionDataQueries.fromJson(
                v as Map<String, dynamic>,
              ),
      ),
      parameterNames: $checkedConvert(
        'parameter_names',
        (v) => (v as Map<String, dynamic>?)?.map(
          (k, e) => MapEntry(
            k,
            (e as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
          ),
        ),
      ),
      outputFormats: $checkedConvert(
        'output_formats',
        (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
      ),
      crs: $checkedConvert(
        'crs',
        (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
      ),
      attribution: $checkedConvert('attribution', (v) => v as String?),
      links: $checkedConvert(
        'links',
        (v) => (v as List<dynamic>)
            .map((e) => OgcLink.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
    );
    return val;
  },
  fieldKeyMap: const {
    'dataQueries': 'data_queries',
    'parameterNames': 'parameter_names',
    'outputFormats': 'output_formats',
  },
);

Map<String, dynamic> _$OgcCoverageCollectionToJson(
  OgcCoverageCollection instance,
) => <String, dynamic>{
  'id': instance.id,
  'title': ?instance.title,
  'description': ?instance.description,
  'itemType': _$OgcCoverageCollectionItemTypeEnumEnumMap[instance.itemType]!,
  'extent': ?instance.extent?.toJson(),
  'data_queries': ?instance.dataQueries?.toJson(),
  'parameter_names': ?instance.parameterNames,
  'output_formats': ?instance.outputFormats,
  'crs': ?instance.crs,
  'attribution': ?instance.attribution,
  'links': instance.links.map((e) => e.toJson()).toList(),
};

const _$OgcCoverageCollectionItemTypeEnumEnumMap = {
  OgcCoverageCollectionItemTypeEnum.coverage: 'coverage',
};
