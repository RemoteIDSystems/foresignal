//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sample_request_points_inner_one_of.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SampleRequestPointsInnerOneOf {
  /// Returns a new [SampleRequestPointsInnerOneOf] instance.
  SampleRequestPointsInnerOneOf({

    required  this.lon,

    required  this.lat,
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





    @override
    bool operator ==(Object other) => identical(this, other) || other is SampleRequestPointsInnerOneOf &&
      other.lon == lon &&
      other.lat == lat;

    @override
    int get hashCode =>
        lon.hashCode +
        lat.hashCode;

  factory SampleRequestPointsInnerOneOf.fromJson(Map<String, dynamic> json) => _$SampleRequestPointsInnerOneOfFromJson(json);

  Map<String, dynamic> toJson() => _$SampleRequestPointsInnerOneOfToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

