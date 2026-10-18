//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/situation_member.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_evidence_families_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationEvidenceFamiliesInner {
  /// Returns a new [SituationEvidenceFamiliesInner] instance.
  SituationEvidenceFamiliesInner({

    required  this.family,

    required  this.count,

     this.coreCount,

    required  this.members,

     this.more,
  });

      /// Signal family from the 119 registry (`intel/signal-families.json`, operator-extensible), e.g. `ais-behaviour`, `aviation-military`, `aviation-ops`, `conflict`, `outage`, `internet-measurement`, `gnss-interference`, `nav-warning`, `attention`, `seismic`, `fire`, `weather-severe`, `cyber`, `markets`, `sanctions`, `health`, `space`
  @JsonKey(
    
    name: r'family',
    required: true,
    includeIfNull: false,
  )


  final String family;



  @JsonKey(
    
    name: r'count',
    required: true,
    includeIfNull: false,
  )


  final int count;



  @JsonKey(
    
    name: r'coreCount',
    required: false,
    includeIfNull: false,
  )


  final int? coreCount;



  @JsonKey(
    
    name: r'members',
    required: true,
    includeIfNull: false,
  )


  final List<SituationMember> members;



      /// True when the family has more members than listed
  @JsonKey(
    
    name: r'more',
    required: false,
    includeIfNull: false,
  )


  final bool? more;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationEvidenceFamiliesInner &&
      other.family == family &&
      other.count == count &&
      other.coreCount == coreCount &&
      other.members == members &&
      other.more == more;

    @override
    int get hashCode =>
        family.hashCode +
        count.hashCode +
        coreCount.hashCode +
        members.hashCode +
        more.hashCode;

  factory SituationEvidenceFamiliesInner.fromJson(Map<String, dynamic> json) => _$SituationEvidenceFamiliesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SituationEvidenceFamiliesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

