//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_volume_point.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialVolumePoint {
  /// Returns a new [SocialVolumePoint] instance.
  SocialVolumePoint({

    required  this.t,

    required  this.observed,

     this.baseline,

     this.mad,

     this.velocityRatio,

     this.zRobust,

    required  this.complete,
  });

  @JsonKey(
    
    name: r't',
    required: true,
    includeIfNull: false,
  )


  final DateTime t;



          // minimum: 0
  @JsonKey(
    
    name: r'observed',
    required: true,
    includeIfNull: false,
  )


  final num observed;



      /// Hour-of-week median over the trailing 8 weeks, scaled by the platform-language denominator
  @JsonKey(
    
    name: r'baseline',
    required: false,
    includeIfNull: false,
  )


  final num? baseline;



  @JsonKey(
    
    name: r'mad',
    required: false,
    includeIfNull: false,
  )


  final num? mad;



      /// observed / baseline
  @JsonKey(
    
    name: r'velocityRatio',
    required: false,
    includeIfNull: false,
  )


  final num? velocityRatio;



  @JsonKey(
    
    name: r'zRobust',
    required: false,
    includeIfNull: false,
  )


  final num? zRobust;



      /// Collection was complete for the interval
  @JsonKey(
    
    name: r'complete',
    required: true,
    includeIfNull: false,
  )


  final bool complete;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialVolumePoint &&
      other.t == t &&
      other.observed == observed &&
      other.baseline == baseline &&
      other.mad == mad &&
      other.velocityRatio == velocityRatio &&
      other.zRobust == zRobust &&
      other.complete == complete;

    @override
    int get hashCode =>
        t.hashCode +
        observed.hashCode +
        (baseline == null ? 0 : baseline.hashCode) +
        (mad == null ? 0 : mad.hashCode) +
        (velocityRatio == null ? 0 : velocityRatio.hashCode) +
        (zRobust == null ? 0 : zRobust.hashCode) +
        complete.hashCode;

  factory SocialVolumePoint.fromJson(Map<String, dynamic> json) => _$SocialVolumePointFromJson(json);

  Map<String, dynamic> toJson() => _$SocialVolumePointToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

