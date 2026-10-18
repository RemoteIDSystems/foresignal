//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_coverage_bucket.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_coverage.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialCoverage {
  /// Returns a new [SocialCoverage] instance.
  SocialCoverage({

    required  this.platform,

     this.regionCode,

    required  this.since,

    required  this.until,

    required  this.buckets,

    required  this.completeShare,

     this.baselinePostsPerHour,

    required  this.absenceOk,

     this.reason,
  });

      /// Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the platform an EDP provider (024, 138) declares
  @JsonKey(
    
    name: r'platform',
    required: true,
    includeIfNull: false,
  )


  final String platform;



      /// 040 region code; null = all regions
  @JsonKey(
    
    name: r'regionCode',
    required: false,
    includeIfNull: false,
  )


  final String? regionCode;



  @JsonKey(
    
    name: r'since',
    required: true,
    includeIfNull: false,
  )


  final DateTime since;



  @JsonKey(
    
    name: r'until',
    required: true,
    includeIfNull: false,
  )


  final DateTime until;



  @JsonKey(
    
    name: r'buckets',
    required: true,
    includeIfNull: false,
  )


  final List<SocialCoverageBucket> buckets;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'completeShare',
    required: true,
    includeIfNull: false,
  )


  final num completeShare;



      /// Trailing 8-week baseline used by social observability
  @JsonKey(
    
    name: r'baselinePostsPerHour',
    required: false,
    includeIfNull: false,
  )


  final num? baselinePostsPerHour;



      /// `canSeeAbsence(social-attention, region, window)`: a healthy complete-stream platform and baseline ≥ `social.minRegionBaseline`
  @JsonKey(
    
    name: r'absenceOk',
    required: true,
    includeIfNull: false,
  )


  final bool absenceOk;



      /// LOW_SOCIAL_COVERAGE or a gap reason when absenceOk is false
  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialCoverage &&
      other.platform == platform &&
      other.regionCode == regionCode &&
      other.since == since &&
      other.until == until &&
      other.buckets == buckets &&
      other.completeShare == completeShare &&
      other.baselinePostsPerHour == baselinePostsPerHour &&
      other.absenceOk == absenceOk &&
      other.reason == reason;

    @override
    int get hashCode =>
        platform.hashCode +
        (regionCode == null ? 0 : regionCode.hashCode) +
        since.hashCode +
        until.hashCode +
        buckets.hashCode +
        completeShare.hashCode +
        (baselinePostsPerHour == null ? 0 : baselinePostsPerHour.hashCode) +
        absenceOk.hashCode +
        (reason == null ? 0 : reason.hashCode);

  factory SocialCoverage.fromJson(Map<String, dynamic> json) => _$SocialCoverageFromJson(json);

  Map<String, dynamic> toJson() => _$SocialCoverageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

