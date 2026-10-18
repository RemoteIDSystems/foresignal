//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/place_weather_digest_now_lightning.dart';
import 'package:foresignal_api/src/model/place_weather_digest_now_alerts_inner.dart';
import 'package:foresignal_api/src/model/place_weather_digest_now_radar.dart';
import 'package:foresignal_api/src/model/place_weather_digest_now_alerts_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_digest_now.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherDigestNow {
  /// Returns a new [PlaceWeatherDigestNow] instance.
  PlaceWeatherDigestNow({

     this.radar,

     this.lightning,

     this.alerts,

     this.alertsStatus,
  });

  @JsonKey(
    
    name: r'radar',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherDigestNowRadar? radar;



  @JsonKey(
    
    name: r'lightning',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherDigestNowLightning? lightning;



  @JsonKey(
    
    name: r'alerts',
    required: false,
    includeIfNull: false,
  )


  final List<PlaceWeatherDigestNowAlertsInner>? alerts;



  @JsonKey(
    
    name: r'alertsStatus',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherDigestNowAlertsStatus? alertsStatus;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherDigestNow &&
      other.radar == radar &&
      other.lightning == lightning &&
      other.alerts == alerts &&
      other.alertsStatus == alertsStatus;

    @override
    int get hashCode =>
        radar.hashCode +
        lightning.hashCode +
        alerts.hashCode +
        alertsStatus.hashCode;

  factory PlaceWeatherDigestNow.fromJson(Map<String, dynamic> json) => _$PlaceWeatherDigestNowFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherDigestNowToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

