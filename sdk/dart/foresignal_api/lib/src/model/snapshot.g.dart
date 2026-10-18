// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'snapshot.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SnapshotCWProxy {
  Snapshot id(String id);

  Snapshot kind(SnapshotKindEnum kind);

  Snapshot format(SnapshotFormatEnum format);

  Snapshot spec(Map<String, Object>? spec);

  Snapshot status(SnapshotStatusEnum status);

  Snapshot progress(int progress);

  Snapshot principal(String? principal);

  Snapshot rowCount(int? rowCount);

  Snapshot bytes(int? bytes);

  Snapshot manifestSha256(String? manifestSha256);

  Snapshot licenceSummary(ExportLicenceSummary? licenceSummary);

  Snapshot watermarkSeq(int? watermarkSeq);

  Snapshot personalUse(bool? personalUse);

  Snapshot createdAt(DateTime createdAt);

  Snapshot startedAt(DateTime? startedAt);

  Snapshot finishedAt(DateTime? finishedAt);

  Snapshot expiresAt(DateTime? expiresAt);

  Snapshot error(String? error);

  Snapshot downloadUrl(String? downloadUrl);

  Snapshot manifestUrl(String? manifestUrl);

  Snapshot files(List<ExportFile>? files);

  Snapshot productId(String? productId);

  Snapshot atSeq(int atSeq);

  Snapshot asOf(DateTime? asOf);

  Snapshot name(String? name);

  Snapshot heldSources(List<String>? heldSources);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Snapshot(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Snapshot(...).copyWith(id: 12, name: "My name")
  /// ```
  Snapshot call({
    String id,
    SnapshotKindEnum kind,
    SnapshotFormatEnum format,
    Map<String, Object>? spec,
    SnapshotStatusEnum status,
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
    int atSeq,
    DateTime? asOf,
    String? name,
    List<String>? heldSources,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSnapshot.copyWith(...)` or call `instanceOfSnapshot.copyWith.fieldName(value)` for a single field.
class _$SnapshotCWProxyImpl implements _$SnapshotCWProxy {
  const _$SnapshotCWProxyImpl(this._value);

  final Snapshot _value;

  @override
  Snapshot id(String id) => call(id: id);

  @override
  Snapshot kind(SnapshotKindEnum kind) => call(kind: kind);

  @override
  Snapshot format(SnapshotFormatEnum format) => call(format: format);

  @override
  Snapshot spec(Map<String, Object>? spec) => call(spec: spec);

  @override
  Snapshot status(SnapshotStatusEnum status) => call(status: status);

  @override
  Snapshot progress(int progress) => call(progress: progress);

  @override
  Snapshot principal(String? principal) => call(principal: principal);

  @override
  Snapshot rowCount(int? rowCount) => call(rowCount: rowCount);

  @override
  Snapshot bytes(int? bytes) => call(bytes: bytes);

  @override
  Snapshot manifestSha256(String? manifestSha256) =>
      call(manifestSha256: manifestSha256);

  @override
  Snapshot licenceSummary(ExportLicenceSummary? licenceSummary) =>
      call(licenceSummary: licenceSummary);

  @override
  Snapshot watermarkSeq(int? watermarkSeq) => call(watermarkSeq: watermarkSeq);

  @override
  Snapshot personalUse(bool? personalUse) => call(personalUse: personalUse);

  @override
  Snapshot createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Snapshot startedAt(DateTime? startedAt) => call(startedAt: startedAt);

  @override
  Snapshot finishedAt(DateTime? finishedAt) => call(finishedAt: finishedAt);

  @override
  Snapshot expiresAt(DateTime? expiresAt) => call(expiresAt: expiresAt);

  @override
  Snapshot error(String? error) => call(error: error);

  @override
  Snapshot downloadUrl(String? downloadUrl) => call(downloadUrl: downloadUrl);

  @override
  Snapshot manifestUrl(String? manifestUrl) => call(manifestUrl: manifestUrl);

  @override
  Snapshot files(List<ExportFile>? files) => call(files: files);

  @override
  Snapshot productId(String? productId) => call(productId: productId);

  @override
  Snapshot atSeq(int atSeq) => call(atSeq: atSeq);

  @override
  Snapshot asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  Snapshot name(String? name) => call(name: name);

  @override
  Snapshot heldSources(List<String>? heldSources) =>
      call(heldSources: heldSources);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Snapshot(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Snapshot(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Snapshot call({
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
    Object? atSeq = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? heldSources = const $CopyWithPlaceholder(),
  }) {
    return Snapshot(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as SnapshotKindEnum,
      format: format == const $CopyWithPlaceholder() || format == null
          ? _value.format
          // ignore: cast_nullable_to_non_nullable
          : format as SnapshotFormatEnum,
      spec: spec == const $CopyWithPlaceholder()
          ? _value.spec
          // ignore: cast_nullable_to_non_nullable
          : spec as Map<String, Object>?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SnapshotStatusEnum,
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
      atSeq: atSeq == const $CopyWithPlaceholder() || atSeq == null
          ? _value.atSeq
          // ignore: cast_nullable_to_non_nullable
          : atSeq as int,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      heldSources: heldSources == const $CopyWithPlaceholder()
          ? _value.heldSources
          // ignore: cast_nullable_to_non_nullable
          : heldSources as List<String>?,
    );
  }
}

extension $SnapshotCopyWith on Snapshot {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSnapshot.copyWith(...)` or `instanceOfSnapshot.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SnapshotCWProxy get copyWith => _$SnapshotCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Snapshot _$SnapshotFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Snapshot', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'kind',
      'format',
      'status',
      'progress',
      'createdAt',
      'atSeq',
    ],
  );
  final val = Snapshot(
    id: $checkedConvert('id', (v) => v as String),
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecode(_$SnapshotKindEnumEnumMap, v),
    ),
    format: $checkedConvert(
      'format',
      (v) => $enumDecode(_$SnapshotFormatEnumEnumMap, v),
    ),
    spec: $checkedConvert(
      'spec',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$SnapshotStatusEnumEnumMap, v),
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
    atSeq: $checkedConvert('atSeq', (v) => (v as num).toInt()),
    asOf: $checkedConvert(
      'asOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    name: $checkedConvert('name', (v) => v as String?),
    heldSources: $checkedConvert(
      'heldSources',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$SnapshotToJson(Snapshot instance) => <String, dynamic>{
  'id': instance.id,
  'kind': _$SnapshotKindEnumEnumMap[instance.kind]!,
  'format': _$SnapshotFormatEnumEnumMap[instance.format]!,
  'spec': ?instance.spec,
  'status': _$SnapshotStatusEnumEnumMap[instance.status]!,
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
  'atSeq': instance.atSeq,
  'asOf': ?instance.asOf?.toIso8601String(),
  'name': ?instance.name,
  'heldSources': ?instance.heldSources,
};

const _$SnapshotKindEnumEnumMap = {
  SnapshotKindEnum.EXPORT: 'EXPORT',
  SnapshotKindEnum.SNAPSHOT: 'SNAPSHOT',
  SnapshotKindEnum.PACK: 'PACK',
  SnapshotKindEnum.RASTER: 'RASTER',
  SnapshotKindEnum.PLACE_PACK: 'PLACE_PACK',
};

const _$SnapshotFormatEnumEnumMap = {
  SnapshotFormatEnum.geoparquet: 'geoparquet',
  SnapshotFormatEnum.geopackage: 'geopackage',
  SnapshotFormatEnum.flatgeobuf: 'flatgeobuf',
  SnapshotFormatEnum.gpx: 'gpx',
  SnapshotFormatEnum.pmtiles: 'pmtiles',
  SnapshotFormatEnum.mbtiles: 'mbtiles',
  SnapshotFormatEnum.cog: 'cog',
  SnapshotFormatEnum.zip: 'zip',
  SnapshotFormatEnum.jsonBundle: 'json-bundle',
};

const _$SnapshotStatusEnumEnumMap = {
  SnapshotStatusEnum.QUEUED: 'QUEUED',
  SnapshotStatusEnum.RUNNING: 'RUNNING',
  SnapshotStatusEnum.DONE: 'DONE',
  SnapshotStatusEnum.FAILED: 'FAILED',
  SnapshotStatusEnum.CANCELLED: 'CANCELLED',
  SnapshotStatusEnum.EXPIRED: 'EXPIRED',
};
