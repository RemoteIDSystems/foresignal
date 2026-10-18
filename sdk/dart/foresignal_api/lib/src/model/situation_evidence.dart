//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/situation_evidence_families_inner.dart';
import 'package:foresignal_api/src/model/situation_evidence_member_counts.dart';
import 'package:foresignal_api/src/model/situation_evidence_corroboration.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_evidence.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationEvidence {
  /// Returns a new [SituationEvidence] instance.
  SituationEvidence({

    required  this.memberCounts,

    required  this.families,

    required  this.corroboration,
  });

  @JsonKey(
    
    name: r'memberCounts',
    required: true,
    includeIfNull: false,
  )


  final SituationEvidenceMemberCounts memberCounts;



  @JsonKey(
    
    name: r'families',
    required: true,
    includeIfNull: false,
  )


  final List<SituationEvidenceFamiliesInner> families;



  @JsonKey(
    
    name: r'corroboration',
    required: true,
    includeIfNull: false,
  )


  final SituationEvidenceCorroboration corroboration;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationEvidence &&
      other.memberCounts == memberCounts &&
      other.families == families &&
      other.corroboration == corroboration;

    @override
    int get hashCode =>
        memberCounts.hashCode +
        families.hashCode +
        corroboration.hashCode;

  factory SituationEvidence.fromJson(Map<String, dynamic> json) => _$SituationEvidenceFromJson(json);

  Map<String, dynamic> toJson() => _$SituationEvidenceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

