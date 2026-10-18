//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/social_volume_series.dart';
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:foresignal_api/src/model/social_volume_scope.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_volume.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialVolume {
  /// Returns a new [SocialVolume] instance.
  SocialVolume({

    required  this.scope,

    required  this.metric,

    required  this.interval,

    required  this.since,

    required  this.until,

    required  this.series,

    required  this.baseline,

     this.caveats,
  });

  @JsonKey(
    
    name: r'scope',
    required: true,
    includeIfNull: false,
  )


  final SocialVolumeScope scope;



  @JsonKey(
    
    name: r'metric',
    required: true,
    includeIfNull: false,
  )


  final SocialVolumeMetricEnum metric;



  @JsonKey(
    
    name: r'interval',
    required: true,
    includeIfNull: false,
  )


  final SocialVolumeIntervalEnum interval;



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
    
    name: r'series',
    required: true,
    includeIfNull: false,
  )


  final List<SocialVolumeSeries> series;



  @JsonKey(
    
    name: r'baseline',
    required: true,
    includeIfNull: false,
  )


  final DetectorRef baseline;



  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialVolume &&
      other.scope == scope &&
      other.metric == metric &&
      other.interval == interval &&
      other.since == since &&
      other.until == until &&
      other.series == series &&
      other.baseline == baseline &&
      other.caveats == caveats;

    @override
    int get hashCode =>
        scope.hashCode +
        metric.hashCode +
        interval.hashCode +
        since.hashCode +
        until.hashCode +
        series.hashCode +
        baseline.hashCode +
        caveats.hashCode;

  factory SocialVolume.fromJson(Map<String, dynamic> json) => _$SocialVolumeFromJson(json);

  Map<String, dynamic> toJson() => _$SocialVolumeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SocialVolumeMetricEnum {
@JsonValue(r'posts')
posts(r'posts'),
@JsonValue(r'uniqueAuthors')
uniqueAuthors(r'uniqueAuthors'),
@JsonValue(r'reposts')
reposts(r'reposts'),
@JsonValue(r'engagement')
engagement(r'engagement'),
@JsonValue(r'denominator')
denominator(r'denominator');

const SocialVolumeMetricEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SocialVolumeIntervalEnum {
@JsonValue(r'PT1H')
PT1H(r'PT1H'),
@JsonValue(r'P1D')
P1D(r'P1D');

const SocialVolumeIntervalEnum(this.value);

final String value;

@override
String toString() => value;
}


