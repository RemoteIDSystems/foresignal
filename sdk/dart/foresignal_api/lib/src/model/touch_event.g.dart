// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'touch_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TouchEventCWProxy {
  TouchEvent sourceId(String sourceId);

  TouchEvent externalId(String externalId);

  TouchEvent observedAt(DateTime observedAt);

  TouchEvent validUntil(DateTime? validUntil);

  TouchEvent bbox(List<num>? bbox);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TouchEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TouchEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  TouchEvent call({
    String sourceId,
    String externalId,
    DateTime observedAt,
    DateTime? validUntil,
    List<num>? bbox,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTouchEvent.copyWith(...)` or call `instanceOfTouchEvent.copyWith.fieldName(value)` for a single field.
class _$TouchEventCWProxyImpl implements _$TouchEventCWProxy {
  const _$TouchEventCWProxyImpl(this._value);

  final TouchEvent _value;

  @override
  TouchEvent sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  TouchEvent externalId(String externalId) => call(externalId: externalId);

  @override
  TouchEvent observedAt(DateTime observedAt) => call(observedAt: observedAt);

  @override
  TouchEvent validUntil(DateTime? validUntil) => call(validUntil: validUntil);

  @override
  TouchEvent bbox(List<num>? bbox) => call(bbox: bbox);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TouchEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TouchEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TouchEvent call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? validUntil = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
  }) {
    return TouchEvent(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime,
      validUntil: validUntil == const $CopyWithPlaceholder()
          ? _value.validUntil
          // ignore: cast_nullable_to_non_nullable
          : validUntil as DateTime?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
    );
  }
}

extension $TouchEventCopyWith on TouchEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTouchEvent.copyWith(...)` or `instanceOfTouchEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TouchEventCWProxy get copyWith => _$TouchEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TouchEvent _$TouchEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TouchEvent', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['sourceId', 'externalId', 'observedAt'],
      );
      final val = TouchEvent(
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        externalId: $checkedConvert('externalId', (v) => v as String),
        observedAt: $checkedConvert(
          'observedAt',
          (v) => DateTime.parse(v as String),
        ),
        validUntil: $checkedConvert(
          'validUntil',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        bbox: $checkedConvert(
          'bbox',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TouchEventToJson(TouchEvent instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'externalId': instance.externalId,
      'observedAt': instance.observedAt.toIso8601String(),
      'validUntil': ?instance.validUntil?.toIso8601String(),
      'bbox': ?instance.bbox,
    };
