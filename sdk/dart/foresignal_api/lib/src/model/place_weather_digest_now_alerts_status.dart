//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_digest_now_alerts_status.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherDigestNowAlertsStatus {
  /// Returns a new [PlaceWeatherDigestNowAlertsStatus] instance.
  PlaceWeatherDigestNowAlertsStatus({

    required  this.status,

     this.asOf,

     this.statusText,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final WeatherSectionStatus status;



  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



  @JsonKey(
    
    name: r'statusText',
    required: false,
    includeIfNull: false,
  )


  final String? statusText;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherDigestNowAlertsStatus &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode;

  factory PlaceWeatherDigestNowAlertsStatus.fromJson(Map<String, dynamic> json) => _$PlaceWeatherDigestNowAlertsStatusFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherDigestNowAlertsStatusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

