// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'resolver_area.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ResolverAreaCWProxy {
  ResolverArea bbox(List<num>? bbox);

  ResolverArea polygon(Geometry? polygon);

  ResolverArea placeId(String? placeId);

  ResolverArea regionCodes(List<String>? regionCodes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResolverArea(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResolverArea(...).copyWith(id: 12, name: "My name")
  /// ```
  ResolverArea call({
    List<num>? bbox,
    Geometry? polygon,
    String? placeId,
    List<String>? regionCodes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfResolverArea.copyWith(...)` or call `instanceOfResolverArea.copyWith.fieldName(value)` for a single field.
class _$ResolverAreaCWProxyImpl implements _$ResolverAreaCWProxy {
  const _$ResolverAreaCWProxyImpl(this._value);

  final ResolverArea _value;

  @override
  ResolverArea bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  ResolverArea polygon(Geometry? polygon) => call(polygon: polygon);

  @override
  ResolverArea placeId(String? placeId) => call(placeId: placeId);

  @override
  ResolverArea regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResolverArea(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResolverArea(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ResolverArea call({
    Object? bbox = const $CopyWithPlaceholder(),
    Object? polygon = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
  }) {
    return ResolverArea(
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      polygon: polygon == const $CopyWithPlaceholder()
          ? _value.polygon
          // ignore: cast_nullable_to_non_nullable
          : polygon as Geometry?,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
    );
  }
}

extension $ResolverAreaCopyWith on ResolverArea {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfResolverArea.copyWith(...)` or `instanceOfResolverArea.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ResolverAreaCWProxy get copyWith => _$ResolverAreaCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ResolverArea _$ResolverAreaFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ResolverArea', json, ($checkedConvert) {
      final val = ResolverArea(
        bbox: $checkedConvert(
          'bbox',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        polygon: $checkedConvert(
          'polygon',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        placeId: $checkedConvert('placeId', (v) => v as String?),
        regionCodes: $checkedConvert(
          'regionCodes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ResolverAreaToJson(ResolverArea instance) =>
    <String, dynamic>{
      'bbox': ?instance.bbox,
      'polygon': ?instance.polygon?.toJson(),
      'placeId': ?instance.placeId,
      'regionCodes': ?instance.regionCodes,
    };
