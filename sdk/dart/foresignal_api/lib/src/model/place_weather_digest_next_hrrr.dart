//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_digest_next_hrrr.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherDigestNextHrrr {
  /// Returns a new [PlaceWeatherDigestNextHrrr] instance.
  PlaceWeatherDigestNextHrrr({

    required  this.status,

     this.asOf,

     this.statusText,

     this.run,

     this.gustMaxMs,

     this.precipMm,

     this.smokeMaxUgM3,

     this.validTime,
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
    
    name: r'run',
    required: false,
    includeIfNull: false,
  )


  final String? run;



  @JsonKey(
    
    name: r'gustMaxMs',
    required: false,
    includeIfNull: false,
  )


  final num? gustMaxMs;



  @JsonKey(
    
    name: r'precipMm',
    required: false,
    includeIfNull: false,
  )


  final num? precipMm;



  @JsonKey(
    
    name: r'smokeMaxUgM3',
    required: false,
    includeIfNull: false,
  )


  final num? smokeMaxUgM3;



  @JsonKey(
    
    name: r'validTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validTime;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherDigestNextHrrr &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText &&
      other.run == run &&
      other.gustMaxMs == gustMaxMs &&
      other.precipMm == precipMm &&
      other.smokeMaxUgM3 == smokeMaxUgM3 &&
      other.validTime == validTime;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode +
        run.hashCode +
        gustMaxMs.hashCode +
        precipMm.hashCode +
        smokeMaxUgM3.hashCode +
        validTime.hashCode;

  factory PlaceWeatherDigestNextHrrr.fromJson(Map<String, dynamic> json) => _$PlaceWeatherDigestNextHrrrFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherDigestNextHrrrToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

