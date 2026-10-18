//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_member_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationMemberInput {
  /// Returns a new [SituationMemberInput] instance.
  SituationMemberInput({

    required  this.memberType,

    required  this.ref,

     this.seq,

     this.role,

     this.moveFromSituationId,
  });

  @JsonKey(
    
    name: r'memberType',
    required: true,
    includeIfNull: false,
  )


  final SituationMemberInputMemberTypeEnum memberType;



      /// Member id, or `sourceId/externalId` for records
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



      /// Record version seq to pin (required for records)
  @JsonKey(
    
    name: r'seq',
    required: false,
    includeIfNull: false,
  )


  final int? seq;



  @JsonKey(
    
    name: r'role',
    required: false,
    includeIfNull: false,
  )


  final SituationMemberInputRoleEnum? role;



      /// Move the member from this situation (logged on both)
  @JsonKey(
    
    name: r'moveFromSituationId',
    required: false,
    includeIfNull: false,
  )


  final String? moveFromSituationId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationMemberInput &&
      other.memberType == memberType &&
      other.ref == ref &&
      other.seq == seq &&
      other.role == role &&
      other.moveFromSituationId == moveFromSituationId;

    @override
    int get hashCode =>
        memberType.hashCode +
        ref.hashCode +
        (seq == null ? 0 : seq.hashCode) +
        role.hashCode +
        (moveFromSituationId == null ? 0 : moveFromSituationId.hashCode);

  factory SituationMemberInput.fromJson(Map<String, dynamic> json) => _$SituationMemberInputFromJson(json);

  Map<String, dynamic> toJson() => _$SituationMemberInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SituationMemberInputMemberTypeEnum {
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

const SituationMemberInputMemberTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SituationMemberInputRoleEnum {
@JsonValue(r'core')
core(r'core'),
@JsonValue(r'context')
context(r'context');

const SituationMemberInputRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


