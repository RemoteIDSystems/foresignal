// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_input_attributes.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceInputAttributesCWProxy {
  PlaceInputAttributes zip(String? zip);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceInputAttributes(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceInputAttributes(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceInputAttributes call({String? zip});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceInputAttributes.copyWith(...)` or call `instanceOfPlaceInputAttributes.copyWith.fieldName(value)` for a single field.
class _$PlaceInputAttributesCWProxyImpl
    implements _$PlaceInputAttributesCWProxy {
  const _$PlaceInputAttributesCWProxyImpl(this._value);

  final PlaceInputAttributes _value;

  @override
  PlaceInputAttributes zip(String? zip) => call(zip: zip);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceInputAttributes(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceInputAttributes(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceInputAttributes call({Object? zip = const $CopyWithPlaceholder()}) {
    return PlaceInputAttributes(
      zip: zip == const $CopyWithPlaceholder()
          ? _value.zip
          // ignore: cast_nullable_to_non_nullable
          : zip as String?,
    );
  }
}

extension $PlaceInputAttributesCopyWith on PlaceInputAttributes {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceInputAttributes.copyWith(...)` or `instanceOfPlaceInputAttributes.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceInputAttributesCWProxy get copyWith =>
      _$PlaceInputAttributesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceInputAttributes _$PlaceInputAttributesFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PlaceInputAttributes', json, ($checkedConvert) {
  final val = PlaceInputAttributes(
    zip: $checkedConvert('zip', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$PlaceInputAttributesToJson(
  PlaceInputAttributes instance,
) => <String, dynamic>{'zip': ?instance.zip};
