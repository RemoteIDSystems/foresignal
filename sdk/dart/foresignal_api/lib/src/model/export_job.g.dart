// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'export_job.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExportJobCWProxy {
  ExportJob id(String id);

  ExportJob kind(ExportJobKindEnum kind);

  ExportJob format(ExportJobFormatEnum format);

  ExportJob spec(Map<String, Object>? spec);

  ExportJob status(ExportJobStatusEnum status);

  ExportJob progress(int progress);

  ExportJob principal(String? principal);

  ExportJob rowCount(int? rowCount);

  ExportJob bytes(int? bytes);

  ExportJob manifestSha256(String? manifestSha256);

  ExportJob licenceSummary(ExportLicenceSummary? licenceSummary);

  ExportJob watermarkSeq(int? watermarkSeq);

  ExportJob personalUse(bool? personalUse);

  ExportJob createdAt(DateTime createdAt);

  ExportJob startedAt(DateTime? startedAt);

  ExportJob finishedAt(DateTime? finishedAt);

  ExportJob expiresAt(DateTime? expiresAt);

  ExportJob error(String? error);

  ExportJob downloadUrl(String? downloadUrl);

  ExportJob manifestUrl(String? manifestUrl);

  ExportJob files(List<ExportFile>? files);

  ExportJob productId(String? productId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportJob(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportJob(...).copyWith(id: 12, name: "My name")
  /// ```
  ExportJob call({
    String id,
    ExportJobKindEnum kind,
    ExportJobFormatEnum format,
    Map<String, Object>? spec,
    ExportJobStatusEnum status,
    int progress,
    String? principal,
    int? rowCount,
    int? bytes,
    String? manifestSha256,
    ExportLicenceSummary? licenceSummary,
    int? watermarkSeq,
    bool? personalUse,
    DateTime createdAt,
    DateTime? startedAt,
    DateTime? finishedAt,
    DateTime? expiresAt,
    String? error,
    String? downloadUrl,
    String? manifestUrl,
    List<ExportFile>? files,
    String? productId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExportJob.copyWith(...)` or call `instanceOfExportJob.copyWith.fieldName(value)` for a single field.
class _$ExportJobCWProxyImpl implements _$ExportJobCWProxy {
  const _$ExportJobCWProxyImpl(this._value);

  final ExportJob _value;

  @override
  ExportJob id(String id) => call(id: id);

  @override
  ExportJob kind(ExportJobKindEnum kind) => call(kind: kind);

  @override
  ExportJob format(ExportJobFormatEnum format) => call(format: format);

  @override
  ExportJob spec(Map<String, Object>? spec) => call(spec: spec);

  @override
  ExportJob status(ExportJobStatusEnum status) => call(status: status);

  @override
  ExportJob progress(int progress) => call(progress: progress);

  @override
  ExportJob principal(String? principal) => call(principal: principal);

  @override
  ExportJob rowCount(int? rowCount) => call(rowCount: rowCount);

  @override
  ExportJob bytes(int? bytes) => call(bytes: bytes);

  @override
  ExportJob manifestSha256(String? manifestSha256) =>
      call(manifestSha256: manifestSha256);

  @override
  ExportJob licenceSummary(ExportLicenceSummary? licenceSummary) =>
      call(licenceSummary: licenceSummary);

  @override
  ExportJob watermarkSeq(int? watermarkSeq) => call(watermarkSeq: watermarkSeq);

  @override
  ExportJob personalUse(bool? personalUse) => call(personalUse: personalUse);

  @override
  ExportJob createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  ExportJob startedAt(DateTime? startedAt) => call(startedAt: startedAt);

  @override
  ExportJob finishedAt(DateTime? finishedAt) => call(finishedAt: finishedAt);

  @override
  ExportJob expiresAt(DateTime? expiresAt) => call(expiresAt: expiresAt);

  @override
  ExportJob error(String? error) => call(error: error);

  @override
  ExportJob downloadUrl(String? downloadUrl) => call(downloadUrl: downloadUrl);

  @override
  ExportJob manifestUrl(String? manifestUrl) => call(manifestUrl: manifestUrl);

  @override
  ExportJob files(List<ExportFile>? files) => call(files: files);

  @override
  ExportJob productId(String? productId) => call(productId: productId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportJob(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportJob(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExportJob call({
    Object? id = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? format = const $CopyWithPlaceholder(),
    Object? spec = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? progress = const $CopyWithPlaceholder(),
    Object? principal = const $CopyWithPlaceholder(),
    Object? rowCount = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
    Object? manifestSha256 = const $CopyWithPlaceholder(),
    Object? licenceSummary = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
    Object? personalUse = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? finishedAt = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
    Object? downloadUrl = const $CopyWithPlaceholder(),
    Object? manifestUrl = const $CopyWithPlaceholder(),
    Object? files = const $CopyWithPlaceholder(),
    Object? productId = const $CopyWithPlaceholder(),
  }) {
    return ExportJob(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as ExportJobKindEnum,
      format: format == const $CopyWithPlaceholder() || format == null
          ? _value.format
          // ignore: cast_nullable_to_non_nullable
          : format as ExportJobFormatEnum,
      spec: spec == const $CopyWithPlaceholder()
          ? _value.spec
          // ignore: cast_nullable_to_non_nullable
          : spec as Map<String, Object>?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ExportJobStatusEnum,
      progress: progress == const $CopyWithPlaceholder() || progress == null
          ? _value.progress
          // ignore: cast_nullable_to_non_nullable
          : progress as int,
      principal: principal == const $CopyWithPlaceholder()
          ? _value.principal
          // ignore: cast_nullable_to_non_nullable
          : principal as String?,
      rowCount: rowCount == const $CopyWithPlaceholder()
          ? _value.rowCount
          // ignore: cast_nullable_to_non_nullable
          : rowCount as int?,
      bytes: bytes == const $CopyWithPlaceholder()
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int?,
      manifestSha256: manifestSha256 == const $CopyWithPlaceholder()
          ? _value.manifestSha256
          // ignore: cast_nullable_to_non_nullable
          : manifestSha256 as String?,
      licenceSummary: licenceSummary == const $CopyWithPlaceholder()
          ? _value.licenceSummary
          // ignore: cast_nullable_to_non_nullable
          : licenceSummary as ExportLicenceSummary?,
      watermarkSeq: watermarkSeq == const $CopyWithPlaceholder()
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int?,
      personalUse: personalUse == const $CopyWithPlaceholder()
          ? _value.personalUse
          // ignore: cast_nullable_to_non_nullable
          : personalUse as bool?,
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
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
      downloadUrl: downloadUrl == const $CopyWithPlaceholder()
          ? _value.downloadUrl
          // ignore: cast_nullable_to_non_nullable
          : downloadUrl as String?,
      manifestUrl: manifestUrl == const $CopyWithPlaceholder()
          ? _value.manifestUrl
          // ignore: cast_nullable_to_non_nullable
          : manifestUrl as String?,
      files: files == const $CopyWithPlaceholder()
          ? _value.files
          // ignore: cast_nullable_to_non_nullable
          : files as List<ExportFile>?,
      productId: productId == const $CopyWithPlaceholder()
          ? _value.productId
          // ignore: cast_nullable_to_non_nullable
          : productId as String?,
    );
  }
}

extension $ExportJobCopyWith on ExportJob {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExportJob.copyWith(...)` or `instanceOfExportJob.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExportJobCWProxy get copyWith => _$ExportJobCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExportJob _$ExportJobFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExportJob', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'kind',
      'format',
      'status',
      'progress',
      'createdAt',
    ],
  );
  final val = ExportJob(
    id: $checkedConvert('id', (v) => v as String),
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecode(_$ExportJobKindEnumEnumMap, v),
    ),
    format: $checkedConvert(
      'format',
      (v) => $enumDecode(_$ExportJobFormatEnumEnumMap, v),
    ),
    spec: $checkedConvert(
      'spec',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$ExportJobStatusEnumEnumMap, v),
    ),
    progress: $checkedConvert('progress', (v) => (v as num).toInt()),
    principal: $checkedConvert('principal', (v) => v as String?),
    rowCount: $checkedConvert('rowCount', (v) => (v as num?)?.toInt()),
    bytes: $checkedConvert('bytes', (v) => (v as num?)?.toInt()),
    manifestSha256: $checkedConvert('manifestSha256', (v) => v as String?),
    licenceSummary: $checkedConvert(
      'licenceSummary',
      (v) => v == null
          ? null
          : ExportLicenceSummary.fromJson(v as Map<String, dynamic>),
    ),
    watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num?)?.toInt()),
    personalUse: $checkedConvert('personalUse', (v) => v as bool?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    startedAt: $checkedConvert(
      'startedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    finishedAt: $checkedConvert(
      'finishedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    expiresAt: $checkedConvert(
      'expiresAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    error: $checkedConvert('error', (v) => v as String?),
    downloadUrl: $checkedConvert('downloadUrl', (v) => v as String?),
    manifestUrl: $checkedConvert('manifestUrl', (v) => v as String?),
    files: $checkedConvert(
      'files',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ExportFile.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    productId: $checkedConvert('productId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ExportJobToJson(ExportJob instance) => <String, dynamic>{
  'id': instance.id,
  'kind': _$ExportJobKindEnumEnumMap[instance.kind]!,
  'format': _$ExportJobFormatEnumEnumMap[instance.format]!,
  'spec': ?instance.spec,
  'status': _$ExportJobStatusEnumEnumMap[instance.status]!,
  'progress': instance.progress,
  'principal': ?instance.principal,
  'rowCount': ?instance.rowCount,
  'bytes': ?instance.bytes,
  'manifestSha256': ?instance.manifestSha256,
  'licenceSummary': ?instance.licenceSummary?.toJson(),
  'watermarkSeq': ?instance.watermarkSeq,
  'personalUse': ?instance.personalUse,
  'createdAt': instance.createdAt.toIso8601String(),
  'startedAt': ?instance.startedAt?.toIso8601String(),
  'finishedAt': ?instance.finishedAt?.toIso8601String(),
  'expiresAt': ?instance.expiresAt?.toIso8601String(),
  'error': ?instance.error,
  'downloadUrl': ?instance.downloadUrl,
  'manifestUrl': ?instance.manifestUrl,
  'files': ?instance.files?.map((e) => e.toJson()).toList(),
  'productId': ?instance.productId,
};

const _$ExportJobKindEnumEnumMap = {
  ExportJobKindEnum.EXPORT: 'EXPORT',
  ExportJobKindEnum.SNAPSHOT: 'SNAPSHOT',
  ExportJobKindEnum.PACK: 'PACK',
  ExportJobKindEnum.RASTER: 'RASTER',
  ExportJobKindEnum.PLACE_PACK: 'PLACE_PACK',
};

const _$ExportJobFormatEnumEnumMap = {
  ExportJobFormatEnum.geoparquet: 'geoparquet',
  ExportJobFormatEnum.geopackage: 'geopackage',
  ExportJobFormatEnum.flatgeobuf: 'flatgeobuf',
  ExportJobFormatEnum.gpx: 'gpx',
  ExportJobFormatEnum.pmtiles: 'pmtiles',
  ExportJobFormatEnum.mbtiles: 'mbtiles',
  ExportJobFormatEnum.cog: 'cog',
  ExportJobFormatEnum.zip: 'zip',
  ExportJobFormatEnum.jsonBundle: 'json-bundle',
};

const _$ExportJobStatusEnumEnumMap = {
  ExportJobStatusEnum.QUEUED: 'QUEUED',
  ExportJobStatusEnum.RUNNING: 'RUNNING',
  ExportJobStatusEnum.DONE: 'DONE',
  ExportJobStatusEnum.FAILED: 'FAILED',
  ExportJobStatusEnum.CANCELLED: 'CANCELLED',
  ExportJobStatusEnum.EXPIRED: 'EXPIRED',
};
