//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/situation_join_reason.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_member.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationMember {
  /// Returns a new [SituationMember] instance.
  SituationMember({

    required  this.id,

    required  this.situationId,

    required  this.memberType,

    required  this.ref,

    required  this.objectRef,

     this.seq,

     this.version,

     this.kind,

     this.signalFamily,

     this.title,

     this.severity,

     this.observedAt,

    required  this.role,

     this.joinReason,

    required  this.corroboration,

    required  this.addedAt,

    required  this.addedBy,
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
    
    name: r'situationId',
    required: true,
    includeIfNull: false,
  )


  final String situationId;



      /// Member type
  @JsonKey(
    
    name: r'memberType',
    required: true,
    includeIfNull: false,
  )


  final SituationMemberMemberTypeEnum memberType;



      /// Member id, or `sourceId/externalId` for records
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'objectRef',
    required: true,
    includeIfNull: false,
  )


  final String objectRef;



      /// Pinned record version seq (records)
  @JsonKey(
    
    name: r'seq',
    required: false,
    includeIfNull: false,
  )


  final int? seq;



      /// Pinned record version number (records)
  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final int? version;



      /// Record kind or member kind
  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final String? kind;



      /// Signal family from the 119 registry (`intel/signal-families.json`, operator-extensible), e.g. `ais-behaviour`, `aviation-military`, `aviation-ops`, `conflict`, `outage`, `internet-measurement`, `gnss-interference`, `nav-warning`, `attention`, `seismic`, `fire`, `weather-severe`, `cyber`, `markets`, `sanctions`, `health`, `space`
  @JsonKey(
    
    name: r'signalFamily',
    required: false,
    includeIfNull: false,
  )


  final String? signalFamily;



      /// Display title of the referenced object
  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



      /// Severity of the referenced object as it reported it
  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final String? severity;



      /// When the member was observed
  @JsonKey(
    
    name: r'observedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? observedAt;



      /// `core` members drive status and severity; `context` members do not
  @JsonKey(
    
    name: r'role',
    required: true,
    includeIfNull: false,
  )


  final SituationMemberRoleEnum role;



  @JsonKey(
    
    name: r'joinReason',
    required: false,
    includeIfNull: false,
  )


  final SituationJoinReason? joinReason;



      /// 116 evidence-group roll-up for this member
  @JsonKey(
    
    name: r'corroboration',
    required: true,
    includeIfNull: false,
  )


  final SituationMemberCorroborationEnum corroboration;



  @JsonKey(
    
    name: r'addedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime addedAt;



      /// Actor: token id, `linker:<version>` or `analyst:<profile>`
  @JsonKey(
    
    name: r'addedBy',
    required: true,
    includeIfNull: false,
  )


  final String addedBy;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationMember &&
      other.id == id &&
      other.situationId == situationId &&
      other.memberType == memberType &&
      other.ref == ref &&
      other.objectRef == objectRef &&
      other.seq == seq &&
      other.version == version &&
      other.kind == kind &&
      other.signalFamily == signalFamily &&
      other.title == title &&
      other.severity == severity &&
      other.observedAt == observedAt &&
      other.role == role &&
      other.joinReason == joinReason &&
      other.corroboration == corroboration &&
      other.addedAt == addedAt &&
      other.addedBy == addedBy;

    @override
    int get hashCode =>
        id.hashCode +
        situationId.hashCode +
        memberType.hashCode +
        ref.hashCode +
        objectRef.hashCode +
        (seq == null ? 0 : seq.hashCode) +
        (version == null ? 0 : version.hashCode) +
        (kind == null ? 0 : kind.hashCode) +
        (signalFamily == null ? 0 : signalFamily.hashCode) +
        (title == null ? 0 : title.hashCode) +
        (severity == null ? 0 : severity.hashCode) +
        (observedAt == null ? 0 : observedAt.hashCode) +
        role.hashCode +
        (joinReason == null ? 0 : joinReason.hashCode) +
        corroboration.hashCode +
        addedAt.hashCode +
        addedBy.hashCode;

  factory SituationMember.fromJson(Map<String, dynamic> json) => _$SituationMemberFromJson(json);

  Map<String, dynamic> toJson() => _$SituationMemberToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Member type
enum SituationMemberMemberTypeEnum {
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

const SituationMemberMemberTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `core` members drive status and severity; `context` members do not
enum SituationMemberRoleEnum {
@JsonValue(r'core')
core(r'core'),
@JsonValue(r'context')
context(r'context');

const SituationMemberRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


/// 116 evidence-group roll-up for this member
enum SituationMemberCorroborationEnum {
@JsonValue(r'corroborated')
corroborated(r'corroborated'),
@JsonValue(r'uncorroborated')
uncorroborated(r'uncorroborated'),
@JsonValue(r'unknown')
unknown(r'unknown'),
@JsonValue(r'n/a')
nSlashA(r'n/a');

const SituationMemberCorroborationEnum(this.value);

final String value;

@override
String toString() => value;
}


