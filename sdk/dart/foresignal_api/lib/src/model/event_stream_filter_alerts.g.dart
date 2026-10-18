// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_stream_filter_alerts.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventStreamFilterAlertsCWProxy {
  EventStreamFilterAlerts minSeverity(
    EventStreamFilterAlertsMinSeverityEnum? minSeverity,
  );

  EventStreamFilterAlerts ruleId(List<String>? ruleId);

  EventStreamFilterAlerts source_(List<String>? source_);

  EventStreamFilterAlerts kind(List<String>? kind);

  EventStreamFilterAlerts includeSuppressed(bool? includeSuppressed);

  EventStreamFilterAlerts includeExercise(bool? includeExercise);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStreamFilterAlerts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStreamFilterAlerts(...).copyWith(id: 12, name: "My name")
  /// ```
  EventStreamFilterAlerts call({
    EventStreamFilterAlertsMinSeverityEnum? minSeverity,
    List<String>? ruleId,
    List<String>? source_,
    List<String>? kind,
    bool? includeSuppressed,
    bool? includeExercise,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventStreamFilterAlerts.copyWith(...)` or call `instanceOfEventStreamFilterAlerts.copyWith.fieldName(value)` for a single field.
class _$EventStreamFilterAlertsCWProxyImpl
    implements _$EventStreamFilterAlertsCWProxy {
  const _$EventStreamFilterAlertsCWProxyImpl(this._value);

  final EventStreamFilterAlerts _value;

  @override
  EventStreamFilterAlerts minSeverity(
    EventStreamFilterAlertsMinSeverityEnum? minSeverity,
  ) => call(minSeverity: minSeverity);

  @override
  EventStreamFilterAlerts ruleId(List<String>? ruleId) => call(ruleId: ruleId);

  @override
  EventStreamFilterAlerts source_(List<String>? source_) =>
      call(source_: source_);

  @override
  EventStreamFilterAlerts kind(List<String>? kind) => call(kind: kind);

  @override
  EventStreamFilterAlerts includeSuppressed(bool? includeSuppressed) =>
      call(includeSuppressed: includeSuppressed);

  @override
  EventStreamFilterAlerts includeExercise(bool? includeExercise) =>
      call(includeExercise: includeExercise);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStreamFilterAlerts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStreamFilterAlerts(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventStreamFilterAlerts call({
    Object? minSeverity = const $CopyWithPlaceholder(),
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? includeSuppressed = const $CopyWithPlaceholder(),
    Object? includeExercise = const $CopyWithPlaceholder(),
  }) {
    return EventStreamFilterAlerts(
      minSeverity: minSeverity == const $CopyWithPlaceholder()
          ? _value.minSeverity
          // ignore: cast_nullable_to_non_nullable
          : minSeverity as EventStreamFilterAlertsMinSeverityEnum?,
      ruleId: ruleId == const $CopyWithPlaceholder()
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as List<String>?,
      source_: source_ == const $CopyWithPlaceholder()
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as List<String>?,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as List<String>?,
      includeSuppressed: includeSuppressed == const $CopyWithPlaceholder()
          ? _value.includeSuppressed
          // ignore: cast_nullable_to_non_nullable
          : includeSuppressed as bool?,
      includeExercise: includeExercise == const $CopyWithPlaceholder()
          ? _value.includeExercise
          // ignore: cast_nullable_to_non_nullable
          : includeExercise as bool?,
    );
  }
}

extension $EventStreamFilterAlertsCopyWith on EventStreamFilterAlerts {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventStreamFilterAlerts.copyWith(...)` or `instanceOfEventStreamFilterAlerts.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventStreamFilterAlertsCWProxy get copyWith =>
      _$EventStreamFilterAlertsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventStreamFilterAlerts _$EventStreamFilterAlertsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EventStreamFilterAlerts', json, ($checkedConvert) {
  final val = EventStreamFilterAlerts(
    minSeverity: $checkedConvert(
      'minSeverity',
      (v) => $enumDecodeNullable(
        _$EventStreamFilterAlertsMinSeverityEnumEnumMap,
        v,
      ),
    ),
    ruleId: $checkedConvert(
      'ruleId',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    source_: $checkedConvert(
      'source',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    kind: $checkedConvert(
      'kind',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    includeSuppressed: $checkedConvert(
      'includeSuppressed',
      (v) => v as bool? ?? false,
    ),
    includeExercise: $checkedConvert(
      'includeExercise',
      (v) => v as bool? ?? false,
    ),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$EventStreamFilterAlertsToJson(
  EventStreamFilterAlerts instance,
) => <String, dynamic>{
  'minSeverity':
      ?_$EventStreamFilterAlertsMinSeverityEnumEnumMap[instance.minSeverity],
  'ruleId': ?instance.ruleId,
  'source': ?instance.source_,
  'kind': ?instance.kind,
  'includeSuppressed': ?instance.includeSuppressed,
  'includeExercise': ?instance.includeExercise,
};

const _$EventStreamFilterAlertsMinSeverityEnumEnumMap = {
  EventStreamFilterAlertsMinSeverityEnum.info: 'info',
  EventStreamFilterAlertsMinSeverityEnum.warning: 'warning',
  EventStreamFilterAlertsMinSeverityEnum.critical: 'critical',
};
