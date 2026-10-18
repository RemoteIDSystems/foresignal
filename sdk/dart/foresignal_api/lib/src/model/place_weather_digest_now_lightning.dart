//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_digest_now_lightning.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherDigestNowLightning {
  /// Returns a new [PlaceWeatherDigestNowLightning] instance.
  PlaceWeatherDigestNowLightning({

    required  this.status,

     this.asOf,

     this.statusText,

     this.nearestKm15m,

     this.count15km15m,
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
    
    name: r'nearestKm15m',
    required: false,
    includeIfNull: false,
  )


  final num? nearestKm15m;



  @JsonKey(
    
    name: r'count15km15m',
    required: false,
    includeIfNull: false,
  )


  final int? count15km15m;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherDigestNowLightning &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText &&
      other.nearestKm15m == nearestKm15m &&
      other.count15km15m == count15km15m;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode +
        nearestKm15m.hashCode +
        count15km15m.hashCode;

  factory PlaceWeatherDigestNowLightning.fromJson(Map<String, dynamic> json) => _$PlaceWeatherDigestNowLightningFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherDigestNowLightningToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

