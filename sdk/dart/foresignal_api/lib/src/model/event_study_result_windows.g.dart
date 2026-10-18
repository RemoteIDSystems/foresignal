// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_study_result_windows.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventStudyResultWindowsCWProxy {
  EventStudyResultWindows estimation(List<int> estimation);

  EventStudyResultWindows preEvent(List<int> preEvent);

  EventStudyResultWindows event(List<int> event);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyResultWindows(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyResultWindows(...).copyWith(id: 12, name: "My name")
  /// ```
  EventStudyResultWindows call({
    List<int> estimation,
    List<int> preEvent,
    List<int> event,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventStudyResultWindows.copyWith(...)` or call `instanceOfEventStudyResultWindows.copyWith.fieldName(value)` for a single field.
class _$EventStudyResultWindowsCWProxyImpl
    implements _$EventStudyResultWindowsCWProxy {
  const _$EventStudyResultWindowsCWProxyImpl(this._value);

  final EventStudyResultWindows _value;

  @override
  EventStudyResultWindows estimation(List<int> estimation) =>
      call(estimation: estimation);

  @override
  EventStudyResultWindows preEvent(List<int> preEvent) =>
      call(preEvent: preEvent);

  @override
  EventStudyResultWindows event(List<int> event) => call(event: event);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyResultWindows(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyResultWindows(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventStudyResultWindows call({
    Object? estimation = const $CopyWithPlaceholder(),
    Object? preEvent = const $CopyWithPlaceholder(),
    Object? event = const $CopyWithPlaceholder(),
  }) {
    return EventStudyResultWindows(
      estimation:
          estimation == const $CopyWithPlaceholder() || estimation == null
          ? _value.estimation
          // ignore: cast_nullable_to_non_nullable
          : estimation as List<int>,
      preEvent: preEvent == const $CopyWithPlaceholder() || preEvent == null
          ? _value.preEvent
          // ignore: cast_nullable_to_non_nullable
          : preEvent as List<int>,
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as List<int>,
    );
  }
}

extension $EventStudyResultWindowsCopyWith on EventStudyResultWindows {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventStudyResultWindows.copyWith(...)` or `instanceOfEventStudyResultWindows.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventStudyResultWindowsCWProxy get copyWith =>
      _$EventStudyResultWindowsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventStudyResultWindows _$EventStudyResultWindowsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EventStudyResultWindows', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['estimation', 'preEvent', 'event']);
  final val = EventStudyResultWindows(
    estimation: $checkedConvert(
      'estimation',
      (v) => (v as List<dynamic>).map((e) => (e as num).toInt()).toList(),
    ),
    preEvent: $checkedConvert(
      'preEvent',
      (v) => (v as List<dynamic>).map((e) => (e as num).toInt()).toList(),
    ),
    event: $checkedConvert(
      'event',
      (v) => (v as List<dynamic>).map((e) => (e as num).toInt()).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$EventStudyResultWindowsToJson(
  EventStudyResultWindows instance,
) => <String, dynamic>{
  'estimation': instance.estimation,
  'preEvent': instance.preEvent,
  'event': instance.event,
};
