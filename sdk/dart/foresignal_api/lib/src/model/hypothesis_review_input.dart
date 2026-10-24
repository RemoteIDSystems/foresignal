//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/hypothesis_review_input_assumptions_inner.dart';
import 'package:foresignal_api/src/model/hypothesis_review_input_lr_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_review_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisReviewInput {
  /// Returns a new [HypothesisReviewInput] instance.
  HypothesisReviewInput({

     this.cause,

    required  this.rationale,

     this.proposalId,

     this.probabilities,

     this.lr,

     this.assumptions,

     this.exceedCap = false,

     this.citations,

     this.version,
  });

      /// default `review`
  @JsonKey(
    
    name: r'cause',
    required: false,
    includeIfNull: false,
  )


  final HypothesisReviewInputCauseEnum? cause;



  @JsonKey(
    
    name: r'rationale',
    required: true,
    includeIfNull: false,
  )


  final String rationale;



      /// ULID identifier
  @JsonKey(
    
    name: r'proposalId',
    required: false,
    includeIfNull: false,
  )


  final String? proposalId;



      /// Direct probability changes per hypothesis id (logged)
  @JsonKey(
    
    name: r'probabilities',
    required: false,
    includeIfNull: false,
  )


  final Map<String, num>? probabilities;



  @JsonKey(
    
    name: r'lr',
    required: false,
    includeIfNull: false,
  )


  final List<HypothesisReviewInputLrInner>? lr;



  @JsonKey(
    
    name: r'assumptions',
    required: false,
    includeIfNull: false,
  )


  final List<HypothesisReviewInputAssumptionsInner>? assumptions;



  @JsonKey(
    defaultValue: false,
    name: r'exceedCap',
    required: false,
    includeIfNull: false,
  )


  final bool? exceedCap;



  @JsonKey(
    
    name: r'citations',
    required: false,
    includeIfNull: false,
  )


  final List<String>? citations;



      /// Set version (optimistic lock)
  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final int? version;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisReviewInput &&
      other.cause == cause &&
      other.rationale == rationale &&
      other.proposalId == proposalId &&
      other.probabilities == probabilities &&
      other.lr == lr &&
      other.assumptions == assumptions &&
      other.exceedCap == exceedCap &&
      other.citations == citations &&
      other.version == version;

    @override
    int get hashCode =>
        cause.hashCode +
        rationale.hashCode +
        (proposalId == null ? 0 : proposalId.hashCode) +
        (probabilities == null ? 0 : probabilities.hashCode) +
        lr.hashCode +
        assumptions.hashCode +
        exceedCap.hashCode +
        citations.hashCode +
        version.hashCode;

  factory HypothesisReviewInput.fromJson(Map<String, dynamic> json) => _$HypothesisReviewInputFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisReviewInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// default `review`
enum HypothesisReviewInputCauseEnum {
@JsonValue(r'review')
review(r'review'),
@JsonValue(r'operator')
operator_(r'operator');

const HypothesisReviewInputCauseEnum(this.value);

final String value;

@override
String toString() => value;
}


