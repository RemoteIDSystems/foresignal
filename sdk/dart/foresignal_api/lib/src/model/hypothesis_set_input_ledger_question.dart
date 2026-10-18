//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/hypothesis_set_input_ledger_question_resolver.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_set_input_ledger_question.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisSetInputLedgerQuestion {
  /// Returns a new [HypothesisSetInputLedgerQuestion] instance.
  HypothesisSetInputLedgerQuestion({

    required  this.resolver,

     this.resolutionCriteria,

     this.category,
  });

  @JsonKey(
    
    name: r'resolver',
    required: true,
    includeIfNull: false,
  )


  final HypothesisSetInputLedgerQuestionResolver resolver;



  @JsonKey(
    
    name: r'resolutionCriteria',
    required: false,
    includeIfNull: false,
  )


  final String? resolutionCriteria;



  @JsonKey(
    
    name: r'category',
    required: false,
    includeIfNull: false,
  )


  final String? category;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisSetInputLedgerQuestion &&
      other.resolver == resolver &&
      other.resolutionCriteria == resolutionCriteria &&
      other.category == category;

    @override
    int get hashCode =>
        resolver.hashCode +
        (resolutionCriteria == null ? 0 : resolutionCriteria.hashCode) +
        (category == null ? 0 : category.hashCode);

  factory HypothesisSetInputLedgerQuestion.fromJson(Map<String, dynamic> json) => _$HypothesisSetInputLedgerQuestionFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisSetInputLedgerQuestionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

