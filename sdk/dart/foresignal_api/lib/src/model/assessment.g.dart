// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'assessment.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AssessmentCWProxy {
  Assessment id(String id);

  Assessment ref(String? ref);

  Assessment cycleId(String? cycleId);

  Assessment situationId(String? situationId);

  Assessment cadence(AssessmentCadenceEnum cadence);

  Assessment periodStart(DateTime periodStart);

  Assessment periodEnd(DateTime periodEnd);

  Assessment previousAssessmentId(String? previousAssessmentId);

  Assessment status(AssessmentStatusEnum status);

  Assessment bluf(List<KeyJudgment> bluf);

  Assessment headline(String? headline);

  Assessment sections(List<AssessmentSectionsInner> sections);

  Assessment markdown(String? markdown);

  Assessment html(String? html);

  Assessment packetRef(String? packetRef);

  Assessment roles(Map<String, CycleRunRolesInner>? roles);

  Assessment model(String? model);

  Assessment effort(AssessmentEffortEnum? effort);

  Assessment manifestId(String? manifestId);

  Assessment promptTemplates(
    List<AssessmentPromptTemplatesInner>? promptTemplates,
  );

  Assessment citations(List<String>? citations);

  Assessment lintWarnings(List<String>? lintWarnings);

  Assessment footer(String? footer);

  Assessment private(bool private);

  Assessment createdAt(DateTime createdAt);

  Assessment coverage(AssessmentCoverage? coverage);

  Assessment social(SocialVersusObserved? social);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Assessment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Assessment(...).copyWith(id: 12, name: "My name")
  /// ```
  Assessment call({
    String id,
    String? ref,
    String? cycleId,
    String? situationId,
    AssessmentCadenceEnum cadence,
    DateTime periodStart,
    DateTime periodEnd,
    String? previousAssessmentId,
    AssessmentStatusEnum status,
    List<KeyJudgment> bluf,
    String? headline,
    List<AssessmentSectionsInner> sections,
    String? markdown,
    String? html,
    String? packetRef,
    Map<String, CycleRunRolesInner>? roles,
    String? model,
    AssessmentEffortEnum? effort,
    String? manifestId,
    List<AssessmentPromptTemplatesInner>? promptTemplates,
    List<String>? citations,
    List<String>? lintWarnings,
    String? footer,
    bool private,
    DateTime createdAt,
    AssessmentCoverage? coverage,
    SocialVersusObserved? social,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAssessment.copyWith(...)` or call `instanceOfAssessment.copyWith.fieldName(value)` for a single field.
class _$AssessmentCWProxyImpl implements _$AssessmentCWProxy {
  const _$AssessmentCWProxyImpl(this._value);

  final Assessment _value;

  @override
  Assessment id(String id) => call(id: id);

  @override
  Assessment ref(String? ref) => call(ref: ref);

  @override
  Assessment cycleId(String? cycleId) => call(cycleId: cycleId);

  @override
  Assessment situationId(String? situationId) => call(situationId: situationId);

  @override
  Assessment cadence(AssessmentCadenceEnum cadence) => call(cadence: cadence);

  @override
  Assessment periodStart(DateTime periodStart) =>
      call(periodStart: periodStart);

  @override
  Assessment periodEnd(DateTime periodEnd) => call(periodEnd: periodEnd);

  @override
  Assessment previousAssessmentId(String? previousAssessmentId) =>
      call(previousAssessmentId: previousAssessmentId);

  @override
  Assessment status(AssessmentStatusEnum status) => call(status: status);

  @override
  Assessment bluf(List<KeyJudgment> bluf) => call(bluf: bluf);

  @override
  Assessment headline(String? headline) => call(headline: headline);

  @override
  Assessment sections(List<AssessmentSectionsInner> sections) =>
      call(sections: sections);

  @override
  Assessment markdown(String? markdown) => call(markdown: markdown);

  @override
  Assessment html(String? html) => call(html: html);

  @override
  Assessment packetRef(String? packetRef) => call(packetRef: packetRef);

  @override
  Assessment roles(Map<String, CycleRunRolesInner>? roles) =>
      call(roles: roles);

  @override
  Assessment model(String? model) => call(model: model);

  @override
  Assessment effort(AssessmentEffortEnum? effort) => call(effort: effort);

  @override
  Assessment manifestId(String? manifestId) => call(manifestId: manifestId);

  @override
  Assessment promptTemplates(
    List<AssessmentPromptTemplatesInner>? promptTemplates,
  ) => call(promptTemplates: promptTemplates);

  @override
  Assessment citations(List<String>? citations) => call(citations: citations);

  @override
  Assessment lintWarnings(List<String>? lintWarnings) =>
      call(lintWarnings: lintWarnings);

  @override
  Assessment footer(String? footer) => call(footer: footer);

  @override
  Assessment private(bool private) => call(private: private);

  @override
  Assessment createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Assessment coverage(AssessmentCoverage? coverage) => call(coverage: coverage);

  @override
  Assessment social(SocialVersusObserved? social) => call(social: social);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Assessment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Assessment(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Assessment call({
    Object? id = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? cycleId = const $CopyWithPlaceholder(),
    Object? situationId = const $CopyWithPlaceholder(),
    Object? cadence = const $CopyWithPlaceholder(),
    Object? periodStart = const $CopyWithPlaceholder(),
    Object? periodEnd = const $CopyWithPlaceholder(),
    Object? previousAssessmentId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? bluf = const $CopyWithPlaceholder(),
    Object? headline = const $CopyWithPlaceholder(),
    Object? sections = const $CopyWithPlaceholder(),
    Object? markdown = const $CopyWithPlaceholder(),
    Object? html = const $CopyWithPlaceholder(),
    Object? packetRef = const $CopyWithPlaceholder(),
    Object? roles = const $CopyWithPlaceholder(),
    Object? model = const $CopyWithPlaceholder(),
    Object? effort = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? promptTemplates = const $CopyWithPlaceholder(),
    Object? citations = const $CopyWithPlaceholder(),
    Object? lintWarnings = const $CopyWithPlaceholder(),
    Object? footer = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? coverage = const $CopyWithPlaceholder(),
    Object? social = const $CopyWithPlaceholder(),
  }) {
    return Assessment(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      cycleId: cycleId == const $CopyWithPlaceholder()
          ? _value.cycleId
          // ignore: cast_nullable_to_non_nullable
          : cycleId as String?,
      situationId: situationId == const $CopyWithPlaceholder()
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String?,
      cadence: cadence == const $CopyWithPlaceholder() || cadence == null
          ? _value.cadence
          // ignore: cast_nullable_to_non_nullable
          : cadence as AssessmentCadenceEnum,
      periodStart:
          periodStart == const $CopyWithPlaceholder() || periodStart == null
          ? _value.periodStart
          // ignore: cast_nullable_to_non_nullable
          : periodStart as DateTime,
      periodEnd: periodEnd == const $CopyWithPlaceholder() || periodEnd == null
          ? _value.periodEnd
          // ignore: cast_nullable_to_non_nullable
          : periodEnd as DateTime,
      previousAssessmentId: previousAssessmentId == const $CopyWithPlaceholder()
          ? _value.previousAssessmentId
          // ignore: cast_nullable_to_non_nullable
          : previousAssessmentId as String?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as AssessmentStatusEnum,
      bluf: bluf == const $CopyWithPlaceholder() || bluf == null
          ? _value.bluf
          // ignore: cast_nullable_to_non_nullable
          : bluf as List<KeyJudgment>,
      headline: headline == const $CopyWithPlaceholder()
          ? _value.headline
          // ignore: cast_nullable_to_non_nullable
          : headline as String?,
      sections: sections == const $CopyWithPlaceholder() || sections == null
          ? _value.sections
          // ignore: cast_nullable_to_non_nullable
          : sections as List<AssessmentSectionsInner>,
      markdown: markdown == const $CopyWithPlaceholder()
          ? _value.markdown
          // ignore: cast_nullable_to_non_nullable
          : markdown as String?,
      html: html == const $CopyWithPlaceholder()
          ? _value.html
          // ignore: cast_nullable_to_non_nullable
          : html as String?,
      packetRef: packetRef == const $CopyWithPlaceholder()
          ? _value.packetRef
          // ignore: cast_nullable_to_non_nullable
          : packetRef as String?,
      roles: roles == const $CopyWithPlaceholder()
          ? _value.roles
          // ignore: cast_nullable_to_non_nullable
          : roles as Map<String, CycleRunRolesInner>?,
      model: model == const $CopyWithPlaceholder()
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as String?,
      effort: effort == const $CopyWithPlaceholder()
          ? _value.effort
          // ignore: cast_nullable_to_non_nullable
          : effort as AssessmentEffortEnum?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
      promptTemplates: promptTemplates == const $CopyWithPlaceholder()
          ? _value.promptTemplates
          // ignore: cast_nullable_to_non_nullable
          : promptTemplates as List<AssessmentPromptTemplatesInner>?,
      citations: citations == const $CopyWithPlaceholder()
          ? _value.citations
          // ignore: cast_nullable_to_non_nullable
          : citations as List<String>?,
      lintWarnings: lintWarnings == const $CopyWithPlaceholder()
          ? _value.lintWarnings
          // ignore: cast_nullable_to_non_nullable
          : lintWarnings as List<String>?,
      footer: footer == const $CopyWithPlaceholder()
          ? _value.footer
          // ignore: cast_nullable_to_non_nullable
          : footer as String?,
      private: private == const $CopyWithPlaceholder() || private == null
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      coverage: coverage == const $CopyWithPlaceholder()
          ? _value.coverage
          // ignore: cast_nullable_to_non_nullable
          : coverage as AssessmentCoverage?,
      social: social == const $CopyWithPlaceholder()
          ? _value.social
          // ignore: cast_nullable_to_non_nullable
          : social as SocialVersusObserved?,
    );
  }
}

extension $AssessmentCopyWith on Assessment {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAssessment.copyWith(...)` or `instanceOfAssessment.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AssessmentCWProxy get copyWith => _$AssessmentCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Assessment _$AssessmentFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Assessment', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'cadence',
      'periodStart',
      'periodEnd',
      'status',
      'bluf',
      'sections',
      'private',
      'createdAt',
    ],
  );
  final val = Assessment(
    id: $checkedConvert('id', (v) => v as String),
    ref: $checkedConvert('ref', (v) => v as String?),
    cycleId: $checkedConvert('cycleId', (v) => v as String?),
    situationId: $checkedConvert('situationId', (v) => v as String?),
    cadence: $checkedConvert(
      'cadence',
      (v) => $enumDecode(_$AssessmentCadenceEnumEnumMap, v),
    ),
    periodStart: $checkedConvert(
      'periodStart',
      (v) => DateTime.parse(v as String),
    ),
    periodEnd: $checkedConvert('periodEnd', (v) => DateTime.parse(v as String)),
    previousAssessmentId: $checkedConvert(
      'previousAssessmentId',
      (v) => v as String?,
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$AssessmentStatusEnumEnumMap, v),
    ),
    bluf: $checkedConvert(
      'bluf',
      (v) => (v as List<dynamic>)
          .map((e) => KeyJudgment.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    headline: $checkedConvert('headline', (v) => v as String?),
    sections: $checkedConvert(
      'sections',
      (v) => (v as List<dynamic>)
          .map(
            (e) => AssessmentSectionsInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    markdown: $checkedConvert('markdown', (v) => v as String?),
    html: $checkedConvert('html', (v) => v as String?),
    packetRef: $checkedConvert('packetRef', (v) => v as String?),
    roles: $checkedConvert(
      'roles',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) =>
            MapEntry(k, CycleRunRolesInner.fromJson(e as Map<String, dynamic>)),
      ),
    ),
    model: $checkedConvert('model', (v) => v as String?),
    effort: $checkedConvert(
      'effort',
      (v) => $enumDecodeNullable(_$AssessmentEffortEnumEnumMap, v),
    ),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
    promptTemplates: $checkedConvert(
      'promptTemplates',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => AssessmentPromptTemplatesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    citations: $checkedConvert(
      'citations',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    lintWarnings: $checkedConvert(
      'lintWarnings',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    footer: $checkedConvert(
      'footer',
      (v) =>
          v as String? ??
          'Automated analysis. Probabilities use ICD 203 terms; evidence is cited; speculation is labelled. Verify before acting.',
    ),
    private: $checkedConvert('private', (v) => v as bool),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    coverage: $checkedConvert(
      'coverage',
      (v) => v == null
          ? null
          : AssessmentCoverage.fromJson(v as Map<String, dynamic>),
    ),
    social: $checkedConvert(
      'social',
      (v) => v == null
          ? null
          : SocialVersusObserved.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$AssessmentToJson(
  Assessment instance,
) => <String, dynamic>{
  'id': instance.id,
  'ref': ?instance.ref,
  'cycleId': ?instance.cycleId,
  'situationId': ?instance.situationId,
  'cadence': _$AssessmentCadenceEnumEnumMap[instance.cadence]!,
  'periodStart': instance.periodStart.toIso8601String(),
  'periodEnd': instance.periodEnd.toIso8601String(),
  'previousAssessmentId': ?instance.previousAssessmentId,
  'status': _$AssessmentStatusEnumEnumMap[instance.status]!,
  'bluf': instance.bluf.map((e) => e.toJson()).toList(),
  'headline': ?instance.headline,
  'sections': instance.sections.map((e) => e.toJson()).toList(),
  'markdown': ?instance.markdown,
  'html': ?instance.html,
  'packetRef': ?instance.packetRef,
  'roles': ?instance.roles?.map((k, e) => MapEntry(k, e.toJson())),
  'model': ?instance.model,
  'effort': ?_$AssessmentEffortEnumEnumMap[instance.effort],
  'manifestId': ?instance.manifestId,
  'promptTemplates': ?instance.promptTemplates?.map((e) => e.toJson()).toList(),
  'citations': ?instance.citations,
  'lintWarnings': ?instance.lintWarnings,
  'footer': ?instance.footer,
  'private': instance.private,
  'createdAt': instance.createdAt.toIso8601String(),
  'coverage': ?instance.coverage?.toJson(),
  'social': ?instance.social?.toJson(),
};

const _$AssessmentCadenceEnumEnumMap = {
  AssessmentCadenceEnum.daily: 'daily',
  AssessmentCadenceEnum.weekly: 'weekly',
  AssessmentCadenceEnum.monthly: 'monthly',
  AssessmentCadenceEnum.onDemand: 'on-demand',
};

const _$AssessmentStatusEnumEnumMap = {
  AssessmentStatusEnum.ok: 'ok',
  AssessmentStatusEnum.partial: 'partial',
  AssessmentStatusEnum.digestOnly: 'digest-only',
  AssessmentStatusEnum.failed: 'failed',
};

const _$AssessmentEffortEnumEnumMap = {
  AssessmentEffortEnum.low: 'low',
  AssessmentEffortEnum.medium: 'medium',
  AssessmentEffortEnum.high: 'high',
};
