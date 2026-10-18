//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/icd_term.dart';
import 'package:foresignal_api/src/model/key_judgment_change.dart';
import 'package:foresignal_api/src/model/confidence_level.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'key_judgment.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class KeyJudgment {
  /// Returns a new [KeyJudgment] instance.
  KeyJudgment({

     this.id,

    required  this.text,

     this.probability,

     this.estimativeTerm,

     this.confidence,

     this.confidenceStatement,

    required  this.citations,

     this.hypothesisSetId,

     this.questionId,

    required  this.textOrigin,

     this.change,

     this.downgraded,

     this.warnings,
  });

  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final String? id;



  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probability',
    required: false,
    includeIfNull: false,
  )


  final num? probability;



  @JsonKey(
    
    name: r'estimativeTerm',
    required: false,
    includeIfNull: false,
  )


  final IcdTerm? estimativeTerm;



  @JsonKey(
    
    name: r'confidence',
    required: false,
    includeIfNull: false,
  )


  final ConfidenceLevel? confidence;



      /// Separate sentence; never in the same sentence as the likelihood term
  @JsonKey(
    
    name: r'confidenceStatement',
    required: false,
    includeIfNull: false,
  )


  final String? confidenceStatement;



  @JsonKey(
    
    name: r'citations',
    required: true,
    includeIfNull: false,
  )


  final List<String> citations;



      /// ULID identifier
  @JsonKey(
    
    name: r'hypothesisSetId',
    required: false,
    includeIfNull: false,
  )


  final String? hypothesisSetId;



      /// ULID identifier
  @JsonKey(
    
    name: r'questionId',
    required: false,
    includeIfNull: false,
  )


  final String? questionId;



  @JsonKey(
    
    name: r'textOrigin',
    required: true,
    includeIfNull: false,
  )


  final KeyJudgmentTextOriginEnum textOrigin;



  @JsonKey(
    
    name: r'change',
    required: false,
    includeIfNull: false,
  )


  final KeyJudgmentChange? change;



      /// Failed lint after one regeneration and was downgraded
  @JsonKey(
    
    name: r'downgraded',
    required: false,
    includeIfNull: false,
  )


  final bool? downgraded;



  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<String>? warnings;





    @override
    bool operator ==(Object other) => identical(this, other) || other is KeyJudgment &&
      other.id == id &&
      other.text == text &&
      other.probability == probability &&
      other.estimativeTerm == estimativeTerm &&
      other.confidence == confidence &&
      other.confidenceStatement == confidenceStatement &&
      other.citations == citations &&
      other.hypothesisSetId == hypothesisSetId &&
      other.questionId == questionId &&
      other.textOrigin == textOrigin &&
      other.change == change &&
      other.downgraded == downgraded &&
      other.warnings == warnings;

    @override
    int get hashCode =>
        (id == null ? 0 : id.hashCode) +
        text.hashCode +
        (probability == null ? 0 : probability.hashCode) +
        (estimativeTerm == null ? 0 : estimativeTerm.hashCode) +
        (confidence == null ? 0 : confidence.hashCode) +
        (confidenceStatement == null ? 0 : confidenceStatement.hashCode) +
        citations.hashCode +
        (hypothesisSetId == null ? 0 : hypothesisSetId.hashCode) +
        (questionId == null ? 0 : questionId.hashCode) +
        textOrigin.hashCode +
        (change == null ? 0 : change.hashCode) +
        downgraded.hashCode +
        warnings.hashCode;

  factory KeyJudgment.fromJson(Map<String, dynamic> json) => _$KeyJudgmentFromJson(json);

  Map<String, dynamic> toJson() => _$KeyJudgmentToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum KeyJudgmentTextOriginEnum {
@JsonValue(r'deterministic')
deterministic(r'deterministic'),
@JsonValue(r'llm')
llm(r'llm');

const KeyJudgmentTextOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


