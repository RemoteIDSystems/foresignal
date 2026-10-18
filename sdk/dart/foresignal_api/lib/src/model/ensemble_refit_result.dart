//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ensemble_refit_result_platt_inner.dart';
import 'package:foresignal_api/src/model/ensemble_refit_result_extremizing.dart';
import 'package:foresignal_api/src/model/ensemble_refit_result_weights_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ensemble_refit_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EnsembleRefitResult {
  /// Returns a new [EnsembleRefitResult] instance.
  EnsembleRefitResult({

    required  this.fittedAt,

    required  this.extremizing,

    required  this.platt,

    required  this.weights,
  });

  @JsonKey(
    
    name: r'fittedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime fittedAt;



  @JsonKey(
    
    name: r'extremizing',
    required: true,
    includeIfNull: false,
  )


  final EnsembleRefitResultExtremizing extremizing;



  @JsonKey(
    
    name: r'platt',
    required: true,
    includeIfNull: false,
  )


  final List<EnsembleRefitResultPlattInner> platt;



  @JsonKey(
    
    name: r'weights',
    required: true,
    includeIfNull: false,
  )


  final List<EnsembleRefitResultWeightsInner> weights;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EnsembleRefitResult &&
      other.fittedAt == fittedAt &&
      other.extremizing == extremizing &&
      other.platt == platt &&
      other.weights == weights;

    @override
    int get hashCode =>
        fittedAt.hashCode +
        extremizing.hashCode +
        platt.hashCode +
        weights.hashCode;

  factory EnsembleRefitResult.fromJson(Map<String, dynamic> json) => _$EnsembleRefitResultFromJson(json);

  Map<String, dynamic> toJson() => _$EnsembleRefitResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

