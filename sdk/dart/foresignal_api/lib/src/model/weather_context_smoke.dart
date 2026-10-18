//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'weather_context_smoke.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WeatherContextSmoke {
  /// Returns a new [WeatherContextSmoke] instance.
  WeatherContextSmoke({

    required  this.status,

     this.asOf,

     this.statusText,

     this.pm25UgM3,

     this.run,

     this.validTime,

     this.step,
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
    
    name: r'pm25UgM3',
    required: false,
    includeIfNull: false,
  )


  final num? pm25UgM3;



  @JsonKey(
    
    name: r'run',
    required: false,
    includeIfNull: false,
  )


  final String? run;



  @JsonKey(
    
    name: r'validTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validTime;



  @JsonKey(
    
    name: r'step',
    required: false,
    includeIfNull: false,
  )


  final int? step;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WeatherContextSmoke &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText &&
      other.pm25UgM3 == pm25UgM3 &&
      other.run == run &&
      other.validTime == validTime &&
      other.step == step;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode +
        pm25UgM3.hashCode +
        run.hashCode +
        validTime.hashCode +
        step.hashCode;

  factory WeatherContextSmoke.fromJson(Map<String, dynamic> json) => _$WeatherContextSmokeFromJson(json);

  Map<String, dynamic> toJson() => _$WeatherContextSmokeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

