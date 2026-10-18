// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_study_result_caar.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventStudyResultCaarCWProxy {
  EventStudyResultCaar preEvent(EventStudyWindowStats preEvent);

  EventStudyResultCaar event(EventStudyWindowStats event);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyResultCaar(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyResultCaar(...).copyWith(id: 12, name: "My name")
  /// ```
  EventStudyResultCaar call({
    EventStudyWindowStats preEvent,
    EventStudyWindowStats event,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventStudyResultCaar.copyWith(...)` or call `instanceOfEventStudyResultCaar.copyWith.fieldName(value)` for a single field.
class _$EventStudyResultCaarCWProxyImpl
    implements _$EventStudyResultCaarCWProxy {
  const _$EventStudyResultCaarCWProxyImpl(this._value);

  final EventStudyResultCaar _value;

  @override
  EventStudyResultCaar preEvent(EventStudyWindowStats preEvent) =>
      call(preEvent: preEvent);

  @override
  EventStudyResultCaar event(EventStudyWindowStats event) => call(event: event);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyResultCaar(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyResultCaar(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventStudyResultCaar call({
    Object? preEvent = const $CopyWithPlaceholder(),
    Object? event = const $CopyWithPlaceholder(),
  }) {
    return EventStudyResultCaar(
      preEvent: preEvent == const $CopyWithPlaceholder() || preEvent == null
          ? _value.preEvent
          // ignore: cast_nullable_to_non_nullable
          : preEvent as EventStudyWindowStats,
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as EventStudyWindowStats,
    );
  }
}

extension $EventStudyResultCaarCopyWith on EventStudyResultCaar {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventStudyResultCaar.copyWith(...)` or `instanceOfEventStudyResultCaar.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventStudyResultCaarCWProxy get copyWith =>
      _$EventStudyResultCaarCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventStudyResultCaar _$EventStudyResultCaarFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EventStudyResultCaar', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['preEvent', 'event']);
  final val = EventStudyResultCaar(
    preEvent: $checkedConvert(
      'preEvent',
      (v) => EventStudyWindowStats.fromJson(v as Map<String, dynamic>),
    ),
    event: $checkedConvert(
      'event',
      (v) => EventStudyWindowStats.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$EventStudyResultCaarToJson(
  EventStudyResultCaar instance,
) => <String, dynamic>{
  'preEvent': instance.preEvent.toJson(),
  'event': instance.event.toJson(),
};
