// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'product_render.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProductRenderCWProxy {
  ProductRender defaultStyle(String defaultStyle);

  ProductRender styles(List<String> styles);

  ProductRender defaultOpacity(num? defaultOpacity);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductRender(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductRender(...).copyWith(id: 12, name: "My name")
  /// ```
  ProductRender call({
    String defaultStyle,
    List<String> styles,
    num? defaultOpacity,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProductRender.copyWith(...)` or call `instanceOfProductRender.copyWith.fieldName(value)` for a single field.
class _$ProductRenderCWProxyImpl implements _$ProductRenderCWProxy {
  const _$ProductRenderCWProxyImpl(this._value);

  final ProductRender _value;

  @override
  ProductRender defaultStyle(String defaultStyle) =>
      call(defaultStyle: defaultStyle);

  @override
  ProductRender styles(List<String> styles) => call(styles: styles);

  @override
  ProductRender defaultOpacity(num? defaultOpacity) =>
      call(defaultOpacity: defaultOpacity);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductRender(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductRender(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProductRender call({
    Object? defaultStyle = const $CopyWithPlaceholder(),
    Object? styles = const $CopyWithPlaceholder(),
    Object? defaultOpacity = const $CopyWithPlaceholder(),
  }) {
    return ProductRender(
      defaultStyle:
          defaultStyle == const $CopyWithPlaceholder() || defaultStyle == null
          ? _value.defaultStyle
          // ignore: cast_nullable_to_non_nullable
          : defaultStyle as String,
      styles: styles == const $CopyWithPlaceholder() || styles == null
          ? _value.styles
          // ignore: cast_nullable_to_non_nullable
          : styles as List<String>,
      defaultOpacity: defaultOpacity == const $CopyWithPlaceholder()
          ? _value.defaultOpacity
          // ignore: cast_nullable_to_non_nullable
          : defaultOpacity as num?,
    );
  }
}

extension $ProductRenderCopyWith on ProductRender {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProductRender.copyWith(...)` or `instanceOfProductRender.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProductRenderCWProxy get copyWith => _$ProductRenderCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProductRender _$ProductRenderFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ProductRender', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['defaultStyle', 'styles']);
      final val = ProductRender(
        defaultStyle: $checkedConvert('defaultStyle', (v) => v as String),
        styles: $checkedConvert(
          'styles',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        defaultOpacity: $checkedConvert('defaultOpacity', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$ProductRenderToJson(ProductRender instance) =>
    <String, dynamic>{
      'defaultStyle': instance.defaultStyle,
      'styles': instance.styles,
      'defaultOpacity': ?instance.defaultOpacity,
    };
