// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportCWProxy {
  Report id(String id);

  Report ref(String ref);

  Report displayId(String displayId);

  Report title(String title);

  Report subtitle(String? subtitle);

  Report status(ReportStatusEnum status);

  Report owner(PrincipalSummary owner);

  Report authors(List<PrincipalSummary> authors);

  Report tlp(Tlp tlp);

  Report visibility(Visibility visibility);

  Report source_(ReportSource source_);

  Report template(ReportTemplateEnum? template);

  Report timeWindow(TimeRange? timeWindow);

  Report area(Geometry? area);

  Report blocks(List<ReportBlock> blocks);

  Report machineGenerated(bool? machineGenerated);

  Report version(int version);

  Report publishedRevision(int? publishedRevision);

  Report publishedAt(DateTime? publishedAt);

  Report manifestId(String? manifestId);

  Report provenanceChain(List<ProvenanceChainEntry>? provenanceChain);

  Report tags(List<String>? tags);

  Report createdAt(DateTime createdAt);

  Report updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Report(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Report(...).copyWith(id: 12, name: "My name")
  /// ```
  Report call({
    String id,
    String ref,
    String displayId,
    String title,
    String? subtitle,
    ReportStatusEnum status,
    PrincipalSummary owner,
    List<PrincipalSummary> authors,
    Tlp tlp,
    Visibility visibility,
    ReportSource source_,
    ReportTemplateEnum? template,
    TimeRange? timeWindow,
    Geometry? area,
    List<ReportBlock> blocks,
    bool? machineGenerated,
    int version,
    int? publishedRevision,
    DateTime? publishedAt,
    String? manifestId,
    List<ProvenanceChainEntry>? provenanceChain,
    List<String>? tags,
    DateTime createdAt,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReport.copyWith(...)` or call `instanceOfReport.copyWith.fieldName(value)` for a single field.
class _$ReportCWProxyImpl implements _$ReportCWProxy {
  const _$ReportCWProxyImpl(this._value);

  final Report _value;

  @override
  Report id(String id) => call(id: id);

  @override
  Report ref(String ref) => call(ref: ref);

  @override
  Report displayId(String displayId) => call(displayId: displayId);

  @override
  Report title(String title) => call(title: title);

  @override
  Report subtitle(String? subtitle) => call(subtitle: subtitle);

  @override
  Report status(ReportStatusEnum status) => call(status: status);

  @override
  Report owner(PrincipalSummary owner) => call(owner: owner);

  @override
  Report authors(List<PrincipalSummary> authors) => call(authors: authors);

  @override
  Report tlp(Tlp tlp) => call(tlp: tlp);

  @override
  Report visibility(Visibility visibility) => call(visibility: visibility);

  @override
  Report source_(ReportSource source_) => call(source_: source_);

  @override
  Report template(ReportTemplateEnum? template) => call(template: template);

  @override
  Report timeWindow(TimeRange? timeWindow) => call(timeWindow: timeWindow);

  @override
  Report area(Geometry? area) => call(area: area);

  @override
  Report blocks(List<ReportBlock> blocks) => call(blocks: blocks);

  @override
  Report machineGenerated(bool? machineGenerated) =>
      call(machineGenerated: machineGenerated);

  @override
  Report version(int version) => call(version: version);

  @override
  Report publishedRevision(int? publishedRevision) =>
      call(publishedRevision: publishedRevision);

  @override
  Report publishedAt(DateTime? publishedAt) => call(publishedAt: publishedAt);

  @override
  Report manifestId(String? manifestId) => call(manifestId: manifestId);

  @override
  Report provenanceChain(List<ProvenanceChainEntry>? provenanceChain) =>
      call(provenanceChain: provenanceChain);

  @override
  Report tags(List<String>? tags) => call(tags: tags);

  @override
  Report createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Report updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Report(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Report(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Report call({
    Object? id = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? displayId = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? subtitle = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? owner = const $CopyWithPlaceholder(),
    Object? authors = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? template = const $CopyWithPlaceholder(),
    Object? timeWindow = const $CopyWithPlaceholder(),
    Object? area = const $CopyWithPlaceholder(),
    Object? blocks = const $CopyWithPlaceholder(),
    Object? machineGenerated = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? publishedRevision = const $CopyWithPlaceholder(),
    Object? publishedAt = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? provenanceChain = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return Report(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      displayId: displayId == const $CopyWithPlaceholder() || displayId == null
          ? _value.displayId
          // ignore: cast_nullable_to_non_nullable
          : displayId as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      subtitle: subtitle == const $CopyWithPlaceholder()
          ? _value.subtitle
          // ignore: cast_nullable_to_non_nullable
          : subtitle as String?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ReportStatusEnum,
      owner: owner == const $CopyWithPlaceholder() || owner == null
          ? _value.owner
          // ignore: cast_nullable_to_non_nullable
          : owner as PrincipalSummary,
      authors: authors == const $CopyWithPlaceholder() || authors == null
          ? _value.authors
          // ignore: cast_nullable_to_non_nullable
          : authors as List<PrincipalSummary>,
      tlp: tlp == const $CopyWithPlaceholder() || tlp == null
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp,
      visibility:
          visibility == const $CopyWithPlaceholder() || visibility == null
          ? _value.visibility
          // ignore: cast_nullable_to_non_nullable
          : visibility as Visibility,
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as ReportSource,
      template: template == const $CopyWithPlaceholder()
          ? _value.template
          // ignore: cast_nullable_to_non_nullable
          : template as ReportTemplateEnum?,
      timeWindow: timeWindow == const $CopyWithPlaceholder()
          ? _value.timeWindow
          // ignore: cast_nullable_to_non_nullable
          : timeWindow as TimeRange?,
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as Geometry?,
      blocks: blocks == const $CopyWithPlaceholder() || blocks == null
          ? _value.blocks
          // ignore: cast_nullable_to_non_nullable
          : blocks as List<ReportBlock>,
      machineGenerated: machineGenerated == const $CopyWithPlaceholder()
          ? _value.machineGenerated
          // ignore: cast_nullable_to_non_nullable
          : machineGenerated as bool?,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      publishedRevision: publishedRevision == const $CopyWithPlaceholder()
          ? _value.publishedRevision
          // ignore: cast_nullable_to_non_nullable
          : publishedRevision as int?,
      publishedAt: publishedAt == const $CopyWithPlaceholder()
          ? _value.publishedAt
          // ignore: cast_nullable_to_non_nullable
          : publishedAt as DateTime?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
      provenanceChain: provenanceChain == const $CopyWithPlaceholder()
          ? _value.provenanceChain
          // ignore: cast_nullable_to_non_nullable
          : provenanceChain as List<ProvenanceChainEntry>?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $ReportCopyWith on Report {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReport.copyWith(...)` or `instanceOfReport.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportCWProxy get copyWith => _$ReportCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Report _$ReportFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Report', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'ref',
      'displayId',
      'title',
      'status',
      'owner',
      'authors',
      'tlp',
      'visibility',
      'source',
      'blocks',
      'version',
      'createdAt',
    ],
  );
  final val = Report(
    id: $checkedConvert('id', (v) => v as String),
    ref: $checkedConvert('ref', (v) => v as String),
    displayId: $checkedConvert('displayId', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String),
    subtitle: $checkedConvert('subtitle', (v) => v as String?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$ReportStatusEnumEnumMap, v),
    ),
    owner: $checkedConvert(
      'owner',
      (v) => PrincipalSummary.fromJson(v as Map<String, dynamic>),
    ),
    authors: $checkedConvert(
      'authors',
      (v) => (v as List<dynamic>)
          .map((e) => PrincipalSummary.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    tlp: $checkedConvert('tlp', (v) => $enumDecode(_$TlpEnumMap, v)),
    visibility: $checkedConvert(
      'visibility',
      (v) => $enumDecode(_$VisibilityEnumMap, v),
    ),
    source_: $checkedConvert(
      'source',
      (v) => ReportSource.fromJson(v as Map<String, dynamic>),
    ),
    template: $checkedConvert(
      'template',
      (v) => $enumDecodeNullable(_$ReportTemplateEnumEnumMap, v),
    ),
    timeWindow: $checkedConvert(
      'timeWindow',
      (v) => v == null ? null : TimeRange.fromJson(v as Map<String, dynamic>),
    ),
    area: $checkedConvert(
      'area',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    blocks: $checkedConvert(
      'blocks',
      (v) => (v as List<dynamic>)
          .map((e) => ReportBlock.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    machineGenerated: $checkedConvert('machineGenerated', (v) => v as bool?),
    version: $checkedConvert('version', (v) => (v as num).toInt()),
    publishedRevision: $checkedConvert(
      'publishedRevision',
      (v) => (v as num?)?.toInt(),
    ),
    publishedAt: $checkedConvert(
      'publishedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
    provenanceChain: $checkedConvert(
      'provenanceChain',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ProvenanceChainEntry.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    tags: $checkedConvert(
      'tags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$ReportToJson(Report instance) => <String, dynamic>{
  'id': instance.id,
  'ref': instance.ref,
  'displayId': instance.displayId,
  'title': instance.title,
  'subtitle': ?instance.subtitle,
  'status': _$ReportStatusEnumEnumMap[instance.status]!,
  'owner': instance.owner.toJson(),
  'authors': instance.authors.map((e) => e.toJson()).toList(),
  'tlp': _$TlpEnumMap[instance.tlp]!,
  'visibility': _$VisibilityEnumMap[instance.visibility]!,
  'source': instance.source_.toJson(),
  'template': ?_$ReportTemplateEnumEnumMap[instance.template],
  'timeWindow': ?instance.timeWindow?.toJson(),
  'area': ?instance.area?.toJson(),
  'blocks': instance.blocks.map((e) => e.toJson()).toList(),
  'machineGenerated': ?instance.machineGenerated,
  'version': instance.version,
  'publishedRevision': ?instance.publishedRevision,
  'publishedAt': ?instance.publishedAt?.toIso8601String(),
  'manifestId': ?instance.manifestId,
  'provenanceChain': ?instance.provenanceChain?.map((e) => e.toJson()).toList(),
  'tags': ?instance.tags,
  'createdAt': instance.createdAt.toIso8601String(),
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
};

const _$ReportStatusEnumEnumMap = {
  ReportStatusEnum.draft: 'draft',
  ReportStatusEnum.review: 'review',
  ReportStatusEnum.published: 'published',
  ReportStatusEnum.retracted: 'retracted',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};

const _$VisibilityEnumMap = {
  Visibility.private: 'private',
  Visibility.shared: 'shared',
  Visibility.instance: 'instance',
};

const _$ReportTemplateEnumEnumMap = {
  ReportTemplateEnum.situationBrief: 'situation-brief',
  ReportTemplateEnum.investigationSummary: 'investigation-summary',
  ReportTemplateEnum.chronicleRecap: 'chronicle-recap',
  ReportTemplateEnum.weeklyAssessment: 'weekly-assessment',
  ReportTemplateEnum.custom: 'custom',
};
