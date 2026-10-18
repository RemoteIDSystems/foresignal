// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_feature_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpFeatureItemCWProxy {
  EdpFeatureItem stream(String stream);

  EdpFeatureItem type(EdpFeatureItemTypeEnum type);

  EdpFeatureItem id(String id);

  EdpFeatureItem observedAt(DateTime? observedAt);

  EdpFeatureItem properties(Map<String, Object>? properties);

  EdpFeatureItem geometry(EdpGeometry? geometry);

  EdpFeatureItem eventTime(DateTime? eventTime);

  EdpFeatureItem validUntil(DateTime? validUntil);

  EdpFeatureItem origin(EdpOrigin? origin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpFeatureItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpFeatureItem(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpFeatureItem call({
    String stream,
    EdpFeatureItemTypeEnum type,
    String id,
    DateTime? observedAt,
    Map<String, Object>? properties,
    EdpGeometry? geometry,
    DateTime? eventTime,
    DateTime? validUntil,
    EdpOrigin? origin,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpFeatureItem.copyWith(...)` or call `instanceOfEdpFeatureItem.copyWith.fieldName(value)` for a single field.
class _$EdpFeatureItemCWProxyImpl implements _$EdpFeatureItemCWProxy {
  const _$EdpFeatureItemCWProxyImpl(this._value);

  final EdpFeatureItem _value;

  @override
  EdpFeatureItem stream(String stream) => call(stream: stream);

  @override
  EdpFeatureItem type(EdpFeatureItemTypeEnum type) => call(type: type);

  @override
  EdpFeatureItem id(String id) => call(id: id);

  @override
  EdpFeatureItem observedAt(DateTime? observedAt) =>
      call(observedAt: observedAt);

  @override
  EdpFeatureItem properties(Map<String, Object>? properties) =>
      call(properties: properties);

  @override
  EdpFeatureItem geometry(EdpGeometry? geometry) => call(geometry: geometry);

  @override
  EdpFeatureItem eventTime(DateTime? eventTime) => call(eventTime: eventTime);

  @override
  EdpFeatureItem validUntil(DateTime? validUntil) =>
      call(validUntil: validUntil);

  @override
  EdpFeatureItem origin(EdpOrigin? origin) => call(origin: origin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpFeatureItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpFeatureItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpFeatureItem call({
    Object? stream = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? properties = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? eventTime = const $CopyWithPlaceholder(),
    Object? validUntil = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
  }) {
    return EdpFeatureItem(
      stream: stream == const $CopyWithPlaceholder() || stream == null
          ? _value.stream
          // ignore: cast_nullable_to_non_nullable
          : stream as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as EdpFeatureItemTypeEnum,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      observedAt: observedAt == const $CopyWithPlaceholder()
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime?,
      properties: properties == const $CopyWithPlaceholder()
          ? _value.properties
          // ignore: cast_nullable_to_non_nullable
          : properties as Map<String, Object>?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as EdpGeometry?,
      eventTime: eventTime == const $CopyWithPlaceholder()
          ? _value.eventTime
          // ignore: cast_nullable_to_non_nullable
          : eventTime as DateTime?,
      validUntil: validUntil == const $CopyWithPlaceholder()
          ? _value.validUntil
          // ignore: cast_nullable_to_non_nullable
          : validUntil as DateTime?,
      origin: origin == const $CopyWithPlaceholder()
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as EdpOrigin?,
    );
  }
}

extension $EdpFeatureItemCopyWith on EdpFeatureItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpFeatureItem.copyWith(...)` or `instanceOfEdpFeatureItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpFeatureItemCWProxy get copyWith => _$EdpFeatureItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpFeatureItem _$EdpFeatureItemFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EdpFeatureItem', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['stream', 'type', 'id']);
  final val = EdpFeatureItem(
    stream: $checkedConvert('stream', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$EdpFeatureItemTypeEnumEnumMap, v),
    ),
    id: $checkedConvert('id', (v) => v as String),
    observedAt: $checkedConvert(
      'observedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    properties: $checkedConvert(
      'properties',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    geometry: $checkedConvert(
      'geometry',
      (v) => v == null ? null : EdpGeometry.fromJson(v as Map<String, dynamic>),
    ),
    eventTime: $checkedConvert(
      'eventTime',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    validUntil: $checkedConvert(
      'validUntil',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    origin: $checkedConvert(
      'origin',
      (v) => v == null ? null : EdpOrigin.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$EdpFeatureItemToJson(EdpFeatureItem instance) =>
    <String, dynamic>{
      'stream': instance.stream,
      'type': _$EdpFeatureItemTypeEnumEnumMap[instance.type]!,
      'id': instance.id,
      'observedAt': ?instance.observedAt?.toIso8601String(),
      'properties': ?instance.properties,
      'geometry': ?instance.geometry?.toJson(),
      'eventTime': ?instance.eventTime?.toIso8601String(),
      'validUntil': ?instance.validUntil?.toIso8601String(),
      'origin': ?instance.origin?.toJson(),
    };

const _$EdpFeatureItemTypeEnumEnumMap = {
  EdpFeatureItemTypeEnum.feature: 'feature',
};
