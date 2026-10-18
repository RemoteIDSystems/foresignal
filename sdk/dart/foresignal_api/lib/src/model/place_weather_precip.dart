//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_precip.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherPrecip {
  /// Returns a new [PlaceWeatherPrecip] instance.
  PlaceWeatherPrecip({

    required  this.status,

     this.asOf,

     this.statusText,

     this.rateMmH,

     this.qpe1hMmMax,

     this.qpe24hMmMax,

     this.ffgRatio1hPctMax,
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



      /// 1 mm/h steps
  @JsonKey(
    
    name: r'rateMmH',
    required: false,
    includeIfNull: false,
  )


  final num? rateMmH;



      /// 053
  @JsonKey(
    
    name: r'qpe1hMmMax',
    required: false,
    includeIfNull: false,
  )


  final num? qpe1hMmMax;



      /// 053
  @JsonKey(
    
    name: r'qpe24hMmMax',
    required: false,
    includeIfNull: false,
  )


  final num? qpe24hMmMax;



      /// 053; ≥ 100 means rainfall exceeds flash-flood guidance
  @JsonKey(
    
    name: r'ffgRatio1hPctMax',
    required: false,
    includeIfNull: false,
  )


  final num? ffgRatio1hPctMax;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherPrecip &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText &&
      other.rateMmH == rateMmH &&
      other.qpe1hMmMax == qpe1hMmMax &&
      other.qpe24hMmMax == qpe24hMmMax &&
      other.ffgRatio1hPctMax == ffgRatio1hPctMax;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode +
        rateMmH.hashCode +
        qpe1hMmMax.hashCode +
        qpe24hMmMax.hashCode +
        ffgRatio1hPctMax.hashCode;

  factory PlaceWeatherPrecip.fromJson(Map<String, dynamic> json) => _$PlaceWeatherPrecipFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherPrecipToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

