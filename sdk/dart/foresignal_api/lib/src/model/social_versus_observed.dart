//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/social_versus_observed_lead_times_inner.dart';
import 'package:foresignal_api/src/model/social_versus_observed_narratives_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_versus_observed.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialVersusObserved {
  /// Returns a new [SocialVersusObserved] instance.
  SocialVersusObserved({

    required  this.narrativesByStatus,

    required  this.narratives,

     this.attentionGapFindingIds,

     this.leadTimes,

     this.mediaReuseFindingIds,

     this.coordinationFindingIds,

     this.caveats,
  });

      /// Status → narrative count
  @JsonKey(
    
    name: r'narrativesByStatus',
    required: true,
    includeIfNull: false,
  )


  final Map<String, int> narrativesByStatus;



  @JsonKey(
    
    name: r'narratives',
    required: true,
    includeIfNull: false,
  )


  final List<SocialVersusObservedNarrativesInner> narratives;



  @JsonKey(
    
    name: r'attentionGapFindingIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? attentionGapFindingIds;



      /// Out-of-sample statistics only
  @JsonKey(
    
    name: r'leadTimes',
    required: false,
    includeIfNull: false,
  )


  final List<SocialVersusObservedLeadTimesInner>? leadTimes;



  @JsonKey(
    
    name: r'mediaReuseFindingIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? mediaReuseFindingIds;



  @JsonKey(
    
    name: r'coordinationFindingIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? coordinationFindingIds;



  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialVersusObserved &&
      other.narrativesByStatus == narrativesByStatus &&
      other.narratives == narratives &&
      other.attentionGapFindingIds == attentionGapFindingIds &&
      other.leadTimes == leadTimes &&
      other.mediaReuseFindingIds == mediaReuseFindingIds &&
      other.coordinationFindingIds == coordinationFindingIds &&
      other.caveats == caveats;

    @override
    int get hashCode =>
        narrativesByStatus.hashCode +
        narratives.hashCode +
        attentionGapFindingIds.hashCode +
        leadTimes.hashCode +
        mediaReuseFindingIds.hashCode +
        coordinationFindingIds.hashCode +
        caveats.hashCode;

  factory SocialVersusObserved.fromJson(Map<String, dynamic> json) => _$SocialVersusObservedFromJson(json);

  Map<String, dynamic> toJson() => _$SocialVersusObservedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

