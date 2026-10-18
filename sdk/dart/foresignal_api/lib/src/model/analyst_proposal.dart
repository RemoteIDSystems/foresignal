//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_input.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_proposal.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystProposal {
  /// Returns a new [AnalystProposal] instance.
  AnalystProposal({

    required  this.id,

    required  this.runId,

    required  this.profileId,

    required  this.type,

    required  this.payload,

     this.rationale,

     this.citations,

    required  this.status,

     this.autoExecuted,

     this.executedRef,

     this.decidedBy,

     this.decidedAt,

     this.reason,

    required  this.createdAt,

    required  this.expiresAt,

     this.chronicleDraft,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// ULID identifier
  @JsonKey(
    
    name: r'runId',
    required: true,
    includeIfNull: false,
  )


  final String runId;



      /// ULID identifier
  @JsonKey(
    
    name: r'profileId',
    required: true,
    includeIfNull: false,
  )


  final String profileId;



      /// Extensible enum (128 adds types)
  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final AnalystProposalTypeEnum type;



      /// Action payload (rule input, event input, investigation item, annotation)
  @JsonKey(
    
    name: r'payload',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> payload;



  @JsonKey(
    
    name: r'rationale',
    required: false,
    includeIfNull: false,
  )


  final String? rationale;



  @JsonKey(
    
    name: r'citations',
    required: false,
    includeIfNull: false,
  )


  final List<String>? citations;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final AnalystProposalStatusEnum status;



  @JsonKey(
    
    name: r'autoExecuted',
    required: false,
    includeIfNull: false,
  )


  final bool? autoExecuted;



      /// Reference of the created object
  @JsonKey(
    
    name: r'executedRef',
    required: false,
    includeIfNull: false,
  )


  final String? executedRef;



  @JsonKey(
    
    name: r'decidedBy',
    required: false,
    includeIfNull: false,
  )


  final String? decidedBy;



  @JsonKey(
    
    name: r'decidedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? decidedAt;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'expiresAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime expiresAt;



      /// For `propose_chronicle`
  @JsonKey(
    
    name: r'chronicleDraft',
    required: false,
    includeIfNull: false,
  )


  final ChronicleInput? chronicleDraft;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystProposal &&
      other.id == id &&
      other.runId == runId &&
      other.profileId == profileId &&
      other.type == type &&
      other.payload == payload &&
      other.rationale == rationale &&
      other.citations == citations &&
      other.status == status &&
      other.autoExecuted == autoExecuted &&
      other.executedRef == executedRef &&
      other.decidedBy == decidedBy &&
      other.decidedAt == decidedAt &&
      other.reason == reason &&
      other.createdAt == createdAt &&
      other.expiresAt == expiresAt &&
      other.chronicleDraft == chronicleDraft;

    @override
    int get hashCode =>
        id.hashCode +
        runId.hashCode +
        profileId.hashCode +
        type.hashCode +
        payload.hashCode +
        rationale.hashCode +
        citations.hashCode +
        status.hashCode +
        autoExecuted.hashCode +
        (executedRef == null ? 0 : executedRef.hashCode) +
        (decidedBy == null ? 0 : decidedBy.hashCode) +
        (decidedAt == null ? 0 : decidedAt.hashCode) +
        (reason == null ? 0 : reason.hashCode) +
        createdAt.hashCode +
        expiresAt.hashCode +
        (chronicleDraft == null ? 0 : chronicleDraft.hashCode);

  factory AnalystProposal.fromJson(Map<String, dynamic> json) => _$AnalystProposalFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystProposalToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Extensible enum (128 adds types)
enum AnalystProposalTypeEnum {
@JsonValue(r'propose_watch')
proposeWatch(r'propose_watch'),
@JsonValue(r'propose_intel_event')
proposeIntelEvent(r'propose_intel_event'),
@JsonValue(r'propose_investigation_item')
proposeInvestigationItem(r'propose_investigation_item'),
@JsonValue(r'annotate_event')
annotateEvent(r'annotate_event'),
@JsonValue(r'propose_chronicle')
proposeChronicle(r'propose_chronicle'),
@JsonValue(r'propose_hypothesis_set')
proposeHypothesisSet(r'propose_hypothesis_set'),
@JsonValue(r'propose_indicator')
proposeIndicator(r'propose_indicator'),
@JsonValue(r'propose_question')
proposeQuestion(r'propose_question'),
@JsonValue(r'propose_situation_merge')
proposeSituationMerge(r'propose_situation_merge');

const AnalystProposalTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnalystProposalStatusEnum {
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'executed')
executed(r'executed'),
@JsonValue(r'rejected')
rejected(r'rejected'),
@JsonValue(r'expired')
expired(r'expired'),
@JsonValue(r'failed')
failed(r'failed');

const AnalystProposalStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


