//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_settings_velocity.dart';
import 'package:foresignal_api/src/model/social_settings_cues.dart';
import 'package:foresignal_api/src/model/social_settings_attention_gap.dart';
import 'package:foresignal_api/src/model/social_settings_track_record.dart';
import 'package:foresignal_api/src/model/social_settings_compare.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialSettings {
  /// Returns a new [SocialSettings] instance.
  SocialSettings({

     this.languages,

     this.minRegionBaseline = 20,

     this.velocity,

     this.llmClaimMinPosts = 50,

     this.llmClaimMinEvidenceGroups = 5,

     this.keepMedia = false,

     this.trackRecord,

     this.compare,

     this.attentionGap,

     this.cues,
  });

      /// Detection languages (Lingua)
  @JsonKey(
    
    name: r'languages',
    required: false,
    includeIfNull: false,
  )


  final List<String>? languages;



      /// Posts per hour below which social observability is LOW_SOCIAL_COVERAGE
          // minimum: 0
  @JsonKey(
    defaultValue: 20,
    name: r'minRegionBaseline',
    required: false,
    includeIfNull: false,
  )


  final num? minRegionBaseline;



  @JsonKey(
    
    name: r'velocity',
    required: false,
    includeIfNull: false,
  )


  final SocialSettingsVelocity? velocity;



          // minimum: 1
  @JsonKey(
    defaultValue: 50,
    name: r'llmClaimMinPosts',
    required: false,
    includeIfNull: false,
  )


  final int? llmClaimMinPosts;



      /// Evidence groups that make a narrative eligible for LLM claim extraction whatever its size; fixed at 5 in this release (a PUT with another value is refused)
          // minimum: 1
  @JsonKey(
    defaultValue: 5,
    name: r'llmClaimMinEvidenceGroups',
    required: false,
    includeIfNull: false,
  )


  final int? llmClaimMinEvidenceGroups;



      /// Store fingerprinted images in 043 under the post's retention
  @JsonKey(
    defaultValue: false,
    name: r'keepMedia',
    required: false,
    includeIfNull: false,
  )


  final bool? keepMedia;



  @JsonKey(
    
    name: r'trackRecord',
    required: false,
    includeIfNull: false,
  )


  final SocialSettingsTrackRecord? trackRecord;



  @JsonKey(
    
    name: r'compare',
    required: false,
    includeIfNull: false,
  )


  final SocialSettingsCompare? compare;



  @JsonKey(
    
    name: r'attentionGap',
    required: false,
    includeIfNull: false,
  )


  final SocialSettingsAttentionGap? attentionGap;



  @JsonKey(
    
    name: r'cues',
    required: false,
    includeIfNull: false,
  )


  final SocialSettingsCues? cues;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialSettings &&
      other.languages == languages &&
      other.minRegionBaseline == minRegionBaseline &&
      other.velocity == velocity &&
      other.llmClaimMinPosts == llmClaimMinPosts &&
      other.llmClaimMinEvidenceGroups == llmClaimMinEvidenceGroups &&
      other.keepMedia == keepMedia &&
      other.trackRecord == trackRecord &&
      other.compare == compare &&
      other.attentionGap == attentionGap &&
      other.cues == cues;

    @override
    int get hashCode =>
        languages.hashCode +
        minRegionBaseline.hashCode +
        velocity.hashCode +
        llmClaimMinPosts.hashCode +
        llmClaimMinEvidenceGroups.hashCode +
        keepMedia.hashCode +
        trackRecord.hashCode +
        compare.hashCode +
        attentionGap.hashCode +
        cues.hashCode;

  factory SocialSettings.fromJson(Map<String, dynamic> json) => _$SocialSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$SocialSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

