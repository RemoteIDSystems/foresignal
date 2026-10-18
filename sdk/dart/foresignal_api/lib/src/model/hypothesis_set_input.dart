//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/hypothesis_input.dart';
import 'package:foresignal_api/src/model/hypothesis_set_input_ledger_question.dart';
import 'package:foresignal_api/src/model/indicator_input.dart';
import 'package:foresignal_api/src/model/key_assumption_input.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_set_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisSetInput {
  /// Returns a new [HypothesisSetInput] instance.
  HypothesisSetInput({

    required  this.question,

     this.description,

    required  this.kind,

    required  this.cadence,

     this.horizon,

     this.situationId,

     this.questionId,

     this.regionCodes,

     this.themes,

     this.private = false,

    required  this.hypotheses,

     this.indicators,

     this.assumptions,

     this.ledgerQuestion,

     this.proposalId,
  });

  @JsonKey(
    
    name: r'question',
    required: true,
    includeIfNull: false,
  )


  final String question;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



      /// `exclusive`: probabilities sum to 1 with exactly one `other` hypothesis
  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final HypothesisSetInputKindEnum kind;



  @JsonKey(
    
    name: r'cadence',
    required: true,
    includeIfNull: false,
  )


  final HypothesisSetInputCadenceEnum cadence;



  @JsonKey(
    
    name: r'horizon',
    required: false,
    includeIfNull: false,
  )


  final DateTime? horizon;



      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: false,
    includeIfNull: false,
  )


  final String? situationId;



      /// ULID identifier
  @JsonKey(
    
    name: r'questionId',
    required: false,
    includeIfNull: false,
  )


  final String? questionId;



      /// ISO 3166-1 alpha-2 country or ISO 3166-2 subdivision codes
  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



  @JsonKey(
    
    name: r'themes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? themes;



  @JsonKey(
    defaultValue: false,
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;



  @JsonKey(
    
    name: r'hypotheses',
    required: true,
    includeIfNull: false,
  )


  final List<HypothesisInput> hypotheses;



  @JsonKey(
    
    name: r'indicators',
    required: false,
    includeIfNull: false,
  )


  final List<IndicatorInput>? indicators;



  @JsonKey(
    
    name: r'assumptions',
    required: false,
    includeIfNull: false,
  )


  final List<KeyAssumptionInput>? assumptions;



  @JsonKey(
    
    name: r'ledgerQuestion',
    required: false,
    includeIfNull: false,
  )


  final HypothesisSetInputLedgerQuestion? ledgerQuestion;



      /// ULID identifier
  @JsonKey(
    
    name: r'proposalId',
    required: false,
    includeIfNull: false,
  )


  final String? proposalId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisSetInput &&
      other.question == question &&
      other.description == description &&
      other.kind == kind &&
      other.cadence == cadence &&
      other.horizon == horizon &&
      other.situationId == situationId &&
      other.questionId == questionId &&
      other.regionCodes == regionCodes &&
      other.themes == themes &&
      other.private == private &&
      other.hypotheses == hypotheses &&
      other.indicators == indicators &&
      other.assumptions == assumptions &&
      other.ledgerQuestion == ledgerQuestion &&
      other.proposalId == proposalId;

    @override
    int get hashCode =>
        question.hashCode +
        (description == null ? 0 : description.hashCode) +
        kind.hashCode +
        cadence.hashCode +
        (horizon == null ? 0 : horizon.hashCode) +
        (situationId == null ? 0 : situationId.hashCode) +
        (questionId == null ? 0 : questionId.hashCode) +
        regionCodes.hashCode +
        themes.hashCode +
        private.hashCode +
        hypotheses.hashCode +
        indicators.hashCode +
        assumptions.hashCode +
        (ledgerQuestion == null ? 0 : ledgerQuestion.hashCode) +
        (proposalId == null ? 0 : proposalId.hashCode);

  factory HypothesisSetInput.fromJson(Map<String, dynamic> json) => _$HypothesisSetInputFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisSetInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `exclusive`: probabilities sum to 1 with exactly one `other` hypothesis
enum HypothesisSetInputKindEnum {
@JsonValue(r'exclusive')
exclusive(r'exclusive'),
@JsonValue(r'independent')
independent(r'independent');

const HypothesisSetInputKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum HypothesisSetInputCadenceEnum {
@JsonValue(r'daily')
daily(r'daily'),
@JsonValue(r'weekly')
weekly(r'weekly'),
@JsonValue(r'monthly')
monthly(r'monthly');

const HypothesisSetInputCadenceEnum(this.value);

final String value;

@override
String toString() => value;
}


