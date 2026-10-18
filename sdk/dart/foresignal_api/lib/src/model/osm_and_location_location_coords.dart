//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'osm_and_location_location_coords.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OsmAndLocationLocationCoords {
  /// Returns a new [OsmAndLocationLocationCoords] instance.
  OsmAndLocationLocationCoords({

    required  this.latitude,

    required  this.longitude,

     this.speed,

     this.heading,

     this.accuracy,

     this.altitude,
  });

  @JsonKey(
    
    name: r'latitude',
    required: true,
    includeIfNull: false,
  )


  final num latitude;



  @JsonKey(
    
    name: r'longitude',
    required: true,
    includeIfNull: false,
  )


  final num longitude;



      /// m/s
  @JsonKey(
    
    name: r'speed',
    required: false,
    includeIfNull: false,
  )


  final num? speed;



  @JsonKey(
    
    name: r'heading',
    required: false,
    includeIfNull: false,
  )


  final num? heading;



  @JsonKey(
    
    name: r'accuracy',
    required: false,
    includeIfNull: false,
  )


  final num? accuracy;



  @JsonKey(
    
    name: r'altitude',
    required: false,
    includeIfNull: false,
  )


  final num? altitude;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OsmAndLocationLocationCoords &&
      other.latitude == latitude &&
      other.longitude == longitude &&
      other.speed == speed &&
      other.heading == heading &&
      other.accuracy == accuracy &&
      other.altitude == altitude;

    @override
    int get hashCode =>
        latitude.hashCode +
        longitude.hashCode +
        speed.hashCode +
        heading.hashCode +
        accuracy.hashCode +
        altitude.hashCode;

  factory OsmAndLocationLocationCoords.fromJson(Map<String, dynamic> json) => _$OsmAndLocationLocationCoordsFromJson(json);

  Map<String, dynamic> toJson() => _$OsmAndLocationLocationCoordsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

