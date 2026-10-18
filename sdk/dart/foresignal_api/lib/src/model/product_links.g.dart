// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'product_links.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProductLinksCWProxy {
  ProductLinks self(String? self);

  ProductLinks frames(String? frames);

  ProductLinks latest(String? latest);

  ProductLinks tilejson(String? tilejson);

  ProductLinks legend(String? legend);

  ProductLinks timeline(String? timeline);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  ProductLinks call({
    String? self,
    String? frames,
    String? latest,
    String? tilejson,
    String? legend,
    String? timeline,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProductLinks.copyWith(...)` or call `instanceOfProductLinks.copyWith.fieldName(value)` for a single field.
class _$ProductLinksCWProxyImpl implements _$ProductLinksCWProxy {
  const _$ProductLinksCWProxyImpl(this._value);

  final ProductLinks _value;

  @override
  ProductLinks self(String? self) => call(self: self);

  @override
  ProductLinks frames(String? frames) => call(frames: frames);

  @override
  ProductLinks latest(String? latest) => call(latest: latest);

  @override
  ProductLinks tilejson(String? tilejson) => call(tilejson: tilejson);

  @override
  ProductLinks legend(String? legend) => call(legend: legend);

  @override
  ProductLinks timeline(String? timeline) => call(timeline: timeline);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProductLinks call({
    Object? self = const $CopyWithPlaceholder(),
    Object? frames = const $CopyWithPlaceholder(),
    Object? latest = const $CopyWithPlaceholder(),
    Object? tilejson = const $CopyWithPlaceholder(),
    Object? legend = const $CopyWithPlaceholder(),
    Object? timeline = const $CopyWithPlaceholder(),
  }) {
    return ProductLinks(
      self: self == const $CopyWithPlaceholder()
          ? _value.self
          // ignore: cast_nullable_to_non_nullable
          : self as String?,
      frames: frames == const $CopyWithPlaceholder()
          ? _value.frames
          // ignore: cast_nullable_to_non_nullable
          : frames as String?,
      latest: latest == const $CopyWithPlaceholder()
          ? _value.latest
          // ignore: cast_nullable_to_non_nullable
          : latest as String?,
      tilejson: tilejson == const $CopyWithPlaceholder()
          ? _value.tilejson
          // ignore: cast_nullable_to_non_nullable
          : tilejson as String?,
      legend: legend == const $CopyWithPlaceholder()
          ? _value.legend
          // ignore: cast_nullable_to_non_nullable
          : legend as String?,
      timeline: timeline == const $CopyWithPlaceholder()
          ? _value.timeline
          // ignore: cast_nullable_to_non_nullable
          : timeline as String?,
    );
  }
}

extension $ProductLinksCopyWith on ProductLinks {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProductLinks.copyWith(...)` or `instanceOfProductLinks.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProductLinksCWProxy get copyWith => _$ProductLinksCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProductLinks _$ProductLinksFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ProductLinks', json, ($checkedConvert) {
      final val = ProductLinks(
        self: $checkedConvert('self', (v) => v as String?),
        frames: $checkedConvert('frames', (v) => v as String?),
        latest: $checkedConvert('latest', (v) => v as String?),
        tilejson: $checkedConvert('tilejson', (v) => v as String?),
        legend: $checkedConvert('legend', (v) => v as String?),
        timeline: $checkedConvert('timeline', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ProductLinksToJson(ProductLinks instance) =>
    <String, dynamic>{
      'self': ?instance.self,
      'frames': ?instance.frames,
      'latest': ?instance.latest,
      'tilejson': ?instance.tilejson,
      'legend': ?instance.legend,
      'timeline': ?instance.timeline,
    };
