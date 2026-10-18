//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_smoke.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherSmoke {
  /// Returns a new [PlaceWeatherSmoke] instance.
  PlaceWeatherSmoke({

    required  this.status,

     this.asOf,

     this.statusText,

     this.hrrrPm25Max24h,
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



      /// µg m⁻³
  @JsonKey(
    
    name: r'hrrrPm25Max24h',
    required: false,
    includeIfNull: false,
  )


  final num? hrrrPm25Max24h;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherSmoke &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText &&
      other.hrrrPm25Max24h == hrrrPm25Max24h;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode +
        hrrrPm25Max24h.hashCode;

  factory PlaceWeatherSmoke.fromJson(Map<String, dynamic> json) => _$PlaceWeatherSmokeFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherSmokeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

