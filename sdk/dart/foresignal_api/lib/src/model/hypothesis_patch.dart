//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/hypothesis_patch_probability_override.dart';
import 'package:foresignal_api/src/model/hypothesis_input_prior_basis.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisPatch {
  /// Returns a new [HypothesisPatch] instance.
  HypothesisPatch({

     this.label,

     this.statement,

     this.priorBasis,

     this.ledgerOutcome,

     this.alternatives,

     this.probabilityOverride,

     this.version,
  });

  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;



  @JsonKey(
    
    name: r'statement',
    required: false,
    includeIfNull: false,
  )


  final String? statement;



  @JsonKey(
    
    name: r'priorBasis',
    required: false,
    includeIfNull: false,
  )


  final HypothesisInputPriorBasis? priorBasis;



  @JsonKey(
    
    name: r'ledgerOutcome',
    required: false,
    includeIfNull: false,
  )


  final String? ledgerOutcome;



  @JsonKey(
    
    name: r'alternatives',
    required: false,
    includeIfNull: false,
  )


  final List<String>? alternatives;



  @JsonKey(
    
    name: r'probabilityOverride',
    required: false,
    includeIfNull: false,
  )


  final HypothesisPatchProbabilityOverride? probabilityOverride;



      /// Optimistic lock (409 on mismatch)
  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final int? version;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisPatch &&
      other.label == label &&
      other.statement == statement &&
      other.priorBasis == priorBasis &&
      other.ledgerOutcome == ledgerOutcome &&
      other.alternatives == alternatives &&
      other.probabilityOverride == probabilityOverride &&
      other.version == version;

    @override
    int get hashCode =>
        label.hashCode +
        statement.hashCode +
        priorBasis.hashCode +
        (ledgerOutcome == null ? 0 : ledgerOutcome.hashCode) +
        alternatives.hashCode +
        (probabilityOverride == null ? 0 : probabilityOverride.hashCode) +
        version.hashCode;

  factory HypothesisPatch.fromJson(Map<String, dynamic> json) => _$HypothesisPatchFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

