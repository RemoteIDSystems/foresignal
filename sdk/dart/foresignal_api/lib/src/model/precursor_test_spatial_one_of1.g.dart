// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_test_spatial_one_of1.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorTestSpatialOneOf1CWProxy {
  PrecursorTestSpatialOneOf1 sameRegion(
    PrecursorTestSpatialOneOf1SameRegionEnum sameRegion,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestSpatialOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestSpatialOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorTestSpatialOneOf1 call({
    PrecursorTestSpatialOneOf1SameRegionEnum sameRegion,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorTestSpatialOneOf1.copyWith(...)` or call `instanceOfPrecursorTestSpatialOneOf1.copyWith.fieldName(value)` for a single field.
class _$PrecursorTestSpatialOneOf1CWProxyImpl
    implements _$PrecursorTestSpatialOneOf1CWProxy {
  const _$PrecursorTestSpatialOneOf1CWProxyImpl(this._value);

  final PrecursorTestSpatialOneOf1 _value;

  @override
  PrecursorTestSpatialOneOf1 sameRegion(
    PrecursorTestSpatialOneOf1SameRegionEnum sameRegion,
  ) => call(sameRegion: sameRegion);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestSpatialOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestSpatialOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorTestSpatialOneOf1 call({
    Object? sameRegion = const $CopyWithPlaceholder(),
  }) {
    return PrecursorTestSpatialOneOf1(
      sameRegion:
          sameRegion == const $CopyWithPlaceholder() || sameRegion == null
          ? _value.sameRegion
          // ignore: cast_nullable_to_non_nullable
          : sameRegion as PrecursorTestSpatialOneOf1SameRegionEnum,
    );
  }
}

extension $PrecursorTestSpatialOneOf1CopyWith on PrecursorTestSpatialOneOf1 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorTestSpatialOneOf1.copyWith(...)` or `instanceOfPrecursorTestSpatialOneOf1.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorTestSpatialOneOf1CWProxy get copyWith =>
      _$PrecursorTestSpatialOneOf1CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorTestSpatialOneOf1 _$PrecursorTestSpatialOneOf1FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorTestSpatialOneOf1', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['sameRegion']);
  final val = PrecursorTestSpatialOneOf1(
    sameRegion: $checkedConvert(
      'sameRegion',
      (v) => $enumDecode(_$PrecursorTestSpatialOneOf1SameRegionEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$PrecursorTestSpatialOneOf1ToJson(
  PrecursorTestSpatialOneOf1 instance,
) => <String, dynamic>{
  'sameRegion':
      _$PrecursorTestSpatialOneOf1SameRegionEnumEnumMap[instance.sameRegion]!,
};

const _$PrecursorTestSpatialOneOf1SameRegionEnumEnumMap = {
  PrecursorTestSpatialOneOf1SameRegionEnum.country: 'country',
  PrecursorTestSpatialOneOf1SameRegionEnum.admin1: 'admin1',
};
