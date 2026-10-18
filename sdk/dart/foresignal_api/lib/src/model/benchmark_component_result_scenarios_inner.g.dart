// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'benchmark_component_result_scenarios_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BenchmarkComponentResultScenariosInnerCWProxy {
  BenchmarkComponentResultScenariosInner scenario(
    BenchmarkComponentResultScenariosInnerScenarioEnum scenario,
  );

  BenchmarkComponentResultScenariosInner caseId(String? caseId);

  BenchmarkComponentResultScenariosInner seed(int? seed);

  BenchmarkComponentResultScenariosInner status(
    BenchmarkComponentResultScenariosInnerStatusEnum status,
  );

  BenchmarkComponentResultScenariosInner metrics(Map<String, num?> metrics);

  BenchmarkComponentResultScenariosInner thresholds(
    List<BenchmarkComponentResultThresholdsInner>? thresholds,
  );

  BenchmarkComponentResultScenariosInner truth(Map<String, Object>? truth);

  BenchmarkComponentResultScenariosInner notes(List<String>? notes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkComponentResultScenariosInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkComponentResultScenariosInner(...).copyWith(id: 12, name: "My name")
  /// ```
  BenchmarkComponentResultScenariosInner call({
    BenchmarkComponentResultScenariosInnerScenarioEnum scenario,
    String? caseId,
    int? seed,
    BenchmarkComponentResultScenariosInnerStatusEnum status,
    Map<String, num?> metrics,
    List<BenchmarkComponentResultThresholdsInner>? thresholds,
    Map<String, Object>? truth,
    List<String>? notes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBenchmarkComponentResultScenariosInner.copyWith(...)` or call `instanceOfBenchmarkComponentResultScenariosInner.copyWith.fieldName(value)` for a single field.
class _$BenchmarkComponentResultScenariosInnerCWProxyImpl
    implements _$BenchmarkComponentResultScenariosInnerCWProxy {
  const _$BenchmarkComponentResultScenariosInnerCWProxyImpl(this._value);

  final BenchmarkComponentResultScenariosInner _value;

  @override
  BenchmarkComponentResultScenariosInner scenario(
    BenchmarkComponentResultScenariosInnerScenarioEnum scenario,
  ) => call(scenario: scenario);

  @override
  BenchmarkComponentResultScenariosInner caseId(String? caseId) =>
      call(caseId: caseId);

  @override
  BenchmarkComponentResultScenariosInner seed(int? seed) => call(seed: seed);

  @override
  BenchmarkComponentResultScenariosInner status(
    BenchmarkComponentResultScenariosInnerStatusEnum status,
  ) => call(status: status);

  @override
  BenchmarkComponentResultScenariosInner metrics(Map<String, num?> metrics) =>
      call(metrics: metrics);

  @override
  BenchmarkComponentResultScenariosInner thresholds(
    List<BenchmarkComponentResultThresholdsInner>? thresholds,
  ) => call(thresholds: thresholds);

  @override
  BenchmarkComponentResultScenariosInner truth(Map<String, Object>? truth) =>
      call(truth: truth);

  @override
  BenchmarkComponentResultScenariosInner notes(List<String>? notes) =>
      call(notes: notes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkComponentResultScenariosInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkComponentResultScenariosInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BenchmarkComponentResultScenariosInner call({
    Object? scenario = const $CopyWithPlaceholder(),
    Object? caseId = const $CopyWithPlaceholder(),
    Object? seed = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? metrics = const $CopyWithPlaceholder(),
    Object? thresholds = const $CopyWithPlaceholder(),
    Object? truth = const $CopyWithPlaceholder(),
    Object? notes = const $CopyWithPlaceholder(),
  }) {
    return BenchmarkComponentResultScenariosInner(
      scenario: scenario == const $CopyWithPlaceholder() || scenario == null
          ? _value.scenario
          // ignore: cast_nullable_to_non_nullable
          : scenario as BenchmarkComponentResultScenariosInnerScenarioEnum,
      caseId: caseId == const $CopyWithPlaceholder()
          ? _value.caseId
          // ignore: cast_nullable_to_non_nullable
          : caseId as String?,
      seed: seed == const $CopyWithPlaceholder()
          ? _value.seed
          // ignore: cast_nullable_to_non_nullable
          : seed as int?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as BenchmarkComponentResultScenariosInnerStatusEnum,
      metrics: metrics == const $CopyWithPlaceholder() || metrics == null
          ? _value.metrics
          // ignore: cast_nullable_to_non_nullable
          : metrics as Map<String, num?>,
      thresholds: thresholds == const $CopyWithPlaceholder()
          ? _value.thresholds
          // ignore: cast_nullable_to_non_nullable
          : thresholds as List<BenchmarkComponentResultThresholdsInner>?,
      truth: truth == const $CopyWithPlaceholder()
          ? _value.truth
          // ignore: cast_nullable_to_non_nullable
          : truth as Map<String, Object>?,
      notes: notes == const $CopyWithPlaceholder()
          ? _value.notes
          // ignore: cast_nullable_to_non_nullable
          : notes as List<String>?,
    );
  }
}

extension $BenchmarkComponentResultScenariosInnerCopyWith
    on BenchmarkComponentResultScenariosInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBenchmarkComponentResultScenariosInner.copyWith(...)` or `instanceOfBenchmarkComponentResultScenariosInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BenchmarkComponentResultScenariosInnerCWProxy get copyWith =>
      _$BenchmarkComponentResultScenariosInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BenchmarkComponentResultScenariosInner
_$BenchmarkComponentResultScenariosInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BenchmarkComponentResultScenariosInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['scenario', 'status', 'metrics']);
      final val = BenchmarkComponentResultScenariosInner(
        scenario: $checkedConvert(
          'scenario',
          (v) => $enumDecode(
            _$BenchmarkComponentResultScenariosInnerScenarioEnumEnumMap,
            v,
          ),
        ),
        caseId: $checkedConvert('caseId', (v) => v as String?),
        seed: $checkedConvert('seed', (v) => (v as num?)?.toInt()),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(
            _$BenchmarkComponentResultScenariosInnerStatusEnumEnumMap,
            v,
          ),
        ),
        metrics: $checkedConvert(
          'metrics',
          (v) => Map<String, num?>.from(v as Map),
        ),
        thresholds: $checkedConvert(
          'thresholds',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => BenchmarkComponentResultThresholdsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        truth: $checkedConvert(
          'truth',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        notes: $checkedConvert(
          'notes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$BenchmarkComponentResultScenariosInnerToJson(
  BenchmarkComponentResultScenariosInner instance,
) => <String, dynamic>{
  'scenario':
      _$BenchmarkComponentResultScenariosInnerScenarioEnumEnumMap[instance
          .scenario]!,
  'caseId': ?instance.caseId,
  'seed': ?instance.seed,
  'status':
      _$BenchmarkComponentResultScenariosInnerStatusEnumEnumMap[instance
          .status]!,
  'metrics': instance.metrics,
  'thresholds': ?instance.thresholds?.map((e) => e.toJson()).toList(),
  'truth': ?instance.truth,
  'notes': ?instance.notes,
};

const _$BenchmarkComponentResultScenariosInnerScenarioEnumEnumMap = {
  BenchmarkComponentResultScenariosInnerScenarioEnum.nullSeasonal:
      'null-seasonal',
  BenchmarkComponentResultScenariosInnerScenarioEnum.stepChange: 'step-change',
  BenchmarkComponentResultScenariosInnerScenarioEnum.spike: 'spike',
  BenchmarkComponentResultScenariosInnerScenarioEnum.coverageOutage:
      'coverage-outage',
  BenchmarkComponentResultScenariosInnerScenarioEnum.duplicateEcho:
      'duplicate-echo',
  BenchmarkComponentResultScenariosInnerScenarioEnum.lateRevision:
      'late-revision',
  BenchmarkComponentResultScenariosInnerScenarioEnum.commonDriver:
      'common-driver',
  BenchmarkComponentResultScenariosInnerScenarioEnum.truePrecursor:
      'true-precursor',
  BenchmarkComponentResultScenariosInnerScenarioEnum.baseRateDrift:
      'base-rate-drift',
  BenchmarkComponentResultScenariosInnerScenarioEnum.preEventMove:
      'pre-event-move',
  BenchmarkComponentResultScenariosInnerScenarioEnum.historical: 'historical',
};

const _$BenchmarkComponentResultScenariosInnerStatusEnumEnumMap = {
  BenchmarkComponentResultScenariosInnerStatusEnum.pass: 'pass',
  BenchmarkComponentResultScenariosInnerStatusEnum.warn: 'warn',
  BenchmarkComponentResultScenariosInnerStatusEnum.fail: 'fail',
};
