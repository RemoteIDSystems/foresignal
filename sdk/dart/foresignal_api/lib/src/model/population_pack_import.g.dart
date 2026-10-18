// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'population_pack_import.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PopulationPackImportCWProxy {
  PopulationPackImport id(String id);

  PopulationPackImport path(String? path);

  PopulationPackImport fileSha256(String fileSha256);

  PopulationPackImport epoch(int epoch);

  PopulationPackImport licence(PopulationPackImportLicenceEnum licence);

  PopulationPackImport attribution(
    PopulationPackImportAttributionEnum attribution,
  );

  PopulationPackImport cells(PopulationPackImportCells? cells);

  PopulationPackImport importedAt(DateTime importedAt);

  PopulationPackImport durationMs(int? durationMs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PopulationPackImport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PopulationPackImport(...).copyWith(id: 12, name: "My name")
  /// ```
  PopulationPackImport call({
    String id,
    String? path,
    String fileSha256,
    int epoch,
    PopulationPackImportLicenceEnum licence,
    PopulationPackImportAttributionEnum attribution,
    PopulationPackImportCells? cells,
    DateTime importedAt,
    int? durationMs,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPopulationPackImport.copyWith(...)` or call `instanceOfPopulationPackImport.copyWith.fieldName(value)` for a single field.
class _$PopulationPackImportCWProxyImpl
    implements _$PopulationPackImportCWProxy {
  const _$PopulationPackImportCWProxyImpl(this._value);

  final PopulationPackImport _value;

  @override
  PopulationPackImport id(String id) => call(id: id);

  @override
  PopulationPackImport path(String? path) => call(path: path);

  @override
  PopulationPackImport fileSha256(String fileSha256) =>
      call(fileSha256: fileSha256);

  @override
  PopulationPackImport epoch(int epoch) => call(epoch: epoch);

  @override
  PopulationPackImport licence(PopulationPackImportLicenceEnum licence) =>
      call(licence: licence);

  @override
  PopulationPackImport attribution(
    PopulationPackImportAttributionEnum attribution,
  ) => call(attribution: attribution);

  @override
  PopulationPackImport cells(PopulationPackImportCells? cells) =>
      call(cells: cells);

  @override
  PopulationPackImport importedAt(DateTime importedAt) =>
      call(importedAt: importedAt);

  @override
  PopulationPackImport durationMs(int? durationMs) =>
      call(durationMs: durationMs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PopulationPackImport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PopulationPackImport(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PopulationPackImport call({
    Object? id = const $CopyWithPlaceholder(),
    Object? path = const $CopyWithPlaceholder(),
    Object? fileSha256 = const $CopyWithPlaceholder(),
    Object? epoch = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? cells = const $CopyWithPlaceholder(),
    Object? importedAt = const $CopyWithPlaceholder(),
    Object? durationMs = const $CopyWithPlaceholder(),
  }) {
    return PopulationPackImport(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      path: path == const $CopyWithPlaceholder()
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String?,
      fileSha256:
          fileSha256 == const $CopyWithPlaceholder() || fileSha256 == null
          ? _value.fileSha256
          // ignore: cast_nullable_to_non_nullable
          : fileSha256 as String,
      epoch: epoch == const $CopyWithPlaceholder() || epoch == null
          ? _value.epoch
          // ignore: cast_nullable_to_non_nullable
          : epoch as int,
      licence: licence == const $CopyWithPlaceholder() || licence == null
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as PopulationPackImportLicenceEnum,
      attribution:
          attribution == const $CopyWithPlaceholder() || attribution == null
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as PopulationPackImportAttributionEnum,
      cells: cells == const $CopyWithPlaceholder()
          ? _value.cells
          // ignore: cast_nullable_to_non_nullable
          : cells as PopulationPackImportCells?,
      importedAt:
          importedAt == const $CopyWithPlaceholder() || importedAt == null
          ? _value.importedAt
          // ignore: cast_nullable_to_non_nullable
          : importedAt as DateTime,
      durationMs: durationMs == const $CopyWithPlaceholder()
          ? _value.durationMs
          // ignore: cast_nullable_to_non_nullable
          : durationMs as int?,
    );
  }
}

extension $PopulationPackImportCopyWith on PopulationPackImport {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPopulationPackImport.copyWith(...)` or `instanceOfPopulationPackImport.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PopulationPackImportCWProxy get copyWith =>
      _$PopulationPackImportCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PopulationPackImport _$PopulationPackImportFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PopulationPackImport', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'fileSha256',
      'epoch',
      'licence',
      'attribution',
      'importedAt',
    ],
  );
  final val = PopulationPackImport(
    id: $checkedConvert('id', (v) => v as String),
    path: $checkedConvert('path', (v) => v as String?),
    fileSha256: $checkedConvert('fileSha256', (v) => v as String),
    epoch: $checkedConvert('epoch', (v) => (v as num).toInt()),
    licence: $checkedConvert(
      'licence',
      (v) => $enumDecode(_$PopulationPackImportLicenceEnumEnumMap, v),
    ),
    attribution: $checkedConvert(
      'attribution',
      (v) => $enumDecode(_$PopulationPackImportAttributionEnumEnumMap, v),
    ),
    cells: $checkedConvert(
      'cells',
      (v) => v == null
          ? null
          : PopulationPackImportCells.fromJson(v as Map<String, dynamic>),
    ),
    importedAt: $checkedConvert(
      'importedAt',
      (v) => DateTime.parse(v as String),
    ),
    durationMs: $checkedConvert('durationMs', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$PopulationPackImportToJson(
  PopulationPackImport instance,
) => <String, dynamic>{
  'id': instance.id,
  'path': ?instance.path,
  'fileSha256': instance.fileSha256,
  'epoch': instance.epoch,
  'licence': _$PopulationPackImportLicenceEnumEnumMap[instance.licence]!,
  'attribution':
      _$PopulationPackImportAttributionEnumEnumMap[instance.attribution]!,
  'cells': ?instance.cells?.toJson(),
  'importedAt': instance.importedAt.toIso8601String(),
  'durationMs': ?instance.durationMs,
};

const _$PopulationPackImportLicenceEnumEnumMap = {
  PopulationPackImportLicenceEnum.cCBY4Period0: 'CC-BY-4.0',
};

const _$PopulationPackImportAttributionEnumEnumMap = {
  PopulationPackImportAttributionEnum.europeanUnionCommaGHSL:
      '© European Union, GHSL',
};
