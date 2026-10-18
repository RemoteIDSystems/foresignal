//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'osm_and_query.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OsmAndQuery {
  /// Returns a new [OsmAndQuery] instance.
  OsmAndQuery({

     this.id,

     this.deviceid,

     this.lat,

     this.lon,

     this.location,

     this.timestamp,

     this.speed,

     this.bearing,

     this.heading,

     this.altitude,

     this.accuracy,

     this.batt,

     this.charge,

     this.valid,
  });

  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final String? id;



  @JsonKey(
    
    name: r'deviceid',
    required: false,
    includeIfNull: false,
  )


  final String? deviceid;



  @JsonKey(
    
    name: r'lat',
    required: false,
    includeIfNull: false,
  )


  final num? lat;



  @JsonKey(
    
    name: r'lon',
    required: false,
    includeIfNull: false,
  )


  final num? lon;



      /// lat,lon
  @JsonKey(
    
    name: r'location',
    required: false,
    includeIfNull: false,
  )


  final String? location;



      /// epoch s, epoch ms, ISO, or `yyyy-MM-dd HH:mm:ss` UTC
  @JsonKey(
    
    name: r'timestamp',
    required: false,
    includeIfNull: false,
  )


  final String? timestamp;



      /// knots
  @JsonKey(
    
    name: r'speed',
    required: false,
    includeIfNull: false,
  )


  final num? speed;



  @JsonKey(
    
    name: r'bearing',
    required: false,
    includeIfNull: false,
  )


  final num? bearing;



  @JsonKey(
    
    name: r'heading',
    required: false,
    includeIfNull: false,
  )


  final num? heading;



  @JsonKey(
    
    name: r'altitude',
    required: false,
    includeIfNull: false,
  )


  final num? altitude;



  @JsonKey(
    
    name: r'accuracy',
    required: false,
    includeIfNull: false,
  )


  final num? accuracy;



  @JsonKey(
    
    name: r'batt',
    required: false,
    includeIfNull: false,
  )


  final num? batt;



  @JsonKey(
    
    name: r'charge',
    required: false,
    includeIfNull: false,
  )


  final bool? charge;



      /// false → rejected
  @JsonKey(
    
    name: r'valid',
    required: false,
    includeIfNull: false,
  )


  final bool? valid;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OsmAndQuery &&
      other.id == id &&
      other.deviceid == deviceid &&
      other.lat == lat &&
      other.lon == lon &&
      other.location == location &&
      other.timestamp == timestamp &&
      other.speed == speed &&
      other.bearing == bearing &&
      other.heading == heading &&
      other.altitude == altitude &&
      other.accuracy == accuracy &&
      other.batt == batt &&
      other.charge == charge &&
      other.valid == valid;

    @override
    int get hashCode =>
        id.hashCode +
        deviceid.hashCode +
        lat.hashCode +
        lon.hashCode +
        location.hashCode +
        timestamp.hashCode +
        speed.hashCode +
        bearing.hashCode +
        heading.hashCode +
        altitude.hashCode +
        accuracy.hashCode +
        batt.hashCode +
        charge.hashCode +
        valid.hashCode;

  factory OsmAndQuery.fromJson(Map<String, dynamic> json) => _$OsmAndQueryFromJson(json);

  Map<String, dynamic> toJson() => _$OsmAndQueryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

