// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'backfill_job.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BackfillJobCWProxy {
  BackfillJob id(String id);

  BackfillJob sourceId(String sourceId);

  BackfillJob status(BackfillJobStatusEnum status);

  BackfillJob from(DateTime from);

  BackfillJob cursor(DateTime? cursor);

  BackfillJob pagesFetched(int? pagesFetched);

  BackfillJob versionsWritten(int? versionsWritten);

  BackfillJob createdAt(DateTime createdAt);

  BackfillJob startedAt(DateTime? startedAt);

  BackfillJob finishedAt(DateTime? finishedAt);

  BackfillJob error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BackfillJob(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BackfillJob(...).copyWith(id: 12, name: "My name")
  /// ```
  BackfillJob call({
    String id,
    String sourceId,
    BackfillJobStatusEnum status,
    DateTime from,
    DateTime? cursor,
    int? pagesFetched,
    int? versionsWritten,
    DateTime createdAt,
    DateTime? startedAt,
    DateTime? finishedAt,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBackfillJob.copyWith(...)` or call `instanceOfBackfillJob.copyWith.fieldName(value)` for a single field.
class _$BackfillJobCWProxyImpl implements _$BackfillJobCWProxy {
  const _$BackfillJobCWProxyImpl(this._value);

  final BackfillJob _value;

  @override
  BackfillJob id(String id) => call(id: id);

  @override
  BackfillJob sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  BackfillJob status(BackfillJobStatusEnum status) => call(status: status);

  @override
  BackfillJob from(DateTime from) => call(from: from);

  @override
  BackfillJob cursor(DateTime? cursor) => call(cursor: cursor);

  @override
  BackfillJob pagesFetched(int? pagesFetched) =>
      call(pagesFetched: pagesFetched);

  @override
  BackfillJob versionsWritten(int? versionsWritten) =>
      call(versionsWritten: versionsWritten);

  @override
  BackfillJob createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  BackfillJob startedAt(DateTime? startedAt) => call(startedAt: startedAt);

  @override
  BackfillJob finishedAt(DateTime? finishedAt) => call(finishedAt: finishedAt);

  @override
  BackfillJob error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BackfillJob(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BackfillJob(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BackfillJob call({
    Object? id = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? cursor = const $CopyWithPlaceholder(),
    Object? pagesFetched = const $CopyWithPlaceholder(),
    Object? versionsWritten = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? finishedAt = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return BackfillJob(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as BackfillJobStatusEnum,
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime,
      cursor: cursor == const $CopyWithPlaceholder()
          ? _value.cursor
          // ignore: cast_nullable_to_non_nullable
          : cursor as DateTime?,
      pagesFetched: pagesFetched == const $CopyWithPlaceholder()
          ? _value.pagesFetched
          // ignore: cast_nullable_to_non_nullable
          : pagesFetched as int?,
      versionsWritten: versionsWritten == const $CopyWithPlaceholder()
          ? _value.versionsWritten
          // ignore: cast_nullable_to_non_nullable
          : versionsWritten as int?,
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
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $BackfillJobCopyWith on BackfillJob {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBackfillJob.copyWith(...)` or `instanceOfBackfillJob.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BackfillJobCWProxy get copyWith => _$BackfillJobCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BackfillJob _$BackfillJobFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('BackfillJob', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['id', 'sourceId', 'status', 'from', 'createdAt'],
  );
  final val = BackfillJob(
    id: $checkedConvert('id', (v) => v as String),
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$BackfillJobStatusEnumEnumMap, v),
    ),
    from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
    cursor: $checkedConvert(
      'cursor',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    pagesFetched: $checkedConvert('pagesFetched', (v) => (v as num?)?.toInt()),
    versionsWritten: $checkedConvert(
      'versionsWritten',
      (v) => (v as num?)?.toInt(),
    ),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    startedAt: $checkedConvert(
      'startedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    finishedAt: $checkedConvert(
      'finishedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    error: $checkedConvert('error', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$BackfillJobToJson(BackfillJob instance) =>
    <String, dynamic>{
      'id': instance.id,
      'sourceId': instance.sourceId,
      'status': _$BackfillJobStatusEnumEnumMap[instance.status]!,
      'from': instance.from.toIso8601String(),
      'cursor': ?instance.cursor?.toIso8601String(),
      'pagesFetched': ?instance.pagesFetched,
      'versionsWritten': ?instance.versionsWritten,
      'createdAt': instance.createdAt.toIso8601String(),
      'startedAt': ?instance.startedAt?.toIso8601String(),
      'finishedAt': ?instance.finishedAt?.toIso8601String(),
      'error': ?instance.error,
    };

const _$BackfillJobStatusEnumEnumMap = {
  BackfillJobStatusEnum.queued: 'queued',
  BackfillJobStatusEnum.running: 'running',
  BackfillJobStatusEnum.done: 'done',
  BackfillJobStatusEnum.failed: 'failed',
  BackfillJobStatusEnum.cancelled: 'cancelled',
};
