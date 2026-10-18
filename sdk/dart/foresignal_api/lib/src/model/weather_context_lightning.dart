//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'weather_context_lightning.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WeatherContextLightning {
  /// Returns a new [WeatherContextLightning] instance.
  WeatherContextLightning({

    required  this.status,

     this.asOf,

     this.statusText,

     this.flashes20km15m,
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



      /// GLM flashes within 20 km over the last 15 min
  @JsonKey(
    
    name: r'flashes20km15m',
    required: false,
    includeIfNull: false,
  )


  final int? flashes20km15m;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WeatherContextLightning &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText &&
      other.flashes20km15m == flashes20km15m;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode +
        flashes20km15m.hashCode;

  factory WeatherContextLightning.fromJson(Map<String, dynamic> json) => _$WeatherContextLightningFromJson(json);

  Map<String, dynamic> toJson() => _$WeatherContextLightningToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

