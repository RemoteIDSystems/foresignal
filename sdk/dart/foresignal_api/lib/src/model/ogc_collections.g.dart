// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_collections.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcCollectionsCWProxy {
  OgcCollections links(List<OgcLink> links);

  OgcCollections collections(List<OgcCollection200Response> collections);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCollections(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCollections(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcCollections call({
    List<OgcLink> links,
    List<OgcCollection200Response> collections,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcCollections.copyWith(...)` or call `instanceOfOgcCollections.copyWith.fieldName(value)` for a single field.
class _$OgcCollectionsCWProxyImpl implements _$OgcCollectionsCWProxy {
  const _$OgcCollectionsCWProxyImpl(this._value);

  final OgcCollections _value;

  @override
  OgcCollections links(List<OgcLink> links) => call(links: links);

  @override
  OgcCollections collections(List<OgcCollection200Response> collections) =>
      call(collections: collections);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCollections(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCollections(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcCollections call({
    Object? links = const $CopyWithPlaceholder(),
    Object? collections = const $CopyWithPlaceholder(),
  }) {
    return OgcCollections(
      links: links == const $CopyWithPlaceholder() || links == null
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as List<OgcLink>,
      collections:
          collections == const $CopyWithPlaceholder() || collections == null
          ? _value.collections
          // ignore: cast_nullable_to_non_nullable
          : collections as List<OgcCollection200Response>,
    );
  }
}

extension $OgcCollectionsCopyWith on OgcCollections {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcCollections.copyWith(...)` or `instanceOfOgcCollections.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcCollectionsCWProxy get copyWith => _$OgcCollectionsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcCollections _$OgcCollectionsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OgcCollections', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['links', 'collections']);
      final val = OgcCollections(
        links: $checkedConvert(
          'links',
          (v) => (v as List<dynamic>)
              .map((e) => OgcLink.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        collections: $checkedConvert(
          'collections',
          (v) => (v as List<dynamic>)
              .map(
                (e) => OgcCollection200Response.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OgcCollectionsToJson(OgcCollections instance) =>
    <String, dynamic>{
      'links': instance.links.map((e) => e.toJson()).toList(),
      'collections': instance.collections.map((e) => e.toJson()).toList(),
    };
