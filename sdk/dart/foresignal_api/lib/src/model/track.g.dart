// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'track.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TrackCWProxy {
  Track type(TrackTypeEnum type);

  Track geometry(Geometry? geometry);

  Track properties(TrackProperties properties);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Track(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Track(...).copyWith(id: 12, name: "My name")
  /// ```
  Track call({
    TrackTypeEnum type,
    Geometry? geometry,
    TrackProperties properties,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTrack.copyWith(...)` or call `instanceOfTrack.copyWith.fieldName(value)` for a single field.
class _$TrackCWProxyImpl implements _$TrackCWProxy {
  const _$TrackCWProxyImpl(this._value);

  final Track _value;

  @override
  Track type(TrackTypeEnum type) => call(type: type);

  @override
  Track geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  Track properties(TrackProperties properties) => call(properties: properties);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Track(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Track(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Track call({
    Object? type = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? properties = const $CopyWithPlaceholder(),
  }) {
    return Track(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as TrackTypeEnum,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      properties:
          properties == const $CopyWithPlaceholder() || properties == null
          ? _value.properties
          // ignore: cast_nullable_to_non_nullable
          : properties as TrackProperties,
    );
  }
}

extension $TrackCopyWith on Track {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTrack.copyWith(...)` or `instanceOfTrack.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TrackCWProxy get copyWith => _$TrackCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Track _$TrackFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Track', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'geometry', 'properties']);
      final val = Track(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$TrackTypeEnumEnumMap, v),
        ),
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        properties: $checkedConvert(
          'properties',
          (v) => TrackProperties.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TrackToJson(Track instance) => <String, dynamic>{
  'type': _$TrackTypeEnumEnumMap[instance.type]!,
  'geometry': instance.geometry?.toJson(),
  'properties': instance.properties.toJson(),
};

const _$TrackTypeEnumEnumMap = {TrackTypeEnum.feature: 'Feature'};
