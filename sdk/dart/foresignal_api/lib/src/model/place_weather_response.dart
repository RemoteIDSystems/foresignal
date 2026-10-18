//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/record.dart';
import 'package:foresignal_api/src/model/forecast_series.dart';
import 'package:foresignal_api/src/model/place_weather.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherResponse {
  /// Returns a new [PlaceWeatherResponse] instance.
  PlaceWeatherResponse({

    required  this.placeId,

     this.place,

     this.timezone,

     this.record,

     this.weather,

    required  this.forecastSeries,

    required  this.serverTime,
  });

  @JsonKey(
    
    name: r'placeId',
    required: true,
    includeIfNull: false,
  )


  final String placeId;



  @JsonKey(
    
    name: r'place',
    required: false,
    includeIfNull: false,
  )


  final String? place;



  @JsonKey(
    
    name: r'timezone',
    required: false,
    includeIfNull: false,
  )


  final String? timezone;



  @JsonKey(
    
    name: r'record',
    required: false,
    includeIfNull: false,
  )


  final Record? record;



  @JsonKey(
    
    name: r'weather',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeather? weather;



  @JsonKey(
    
    name: r'forecastSeries',
    required: true,
    includeIfNull: false,
  )


  final List<ForecastSeries> forecastSeries;



  @JsonKey(
    
    name: r'serverTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime serverTime;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherResponse &&
      other.placeId == placeId &&
      other.place == place &&
      other.timezone == timezone &&
      other.record == record &&
      other.weather == weather &&
      other.forecastSeries == forecastSeries &&
      other.serverTime == serverTime;

    @override
    int get hashCode =>
        placeId.hashCode +
        place.hashCode +
        timezone.hashCode +
        (record == null ? 0 : record.hashCode) +
        (weather == null ? 0 : weather.hashCode) +
        forecastSeries.hashCode +
        serverTime.hashCode;

  factory PlaceWeatherResponse.fromJson(Map<String, dynamic> json) => _$PlaceWeatherResponseFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

