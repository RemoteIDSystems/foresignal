//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/situation_join_reason.dart';
import 'package:foresignal_api/src/model/situation_link_decision_candidates_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_link_decision.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationLinkDecision {
  /// Returns a new [SituationLinkDecision] instance.
  SituationLinkDecision({

    required  this.seq,

    required  this.inputRef,

     this.memberType,

     this.signalFamily,

    required  this.action,

     this.situationId,

     this.seedTitle,

     this.score,

     this.joinReason,

     this.candidates,
  });

  @JsonKey(
    
    name: r'seq',
    required: true,
    includeIfNull: false,
  )


  final int seq;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'inputRef',
    required: true,
    includeIfNull: false,
  )


  final String inputRef;



  @JsonKey(
    
    name: r'memberType',
    required: false,
    includeIfNull: false,
  )


  final SituationLinkDecisionMemberTypeEnum? memberType;



  @JsonKey(
    
    name: r'signalFamily',
    required: false,
    includeIfNull: false,
  )


  final String? signalFamily;



  @JsonKey(
    
    name: r'action',
    required: true,
    includeIfNull: false,
  )


  final SituationLinkDecisionActionEnum action;



      /// Joined situation, or the would-be id of a seeded one
  @JsonKey(
    
    name: r'situationId',
    required: false,
    includeIfNull: false,
  )


  final String? situationId;



      /// Deterministic title of a seeded situation
  @JsonKey(
    
    name: r'seedTitle',
    required: false,
    includeIfNull: false,
  )


  final String? seedTitle;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'score',
    required: false,
    includeIfNull: false,
  )


  final num? score;



  @JsonKey(
    
    name: r'joinReason',
    required: false,
    includeIfNull: false,
  )


  final SituationJoinReason? joinReason;



  @JsonKey(
    
    name: r'candidates',
    required: false,
    includeIfNull: false,
  )


  final List<SituationLinkDecisionCandidatesInner>? candidates;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationLinkDecision &&
      other.seq == seq &&
      other.inputRef == inputRef &&
      other.memberType == memberType &&
      other.signalFamily == signalFamily &&
      other.action == action &&
      other.situationId == situationId &&
      other.seedTitle == seedTitle &&
      other.score == score &&
      other.joinReason == joinReason &&
      other.candidates == candidates;

    @override
    int get hashCode =>
        seq.hashCode +
        inputRef.hashCode +
        memberType.hashCode +
        (signalFamily == null ? 0 : signalFamily.hashCode) +
        action.hashCode +
        (situationId == null ? 0 : situationId.hashCode) +
        (seedTitle == null ? 0 : seedTitle.hashCode) +
        (score == null ? 0 : score.hashCode) +
        (joinReason == null ? 0 : joinReason.hashCode) +
        candidates.hashCode;

  factory SituationLinkDecision.fromJson(Map<String, dynamic> json) => _$SituationLinkDecisionFromJson(json);

  Map<String, dynamic> toJson() => _$SituationLinkDecisionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SituationLinkDecisionMemberTypeEnum {
@JsonValue(r'intel_event')
intelEvent(r'intel_event'),
@JsonValue(r'finding')
finding(r'finding'),
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'anomaly')
anomaly(r'anomaly'),
@JsonValue(r'record')
record(r'record'),
@JsonValue(r'investigation')
investigation(r'investigation');

const SituationLinkDecisionMemberTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SituationLinkDecisionActionEnum {
@JsonValue(r'join')
join(r'join'),
@JsonValue(r'seed')
seed(r'seed'),
@JsonValue(r'skip')
skip(r'skip'),
@JsonValue(r'already-member')
alreadyMember(r'already-member');

const SituationLinkDecisionActionEnum(this.value);

final String value;

@override
String toString() => value;
}


