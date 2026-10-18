// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stats_series.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StatsSeriesCWProxy {
  StatsSeries from(DateTime from);

  StatsSeries to(DateTime to);

  StatsSeries bucket(String bucket);

  StatsSeries measure(StatsSeriesMeasureEnum measure);

  StatsSeries groupBy(StatsSeriesGroupByEnum? groupBy);

  StatsSeries timeField(StatsSeriesTimeFieldEnum? timeField);

  StatsSeries backend(StatsSeriesBackendEnum backend);

  StatsSeries series(List<StatsSeriesSeriesInner> series);

  StatsSeries retainedFrom(DateTime? retainedFrom);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsSeries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsSeries(...).copyWith(id: 12, name: "My name")
  /// ```
  StatsSeries call({
    DateTime from,
    DateTime to,
    String bucket,
    StatsSeriesMeasureEnum measure,
    StatsSeriesGroupByEnum? groupBy,
    StatsSeriesTimeFieldEnum? timeField,
    StatsSeriesBackendEnum backend,
    List<StatsSeriesSeriesInner> series,
    DateTime? retainedFrom,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStatsSeries.copyWith(...)` or call `instanceOfStatsSeries.copyWith.fieldName(value)` for a single field.
class _$StatsSeriesCWProxyImpl implements _$StatsSeriesCWProxy {
  const _$StatsSeriesCWProxyImpl(this._value);

  final StatsSeries _value;

  @override
  StatsSeries from(DateTime from) => call(from: from);

  @override
  StatsSeries to(DateTime to) => call(to: to);

  @override
  StatsSeries bucket(String bucket) => call(bucket: bucket);

  @override
  StatsSeries measure(StatsSeriesMeasureEnum measure) => call(measure: measure);

  @override
  StatsSeries groupBy(StatsSeriesGroupByEnum? groupBy) =>
      call(groupBy: groupBy);

  @override
  StatsSeries timeField(StatsSeriesTimeFieldEnum? timeField) =>
      call(timeField: timeField);

  @override
  StatsSeries backend(StatsSeriesBackendEnum backend) => call(backend: backend);

  @override
  StatsSeries series(List<StatsSeriesSeriesInner> series) =>
      call(series: series);

  @override
  StatsSeries retainedFrom(DateTime? retainedFrom) =>
      call(retainedFrom: retainedFrom);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsSeries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsSeries(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StatsSeries call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? bucket = const $CopyWithPlaceholder(),
    Object? measure = const $CopyWithPlaceholder(),
    Object? groupBy = const $CopyWithPlaceholder(),
    Object? timeField = const $CopyWithPlaceholder(),
    Object? backend = const $CopyWithPlaceholder(),
    Object? series = const $CopyWithPlaceholder(),
    Object? retainedFrom = const $CopyWithPlaceholder(),
  }) {
    return StatsSeries(
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
      measure: measure == const $CopyWithPlaceholder() || measure == null
          ? _value.measure
          // ignore: cast_nullable_to_non_nullable
          : measure as StatsSeriesMeasureEnum,
      groupBy: groupBy == const $CopyWithPlaceholder()
          ? _value.groupBy
          // ignore: cast_nullable_to_non_nullable
          : groupBy as StatsSeriesGroupByEnum?,
      timeField: timeField == const $CopyWithPlaceholder()
          ? _value.timeField
          // ignore: cast_nullable_to_non_nullable
          : timeField as StatsSeriesTimeFieldEnum?,
      backend: backend == const $CopyWithPlaceholder() || backend == null
          ? _value.backend
          // ignore: cast_nullable_to_non_nullable
          : backend as StatsSeriesBackendEnum,
      series: series == const $CopyWithPlaceholder() || series == null
          ? _value.series
          // ignore: cast_nullable_to_non_nullable
          : series as List<StatsSeriesSeriesInner>,
      retainedFrom: retainedFrom == const $CopyWithPlaceholder()
          ? _value.retainedFrom
          // ignore: cast_nullable_to_non_nullable
          : retainedFrom as DateTime?,
    );
  }
}

extension $StatsSeriesCopyWith on StatsSeries {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStatsSeries.copyWith(...)` or `instanceOfStatsSeries.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StatsSeriesCWProxy get copyWith => _$StatsSeriesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StatsSeries _$StatsSeriesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StatsSeries', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'from',
          'to',
          'bucket',
          'measure',
          'backend',
          'series',
        ],
      );
      final val = StatsSeries(
        from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
        to: $checkedConvert('to', (v) => DateTime.parse(v as String)),
        bucket: $checkedConvert('bucket', (v) => v as String),
        measure: $checkedConvert(
          'measure',
          (v) => $enumDecode(_$StatsSeriesMeasureEnumEnumMap, v),
        ),
        groupBy: $checkedConvert(
          'groupBy',
          (v) => $enumDecodeNullable(_$StatsSeriesGroupByEnumEnumMap, v),
        ),
        timeField: $checkedConvert(
          'timeField',
          (v) => $enumDecodeNullable(_$StatsSeriesTimeFieldEnumEnumMap, v),
        ),
        backend: $checkedConvert(
          'backend',
          (v) => $enumDecode(_$StatsSeriesBackendEnumEnumMap, v),
        ),
        series: $checkedConvert(
          'series',
          (v) => (v as List<dynamic>)
              .map(
                (e) =>
                    StatsSeriesSeriesInner.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
        retainedFrom: $checkedConvert(
          'retainedFrom',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$StatsSeriesToJson(StatsSeries instance) =>
    <String, dynamic>{
      'from': instance.from.toIso8601String(),
      'to': instance.to.toIso8601String(),
      'bucket': instance.bucket,
      'measure': _$StatsSeriesMeasureEnumEnumMap[instance.measure]!,
      'groupBy': ?_$StatsSeriesGroupByEnumEnumMap[instance.groupBy],
      'timeField': ?_$StatsSeriesTimeFieldEnumEnumMap[instance.timeField],
      'backend': _$StatsSeriesBackendEnumEnumMap[instance.backend]!,
      'series': instance.series.map((e) => e.toJson()).toList(),
      'retainedFrom': ?instance.retainedFrom?.toIso8601String(),
    };

const _$StatsSeriesMeasureEnumEnumMap = {
  StatsSeriesMeasureEnum.versions: 'versions',
  StatsSeriesMeasureEnum.entities: 'entities',
  StatsSeriesMeasureEnum.firsts: 'firsts',
  StatsSeriesMeasureEnum.tombstones: 'tombstones',
  StatsSeriesMeasureEnum.edges: 'edges',
  StatsSeriesMeasureEnum.alerts: 'alerts',
};

const _$StatsSeriesGroupByEnumEnumMap = {
  StatsSeriesGroupByEnum.none: 'none',
  StatsSeriesGroupByEnum.source_: 'source',
  StatsSeriesGroupByEnum.kind: 'kind',
};

const _$StatsSeriesTimeFieldEnumEnumMap = {
  StatsSeriesTimeFieldEnum.ingested: 'ingested',
  StatsSeriesTimeFieldEnum.effective: 'effective',
  StatsSeriesTimeFieldEnum.observed: 'observed',
  StatsSeriesTimeFieldEnum.event: 'event',
};

const _$StatsSeriesBackendEnumEnumMap = {
  StatsSeriesBackendEnum.rollup: 'rollup',
  StatsSeriesBackendEnum.raw: 'raw',
  StatsSeriesBackendEnum.mixed: 'mixed',
};
