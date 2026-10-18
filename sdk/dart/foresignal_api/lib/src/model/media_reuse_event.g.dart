// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'media_reuse_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MediaReuseEventCWProxy {
  MediaReuseEvent event(MediaReuseEventEventEnum event);

  MediaReuseEvent findingId(String findingId);

  MediaReuseEvent fingerprintId(String fingerprintId);

  MediaReuseEvent narrativeId(String narrativeId);

  MediaReuseEvent earlierNarrativeId(String? earlierNarrativeId);

  MediaReuseEvent firstSeenAt(DateTime firstSeenAt);

  MediaReuseEvent reason(MediaReuseEventReasonEnum reason);

  MediaReuseEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaReuseEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaReuseEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  MediaReuseEvent call({
    MediaReuseEventEventEnum event,
    String findingId,
    String fingerprintId,
    String narrativeId,
    String? earlierNarrativeId,
    DateTime firstSeenAt,
    MediaReuseEventReasonEnum reason,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMediaReuseEvent.copyWith(...)` or call `instanceOfMediaReuseEvent.copyWith.fieldName(value)` for a single field.
class _$MediaReuseEventCWProxyImpl implements _$MediaReuseEventCWProxy {
  const _$MediaReuseEventCWProxyImpl(this._value);

  final MediaReuseEvent _value;

  @override
  MediaReuseEvent event(MediaReuseEventEventEnum event) => call(event: event);

  @override
  MediaReuseEvent findingId(String findingId) => call(findingId: findingId);

  @override
  MediaReuseEvent fingerprintId(String fingerprintId) =>
      call(fingerprintId: fingerprintId);

  @override
  MediaReuseEvent narrativeId(String narrativeId) =>
      call(narrativeId: narrativeId);

  @override
  MediaReuseEvent earlierNarrativeId(String? earlierNarrativeId) =>
      call(earlierNarrativeId: earlierNarrativeId);

  @override
  MediaReuseEvent firstSeenAt(DateTime firstSeenAt) =>
      call(firstSeenAt: firstSeenAt);

  @override
  MediaReuseEvent reason(MediaReuseEventReasonEnum reason) =>
      call(reason: reason);

  @override
  MediaReuseEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaReuseEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaReuseEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MediaReuseEvent call({
    Object? event = const $CopyWithPlaceholder(),
    Object? findingId = const $CopyWithPlaceholder(),
    Object? fingerprintId = const $CopyWithPlaceholder(),
    Object? narrativeId = const $CopyWithPlaceholder(),
    Object? earlierNarrativeId = const $CopyWithPlaceholder(),
    Object? firstSeenAt = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return MediaReuseEvent(
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as MediaReuseEventEventEnum,
      findingId: findingId == const $CopyWithPlaceholder() || findingId == null
          ? _value.findingId
          // ignore: cast_nullable_to_non_nullable
          : findingId as String,
      fingerprintId:
          fingerprintId == const $CopyWithPlaceholder() || fingerprintId == null
          ? _value.fingerprintId
          // ignore: cast_nullable_to_non_nullable
          : fingerprintId as String,
      narrativeId:
          narrativeId == const $CopyWithPlaceholder() || narrativeId == null
          ? _value.narrativeId
          // ignore: cast_nullable_to_non_nullable
          : narrativeId as String,
      earlierNarrativeId: earlierNarrativeId == const $CopyWithPlaceholder()
          ? _value.earlierNarrativeId
          // ignore: cast_nullable_to_non_nullable
          : earlierNarrativeId as String?,
      firstSeenAt:
          firstSeenAt == const $CopyWithPlaceholder() || firstSeenAt == null
          ? _value.firstSeenAt
          // ignore: cast_nullable_to_non_nullable
          : firstSeenAt as DateTime,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as MediaReuseEventReasonEnum,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $MediaReuseEventCopyWith on MediaReuseEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMediaReuseEvent.copyWith(...)` or `instanceOfMediaReuseEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MediaReuseEventCWProxy get copyWith => _$MediaReuseEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MediaReuseEvent _$MediaReuseEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MediaReuseEvent', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'event',
          'findingId',
          'fingerprintId',
          'narrativeId',
          'firstSeenAt',
          'reason',
          'at',
        ],
      );
      final val = MediaReuseEvent(
        event: $checkedConvert(
          'event',
          (v) => $enumDecode(_$MediaReuseEventEventEnumEnumMap, v),
        ),
        findingId: $checkedConvert('findingId', (v) => v as String),
        fingerprintId: $checkedConvert('fingerprintId', (v) => v as String),
        narrativeId: $checkedConvert('narrativeId', (v) => v as String),
        earlierNarrativeId: $checkedConvert(
          'earlierNarrativeId',
          (v) => v as String?,
        ),
        firstSeenAt: $checkedConvert(
          'firstSeenAt',
          (v) => DateTime.parse(v as String),
        ),
        reason: $checkedConvert(
          'reason',
          (v) => $enumDecode(_$MediaReuseEventReasonEnumEnumMap, v),
        ),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$MediaReuseEventToJson(MediaReuseEvent instance) =>
    <String, dynamic>{
      'event': _$MediaReuseEventEventEnumEnumMap[instance.event]!,
      'findingId': instance.findingId,
      'fingerprintId': instance.fingerprintId,
      'narrativeId': instance.narrativeId,
      'earlierNarrativeId': ?instance.earlierNarrativeId,
      'firstSeenAt': instance.firstSeenAt.toIso8601String(),
      'reason': _$MediaReuseEventReasonEnumEnumMap[instance.reason]!,
      'at': instance.at.toIso8601String(),
    };

const _$MediaReuseEventEventEnumEnumMap = {
  MediaReuseEventEventEnum.mediaPeriodReuse: 'media.reuse',
};

const _$MediaReuseEventReasonEnumEnumMap = {
  MediaReuseEventReasonEnum.earlier: 'earlier',
  MediaReuseEventReasonEnum.elsewhere: 'elsewhere',
};
