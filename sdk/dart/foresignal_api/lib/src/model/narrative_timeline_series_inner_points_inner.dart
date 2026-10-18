//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'narrative_timeline_series_inner_points_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NarrativeTimelineSeriesInnerPointsInner {
  /// Returns a new [NarrativeTimelineSeriesInnerPointsInner] instance.
  NarrativeTimelineSeriesInnerPointsInner({

    required  this.t,

    required  this.posts,

    required  this.uniqueAuthors,
  });

  @JsonKey(
    
    name: r't',
    required: true,
    includeIfNull: false,
  )


  final DateTime t;



          // minimum: 0
  @JsonKey(
    
    name: r'posts',
    required: true,
    includeIfNull: false,
  )


  final int posts;



          // minimum: 0
  @JsonKey(
    
    name: r'uniqueAuthors',
    required: true,
    includeIfNull: false,
  )


  final int uniqueAuthors;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NarrativeTimelineSeriesInnerPointsInner &&
      other.t == t &&
      other.posts == posts &&
      other.uniqueAuthors == uniqueAuthors;

    @override
    int get hashCode =>
        t.hashCode +
        posts.hashCode +
        uniqueAuthors.hashCode;

  factory NarrativeTimelineSeriesInnerPointsInner.fromJson(Map<String, dynamic> json) => _$NarrativeTimelineSeriesInnerPointsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$NarrativeTimelineSeriesInnerPointsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

