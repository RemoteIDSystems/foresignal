// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'backtest_job.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BacktestJobCWProxy {
  BacktestJob id(String id);

  BacktestJob type(BacktestJobTypeEnum type);

  BacktestJob targetId(String? targetId);

  BacktestJob definition(Map<String, Object>? definition);

  BacktestJob definitionHash(String definitionHash);

  BacktestJob fromSeq(int fromSeq);

  BacktestJob toSeq(int toSeq);

  BacktestJob from(DateTime? from);

  BacktestJob to(DateTime? to);

  BacktestJob status(BacktestJobStatusEnum status);

  BacktestJob progress(num progress);

  BacktestJob createdAt(DateTime createdAt);

  BacktestJob startedAt(DateTime? startedAt);

  BacktestJob finishedAt(DateTime? finishedAt);

  BacktestJob createdBy(String? createdBy);

  BacktestJob detectorVersion(String? detectorVersion);

  BacktestJob caveats(List<BacktestCaveat> caveats);

  BacktestJob result(BacktestJobResult? result);

  BacktestJob truncated(bool? truncated);

  BacktestJob error(String? error);

  BacktestJob manifestId(String? manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BacktestJob(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BacktestJob(...).copyWith(id: 12, name: "My name")
  /// ```
  BacktestJob call({
    String id,
    BacktestJobTypeEnum type,
    String? targetId,
    Map<String, Object>? definition,
    String definitionHash,
    int fromSeq,
    int toSeq,
    DateTime? from,
    DateTime? to,
    BacktestJobStatusEnum status,
    num progress,
    DateTime createdAt,
    DateTime? startedAt,
    DateTime? finishedAt,
    String? createdBy,
    String? detectorVersion,
    List<BacktestCaveat> caveats,
    BacktestJobResult? result,
    bool? truncated,
    String? error,
    String? manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBacktestJob.copyWith(...)` or call `instanceOfBacktestJob.copyWith.fieldName(value)` for a single field.
class _$BacktestJobCWProxyImpl implements _$BacktestJobCWProxy {
  const _$BacktestJobCWProxyImpl(this._value);

  final BacktestJob _value;

  @override
  BacktestJob id(String id) => call(id: id);

  @override
  BacktestJob type(BacktestJobTypeEnum type) => call(type: type);

  @override
  BacktestJob targetId(String? targetId) => call(targetId: targetId);

  @override
  BacktestJob definition(Map<String, Object>? definition) =>
      call(definition: definition);

  @override
  BacktestJob definitionHash(String definitionHash) =>
      call(definitionHash: definitionHash);

  @override
  BacktestJob fromSeq(int fromSeq) => call(fromSeq: fromSeq);

  @override
  BacktestJob toSeq(int toSeq) => call(toSeq: toSeq);

  @override
  BacktestJob from(DateTime? from) => call(from: from);

  @override
  BacktestJob to(DateTime? to) => call(to: to);

  @override
  BacktestJob status(BacktestJobStatusEnum status) => call(status: status);

  @override
  BacktestJob progress(num progress) => call(progress: progress);

  @override
  BacktestJob createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  BacktestJob startedAt(DateTime? startedAt) => call(startedAt: startedAt);

  @override
  BacktestJob finishedAt(DateTime? finishedAt) => call(finishedAt: finishedAt);

  @override
  BacktestJob createdBy(String? createdBy) => call(createdBy: createdBy);

  @override
  BacktestJob detectorVersion(String? detectorVersion) =>
      call(detectorVersion: detectorVersion);

  @override
  BacktestJob caveats(List<BacktestCaveat> caveats) => call(caveats: caveats);

  @override
  BacktestJob result(BacktestJobResult? result) => call(result: result);

  @override
  BacktestJob truncated(bool? truncated) => call(truncated: truncated);

  @override
  BacktestJob error(String? error) => call(error: error);

  @override
  BacktestJob manifestId(String? manifestId) => call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BacktestJob(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BacktestJob(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BacktestJob call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? targetId = const $CopyWithPlaceholder(),
    Object? definition = const $CopyWithPlaceholder(),
    Object? definitionHash = const $CopyWithPlaceholder(),
    Object? fromSeq = const $CopyWithPlaceholder(),
    Object? toSeq = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? progress = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? finishedAt = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? detectorVersion = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
    Object? result = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return BacktestJob(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as BacktestJobTypeEnum,
      targetId: targetId == const $CopyWithPlaceholder()
          ? _value.targetId
          // ignore: cast_nullable_to_non_nullable
          : targetId as String?,
      definition: definition == const $CopyWithPlaceholder()
          ? _value.definition
          // ignore: cast_nullable_to_non_nullable
          : definition as Map<String, Object>?,
      definitionHash:
          definitionHash == const $CopyWithPlaceholder() ||
              definitionHash == null
          ? _value.definitionHash
          // ignore: cast_nullable_to_non_nullable
          : definitionHash as String,
      fromSeq: fromSeq == const $CopyWithPlaceholder() || fromSeq == null
          ? _value.fromSeq
          // ignore: cast_nullable_to_non_nullable
          : fromSeq as int,
      toSeq: toSeq == const $CopyWithPlaceholder() || toSeq == null
          ? _value.toSeq
          // ignore: cast_nullable_to_non_nullable
          : toSeq as int,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as BacktestJobStatusEnum,
      progress: progress == const $CopyWithPlaceholder() || progress == null
          ? _value.progress
          // ignore: cast_nullable_to_non_nullable
          : progress as num,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      startedAt: startedAt == const $CopyWithPlaceholder()
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime?,
      finishedAt: finishedAt == const $CopyWithPlaceholder()
          ? _value.finishedAt
          // ignore: cast_nullable_to_non_nullable
          : finishedAt as DateTime?,
      createdBy: createdBy == const $CopyWithPlaceholder()
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as String?,
      detectorVersion: detectorVersion == const $CopyWithPlaceholder()
          ? _value.detectorVersion
          // ignore: cast_nullable_to_non_nullable
          : detectorVersion as String?,
      caveats: caveats == const $CopyWithPlaceholder() || caveats == null
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<BacktestCaveat>,
      result: result == const $CopyWithPlaceholder()
          ? _value.result
          // ignore: cast_nullable_to_non_nullable
          : result as BacktestJobResult?,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
    );
  }
}

extension $BacktestJobCopyWith on BacktestJob {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBacktestJob.copyWith(...)` or `instanceOfBacktestJob.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BacktestJobCWProxy get copyWith => _$BacktestJobCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BacktestJob _$BacktestJobFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('BacktestJob', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'type',
      'definitionHash',
      'fromSeq',
      'toSeq',
      'status',
      'progress',
      'createdAt',
      'caveats',
    ],
  );
  final val = BacktestJob(
    id: $checkedConvert('id', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$BacktestJobTypeEnumEnumMap, v),
    ),
    targetId: $checkedConvert('targetId', (v) => v as String?),
    definition: $checkedConvert(
      'definition',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    definitionHash: $checkedConvert('definitionHash', (v) => v as String),
    fromSeq: $checkedConvert('fromSeq', (v) => (v as num).toInt()),
    toSeq: $checkedConvert('toSeq', (v) => (v as num).toInt()),
    from: $checkedConvert(
      'from',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    to: $checkedConvert(
      'to',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$BacktestJobStatusEnumEnumMap, v),
    ),
    progress: $checkedConvert('progress', (v) => v as num),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    startedAt: $checkedConvert(
      'startedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    finishedAt: $checkedConvert(
      'finishedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    createdBy: $checkedConvert('createdBy', (v) => v as String?),
    detectorVersion: $checkedConvert('detectorVersion', (v) => v as String?),
    caveats: $checkedConvert(
      'caveats',
      (v) => (v as List<dynamic>)
          .map((e) => BacktestCaveat.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    result: $checkedConvert(
      'result',
      (v) => v == null
          ? null
          : BacktestJobResult.fromJson(v as Map<String, dynamic>),
    ),
    truncated: $checkedConvert('truncated', (v) => v as bool?),
    error: $checkedConvert('error', (v) => v as String?),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$BacktestJobToJson(BacktestJob instance) =>
    <String, dynamic>{
      'id': instance.id,
      'type': _$BacktestJobTypeEnumEnumMap[instance.type]!,
      'targetId': ?instance.targetId,
      'definition': ?instance.definition,
      'definitionHash': instance.definitionHash,
      'fromSeq': instance.fromSeq,
      'toSeq': instance.toSeq,
      'from': ?instance.from?.toIso8601String(),
      'to': ?instance.to?.toIso8601String(),
      'status': _$BacktestJobStatusEnumEnumMap[instance.status]!,
      'progress': instance.progress,
      'createdAt': instance.createdAt.toIso8601String(),
      'startedAt': ?instance.startedAt?.toIso8601String(),
      'finishedAt': ?instance.finishedAt?.toIso8601String(),
      'createdBy': ?instance.createdBy,
      'detectorVersion': ?instance.detectorVersion,
      'caveats': instance.caveats.map((e) => e.toJson()).toList(),
      'result': ?instance.result?.toJson(),
      'truncated': ?instance.truncated,
      'error': ?instance.error,
      'manifestId': ?instance.manifestId,
    };

const _$BacktestJobTypeEnumEnumMap = {
  BacktestJobTypeEnum.rule: 'rule',
  BacktestJobTypeEnum.recipe: 'recipe',
  BacktestJobTypeEnum.precursor: 'precursor',
  BacktestJobTypeEnum.quality: 'quality',
  BacktestJobTypeEnum.eventStudy: 'event-study',
  BacktestJobTypeEnum.precursorMining: 'precursor-mining',
  BacktestJobTypeEnum.forecastEvaluation: 'forecast-evaluation',
  BacktestJobTypeEnum.referenceImport: 'reference-import',
  BacktestJobTypeEnum.baseRateRefresh: 'base-rate-refresh',
  BacktestJobTypeEnum.manifestReplay: 'manifest-replay',
  BacktestJobTypeEnum.benchmark: 'benchmark',
};

const _$BacktestJobStatusEnumEnumMap = {
  BacktestJobStatusEnum.queued: 'queued',
  BacktestJobStatusEnum.running: 'running',
  BacktestJobStatusEnum.done: 'done',
  BacktestJobStatusEnum.failed: 'failed',
  BacktestJobStatusEnum.cancelled: 'cancelled',
};
