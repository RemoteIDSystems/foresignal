//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_evaluation_skipped.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastEvaluationSkipped {
  /// Returns a new [ForecastEvaluationSkipped] instance.
  ForecastEvaluationSkipped({

    required  this.leakageRisk,

     this.other,
  });

      /// Opened before the model's knowledgeCutoff (`LEAKAGE_RISK`)
  @JsonKey(
    
    name: r'leakageRisk',
    required: true,
    includeIfNull: false,
  )


  final int leakageRisk;



  @JsonKey(
    
    name: r'other',
    required: false,
    includeIfNull: false,
  )


  final int? other;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastEvaluationSkipped &&
      other.leakageRisk == leakageRisk &&
      other.other == other;

    @override
    int get hashCode =>
        leakageRisk.hashCode +
        other.hashCode;

  factory ForecastEvaluationSkipped.fromJson(Map<String, dynamic> json) => _$ForecastEvaluationSkippedFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastEvaluationSkippedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

