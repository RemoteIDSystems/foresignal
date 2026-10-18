// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'saved_search_query_area.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SavedSearchQueryAreaCWProxy {
  SavedSearchQueryArea bbox(String? bbox);

  SavedSearchQueryArea geometry(Geometry? geometry);

  SavedSearchQueryArea placeId(String? placeId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchQueryArea(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchQueryArea(...).copyWith(id: 12, name: "My name")
  /// ```
  SavedSearchQueryArea call({
    String? bbox,
    Geometry? geometry,
    String? placeId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSavedSearchQueryArea.copyWith(...)` or call `instanceOfSavedSearchQueryArea.copyWith.fieldName(value)` for a single field.
class _$SavedSearchQueryAreaCWProxyImpl
    implements _$SavedSearchQueryAreaCWProxy {
  const _$SavedSearchQueryAreaCWProxyImpl(this._value);

  final SavedSearchQueryArea _value;

  @override
  SavedSearchQueryArea bbox(String? bbox) => call(bbox: bbox);

  @override
  SavedSearchQueryArea geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  SavedSearchQueryArea placeId(String? placeId) => call(placeId: placeId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchQueryArea(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchQueryArea(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SavedSearchQueryArea call({
    Object? bbox = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
  }) {
    return SavedSearchQueryArea(
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as String?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
    );
  }
}

extension $SavedSearchQueryAreaCopyWith on SavedSearchQueryArea {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSavedSearchQueryArea.copyWith(...)` or `instanceOfSavedSearchQueryArea.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SavedSearchQueryAreaCWProxy get copyWith =>
      _$SavedSearchQueryAreaCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SavedSearchQueryArea _$SavedSearchQueryAreaFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SavedSearchQueryArea', json, ($checkedConvert) {
  final val = SavedSearchQueryArea(
    bbox: $checkedConvert('bbox', (v) => v as String?),
    geometry: $checkedConvert(
      'geometry',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    placeId: $checkedConvert('placeId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$SavedSearchQueryAreaToJson(
  SavedSearchQueryArea instance,
) => <String, dynamic>{
  'bbox': ?instance.bbox,
  'geometry': ?instance.geometry?.toJson(),
  'placeId': ?instance.placeId,
};
