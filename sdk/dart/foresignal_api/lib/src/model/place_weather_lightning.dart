//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_lightning.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherLightning {
  /// Returns a new [PlaceWeatherLightning] instance.
  PlaceWeatherLightning({

    required  this.status,

     this.asOf,

     this.statusText,

     this.nearestKm15m,

     this.nearestKm30m,

     this.count15km15m,

     this.verifiedNearestKm30m,
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



      /// km (999 = none within 100 km)
  @JsonKey(
    
    name: r'nearestKm15m',
    required: false,
    includeIfNull: false,
  )


  final num? nearestKm15m;



      /// Rolling 30-min minimum, km (999 = none within 100 km)
  @JsonKey(
    
    name: r'nearestKm30m',
    required: false,
    includeIfNull: false,
  )


  final num? nearestKm30m;



  @JsonKey(
    
    name: r'count15km15m',
    required: false,
    includeIfNull: false,
  )


  final int? count15km15m;



      /// Equals nearestKm30m when ok, else 0
  @JsonKey(
    
    name: r'verifiedNearestKm30m',
    required: false,
    includeIfNull: false,
  )


  final num? verifiedNearestKm30m;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherLightning &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText &&
      other.nearestKm15m == nearestKm15m &&
      other.nearestKm30m == nearestKm30m &&
      other.count15km15m == count15km15m &&
      other.verifiedNearestKm30m == verifiedNearestKm30m;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode +
        nearestKm15m.hashCode +
        nearestKm30m.hashCode +
        count15km15m.hashCode +
        verifiedNearestKm30m.hashCode;

  factory PlaceWeatherLightning.fromJson(Map<String, dynamic> json) => _$PlaceWeatherLightningFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherLightningToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

