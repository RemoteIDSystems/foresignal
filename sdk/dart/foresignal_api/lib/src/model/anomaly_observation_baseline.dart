//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'anomaly_observation_baseline.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnomalyObservationBaseline {
  /// Returns a new [AnomalyObservationBaseline] instance.
  AnomalyObservationBaseline({

    required  this.weeks,

    required  this.slotMode,

    required  this.mean,

    required  this.variance,

    required  this.model,
  });

  @JsonKey(
    
    name: r'weeks',
    required: true,
    includeIfNull: false,
  )


  final num weeks;



  @JsonKey(
    
    name: r'slotMode',
    required: true,
    includeIfNull: false,
  )


  final AnomalyObservationBaselineSlotModeEnum slotMode;



  @JsonKey(
    
    name: r'mean',
    required: true,
    includeIfNull: false,
  )


  final num mean;



  @JsonKey(
    
    name: r'variance',
    required: true,
    includeIfNull: false,
  )


  final num variance;



  @JsonKey(
    
    name: r'model',
    required: true,
    includeIfNull: false,
  )


  final AnomalyObservationBaselineModelEnum model;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnomalyObservationBaseline &&
      other.weeks == weeks &&
      other.slotMode == slotMode &&
      other.mean == mean &&
      other.variance == variance &&
      other.model == model;

    @override
    int get hashCode =>
        weeks.hashCode +
        slotMode.hashCode +
        mean.hashCode +
        variance.hashCode +
        model.hashCode;

  factory AnomalyObservationBaseline.fromJson(Map<String, dynamic> json) => _$AnomalyObservationBaselineFromJson(json);

  Map<String, dynamic> toJson() => _$AnomalyObservationBaselineToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnomalyObservationBaselineSlotModeEnum {
@JsonValue(r'hour-of-week')
hourOfWeek(r'hour-of-week'),
@JsonValue(r'hour-of-day')
hourOfDay(r'hour-of-day');

const AnomalyObservationBaselineSlotModeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnomalyObservationBaselineModelEnum {
@JsonValue(r'poisson')
poisson(r'poisson'),
@JsonValue(r'nb')
nb(r'nb');

const AnomalyObservationBaselineModelEnum(this.value);

final String value;

@override
String toString() => value;
}


