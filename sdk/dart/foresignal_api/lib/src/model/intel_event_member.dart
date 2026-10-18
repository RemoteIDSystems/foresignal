//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/intel_event_member_join_reason.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_event_member.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelEventMember {
  /// Returns a new [IntelEventMember] instance.
  IntelEventMember({

    required  this.id,

    required  this.role,

    required  this.memberType,

     this.sourceId,

     this.externalId,

     this.seq,

     this.version,

     this.alertId,

     this.anomalyId,

     this.eventId,

     this.kind,

     this.observedAt,

     this.joinReason,

     this.signalFamily,

     this.weight,

     this.snapshot,

    required  this.addedAt,

    required  this.addedBy,
  });

      /// Member id
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'role',
    required: true,
    includeIfNull: false,
  )


  final IntelEventMemberRoleEnum role;



  @JsonKey(
    
    name: r'memberType',
    required: true,
    includeIfNull: false,
  )


  final IntelEventMemberMemberTypeEnum memberType;



  @JsonKey(
    
    name: r'sourceId',
    required: false,
    includeIfNull: false,
  )


  final String? sourceId;



  @JsonKey(
    
    name: r'externalId',
    required: false,
    includeIfNull: false,
  )


  final String? externalId;



      /// Pinned version seq
  @JsonKey(
    
    name: r'seq',
    required: false,
    includeIfNull: false,
  )


  final int? seq;



  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final int? version;



  @JsonKey(
    
    name: r'alertId',
    required: false,
    includeIfNull: false,
  )


  final int? alertId;



  @JsonKey(
    
    name: r'anomalyId',
    required: false,
    includeIfNull: false,
  )


  final int? anomalyId;



      /// ULID identifier
  @JsonKey(
    
    name: r'eventId',
    required: false,
    includeIfNull: false,
  )


  final String? eventId;



  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final String? kind;



  @JsonKey(
    
    name: r'observedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? observedAt;



  @JsonKey(
    
    name: r'joinReason',
    required: false,
    includeIfNull: false,
  )


  final IntelEventMemberJoinReason? joinReason;



      /// Signal family from the shared registry (`intel/signal-families.json`, 119), e.g. `ais-behaviour`, `nav-warning`, `internet-measurement`
  @JsonKey(
    
    name: r'signalFamily',
    required: false,
    includeIfNull: false,
  )


  final String? signalFamily;



  @JsonKey(
    
    name: r'weight',
    required: false,
    includeIfNull: false,
  )


  final num? weight;



      /// Payload snapshot (≤ 16 KB) so the evidence survives retention
  @JsonKey(
    
    name: r'snapshot',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? snapshot;



  @JsonKey(
    
    name: r'addedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime addedAt;



      /// Token id or `recipe:<id>`
  @JsonKey(
    
    name: r'addedBy',
    required: true,
    includeIfNull: false,
  )


  final String addedBy;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelEventMember &&
      other.id == id &&
      other.role == role &&
      other.memberType == memberType &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.seq == seq &&
      other.version == version &&
      other.alertId == alertId &&
      other.anomalyId == anomalyId &&
      other.eventId == eventId &&
      other.kind == kind &&
      other.observedAt == observedAt &&
      other.joinReason == joinReason &&
      other.signalFamily == signalFamily &&
      other.weight == weight &&
      other.snapshot == snapshot &&
      other.addedAt == addedAt &&
      other.addedBy == addedBy;

    @override
    int get hashCode =>
        id.hashCode +
        role.hashCode +
        memberType.hashCode +
        (sourceId == null ? 0 : sourceId.hashCode) +
        (externalId == null ? 0 : externalId.hashCode) +
        (seq == null ? 0 : seq.hashCode) +
        (version == null ? 0 : version.hashCode) +
        (alertId == null ? 0 : alertId.hashCode) +
        (anomalyId == null ? 0 : anomalyId.hashCode) +
        (eventId == null ? 0 : eventId.hashCode) +
        (kind == null ? 0 : kind.hashCode) +
        (observedAt == null ? 0 : observedAt.hashCode) +
        joinReason.hashCode +
        signalFamily.hashCode +
        (weight == null ? 0 : weight.hashCode) +
        (snapshot == null ? 0 : snapshot.hashCode) +
        addedAt.hashCode +
        addedBy.hashCode;

  factory IntelEventMember.fromJson(Map<String, dynamic> json) => _$IntelEventMemberFromJson(json);

  Map<String, dynamic> toJson() => _$IntelEventMemberToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IntelEventMemberRoleEnum {
@JsonValue(r'supporting')
supporting(r'supporting'),
@JsonValue(r'contradicting')
contradicting(r'contradicting'),
@JsonValue(r'context')
context(r'context');

const IntelEventMemberRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IntelEventMemberMemberTypeEnum {
@JsonValue(r'record')
record(r'record'),
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'anomaly')
anomaly(r'anomaly'),
@JsonValue(r'event')
event(r'event');

const IntelEventMemberMemberTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


