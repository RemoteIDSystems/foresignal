//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_context_lightning.dart';
import 'package:foresignal_api/src/model/weather_context_warnings.dart';
import 'package:foresignal_api/src/model/weather_context_wind.dart';
import 'package:foresignal_api/src/model/weather_context_precip.dart';
import 'package:foresignal_api/src/model/weather_context_smoke.dart';
import 'package:foresignal_api/src/model/weather_context_radar.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'weather_context.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WeatherContext {
  /// Returns a new [WeatherContext] instance.
  WeatherContext({

    required  this.status,

     this.at,

     this.computedAt,

     this.radar,

     this.lightning,

     this.precip,

     this.warnings,

     this.wind,

     this.smoke,

     this.summary,
  });

      /// unavailable when the computation failed or timed out (sections omitted)
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final WeatherContextStatusEnum status;



      /// Time computed for (the alert's observed_at)
  @JsonKey(
    
    name: r'at',
    required: false,
    includeIfNull: false,
  )


  final DateTime? at;



  @JsonKey(
    
    name: r'computedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? computedAt;



  @JsonKey(
    
    name: r'radar',
    required: false,
    includeIfNull: false,
  )


  final WeatherContextRadar? radar;



  @JsonKey(
    
    name: r'lightning',
    required: false,
    includeIfNull: false,
  )


  final WeatherContextLightning? lightning;



  @JsonKey(
    
    name: r'precip',
    required: false,
    includeIfNull: false,
  )


  final WeatherContextPrecip? precip;



  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final WeatherContextWarnings? warnings;



  @JsonKey(
    
    name: r'wind',
    required: false,
    includeIfNull: false,
  )


  final WeatherContextWind? wind;



  @JsonKey(
    
    name: r'smoke',
    required: false,
    includeIfNull: false,
  )


  final WeatherContextSmoke? smoke;



      /// One deterministic sentence; non-ok sections are worded as unknown, never as absence
  @JsonKey(
    
    name: r'summary',
    required: false,
    includeIfNull: false,
  )


  final String? summary;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WeatherContext &&
      other.status == status &&
      other.at == at &&
      other.computedAt == computedAt &&
      other.radar == radar &&
      other.lightning == lightning &&
      other.precip == precip &&
      other.warnings == warnings &&
      other.wind == wind &&
      other.smoke == smoke &&
      other.summary == summary;

    @override
    int get hashCode =>
        status.hashCode +
        at.hashCode +
        computedAt.hashCode +
        radar.hashCode +
        lightning.hashCode +
        precip.hashCode +
        warnings.hashCode +
        wind.hashCode +
        smoke.hashCode +
        summary.hashCode;

  factory WeatherContext.fromJson(Map<String, dynamic> json) => _$WeatherContextFromJson(json);

  Map<String, dynamic> toJson() => _$WeatherContextToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// unavailable when the computation failed or timed out (sections omitted)
enum WeatherContextStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'unavailable')
unavailable(r'unavailable');

const WeatherContextStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


