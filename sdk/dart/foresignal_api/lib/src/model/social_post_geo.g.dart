// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_post_geo.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialPostGeoCWProxy {
  SocialPostGeo geometry(Geometry? geometry);

  SocialPostGeo placeName(String? placeName);

  SocialPostGeo placeId(String? placeId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPostGeo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPostGeo(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialPostGeo call({Geometry? geometry, String? placeName, String? placeId});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialPostGeo.copyWith(...)` or call `instanceOfSocialPostGeo.copyWith.fieldName(value)` for a single field.
class _$SocialPostGeoCWProxyImpl implements _$SocialPostGeoCWProxy {
  const _$SocialPostGeoCWProxyImpl(this._value);

  final SocialPostGeo _value;

  @override
  SocialPostGeo geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  SocialPostGeo placeName(String? placeName) => call(placeName: placeName);

  @override
  SocialPostGeo placeId(String? placeId) => call(placeId: placeId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPostGeo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPostGeo(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialPostGeo call({
    Object? geometry = const $CopyWithPlaceholder(),
    Object? placeName = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
  }) {
    return SocialPostGeo(
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      placeName: placeName == const $CopyWithPlaceholder()
          ? _value.placeName
          // ignore: cast_nullable_to_non_nullable
          : placeName as String?,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
    );
  }
}

extension $SocialPostGeoCopyWith on SocialPostGeo {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialPostGeo.copyWith(...)` or `instanceOfSocialPostGeo.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialPostGeoCWProxy get copyWith => _$SocialPostGeoCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialPostGeo _$SocialPostGeoFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialPostGeo', json, ($checkedConvert) {
      final val = SocialPostGeo(
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        placeName: $checkedConvert('placeName', (v) => v as String?),
        placeId: $checkedConvert('placeId', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SocialPostGeoToJson(SocialPostGeo instance) =>
    <String, dynamic>{
      'geometry': ?instance.geometry?.toJson(),
      'placeName': ?instance.placeName,
      'placeId': ?instance.placeId,
    };
