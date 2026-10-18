// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'reference_import.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReferenceImportCWProxy {
  ReferenceImport id(String id);

  ReferenceImport dataset(ReferenceImportDatasetEnum dataset);

  ReferenceImport params(Map<String, Object>? params);

  ReferenceImport status(ReferenceImportStatusEnum status);

  ReferenceImport statusReason(String? statusReason);

  ReferenceImport progress(num? progress);

  ReferenceImport fileSha256(String? fileSha256);

  ReferenceImport rowsRead(int rowsRead);

  ReferenceImport rowsWritten(int rowsWritten);

  ReferenceImport periodFrom(DateTime? periodFrom);

  ReferenceImport periodTo(DateTime? periodTo);

  ReferenceImport licence(String licence);

  ReferenceImport egress(ReferenceImportEgressEnum egress);

  ReferenceImport jobId(String? jobId);

  ReferenceImport startedAt(DateTime? startedAt);

  ReferenceImport finishedAt(DateTime? finishedAt);

  ReferenceImport error(String? error);

  ReferenceImport createdBy(String createdBy);

  ReferenceImport createdAt(DateTime createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceImport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceImport(...).copyWith(id: 12, name: "My name")
  /// ```
  ReferenceImport call({
    String id,
    ReferenceImportDatasetEnum dataset,
    Map<String, Object>? params,
    ReferenceImportStatusEnum status,
    String? statusReason,
    num? progress,
    String? fileSha256,
    int rowsRead,
    int rowsWritten,
    DateTime? periodFrom,
    DateTime? periodTo,
    String licence,
    ReferenceImportEgressEnum egress,
    String? jobId,
    DateTime? startedAt,
    DateTime? finishedAt,
    String? error,
    String createdBy,
    DateTime createdAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReferenceImport.copyWith(...)` or call `instanceOfReferenceImport.copyWith.fieldName(value)` for a single field.
class _$ReferenceImportCWProxyImpl implements _$ReferenceImportCWProxy {
  const _$ReferenceImportCWProxyImpl(this._value);

  final ReferenceImport _value;

  @override
  ReferenceImport id(String id) => call(id: id);

  @override
  ReferenceImport dataset(ReferenceImportDatasetEnum dataset) =>
      call(dataset: dataset);

  @override
  ReferenceImport params(Map<String, Object>? params) => call(params: params);

  @override
  ReferenceImport status(ReferenceImportStatusEnum status) =>
      call(status: status);

  @override
  ReferenceImport statusReason(String? statusReason) =>
      call(statusReason: statusReason);

  @override
  ReferenceImport progress(num? progress) => call(progress: progress);

  @override
  ReferenceImport fileSha256(String? fileSha256) =>
      call(fileSha256: fileSha256);

  @override
  ReferenceImport rowsRead(int rowsRead) => call(rowsRead: rowsRead);

  @override
  ReferenceImport rowsWritten(int rowsWritten) =>
      call(rowsWritten: rowsWritten);

  @override
  ReferenceImport periodFrom(DateTime? periodFrom) =>
      call(periodFrom: periodFrom);

  @override
  ReferenceImport periodTo(DateTime? periodTo) => call(periodTo: periodTo);

  @override
  ReferenceImport licence(String licence) => call(licence: licence);

  @override
  ReferenceImport egress(ReferenceImportEgressEnum egress) =>
      call(egress: egress);

  @override
  ReferenceImport jobId(String? jobId) => call(jobId: jobId);

  @override
  ReferenceImport startedAt(DateTime? startedAt) => call(startedAt: startedAt);

  @override
  ReferenceImport finishedAt(DateTime? finishedAt) =>
      call(finishedAt: finishedAt);

  @override
  ReferenceImport error(String? error) => call(error: error);

  @override
  ReferenceImport createdBy(String createdBy) => call(createdBy: createdBy);

  @override
  ReferenceImport createdAt(DateTime createdAt) => call(createdAt: createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceImport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceImport(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReferenceImport call({
    Object? id = const $CopyWithPlaceholder(),
    Object? dataset = const $CopyWithPlaceholder(),
    Object? params = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? statusReason = const $CopyWithPlaceholder(),
    Object? progress = const $CopyWithPlaceholder(),
    Object? fileSha256 = const $CopyWithPlaceholder(),
    Object? rowsRead = const $CopyWithPlaceholder(),
    Object? rowsWritten = const $CopyWithPlaceholder(),
    Object? periodFrom = const $CopyWithPlaceholder(),
    Object? periodTo = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? egress = const $CopyWithPlaceholder(),
    Object? jobId = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? finishedAt = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
  }) {
    return ReferenceImport(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      dataset: dataset == const $CopyWithPlaceholder() || dataset == null
          ? _value.dataset
          // ignore: cast_nullable_to_non_nullable
          : dataset as ReferenceImportDatasetEnum,
      params: params == const $CopyWithPlaceholder()
          ? _value.params
          // ignore: cast_nullable_to_non_nullable
          : params as Map<String, Object>?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ReferenceImportStatusEnum,
      statusReason: statusReason == const $CopyWithPlaceholder()
          ? _value.statusReason
          // ignore: cast_nullable_to_non_nullable
          : statusReason as String?,
      progress: progress == const $CopyWithPlaceholder()
          ? _value.progress
          // ignore: cast_nullable_to_non_nullable
          : progress as num?,
      fileSha256: fileSha256 == const $CopyWithPlaceholder()
          ? _value.fileSha256
          // ignore: cast_nullable_to_non_nullable
          : fileSha256 as String?,
      rowsRead: rowsRead == const $CopyWithPlaceholder() || rowsRead == null
          ? _value.rowsRead
          // ignore: cast_nullable_to_non_nullable
          : rowsRead as int,
      rowsWritten:
          rowsWritten == const $CopyWithPlaceholder() || rowsWritten == null
          ? _value.rowsWritten
          // ignore: cast_nullable_to_non_nullable
          : rowsWritten as int,
      periodFrom: periodFrom == const $CopyWithPlaceholder()
          ? _value.periodFrom
          // ignore: cast_nullable_to_non_nullable
          : periodFrom as DateTime?,
      periodTo: periodTo == const $CopyWithPlaceholder()
          ? _value.periodTo
          // ignore: cast_nullable_to_non_nullable
          : periodTo as DateTime?,
      licence: licence == const $CopyWithPlaceholder() || licence == null
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as String,
      egress: egress == const $CopyWithPlaceholder() || egress == null
          ? _value.egress
          // ignore: cast_nullable_to_non_nullable
          : egress as ReferenceImportEgressEnum,
      jobId: jobId == const $CopyWithPlaceholder()
          ? _value.jobId
          // ignore: cast_nullable_to_non_nullable
          : jobId as String?,
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
      createdBy: createdBy == const $CopyWithPlaceholder() || createdBy == null
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
    );
  }
}

extension $ReferenceImportCopyWith on ReferenceImport {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReferenceImport.copyWith(...)` or `instanceOfReferenceImport.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReferenceImportCWProxy get copyWith => _$ReferenceImportCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReferenceImport _$ReferenceImportFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReferenceImport', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'dataset',
      'status',
      'rowsRead',
      'rowsWritten',
      'licence',
      'egress',
      'createdBy',
      'createdAt',
    ],
  );
  final val = ReferenceImport(
    id: $checkedConvert('id', (v) => v as String),
    dataset: $checkedConvert(
      'dataset',
      (v) => $enumDecode(_$ReferenceImportDatasetEnumEnumMap, v),
    ),
    params: $checkedConvert(
      'params',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$ReferenceImportStatusEnumEnumMap, v),
    ),
    statusReason: $checkedConvert('statusReason', (v) => v as String?),
    progress: $checkedConvert('progress', (v) => v as num?),
    fileSha256: $checkedConvert('fileSha256', (v) => v as String?),
    rowsRead: $checkedConvert('rowsRead', (v) => (v as num).toInt()),
    rowsWritten: $checkedConvert('rowsWritten', (v) => (v as num).toInt()),
    periodFrom: $checkedConvert(
      'periodFrom',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    periodTo: $checkedConvert(
      'periodTo',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    licence: $checkedConvert('licence', (v) => v as String),
    egress: $checkedConvert(
      'egress',
      (v) => $enumDecode(_$ReferenceImportEgressEnumEnumMap, v),
    ),
    jobId: $checkedConvert('jobId', (v) => v as String?),
    startedAt: $checkedConvert(
      'startedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    finishedAt: $checkedConvert(
      'finishedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    error: $checkedConvert('error', (v) => v as String?),
    createdBy: $checkedConvert('createdBy', (v) => v as String),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$ReferenceImportToJson(ReferenceImport instance) =>
    <String, dynamic>{
      'id': instance.id,
      'dataset': _$ReferenceImportDatasetEnumEnumMap[instance.dataset]!,
      'params': ?instance.params,
      'status': _$ReferenceImportStatusEnumEnumMap[instance.status]!,
      'statusReason': ?instance.statusReason,
      'progress': ?instance.progress,
      'fileSha256': ?instance.fileSha256,
      'rowsRead': instance.rowsRead,
      'rowsWritten': instance.rowsWritten,
      'periodFrom': ?instance.periodFrom?.toIso8601String(),
      'periodTo': ?instance.periodTo?.toIso8601String(),
      'licence': instance.licence,
      'egress': _$ReferenceImportEgressEnumEnumMap[instance.egress]!,
      'jobId': ?instance.jobId,
      'startedAt': ?instance.startedAt?.toIso8601String(),
      'finishedAt': ?instance.finishedAt?.toIso8601String(),
      'error': ?instance.error,
      'createdBy': instance.createdBy,
      'createdAt': instance.createdAt.toIso8601String(),
    };

const _$ReferenceImportDatasetEnumEnumMap = {
  ReferenceImportDatasetEnum.ucdpGed: 'ucdp-ged',
  ReferenceImportDatasetEnum.acled: 'acled',
  ReferenceImportDatasetEnum.usgsComcat: 'usgs-comcat',
  ReferenceImportDatasetEnum.gdacs: 'gdacs',
};

const _$ReferenceImportStatusEnumEnumMap = {
  ReferenceImportStatusEnum.queued: 'queued',
  ReferenceImportStatusEnum.running: 'running',
  ReferenceImportStatusEnum.done: 'done',
  ReferenceImportStatusEnum.failed: 'failed',
  ReferenceImportStatusEnum.skipped: 'skipped',
  ReferenceImportStatusEnum.cancelled: 'cancelled',
};

const _$ReferenceImportEgressEnumEnumMap = {
  ReferenceImportEgressEnum.OPEN: 'OPEN',
  ReferenceImportEgressEnum.LOCAL_ONLY: 'LOCAL_ONLY',
};
