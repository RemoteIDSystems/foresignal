// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'product_legend.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProductLegendCWProxy {
  ProductLegend summary(String? summary);

  ProductLegend url(String? url);

  ProductLegend pngUrl(String? pngUrl);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductLegend(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductLegend(...).copyWith(id: 12, name: "My name")
  /// ```
  ProductLegend call({String? summary, String? url, String? pngUrl});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProductLegend.copyWith(...)` or call `instanceOfProductLegend.copyWith.fieldName(value)` for a single field.
class _$ProductLegendCWProxyImpl implements _$ProductLegendCWProxy {
  const _$ProductLegendCWProxyImpl(this._value);

  final ProductLegend _value;

  @override
  ProductLegend summary(String? summary) => call(summary: summary);

  @override
  ProductLegend url(String? url) => call(url: url);

  @override
  ProductLegend pngUrl(String? pngUrl) => call(pngUrl: pngUrl);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductLegend(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductLegend(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProductLegend call({
    Object? summary = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? pngUrl = const $CopyWithPlaceholder(),
  }) {
    return ProductLegend(
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as String?,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      pngUrl: pngUrl == const $CopyWithPlaceholder()
          ? _value.pngUrl
          // ignore: cast_nullable_to_non_nullable
          : pngUrl as String?,
    );
  }
}

extension $ProductLegendCopyWith on ProductLegend {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProductLegend.copyWith(...)` or `instanceOfProductLegend.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProductLegendCWProxy get copyWith => _$ProductLegendCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProductLegend _$ProductLegendFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ProductLegend', json, ($checkedConvert) {
      final val = ProductLegend(
        summary: $checkedConvert('summary', (v) => v as String?),
        url: $checkedConvert('url', (v) => v as String?),
        pngUrl: $checkedConvert('pngUrl', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ProductLegendToJson(ProductLegend instance) =>
    <String, dynamic>{
      'summary': ?instance.summary,
      'url': ?instance.url,
      'pngUrl': ?instance.pngUrl,
    };
