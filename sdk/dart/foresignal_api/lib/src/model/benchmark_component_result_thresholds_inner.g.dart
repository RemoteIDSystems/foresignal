// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'benchmark_component_result_thresholds_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BenchmarkComponentResultThresholdsInnerCWProxy {
  BenchmarkComponentResultThresholdsInner metric(String metric);

  BenchmarkComponentResultThresholdsInner comparator(
    BenchmarkComponentResultThresholdsInnerComparatorEnum comparator,
  );

  BenchmarkComponentResultThresholdsInner threshold(num threshold);

  BenchmarkComponentResultThresholdsInner observed(num? observed);

  BenchmarkComponentResultThresholdsInner status(
    BenchmarkComponentResultThresholdsInnerStatusEnum status,
  );

  BenchmarkComponentResultThresholdsInner warn(num? warn);

  BenchmarkComponentResultThresholdsInner scenario(String? scenario);

  BenchmarkComponentResultThresholdsInner note(String? note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkComponentResultThresholdsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkComponentResultThresholdsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  BenchmarkComponentResultThresholdsInner call({
    String metric,
    BenchmarkComponentResultThresholdsInnerComparatorEnum comparator,
    num threshold,
    num? observed,
    BenchmarkComponentResultThresholdsInnerStatusEnum status,
    num? warn,
    String? scenario,
    String? note,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBenchmarkComponentResultThresholdsInner.copyWith(...)` or call `instanceOfBenchmarkComponentResultThresholdsInner.copyWith.fieldName(value)` for a single field.
class _$BenchmarkComponentResultThresholdsInnerCWProxyImpl
    implements _$BenchmarkComponentResultThresholdsInnerCWProxy {
  const _$BenchmarkComponentResultThresholdsInnerCWProxyImpl(this._value);

  final BenchmarkComponentResultThresholdsInner _value;

  @override
  BenchmarkComponentResultThresholdsInner metric(String metric) =>
      call(metric: metric);

  @override
  BenchmarkComponentResultThresholdsInner comparator(
    BenchmarkComponentResultThresholdsInnerComparatorEnum comparator,
  ) => call(comparator: comparator);

  @override
  BenchmarkComponentResultThresholdsInner threshold(num threshold) =>
      call(threshold: threshold);

  @override
  BenchmarkComponentResultThresholdsInner observed(num? observed) =>
      call(observed: observed);

  @override
  BenchmarkComponentResultThresholdsInner status(
    BenchmarkComponentResultThresholdsInnerStatusEnum status,
  ) => call(status: status);

  @override
  BenchmarkComponentResultThresholdsInner warn(num? warn) => call(warn: warn);

  @override
  BenchmarkComponentResultThresholdsInner scenario(String? scenario) =>
      call(scenario: scenario);

  @override
  BenchmarkComponentResultThresholdsInner note(String? note) =>
      call(note: note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkComponentResultThresholdsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkComponentResultThresholdsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BenchmarkComponentResultThresholdsInner call({
    Object? metric = const $CopyWithPlaceholder(),
    Object? comparator = const $CopyWithPlaceholder(),
    Object? threshold = const $CopyWithPlaceholder(),
    Object? observed = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? warn = const $CopyWithPlaceholder(),
    Object? scenario = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
  }) {
    return BenchmarkComponentResultThresholdsInner(
      metric: metric == const $CopyWithPlaceholder() || metric == null
          ? _value.metric
          // ignore: cast_nullable_to_non_nullable
          : metric as String,
      comparator:
          comparator == const $CopyWithPlaceholder() || comparator == null
          ? _value.comparator
          // ignore: cast_nullable_to_non_nullable
          : comparator as BenchmarkComponentResultThresholdsInnerComparatorEnum,
      threshold: threshold == const $CopyWithPlaceholder() || threshold == null
          ? _value.threshold
          // ignore: cast_nullable_to_non_nullable
          : threshold as num,
      observed: observed == const $CopyWithPlaceholder()
          ? _value.observed
          // ignore: cast_nullable_to_non_nullable
          : observed as num?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as BenchmarkComponentResultThresholdsInnerStatusEnum,
      warn: warn == const $CopyWithPlaceholder()
          ? _value.warn
          // ignore: cast_nullable_to_non_nullable
          : warn as num?,
      scenario: scenario == const $CopyWithPlaceholder()
          ? _value.scenario
          // ignore: cast_nullable_to_non_nullable
          : scenario as String?,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
    );
  }
}

extension $BenchmarkComponentResultThresholdsInnerCopyWith
    on BenchmarkComponentResultThresholdsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBenchmarkComponentResultThresholdsInner.copyWith(...)` or `instanceOfBenchmarkComponentResultThresholdsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BenchmarkComponentResultThresholdsInnerCWProxy get copyWith =>
      _$BenchmarkComponentResultThresholdsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BenchmarkComponentResultThresholdsInner
_$BenchmarkComponentResultThresholdsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BenchmarkComponentResultThresholdsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(
        json,
        requiredKeys: const ['metric', 'comparator', 'threshold', 'status'],
      );
      final val = BenchmarkComponentResultThresholdsInner(
        metric: $checkedConvert('metric', (v) => v as String),
        comparator: $checkedConvert(
          'comparator',
          (v) => $enumDecode(
            _$BenchmarkComponentResultThresholdsInnerComparatorEnumEnumMap,
            v,
          ),
        ),
        threshold: $checkedConvert('threshold', (v) => v as num),
        observed: $checkedConvert('observed', (v) => v as num?),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(
            _$BenchmarkComponentResultThresholdsInnerStatusEnumEnumMap,
            v,
          ),
        ),
        warn: $checkedConvert('warn', (v) => v as num?),
        scenario: $checkedConvert('scenario', (v) => v as String?),
        note: $checkedConvert('note', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$BenchmarkComponentResultThresholdsInnerToJson(
  BenchmarkComponentResultThresholdsInner instance,
) => <String, dynamic>{
  'metric': instance.metric,
  'comparator':
      _$BenchmarkComponentResultThresholdsInnerComparatorEnumEnumMap[instance
          .comparator]!,
  'threshold': instance.threshold,
  'observed': ?instance.observed,
  'status':
      _$BenchmarkComponentResultThresholdsInnerStatusEnumEnumMap[instance
          .status]!,
  'warn': ?instance.warn,
  'scenario': ?instance.scenario,
  'note': ?instance.note,
};

const _$BenchmarkComponentResultThresholdsInnerComparatorEnumEnumMap = {
  BenchmarkComponentResultThresholdsInnerComparatorEnum.le: 'le',
  BenchmarkComponentResultThresholdsInnerComparatorEnum.ge: 'ge',
  BenchmarkComponentResultThresholdsInnerComparatorEnum.eq: 'eq',
  BenchmarkComponentResultThresholdsInnerComparatorEnum.within: 'within',
};

const _$BenchmarkComponentResultThresholdsInnerStatusEnumEnumMap = {
  BenchmarkComponentResultThresholdsInnerStatusEnum.pass: 'pass',
  BenchmarkComponentResultThresholdsInnerStatusEnum.warn: 'warn',
  BenchmarkComponentResultThresholdsInnerStatusEnum.fail: 'fail',
};
