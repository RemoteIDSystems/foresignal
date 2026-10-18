//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/lead_time_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'lead_time_stats_groups_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LeadTimeStatsGroupsInner {
  /// Returns a new [LeadTimeStatsGroupsInner] instance.
  LeadTimeStatsGroupsInner({

    required  this.claimType,

    required  this.platform,

     this.regionCode,

    required  this.foundBy,

    required  this.comparisons,

    required  this.outOfSample,

    required  this.overPhysical,

    required  this.overNews,

    required  this.overOfficial,

     this.outOfSampleOverPhysical,
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
    
    name: r'foundBy',
    required: true,
    includeIfNull: false,
  )


  final LeadTimeStatsGroupsInnerFoundByEnum foundBy;



          // minimum: 0
  @JsonKey(
    
    name: r'comparisons',
    required: true,
    includeIfNull: false,
  )


  final int comparisons;



          // minimum: 0
  @JsonKey(
    
    name: r'outOfSample',
    required: true,
    includeIfNull: false,
  )


  final int outOfSample;



  @JsonKey(
    
    name: r'overPhysical',
    required: true,
    includeIfNull: false,
  )


  final LeadTimeSummary overPhysical;



  @JsonKey(
    
    name: r'overNews',
    required: true,
    includeIfNull: false,
  )


  final LeadTimeSummary overNews;



  @JsonKey(
    
    name: r'overOfficial',
    required: true,
    includeIfNull: false,
  )


  final LeadTimeSummary overOfficial;



  @JsonKey(
    
    name: r'outOfSampleOverPhysical',
    required: false,
    includeIfNull: false,
  )


  final LeadTimeSummary? outOfSampleOverPhysical;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LeadTimeStatsGroupsInner &&
      other.claimType == claimType &&
      other.platform == platform &&
      other.regionCode == regionCode &&
      other.foundBy == foundBy &&
      other.comparisons == comparisons &&
      other.outOfSample == outOfSample &&
      other.overPhysical == overPhysical &&
      other.overNews == overNews &&
      other.overOfficial == overOfficial &&
      other.outOfSampleOverPhysical == outOfSampleOverPhysical;

    @override
    int get hashCode =>
        claimType.hashCode +
        platform.hashCode +
        (regionCode == null ? 0 : regionCode.hashCode) +
        foundBy.hashCode +
        comparisons.hashCode +
        outOfSample.hashCode +
        overPhysical.hashCode +
        overNews.hashCode +
        overOfficial.hashCode +
        outOfSampleOverPhysical.hashCode;

  factory LeadTimeStatsGroupsInner.fromJson(Map<String, dynamic> json) => _$LeadTimeStatsGroupsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$LeadTimeStatsGroupsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum LeadTimeStatsGroupsInnerFoundByEnum {
@JsonValue(r'organic')
organic(r'organic'),
@JsonValue(r'cue')
cue(r'cue');

const LeadTimeStatsGroupsInnerFoundByEnum(this.value);

final String value;

@override
String toString() => value;
}


