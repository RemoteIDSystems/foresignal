//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/narrative_timeline_series_inner_points_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'narrative_timeline_series_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NarrativeTimelineSeriesInner {
  /// Returns a new [NarrativeTimelineSeriesInner] instance.
  NarrativeTimelineSeriesInner({

    required  this.platform,

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
    
    name: r'points',
    required: true,
    includeIfNull: false,
  )


  final List<NarrativeTimelineSeriesInnerPointsInner> points;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NarrativeTimelineSeriesInner &&
      other.platform == platform &&
      other.points == points;

    @override
    int get hashCode =>
        platform.hashCode +
        points.hashCode;

  factory NarrativeTimelineSeriesInner.fromJson(Map<String, dynamic> json) => _$NarrativeTimelineSeriesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$NarrativeTimelineSeriesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

