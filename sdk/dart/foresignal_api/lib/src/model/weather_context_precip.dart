//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'weather_context_precip.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WeatherContextPrecip {
  /// Returns a new [WeatherContextPrecip] instance.
  WeatherContextPrecip({

    required  this.status,

     this.asOf,

     this.statusText,

     this.rateMmH,

     this.qpe1hMm,
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
    
    name: r'rateMmH',
    required: false,
    includeIfNull: false,
  )


  final num? rateMmH;



  @JsonKey(
    
    name: r'qpe1hMm',
    required: false,
    includeIfNull: false,
  )


  final num? qpe1hMm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WeatherContextPrecip &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText &&
      other.rateMmH == rateMmH &&
      other.qpe1hMm == qpe1hMm;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode +
        rateMmH.hashCode +
        (qpe1hMm == null ? 0 : qpe1hMm.hashCode);

  factory WeatherContextPrecip.fromJson(Map<String, dynamic> json) => _$WeatherContextPrecipFromJson(json);

  Map<String, dynamic> toJson() => _$WeatherContextPrecipToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

