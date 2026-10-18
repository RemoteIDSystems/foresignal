//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_radar.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherRadar {
  /// Returns a new [PlaceWeatherRadar] instance.
  PlaceWeatherRadar({

    required  this.status,

     this.asOf,

     this.statusText,

     this.maxDbzInRadius,

     this.nearest40Km,

     this.nearest50Km,

     this.nearest50Km10m,

     this.nearest60Km,

     this.verifiedNearest50Km10m,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final WeatherSectionStatus status;



      /// Time of the last good data; present only while status is not ok, fixed while not ok
  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



      /// Explanation for non-ok statuses
  @JsonKey(
    
    name: r'statusText',
    required: false,
    includeIfNull: false,
  )


  final String? statusText;



      /// 0.5 dBZ steps
  @JsonKey(
    
    name: r'maxDbzInRadius',
    required: false,
    includeIfNull: false,
  )


  final num? maxDbzInRadius;



      /// km (999 = none within 100 km)
  @JsonKey(
    
    name: r'nearest40Km',
    required: false,
    includeIfNull: false,
  )


  final num? nearest40Km;



      /// km (999 = none within 100 km)
  @JsonKey(
    
    name: r'nearest50Km',
    required: false,
    includeIfNull: false,
  )


  final num? nearest50Km;



      /// Rolling 10-min minimum, km (999 = none within 100 km)
  @JsonKey(
    
    name: r'nearest50Km10m',
    required: false,
    includeIfNull: false,
  )


  final num? nearest50Km10m;



      /// km (999 = none within 100 km)
  @JsonKey(
    
    name: r'nearest60Km',
    required: false,
    includeIfNull: false,
  )


  final num? nearest60Km;



      /// Equals nearest50Km10m when ok, else 0 (for all-clear rules)
  @JsonKey(
    
    name: r'verifiedNearest50Km10m',
    required: false,
    includeIfNull: false,
  )


  final num? verifiedNearest50Km10m;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherRadar &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText &&
      other.maxDbzInRadius == maxDbzInRadius &&
      other.nearest40Km == nearest40Km &&
      other.nearest50Km == nearest50Km &&
      other.nearest50Km10m == nearest50Km10m &&
      other.nearest60Km == nearest60Km &&
      other.verifiedNearest50Km10m == verifiedNearest50Km10m;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode +
        maxDbzInRadius.hashCode +
        nearest40Km.hashCode +
        nearest50Km.hashCode +
        nearest50Km10m.hashCode +
        nearest60Km.hashCode +
        verifiedNearest50Km10m.hashCode;

  factory PlaceWeatherRadar.fromJson(Map<String, dynamic> json) => _$PlaceWeatherRadarFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherRadarToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

