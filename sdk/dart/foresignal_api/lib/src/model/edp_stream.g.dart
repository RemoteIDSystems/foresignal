// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_stream.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpStreamCWProxy {
  EdpStream id(String id);

  EdpStream mode(EdpStreamModeEnum mode);

  EdpStream itemType(EdpStreamItemTypeEnum itemType);

  EdpStream kind(String kind);

  EdpStream title(String? title);

  EdpStream description(String? description);

  EdpStream geometry(EdpStreamGeometryEnum? geometry);

  EdpStream update(EdpStreamUpdateEnum? update);

  EdpStream payloadSchema(Map<String, Object>? payloadSchema);

  EdpStream fields(EdpStreamFields? fields);

  EdpStream ttl(String? ttl);

  EdpStream retentionClass(EdpStreamRetentionClassEnum? retentionClass);

  EdpStream unit(String? unit);

  EdpStream documentType(EdpStreamDocumentTypeEnum? documentType);

  EdpStream presentation(EdpPresentation? presentation);

  EdpStream coverage(EdpCoverage? coverage);

  EdpStream complete(bool? complete);

  EdpStream suggestedBindings(List<EdpSuggestedBinding>? suggestedBindings);

  EdpStream snapshotScope(String? snapshotScope);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpStream(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpStream(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpStream call({
    String id,
    EdpStreamModeEnum mode,
    EdpStreamItemTypeEnum itemType,
    String kind,
    String? title,
    String? description,
    EdpStreamGeometryEnum? geometry,
    EdpStreamUpdateEnum? update,
    Map<String, Object>? payloadSchema,
    EdpStreamFields? fields,
    String? ttl,
    EdpStreamRetentionClassEnum? retentionClass,
    String? unit,
    EdpStreamDocumentTypeEnum? documentType,
    EdpPresentation? presentation,
    EdpCoverage? coverage,
    bool? complete,
    List<EdpSuggestedBinding>? suggestedBindings,
    String? snapshotScope,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpStream.copyWith(...)` or call `instanceOfEdpStream.copyWith.fieldName(value)` for a single field.
class _$EdpStreamCWProxyImpl implements _$EdpStreamCWProxy {
  const _$EdpStreamCWProxyImpl(this._value);

  final EdpStream _value;

  @override
  EdpStream id(String id) => call(id: id);

  @override
  EdpStream mode(EdpStreamModeEnum mode) => call(mode: mode);

  @override
  EdpStream itemType(EdpStreamItemTypeEnum itemType) =>
      call(itemType: itemType);

  @override
  EdpStream kind(String kind) => call(kind: kind);

  @override
  EdpStream title(String? title) => call(title: title);

  @override
  EdpStream description(String? description) => call(description: description);

  @override
  EdpStream geometry(EdpStreamGeometryEnum? geometry) =>
      call(geometry: geometry);

  @override
  EdpStream update(EdpStreamUpdateEnum? update) => call(update: update);

  @override
  EdpStream payloadSchema(Map<String, Object>? payloadSchema) =>
      call(payloadSchema: payloadSchema);

  @override
  EdpStream fields(EdpStreamFields? fields) => call(fields: fields);

  @override
  EdpStream ttl(String? ttl) => call(ttl: ttl);

  @override
  EdpStream retentionClass(EdpStreamRetentionClassEnum? retentionClass) =>
      call(retentionClass: retentionClass);

  @override
  EdpStream unit(String? unit) => call(unit: unit);

  @override
  EdpStream documentType(EdpStreamDocumentTypeEnum? documentType) =>
      call(documentType: documentType);

  @override
  EdpStream presentation(EdpPresentation? presentation) =>
      call(presentation: presentation);

  @override
  EdpStream coverage(EdpCoverage? coverage) => call(coverage: coverage);

  @override
  EdpStream complete(bool? complete) => call(complete: complete);

  @override
  EdpStream suggestedBindings(List<EdpSuggestedBinding>? suggestedBindings) =>
      call(suggestedBindings: suggestedBindings);

  @override
  EdpStream snapshotScope(String? snapshotScope) =>
      call(snapshotScope: snapshotScope);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpStream(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpStream(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpStream call({
    Object? id = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? itemType = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? update = const $CopyWithPlaceholder(),
    Object? payloadSchema = const $CopyWithPlaceholder(),
    Object? fields = const $CopyWithPlaceholder(),
    Object? ttl = const $CopyWithPlaceholder(),
    Object? retentionClass = const $CopyWithPlaceholder(),
    Object? unit = const $CopyWithPlaceholder(),
    Object? documentType = const $CopyWithPlaceholder(),
    Object? presentation = const $CopyWithPlaceholder(),
    Object? coverage = const $CopyWithPlaceholder(),
    Object? complete = const $CopyWithPlaceholder(),
    Object? suggestedBindings = const $CopyWithPlaceholder(),
    Object? snapshotScope = const $CopyWithPlaceholder(),
  }) {
    return EdpStream(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      mode: mode == const $CopyWithPlaceholder() || mode == null
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as EdpStreamModeEnum,
      itemType: itemType == const $CopyWithPlaceholder() || itemType == null
          ? _value.itemType
          // ignore: cast_nullable_to_non_nullable
          : itemType as EdpStreamItemTypeEnum,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as EdpStreamGeometryEnum?,
      update: update == const $CopyWithPlaceholder()
          ? _value.update
          // ignore: cast_nullable_to_non_nullable
          : update as EdpStreamUpdateEnum?,
      payloadSchema: payloadSchema == const $CopyWithPlaceholder()
          ? _value.payloadSchema
          // ignore: cast_nullable_to_non_nullable
          : payloadSchema as Map<String, Object>?,
      fields: fields == const $CopyWithPlaceholder()
          ? _value.fields
          // ignore: cast_nullable_to_non_nullable
          : fields as EdpStreamFields?,
      ttl: ttl == const $CopyWithPlaceholder()
          ? _value.ttl
          // ignore: cast_nullable_to_non_nullable
          : ttl as String?,
      retentionClass: retentionClass == const $CopyWithPlaceholder()
          ? _value.retentionClass
          // ignore: cast_nullable_to_non_nullable
          : retentionClass as EdpStreamRetentionClassEnum?,
      unit: unit == const $CopyWithPlaceholder()
          ? _value.unit
          // ignore: cast_nullable_to_non_nullable
          : unit as String?,
      documentType: documentType == const $CopyWithPlaceholder()
          ? _value.documentType
          // ignore: cast_nullable_to_non_nullable
          : documentType as EdpStreamDocumentTypeEnum?,
      presentation: presentation == const $CopyWithPlaceholder()
          ? _value.presentation
          // ignore: cast_nullable_to_non_nullable
          : presentation as EdpPresentation?,
      coverage: coverage == const $CopyWithPlaceholder()
          ? _value.coverage
          // ignore: cast_nullable_to_non_nullable
          : coverage as EdpCoverage?,
      complete: complete == const $CopyWithPlaceholder()
          ? _value.complete
          // ignore: cast_nullable_to_non_nullable
          : complete as bool?,
      suggestedBindings: suggestedBindings == const $CopyWithPlaceholder()
          ? _value.suggestedBindings
          // ignore: cast_nullable_to_non_nullable
          : suggestedBindings as List<EdpSuggestedBinding>?,
      snapshotScope: snapshotScope == const $CopyWithPlaceholder()
          ? _value.snapshotScope
          // ignore: cast_nullable_to_non_nullable
          : snapshotScope as String?,
    );
  }
}

extension $EdpStreamCopyWith on EdpStream {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpStream.copyWith(...)` or `instanceOfEdpStream.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpStreamCWProxy get copyWith => _$EdpStreamCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpStream _$EdpStreamFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EdpStream', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'mode', 'itemType', 'kind']);
  final val = EdpStream(
    id: $checkedConvert('id', (v) => v as String),
    mode: $checkedConvert(
      'mode',
      (v) => $enumDecode(_$EdpStreamModeEnumEnumMap, v),
    ),
    itemType: $checkedConvert(
      'itemType',
      (v) => $enumDecode(_$EdpStreamItemTypeEnumEnumMap, v),
    ),
    kind: $checkedConvert('kind', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String?),
    description: $checkedConvert('description', (v) => v as String?),
    geometry: $checkedConvert(
      'geometry',
      (v) => $enumDecodeNullable(_$EdpStreamGeometryEnumEnumMap, v),
    ),
    update: $checkedConvert(
      'update',
      (v) => $enumDecodeNullable(_$EdpStreamUpdateEnumEnumMap, v),
    ),
    payloadSchema: $checkedConvert(
      'payloadSchema',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    fields: $checkedConvert(
      'fields',
      (v) => v == null
          ? null
          : EdpStreamFields.fromJson(v as Map<String, dynamic>),
    ),
    ttl: $checkedConvert('ttl', (v) => v as String?),
    retentionClass: $checkedConvert(
      'retentionClass',
      (v) => $enumDecodeNullable(_$EdpStreamRetentionClassEnumEnumMap, v),
    ),
    unit: $checkedConvert('unit', (v) => v as String?),
    documentType: $checkedConvert(
      'documentType',
      (v) => $enumDecodeNullable(_$EdpStreamDocumentTypeEnumEnumMap, v),
    ),
    presentation: $checkedConvert(
      'presentation',
      (v) => v == null
          ? null
          : EdpPresentation.fromJson(v as Map<String, dynamic>),
    ),
    coverage: $checkedConvert(
      'coverage',
      (v) => v == null ? null : EdpCoverage.fromJson(v as Map<String, dynamic>),
    ),
    complete: $checkedConvert('complete', (v) => v as bool? ?? false),
    suggestedBindings: $checkedConvert(
      'suggestedBindings',
      (v) => (v as List<dynamic>?)
          ?.map((e) => EdpSuggestedBinding.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    snapshotScope: $checkedConvert('snapshotScope', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$EdpStreamToJson(EdpStream instance) => <String, dynamic>{
  'id': instance.id,
  'mode': _$EdpStreamModeEnumEnumMap[instance.mode]!,
  'itemType': _$EdpStreamItemTypeEnumEnumMap[instance.itemType]!,
  'kind': instance.kind,
  'title': ?instance.title,
  'description': ?instance.description,
  'geometry': ?_$EdpStreamGeometryEnumEnumMap[instance.geometry],
  'update': ?_$EdpStreamUpdateEnumEnumMap[instance.update],
  'payloadSchema': ?instance.payloadSchema,
  'fields': ?instance.fields?.toJson(),
  'ttl': ?instance.ttl,
  'retentionClass':
      ?_$EdpStreamRetentionClassEnumEnumMap[instance.retentionClass],
  'unit': ?instance.unit,
  'documentType': ?_$EdpStreamDocumentTypeEnumEnumMap[instance.documentType],
  'presentation': ?instance.presentation?.toJson(),
  'coverage': ?instance.coverage?.toJson(),
  'complete': ?instance.complete,
  'suggestedBindings': ?instance.suggestedBindings
      ?.map((e) => e.toJson())
      .toList(),
  'snapshotScope': ?instance.snapshotScope,
};

const _$EdpStreamModeEnumEnumMap = {
  EdpStreamModeEnum.layer: 'layer',
  EdpStreamModeEnum.atomic: 'atomic',
};

const _$EdpStreamItemTypeEnumEnumMap = {
  EdpStreamItemTypeEnum.feature: 'feature',
  EdpStreamItemTypeEnum.event: 'event',
  EdpStreamItemTypeEnum.observation: 'observation',
  EdpStreamItemTypeEnum.document: 'document',
  EdpStreamItemTypeEnum.forecast: 'forecast',
};

const _$EdpStreamGeometryEnumEnumMap = {
  EdpStreamGeometryEnum.point: 'Point',
  EdpStreamGeometryEnum.lineString: 'LineString',
  EdpStreamGeometryEnum.polygon: 'Polygon',
  EdpStreamGeometryEnum.multiPolygon: 'MultiPolygon',
  EdpStreamGeometryEnum.any: 'any',
  EdpStreamGeometryEnum.none: 'none',
};

const _$EdpStreamUpdateEnumEnumMap = {
  EdpStreamUpdateEnum.incremental: 'incremental',
  EdpStreamUpdateEnum.snapshot: 'snapshot',
};

const _$EdpStreamRetentionClassEnumEnumMap = {
  EdpStreamRetentionClassEnum.event: 'event',
  EdpStreamRetentionClassEnum.bulkEvent: 'bulk-event',
  EdpStreamRetentionClassEnum.series: 'series',
  EdpStreamRetentionClassEnum.state: 'state',
  EdpStreamRetentionClassEnum.track: 'track',
  EdpStreamRetentionClassEnum.reference: 'reference',
  EdpStreamRetentionClassEnum.social: 'social',
};

const _$EdpStreamDocumentTypeEnumEnumMap = {
  EdpStreamDocumentTypeEnum.socialPost: 'social-post',
  EdpStreamDocumentTypeEnum.article: 'article',
  EdpStreamDocumentTypeEnum.report: 'report',
  EdpStreamDocumentTypeEnum.transcript: 'transcript',
  EdpStreamDocumentTypeEnum.note: 'note',
};
