// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'field_encodings_png_uv.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FieldEncodingsPngUvCWProxy {
  FieldEncodingsPngUv url(String? url);

  FieldEncodingsPngUv uMin(num? uMin);

  FieldEncodingsPngUv uMax(num? uMax);

  FieldEncodingsPngUv vMin(num? vMin);

  FieldEncodingsPngUv vMax(num? vMax);

  FieldEncodingsPngUv width(int? width);

  FieldEncodingsPngUv height(int? height);

  FieldEncodingsPngUv bounds(List<num>? bounds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldEncodingsPngUv(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldEncodingsPngUv(...).copyWith(id: 12, name: "My name")
  /// ```
  FieldEncodingsPngUv call({
    String? url,
    num? uMin,
    num? uMax,
    num? vMin,
    num? vMax,
    int? width,
    int? height,
    List<num>? bounds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFieldEncodingsPngUv.copyWith(...)` or call `instanceOfFieldEncodingsPngUv.copyWith.fieldName(value)` for a single field.
class _$FieldEncodingsPngUvCWProxyImpl implements _$FieldEncodingsPngUvCWProxy {
  const _$FieldEncodingsPngUvCWProxyImpl(this._value);

  final FieldEncodingsPngUv _value;

  @override
  FieldEncodingsPngUv url(String? url) => call(url: url);

  @override
  FieldEncodingsPngUv uMin(num? uMin) => call(uMin: uMin);

  @override
  FieldEncodingsPngUv uMax(num? uMax) => call(uMax: uMax);

  @override
  FieldEncodingsPngUv vMin(num? vMin) => call(vMin: vMin);

  @override
  FieldEncodingsPngUv vMax(num? vMax) => call(vMax: vMax);

  @override
  FieldEncodingsPngUv width(int? width) => call(width: width);

  @override
  FieldEncodingsPngUv height(int? height) => call(height: height);

  @override
  FieldEncodingsPngUv bounds(List<num>? bounds) => call(bounds: bounds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldEncodingsPngUv(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldEncodingsPngUv(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FieldEncodingsPngUv call({
    Object? url = const $CopyWithPlaceholder(),
    Object? uMin = const $CopyWithPlaceholder(),
    Object? uMax = const $CopyWithPlaceholder(),
    Object? vMin = const $CopyWithPlaceholder(),
    Object? vMax = const $CopyWithPlaceholder(),
    Object? width = const $CopyWithPlaceholder(),
    Object? height = const $CopyWithPlaceholder(),
    Object? bounds = const $CopyWithPlaceholder(),
  }) {
    return FieldEncodingsPngUv(
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      uMin: uMin == const $CopyWithPlaceholder()
          ? _value.uMin
          // ignore: cast_nullable_to_non_nullable
          : uMin as num?,
      uMax: uMax == const $CopyWithPlaceholder()
          ? _value.uMax
          // ignore: cast_nullable_to_non_nullable
          : uMax as num?,
      vMin: vMin == const $CopyWithPlaceholder()
          ? _value.vMin
          // ignore: cast_nullable_to_non_nullable
          : vMin as num?,
      vMax: vMax == const $CopyWithPlaceholder()
          ? _value.vMax
          // ignore: cast_nullable_to_non_nullable
          : vMax as num?,
      width: width == const $CopyWithPlaceholder()
          ? _value.width
          // ignore: cast_nullable_to_non_nullable
          : width as int?,
      height: height == const $CopyWithPlaceholder()
          ? _value.height
          // ignore: cast_nullable_to_non_nullable
          : height as int?,
      bounds: bounds == const $CopyWithPlaceholder()
          ? _value.bounds
          // ignore: cast_nullable_to_non_nullable
          : bounds as List<num>?,
    );
  }
}

extension $FieldEncodingsPngUvCopyWith on FieldEncodingsPngUv {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFieldEncodingsPngUv.copyWith(...)` or `instanceOfFieldEncodingsPngUv.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FieldEncodingsPngUvCWProxy get copyWith =>
      _$FieldEncodingsPngUvCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FieldEncodingsPngUv _$FieldEncodingsPngUvFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FieldEncodingsPngUv', json, ($checkedConvert) {
      final val = FieldEncodingsPngUv(
        url: $checkedConvert('url', (v) => v as String?),
        uMin: $checkedConvert('uMin', (v) => v as num?),
        uMax: $checkedConvert('uMax', (v) => v as num?),
        vMin: $checkedConvert('vMin', (v) => v as num?),
        vMax: $checkedConvert('vMax', (v) => v as num?),
        width: $checkedConvert('width', (v) => (v as num?)?.toInt()),
        height: $checkedConvert('height', (v) => (v as num?)?.toInt()),
        bounds: $checkedConvert(
          'bounds',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$FieldEncodingsPngUvToJson(
  FieldEncodingsPngUv instance,
) => <String, dynamic>{
  'url': ?instance.url,
  'uMin': ?instance.uMin,
  'uMax': ?instance.uMax,
  'vMin': ?instance.vMin,
  'vMax': ?instance.vMax,
  'width': ?instance.width,
  'height': ?instance.height,
  'bounds': ?instance.bounds,
};
