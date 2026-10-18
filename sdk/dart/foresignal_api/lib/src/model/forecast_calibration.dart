//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/forecast_calibration_bins_inner.dart';
import 'package:foresignal_api/src/model/forecast_calibration_decomposition.dart';
import 'package:foresignal_api/src/model/forecast_calibration_curve_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_calibration.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastCalibration {
  /// Returns a new [ForecastCalibration] instance.
  ForecastCalibration({

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


  final ForecastCalibrationLabelEnum label;



      /// `corp`, or `bins` when n < 50
  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final ForecastCalibrationMethodEnum method;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastCalibration &&
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
      other.computedAt == computedAt;

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
        computedAt.hashCode;

  factory ForecastCalibration.fromJson(Map<String, dynamic> json) => _$ForecastCalibrationFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastCalibrationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ForecastCalibrationLabelEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient')
insufficient(r'insufficient');

const ForecastCalibrationLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `corp`, or `bins` when n < 50
enum ForecastCalibrationMethodEnum {
@JsonValue(r'corp')
corp(r'corp'),
@JsonValue(r'bins')
bins(r'bins');

const ForecastCalibrationMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


