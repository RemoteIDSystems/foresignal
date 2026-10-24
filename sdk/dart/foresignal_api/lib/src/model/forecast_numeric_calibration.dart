//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/forecast_numeric_calibration_summary.dart';
import 'package:foresignal_api/src/model/forecast_numeric_calibration_quantile_reliability_inner.dart';
import 'package:foresignal_api/src/model/forecast_numeric_calibration_pit.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_numeric_calibration.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastNumericCalibration {
  /// Returns a new [ForecastNumericCalibration] instance.
  ForecastNumericCalibration({

     this.forecasterId,

     this.category,

    required  this.type,

    required  this.n,

    required  this.label,

    required  this.quantileReliability,

    required  this.pit,

     this.summary,

    required  this.scorerVersion,

    required  this.computedAt,
  });

  @JsonKey(
    
    name: r'forecasterId',
    required: false,
    includeIfNull: false,
  )


  final String? forecasterId;



  @JsonKey(
    
    name: r'category',
    required: false,
    includeIfNull: false,
  )


  final String? category;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ForecastNumericCalibrationTypeEnum type;



      /// Resolved numeric questions with quantile coverage
  @JsonKey(
    
    name: r'n',
    required: true,
    includeIfNull: false,
  )


  final int n;



  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final ForecastNumericCalibrationLabelEnum label;



      /// Empirical coverage P(y ≤ q) of each stated quantile vs its nominal level
  @JsonKey(
    
    name: r'quantileReliability',
    required: true,
    includeIfNull: false,
  )


  final List<ForecastNumericCalibrationQuantileReliabilityInner> quantileReliability;



  @JsonKey(
    
    name: r'pit',
    required: true,
    includeIfNull: false,
  )


  final ForecastNumericCalibrationPit pit;



  @JsonKey(
    
    name: r'summary',
    required: false,
    includeIfNull: false,
  )


  final ForecastNumericCalibrationSummary? summary;



  @JsonKey(
    
    name: r'scorerVersion',
    required: true,
    includeIfNull: false,
  )


  final String scorerVersion;



  @JsonKey(
    
    name: r'computedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime computedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastNumericCalibration &&
      other.forecasterId == forecasterId &&
      other.category == category &&
      other.type == type &&
      other.n == n &&
      other.label == label &&
      other.quantileReliability == quantileReliability &&
      other.pit == pit &&
      other.summary == summary &&
      other.scorerVersion == scorerVersion &&
      other.computedAt == computedAt;

    @override
    int get hashCode =>
        (forecasterId == null ? 0 : forecasterId.hashCode) +
        (category == null ? 0 : category.hashCode) +
        type.hashCode +
        n.hashCode +
        label.hashCode +
        quantileReliability.hashCode +
        pit.hashCode +
        summary.hashCode +
        scorerVersion.hashCode +
        computedAt.hashCode;

  factory ForecastNumericCalibration.fromJson(Map<String, dynamic> json) => _$ForecastNumericCalibrationFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastNumericCalibrationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ForecastNumericCalibrationTypeEnum {
@JsonValue(r'numeric')
numeric(r'numeric');

const ForecastNumericCalibrationTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ForecastNumericCalibrationLabelEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient')
insufficient(r'insufficient');

const ForecastNumericCalibrationLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


