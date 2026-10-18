// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'field_encodings_png_tiles.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FieldEncodingsPngTilesCWProxy {
  FieldEncodingsPngTiles template(String? template);

  FieldEncodingsPngTiles tms(FieldEncodingsPngTilesTmsEnum? tms);

  FieldEncodingsPngTiles uMin(num? uMin);

  FieldEncodingsPngTiles uMax(num? uMax);

  FieldEncodingsPngTiles vMin(num? vMin);

  FieldEncodingsPngTiles vMax(num? vMax);

  FieldEncodingsPngTiles minzoom(int? minzoom);

  FieldEncodingsPngTiles maxzoom(int? maxzoom);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldEncodingsPngTiles(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldEncodingsPngTiles(...).copyWith(id: 12, name: "My name")
  /// ```
  FieldEncodingsPngTiles call({
    String? template,
    FieldEncodingsPngTilesTmsEnum? tms,
    num? uMin,
    num? uMax,
    num? vMin,
    num? vMax,
    int? minzoom,
    int? maxzoom,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFieldEncodingsPngTiles.copyWith(...)` or call `instanceOfFieldEncodingsPngTiles.copyWith.fieldName(value)` for a single field.
class _$FieldEncodingsPngTilesCWProxyImpl
    implements _$FieldEncodingsPngTilesCWProxy {
  const _$FieldEncodingsPngTilesCWProxyImpl(this._value);

  final FieldEncodingsPngTiles _value;

  @override
  FieldEncodingsPngTiles template(String? template) => call(template: template);

  @override
  FieldEncodingsPngTiles tms(FieldEncodingsPngTilesTmsEnum? tms) =>
      call(tms: tms);

  @override
  FieldEncodingsPngTiles uMin(num? uMin) => call(uMin: uMin);

  @override
  FieldEncodingsPngTiles uMax(num? uMax) => call(uMax: uMax);

  @override
  FieldEncodingsPngTiles vMin(num? vMin) => call(vMin: vMin);

  @override
  FieldEncodingsPngTiles vMax(num? vMax) => call(vMax: vMax);

  @override
  FieldEncodingsPngTiles minzoom(int? minzoom) => call(minzoom: minzoom);

  @override
  FieldEncodingsPngTiles maxzoom(int? maxzoom) => call(maxzoom: maxzoom);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldEncodingsPngTiles(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldEncodingsPngTiles(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FieldEncodingsPngTiles call({
    Object? template = const $CopyWithPlaceholder(),
    Object? tms = const $CopyWithPlaceholder(),
    Object? uMin = const $CopyWithPlaceholder(),
    Object? uMax = const $CopyWithPlaceholder(),
    Object? vMin = const $CopyWithPlaceholder(),
    Object? vMax = const $CopyWithPlaceholder(),
    Object? minzoom = const $CopyWithPlaceholder(),
    Object? maxzoom = const $CopyWithPlaceholder(),
  }) {
    return FieldEncodingsPngTiles(
      template: template == const $CopyWithPlaceholder()
          ? _value.template
          // ignore: cast_nullable_to_non_nullable
          : template as String?,
      tms: tms == const $CopyWithPlaceholder()
          ? _value.tms
          // ignore: cast_nullable_to_non_nullable
          : tms as FieldEncodingsPngTilesTmsEnum?,
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
      minzoom: minzoom == const $CopyWithPlaceholder()
          ? _value.minzoom
          // ignore: cast_nullable_to_non_nullable
          : minzoom as int?,
      maxzoom: maxzoom == const $CopyWithPlaceholder()
          ? _value.maxzoom
          // ignore: cast_nullable_to_non_nullable
          : maxzoom as int?,
    );
  }
}

extension $FieldEncodingsPngTilesCopyWith on FieldEncodingsPngTiles {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFieldEncodingsPngTiles.copyWith(...)` or `instanceOfFieldEncodingsPngTiles.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FieldEncodingsPngTilesCWProxy get copyWith =>
      _$FieldEncodingsPngTilesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FieldEncodingsPngTiles _$FieldEncodingsPngTilesFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('FieldEncodingsPngTiles', json, ($checkedConvert) {
  final val = FieldEncodingsPngTiles(
    template: $checkedConvert('template', (v) => v as String?),
    tms: $checkedConvert(
      'tms',
      (v) => $enumDecodeNullable(_$FieldEncodingsPngTilesTmsEnumEnumMap, v),
    ),
    uMin: $checkedConvert('uMin', (v) => v as num?),
    uMax: $checkedConvert('uMax', (v) => v as num?),
    vMin: $checkedConvert('vMin', (v) => v as num?),
    vMax: $checkedConvert('vMax', (v) => v as num?),
    minzoom: $checkedConvert('minzoom', (v) => (v as num?)?.toInt()),
    maxzoom: $checkedConvert('maxzoom', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$FieldEncodingsPngTilesToJson(
  FieldEncodingsPngTiles instance,
) => <String, dynamic>{
  'template': ?instance.template,
  'tms': ?_$FieldEncodingsPngTilesTmsEnumEnumMap[instance.tms],
  'uMin': ?instance.uMin,
  'uMax': ?instance.uMax,
  'vMin': ?instance.vMin,
  'vMax': ?instance.vMax,
  'minzoom': ?instance.minzoom,
  'maxzoom': ?instance.maxzoom,
};

const _$FieldEncodingsPngTilesTmsEnumEnumMap = {
  FieldEncodingsPngTilesTmsEnum.webMercatorQuad: 'WebMercatorQuad',
  FieldEncodingsPngTilesTmsEnum.worldCRS84Quad: 'WorldCRS84Quad',
};
