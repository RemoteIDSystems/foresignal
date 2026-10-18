// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_tile_matrix_set_list.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcTileMatrixSetListCWProxy {
  OgcTileMatrixSetList tileMatrixSets(
    List<OgcTileMatrixSetListTileMatrixSetsInner> tileMatrixSets,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcTileMatrixSetList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcTileMatrixSetList(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcTileMatrixSetList call({
    List<OgcTileMatrixSetListTileMatrixSetsInner> tileMatrixSets,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcTileMatrixSetList.copyWith(...)` or call `instanceOfOgcTileMatrixSetList.copyWith.fieldName(value)` for a single field.
class _$OgcTileMatrixSetListCWProxyImpl
    implements _$OgcTileMatrixSetListCWProxy {
  const _$OgcTileMatrixSetListCWProxyImpl(this._value);

  final OgcTileMatrixSetList _value;

  @override
  OgcTileMatrixSetList tileMatrixSets(
    List<OgcTileMatrixSetListTileMatrixSetsInner> tileMatrixSets,
  ) => call(tileMatrixSets: tileMatrixSets);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcTileMatrixSetList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcTileMatrixSetList(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcTileMatrixSetList call({
    Object? tileMatrixSets = const $CopyWithPlaceholder(),
  }) {
    return OgcTileMatrixSetList(
      tileMatrixSets:
          tileMatrixSets == const $CopyWithPlaceholder() ||
              tileMatrixSets == null
          ? _value.tileMatrixSets
          // ignore: cast_nullable_to_non_nullable
          : tileMatrixSets as List<OgcTileMatrixSetListTileMatrixSetsInner>,
    );
  }
}

extension $OgcTileMatrixSetListCopyWith on OgcTileMatrixSetList {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcTileMatrixSetList.copyWith(...)` or `instanceOfOgcTileMatrixSetList.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcTileMatrixSetListCWProxy get copyWith =>
      _$OgcTileMatrixSetListCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcTileMatrixSetList _$OgcTileMatrixSetListFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OgcTileMatrixSetList', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['tileMatrixSets']);
  final val = OgcTileMatrixSetList(
    tileMatrixSets: $checkedConvert(
      'tileMatrixSets',
      (v) => (v as List<dynamic>)
          .map(
            (e) => OgcTileMatrixSetListTileMatrixSetsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$OgcTileMatrixSetListToJson(
  OgcTileMatrixSetList instance,
) => <String, dynamic>{
  'tileMatrixSets': instance.tileMatrixSets.map((e) => e.toJson()).toList(),
};
