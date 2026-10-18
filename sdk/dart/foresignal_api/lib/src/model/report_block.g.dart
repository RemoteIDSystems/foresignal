// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockCWProxy {
  ReportBlock id(String? id);

  ReportBlock type(ReportBlockTypeEnum type);

  ReportBlock title(String? title);

  ReportBlock textOrigin(ReportBlockTextOriginEnum? textOrigin);

  ReportBlock updatedAt(DateTime? updatedAt);

  ReportBlock updatedBy(PrincipalSummary? updatedBy);

  ReportBlock markdown(String markdown);

  ReportBlock citations(List<String>? citations);

  ReportBlock judgments(List<ReportBlockOneOf1AllOfJudgmentsInner> judgments);

  ReportBlock items(List<ReportBlockOneOf7AllOfItemsInner> items);

  ReportBlock layers(List<String>? layers);

  ReportBlock area(Geometry? area);

  ReportBlock bbox(List<num>? bbox);

  ReportBlock timeRange(TimeRange? timeRange);

  ReportBlock attachmentId(String? attachmentId);

  ReportBlock mediaId(String? mediaId);

  ReportBlock caveat(String? caveat);

  ReportBlock sourceRef(String? sourceRef);

  ReportBlock hypothesisSetRef(String hypothesisSetRef);

  ReportBlock snapshot(Map<String, Object>? snapshot);

  ReportBlock revisionIds(List<String>? revisionIds);

  ReportBlock questions(List<ReportBlockOneOf6AllOfQuestionsInner> questions);

  ReportBlock caveats(List<Caveat>? caveats);

  ReportBlock coverage(List<CoverageStats>? coverage);

  ReportBlock attachmentIds(List<String> attachmentIds);

  ReportBlock attributions(
    List<ReportBlockOneOf10AllOfAttributionsInner> attributions,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlock(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlock(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlock call({
    String? id,
    ReportBlockTypeEnum type,
    String? title,
    ReportBlockTextOriginEnum? textOrigin,
    DateTime? updatedAt,
    PrincipalSummary? updatedBy,
    String markdown,
    List<String>? citations,
    List<ReportBlockOneOf1AllOfJudgmentsInner> judgments,
    List<ReportBlockOneOf7AllOfItemsInner> items,
    List<String>? layers,
    Geometry? area,
    List<num>? bbox,
    TimeRange? timeRange,
    String? attachmentId,
    String? mediaId,
    String? caveat,
    String? sourceRef,
    String hypothesisSetRef,
    Map<String, Object>? snapshot,
    List<String>? revisionIds,
    List<ReportBlockOneOf6AllOfQuestionsInner> questions,
    List<Caveat>? caveats,
    List<CoverageStats>? coverage,
    List<String> attachmentIds,
    List<ReportBlockOneOf10AllOfAttributionsInner> attributions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlock.copyWith(...)` or call `instanceOfReportBlock.copyWith.fieldName(value)` for a single field.
class _$ReportBlockCWProxyImpl implements _$ReportBlockCWProxy {
  const _$ReportBlockCWProxyImpl(this._value);

  final ReportBlock _value;

  @override
  ReportBlock id(String? id) => call(id: id);

  @override
  ReportBlock type(ReportBlockTypeEnum type) => call(type: type);

  @override
  ReportBlock title(String? title) => call(title: title);

  @override
  ReportBlock textOrigin(ReportBlockTextOriginEnum? textOrigin) =>
      call(textOrigin: textOrigin);

  @override
  ReportBlock updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  @override
  ReportBlock updatedBy(PrincipalSummary? updatedBy) =>
      call(updatedBy: updatedBy);

  @override
  ReportBlock markdown(String markdown) => call(markdown: markdown);

  @override
  ReportBlock citations(List<String>? citations) => call(citations: citations);

  @override
  ReportBlock judgments(List<ReportBlockOneOf1AllOfJudgmentsInner> judgments) =>
      call(judgments: judgments);

  @override
  ReportBlock items(List<ReportBlockOneOf7AllOfItemsInner> items) =>
      call(items: items);

  @override
  ReportBlock layers(List<String>? layers) => call(layers: layers);

  @override
  ReportBlock area(Geometry? area) => call(area: area);

  @override
  ReportBlock bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  ReportBlock timeRange(TimeRange? timeRange) => call(timeRange: timeRange);

  @override
  ReportBlock attachmentId(String? attachmentId) =>
      call(attachmentId: attachmentId);

  @override
  ReportBlock mediaId(String? mediaId) => call(mediaId: mediaId);

  @override
  ReportBlock caveat(String? caveat) => call(caveat: caveat);

  @override
  ReportBlock sourceRef(String? sourceRef) => call(sourceRef: sourceRef);

  @override
  ReportBlock hypothesisSetRef(String hypothesisSetRef) =>
      call(hypothesisSetRef: hypothesisSetRef);

  @override
  ReportBlock snapshot(Map<String, Object>? snapshot) =>
      call(snapshot: snapshot);

  @override
  ReportBlock revisionIds(List<String>? revisionIds) =>
      call(revisionIds: revisionIds);

  @override
  ReportBlock questions(List<ReportBlockOneOf6AllOfQuestionsInner> questions) =>
      call(questions: questions);

  @override
  ReportBlock caveats(List<Caveat>? caveats) => call(caveats: caveats);

  @override
  ReportBlock coverage(List<CoverageStats>? coverage) =>
      call(coverage: coverage);

  @override
  ReportBlock attachmentIds(List<String> attachmentIds) =>
      call(attachmentIds: attachmentIds);

  @override
  ReportBlock attributions(
    List<ReportBlockOneOf10AllOfAttributionsInner> attributions,
  ) => call(attributions: attributions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlock(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlock(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlock call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? updatedBy = const $CopyWithPlaceholder(),
    Object? markdown = const $CopyWithPlaceholder(),
    Object? citations = const $CopyWithPlaceholder(),
    Object? judgments = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
    Object? layers = const $CopyWithPlaceholder(),
    Object? area = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? timeRange = const $CopyWithPlaceholder(),
    Object? attachmentId = const $CopyWithPlaceholder(),
    Object? mediaId = const $CopyWithPlaceholder(),
    Object? caveat = const $CopyWithPlaceholder(),
    Object? sourceRef = const $CopyWithPlaceholder(),
    Object? hypothesisSetRef = const $CopyWithPlaceholder(),
    Object? snapshot = const $CopyWithPlaceholder(),
    Object? revisionIds = const $CopyWithPlaceholder(),
    Object? questions = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
    Object? coverage = const $CopyWithPlaceholder(),
    Object? attachmentIds = const $CopyWithPlaceholder(),
    Object? attributions = const $CopyWithPlaceholder(),
  }) {
    return ReportBlock(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ReportBlockTypeEnum,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      textOrigin: textOrigin == const $CopyWithPlaceholder()
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as ReportBlockTextOriginEnum?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      updatedBy: updatedBy == const $CopyWithPlaceholder()
          ? _value.updatedBy
          // ignore: cast_nullable_to_non_nullable
          : updatedBy as PrincipalSummary?,
      markdown: markdown == const $CopyWithPlaceholder() || markdown == null
          ? _value.markdown
          // ignore: cast_nullable_to_non_nullable
          : markdown as String,
      citations: citations == const $CopyWithPlaceholder()
          ? _value.citations
          // ignore: cast_nullable_to_non_nullable
          : citations as List<String>?,
      judgments: judgments == const $CopyWithPlaceholder() || judgments == null
          ? _value.judgments
          // ignore: cast_nullable_to_non_nullable
          : judgments as List<ReportBlockOneOf1AllOfJudgmentsInner>,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ReportBlockOneOf7AllOfItemsInner>,
      layers: layers == const $CopyWithPlaceholder()
          ? _value.layers
          // ignore: cast_nullable_to_non_nullable
          : layers as List<String>?,
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as Geometry?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      timeRange: timeRange == const $CopyWithPlaceholder()
          ? _value.timeRange
          // ignore: cast_nullable_to_non_nullable
          : timeRange as TimeRange?,
      attachmentId: attachmentId == const $CopyWithPlaceholder()
          ? _value.attachmentId
          // ignore: cast_nullable_to_non_nullable
          : attachmentId as String?,
      mediaId: mediaId == const $CopyWithPlaceholder()
          ? _value.mediaId
          // ignore: cast_nullable_to_non_nullable
          : mediaId as String?,
      caveat: caveat == const $CopyWithPlaceholder()
          ? _value.caveat
          // ignore: cast_nullable_to_non_nullable
          : caveat as String?,
      sourceRef: sourceRef == const $CopyWithPlaceholder()
          ? _value.sourceRef
          // ignore: cast_nullable_to_non_nullable
          : sourceRef as String?,
      hypothesisSetRef:
          hypothesisSetRef == const $CopyWithPlaceholder() ||
              hypothesisSetRef == null
          ? _value.hypothesisSetRef
          // ignore: cast_nullable_to_non_nullable
          : hypothesisSetRef as String,
      snapshot: snapshot == const $CopyWithPlaceholder()
          ? _value.snapshot
          // ignore: cast_nullable_to_non_nullable
          : snapshot as Map<String, Object>?,
      revisionIds: revisionIds == const $CopyWithPlaceholder()
          ? _value.revisionIds
          // ignore: cast_nullable_to_non_nullable
          : revisionIds as List<String>?,
      questions: questions == const $CopyWithPlaceholder() || questions == null
          ? _value.questions
          // ignore: cast_nullable_to_non_nullable
          : questions as List<ReportBlockOneOf6AllOfQuestionsInner>,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
      coverage: coverage == const $CopyWithPlaceholder()
          ? _value.coverage
          // ignore: cast_nullable_to_non_nullable
          : coverage as List<CoverageStats>?,
      attachmentIds:
          attachmentIds == const $CopyWithPlaceholder() || attachmentIds == null
          ? _value.attachmentIds
          // ignore: cast_nullable_to_non_nullable
          : attachmentIds as List<String>,
      attributions:
          attributions == const $CopyWithPlaceholder() || attributions == null
          ? _value.attributions
          // ignore: cast_nullable_to_non_nullable
          : attributions as List<ReportBlockOneOf10AllOfAttributionsInner>,
    );
  }
}

extension $ReportBlockCopyWith on ReportBlock {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlock.copyWith(...)` or `instanceOfReportBlock.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockCWProxy get copyWith => _$ReportBlockCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlock _$ReportBlockFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReportBlock', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'type',
      'markdown',
      'judgments',
      'items',
      'hypothesisSetRef',
      'questions',
      'attachmentIds',
      'attributions',
    ],
  );
  final val = ReportBlock(
    id: $checkedConvert('id', (v) => v as String?),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ReportBlockTypeEnumEnumMap, v),
    ),
    title: $checkedConvert('title', (v) => v as String?),
    textOrigin: $checkedConvert(
      'textOrigin',
      (v) => $enumDecodeNullable(_$ReportBlockTextOriginEnumEnumMap, v),
    ),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    updatedBy: $checkedConvert(
      'updatedBy',
      (v) => v == null
          ? null
          : PrincipalSummary.fromJson(v as Map<String, dynamic>),
    ),
    markdown: $checkedConvert('markdown', (v) => v as String),
    citations: $checkedConvert(
      'citations',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    judgments: $checkedConvert(
      'judgments',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ReportBlockOneOf1AllOfJudgmentsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ReportBlockOneOf7AllOfItemsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    layers: $checkedConvert(
      'layers',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    area: $checkedConvert(
      'area',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    bbox: $checkedConvert(
      'bbox',
      (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
    ),
    timeRange: $checkedConvert(
      'timeRange',
      (v) => v == null ? null : TimeRange.fromJson(v as Map<String, dynamic>),
    ),
    attachmentId: $checkedConvert('attachmentId', (v) => v as String?),
    mediaId: $checkedConvert('mediaId', (v) => v as String?),
    caveat: $checkedConvert('caveat', (v) => v as String?),
    sourceRef: $checkedConvert('sourceRef', (v) => v as String?),
    hypothesisSetRef: $checkedConvert('hypothesisSetRef', (v) => v as String),
    snapshot: $checkedConvert(
      'snapshot',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    revisionIds: $checkedConvert(
      'revisionIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    questions: $checkedConvert(
      'questions',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ReportBlockOneOf6AllOfQuestionsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    caveats: $checkedConvert(
      'caveats',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    coverage: $checkedConvert(
      'coverage',
      (v) => (v as List<dynamic>?)
          ?.map((e) => CoverageStats.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    attachmentIds: $checkedConvert(
      'attachmentIds',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    attributions: $checkedConvert(
      'attributions',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ReportBlockOneOf10AllOfAttributionsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ReportBlockToJson(ReportBlock instance) =>
    <String, dynamic>{
      'id': ?instance.id,
      'type': _$ReportBlockTypeEnumEnumMap[instance.type]!,
      'title': ?instance.title,
      'textOrigin': ?_$ReportBlockTextOriginEnumEnumMap[instance.textOrigin],
      'updatedAt': ?instance.updatedAt?.toIso8601String(),
      'updatedBy': ?instance.updatedBy?.toJson(),
      'markdown': instance.markdown,
      'citations': ?instance.citations,
      'judgments': instance.judgments.map((e) => e.toJson()).toList(),
      'items': instance.items.map((e) => e.toJson()).toList(),
      'layers': ?instance.layers,
      'area': ?instance.area?.toJson(),
      'bbox': ?instance.bbox,
      'timeRange': ?instance.timeRange?.toJson(),
      'attachmentId': ?instance.attachmentId,
      'mediaId': ?instance.mediaId,
      'caveat': ?instance.caveat,
      'sourceRef': ?instance.sourceRef,
      'hypothesisSetRef': instance.hypothesisSetRef,
      'snapshot': ?instance.snapshot,
      'revisionIds': ?instance.revisionIds,
      'questions': instance.questions.map((e) => e.toJson()).toList(),
      'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
      'coverage': ?instance.coverage?.map((e) => e.toJson()).toList(),
      'attachmentIds': instance.attachmentIds,
      'attributions': instance.attributions.map((e) => e.toJson()).toList(),
    };

const _$ReportBlockTypeEnumEnumMap = {
  ReportBlockTypeEnum.markdown: 'markdown',
  ReportBlockTypeEnum.bluf: 'bluf',
  ReportBlockTypeEnum.objects: 'objects',
  ReportBlockTypeEnum.map: 'map',
  ReportBlockTypeEnum.timeline: 'timeline',
  ReportBlockTypeEnum.hypotheses: 'hypotheses',
  ReportBlockTypeEnum.forecasts: 'forecasts',
  ReportBlockTypeEnum.evidence: 'evidence',
  ReportBlockTypeEnum.coverage: 'coverage',
  ReportBlockTypeEnum.attachments: 'attachments',
  ReportBlockTypeEnum.sources: 'sources',
};

const _$ReportBlockTextOriginEnumEnumMap = {
  ReportBlockTextOriginEnum.author: 'author',
  ReportBlockTextOriginEnum.deterministic: 'deterministic',
  ReportBlockTextOriginEnum.llm: 'llm',
};
