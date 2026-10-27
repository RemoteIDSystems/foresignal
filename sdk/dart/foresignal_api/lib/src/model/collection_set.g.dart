// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'collection_set.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CollectionSetCWProxy {
  CollectionSet sources(List<CollectionSource> sources);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollectionSet(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollectionSet(...).copyWith(id: 12, name: "My name")
  /// ```
  CollectionSet call({List<CollectionSource> sources});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCollectionSet.copyWith(...)` or call `instanceOfCollectionSet.copyWith.fieldName(value)` for a single field.
class _$CollectionSetCWProxyImpl implements _$CollectionSetCWProxy {
  const _$CollectionSetCWProxyImpl(this._value);

  final CollectionSet _value;

  @override
  CollectionSet sources(List<CollectionSource> sources) =>
      call(sources: sources);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollectionSet(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollectionSet(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CollectionSet call({Object? sources = const $CopyWithPlaceholder()}) {
    return CollectionSet(
      sources: sources == const $CopyWithPlaceholder() || sources == null
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<CollectionSource>,
    );
  }
}

extension $CollectionSetCopyWith on CollectionSet {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCollectionSet.copyWith(...)` or `instanceOfCollectionSet.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CollectionSetCWProxy get copyWith => _$CollectionSetCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CollectionSet _$CollectionSetFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CollectionSet', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['sources']);
      final val = CollectionSet(
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>)
              .map((e) => CollectionSource.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CollectionSetToJson(CollectionSet instance) =>
    <String, dynamic>{
      'sources': instance.sources.map((e) => e.toJson()).toList(),
    };
