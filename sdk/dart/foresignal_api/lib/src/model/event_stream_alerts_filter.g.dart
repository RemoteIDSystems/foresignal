// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_stream_alerts_filter.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventStreamAlertsFilterCWProxy {
  EventStreamAlertsFilter minSeverity(
    EventStreamAlertsFilterMinSeverityEnum? minSeverity,
  );

  EventStreamAlertsFilter ruleId(List<String>? ruleId);

  EventStreamAlertsFilter source_(List<String>? source_);

  EventStreamAlertsFilter kind(List<String>? kind);

  EventStreamAlertsFilter includeSuppressed(bool? includeSuppressed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStreamAlertsFilter(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStreamAlertsFilter(...).copyWith(id: 12, name: "My name")
  /// ```
  EventStreamAlertsFilter call({
    EventStreamAlertsFilterMinSeverityEnum? minSeverity,
    List<String>? ruleId,
    List<String>? source_,
    List<String>? kind,
    bool? includeSuppressed,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventStreamAlertsFilter.copyWith(...)` or call `instanceOfEventStreamAlertsFilter.copyWith.fieldName(value)` for a single field.
class _$EventStreamAlertsFilterCWProxyImpl
    implements _$EventStreamAlertsFilterCWProxy {
  const _$EventStreamAlertsFilterCWProxyImpl(this._value);

  final EventStreamAlertsFilter _value;

  @override
  EventStreamAlertsFilter minSeverity(
    EventStreamAlertsFilterMinSeverityEnum? minSeverity,
  ) => call(minSeverity: minSeverity);

  @override
  EventStreamAlertsFilter ruleId(List<String>? ruleId) => call(ruleId: ruleId);

  @override
  EventStreamAlertsFilter source_(List<String>? source_) =>
      call(source_: source_);

  @override
  EventStreamAlertsFilter kind(List<String>? kind) => call(kind: kind);

  @override
  EventStreamAlertsFilter includeSuppressed(bool? includeSuppressed) =>
      call(includeSuppressed: includeSuppressed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStreamAlertsFilter(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStreamAlertsFilter(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventStreamAlertsFilter call({
    Object? minSeverity = const $CopyWithPlaceholder(),
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? includeSuppressed = const $CopyWithPlaceholder(),
  }) {
    return EventStreamAlertsFilter(
      minSeverity: minSeverity == const $CopyWithPlaceholder()
          ? _value.minSeverity
          // ignore: cast_nullable_to_non_nullable
          : minSeverity as EventStreamAlertsFilterMinSeverityEnum?,
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
    );
  }
}

extension $EventStreamAlertsFilterCopyWith on EventStreamAlertsFilter {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventStreamAlertsFilter.copyWith(...)` or `instanceOfEventStreamAlertsFilter.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventStreamAlertsFilterCWProxy get copyWith =>
      _$EventStreamAlertsFilterCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventStreamAlertsFilter _$EventStreamAlertsFilterFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EventStreamAlertsFilter', json, ($checkedConvert) {
  final val = EventStreamAlertsFilter(
    minSeverity: $checkedConvert(
      'minSeverity',
      (v) => $enumDecodeNullable(
        _$EventStreamAlertsFilterMinSeverityEnumEnumMap,
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
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$EventStreamAlertsFilterToJson(
  EventStreamAlertsFilter instance,
) => <String, dynamic>{
  'minSeverity':
      ?_$EventStreamAlertsFilterMinSeverityEnumEnumMap[instance.minSeverity],
  'ruleId': ?instance.ruleId,
  'source': ?instance.source_,
  'kind': ?instance.kind,
  'includeSuppressed': ?instance.includeSuppressed,
};

const _$EventStreamAlertsFilterMinSeverityEnumEnumMap = {
  EventStreamAlertsFilterMinSeverityEnum.info: 'info',
  EventStreamAlertsFilterMinSeverityEnum.warning: 'warning',
  EventStreamAlertsFilterMinSeverityEnum.critical: 'critical',
};
