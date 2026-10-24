//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/forecast_calibration_bins_inner.dart';
import 'package:foresignal_api/src/model/forecast_calibration.dart';
import 'package:foresignal_api/src/model/forecast_calibration_decomposition.dart';
import 'package:foresignal_api/src/model/forecast_numeric_calibration_summary.dart';
import 'package:foresignal_api/src/model/forecast_numeric_calibration_quantile_reliability_inner.dart';
import 'package:foresignal_api/src/model/forecast_calibration_curve_inner.dart';
import 'package:foresignal_api/src/model/forecast_numeric_calibration.dart';
import 'package:foresignal_api/src/model/forecast_numeric_calibration_pit.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'get_forecast_calibration200_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GetForecastCalibration200Response {
  /// Returns a new [GetForecastCalibration200Response] instance.
  GetForecastCalibration200Response({

     this.forecasterId,

     this.category,

    required  this.n,

    required  this.label,

    required  this.method,

    required  this.curve,

    required  this.bins,

     this.decomposition,

     this.sharpness,

    required  this.scorerVersion,

    required  this.computedAt,

    required  this.type,

    required  this.quantileReliability,

    required  this.pit,

     this.summary,
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


  final GetForecastCalibration200ResponseLabelEnum label;



      /// `corp`, or `bins` when n < 50
  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final GetForecastCalibration200ResponseMethodEnum method;



  @JsonKey(
    
    name: r'curve',
    required: true,
    includeIfNull: false,
  )


  final List<ForecastCalibrationCurveInner> curve;



  @JsonKey(
    
    name: r'bins',
    required: true,
    includeIfNull: false,
  )


  final List<ForecastCalibrationBinsInner> bins;



  @JsonKey(
    
    name: r'decomposition',
    required: false,
    includeIfNull: false,
  )


  final ForecastCalibrationDecomposition? decomposition;



  @JsonKey(
    
    name: r'sharpness',
    required: false,
    includeIfNull: false,
  )


  final num? sharpness;



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



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final GetForecastCalibration200ResponseTypeEnum type;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is GetForecastCalibration200Response &&
      other.forecasterId == forecasterId &&
      other.category == category &&
      other.n == n &&
      other.label == label &&
      other.method == method &&
      other.curve == curve &&
      other.bins == bins &&
      other.decomposition == decomposition &&
      other.sharpness == sharpness &&
      other.scorerVersion == scorerVersion &&
      other.computedAt == computedAt &&
      other.type == type &&
      other.quantileReliability == quantileReliability &&
      other.pit == pit &&
      other.summary == summary;

    @override
    int get hashCode =>
        (forecasterId == null ? 0 : forecasterId.hashCode) +
        (category == null ? 0 : category.hashCode) +
        n.hashCode +
        label.hashCode +
        method.hashCode +
        curve.hashCode +
        bins.hashCode +
        (decomposition == null ? 0 : decomposition.hashCode) +
        (sharpness == null ? 0 : sharpness.hashCode) +
        scorerVersion.hashCode +
        computedAt.hashCode +
        type.hashCode +
        quantileReliability.hashCode +
        pit.hashCode +
        summary.hashCode;

  factory GetForecastCalibration200Response.fromJson(Map<String, dynamic> json) => _$GetForecastCalibration200ResponseFromJson(json);

  Map<String, dynamic> toJson() => _$GetForecastCalibration200ResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum GetForecastCalibration200ResponseLabelEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient')
insufficient(r'insufficient');

const GetForecastCalibration200ResponseLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `corp`, or `bins` when n < 50
enum GetForecastCalibration200ResponseMethodEnum {
@JsonValue(r'corp')
corp(r'corp'),
@JsonValue(r'bins')
bins(r'bins');

const GetForecastCalibration200ResponseMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


enum GetForecastCalibration200ResponseTypeEnum {
@JsonValue(r'numeric')
numeric(r'numeric');

const GetForecastCalibration200ResponseTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


