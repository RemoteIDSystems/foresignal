//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_collection_type.dart';
import 'package:foresignal_api/src/model/social_volume_point.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_volume_series.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialVolumeSeries {
  /// Returns a new [SocialVolumeSeries] instance.
  SocialVolumeSeries({

    required  this.platform,

     this.language,

    required  this.seriesRef,

    required  this.collectionType,

    required  this.baselineEligible,

    required  this.points,
  });

      /// Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the platform an EDP provider (024, 138) declares
  @JsonKey(
    
    name: r'platform',
    required: true,
    includeIfNull: false,
  )


  final String platform;



  @JsonKey(
    
    name: r'language',
    required: false,
    includeIfNull: false,
  )


  final String? language;



      /// 119 analysis-series registry id `social:{scope}/{platform}/{metric}`
  @JsonKey(
    
    name: r'seriesRef',
    required: true,
    includeIfNull: false,
  )


  final String seriesRef;



  @JsonKey(
    
    name: r'collectionType',
    required: true,
    includeIfNull: false,
  )


  final SocialCollectionType collectionType;



      /// False for sampled paths (no baseline, no velocity)
  @JsonKey(
    
    name: r'baselineEligible',
    required: true,
    includeIfNull: false,
  )


  final bool baselineEligible;



  @JsonKey(
    
    name: r'points',
    required: true,
    includeIfNull: false,
  )


  final List<SocialVolumePoint> points;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialVolumeSeries &&
      other.platform == platform &&
      other.language == language &&
      other.seriesRef == seriesRef &&
      other.collectionType == collectionType &&
      other.baselineEligible == baselineEligible &&
      other.points == points;

    @override
    int get hashCode =>
        platform.hashCode +
        (language == null ? 0 : language.hashCode) +
        seriesRef.hashCode +
        collectionType.hashCode +
        baselineEligible.hashCode +
        points.hashCode;

  factory SocialVolumeSeries.fromJson(Map<String, dynamic> json) => _$SocialVolumeSeriesFromJson(json);

  Map<String, dynamic> toJson() => _$SocialVolumeSeriesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

