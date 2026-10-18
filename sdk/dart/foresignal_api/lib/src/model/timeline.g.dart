// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'timeline.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TimelineCWProxy {
  Timeline from(DateTime from);

  Timeline to(DateTime to);

  Timeline bucket(String bucket);

  Timeline timeField(TimelineTimeFieldEnum timeField);

  Timeline measure(TimelineMeasureEnum? measure);

  Timeline groupBy(TimelineGroupByEnum? groupBy);

  Timeline buckets(List<TimelineBucket> buckets);

  Timeline alerts(List<TimelineAlertsInner>? alerts);

  Timeline maxBucket(int? maxBucket);

  Timeline approximate(bool? approximate);

  Timeline retainedFrom(DateTime? retainedFrom);

  Timeline backend(TimelineBackendEnum? backend);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Timeline(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Timeline(...).copyWith(id: 12, name: "My name")
  /// ```
  Timeline call({
    DateTime from,
    DateTime to,
    String bucket,
    TimelineTimeFieldEnum timeField,
    TimelineMeasureEnum? measure,
    TimelineGroupByEnum? groupBy,
    List<TimelineBucket> buckets,
    List<TimelineAlertsInner>? alerts,
    int? maxBucket,
    bool? approximate,
    DateTime? retainedFrom,
    TimelineBackendEnum? backend,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTimeline.copyWith(...)` or call `instanceOfTimeline.copyWith.fieldName(value)` for a single field.
class _$TimelineCWProxyImpl implements _$TimelineCWProxy {
  const _$TimelineCWProxyImpl(this._value);

  final Timeline _value;

  @override
  Timeline from(DateTime from) => call(from: from);

  @override
  Timeline to(DateTime to) => call(to: to);

  @override
  Timeline bucket(String bucket) => call(bucket: bucket);

  @override
  Timeline timeField(TimelineTimeFieldEnum timeField) =>
      call(timeField: timeField);

  @override
  Timeline measure(TimelineMeasureEnum? measure) => call(measure: measure);

  @override
  Timeline groupBy(TimelineGroupByEnum? groupBy) => call(groupBy: groupBy);

  @override
  Timeline buckets(List<TimelineBucket> buckets) => call(buckets: buckets);

  @override
  Timeline alerts(List<TimelineAlertsInner>? alerts) => call(alerts: alerts);

  @override
  Timeline maxBucket(int? maxBucket) => call(maxBucket: maxBucket);

  @override
  Timeline approximate(bool? approximate) => call(approximate: approximate);

  @override
  Timeline retainedFrom(DateTime? retainedFrom) =>
      call(retainedFrom: retainedFrom);

  @override
  Timeline backend(TimelineBackendEnum? backend) => call(backend: backend);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Timeline(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Timeline(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Timeline call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? bucket = const $CopyWithPlaceholder(),
    Object? timeField = const $CopyWithPlaceholder(),
    Object? measure = const $CopyWithPlaceholder(),
    Object? groupBy = const $CopyWithPlaceholder(),
    Object? buckets = const $CopyWithPlaceholder(),
    Object? alerts = const $CopyWithPlaceholder(),
    Object? maxBucket = const $CopyWithPlaceholder(),
    Object? approximate = const $CopyWithPlaceholder(),
    Object? retainedFrom = const $CopyWithPlaceholder(),
    Object? backend = const $CopyWithPlaceholder(),
  }) {
    return Timeline(
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime,
      bucket: bucket == const $CopyWithPlaceholder() || bucket == null
          ? _value.bucket
          // ignore: cast_nullable_to_non_nullable
          : bucket as String,
      timeField: timeField == const $CopyWithPlaceholder() || timeField == null
          ? _value.timeField
          // ignore: cast_nullable_to_non_nullable
          : timeField as TimelineTimeFieldEnum,
      measure: measure == const $CopyWithPlaceholder()
          ? _value.measure
          // ignore: cast_nullable_to_non_nullable
          : measure as TimelineMeasureEnum?,
      groupBy: groupBy == const $CopyWithPlaceholder()
          ? _value.groupBy
          // ignore: cast_nullable_to_non_nullable
          : groupBy as TimelineGroupByEnum?,
      buckets: buckets == const $CopyWithPlaceholder() || buckets == null
          ? _value.buckets
          // ignore: cast_nullable_to_non_nullable
          : buckets as List<TimelineBucket>,
      alerts: alerts == const $CopyWithPlaceholder()
          ? _value.alerts
          // ignore: cast_nullable_to_non_nullable
          : alerts as List<TimelineAlertsInner>?,
      maxBucket: maxBucket == const $CopyWithPlaceholder()
          ? _value.maxBucket
          // ignore: cast_nullable_to_non_nullable
          : maxBucket as int?,
      approximate: approximate == const $CopyWithPlaceholder()
          ? _value.approximate
          // ignore: cast_nullable_to_non_nullable
          : approximate as bool?,
      retainedFrom: retainedFrom == const $CopyWithPlaceholder()
          ? _value.retainedFrom
          // ignore: cast_nullable_to_non_nullable
          : retainedFrom as DateTime?,
      backend: backend == const $CopyWithPlaceholder()
          ? _value.backend
          // ignore: cast_nullable_to_non_nullable
          : backend as TimelineBackendEnum?,
    );
  }
}

extension $TimelineCopyWith on Timeline {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTimeline.copyWith(...)` or `instanceOfTimeline.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TimelineCWProxy get copyWith => _$TimelineCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Timeline _$TimelineFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Timeline', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['from', 'to', 'bucket', 'timeField', 'buckets'],
      );
      final val = Timeline(
        from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
        to: $checkedConvert('to', (v) => DateTime.parse(v as String)),
        bucket: $checkedConvert('bucket', (v) => v as String),
        timeField: $checkedConvert(
          'timeField',
          (v) => $enumDecode(_$TimelineTimeFieldEnumEnumMap, v),
        ),
        measure: $checkedConvert(
          'measure',
          (v) => $enumDecodeNullable(_$TimelineMeasureEnumEnumMap, v),
        ),
        groupBy: $checkedConvert(
          'groupBy',
          (v) => $enumDecodeNullable(_$TimelineGroupByEnumEnumMap, v),
        ),
        buckets: $checkedConvert(
          'buckets',
          (v) => (v as List<dynamic>)
              .map((e) => TimelineBucket.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        alerts: $checkedConvert(
          'alerts',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => TimelineAlertsInner.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
        maxBucket: $checkedConvert('maxBucket', (v) => (v as num?)?.toInt()),
        approximate: $checkedConvert('approximate', (v) => v as bool?),
        retainedFrom: $checkedConvert(
          'retainedFrom',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        backend: $checkedConvert(
          'backend',
          (v) => $enumDecodeNullable(_$TimelineBackendEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TimelineToJson(Timeline instance) => <String, dynamic>{
  'from': instance.from.toIso8601String(),
  'to': instance.to.toIso8601String(),
  'bucket': instance.bucket,
  'timeField': _$TimelineTimeFieldEnumEnumMap[instance.timeField]!,
  'measure': ?_$TimelineMeasureEnumEnumMap[instance.measure],
  'groupBy': ?_$TimelineGroupByEnumEnumMap[instance.groupBy],
  'buckets': instance.buckets.map((e) => e.toJson()).toList(),
  'alerts': ?instance.alerts?.map((e) => e.toJson()).toList(),
  'maxBucket': ?instance.maxBucket,
  'approximate': ?instance.approximate,
  'retainedFrom': ?instance.retainedFrom?.toIso8601String(),
  'backend': ?_$TimelineBackendEnumEnumMap[instance.backend],
};

const _$TimelineTimeFieldEnumEnumMap = {
  TimelineTimeFieldEnum.effective: 'effective',
  TimelineTimeFieldEnum.observed: 'observed',
  TimelineTimeFieldEnum.event: 'event',
  TimelineTimeFieldEnum.ingested: 'ingested',
};

const _$TimelineMeasureEnumEnumMap = {
  TimelineMeasureEnum.versions: 'versions',
  TimelineMeasureEnum.entities: 'entities',
};

const _$TimelineGroupByEnumEnumMap = {
  TimelineGroupByEnum.none: 'none',
  TimelineGroupByEnum.kind: 'kind',
  TimelineGroupByEnum.source_: 'source',
};

const _$TimelineBackendEnumEnumMap = {
  TimelineBackendEnum.raw: 'raw',
  TimelineBackendEnum.rollup: 'rollup',
  TimelineBackendEnum.mixed: 'mixed',
};
