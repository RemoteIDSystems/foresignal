//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_digest_now_radar.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherDigestNowRadar {
  /// Returns a new [PlaceWeatherDigestNowRadar] instance.
  PlaceWeatherDigestNowRadar({

    required  this.status,

     this.asOf,

     this.statusText,

     this.maxDbzInRadius,

     this.nearest50Km,
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



  @JsonKey(
    
    name: r'maxDbzInRadius',
    required: false,
    includeIfNull: false,
  )


  final num? maxDbzInRadius;



  @JsonKey(
    
    name: r'nearest50Km',
    required: false,
    includeIfNull: false,
  )


  final num? nearest50Km;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherDigestNowRadar &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText &&
      other.maxDbzInRadius == maxDbzInRadius &&
      other.nearest50Km == nearest50Km;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode +
        maxDbzInRadius.hashCode +
        nearest50Km.hashCode;

  factory PlaceWeatherDigestNowRadar.fromJson(Map<String, dynamic> json) => _$PlaceWeatherDigestNowRadarFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherDigestNowRadarToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

