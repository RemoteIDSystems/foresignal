//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/situation_social_section_narratives_inner.dart';
import 'package:foresignal_api/src/model/situation_social_section_attention_gap.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_social_section.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationSocialSection {
  /// Returns a new [SituationSocialSection] instance.
  SituationSocialSection({

    required  this.narratives,

    required  this.socialEvidenceGroups,

     this.attentionGap,

    required  this.socialCoverageOk,

     this.updatedAt,
  });

  @JsonKey(
    
    name: r'narratives',
    required: true,
    includeIfNull: false,
  )


  final List<SituationSocialSectionNarrativesInner> narratives;



      /// Narrative evidence groups, counted separately from physical corroboration
          // minimum: 0
  @JsonKey(
    
    name: r'socialEvidenceGroups',
    required: true,
    includeIfNull: false,
  )


  final int socialEvidenceGroups;



  @JsonKey(
    
    name: r'attentionGap',
    required: false,
    includeIfNull: false,
  )


  final SituationSocialSectionAttentionGap? attentionGap;



      /// 135 social observability for the situation's regions
  @JsonKey(
    
    name: r'socialCoverageOk',
    required: true,
    includeIfNull: false,
  )


  final bool socialCoverageOk;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationSocialSection &&
      other.narratives == narratives &&
      other.socialEvidenceGroups == socialEvidenceGroups &&
      other.attentionGap == attentionGap &&
      other.socialCoverageOk == socialCoverageOk &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        narratives.hashCode +
        socialEvidenceGroups.hashCode +
        (attentionGap == null ? 0 : attentionGap.hashCode) +
        socialCoverageOk.hashCode +
        updatedAt.hashCode;

  factory SituationSocialSection.fromJson(Map<String, dynamic> json) => _$SituationSocialSectionFromJson(json);

  Map<String, dynamic> toJson() => _$SituationSocialSectionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

