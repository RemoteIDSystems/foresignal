//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'weather_context_wind.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WeatherContextWind {
  /// Returns a new [WeatherContextWind] instance.
  WeatherContextWind({

    required  this.status,

     this.asOf,

     this.statusText,

     this.productId,

     this.speedMs,

     this.gustMs,

     this.fromDeg,

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
    
    name: r'productId',
    required: false,
    includeIfNull: false,
  )


  final String? productId;



  @JsonKey(
    
    name: r'speedMs',
    required: false,
    includeIfNull: false,
  )


  final num? speedMs;



  @JsonKey(
    
    name: r'gustMs',
    required: false,
    includeIfNull: false,
  )


  final num? gustMs;



  @JsonKey(
    
    name: r'fromDeg',
    required: false,
    includeIfNull: false,
  )


  final int? fromDeg;



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
    bool operator ==(Object other) => identical(this, other) || other is WeatherContextWind &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText &&
      other.productId == productId &&
      other.speedMs == speedMs &&
      other.gustMs == gustMs &&
      other.fromDeg == fromDeg &&
      other.run == run &&
      other.validTime == validTime &&
      other.step == step;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode +
        productId.hashCode +
        speedMs.hashCode +
        (gustMs == null ? 0 : gustMs.hashCode) +
        fromDeg.hashCode +
        run.hashCode +
        validTime.hashCode +
        step.hashCode;

  factory WeatherContextWind.fromJson(Map<String, dynamic> json) => _$WeatherContextWindFromJson(json);

  Map<String, dynamic> toJson() => _$WeatherContextWindToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

