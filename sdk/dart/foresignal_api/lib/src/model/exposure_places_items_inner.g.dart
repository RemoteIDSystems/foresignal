// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exposure_places_items_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExposurePlacesItemsInnerCWProxy {
  ExposurePlacesItemsInner placeId(String placeId);

  ExposurePlacesItemsInner name(String name);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposurePlacesItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposurePlacesItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ExposurePlacesItemsInner call({String placeId, String name});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExposurePlacesItemsInner.copyWith(...)` or call `instanceOfExposurePlacesItemsInner.copyWith.fieldName(value)` for a single field.
class _$ExposurePlacesItemsInnerCWProxyImpl
    implements _$ExposurePlacesItemsInnerCWProxy {
  const _$ExposurePlacesItemsInnerCWProxyImpl(this._value);

  final ExposurePlacesItemsInner _value;

  @override
  ExposurePlacesItemsInner placeId(String placeId) => call(placeId: placeId);

  @override
  ExposurePlacesItemsInner name(String name) => call(name: name);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposurePlacesItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposurePlacesItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExposurePlacesItemsInner call({
    Object? placeId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
  }) {
    return ExposurePlacesItemsInner(
      placeId: placeId == const $CopyWithPlaceholder() || placeId == null
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
    );
  }
}

extension $ExposurePlacesItemsInnerCopyWith on ExposurePlacesItemsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExposurePlacesItemsInner.copyWith(...)` or `instanceOfExposurePlacesItemsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExposurePlacesItemsInnerCWProxy get copyWith =>
      _$ExposurePlacesItemsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExposurePlacesItemsInner _$ExposurePlacesItemsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExposurePlacesItemsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['placeId', 'name']);
  final val = ExposurePlacesItemsInner(
    placeId: $checkedConvert('placeId', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$ExposurePlacesItemsInnerToJson(
  ExposurePlacesItemsInner instance,
) => <String, dynamic>{'placeId': instance.placeId, 'name': instance.name};
