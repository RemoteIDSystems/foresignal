// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'collection_set_update.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CollectionSetUpdateCWProxy {
  CollectionSetUpdate sources(List<CollectionSource> sources);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollectionSetUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollectionSetUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  CollectionSetUpdate call({List<CollectionSource> sources});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCollectionSetUpdate.copyWith(...)` or call `instanceOfCollectionSetUpdate.copyWith.fieldName(value)` for a single field.
class _$CollectionSetUpdateCWProxyImpl implements _$CollectionSetUpdateCWProxy {
  const _$CollectionSetUpdateCWProxyImpl(this._value);

  final CollectionSetUpdate _value;

  @override
  CollectionSetUpdate sources(List<CollectionSource> sources) =>
      call(sources: sources);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollectionSetUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollectionSetUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CollectionSetUpdate call({Object? sources = const $CopyWithPlaceholder()}) {
    return CollectionSetUpdate(
      sources: sources == const $CopyWithPlaceholder() || sources == null
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<CollectionSource>,
    );
  }
}

extension $CollectionSetUpdateCopyWith on CollectionSetUpdate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCollectionSetUpdate.copyWith(...)` or `instanceOfCollectionSetUpdate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CollectionSetUpdateCWProxy get copyWith =>
      _$CollectionSetUpdateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CollectionSetUpdate _$CollectionSetUpdateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CollectionSetUpdate', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['sources']);
      final val = CollectionSetUpdate(
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>)
              .map((e) => CollectionSource.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CollectionSetUpdateToJson(
  CollectionSetUpdate instance,
) => <String, dynamic>{
  'sources': instance.sources.map((e) => e.toJson()).toList(),
};
