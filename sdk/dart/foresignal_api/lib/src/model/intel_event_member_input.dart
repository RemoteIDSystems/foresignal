//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_event_member_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelEventMemberInput {
  /// Returns a new [IntelEventMemberInput] instance.
  IntelEventMemberInput({

    required  this.memberType,

     this.role,

     this.sourceId,

     this.externalId,

     this.seq,

     this.alertId,

     this.anomalyId,

     this.eventId,

     this.signalFamily,

     this.weight,
  });

  @JsonKey(
    
    name: r'memberType',
    required: true,
    includeIfNull: false,
  )


  final IntelEventMemberInputMemberTypeEnum memberType;



  @JsonKey(
    
    name: r'role',
    required: false,
    includeIfNull: false,
  )


  final IntelEventMemberInputRoleEnum? role;



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



      /// Required for records
  @JsonKey(
    
    name: r'seq',
    required: false,
    includeIfNull: false,
  )


  final int? seq;



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



      /// Ignored: the server derives a member's signal family from the recipe registry (invariant INT-3). Accepted for compatibility only.
  @JsonKey(
    
    name: r'signalFamily',
    required: false,
    includeIfNull: false,
  )


  final String? signalFamily;



      /// Ignored: the server derives a member's weight from the recipe registry (invariant INT-3). Accepted for compatibility only.
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'weight',
    required: false,
    includeIfNull: false,
  )


  final num? weight;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelEventMemberInput &&
      other.memberType == memberType &&
      other.role == role &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.seq == seq &&
      other.alertId == alertId &&
      other.anomalyId == anomalyId &&
      other.eventId == eventId &&
      other.signalFamily == signalFamily &&
      other.weight == weight;

    @override
    int get hashCode =>
        memberType.hashCode +
        role.hashCode +
        sourceId.hashCode +
        externalId.hashCode +
        seq.hashCode +
        alertId.hashCode +
        anomalyId.hashCode +
        eventId.hashCode +
        signalFamily.hashCode +
        weight.hashCode;

  factory IntelEventMemberInput.fromJson(Map<String, dynamic> json) => _$IntelEventMemberInputFromJson(json);

  Map<String, dynamic> toJson() => _$IntelEventMemberInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IntelEventMemberInputMemberTypeEnum {
@JsonValue(r'record')
record(r'record'),
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'anomaly')
anomaly(r'anomaly'),
@JsonValue(r'event')
event(r'event');

const IntelEventMemberInputMemberTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IntelEventMemberInputRoleEnum {
@JsonValue(r'supporting')
supporting(r'supporting'),
@JsonValue(r'contradicting')
contradicting(r'contradicting'),
@JsonValue(r'context')
context(r'context');

const IntelEventMemberInputRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


