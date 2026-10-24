// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'maintenance_run_steps_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MaintenanceRunStepsInnerCWProxy {
  MaintenanceRunStepsInner step(MaintenanceRunStepsInnerStepEnum step);

  MaintenanceRunStepsInner status(MaintenanceRunStepsInnerStatusEnum status);

  MaintenanceRunStepsInner startedAt(DateTime? startedAt);

  MaintenanceRunStepsInner finishedAt(DateTime? finishedAt);

  MaintenanceRunStepsInner jobId(String? jobId);

  MaintenanceRunStepsInner result(Map<String, Object>? result);

  MaintenanceRunStepsInner error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MaintenanceRunStepsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MaintenanceRunStepsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  MaintenanceRunStepsInner call({
    MaintenanceRunStepsInnerStepEnum step,
    MaintenanceRunStepsInnerStatusEnum status,
    DateTime? startedAt,
    DateTime? finishedAt,
    String? jobId,
    Map<String, Object>? result,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMaintenanceRunStepsInner.copyWith(...)` or call `instanceOfMaintenanceRunStepsInner.copyWith.fieldName(value)` for a single field.
class _$MaintenanceRunStepsInnerCWProxyImpl
    implements _$MaintenanceRunStepsInnerCWProxy {
  const _$MaintenanceRunStepsInnerCWProxyImpl(this._value);

  final MaintenanceRunStepsInner _value;

  @override
  MaintenanceRunStepsInner step(MaintenanceRunStepsInnerStepEnum step) =>
      call(step: step);

  @override
  MaintenanceRunStepsInner status(MaintenanceRunStepsInnerStatusEnum status) =>
      call(status: status);

  @override
  MaintenanceRunStepsInner startedAt(DateTime? startedAt) =>
      call(startedAt: startedAt);

  @override
  MaintenanceRunStepsInner finishedAt(DateTime? finishedAt) =>
      call(finishedAt: finishedAt);

  @override
  MaintenanceRunStepsInner jobId(String? jobId) => call(jobId: jobId);

  @override
  MaintenanceRunStepsInner result(Map<String, Object>? result) =>
      call(result: result);

  @override
  MaintenanceRunStepsInner error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MaintenanceRunStepsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MaintenanceRunStepsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MaintenanceRunStepsInner call({
    Object? step = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? finishedAt = const $CopyWithPlaceholder(),
    Object? jobId = const $CopyWithPlaceholder(),
    Object? result = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return MaintenanceRunStepsInner(
      step: step == const $CopyWithPlaceholder() || step == null
          ? _value.step
          // ignore: cast_nullable_to_non_nullable
          : step as MaintenanceRunStepsInnerStepEnum,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as MaintenanceRunStepsInnerStatusEnum,
      startedAt: startedAt == const $CopyWithPlaceholder()
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime?,
      finishedAt: finishedAt == const $CopyWithPlaceholder()
          ? _value.finishedAt
          // ignore: cast_nullable_to_non_nullable
          : finishedAt as DateTime?,
      jobId: jobId == const $CopyWithPlaceholder()
          ? _value.jobId
          // ignore: cast_nullable_to_non_nullable
          : jobId as String?,
      result: result == const $CopyWithPlaceholder()
          ? _value.result
          // ignore: cast_nullable_to_non_nullable
          : result as Map<String, Object>?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $MaintenanceRunStepsInnerCopyWith on MaintenanceRunStepsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMaintenanceRunStepsInner.copyWith(...)` or `instanceOfMaintenanceRunStepsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MaintenanceRunStepsInnerCWProxy get copyWith =>
      _$MaintenanceRunStepsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MaintenanceRunStepsInner _$MaintenanceRunStepsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MaintenanceRunStepsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['step', 'status']);
  final val = MaintenanceRunStepsInner(
    step: $checkedConvert(
      'step',
      (v) => $enumDecode(_$MaintenanceRunStepsInnerStepEnumEnumMap, v),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$MaintenanceRunStepsInnerStatusEnumEnumMap, v),
    ),
    startedAt: $checkedConvert(
      'startedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    finishedAt: $checkedConvert(
      'finishedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    jobId: $checkedConvert('jobId', (v) => v as String?),
    result: $checkedConvert(
      'result',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    error: $checkedConvert('error', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$MaintenanceRunStepsInnerToJson(
  MaintenanceRunStepsInner instance,
) => <String, dynamic>{
  'step': _$MaintenanceRunStepsInnerStepEnumEnumMap[instance.step]!,
  'status': _$MaintenanceRunStepsInnerStatusEnumEnumMap[instance.status]!,
  'startedAt': ?instance.startedAt?.toIso8601String(),
  'finishedAt': ?instance.finishedAt?.toIso8601String(),
  'jobId': ?instance.jobId,
  'result': ?instance.result,
  'error': ?instance.error,
};

const _$MaintenanceRunStepsInnerStepEnumEnumMap = {
  MaintenanceRunStepsInnerStepEnum.observabilityReestimation:
      'observability-reestimation',
  MaintenanceRunStepsInnerStepEnum.benchmarkHistorical: 'benchmark-historical',
  MaintenanceRunStepsInnerStepEnum.baseRateRefresh: 'base-rate-refresh',
  MaintenanceRunStepsInnerStepEnum.precursorRetest: 'precursor-retest',
  MaintenanceRunStepsInnerStepEnum.plattRefit: 'platt-refit',
  MaintenanceRunStepsInnerStepEnum.ensembleWeights: 'ensemble-weights',
  MaintenanceRunStepsInnerStepEnum.extremizingCheck: 'extremizing-check',
  MaintenanceRunStepsInnerStepEnum.indexWeightFit: 'index-weight-fit',
  MaintenanceRunStepsInnerStepEnum.seriesMethodReselection:
      'series-method-reselection',
  MaintenanceRunStepsInnerStepEnum.hypothesisArchivalProposals:
      'hypothesis-archival-proposals',
  MaintenanceRunStepsInnerStepEnum.llmRetrospectiveEvaluation:
      'llm-retrospective-evaluation',
};

const _$MaintenanceRunStepsInnerStatusEnumEnumMap = {
  MaintenanceRunStepsInnerStatusEnum.pending: 'pending',
  MaintenanceRunStepsInnerStatusEnum.running: 'running',
  MaintenanceRunStepsInnerStatusEnum.done: 'done',
  MaintenanceRunStepsInnerStatusEnum.failed: 'failed',
  MaintenanceRunStepsInnerStatusEnum.skipped: 'skipped',
};
