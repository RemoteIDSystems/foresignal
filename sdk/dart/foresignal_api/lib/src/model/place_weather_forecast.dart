//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/place_weather_forecast_gfs.dart';
import 'package:foresignal_api/src/model/place_weather_forecast_nws.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_forecast.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherForecast {
  /// Returns a new [PlaceWeatherForecast] instance.
  PlaceWeatherForecast({

     this.gfs,

     this.nws,
  });

  @JsonKey(
    
    name: r'gfs',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherForecastGfs? gfs;



  @JsonKey(
    
    name: r'nws',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherForecastNws? nws;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherForecast &&
      other.gfs == gfs &&
      other.nws == nws;

    @override
    int get hashCode =>
        gfs.hashCode +
        nws.hashCode;

  factory PlaceWeatherForecast.fromJson(Map<String, dynamic> json) => _$PlaceWeatherForecastFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherForecastToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

