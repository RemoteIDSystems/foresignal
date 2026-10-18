// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_resolver.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastResolverCWProxy {
  ForecastResolver type(ForecastResolverTypeEnum type);

  ForecastResolver selector(EventSelector? selector);

  ForecastResolver area(ResolverArea? area);

  ForecastResolver window(TimeRange? window);

  ForecastResolver op(ForecastResolverOpEnum? op);

  ForecastResolver threshold(num? threshold);

  ForecastResolver ref(String? ref);

  ForecastResolver period(String? period);

  ForecastResolver aggregate(ForecastResolverAggregateEnum? aggregate);

  ForecastResolver ruleId(String? ruleId);

  ForecastResolver situationId(String? situationId);

  ForecastResolver status(SituationStatus? status);

  ForecastResolver minSeverity(Severity? minSeverity);

  ForecastResolver setId(String? setId);

  ForecastResolver version(String? version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastResolver(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastResolver(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastResolver call({
    ForecastResolverTypeEnum type,
    EventSelector? selector,
    ResolverArea? area,
    TimeRange? window,
    ForecastResolverOpEnum? op,
    num? threshold,
    String? ref,
    String? period,
    ForecastResolverAggregateEnum? aggregate,
    String? ruleId,
    String? situationId,
    SituationStatus? status,
    Severity? minSeverity,
    String? setId,
    String? version,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastResolver.copyWith(...)` or call `instanceOfForecastResolver.copyWith.fieldName(value)` for a single field.
class _$ForecastResolverCWProxyImpl implements _$ForecastResolverCWProxy {
  const _$ForecastResolverCWProxyImpl(this._value);

  final ForecastResolver _value;

  @override
  ForecastResolver type(ForecastResolverTypeEnum type) => call(type: type);

  @override
  ForecastResolver selector(EventSelector? selector) =>
      call(selector: selector);

  @override
  ForecastResolver area(ResolverArea? area) => call(area: area);

  @override
  ForecastResolver window(TimeRange? window) => call(window: window);

  @override
  ForecastResolver op(ForecastResolverOpEnum? op) => call(op: op);

  @override
  ForecastResolver threshold(num? threshold) => call(threshold: threshold);

  @override
  ForecastResolver ref(String? ref) => call(ref: ref);

  @override
  ForecastResolver period(String? period) => call(period: period);

  @override
  ForecastResolver aggregate(ForecastResolverAggregateEnum? aggregate) =>
      call(aggregate: aggregate);

  @override
  ForecastResolver ruleId(String? ruleId) => call(ruleId: ruleId);

  @override
  ForecastResolver situationId(String? situationId) =>
      call(situationId: situationId);

  @override
  ForecastResolver status(SituationStatus? status) => call(status: status);

  @override
  ForecastResolver minSeverity(Severity? minSeverity) =>
      call(minSeverity: minSeverity);

  @override
  ForecastResolver setId(String? setId) => call(setId: setId);

  @override
  ForecastResolver version(String? version) => call(version: version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastResolver(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastResolver(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastResolver call({
    Object? type = const $CopyWithPlaceholder(),
    Object? selector = const $CopyWithPlaceholder(),
    Object? area = const $CopyWithPlaceholder(),
    Object? window = const $CopyWithPlaceholder(),
    Object? op = const $CopyWithPlaceholder(),
    Object? threshold = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? period = const $CopyWithPlaceholder(),
    Object? aggregate = const $CopyWithPlaceholder(),
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? situationId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? minSeverity = const $CopyWithPlaceholder(),
    Object? setId = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
  }) {
    return ForecastResolver(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ForecastResolverTypeEnum,
      selector: selector == const $CopyWithPlaceholder()
          ? _value.selector
          // ignore: cast_nullable_to_non_nullable
          : selector as EventSelector?,
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as ResolverArea?,
      window: window == const $CopyWithPlaceholder()
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as TimeRange?,
      op: op == const $CopyWithPlaceholder()
          ? _value.op
          // ignore: cast_nullable_to_non_nullable
          : op as ForecastResolverOpEnum?,
      threshold: threshold == const $CopyWithPlaceholder()
          ? _value.threshold
          // ignore: cast_nullable_to_non_nullable
          : threshold as num?,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      period: period == const $CopyWithPlaceholder()
          ? _value.period
          // ignore: cast_nullable_to_non_nullable
          : period as String?,
      aggregate: aggregate == const $CopyWithPlaceholder()
          ? _value.aggregate
          // ignore: cast_nullable_to_non_nullable
          : aggregate as ForecastResolverAggregateEnum?,
      ruleId: ruleId == const $CopyWithPlaceholder()
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String?,
      situationId: situationId == const $CopyWithPlaceholder()
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SituationStatus?,
      minSeverity: minSeverity == const $CopyWithPlaceholder()
          ? _value.minSeverity
          // ignore: cast_nullable_to_non_nullable
          : minSeverity as Severity?,
      setId: setId == const $CopyWithPlaceholder()
          ? _value.setId
          // ignore: cast_nullable_to_non_nullable
          : setId as String?,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String?,
    );
  }
}

extension $ForecastResolverCopyWith on ForecastResolver {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastResolver.copyWith(...)` or `instanceOfForecastResolver.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastResolverCWProxy get copyWith => _$ForecastResolverCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastResolver _$ForecastResolverFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastResolver', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type']);
  final val = ForecastResolver(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ForecastResolverTypeEnumEnumMap, v),
    ),
    selector: $checkedConvert(
      'selector',
      (v) =>
          v == null ? null : EventSelector.fromJson(v as Map<String, dynamic>),
    ),
    area: $checkedConvert(
      'area',
      (v) =>
          v == null ? null : ResolverArea.fromJson(v as Map<String, dynamic>),
    ),
    window: $checkedConvert(
      'window',
      (v) => v == null ? null : TimeRange.fromJson(v as Map<String, dynamic>),
    ),
    op: $checkedConvert(
      'op',
      (v) => $enumDecodeNullable(_$ForecastResolverOpEnumEnumMap, v),
    ),
    threshold: $checkedConvert('threshold', (v) => v as num?),
    ref: $checkedConvert('ref', (v) => v as String?),
    period: $checkedConvert('period', (v) => v as String?),
    aggregate: $checkedConvert(
      'aggregate',
      (v) => $enumDecodeNullable(_$ForecastResolverAggregateEnumEnumMap, v),
    ),
    ruleId: $checkedConvert('ruleId', (v) => v as String?),
    situationId: $checkedConvert('situationId', (v) => v as String?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecodeNullable(_$SituationStatusEnumMap, v),
    ),
    minSeverity: $checkedConvert(
      'minSeverity',
      (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
    ),
    setId: $checkedConvert('setId', (v) => v as String?),
    version: $checkedConvert('version', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ForecastResolverToJson(ForecastResolver instance) =>
    <String, dynamic>{
      'type': _$ForecastResolverTypeEnumEnumMap[instance.type]!,
      'selector': ?instance.selector?.toJson(),
      'area': ?instance.area?.toJson(),
      'window': ?instance.window?.toJson(),
      'op': ?_$ForecastResolverOpEnumEnumMap[instance.op],
      'threshold': ?instance.threshold,
      'ref': ?instance.ref,
      'period': ?instance.period,
      'aggregate': ?_$ForecastResolverAggregateEnumEnumMap[instance.aggregate],
      'ruleId': ?instance.ruleId,
      'situationId': ?instance.situationId,
      'status': ?_$SituationStatusEnumMap[instance.status],
      'minSeverity': ?_$SeverityEnumMap[instance.minSeverity],
      'setId': ?instance.setId,
      'version': ?instance.version,
    };

const _$ForecastResolverTypeEnumEnumMap = {
  ForecastResolverTypeEnum.eventExists: 'event-exists',
  ForecastResolverTypeEnum.countThreshold: 'count-threshold',
  ForecastResolverTypeEnum.seriesValue: 'series-value',
  ForecastResolverTypeEnum.alertFired: 'alert-fired',
  ForecastResolverTypeEnum.situationStatus: 'situation-status',
  ForecastResolverTypeEnum.hypothesisOutcome: 'hypothesis-outcome',
  ForecastResolverTypeEnum.manual: 'manual',
};

const _$ForecastResolverOpEnumEnumMap = {
  ForecastResolverOpEnum.gt: 'gt',
  ForecastResolverOpEnum.gte: 'gte',
  ForecastResolverOpEnum.lt: 'lt',
  ForecastResolverOpEnum.lte: 'lte',
  ForecastResolverOpEnum.eq: 'eq',
};

const _$ForecastResolverAggregateEnumEnumMap = {
  ForecastResolverAggregateEnum.last: 'last',
  ForecastResolverAggregateEnum.mean: 'mean',
  ForecastResolverAggregateEnum.max: 'max',
  ForecastResolverAggregateEnum.sum: 'sum',
};

const _$SituationStatusEnumMap = {
  SituationStatus.emerging: 'emerging',
  SituationStatus.active: 'active',
  SituationStatus.dormant: 'dormant',
  SituationStatus.closed: 'closed',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};
