// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_collection200_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcCollection200ResponseCWProxy {
  OgcCollection200Response id(String id);

  OgcCollection200Response title(String? title);

  OgcCollection200Response description(String? description);

  OgcCollection200Response itemType(
    OgcCollection200ResponseItemTypeEnum itemType,
  );

  OgcCollection200Response crs(List<String>? crs);

  OgcCollection200Response attribution(String? attribution);

  OgcCollection200Response extent(OgcCoverageCollectionExtent? extent);

  OgcCollection200Response links(List<OgcLink> links);

  OgcCollection200Response dataQueries(
    OgcCoverageCollectionDataQueries? dataQueries,
  );

  OgcCollection200Response parameterNames(
    Map<String, Map<String, Object>>? parameterNames,
  );

  OgcCollection200Response outputFormats(List<String>? outputFormats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCollection200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCollection200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcCollection200Response call({
    String id,
    String? title,
    String? description,
    OgcCollection200ResponseItemTypeEnum itemType,
    List<String>? crs,
    String? attribution,
    OgcCoverageCollectionExtent? extent,
    List<OgcLink> links,
    OgcCoverageCollectionDataQueries? dataQueries,
    Map<String, Map<String, Object>>? parameterNames,
    List<String>? outputFormats,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcCollection200Response.copyWith(...)` or call `instanceOfOgcCollection200Response.copyWith.fieldName(value)` for a single field.
class _$OgcCollection200ResponseCWProxyImpl
    implements _$OgcCollection200ResponseCWProxy {
  const _$OgcCollection200ResponseCWProxyImpl(this._value);

  final OgcCollection200Response _value;

  @override
  OgcCollection200Response id(String id) => call(id: id);

  @override
  OgcCollection200Response title(String? title) => call(title: title);

  @override
  OgcCollection200Response description(String? description) =>
      call(description: description);

  @override
  OgcCollection200Response itemType(
    OgcCollection200ResponseItemTypeEnum itemType,
  ) => call(itemType: itemType);

  @override
  OgcCollection200Response crs(List<String>? crs) => call(crs: crs);

  @override
  OgcCollection200Response attribution(String? attribution) =>
      call(attribution: attribution);

  @override
  OgcCollection200Response extent(OgcCoverageCollectionExtent? extent) =>
      call(extent: extent);

  @override
  OgcCollection200Response links(List<OgcLink> links) => call(links: links);

  @override
  OgcCollection200Response dataQueries(
    OgcCoverageCollectionDataQueries? dataQueries,
  ) => call(dataQueries: dataQueries);

  @override
  OgcCollection200Response parameterNames(
    Map<String, Map<String, Object>>? parameterNames,
  ) => call(parameterNames: parameterNames);

  @override
  OgcCollection200Response outputFormats(List<String>? outputFormats) =>
      call(outputFormats: outputFormats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCollection200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCollection200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcCollection200Response call({
    Object? id = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? itemType = const $CopyWithPlaceholder(),
    Object? crs = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? extent = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? dataQueries = const $CopyWithPlaceholder(),
    Object? parameterNames = const $CopyWithPlaceholder(),
    Object? outputFormats = const $CopyWithPlaceholder(),
  }) {
    return OgcCollection200Response(
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
          : itemType as OgcCollection200ResponseItemTypeEnum,
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
          : extent as OgcCoverageCollectionExtent?,
      links: links == const $CopyWithPlaceholder() || links == null
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as List<OgcLink>,
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
    );
  }
}

extension $OgcCollection200ResponseCopyWith on OgcCollection200Response {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcCollection200Response.copyWith(...)` or `instanceOfOgcCollection200Response.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcCollection200ResponseCWProxy get copyWith =>
      _$OgcCollection200ResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcCollection200Response _$OgcCollection200ResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate(
  'OgcCollection200Response',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['id', 'itemType', 'links']);
    final val = OgcCollection200Response(
      id: $checkedConvert('id', (v) => v as String),
      title: $checkedConvert('title', (v) => v as String?),
      description: $checkedConvert('description', (v) => v as String?),
      itemType: $checkedConvert(
        'itemType',
        (v) => $enumDecode(_$OgcCollection200ResponseItemTypeEnumEnumMap, v),
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
            : OgcCoverageCollectionExtent.fromJson(v as Map<String, dynamic>),
      ),
      links: $checkedConvert(
        'links',
        (v) => (v as List<dynamic>)
            .map((e) => OgcLink.fromJson(e as Map<String, dynamic>))
            .toList(),
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
    );
    return val;
  },
  fieldKeyMap: const {
    'dataQueries': 'data_queries',
    'parameterNames': 'parameter_names',
    'outputFormats': 'output_formats',
  },
);

Map<String, dynamic> _$OgcCollection200ResponseToJson(
  OgcCollection200Response instance,
) => <String, dynamic>{
  'id': instance.id,
  'title': ?instance.title,
  'description': ?instance.description,
  'itemType': _$OgcCollection200ResponseItemTypeEnumEnumMap[instance.itemType]!,
  'crs': ?instance.crs,
  'attribution': ?instance.attribution,
  'extent': ?instance.extent?.toJson(),
  'links': instance.links.map((e) => e.toJson()).toList(),
  'data_queries': ?instance.dataQueries?.toJson(),
  'parameter_names': ?instance.parameterNames,
  'output_formats': ?instance.outputFormats,
};

const _$OgcCollection200ResponseItemTypeEnumEnumMap = {
  OgcCollection200ResponseItemTypeEnum.coverage: 'coverage',
};
