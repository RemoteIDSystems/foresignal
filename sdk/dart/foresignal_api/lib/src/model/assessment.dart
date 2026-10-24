//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/cycle_run_roles_inner.dart';
import 'package:foresignal_api/src/model/assessment_coverage.dart';
import 'package:foresignal_api/src/model/assessment_sections_inner.dart';
import 'package:foresignal_api/src/model/key_judgment.dart';
import 'package:foresignal_api/src/model/assessment_prompt_templates_inner.dart';
import 'package:foresignal_api/src/model/social_versus_observed.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'assessment.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Assessment {
  /// Returns a new [Assessment] instance.
  Assessment({

    required  this.id,

     this.ref,

     this.cycleId,

     this.situationId,

    required  this.cadence,

    required  this.periodStart,

    required  this.periodEnd,

     this.previousAssessmentId,

    required  this.status,

    required  this.bluf,

     this.headline,

    required  this.sections,

     this.markdown,

     this.html,

     this.packetRef,

     this.roles,

     this.model,

     this.effort,

     this.manifestId,

     this.promptTemplates,

     this.citations,

     this.lintWarnings,

     this.footer = 'Automated analysis. Probabilities use ICD 203 terms; evidence is cited; speculation is labeled. Verify before acting.',

    required  this.private,

    required  this.createdAt,

     this.coverage,

     this.social,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



      /// ULID identifier
  @JsonKey(
    
    name: r'cycleId',
    required: false,
    includeIfNull: false,
  )


  final String? cycleId;



      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: false,
    includeIfNull: false,
  )


  final String? situationId;



  @JsonKey(
    
    name: r'cadence',
    required: true,
    includeIfNull: false,
  )


  final AssessmentCadenceEnum cadence;



  @JsonKey(
    
    name: r'periodStart',
    required: true,
    includeIfNull: false,
  )


  final DateTime periodStart;



  @JsonKey(
    
    name: r'periodEnd',
    required: true,
    includeIfNull: false,
  )


  final DateTime periodEnd;



      /// ULID identifier
  @JsonKey(
    
    name: r'previousAssessmentId',
    required: false,
    includeIfNull: false,
  )


  final String? previousAssessmentId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final AssessmentStatusEnum status;



  @JsonKey(
    
    name: r'bluf',
    required: true,
    includeIfNull: false,
  )


  final List<KeyJudgment> bluf;



      /// First BLUF judgment or top deterministic change
  @JsonKey(
    
    name: r'headline',
    required: false,
    includeIfNull: false,
  )


  final String? headline;



  @JsonKey(
    
    name: r'sections',
    required: true,
    includeIfNull: false,
  )


  final List<AssessmentSectionsInner> sections;



      /// Full rendering (omitted in lists)
  @JsonKey(
    
    name: r'markdown',
    required: false,
    includeIfNull: false,
  )


  final String? markdown;



      /// Full rendering (omitted in lists)
  @JsonKey(
    
    name: r'html',
    required: false,
    includeIfNull: false,
  )


  final String? html;



      /// ULID identifier
  @JsonKey(
    
    name: r'packetRef',
    required: false,
    includeIfNull: false,
  )


  final String? packetRef;



      /// Per role
  @JsonKey(
    
    name: r'roles',
    required: false,
    includeIfNull: false,
  )


  final Map<String, CycleRunRolesInner>? roles;



  @JsonKey(
    
    name: r'model',
    required: false,
    includeIfNull: false,
  )


  final String? model;



      /// LLM effort
  @JsonKey(
    
    name: r'effort',
    required: false,
    includeIfNull: false,
  )


  final AssessmentEffortEnum? effort;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;



  @JsonKey(
    
    name: r'promptTemplates',
    required: false,
    includeIfNull: false,
  )


  final List<AssessmentPromptTemplatesInner>? promptTemplates;



  @JsonKey(
    
    name: r'citations',
    required: false,
    includeIfNull: false,
  )


  final List<String>? citations;



  @JsonKey(
    
    name: r'lintWarnings',
    required: false,
    includeIfNull: false,
  )


  final List<String>? lintWarnings;



  @JsonKey(
    defaultValue: 'Automated analysis. Probabilities use ICD 203 terms; evidence is cited; speculation is labeled. Verify before acting.',
    name: r'footer',
    required: false,
    includeIfNull: false,
  )


  final String? footer;



  @JsonKey(
    
    name: r'private',
    required: true,
    includeIfNull: false,
  )


  final bool private;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'coverage',
    required: false,
    includeIfNull: false,
  )


  final AssessmentCoverage? coverage;



      /// \"What people are saying versus what we observe\" (137)
  @JsonKey(
    
    name: r'social',
    required: false,
    includeIfNull: false,
  )


  final SocialVersusObserved? social;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Assessment &&
      other.id == id &&
      other.ref == ref &&
      other.cycleId == cycleId &&
      other.situationId == situationId &&
      other.cadence == cadence &&
      other.periodStart == periodStart &&
      other.periodEnd == periodEnd &&
      other.previousAssessmentId == previousAssessmentId &&
      other.status == status &&
      other.bluf == bluf &&
      other.headline == headline &&
      other.sections == sections &&
      other.markdown == markdown &&
      other.html == html &&
      other.packetRef == packetRef &&
      other.roles == roles &&
      other.model == model &&
      other.effort == effort &&
      other.manifestId == manifestId &&
      other.promptTemplates == promptTemplates &&
      other.citations == citations &&
      other.lintWarnings == lintWarnings &&
      other.footer == footer &&
      other.private == private &&
      other.createdAt == createdAt &&
      other.coverage == coverage &&
      other.social == social;

    @override
    int get hashCode =>
        id.hashCode +
        ref.hashCode +
        (cycleId == null ? 0 : cycleId.hashCode) +
        (situationId == null ? 0 : situationId.hashCode) +
        cadence.hashCode +
        periodStart.hashCode +
        periodEnd.hashCode +
        (previousAssessmentId == null ? 0 : previousAssessmentId.hashCode) +
        status.hashCode +
        bluf.hashCode +
        (headline == null ? 0 : headline.hashCode) +
        sections.hashCode +
        (markdown == null ? 0 : markdown.hashCode) +
        (html == null ? 0 : html.hashCode) +
        (packetRef == null ? 0 : packetRef.hashCode) +
        roles.hashCode +
        (model == null ? 0 : model.hashCode) +
        (effort == null ? 0 : effort.hashCode) +
        (manifestId == null ? 0 : manifestId.hashCode) +
        promptTemplates.hashCode +
        citations.hashCode +
        lintWarnings.hashCode +
        footer.hashCode +
        private.hashCode +
        createdAt.hashCode +
        (coverage == null ? 0 : coverage.hashCode) +
        (social == null ? 0 : social.hashCode);

  factory Assessment.fromJson(Map<String, dynamic> json) => _$AssessmentFromJson(json);

  Map<String, dynamic> toJson() => _$AssessmentToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AssessmentCadenceEnum {
@JsonValue(r'daily')
daily(r'daily'),
@JsonValue(r'weekly')
weekly(r'weekly'),
@JsonValue(r'monthly')
monthly(r'monthly'),
@JsonValue(r'on-demand')
onDemand(r'on-demand');

const AssessmentCadenceEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AssessmentStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'partial')
partial(r'partial'),
@JsonValue(r'digest-only')
digestOnly(r'digest-only'),
@JsonValue(r'failed')
failed(r'failed');

const AssessmentStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


/// LLM effort
enum AssessmentEffortEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'medium')
medium(r'medium'),
@JsonValue(r'high')
high(r'high');

const AssessmentEffortEnum(this.value);

final String value;

@override
String toString() => value;
}


