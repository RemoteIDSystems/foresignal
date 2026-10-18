// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'product_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProductPageCWProxy {
  ProductPage items(List<Product> items);

  ProductPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductPage(...).copyWith(id: 12, name: "My name")
  /// ```
  ProductPage call({List<Product> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProductPage.copyWith(...)` or call `instanceOfProductPage.copyWith.fieldName(value)` for a single field.
class _$ProductPageCWProxyImpl implements _$ProductPageCWProxy {
  const _$ProductPageCWProxyImpl(this._value);

  final ProductPage _value;

  @override
  ProductPage items(List<Product> items) => call(items: items);

  @override
  ProductPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProductPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ProductPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Product>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ProductPageCopyWith on ProductPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProductPage.copyWith(...)` or `instanceOfProductPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProductPageCWProxy get copyWith => _$ProductPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProductPage _$ProductPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ProductPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = ProductPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Product.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ProductPageToJson(ProductPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
