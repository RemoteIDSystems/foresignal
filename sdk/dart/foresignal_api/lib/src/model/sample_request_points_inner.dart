//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/sample_request_points_inner_one_of.dart';
import 'package:foresignal_api/src/model/sample_request_points_inner_one_of1.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sample_request_points_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SampleRequestPointsInner {
  /// Returns a new [SampleRequestPointsInner] instance.
  SampleRequestPointsInner({

    required  this.lon,

    required  this.lat,

    required  this.place,
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



      /// Place id (requires a token)
  @JsonKey(
    
    name: r'place',
    required: true,
    includeIfNull: false,
  )


  final String place;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SampleRequestPointsInner &&
      other.lon == lon &&
      other.lat == lat &&
      other.place == place;

    @override
    int get hashCode =>
        lon.hashCode +
        lat.hashCode +
        place.hashCode;

  factory SampleRequestPointsInner.fromJson(Map<String, dynamic> json) => _$SampleRequestPointsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SampleRequestPointsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

