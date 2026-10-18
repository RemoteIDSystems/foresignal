//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ensemble_refit_result_extremizing.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EnsembleRefitResultExtremizing {
  /// Returns a new [EnsembleRefitResultExtremizing] instance.
  EnsembleRefitResultExtremizing({

    required  this.a,

    required  this.enabled,

    required  this.nResolutions,

    required  this.distinctForecasterTypes,

     this.logScoreImprovement,
  });

  @JsonKey(
    
    name: r'a',
    required: true,
    includeIfNull: false,
  )


  final num a;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'nResolutions',
    required: true,
    includeIfNull: false,
  )


  final int nResolutions;



  @JsonKey(
    
    name: r'distinctForecasterTypes',
    required: true,
    includeIfNull: false,
  )


  final int distinctForecasterTypes;



      /// Out-of-sample log-score improvement; enabled only when positive over ≥ 100 resolutions
  @JsonKey(
    
    name: r'logScoreImprovement',
    required: false,
    includeIfNull: false,
  )


  final num? logScoreImprovement;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EnsembleRefitResultExtremizing &&
      other.a == a &&
      other.enabled == enabled &&
      other.nResolutions == nResolutions &&
      other.distinctForecasterTypes == distinctForecasterTypes &&
      other.logScoreImprovement == logScoreImprovement;

    @override
    int get hashCode =>
        a.hashCode +
        enabled.hashCode +
        nResolutions.hashCode +
        distinctForecasterTypes.hashCode +
        (logScoreImprovement == null ? 0 : logScoreImprovement.hashCode);

  factory EnsembleRefitResultExtremizing.fromJson(Map<String, dynamic> json) => _$EnsembleRefitResultExtremizingFromJson(json);

  Map<String, dynamic> toJson() => _$EnsembleRefitResultExtremizingToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

