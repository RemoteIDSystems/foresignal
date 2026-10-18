// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'frame_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FrameItemCWProxy {
  FrameItem id(String id);

  FrameItem kind(String kind);

  FrameItem title(String? title);

  FrameItem observedAt(DateTime observedAt);

  FrameItem version(int version);

  FrameItem lon(num? lon);

  FrameItem lat(num? lat);

  FrameItem geometry(Geometry? geometry);

  FrameItem props(Map<String, Object>? props);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FrameItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FrameItem(...).copyWith(id: 12, name: "My name")
  /// ```
  FrameItem call({
    String id,
    String kind,
    String? title,
    DateTime observedAt,
    int version,
    num? lon,
    num? lat,
    Geometry? geometry,
    Map<String, Object>? props,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFrameItem.copyWith(...)` or call `instanceOfFrameItem.copyWith.fieldName(value)` for a single field.
class _$FrameItemCWProxyImpl implements _$FrameItemCWProxy {
  const _$FrameItemCWProxyImpl(this._value);

  final FrameItem _value;

  @override
  FrameItem id(String id) => call(id: id);

  @override
  FrameItem kind(String kind) => call(kind: kind);

  @override
  FrameItem title(String? title) => call(title: title);

  @override
  FrameItem observedAt(DateTime observedAt) => call(observedAt: observedAt);

  @override
  FrameItem version(int version) => call(version: version);

  @override
  FrameItem lon(num? lon) => call(lon: lon);

  @override
  FrameItem lat(num? lat) => call(lat: lat);

  @override
  FrameItem geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  FrameItem props(Map<String, Object>? props) => call(props: props);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FrameItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FrameItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FrameItem call({
    Object? id = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? lon = const $CopyWithPlaceholder(),
    Object? lat = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? props = const $CopyWithPlaceholder(),
  }) {
    return FrameItem(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      lon: lon == const $CopyWithPlaceholder()
          ? _value.lon
          // ignore: cast_nullable_to_non_nullable
          : lon as num?,
      lat: lat == const $CopyWithPlaceholder()
          ? _value.lat
          // ignore: cast_nullable_to_non_nullable
          : lat as num?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      props: props == const $CopyWithPlaceholder()
          ? _value.props
          // ignore: cast_nullable_to_non_nullable
          : props as Map<String, Object>?,
    );
  }
}

extension $FrameItemCopyWith on FrameItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFrameItem.copyWith(...)` or `instanceOfFrameItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FrameItemCWProxy get copyWith => _$FrameItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FrameItem _$FrameItemFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('FrameItem', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'kind', 'observedAt', 'version']);
  final val = FrameItem(
    id: $checkedConvert('id', (v) => v as String),
    kind: $checkedConvert('kind', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String?),
    observedAt: $checkedConvert(
      'observedAt',
      (v) => DateTime.parse(v as String),
    ),
    version: $checkedConvert('version', (v) => (v as num).toInt()),
    lon: $checkedConvert('lon', (v) => v as num?),
    lat: $checkedConvert('lat', (v) => v as num?),
    geometry: $checkedConvert(
      'geometry',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    props: $checkedConvert(
      'props',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
  );
  return val;
});

Map<String, dynamic> _$FrameItemToJson(FrameItem instance) => <String, dynamic>{
  'id': instance.id,
  'kind': instance.kind,
  'title': ?instance.title,
  'observedAt': instance.observedAt.toIso8601String(),
  'version': instance.version,
  'lon': ?instance.lon,
  'lat': ?instance.lat,
  'geometry': ?instance.geometry?.toJson(),
  'props': ?instance.props,
};
