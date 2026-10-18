//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/narrative_timeline_series_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'narrative_timeline.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NarrativeTimeline {
  /// Returns a new [NarrativeTimeline] instance.
  NarrativeTimeline({

    required  this.narrativeId,

    required  this.interval,

    required  this.series,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'narrativeId',
    required: true,
    includeIfNull: false,
  )


  final String narrativeId;



  @JsonKey(
    
    name: r'interval',
    required: true,
    includeIfNull: false,
  )


  final NarrativeTimelineIntervalEnum interval;



  @JsonKey(
    
    name: r'series',
    required: true,
    includeIfNull: false,
  )


  final List<NarrativeTimelineSeriesInner> series;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NarrativeTimeline &&
      other.narrativeId == narrativeId &&
      other.interval == interval &&
      other.series == series;

    @override
    int get hashCode =>
        narrativeId.hashCode +
        interval.hashCode +
        series.hashCode;

  factory NarrativeTimeline.fromJson(Map<String, dynamic> json) => _$NarrativeTimelineFromJson(json);

  Map<String, dynamic> toJson() => _$NarrativeTimelineToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NarrativeTimelineIntervalEnum {
@JsonValue(r'PT5M')
PT5M(r'PT5M'),
@JsonValue(r'PT15M')
PT15M(r'PT15M'),
@JsonValue(r'PT1H')
PT1H(r'PT1H'),
@JsonValue(r'P1D')
P1D(r'P1D');

const NarrativeTimelineIntervalEnum(this.value);

final String value;

@override
String toString() => value;
}


