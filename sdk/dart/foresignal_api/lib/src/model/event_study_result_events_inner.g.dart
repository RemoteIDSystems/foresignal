// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_study_result_events_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventStudyResultEventsInnerCWProxy {
  EventStudyResultEventsInner at(DateTime at);

  EventStudyResultEventsInner ref(String? ref);

  EventStudyResultEventsInner clusterId(int clusterId);

  EventStudyResultEventsInner excluded(String? excluded);

  EventStudyResultEventsInner estimationPoints(int? estimationPoints);

  EventStudyResultEventsInner abnormal(
    List<EventStudyResultEventsInnerAbnormalInner>? abnormal,
  );

  EventStudyResultEventsInner car(EventStudyResultEventsInnerCar? car);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyResultEventsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyResultEventsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  EventStudyResultEventsInner call({
    DateTime at,
    String? ref,
    int clusterId,
    String? excluded,
    int? estimationPoints,
    List<EventStudyResultEventsInnerAbnormalInner>? abnormal,
    EventStudyResultEventsInnerCar? car,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventStudyResultEventsInner.copyWith(...)` or call `instanceOfEventStudyResultEventsInner.copyWith.fieldName(value)` for a single field.
class _$EventStudyResultEventsInnerCWProxyImpl
    implements _$EventStudyResultEventsInnerCWProxy {
  const _$EventStudyResultEventsInnerCWProxyImpl(this._value);

  final EventStudyResultEventsInner _value;

  @override
  EventStudyResultEventsInner at(DateTime at) => call(at: at);

  @override
  EventStudyResultEventsInner ref(String? ref) => call(ref: ref);

  @override
  EventStudyResultEventsInner clusterId(int clusterId) =>
      call(clusterId: clusterId);

  @override
  EventStudyResultEventsInner excluded(String? excluded) =>
      call(excluded: excluded);

  @override
  EventStudyResultEventsInner estimationPoints(int? estimationPoints) =>
      call(estimationPoints: estimationPoints);

  @override
  EventStudyResultEventsInner abnormal(
    List<EventStudyResultEventsInnerAbnormalInner>? abnormal,
  ) => call(abnormal: abnormal);

  @override
  EventStudyResultEventsInner car(EventStudyResultEventsInnerCar? car) =>
      call(car: car);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyResultEventsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyResultEventsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventStudyResultEventsInner call({
    Object? at = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? clusterId = const $CopyWithPlaceholder(),
    Object? excluded = const $CopyWithPlaceholder(),
    Object? estimationPoints = const $CopyWithPlaceholder(),
    Object? abnormal = const $CopyWithPlaceholder(),
    Object? car = const $CopyWithPlaceholder(),
  }) {
    return EventStudyResultEventsInner(
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      clusterId: clusterId == const $CopyWithPlaceholder() || clusterId == null
          ? _value.clusterId
          // ignore: cast_nullable_to_non_nullable
          : clusterId as int,
      excluded: excluded == const $CopyWithPlaceholder()
          ? _value.excluded
          // ignore: cast_nullable_to_non_nullable
          : excluded as String?,
      estimationPoints: estimationPoints == const $CopyWithPlaceholder()
          ? _value.estimationPoints
          // ignore: cast_nullable_to_non_nullable
          : estimationPoints as int?,
      abnormal: abnormal == const $CopyWithPlaceholder()
          ? _value.abnormal
          // ignore: cast_nullable_to_non_nullable
          : abnormal as List<EventStudyResultEventsInnerAbnormalInner>?,
      car: car == const $CopyWithPlaceholder()
          ? _value.car
          // ignore: cast_nullable_to_non_nullable
          : car as EventStudyResultEventsInnerCar?,
    );
  }
}

extension $EventStudyResultEventsInnerCopyWith on EventStudyResultEventsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventStudyResultEventsInner.copyWith(...)` or `instanceOfEventStudyResultEventsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventStudyResultEventsInnerCWProxy get copyWith =>
      _$EventStudyResultEventsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventStudyResultEventsInner _$EventStudyResultEventsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EventStudyResultEventsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['at', 'clusterId']);
  final val = EventStudyResultEventsInner(
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
    ref: $checkedConvert('ref', (v) => v as String?),
    clusterId: $checkedConvert('clusterId', (v) => (v as num).toInt()),
    excluded: $checkedConvert('excluded', (v) => v as String?),
    estimationPoints: $checkedConvert(
      'estimationPoints',
      (v) => (v as num?)?.toInt(),
    ),
    abnormal: $checkedConvert(
      'abnormal',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => EventStudyResultEventsInnerAbnormalInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    car: $checkedConvert(
      'car',
      (v) => v == null
          ? null
          : EventStudyResultEventsInnerCar.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$EventStudyResultEventsInnerToJson(
  EventStudyResultEventsInner instance,
) => <String, dynamic>{
  'at': instance.at.toIso8601String(),
  'ref': ?instance.ref,
  'clusterId': instance.clusterId,
  'excluded': ?instance.excluded,
  'estimationPoints': ?instance.estimationPoints,
  'abnormal': ?instance.abnormal?.map((e) => e.toJson()).toList(),
  'car': ?instance.car?.toJson(),
};
