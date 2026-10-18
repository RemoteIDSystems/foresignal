//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/intel_event_member.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intelligence_event_evidence.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelligenceEventEvidence {
  /// Returns a new [IntelligenceEventEvidence] instance.
  IntelligenceEventEvidence({

    required  this.memberCount,

     this.signalFamilies,

    required  this.members,
  });

  @JsonKey(
    
    name: r'memberCount',
    required: true,
    includeIfNull: false,
  )


  final int memberCount;



  @JsonKey(
    
    name: r'signalFamilies',
    required: false,
    includeIfNull: false,
  )


  final List<String>? signalFamilies;



  @JsonKey(
    
    name: r'members',
    required: true,
    includeIfNull: false,
  )


  final List<IntelEventMember> members;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelligenceEventEvidence &&
      other.memberCount == memberCount &&
      other.signalFamilies == signalFamilies &&
      other.members == members;

    @override
    int get hashCode =>
        memberCount.hashCode +
        signalFamilies.hashCode +
        members.hashCode;

  factory IntelligenceEventEvidence.fromJson(Map<String, dynamic> json) => _$IntelligenceEventEvidenceFromJson(json);

  Map<String, dynamic> toJson() => _$IntelligenceEventEvidenceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

