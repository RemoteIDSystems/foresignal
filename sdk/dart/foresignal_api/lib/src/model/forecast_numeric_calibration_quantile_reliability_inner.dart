//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_numeric_calibration_quantile_reliability_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastNumericCalibrationQuantileReliabilityInner {
  /// Returns a new [ForecastNumericCalibrationQuantileReliabilityInner] instance.
  ForecastNumericCalibrationQuantileReliabilityInner({

     this.level,

     this.quantile,

     this.nominal,

     this.empirical,

     this.n,
  });

  @JsonKey(
    
    name: r'level',
    required: false,
    includeIfNull: false,
  )


  final num? level;



  @JsonKey(
    
    name: r'quantile',
    required: false,
    includeIfNull: false,
  )


  final String? quantile;



  @JsonKey(
    
    name: r'nominal',
    required: false,
    includeIfNull: false,
  )


  final num? nominal;



  @JsonKey(
    
    name: r'empirical',
    required: false,
    includeIfNull: false,
  )


  final num? empirical;



  @JsonKey(
    
    name: r'n',
    required: false,
    includeIfNull: false,
  )


  final int? n;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastNumericCalibrationQuantileReliabilityInner &&
      other.level == level &&
      other.quantile == quantile &&
      other.nominal == nominal &&
      other.empirical == empirical &&
      other.n == n;

    @override
    int get hashCode =>
        level.hashCode +
        quantile.hashCode +
        nominal.hashCode +
        empirical.hashCode +
        n.hashCode;

  factory ForecastNumericCalibrationQuantileReliabilityInner.fromJson(Map<String, dynamic> json) => _$ForecastNumericCalibrationQuantileReliabilityInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastNumericCalibrationQuantileReliabilityInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

