//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_review_input_assumptions_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisReviewInputAssumptionsInner {
  /// Returns a new [HypothesisReviewInputAssumptionsInner] instance.
  HypothesisReviewInputAssumptionsInner({

    required  this.assumptionId,

    required  this.status,

     this.rationale,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'assumptionId',
    required: true,
    includeIfNull: false,
  )


  final String assumptionId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final HypothesisReviewInputAssumptionsInnerStatusEnum status;



  @JsonKey(
    
    name: r'rationale',
    required: false,
    includeIfNull: false,
  )


  final String? rationale;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisReviewInputAssumptionsInner &&
      other.assumptionId == assumptionId &&
      other.status == status &&
      other.rationale == rationale;

    @override
    int get hashCode =>
        assumptionId.hashCode +
        status.hashCode +
        (rationale == null ? 0 : rationale.hashCode);

  factory HypothesisReviewInputAssumptionsInner.fromJson(Map<String, dynamic> json) => _$HypothesisReviewInputAssumptionsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisReviewInputAssumptionsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum HypothesisReviewInputAssumptionsInnerStatusEnum {
@JsonValue(r'solid')
solid(r'solid'),
@JsonValue(r'caveated')
caveated(r'caveated'),
@JsonValue(r'unsupported')
unsupported(r'unsupported'),
@JsonValue(r'broken')
broken(r'broken');

const HypothesisReviewInputAssumptionsInnerStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


