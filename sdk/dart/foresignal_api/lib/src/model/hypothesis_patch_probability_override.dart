//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_patch_probability_override.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisPatchProbabilityOverride {
  /// Returns a new [HypothesisPatchProbabilityOverride] instance.
  HypothesisPatchProbabilityOverride({

    required  this.probability,

    required  this.rationale,

     this.exceedCap = false,
  });

          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probability',
    required: true,
    includeIfNull: false,
  )


  final num probability;



  @JsonKey(
    
    name: r'rationale',
    required: true,
    includeIfNull: false,
  )


  final String rationale;



      /// Allow a speculative link above its cap (logged and labelled)
  @JsonKey(
    defaultValue: false,
    name: r'exceedCap',
    required: false,
    includeIfNull: false,
  )


  final bool? exceedCap;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisPatchProbabilityOverride &&
      other.probability == probability &&
      other.rationale == rationale &&
      other.exceedCap == exceedCap;

    @override
    int get hashCode =>
        probability.hashCode +
        rationale.hashCode +
        exceedCap.hashCode;

  factory HypothesisPatchProbabilityOverride.fromJson(Map<String, dynamic> json) => _$HypothesisPatchProbabilityOverrideFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisPatchProbabilityOverrideToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

