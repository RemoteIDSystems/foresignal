// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'live_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LiveEventCWProxy {
  LiveEvent sourceId(String sourceId);

  LiveEvent externalId(String externalId);

  LiveEvent kind(String kind);

  LiveEvent observedAt(DateTime observedAt);

  LiveEvent validUntil(DateTime? validUntil);

  LiveEvent version(int version);

  LiveEvent geometry(Geometry? geometry);

  LiveEvent bbox(List<num>? bbox);

  LiveEvent prevBbox(List<num>? prevBbox);

  LiveEvent properties(Map<String, Object>? properties);

  LiveEvent quality(Quality? quality);

  LiveEvent leftFilter(bool? leftFilter);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LiveEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LiveEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  LiveEvent call({
    String sourceId,
    String externalId,
    String kind,
    DateTime observedAt,
    DateTime? validUntil,
    int version,
    Geometry? geometry,
    List<num>? bbox,
    List<num>? prevBbox,
    Map<String, Object>? properties,
    Quality? quality,
    bool? leftFilter,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLiveEvent.copyWith(...)` or call `instanceOfLiveEvent.copyWith.fieldName(value)` for a single field.
class _$LiveEventCWProxyImpl implements _$LiveEventCWProxy {
  const _$LiveEventCWProxyImpl(this._value);

  final LiveEvent _value;

  @override
  LiveEvent sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  LiveEvent externalId(String externalId) => call(externalId: externalId);

  @override
  LiveEvent kind(String kind) => call(kind: kind);

  @override
  LiveEvent observedAt(DateTime observedAt) => call(observedAt: observedAt);

  @override
  LiveEvent validUntil(DateTime? validUntil) => call(validUntil: validUntil);

  @override
  LiveEvent version(int version) => call(version: version);

  @override
  LiveEvent geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  LiveEvent bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  LiveEvent prevBbox(List<num>? prevBbox) => call(prevBbox: prevBbox);

  @override
  LiveEvent properties(Map<String, Object>? properties) =>
      call(properties: properties);

  @override
  LiveEvent quality(Quality? quality) => call(quality: quality);

  @override
  LiveEvent leftFilter(bool? leftFilter) => call(leftFilter: leftFilter);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LiveEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LiveEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LiveEvent call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? validUntil = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? prevBbox = const $CopyWithPlaceholder(),
    Object? properties = const $CopyWithPlaceholder(),
    Object? quality = const $CopyWithPlaceholder(),
    Object? leftFilter = const $CopyWithPlaceholder(),
  }) {
    return LiveEvent(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String,
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime,
      validUntil: validUntil == const $CopyWithPlaceholder()
          ? _value.validUntil
          // ignore: cast_nullable_to_non_nullable
          : validUntil as DateTime?,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      prevBbox: prevBbox == const $CopyWithPlaceholder()
          ? _value.prevBbox
          // ignore: cast_nullable_to_non_nullable
          : prevBbox as List<num>?,
      properties: properties == const $CopyWithPlaceholder()
          ? _value.properties
          // ignore: cast_nullable_to_non_nullable
          : properties as Map<String, Object>?,
      quality: quality == const $CopyWithPlaceholder()
          ? _value.quality
          // ignore: cast_nullable_to_non_nullable
          : quality as Quality?,
      leftFilter: leftFilter == const $CopyWithPlaceholder()
          ? _value.leftFilter
          // ignore: cast_nullable_to_non_nullable
          : leftFilter as bool?,
    );
  }
}

extension $LiveEventCopyWith on LiveEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLiveEvent.copyWith(...)` or `instanceOfLiveEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LiveEventCWProxy get copyWith => _$LiveEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LiveEvent _$LiveEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LiveEvent', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'sourceId',
          'externalId',
          'kind',
          'observedAt',
          'version',
        ],
      );
      final val = LiveEvent(
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        externalId: $checkedConvert('externalId', (v) => v as String),
        kind: $checkedConvert('kind', (v) => v as String),
        observedAt: $checkedConvert(
          'observedAt',
          (v) => DateTime.parse(v as String),
        ),
        validUntil: $checkedConvert(
          'validUntil',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        version: $checkedConvert('version', (v) => (v as num).toInt()),
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        bbox: $checkedConvert(
          'bbox',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        prevBbox: $checkedConvert(
          'prevBbox',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        properties: $checkedConvert(
          'properties',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        quality: $checkedConvert(
          'quality',
          (v) => v == null ? null : Quality.fromJson(v as Map<String, dynamic>),
        ),
        leftFilter: $checkedConvert('leftFilter', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$LiveEventToJson(LiveEvent instance) => <String, dynamic>{
  'sourceId': instance.sourceId,
  'externalId': instance.externalId,
  'kind': instance.kind,
  'observedAt': instance.observedAt.toIso8601String(),
  'validUntil': ?instance.validUntil?.toIso8601String(),
  'version': instance.version,
  'geometry': ?instance.geometry?.toJson(),
  'bbox': ?instance.bbox,
  'prevBbox': ?instance.prevBbox,
  'properties': ?instance.properties,
  'quality': ?instance.quality?.toJson(),
  'leftFilter': ?instance.leftFilter,
};
