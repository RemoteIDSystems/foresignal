// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_event_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpEventItemCWProxy {
  EdpEventItem stream(String stream);

  EdpEventItem type(EdpEventItemTypeEnum type);

  EdpEventItem id(String id);

  EdpEventItem observedAt(DateTime? observedAt);

  EdpEventItem properties(Map<String, Object>? properties);

  EdpEventItem eventTime(DateTime eventTime);

  EdpEventItem geometry(EdpGeometry? geometry);

  EdpEventItem upstreamEventId(String? upstreamEventId);

  EdpEventItem origin(EdpOrigin? origin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpEventItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpEventItem(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpEventItem call({
    String stream,
    EdpEventItemTypeEnum type,
    String id,
    DateTime? observedAt,
    Map<String, Object>? properties,
    DateTime eventTime,
    EdpGeometry? geometry,
    String? upstreamEventId,
    EdpOrigin? origin,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpEventItem.copyWith(...)` or call `instanceOfEdpEventItem.copyWith.fieldName(value)` for a single field.
class _$EdpEventItemCWProxyImpl implements _$EdpEventItemCWProxy {
  const _$EdpEventItemCWProxyImpl(this._value);

  final EdpEventItem _value;

  @override
  EdpEventItem stream(String stream) => call(stream: stream);

  @override
  EdpEventItem type(EdpEventItemTypeEnum type) => call(type: type);

  @override
  EdpEventItem id(String id) => call(id: id);

  @override
  EdpEventItem observedAt(DateTime? observedAt) => call(observedAt: observedAt);

  @override
  EdpEventItem properties(Map<String, Object>? properties) =>
      call(properties: properties);

  @override
  EdpEventItem eventTime(DateTime eventTime) => call(eventTime: eventTime);

  @override
  EdpEventItem geometry(EdpGeometry? geometry) => call(geometry: geometry);

  @override
  EdpEventItem upstreamEventId(String? upstreamEventId) =>
      call(upstreamEventId: upstreamEventId);

  @override
  EdpEventItem origin(EdpOrigin? origin) => call(origin: origin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpEventItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpEventItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpEventItem call({
    Object? stream = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? properties = const $CopyWithPlaceholder(),
    Object? eventTime = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? upstreamEventId = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
  }) {
    return EdpEventItem(
      stream: stream == const $CopyWithPlaceholder() || stream == null
          ? _value.stream
          // ignore: cast_nullable_to_non_nullable
          : stream as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as EdpEventItemTypeEnum,
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
      eventTime: eventTime == const $CopyWithPlaceholder() || eventTime == null
          ? _value.eventTime
          // ignore: cast_nullable_to_non_nullable
          : eventTime as DateTime,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as EdpGeometry?,
      upstreamEventId: upstreamEventId == const $CopyWithPlaceholder()
          ? _value.upstreamEventId
          // ignore: cast_nullable_to_non_nullable
          : upstreamEventId as String?,
      origin: origin == const $CopyWithPlaceholder()
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as EdpOrigin?,
    );
  }
}

extension $EdpEventItemCopyWith on EdpEventItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpEventItem.copyWith(...)` or `instanceOfEdpEventItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpEventItemCWProxy get copyWith => _$EdpEventItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpEventItem _$EdpEventItemFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EdpEventItem', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['stream', 'type', 'id', 'eventTime']);
  final val = EdpEventItem(
    stream: $checkedConvert('stream', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$EdpEventItemTypeEnumEnumMap, v),
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
    eventTime: $checkedConvert('eventTime', (v) => DateTime.parse(v as String)),
    geometry: $checkedConvert(
      'geometry',
      (v) => v == null ? null : EdpGeometry.fromJson(v as Map<String, dynamic>),
    ),
    upstreamEventId: $checkedConvert('upstreamEventId', (v) => v as String?),
    origin: $checkedConvert(
      'origin',
      (v) => v == null ? null : EdpOrigin.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$EdpEventItemToJson(EdpEventItem instance) =>
    <String, dynamic>{
      'stream': instance.stream,
      'type': _$EdpEventItemTypeEnumEnumMap[instance.type]!,
      'id': instance.id,
      'observedAt': ?instance.observedAt?.toIso8601String(),
      'properties': ?instance.properties,
      'eventTime': instance.eventTime.toIso8601String(),
      'geometry': ?instance.geometry?.toJson(),
      'upstreamEventId': ?instance.upstreamEventId,
      'origin': ?instance.origin?.toJson(),
    };

const _$EdpEventItemTypeEnumEnumMap = {EdpEventItemTypeEnum.event: 'event'};
