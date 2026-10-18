// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'transit_catalog_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TransitCatalogPageCWProxy {
  TransitCatalogPage items(List<TransitCatalogEntry> items);

  TransitCatalogPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TransitCatalogPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TransitCatalogPage(...).copyWith(id: 12, name: "My name")
  /// ```
  TransitCatalogPage call({
    List<TransitCatalogEntry> items,
    String? nextCursor,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTransitCatalogPage.copyWith(...)` or call `instanceOfTransitCatalogPage.copyWith.fieldName(value)` for a single field.
class _$TransitCatalogPageCWProxyImpl implements _$TransitCatalogPageCWProxy {
  const _$TransitCatalogPageCWProxyImpl(this._value);

  final TransitCatalogPage _value;

  @override
  TransitCatalogPage items(List<TransitCatalogEntry> items) =>
      call(items: items);

  @override
  TransitCatalogPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TransitCatalogPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TransitCatalogPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TransitCatalogPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return TransitCatalogPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<TransitCatalogEntry>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $TransitCatalogPageCopyWith on TransitCatalogPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTransitCatalogPage.copyWith(...)` or `instanceOfTransitCatalogPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TransitCatalogPageCWProxy get copyWith =>
      _$TransitCatalogPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TransitCatalogPage _$TransitCatalogPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TransitCatalogPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = TransitCatalogPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map(
                (e) => TransitCatalogEntry.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$TransitCatalogPageToJson(TransitCatalogPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
