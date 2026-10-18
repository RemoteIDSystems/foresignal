// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'export_manifest.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExportManifestCWProxy {
  ExportManifest manifestVersion(
    ExportManifestManifestVersionEnum? manifestVersion,
  );

  ExportManifest id(String id);

  ExportManifest kind(ExportManifestKindEnum kind);

  ExportManifest format(ExportManifestFormatEnum format);

  ExportManifest instanceId(String instanceId);

  ExportManifest createdAt(DateTime createdAt);

  ExportManifest watermarkSeq(int? watermarkSeq);

  ExportManifest atSeq(int? atSeq);

  ExportManifest spec(Map<String, Object>? spec);

  ExportManifest rowCount(int? rowCount);

  ExportManifest files(List<ExportManifestFilesInner> files);

  ExportManifest licence(ExportLicenceSummary licence);

  ExportManifest personalUse(bool? personalUse);

  ExportManifest signature(ExportManifestSignature? signature);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportManifest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportManifest(...).copyWith(id: 12, name: "My name")
  /// ```
  ExportManifest call({
    ExportManifestManifestVersionEnum? manifestVersion,
    String id,
    ExportManifestKindEnum kind,
    ExportManifestFormatEnum format,
    String instanceId,
    DateTime createdAt,
    int? watermarkSeq,
    int? atSeq,
    Map<String, Object>? spec,
    int? rowCount,
    List<ExportManifestFilesInner> files,
    ExportLicenceSummary licence,
    bool? personalUse,
    ExportManifestSignature? signature,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExportManifest.copyWith(...)` or call `instanceOfExportManifest.copyWith.fieldName(value)` for a single field.
class _$ExportManifestCWProxyImpl implements _$ExportManifestCWProxy {
  const _$ExportManifestCWProxyImpl(this._value);

  final ExportManifest _value;

  @override
  ExportManifest manifestVersion(
    ExportManifestManifestVersionEnum? manifestVersion,
  ) => call(manifestVersion: manifestVersion);

  @override
  ExportManifest id(String id) => call(id: id);

  @override
  ExportManifest kind(ExportManifestKindEnum kind) => call(kind: kind);

  @override
  ExportManifest format(ExportManifestFormatEnum format) =>
      call(format: format);

  @override
  ExportManifest instanceId(String instanceId) => call(instanceId: instanceId);

  @override
  ExportManifest createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  ExportManifest watermarkSeq(int? watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  @override
  ExportManifest atSeq(int? atSeq) => call(atSeq: atSeq);

  @override
  ExportManifest spec(Map<String, Object>? spec) => call(spec: spec);

  @override
  ExportManifest rowCount(int? rowCount) => call(rowCount: rowCount);

  @override
  ExportManifest files(List<ExportManifestFilesInner> files) =>
      call(files: files);

  @override
  ExportManifest licence(ExportLicenceSummary licence) =>
      call(licence: licence);

  @override
  ExportManifest personalUse(bool? personalUse) =>
      call(personalUse: personalUse);

  @override
  ExportManifest signature(ExportManifestSignature? signature) =>
      call(signature: signature);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportManifest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportManifest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExportManifest call({
    Object? manifestVersion = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? format = const $CopyWithPlaceholder(),
    Object? instanceId = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
    Object? atSeq = const $CopyWithPlaceholder(),
    Object? spec = const $CopyWithPlaceholder(),
    Object? rowCount = const $CopyWithPlaceholder(),
    Object? files = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? personalUse = const $CopyWithPlaceholder(),
    Object? signature = const $CopyWithPlaceholder(),
  }) {
    return ExportManifest(
      manifestVersion: manifestVersion == const $CopyWithPlaceholder()
          ? _value.manifestVersion
          // ignore: cast_nullable_to_non_nullable
          : manifestVersion as ExportManifestManifestVersionEnum?,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as ExportManifestKindEnum,
      format: format == const $CopyWithPlaceholder() || format == null
          ? _value.format
          // ignore: cast_nullable_to_non_nullable
          : format as ExportManifestFormatEnum,
      instanceId:
          instanceId == const $CopyWithPlaceholder() || instanceId == null
          ? _value.instanceId
          // ignore: cast_nullable_to_non_nullable
          : instanceId as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      watermarkSeq: watermarkSeq == const $CopyWithPlaceholder()
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int?,
      atSeq: atSeq == const $CopyWithPlaceholder()
          ? _value.atSeq
          // ignore: cast_nullable_to_non_nullable
          : atSeq as int?,
      spec: spec == const $CopyWithPlaceholder()
          ? _value.spec
          // ignore: cast_nullable_to_non_nullable
          : spec as Map<String, Object>?,
      rowCount: rowCount == const $CopyWithPlaceholder()
          ? _value.rowCount
          // ignore: cast_nullable_to_non_nullable
          : rowCount as int?,
      files: files == const $CopyWithPlaceholder() || files == null
          ? _value.files
          // ignore: cast_nullable_to_non_nullable
          : files as List<ExportManifestFilesInner>,
      licence: licence == const $CopyWithPlaceholder() || licence == null
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as ExportLicenceSummary,
      personalUse: personalUse == const $CopyWithPlaceholder()
          ? _value.personalUse
          // ignore: cast_nullable_to_non_nullable
          : personalUse as bool?,
      signature: signature == const $CopyWithPlaceholder()
          ? _value.signature
          // ignore: cast_nullable_to_non_nullable
          : signature as ExportManifestSignature?,
    );
  }
}

extension $ExportManifestCopyWith on ExportManifest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExportManifest.copyWith(...)` or `instanceOfExportManifest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExportManifestCWProxy get copyWith => _$ExportManifestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExportManifest _$ExportManifestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExportManifest', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'kind',
      'format',
      'instanceId',
      'createdAt',
      'files',
      'licence',
    ],
  );
  final val = ExportManifest(
    manifestVersion: $checkedConvert(
      'manifestVersion',
      (v) => $enumDecodeNullable(_$ExportManifestManifestVersionEnumEnumMap, v),
    ),
    id: $checkedConvert('id', (v) => v as String),
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecode(_$ExportManifestKindEnumEnumMap, v),
    ),
    format: $checkedConvert(
      'format',
      (v) => $enumDecode(_$ExportManifestFormatEnumEnumMap, v),
    ),
    instanceId: $checkedConvert('instanceId', (v) => v as String),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num?)?.toInt()),
    atSeq: $checkedConvert('atSeq', (v) => (v as num?)?.toInt()),
    spec: $checkedConvert(
      'spec',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    rowCount: $checkedConvert('rowCount', (v) => (v as num?)?.toInt()),
    files: $checkedConvert(
      'files',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ExportManifestFilesInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    licence: $checkedConvert(
      'licence',
      (v) => ExportLicenceSummary.fromJson(v as Map<String, dynamic>),
    ),
    personalUse: $checkedConvert('personalUse', (v) => v as bool?),
    signature: $checkedConvert(
      'signature',
      (v) => v == null
          ? null
          : ExportManifestSignature.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$ExportManifestToJson(ExportManifest instance) =>
    <String, dynamic>{
      'manifestVersion':
          ?_$ExportManifestManifestVersionEnumEnumMap[instance.manifestVersion],
      'id': instance.id,
      'kind': _$ExportManifestKindEnumEnumMap[instance.kind]!,
      'format': _$ExportManifestFormatEnumEnumMap[instance.format]!,
      'instanceId': instance.instanceId,
      'createdAt': instance.createdAt.toIso8601String(),
      'watermarkSeq': ?instance.watermarkSeq,
      'atSeq': ?instance.atSeq,
      'spec': ?instance.spec,
      'rowCount': ?instance.rowCount,
      'files': instance.files.map((e) => e.toJson()).toList(),
      'licence': instance.licence.toJson(),
      'personalUse': ?instance.personalUse,
      'signature': ?instance.signature?.toJson(),
    };

const _$ExportManifestManifestVersionEnumEnumMap = {
  ExportManifestManifestVersionEnum.number1: 1,
};

const _$ExportManifestKindEnumEnumMap = {
  ExportManifestKindEnum.EXPORT: 'EXPORT',
  ExportManifestKindEnum.SNAPSHOT: 'SNAPSHOT',
  ExportManifestKindEnum.PACK: 'PACK',
};

const _$ExportManifestFormatEnumEnumMap = {
  ExportManifestFormatEnum.geoparquet: 'geoparquet',
  ExportManifestFormatEnum.geopackage: 'geopackage',
  ExportManifestFormatEnum.flatgeobuf: 'flatgeobuf',
  ExportManifestFormatEnum.gpx: 'gpx',
  ExportManifestFormatEnum.pmtiles: 'pmtiles',
};
