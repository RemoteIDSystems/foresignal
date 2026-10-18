//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/forecast_area_payload_threshold.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_area_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastAreaPayload {
  /// Returns a new [ForecastAreaPayload] instance.
  ForecastAreaPayload({

    required  this.model,

    required  this.field,

    required  this.threshold,

    required  this.firstExceedAt,

     this.peakValue,

     this.peakAt,

     this.areaKm2,

    required  this.run,

     this.attribution,

     this.automated,

     this.notAWarning,
  });

  @JsonKey(
    
    name: r'model',
    required: true,
    includeIfNull: false,
  )


  final ForecastAreaPayloadModelEnum model;



  @JsonKey(
    
    name: r'field',
    required: true,
    includeIfNull: false,
  )


  final String field;



  @JsonKey(
    
    name: r'threshold',
    required: true,
    includeIfNull: false,
  )


  final ForecastAreaPayloadThreshold threshold;



  @JsonKey(
    
    name: r'firstExceedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime firstExceedAt;



  @JsonKey(
    
    name: r'peakValue',
    required: false,
    includeIfNull: false,
  )


  final num? peakValue;



  @JsonKey(
    
    name: r'peakAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? peakAt;



  @JsonKey(
    
    name: r'areaKm2',
    required: false,
    includeIfNull: false,
  )


  final num? areaKm2;



  @JsonKey(
    
    name: r'run',
    required: true,
    includeIfNull: false,
  )


  final DateTime run;



      /// ECMWF CC BY 4.0 attribution with the changes-made line; present on IFS- and AIFS-derived areas. `run` is volatile: a newer run with the same area writes no version
  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final String? attribution;



  @JsonKey(
    
    name: r'automated',
    required: false,
    includeIfNull: false,
  )


  final bool? automated;



  @JsonKey(
    
    name: r'notAWarning',
    required: false,
    includeIfNull: false,
  )


  final bool? notAWarning;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastAreaPayload &&
      other.model == model &&
      other.field == field &&
      other.threshold == threshold &&
      other.firstExceedAt == firstExceedAt &&
      other.peakValue == peakValue &&
      other.peakAt == peakAt &&
      other.areaKm2 == areaKm2 &&
      other.run == run &&
      other.attribution == attribution &&
      other.automated == automated &&
      other.notAWarning == notAWarning;

    @override
    int get hashCode =>
        model.hashCode +
        field.hashCode +
        threshold.hashCode +
        firstExceedAt.hashCode +
        peakValue.hashCode +
        peakAt.hashCode +
        areaKm2.hashCode +
        run.hashCode +
        attribution.hashCode +
        automated.hashCode +
        notAWarning.hashCode;

  factory ForecastAreaPayload.fromJson(Map<String, dynamic> json) => _$ForecastAreaPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastAreaPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ForecastAreaPayloadModelEnum {
@JsonValue(r'gfs')
gfs(r'gfs'),
@JsonValue(r'hrrr')
hrrr(r'hrrr'),
@JsonValue(r'ifs')
ifs(r'ifs'),
@JsonValue(r'aifs')
aifs(r'aifs');

const ForecastAreaPayloadModelEnum(this.value);

final String value;

@override
String toString() => value;
}


