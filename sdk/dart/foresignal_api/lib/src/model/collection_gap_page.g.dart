// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'collection_gap_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CollectionGapPageCWProxy {
  CollectionGapPage items(List<CollectionGap> items);

  CollectionGapPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollectionGapPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollectionGapPage(...).copyWith(id: 12, name: "My name")
  /// ```
  CollectionGapPage call({List<CollectionGap> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCollectionGapPage.copyWith(...)` or call `instanceOfCollectionGapPage.copyWith.fieldName(value)` for a single field.
class _$CollectionGapPageCWProxyImpl implements _$CollectionGapPageCWProxy {
  const _$CollectionGapPageCWProxyImpl(this._value);

  final CollectionGapPage _value;

  @override
  CollectionGapPage items(List<CollectionGap> items) => call(items: items);

  @override
  CollectionGapPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollectionGapPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollectionGapPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CollectionGapPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return CollectionGapPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<CollectionGap>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $CollectionGapPageCopyWith on CollectionGapPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCollectionGapPage.copyWith(...)` or `instanceOfCollectionGapPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CollectionGapPageCWProxy get copyWith =>
      _$CollectionGapPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CollectionGapPage _$CollectionGapPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CollectionGapPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = CollectionGapPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => CollectionGap.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$CollectionGapPageToJson(CollectionGapPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
