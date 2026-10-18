//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/place_weather_cell.dart';
import 'package:foresignal_api/src/model/place_weather_lightning.dart';
import 'package:foresignal_api/src/model/place_weather_marine.dart';
import 'package:foresignal_api/src/model/place_weather_smoke.dart';
import 'package:foresignal_api/src/model/place_weather_uv.dart';
import 'package:foresignal_api/src/model/place_weather_forecast.dart';
import 'package:foresignal_api/src/model/place_weather_aurora.dart';
import 'package:foresignal_api/src/model/place_weather_hail.dart';
import 'package:foresignal_api/src/model/place_weather_radar.dart';
import 'package:foresignal_api/src/model/place_weather_precip.dart';
import 'package:foresignal_api/src/model/place_weather_heat.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeather {
  /// Returns a new [PlaceWeather] instance.
  PlaceWeather({

    required  this.place,

    required  this.radiusKm,

     this.radar,

     this.hail,

     this.precip,

     this.lightning,

     this.cell,

     this.smoke,

     this.forecast,

    required  this.automated,

    required  this.notAWarning,

     this.marine,

     this.aurora,

     this.heat,

     this.uv,
  });

      /// Place name
  @JsonKey(
    
    name: r'place',
    required: true,
    includeIfNull: false,
  )


  final String place;



  @JsonKey(
    
    name: r'radiusKm',
    required: true,
    includeIfNull: false,
  )


  final num radiusKm;



  @JsonKey(
    
    name: r'radar',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherRadar? radar;



  @JsonKey(
    
    name: r'hail',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherHail? hail;



  @JsonKey(
    
    name: r'precip',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherPrecip? precip;



  @JsonKey(
    
    name: r'lightning',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherLightning? lightning;



  @JsonKey(
    
    name: r'cell',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherCell? cell;



  @JsonKey(
    
    name: r'smoke',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherSmoke? smoke;



  @JsonKey(
    
    name: r'forecast',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherForecast? forecast;



  @JsonKey(
    
    name: r'automated',
    required: true,
    includeIfNull: false,
  )


  final bool automated;



  @JsonKey(
    
    name: r'notAWarning',
    required: true,
    includeIfNull: false,
  )


  final bool notAWarning;



  @JsonKey(
    
    name: r'marine',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherMarine? marine;



  @JsonKey(
    
    name: r'aurora',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherAurora? aurora;



  @JsonKey(
    
    name: r'heat',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherHeat? heat;



  @JsonKey(
    
    name: r'uv',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherUv? uv;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeather &&
      other.place == place &&
      other.radiusKm == radiusKm &&
      other.radar == radar &&
      other.hail == hail &&
      other.precip == precip &&
      other.lightning == lightning &&
      other.cell == cell &&
      other.smoke == smoke &&
      other.forecast == forecast &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.marine == marine &&
      other.aurora == aurora &&
      other.heat == heat &&
      other.uv == uv;

    @override
    int get hashCode =>
        place.hashCode +
        radiusKm.hashCode +
        radar.hashCode +
        hail.hashCode +
        precip.hashCode +
        lightning.hashCode +
        cell.hashCode +
        smoke.hashCode +
        forecast.hashCode +
        automated.hashCode +
        notAWarning.hashCode +
        (marine == null ? 0 : marine.hashCode) +
        (aurora == null ? 0 : aurora.hashCode) +
        (heat == null ? 0 : heat.hashCode) +
        (uv == null ? 0 : uv.hashCode);

  factory PlaceWeather.fromJson(Map<String, dynamic> json) => _$PlaceWeatherFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

