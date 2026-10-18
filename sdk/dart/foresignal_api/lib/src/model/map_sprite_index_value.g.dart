// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'map_sprite_index_value.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MapSpriteIndexValueCWProxy {
  MapSpriteIndexValue x(int x);

  MapSpriteIndexValue y(int y);

  MapSpriteIndexValue width(int width);

  MapSpriteIndexValue height(int height);

  MapSpriteIndexValue pixelRatio(num pixelRatio);

  MapSpriteIndexValue sdf(bool? sdf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MapSpriteIndexValue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MapSpriteIndexValue(...).copyWith(id: 12, name: "My name")
  /// ```
  MapSpriteIndexValue call({
    int x,
    int y,
    int width,
    int height,
    num pixelRatio,
    bool? sdf,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMapSpriteIndexValue.copyWith(...)` or call `instanceOfMapSpriteIndexValue.copyWith.fieldName(value)` for a single field.
class _$MapSpriteIndexValueCWProxyImpl implements _$MapSpriteIndexValueCWProxy {
  const _$MapSpriteIndexValueCWProxyImpl(this._value);

  final MapSpriteIndexValue _value;

  @override
  MapSpriteIndexValue x(int x) => call(x: x);

  @override
  MapSpriteIndexValue y(int y) => call(y: y);

  @override
  MapSpriteIndexValue width(int width) => call(width: width);

  @override
  MapSpriteIndexValue height(int height) => call(height: height);

  @override
  MapSpriteIndexValue pixelRatio(num pixelRatio) =>
      call(pixelRatio: pixelRatio);

  @override
  MapSpriteIndexValue sdf(bool? sdf) => call(sdf: sdf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MapSpriteIndexValue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MapSpriteIndexValue(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MapSpriteIndexValue call({
    Object? x = const $CopyWithPlaceholder(),
    Object? y = const $CopyWithPlaceholder(),
    Object? width = const $CopyWithPlaceholder(),
    Object? height = const $CopyWithPlaceholder(),
    Object? pixelRatio = const $CopyWithPlaceholder(),
    Object? sdf = const $CopyWithPlaceholder(),
  }) {
    return MapSpriteIndexValue(
      x: x == const $CopyWithPlaceholder() || x == null
          ? _value.x
          // ignore: cast_nullable_to_non_nullable
          : x as int,
      y: y == const $CopyWithPlaceholder() || y == null
          ? _value.y
          // ignore: cast_nullable_to_non_nullable
          : y as int,
      width: width == const $CopyWithPlaceholder() || width == null
          ? _value.width
          // ignore: cast_nullable_to_non_nullable
          : width as int,
      height: height == const $CopyWithPlaceholder() || height == null
          ? _value.height
          // ignore: cast_nullable_to_non_nullable
          : height as int,
      pixelRatio:
          pixelRatio == const $CopyWithPlaceholder() || pixelRatio == null
          ? _value.pixelRatio
          // ignore: cast_nullable_to_non_nullable
          : pixelRatio as num,
      sdf: sdf == const $CopyWithPlaceholder()
          ? _value.sdf
          // ignore: cast_nullable_to_non_nullable
          : sdf as bool?,
    );
  }
}

extension $MapSpriteIndexValueCopyWith on MapSpriteIndexValue {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMapSpriteIndexValue.copyWith(...)` or `instanceOfMapSpriteIndexValue.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MapSpriteIndexValueCWProxy get copyWith =>
      _$MapSpriteIndexValueCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MapSpriteIndexValue _$MapSpriteIndexValueFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MapSpriteIndexValue', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['x', 'y', 'width', 'height', 'pixelRatio'],
      );
      final val = MapSpriteIndexValue(
        x: $checkedConvert('x', (v) => (v as num).toInt()),
        y: $checkedConvert('y', (v) => (v as num).toInt()),
        width: $checkedConvert('width', (v) => (v as num).toInt()),
        height: $checkedConvert('height', (v) => (v as num).toInt()),
        pixelRatio: $checkedConvert('pixelRatio', (v) => v as num),
        sdf: $checkedConvert('sdf', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$MapSpriteIndexValueToJson(
  MapSpriteIndexValue instance,
) => <String, dynamic>{
  'x': instance.x,
  'y': instance.y,
  'width': instance.width,
  'height': instance.height,
  'pixelRatio': instance.pixelRatio,
  'sdf': ?instance.sdf,
};
