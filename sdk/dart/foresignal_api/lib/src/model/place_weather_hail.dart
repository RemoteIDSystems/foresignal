//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_hail.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherHail {
  /// Returns a new [PlaceWeatherHail] instance.
  PlaceWeatherHail({

    required  this.status,

     this.asOf,

     this.statusText,

     this.meshMaxMmInRadius,

     this.meshNearest25Km,
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
    
    name: r'meshMaxMmInRadius',
    required: false,
    includeIfNull: false,
  )


  final num? meshMaxMmInRadius;



      /// km (999 = none within 100 km)
  @JsonKey(
    
    name: r'meshNearest25Km',
    required: false,
    includeIfNull: false,
  )


  final num? meshNearest25Km;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherHail &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText &&
      other.meshMaxMmInRadius == meshMaxMmInRadius &&
      other.meshNearest25Km == meshNearest25Km;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode +
        meshMaxMmInRadius.hashCode +
        meshNearest25Km.hashCode;

  factory PlaceWeatherHail.fromJson(Map<String, dynamic> json) => _$PlaceWeatherHailFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherHailToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

