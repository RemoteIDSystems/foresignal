// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'taxii_collections.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TaxiiCollectionsCWProxy {
  TaxiiCollections collections(List<TaxiiCollection>? collections);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TaxiiCollections(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TaxiiCollections(...).copyWith(id: 12, name: "My name")
  /// ```
  TaxiiCollections call({List<TaxiiCollection>? collections});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTaxiiCollections.copyWith(...)` or call `instanceOfTaxiiCollections.copyWith.fieldName(value)` for a single field.
class _$TaxiiCollectionsCWProxyImpl implements _$TaxiiCollectionsCWProxy {
  const _$TaxiiCollectionsCWProxyImpl(this._value);

  final TaxiiCollections _value;

  @override
  TaxiiCollections collections(List<TaxiiCollection>? collections) =>
      call(collections: collections);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TaxiiCollections(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TaxiiCollections(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TaxiiCollections call({Object? collections = const $CopyWithPlaceholder()}) {
    return TaxiiCollections(
      collections: collections == const $CopyWithPlaceholder()
          ? _value.collections
          // ignore: cast_nullable_to_non_nullable
          : collections as List<TaxiiCollection>?,
    );
  }
}

extension $TaxiiCollectionsCopyWith on TaxiiCollections {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTaxiiCollections.copyWith(...)` or `instanceOfTaxiiCollections.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TaxiiCollectionsCWProxy get copyWith => _$TaxiiCollectionsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TaxiiCollections _$TaxiiCollectionsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TaxiiCollections', json, ($checkedConvert) {
      final val = TaxiiCollections(
        collections: $checkedConvert(
          'collections',
          (v) => (v as List<dynamic>?)
              ?.map((e) => TaxiiCollection.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TaxiiCollectionsToJson(TaxiiCollections instance) =>
    <String, dynamic>{
      'collections': ?instance.collections?.map((e) => e.toJson()).toList(),
    };
