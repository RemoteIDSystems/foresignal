//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/alert_context_anchor.dart';
import 'package:foresignal_api/src/model/anomaly_observation.dart';
import 'package:foresignal_api/src/model/weather_context.dart';
import 'package:foresignal_api/src/model/alert_context_near.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'alert_context.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AlertContext {
  /// Returns a new [AlertContext] instance.
  AlertContext({

     this.weather,

     this.near,

     this.anomaly,

     this.anchor,
  });

  @JsonKey(
    
    name: r'weather',
    required: false,
    includeIfNull: false,
  )


  final WeatherContext? weather;



  @JsonKey(
    
    name: r'near',
    required: false,
    includeIfNull: false,
  )


  final AlertContextNear? near;



      /// Anomaly rule alerts: the evaluated bucket (100)
  @JsonKey(
    
    name: r'anomaly',
    required: false,
    includeIfNull: false,
  )


  final AnomalyObservation? anomaly;



  @JsonKey(
    
    name: r'anchor',
    required: false,
    includeIfNull: false,
  )


  final AlertContextAnchor? anchor;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AlertContext &&
      other.weather == weather &&
      other.near == near &&
      other.anomaly == anomaly &&
      other.anchor == anchor;

    @override
    int get hashCode =>
        weather.hashCode +
        near.hashCode +
        (anomaly == null ? 0 : anomaly.hashCode) +
        anchor.hashCode;

  factory AlertContext.fromJson(Map<String, dynamic> json) => _$AlertContextFromJson(json);

  Map<String, dynamic> toJson() => _$AlertContextToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

