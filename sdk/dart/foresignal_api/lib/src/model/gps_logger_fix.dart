//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'gps_logger_fix.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GpsLoggerFix {
  /// Returns a new [GpsLoggerFix] instance.
  GpsLoggerFix({

    required  this.lat,

    required  this.lon,

     this.time,

     this.acc,

     this.alt,

     this.spd,

     this.dir,

     this.batt,

     this.dev,
  });

  @JsonKey(
    
    name: r'lat',
    required: true,
    includeIfNull: false,
  )


  final num lat;



  @JsonKey(
    
    name: r'lon',
    required: true,
    includeIfNull: false,
  )


  final num lon;



  @JsonKey(
    
    name: r'time',
    required: false,
    includeIfNull: false,
  )


  final String? time;



  @JsonKey(
    
    name: r'acc',
    required: false,
    includeIfNull: false,
  )


  final num? acc;



  @JsonKey(
    
    name: r'alt',
    required: false,
    includeIfNull: false,
  )


  final num? alt;



      /// m/s
  @JsonKey(
    
    name: r'spd',
    required: false,
    includeIfNull: false,
  )


  final num? spd;



  @JsonKey(
    
    name: r'dir',
    required: false,
    includeIfNull: false,
  )


  final num? dir;



  @JsonKey(
    
    name: r'batt',
    required: false,
    includeIfNull: false,
  )


  final num? batt;



      /// → externalId
  @JsonKey(
    
    name: r'dev',
    required: false,
    includeIfNull: false,
  )


  final String? dev;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GpsLoggerFix &&
      other.lat == lat &&
      other.lon == lon &&
      other.time == time &&
      other.acc == acc &&
      other.alt == alt &&
      other.spd == spd &&
      other.dir == dir &&
      other.batt == batt &&
      other.dev == dev;

    @override
    int get hashCode =>
        lat.hashCode +
        lon.hashCode +
        time.hashCode +
        acc.hashCode +
        alt.hashCode +
        spd.hashCode +
        dir.hashCode +
        batt.hashCode +
        dev.hashCode;

  factory GpsLoggerFix.fromJson(Map<String, dynamic> json) => _$GpsLoggerFixFromJson(json);

  Map<String, dynamic> toJson() => _$GpsLoggerFixToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

