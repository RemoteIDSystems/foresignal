//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/indicator.dart';
import 'package:foresignal_api/src/model/hypothesis.dart';
import 'package:foresignal_api/src/model/key_assumption.dart';
import 'package:foresignal_api/src/model/confidence_level.dart';
import 'package:foresignal_api/src/model/ledger_question_brief.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_set.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisSet {
  /// Returns a new [HypothesisSet] instance.
  HypothesisSet({

    required  this.id,

    required  this.question,

     this.description,

    required  this.kind,

    required  this.cadence,

     this.horizon,

    required  this.status,

     this.situationId,

     this.questionId,

     this.regionCodes,

     this.themes,

    required  this.origin,

    required  this.confidence,

     this.lastReviewedAt,

     this.nextReviewAt,

     this.lastRevisionId,

     this.lastRevisionAt,

    required  this.private,

    required  this.version,

    required  this.hypotheses,

     this.indicators,

     this.assumptions,

     this.linkedQuestion,

     this.recordRef,

    required  this.createdAt,

    required  this.updatedAt,

     this.reviewFlagged,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'question',
    required: true,
    includeIfNull: false,
  )


  final String question;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



      /// `exclusive`: probabilities sum to 1 with exactly one `other` hypothesis
  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final HypothesisSetKindEnum kind;



  @JsonKey(
    
    name: r'cadence',
    required: true,
    includeIfNull: false,
  )


  final HypothesisSetCadenceEnum cadence;



  @JsonKey(
    
    name: r'horizon',
    required: false,
    includeIfNull: false,
  )


  final DateTime? horizon;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final HypothesisSetStatusEnum status;



      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: false,
    includeIfNull: false,
  )


  final String? situationId;



      /// ULID identifier
  @JsonKey(
    
    name: r'questionId',
    required: false,
    includeIfNull: false,
  )


  final String? questionId;



      /// ISO 3166-1 alpha-2 country or ISO 3166-2 subdivision codes
  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



  @JsonKey(
    
    name: r'themes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? themes;



  @JsonKey(
    
    name: r'origin',
    required: true,
    includeIfNull: false,
  )


  final HypothesisSetOriginEnum origin;



  @JsonKey(
    
    name: r'confidence',
    required: true,
    includeIfNull: false,
  )


  final ConfidenceLevel confidence;



  @JsonKey(
    
    name: r'lastReviewedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastReviewedAt;



  @JsonKey(
    
    name: r'nextReviewAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? nextReviewAt;



      /// ULID identifier
  @JsonKey(
    
    name: r'lastRevisionId',
    required: false,
    includeIfNull: false,
  )


  final String? lastRevisionId;



  @JsonKey(
    
    name: r'lastRevisionAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastRevisionAt;



  @JsonKey(
    
    name: r'private',
    required: true,
    includeIfNull: false,
  )


  final bool private;



      /// Optimistic lock for PATCH
  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



  @JsonKey(
    
    name: r'hypotheses',
    required: true,
    includeIfNull: false,
  )


  final List<Hypothesis> hypotheses;



  @JsonKey(
    
    name: r'indicators',
    required: false,
    includeIfNull: false,
  )


  final List<Indicator>? indicators;



  @JsonKey(
    
    name: r'assumptions',
    required: false,
    includeIfNull: false,
  )


  final List<KeyAssumption>? assumptions;



  @JsonKey(
    
    name: r'linkedQuestion',
    required: false,
    includeIfNull: false,
  )


  final LedgerQuestionBrief? linkedQuestion;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'recordRef',
    required: false,
    includeIfNull: false,
  )


  final String? recordRef;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime updatedAt;



      /// A broken linchpin assumption flagged the set for review; a review clears it
  @JsonKey(
    
    name: r'reviewFlagged',
    required: false,
    includeIfNull: false,
  )


  final bool? reviewFlagged;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisSet &&
      other.id == id &&
      other.question == question &&
      other.description == description &&
      other.kind == kind &&
      other.cadence == cadence &&
      other.horizon == horizon &&
      other.status == status &&
      other.situationId == situationId &&
      other.questionId == questionId &&
      other.regionCodes == regionCodes &&
      other.themes == themes &&
      other.origin == origin &&
      other.confidence == confidence &&
      other.lastReviewedAt == lastReviewedAt &&
      other.nextReviewAt == nextReviewAt &&
      other.lastRevisionId == lastRevisionId &&
      other.lastRevisionAt == lastRevisionAt &&
      other.private == private &&
      other.version == version &&
      other.hypotheses == hypotheses &&
      other.indicators == indicators &&
      other.assumptions == assumptions &&
      other.linkedQuestion == linkedQuestion &&
      other.recordRef == recordRef &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt &&
      other.reviewFlagged == reviewFlagged;

    @override
    int get hashCode =>
        id.hashCode +
        question.hashCode +
        (description == null ? 0 : description.hashCode) +
        kind.hashCode +
        cadence.hashCode +
        (horizon == null ? 0 : horizon.hashCode) +
        status.hashCode +
        (situationId == null ? 0 : situationId.hashCode) +
        (questionId == null ? 0 : questionId.hashCode) +
        regionCodes.hashCode +
        themes.hashCode +
        origin.hashCode +
        confidence.hashCode +
        (lastReviewedAt == null ? 0 : lastReviewedAt.hashCode) +
        (nextReviewAt == null ? 0 : nextReviewAt.hashCode) +
        (lastRevisionId == null ? 0 : lastRevisionId.hashCode) +
        (lastRevisionAt == null ? 0 : lastRevisionAt.hashCode) +
        private.hashCode +
        version.hashCode +
        hypotheses.hashCode +
        indicators.hashCode +
        assumptions.hashCode +
        (linkedQuestion == null ? 0 : linkedQuestion.hashCode) +
        (recordRef == null ? 0 : recordRef.hashCode) +
        createdAt.hashCode +
        updatedAt.hashCode +
        reviewFlagged.hashCode;

  factory HypothesisSet.fromJson(Map<String, dynamic> json) => _$HypothesisSetFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisSetToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `exclusive`: probabilities sum to 1 with exactly one `other` hypothesis
enum HypothesisSetKindEnum {
@JsonValue(r'exclusive')
exclusive(r'exclusive'),
@JsonValue(r'independent')
independent(r'independent');

const HypothesisSetKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum HypothesisSetCadenceEnum {
@JsonValue(r'daily')
daily(r'daily'),
@JsonValue(r'weekly')
weekly(r'weekly'),
@JsonValue(r'monthly')
monthly(r'monthly');

const HypothesisSetCadenceEnum(this.value);

final String value;

@override
String toString() => value;
}


enum HypothesisSetStatusEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'resolved')
resolved(r'resolved'),
@JsonValue(r'archived')
archived(r'archived');

const HypothesisSetStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum HypothesisSetOriginEnum {
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'analyst')
analyst(r'analyst');

const HypothesisSetOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


