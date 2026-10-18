//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/icd_term.dart';
import 'package:foresignal_api/src/model/confidence_level.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_probability_state.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisProbabilityState {
  /// Returns a new [HypothesisProbabilityState] instance.
  HypothesisProbabilityState({

    required  this.hypothesisId,

     this.label,

    required  this.probability,

    required  this.estimativeTerm,

     this.confidence,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'hypothesisId',
    required: true,
    includeIfNull: false,
  )


  final String hypothesisId;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probability',
    required: true,
    includeIfNull: false,
  )


  final num probability;



  @JsonKey(
    
    name: r'estimativeTerm',
    required: true,
    includeIfNull: false,
  )


  final IcdTerm estimativeTerm;



  @JsonKey(
    
    name: r'confidence',
    required: false,
    includeIfNull: false,
  )


  final ConfidenceLevel? confidence;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisProbabilityState &&
      other.hypothesisId == hypothesisId &&
      other.label == label &&
      other.probability == probability &&
      other.estimativeTerm == estimativeTerm &&
      other.confidence == confidence;

    @override
    int get hashCode =>
        hypothesisId.hashCode +
        label.hashCode +
        probability.hashCode +
        estimativeTerm.hashCode +
        confidence.hashCode;

  factory HypothesisProbabilityState.fromJson(Map<String, dynamic> json) => _$HypothesisProbabilityStateFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisProbabilityStateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

