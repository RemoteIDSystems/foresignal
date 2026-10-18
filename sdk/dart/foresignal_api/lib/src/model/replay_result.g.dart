// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'replay_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReplayResultCWProxy {
  ReplayResult jobId(String jobId);

  ReplayResult manifestId(String manifestId);

  ReplayResult status(ReplayResultStatusEnum status);

  ReplayResult outcome(ReplayResultOutcomeEnum? outcome);

  ReplayResult reason(ReplayResultReasonEnum? reason);

  ReplayResult diffSummary(String? diffSummary);

  ReplayResult message(String? message);

  ReplayResult startedAt(DateTime? startedAt);

  ReplayResult finishedAt(DateTime? finishedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReplayResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReplayResult(...).copyWith(id: 12, name: "My name")
  /// ```
  ReplayResult call({
    String jobId,
    String manifestId,
    ReplayResultStatusEnum status,
    ReplayResultOutcomeEnum? outcome,
    ReplayResultReasonEnum? reason,
    String? diffSummary,
    String? message,
    DateTime? startedAt,
    DateTime? finishedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReplayResult.copyWith(...)` or call `instanceOfReplayResult.copyWith.fieldName(value)` for a single field.
class _$ReplayResultCWProxyImpl implements _$ReplayResultCWProxy {
  const _$ReplayResultCWProxyImpl(this._value);

  final ReplayResult _value;

  @override
  ReplayResult jobId(String jobId) => call(jobId: jobId);

  @override
  ReplayResult manifestId(String manifestId) => call(manifestId: manifestId);

  @override
  ReplayResult status(ReplayResultStatusEnum status) => call(status: status);

  @override
  ReplayResult outcome(ReplayResultOutcomeEnum? outcome) =>
      call(outcome: outcome);

  @override
  ReplayResult reason(ReplayResultReasonEnum? reason) => call(reason: reason);

  @override
  ReplayResult diffSummary(String? diffSummary) =>
      call(diffSummary: diffSummary);

  @override
  ReplayResult message(String? message) => call(message: message);

  @override
  ReplayResult startedAt(DateTime? startedAt) => call(startedAt: startedAt);

  @override
  ReplayResult finishedAt(DateTime? finishedAt) => call(finishedAt: finishedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReplayResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReplayResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReplayResult call({
    Object? jobId = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? outcome = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? diffSummary = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? finishedAt = const $CopyWithPlaceholder(),
  }) {
    return ReplayResult(
      jobId: jobId == const $CopyWithPlaceholder() || jobId == null
          ? _value.jobId
          // ignore: cast_nullable_to_non_nullable
          : jobId as String,
      manifestId:
          manifestId == const $CopyWithPlaceholder() || manifestId == null
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ReplayResultStatusEnum,
      outcome: outcome == const $CopyWithPlaceholder()
          ? _value.outcome
          // ignore: cast_nullable_to_non_nullable
          : outcome as ReplayResultOutcomeEnum?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as ReplayResultReasonEnum?,
      diffSummary: diffSummary == const $CopyWithPlaceholder()
          ? _value.diffSummary
          // ignore: cast_nullable_to_non_nullable
          : diffSummary as String?,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      startedAt: startedAt == const $CopyWithPlaceholder()
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime?,
      finishedAt: finishedAt == const $CopyWithPlaceholder()
          ? _value.finishedAt
          // ignore: cast_nullable_to_non_nullable
          : finishedAt as DateTime?,
    );
  }
}

extension $ReplayResultCopyWith on ReplayResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReplayResult.copyWith(...)` or `instanceOfReplayResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReplayResultCWProxy get copyWith => _$ReplayResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReplayResult _$ReplayResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReplayResult', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['jobId', 'manifestId', 'status']);
      final val = ReplayResult(
        jobId: $checkedConvert('jobId', (v) => v as String),
        manifestId: $checkedConvert('manifestId', (v) => v as String),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$ReplayResultStatusEnumEnumMap, v),
        ),
        outcome: $checkedConvert(
          'outcome',
          (v) => $enumDecodeNullable(_$ReplayResultOutcomeEnumEnumMap, v),
        ),
        reason: $checkedConvert(
          'reason',
          (v) => $enumDecodeNullable(_$ReplayResultReasonEnumEnumMap, v),
        ),
        diffSummary: $checkedConvert('diffSummary', (v) => v as String?),
        message: $checkedConvert('message', (v) => v as String?),
        startedAt: $checkedConvert(
          'startedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        finishedAt: $checkedConvert(
          'finishedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ReplayResultToJson(ReplayResult instance) =>
    <String, dynamic>{
      'jobId': instance.jobId,
      'manifestId': instance.manifestId,
      'status': _$ReplayResultStatusEnumEnumMap[instance.status]!,
      'outcome': ?_$ReplayResultOutcomeEnumEnumMap[instance.outcome],
      'reason': ?_$ReplayResultReasonEnumEnumMap[instance.reason],
      'diffSummary': ?instance.diffSummary,
      'message': ?instance.message,
      'startedAt': ?instance.startedAt?.toIso8601String(),
      'finishedAt': ?instance.finishedAt?.toIso8601String(),
    };

const _$ReplayResultStatusEnumEnumMap = {
  ReplayResultStatusEnum.queued: 'queued',
  ReplayResultStatusEnum.running: 'running',
  ReplayResultStatusEnum.done: 'done',
  ReplayResultStatusEnum.failed: 'failed',
};

const _$ReplayResultOutcomeEnumEnumMap = {
  ReplayResultOutcomeEnum.identical: 'identical',
  ReplayResultOutcomeEnum.differs: 'differs',
  ReplayResultOutcomeEnum.notReplayable: 'not-replayable',
};

const _$ReplayResultReasonEnumEnumMap = {
  ReplayResultReasonEnum.inputsPurged: 'inputs-purged',
  ReplayResultReasonEnum.componentRemoved: 'component-removed',
  ReplayResultReasonEnum.llmOutput: 'llm-output',
};
