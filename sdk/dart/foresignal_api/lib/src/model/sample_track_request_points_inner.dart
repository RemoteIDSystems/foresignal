//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sample_track_request_points_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SampleTrackRequestPointsInner {
  /// Returns a new [SampleTrackRequestPointsInner] instance.
  SampleTrackRequestPointsInner({

    required  this.lon,

    required  this.lat,

    required  this.time,
  });

          // minimum: -180
          // maximum: 180
  @JsonKey(
    
    name: r'lon',
    required: true,
    includeIfNull: false,
  )


  final num lon;



          // minimum: -90
          // maximum: 90
  @JsonKey(
    
    name: r'lat',
    required: true,
    includeIfNull: false,
  )


  final num lat;



  @JsonKey(
    
    name: r'time',
    required: true,
    includeIfNull: false,
  )


  final DateTime time;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SampleTrackRequestPointsInner &&
      other.lon == lon &&
      other.lat == lat &&
      other.time == time;

    @override
    int get hashCode =>
        lon.hashCode +
        lat.hashCode +
        time.hashCode;

  factory SampleTrackRequestPointsInner.fromJson(Map<String, dynamic> json) => _$SampleTrackRequestPointsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SampleTrackRequestPointsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

