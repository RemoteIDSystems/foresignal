// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_batch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorBatchCWProxy {
  PrecursorBatch id(String id);

  PrecursorBatch jobId(String? jobId);

  PrecursorBatch mode(PrecursorBatchModeEnum mode);

  PrecursorBatch trigger(PrecursorBatchTriggerEnum? trigger);

  PrecursorBatch status(PrecursorBatchStatusEnum status);

  PrecursorBatch startedAt(DateTime startedAt);

  PrecursorBatch finishedAt(DateTime? finishedAt);

  PrecursorBatch candidatesGenerated(int candidatesGenerated);

  PrecursorBatch tested(int tested);

  PrecursorBatch capped(int capped);

  PrecursorBatch insufficient(int? insufficient);

  PrecursorBatch survivors(int survivors);

  PrecursorBatch validated(int validated);

  PrecursorBatch q(num q);

  PrecursorBatch method(PrecursorBatchMethodEnum method);

  PrecursorBatch windows(Map<String, Object>? windows);

  PrecursorBatch error(String? error);

  PrecursorBatch manifestId(String? manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorBatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorBatch(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorBatch call({
    String id,
    String? jobId,
    PrecursorBatchModeEnum mode,
    PrecursorBatchTriggerEnum? trigger,
    PrecursorBatchStatusEnum status,
    DateTime startedAt,
    DateTime? finishedAt,
    int candidatesGenerated,
    int tested,
    int capped,
    int? insufficient,
    int survivors,
    int validated,
    num q,
    PrecursorBatchMethodEnum method,
    Map<String, Object>? windows,
    String? error,
    String? manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorBatch.copyWith(...)` or call `instanceOfPrecursorBatch.copyWith.fieldName(value)` for a single field.
class _$PrecursorBatchCWProxyImpl implements _$PrecursorBatchCWProxy {
  const _$PrecursorBatchCWProxyImpl(this._value);

  final PrecursorBatch _value;

  @override
  PrecursorBatch id(String id) => call(id: id);

  @override
  PrecursorBatch jobId(String? jobId) => call(jobId: jobId);

  @override
  PrecursorBatch mode(PrecursorBatchModeEnum mode) => call(mode: mode);

  @override
  PrecursorBatch trigger(PrecursorBatchTriggerEnum? trigger) =>
      call(trigger: trigger);

  @override
  PrecursorBatch status(PrecursorBatchStatusEnum status) =>
      call(status: status);

  @override
  PrecursorBatch startedAt(DateTime startedAt) => call(startedAt: startedAt);

  @override
  PrecursorBatch finishedAt(DateTime? finishedAt) =>
      call(finishedAt: finishedAt);

  @override
  PrecursorBatch candidatesGenerated(int candidatesGenerated) =>
      call(candidatesGenerated: candidatesGenerated);

  @override
  PrecursorBatch tested(int tested) => call(tested: tested);

  @override
  PrecursorBatch capped(int capped) => call(capped: capped);

  @override
  PrecursorBatch insufficient(int? insufficient) =>
      call(insufficient: insufficient);

  @override
  PrecursorBatch survivors(int survivors) => call(survivors: survivors);

  @override
  PrecursorBatch validated(int validated) => call(validated: validated);

  @override
  PrecursorBatch q(num q) => call(q: q);

  @override
  PrecursorBatch method(PrecursorBatchMethodEnum method) =>
      call(method: method);

  @override
  PrecursorBatch windows(Map<String, Object>? windows) =>
      call(windows: windows);

  @override
  PrecursorBatch error(String? error) => call(error: error);

  @override
  PrecursorBatch manifestId(String? manifestId) => call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorBatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorBatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorBatch call({
    Object? id = const $CopyWithPlaceholder(),
    Object? jobId = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? trigger = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? finishedAt = const $CopyWithPlaceholder(),
    Object? candidatesGenerated = const $CopyWithPlaceholder(),
    Object? tested = const $CopyWithPlaceholder(),
    Object? capped = const $CopyWithPlaceholder(),
    Object? insufficient = const $CopyWithPlaceholder(),
    Object? survivors = const $CopyWithPlaceholder(),
    Object? validated = const $CopyWithPlaceholder(),
    Object? q = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? windows = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return PrecursorBatch(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      jobId: jobId == const $CopyWithPlaceholder()
          ? _value.jobId
          // ignore: cast_nullable_to_non_nullable
          : jobId as String?,
      mode: mode == const $CopyWithPlaceholder() || mode == null
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as PrecursorBatchModeEnum,
      trigger: trigger == const $CopyWithPlaceholder()
          ? _value.trigger
          // ignore: cast_nullable_to_non_nullable
          : trigger as PrecursorBatchTriggerEnum?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as PrecursorBatchStatusEnum,
      startedAt: startedAt == const $CopyWithPlaceholder() || startedAt == null
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime,
      finishedAt: finishedAt == const $CopyWithPlaceholder()
          ? _value.finishedAt
          // ignore: cast_nullable_to_non_nullable
          : finishedAt as DateTime?,
      candidatesGenerated:
          candidatesGenerated == const $CopyWithPlaceholder() ||
              candidatesGenerated == null
          ? _value.candidatesGenerated
          // ignore: cast_nullable_to_non_nullable
          : candidatesGenerated as int,
      tested: tested == const $CopyWithPlaceholder() || tested == null
          ? _value.tested
          // ignore: cast_nullable_to_non_nullable
          : tested as int,
      capped: capped == const $CopyWithPlaceholder() || capped == null
          ? _value.capped
          // ignore: cast_nullable_to_non_nullable
          : capped as int,
      insufficient: insufficient == const $CopyWithPlaceholder()
          ? _value.insufficient
          // ignore: cast_nullable_to_non_nullable
          : insufficient as int?,
      survivors: survivors == const $CopyWithPlaceholder() || survivors == null
          ? _value.survivors
          // ignore: cast_nullable_to_non_nullable
          : survivors as int,
      validated: validated == const $CopyWithPlaceholder() || validated == null
          ? _value.validated
          // ignore: cast_nullable_to_non_nullable
          : validated as int,
      q: q == const $CopyWithPlaceholder() || q == null
          ? _value.q
          // ignore: cast_nullable_to_non_nullable
          : q as num,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as PrecursorBatchMethodEnum,
      windows: windows == const $CopyWithPlaceholder()
          ? _value.windows
          // ignore: cast_nullable_to_non_nullable
          : windows as Map<String, Object>?,
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

extension $PrecursorBatchCopyWith on PrecursorBatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorBatch.copyWith(...)` or `instanceOfPrecursorBatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorBatchCWProxy get copyWith => _$PrecursorBatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorBatch _$PrecursorBatchFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorBatch', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'mode',
      'status',
      'startedAt',
      'candidatesGenerated',
      'tested',
      'capped',
      'survivors',
      'validated',
      'q',
      'method',
    ],
  );
  final val = PrecursorBatch(
    id: $checkedConvert('id', (v) => v as String),
    jobId: $checkedConvert('jobId', (v) => v as String?),
    mode: $checkedConvert(
      'mode',
      (v) => $enumDecode(_$PrecursorBatchModeEnumEnumMap, v),
    ),
    trigger: $checkedConvert(
      'trigger',
      (v) => $enumDecodeNullable(_$PrecursorBatchTriggerEnumEnumMap, v),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$PrecursorBatchStatusEnumEnumMap, v),
    ),
    startedAt: $checkedConvert('startedAt', (v) => DateTime.parse(v as String)),
    finishedAt: $checkedConvert(
      'finishedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    candidatesGenerated: $checkedConvert(
      'candidatesGenerated',
      (v) => (v as num).toInt(),
    ),
    tested: $checkedConvert('tested', (v) => (v as num).toInt()),
    capped: $checkedConvert('capped', (v) => (v as num).toInt()),
    insufficient: $checkedConvert('insufficient', (v) => (v as num?)?.toInt()),
    survivors: $checkedConvert('survivors', (v) => (v as num).toInt()),
    validated: $checkedConvert('validated', (v) => (v as num).toInt()),
    q: $checkedConvert('q', (v) => v as num),
    method: $checkedConvert(
      'method',
      (v) => $enumDecode(_$PrecursorBatchMethodEnumEnumMap, v),
    ),
    windows: $checkedConvert(
      'windows',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    error: $checkedConvert('error', (v) => v as String?),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$PrecursorBatchToJson(PrecursorBatch instance) =>
    <String, dynamic>{
      'id': instance.id,
      'jobId': ?instance.jobId,
      'mode': _$PrecursorBatchModeEnumEnumMap[instance.mode]!,
      'trigger': ?_$PrecursorBatchTriggerEnumEnumMap[instance.trigger],
      'status': _$PrecursorBatchStatusEnumEnumMap[instance.status]!,
      'startedAt': instance.startedAt.toIso8601String(),
      'finishedAt': ?instance.finishedAt?.toIso8601String(),
      'candidatesGenerated': instance.candidatesGenerated,
      'tested': instance.tested,
      'capped': instance.capped,
      'insufficient': ?instance.insufficient,
      'survivors': instance.survivors,
      'validated': instance.validated,
      'q': instance.q,
      'method': _$PrecursorBatchMethodEnumEnumMap[instance.method]!,
      'windows': ?instance.windows,
      'error': ?instance.error,
      'manifestId': ?instance.manifestId,
    };

const _$PrecursorBatchModeEnumEnumMap = {
  PrecursorBatchModeEnum.mine: 'mine',
  PrecursorBatchModeEnum.retest: 'retest',
};

const _$PrecursorBatchTriggerEnumEnumMap = {
  PrecursorBatchTriggerEnum.schedule: 'schedule',
  PrecursorBatchTriggerEnum.manual: 'manual',
  PrecursorBatchTriggerEnum.cycle: 'cycle',
};

const _$PrecursorBatchStatusEnumEnumMap = {
  PrecursorBatchStatusEnum.running: 'running',
  PrecursorBatchStatusEnum.done: 'done',
  PrecursorBatchStatusEnum.failed: 'failed',
  PrecursorBatchStatusEnum.cancelled: 'cancelled',
};

const _$PrecursorBatchMethodEnumEnumMap = {
  PrecursorBatchMethodEnum.BH: 'BH',
  PrecursorBatchMethodEnum.BY: 'BY',
};
