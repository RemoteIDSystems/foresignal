//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/indicator_likelihood.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_review_input_lr_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisReviewInputLrInner {
  /// Returns a new [HypothesisReviewInputLrInner] instance.
  HypothesisReviewInputLrInner({

    required  this.indicatorId,

    required  this.hypothesisId,

    required  this.likelihood,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'indicatorId',
    required: true,
    includeIfNull: false,
  )


  final String indicatorId;



      /// ULID identifier
  @JsonKey(
    
    name: r'hypothesisId',
    required: true,
    includeIfNull: false,
  )


  final String hypothesisId;



  @JsonKey(
    
    name: r'likelihood',
    required: true,
    includeIfNull: false,
  )


  final IndicatorLikelihood likelihood;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisReviewInputLrInner &&
      other.indicatorId == indicatorId &&
      other.hypothesisId == hypothesisId &&
      other.likelihood == likelihood;

    @override
    int get hashCode =>
        indicatorId.hashCode +
        hypothesisId.hashCode +
        likelihood.hashCode;

  factory HypothesisReviewInputLrInner.fromJson(Map<String, dynamic> json) => _$HypothesisReviewInputLrInnerFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisReviewInputLrInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

