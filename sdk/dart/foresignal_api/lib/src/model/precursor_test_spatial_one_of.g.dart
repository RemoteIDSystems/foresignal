// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_test_spatial_one_of.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorTestSpatialOneOfCWProxy {
  PrecursorTestSpatialOneOf withinKm(num withinKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestSpatialOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestSpatialOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorTestSpatialOneOf call({num withinKm});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorTestSpatialOneOf.copyWith(...)` or call `instanceOfPrecursorTestSpatialOneOf.copyWith.fieldName(value)` for a single field.
class _$PrecursorTestSpatialOneOfCWProxyImpl
    implements _$PrecursorTestSpatialOneOfCWProxy {
  const _$PrecursorTestSpatialOneOfCWProxyImpl(this._value);

  final PrecursorTestSpatialOneOf _value;

  @override
  PrecursorTestSpatialOneOf withinKm(num withinKm) => call(withinKm: withinKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestSpatialOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestSpatialOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorTestSpatialOneOf call({
    Object? withinKm = const $CopyWithPlaceholder(),
  }) {
    return PrecursorTestSpatialOneOf(
      withinKm: withinKm == const $CopyWithPlaceholder() || withinKm == null
          ? _value.withinKm
          // ignore: cast_nullable_to_non_nullable
          : withinKm as num,
    );
  }
}

extension $PrecursorTestSpatialOneOfCopyWith on PrecursorTestSpatialOneOf {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorTestSpatialOneOf.copyWith(...)` or `instanceOfPrecursorTestSpatialOneOf.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorTestSpatialOneOfCWProxy get copyWith =>
      _$PrecursorTestSpatialOneOfCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorTestSpatialOneOf _$PrecursorTestSpatialOneOfFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorTestSpatialOneOf', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['withinKm']);
  final val = PrecursorTestSpatialOneOf(
    withinKm: $checkedConvert('withinKm', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$PrecursorTestSpatialOneOfToJson(
  PrecursorTestSpatialOneOf instance,
) => <String, dynamic>{'withinKm': instance.withinKm};
