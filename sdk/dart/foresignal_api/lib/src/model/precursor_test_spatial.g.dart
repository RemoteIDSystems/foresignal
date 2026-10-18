// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_test_spatial.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorTestSpatialCWProxy {
  PrecursorTestSpatial withinKm(num withinKm);

  PrecursorTestSpatial sameRegion(
    PrecursorTestSpatialSameRegionEnum sameRegion,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestSpatial(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestSpatial(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorTestSpatial call({
    num withinKm,
    PrecursorTestSpatialSameRegionEnum sameRegion,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorTestSpatial.copyWith(...)` or call `instanceOfPrecursorTestSpatial.copyWith.fieldName(value)` for a single field.
class _$PrecursorTestSpatialCWProxyImpl
    implements _$PrecursorTestSpatialCWProxy {
  const _$PrecursorTestSpatialCWProxyImpl(this._value);

  final PrecursorTestSpatial _value;

  @override
  PrecursorTestSpatial withinKm(num withinKm) => call(withinKm: withinKm);

  @override
  PrecursorTestSpatial sameRegion(
    PrecursorTestSpatialSameRegionEnum sameRegion,
  ) => call(sameRegion: sameRegion);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestSpatial(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestSpatial(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorTestSpatial call({
    Object? withinKm = const $CopyWithPlaceholder(),
    Object? sameRegion = const $CopyWithPlaceholder(),
  }) {
    return PrecursorTestSpatial(
      withinKm: withinKm == const $CopyWithPlaceholder() || withinKm == null
          ? _value.withinKm
          // ignore: cast_nullable_to_non_nullable
          : withinKm as num,
      sameRegion:
          sameRegion == const $CopyWithPlaceholder() || sameRegion == null
          ? _value.sameRegion
          // ignore: cast_nullable_to_non_nullable
          : sameRegion as PrecursorTestSpatialSameRegionEnum,
    );
  }
}

extension $PrecursorTestSpatialCopyWith on PrecursorTestSpatial {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorTestSpatial.copyWith(...)` or `instanceOfPrecursorTestSpatial.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorTestSpatialCWProxy get copyWith =>
      _$PrecursorTestSpatialCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorTestSpatial _$PrecursorTestSpatialFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorTestSpatial', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['withinKm', 'sameRegion']);
  final val = PrecursorTestSpatial(
    withinKm: $checkedConvert('withinKm', (v) => v as num),
    sameRegion: $checkedConvert(
      'sameRegion',
      (v) => $enumDecode(_$PrecursorTestSpatialSameRegionEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$PrecursorTestSpatialToJson(
  PrecursorTestSpatial instance,
) => <String, dynamic>{
  'withinKm': instance.withinKm,
  'sameRegion':
      _$PrecursorTestSpatialSameRegionEnumEnumMap[instance.sameRegion]!,
};

const _$PrecursorTestSpatialSameRegionEnumEnumMap = {
  PrecursorTestSpatialSameRegionEnum.country: 'country',
  PrecursorTestSpatialSameRegionEnum.admin1: 'admin1',
};
