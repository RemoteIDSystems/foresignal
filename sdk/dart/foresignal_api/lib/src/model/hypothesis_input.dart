//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/hypothesis_coincidence_baseline.dart';
import 'package:foresignal_api/src/model/hypothesis_input_prior_basis.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisInput {
  /// Returns a new [HypothesisInput] instance.
  HypothesisInput({

    required  this.label,

    required  this.statement,

    required  this.type,

    required  this.prior,

    required  this.priorBasis,

     this.ledgerOutcome,

     this.linkedFindingIds,

     this.coincidenceBaseline,

     this.alternatives,
  });

  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final String label;



  @JsonKey(
    
    name: r'statement',
    required: true,
    includeIfNull: false,
  )


  final String statement;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final HypothesisInputTypeEnum type;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'prior',
    required: true,
    includeIfNull: false,
  )


  final num prior;



  @JsonKey(
    
    name: r'priorBasis',
    required: true,
    includeIfNull: false,
  )


  final HypothesisInputPriorBasis priorBasis;



  @JsonKey(
    
    name: r'ledgerOutcome',
    required: false,
    includeIfNull: false,
  )


  final String? ledgerOutcome;



  @JsonKey(
    
    name: r'linkedFindingIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? linkedFindingIds;



  @JsonKey(
    
    name: r'coincidenceBaseline',
    required: false,
    includeIfNull: false,
  )


  final HypothesisCoincidenceBaseline? coincidenceBaseline;



      /// Ids or labels of ≥ 2 other hypotheses in the same set (labels when created together with the set)
  @JsonKey(
    
    name: r'alternatives',
    required: false,
    includeIfNull: false,
  )


  final List<String>? alternatives;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisInput &&
      other.label == label &&
      other.statement == statement &&
      other.type == type &&
      other.prior == prior &&
      other.priorBasis == priorBasis &&
      other.ledgerOutcome == ledgerOutcome &&
      other.linkedFindingIds == linkedFindingIds &&
      other.coincidenceBaseline == coincidenceBaseline &&
      other.alternatives == alternatives;

    @override
    int get hashCode =>
        label.hashCode +
        statement.hashCode +
        type.hashCode +
        prior.hashCode +
        priorBasis.hashCode +
        (ledgerOutcome == null ? 0 : ledgerOutcome.hashCode) +
        linkedFindingIds.hashCode +
        (coincidenceBaseline == null ? 0 : coincidenceBaseline.hashCode) +
        alternatives.hashCode;

  factory HypothesisInput.fromJson(Map<String, dynamic> json) => _$HypothesisInputFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum HypothesisInputTypeEnum {
@JsonValue(r'outcome')
outcome(r'outcome'),
@JsonValue(r'explanation')
explanation(r'explanation'),
@JsonValue(r'speculative-link')
speculativeLink(r'speculative-link'),
@JsonValue(r'other')
other(r'other');

const HypothesisInputTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


