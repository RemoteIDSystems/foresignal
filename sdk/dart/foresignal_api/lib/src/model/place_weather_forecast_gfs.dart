//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_forecast_gfs.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherForecastGfs {
  /// Returns a new [PlaceWeatherForecastGfs] instance.
  PlaceWeatherForecastGfs({

     this.status,

     this.asOf,

     this.statusText,

     this.gustMax24h,

     this.precip24hMm,
  });

  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final WeatherSectionStatus? status;



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



      /// m/s
  @JsonKey(
    
    name: r'gustMax24h',
    required: false,
    includeIfNull: false,
  )


  final num? gustMax24h;



  @JsonKey(
    
    name: r'precip24hMm',
    required: false,
    includeIfNull: false,
  )


  final num? precip24hMm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherForecastGfs &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText &&
      other.gustMax24h == gustMax24h &&
      other.precip24hMm == precip24hMm;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode +
        gustMax24h.hashCode +
        precip24hMm.hashCode;

  factory PlaceWeatherForecastGfs.fromJson(Map<String, dynamic> json) => _$PlaceWeatherForecastGfsFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherForecastGfsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

