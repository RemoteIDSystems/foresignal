// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_tile_set_tile_matrix_set_limits_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcTileSetTileMatrixSetLimitsInnerCWProxy {
  OgcTileSetTileMatrixSetLimitsInner tileMatrix(String? tileMatrix);

  OgcTileSetTileMatrixSetLimitsInner minTileRow(int? minTileRow);

  OgcTileSetTileMatrixSetLimitsInner maxTileRow(int? maxTileRow);

  OgcTileSetTileMatrixSetLimitsInner minTileCol(int? minTileCol);

  OgcTileSetTileMatrixSetLimitsInner maxTileCol(int? maxTileCol);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcTileSetTileMatrixSetLimitsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcTileSetTileMatrixSetLimitsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcTileSetTileMatrixSetLimitsInner call({
    String? tileMatrix,
    int? minTileRow,
    int? maxTileRow,
    int? minTileCol,
    int? maxTileCol,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcTileSetTileMatrixSetLimitsInner.copyWith(...)` or call `instanceOfOgcTileSetTileMatrixSetLimitsInner.copyWith.fieldName(value)` for a single field.
class _$OgcTileSetTileMatrixSetLimitsInnerCWProxyImpl
    implements _$OgcTileSetTileMatrixSetLimitsInnerCWProxy {
  const _$OgcTileSetTileMatrixSetLimitsInnerCWProxyImpl(this._value);

  final OgcTileSetTileMatrixSetLimitsInner _value;

  @override
  OgcTileSetTileMatrixSetLimitsInner tileMatrix(String? tileMatrix) =>
      call(tileMatrix: tileMatrix);

  @override
  OgcTileSetTileMatrixSetLimitsInner minTileRow(int? minTileRow) =>
      call(minTileRow: minTileRow);

  @override
  OgcTileSetTileMatrixSetLimitsInner maxTileRow(int? maxTileRow) =>
      call(maxTileRow: maxTileRow);

  @override
  OgcTileSetTileMatrixSetLimitsInner minTileCol(int? minTileCol) =>
      call(minTileCol: minTileCol);

  @override
  OgcTileSetTileMatrixSetLimitsInner maxTileCol(int? maxTileCol) =>
      call(maxTileCol: maxTileCol);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcTileSetTileMatrixSetLimitsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcTileSetTileMatrixSetLimitsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcTileSetTileMatrixSetLimitsInner call({
    Object? tileMatrix = const $CopyWithPlaceholder(),
    Object? minTileRow = const $CopyWithPlaceholder(),
    Object? maxTileRow = const $CopyWithPlaceholder(),
    Object? minTileCol = const $CopyWithPlaceholder(),
    Object? maxTileCol = const $CopyWithPlaceholder(),
  }) {
    return OgcTileSetTileMatrixSetLimitsInner(
      tileMatrix: tileMatrix == const $CopyWithPlaceholder()
          ? _value.tileMatrix
          // ignore: cast_nullable_to_non_nullable
          : tileMatrix as String?,
      minTileRow: minTileRow == const $CopyWithPlaceholder()
          ? _value.minTileRow
          // ignore: cast_nullable_to_non_nullable
          : minTileRow as int?,
      maxTileRow: maxTileRow == const $CopyWithPlaceholder()
          ? _value.maxTileRow
          // ignore: cast_nullable_to_non_nullable
          : maxTileRow as int?,
      minTileCol: minTileCol == const $CopyWithPlaceholder()
          ? _value.minTileCol
          // ignore: cast_nullable_to_non_nullable
          : minTileCol as int?,
      maxTileCol: maxTileCol == const $CopyWithPlaceholder()
          ? _value.maxTileCol
          // ignore: cast_nullable_to_non_nullable
          : maxTileCol as int?,
    );
  }
}

extension $OgcTileSetTileMatrixSetLimitsInnerCopyWith
    on OgcTileSetTileMatrixSetLimitsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcTileSetTileMatrixSetLimitsInner.copyWith(...)` or `instanceOfOgcTileSetTileMatrixSetLimitsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcTileSetTileMatrixSetLimitsInnerCWProxy get copyWith =>
      _$OgcTileSetTileMatrixSetLimitsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcTileSetTileMatrixSetLimitsInner _$OgcTileSetTileMatrixSetLimitsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OgcTileSetTileMatrixSetLimitsInner', json, (
  $checkedConvert,
) {
  final val = OgcTileSetTileMatrixSetLimitsInner(
    tileMatrix: $checkedConvert('tileMatrix', (v) => v as String?),
    minTileRow: $checkedConvert('minTileRow', (v) => (v as num?)?.toInt()),
    maxTileRow: $checkedConvert('maxTileRow', (v) => (v as num?)?.toInt()),
    minTileCol: $checkedConvert('minTileCol', (v) => (v as num?)?.toInt()),
    maxTileCol: $checkedConvert('maxTileCol', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$OgcTileSetTileMatrixSetLimitsInnerToJson(
  OgcTileSetTileMatrixSetLimitsInner instance,
) => <String, dynamic>{
  'tileMatrix': ?instance.tileMatrix,
  'minTileRow': ?instance.minTileRow,
  'maxTileRow': ?instance.maxTileRow,
  'minTileCol': ?instance.minTileCol,
  'maxTileCol': ?instance.maxTileCol,
};
