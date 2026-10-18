// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'near.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NearCWProxy {
  Near placeIds(List<String>? placeIds);

  Near placeId(String? placeId);

  Near radiusKm(num radiusKm);

  Near point(List<num> point);

  Near anchor(NearOneOf2Anchor anchor);

  Near maxAnchorAge(String? maxAnchorAge);

  Near minMoveMeters(num? minMoveMeters);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Near(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Near(...).copyWith(id: 12, name: "My name")
  /// ```
  Near call({
    List<String>? placeIds,
    String? placeId,
    num radiusKm,
    List<num> point,
    NearOneOf2Anchor anchor,
    String? maxAnchorAge,
    num? minMoveMeters,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNear.copyWith(...)` or call `instanceOfNear.copyWith.fieldName(value)` for a single field.
class _$NearCWProxyImpl implements _$NearCWProxy {
  const _$NearCWProxyImpl(this._value);

  final Near _value;

  @override
  Near placeIds(List<String>? placeIds) => call(placeIds: placeIds);

  @override
  Near placeId(String? placeId) => call(placeId: placeId);

  @override
  Near radiusKm(num radiusKm) => call(radiusKm: radiusKm);

  @override
  Near point(List<num> point) => call(point: point);

  @override
  Near anchor(NearOneOf2Anchor anchor) => call(anchor: anchor);

  @override
  Near maxAnchorAge(String? maxAnchorAge) => call(maxAnchorAge: maxAnchorAge);

  @override
  Near minMoveMeters(num? minMoveMeters) => call(minMoveMeters: minMoveMeters);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Near(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Near(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Near call({
    Object? placeIds = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
    Object? radiusKm = const $CopyWithPlaceholder(),
    Object? point = const $CopyWithPlaceholder(),
    Object? anchor = const $CopyWithPlaceholder(),
    Object? maxAnchorAge = const $CopyWithPlaceholder(),
    Object? minMoveMeters = const $CopyWithPlaceholder(),
  }) {
    return Near(
      placeIds: placeIds == const $CopyWithPlaceholder()
          ? _value.placeIds
          // ignore: cast_nullable_to_non_nullable
          : placeIds as List<String>?,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      radiusKm: radiusKm == const $CopyWithPlaceholder() || radiusKm == null
          ? _value.radiusKm
          // ignore: cast_nullable_to_non_nullable
          : radiusKm as num,
      point: point == const $CopyWithPlaceholder() || point == null
          ? _value.point
          // ignore: cast_nullable_to_non_nullable
          : point as List<num>,
      anchor: anchor == const $CopyWithPlaceholder() || anchor == null
          ? _value.anchor
          // ignore: cast_nullable_to_non_nullable
          : anchor as NearOneOf2Anchor,
      maxAnchorAge: maxAnchorAge == const $CopyWithPlaceholder()
          ? _value.maxAnchorAge
          // ignore: cast_nullable_to_non_nullable
          : maxAnchorAge as String?,
      minMoveMeters: minMoveMeters == const $CopyWithPlaceholder()
          ? _value.minMoveMeters
          // ignore: cast_nullable_to_non_nullable
          : minMoveMeters as num?,
    );
  }
}

extension $NearCopyWith on Near {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNear.copyWith(...)` or `instanceOfNear.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NearCWProxy get copyWith => _$NearCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Near _$NearFromJson(Map<String, dynamic> json) => $checkedCreate('Near', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['radiusKm', 'point', 'anchor']);
  final val = Near(
    placeIds: $checkedConvert(
      'placeIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    placeId: $checkedConvert('placeId', (v) => v as String?),
    radiusKm: $checkedConvert('radiusKm', (v) => v as num),
    point: $checkedConvert(
      'point',
      (v) => (v as List<dynamic>).map((e) => e as num).toList(),
    ),
    anchor: $checkedConvert(
      'anchor',
      (v) => NearOneOf2Anchor.fromJson(v as Map<String, dynamic>),
    ),
    maxAnchorAge: $checkedConvert('maxAnchorAge', (v) => v as String?),
    minMoveMeters: $checkedConvert('minMoveMeters', (v) => v as num? ?? 200),
  );
  return val;
});

Map<String, dynamic> _$NearToJson(Near instance) => <String, dynamic>{
  'placeIds': ?instance.placeIds,
  'placeId': ?instance.placeId,
  'radiusKm': instance.radiusKm,
  'point': instance.point,
  'anchor': instance.anchor.toJson(),
  'maxAnchorAge': ?instance.maxAnchorAge,
  'minMoveMeters': ?instance.minMoveMeters,
};
