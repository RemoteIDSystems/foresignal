// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_study_result_events_inner_car.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventStudyResultEventsInnerCarCWProxy {
  EventStudyResultEventsInnerCar preEvent(num? preEvent);

  EventStudyResultEventsInnerCar event(num? event);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyResultEventsInnerCar(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyResultEventsInnerCar(...).copyWith(id: 12, name: "My name")
  /// ```
  EventStudyResultEventsInnerCar call({num? preEvent, num? event});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventStudyResultEventsInnerCar.copyWith(...)` or call `instanceOfEventStudyResultEventsInnerCar.copyWith.fieldName(value)` for a single field.
class _$EventStudyResultEventsInnerCarCWProxyImpl
    implements _$EventStudyResultEventsInnerCarCWProxy {
  const _$EventStudyResultEventsInnerCarCWProxyImpl(this._value);

  final EventStudyResultEventsInnerCar _value;

  @override
  EventStudyResultEventsInnerCar preEvent(num? preEvent) =>
      call(preEvent: preEvent);

  @override
  EventStudyResultEventsInnerCar event(num? event) => call(event: event);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyResultEventsInnerCar(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyResultEventsInnerCar(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventStudyResultEventsInnerCar call({
    Object? preEvent = const $CopyWithPlaceholder(),
    Object? event = const $CopyWithPlaceholder(),
  }) {
    return EventStudyResultEventsInnerCar(
      preEvent: preEvent == const $CopyWithPlaceholder()
          ? _value.preEvent
          // ignore: cast_nullable_to_non_nullable
          : preEvent as num?,
      event: event == const $CopyWithPlaceholder()
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as num?,
    );
  }
}

extension $EventStudyResultEventsInnerCarCopyWith
    on EventStudyResultEventsInnerCar {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventStudyResultEventsInnerCar.copyWith(...)` or `instanceOfEventStudyResultEventsInnerCar.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventStudyResultEventsInnerCarCWProxy get copyWith =>
      _$EventStudyResultEventsInnerCarCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventStudyResultEventsInnerCar _$EventStudyResultEventsInnerCarFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EventStudyResultEventsInnerCar', json, ($checkedConvert) {
  final val = EventStudyResultEventsInnerCar(
    preEvent: $checkedConvert('preEvent', (v) => v as num?),
    event: $checkedConvert('event', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$EventStudyResultEventsInnerCarToJson(
  EventStudyResultEventsInnerCar instance,
) => <String, dynamic>{
  'preEvent': ?instance.preEvent,
  'event': ?instance.event,
};
