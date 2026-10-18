//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/lead_time_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_versus_observed_lead_times_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialVersusObservedLeadTimesInner {
  /// Returns a new [SocialVersusObservedLeadTimesInner] instance.
  SocialVersusObservedLeadTimesInner({

    required  this.claimType,

    required  this.platform,

     this.regionCode,

    required  this.overPhysical,
  });

  @JsonKey(
    
    name: r'claimType',
    required: true,
    includeIfNull: false,
  )


  final String claimType;



      /// Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the platform an EDP provider (024, 138) declares
  @JsonKey(
    
    name: r'platform',
    required: true,
    includeIfNull: false,
  )


  final String platform;



  @JsonKey(
    
    name: r'regionCode',
    required: false,
    includeIfNull: false,
  )


  final String? regionCode;



  @JsonKey(
    
    name: r'overPhysical',
    required: true,
    includeIfNull: false,
  )


  final LeadTimeSummary overPhysical;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialVersusObservedLeadTimesInner &&
      other.claimType == claimType &&
      other.platform == platform &&
      other.regionCode == regionCode &&
      other.overPhysical == overPhysical;

    @override
    int get hashCode =>
        claimType.hashCode +
        platform.hashCode +
        (regionCode == null ? 0 : regionCode.hashCode) +
        overPhysical.hashCode;

  factory SocialVersusObservedLeadTimesInner.fromJson(Map<String, dynamic> json) => _$SocialVersusObservedLeadTimesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SocialVersusObservedLeadTimesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

