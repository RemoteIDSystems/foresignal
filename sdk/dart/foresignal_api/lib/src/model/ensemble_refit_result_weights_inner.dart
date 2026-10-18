//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ensemble_refit_result_weights_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EnsembleRefitResultWeightsInner {
  /// Returns a new [EnsembleRefitResultWeightsInner] instance.
  EnsembleRefitResultWeightsInner({

    required  this.forecasterId,

    required  this.category,

    required  this.weight,

     this.n,
  });

  @JsonKey(
    
    name: r'forecasterId',
    required: true,
    includeIfNull: false,
  )


  final String forecasterId;



  @JsonKey(
    
    name: r'category',
    required: true,
    includeIfNull: false,
  )


  final String category;



  @JsonKey(
    
    name: r'weight',
    required: true,
    includeIfNull: false,
  )


  final num weight;



  @JsonKey(
    
    name: r'n',
    required: false,
    includeIfNull: false,
  )


  final int? n;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EnsembleRefitResultWeightsInner &&
      other.forecasterId == forecasterId &&
      other.category == category &&
      other.weight == weight &&
      other.n == n;

    @override
    int get hashCode =>
        forecasterId.hashCode +
        category.hashCode +
        weight.hashCode +
        n.hashCode;

  factory EnsembleRefitResultWeightsInner.fromJson(Map<String, dynamic> json) => _$EnsembleRefitResultWeightsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$EnsembleRefitResultWeightsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

