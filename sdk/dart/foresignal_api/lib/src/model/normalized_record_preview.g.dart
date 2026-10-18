// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'normalized_record_preview.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NormalizedRecordPreviewCWProxy {
  NormalizedRecordPreview externalId(String externalId);

  NormalizedRecordPreview kind(String kind);

  NormalizedRecordPreview observedAt(DateTime observedAt);

  NormalizedRecordPreview geometry(NormalizedRecordPreviewGeometry? geometry);

  NormalizedRecordPreview validUntil(DateTime? validUntil);

  NormalizedRecordPreview tombstone(bool? tombstone);

  NormalizedRecordPreview payload(Map<String, Object> payload);

  NormalizedRecordPreview eventTime(DateTime? eventTime);

  NormalizedRecordPreview validFrom(DateTime? validFrom);

  NormalizedRecordPreview quality(Quality? quality);

  NormalizedRecordPreview observers(List<Observer>? observers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NormalizedRecordPreview(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NormalizedRecordPreview(...).copyWith(id: 12, name: "My name")
  /// ```
  NormalizedRecordPreview call({
    String externalId,
    String kind,
    DateTime observedAt,
    NormalizedRecordPreviewGeometry? geometry,
    DateTime? validUntil,
    bool? tombstone,
    Map<String, Object> payload,
    DateTime? eventTime,
    DateTime? validFrom,
    Quality? quality,
    List<Observer>? observers,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNormalizedRecordPreview.copyWith(...)` or call `instanceOfNormalizedRecordPreview.copyWith.fieldName(value)` for a single field.
class _$NormalizedRecordPreviewCWProxyImpl
    implements _$NormalizedRecordPreviewCWProxy {
  const _$NormalizedRecordPreviewCWProxyImpl(this._value);

  final NormalizedRecordPreview _value;

  @override
  NormalizedRecordPreview externalId(String externalId) =>
      call(externalId: externalId);

  @override
  NormalizedRecordPreview kind(String kind) => call(kind: kind);

  @override
  NormalizedRecordPreview observedAt(DateTime observedAt) =>
      call(observedAt: observedAt);

  @override
  NormalizedRecordPreview geometry(NormalizedRecordPreviewGeometry? geometry) =>
      call(geometry: geometry);

  @override
  NormalizedRecordPreview validUntil(DateTime? validUntil) =>
      call(validUntil: validUntil);

  @override
  NormalizedRecordPreview tombstone(bool? tombstone) =>
      call(tombstone: tombstone);

  @override
  NormalizedRecordPreview payload(Map<String, Object> payload) =>
      call(payload: payload);

  @override
  NormalizedRecordPreview eventTime(DateTime? eventTime) =>
      call(eventTime: eventTime);

  @override
  NormalizedRecordPreview validFrom(DateTime? validFrom) =>
      call(validFrom: validFrom);

  @override
  NormalizedRecordPreview quality(Quality? quality) => call(quality: quality);

  @override
  NormalizedRecordPreview observers(List<Observer>? observers) =>
      call(observers: observers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NormalizedRecordPreview(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NormalizedRecordPreview(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NormalizedRecordPreview call({
    Object? externalId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? validUntil = const $CopyWithPlaceholder(),
    Object? tombstone = const $CopyWithPlaceholder(),
    Object? payload = const $CopyWithPlaceholder(),
    Object? eventTime = const $CopyWithPlaceholder(),
    Object? validFrom = const $CopyWithPlaceholder(),
    Object? quality = const $CopyWithPlaceholder(),
    Object? observers = const $CopyWithPlaceholder(),
  }) {
    return NormalizedRecordPreview(
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
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as NormalizedRecordPreviewGeometry?,
      validUntil: validUntil == const $CopyWithPlaceholder()
          ? _value.validUntil
          // ignore: cast_nullable_to_non_nullable
          : validUntil as DateTime?,
      tombstone: tombstone == const $CopyWithPlaceholder()
          ? _value.tombstone
          // ignore: cast_nullable_to_non_nullable
          : tombstone as bool?,
      payload: payload == const $CopyWithPlaceholder() || payload == null
          ? _value.payload
          // ignore: cast_nullable_to_non_nullable
          : payload as Map<String, Object>,
      eventTime: eventTime == const $CopyWithPlaceholder()
          ? _value.eventTime
          // ignore: cast_nullable_to_non_nullable
          : eventTime as DateTime?,
      validFrom: validFrom == const $CopyWithPlaceholder()
          ? _value.validFrom
          // ignore: cast_nullable_to_non_nullable
          : validFrom as DateTime?,
      quality: quality == const $CopyWithPlaceholder()
          ? _value.quality
          // ignore: cast_nullable_to_non_nullable
          : quality as Quality?,
      observers: observers == const $CopyWithPlaceholder()
          ? _value.observers
          // ignore: cast_nullable_to_non_nullable
          : observers as List<Observer>?,
    );
  }
}

extension $NormalizedRecordPreviewCopyWith on NormalizedRecordPreview {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNormalizedRecordPreview.copyWith(...)` or `instanceOfNormalizedRecordPreview.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NormalizedRecordPreviewCWProxy get copyWith =>
      _$NormalizedRecordPreviewCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NormalizedRecordPreview _$NormalizedRecordPreviewFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NormalizedRecordPreview', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['externalId', 'kind', 'observedAt', 'payload'],
  );
  final val = NormalizedRecordPreview(
    externalId: $checkedConvert('externalId', (v) => v as String),
    kind: $checkedConvert('kind', (v) => v as String),
    observedAt: $checkedConvert(
      'observedAt',
      (v) => DateTime.parse(v as String),
    ),
    geometry: $checkedConvert(
      'geometry',
      (v) => v == null
          ? null
          : NormalizedRecordPreviewGeometry.fromJson(v as Map<String, dynamic>),
    ),
    validUntil: $checkedConvert(
      'validUntil',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    tombstone: $checkedConvert('tombstone', (v) => v as bool?),
    payload: $checkedConvert(
      'payload',
      (v) =>
          (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
    ),
    eventTime: $checkedConvert(
      'eventTime',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    validFrom: $checkedConvert(
      'validFrom',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    quality: $checkedConvert(
      'quality',
      (v) => v == null ? null : Quality.fromJson(v as Map<String, dynamic>),
    ),
    observers: $checkedConvert(
      'observers',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Observer.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$NormalizedRecordPreviewToJson(
  NormalizedRecordPreview instance,
) => <String, dynamic>{
  'externalId': instance.externalId,
  'kind': instance.kind,
  'observedAt': instance.observedAt.toIso8601String(),
  'geometry': ?instance.geometry?.toJson(),
  'validUntil': ?instance.validUntil?.toIso8601String(),
  'tombstone': ?instance.tombstone,
  'payload': instance.payload,
  'eventTime': ?instance.eventTime?.toIso8601String(),
  'validFrom': ?instance.validFrom?.toIso8601String(),
  'quality': ?instance.quality?.toJson(),
  'observers': ?instance.observers?.map((e) => e.toJson()).toList(),
};
