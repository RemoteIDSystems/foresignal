//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_test_antecedent_one_of3.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorTestAntecedentOneOf3 {
  /// Returns a new [PrecursorTestAntecedentOneOf3] instance.
  PrecursorTestAntecedentOneOf3({

    required  this.anomalyRuleId,

     this.classification,
  });

  @JsonKey(
    
    name: r'anomalyRuleId',
    required: true,
    includeIfNull: false,
  )


  final String anomalyRuleId;



  @JsonKey(
    
    name: r'classification',
    required: false,
    includeIfNull: false,
  )


  final PrecursorTestAntecedentOneOf3ClassificationEnum? classification;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorTestAntecedentOneOf3 &&
      other.anomalyRuleId == anomalyRuleId &&
      other.classification == classification;

    @override
    int get hashCode =>
        anomalyRuleId.hashCode +
        classification.hashCode;

  factory PrecursorTestAntecedentOneOf3.fromJson(Map<String, dynamic> json) => _$PrecursorTestAntecedentOneOf3FromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorTestAntecedentOneOf3ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrecursorTestAntecedentOneOf3ClassificationEnum {
@JsonValue(r'activity-up')
activityUp(r'activity-up'),
@JsonValue(r'activity-down')
activityDown(r'activity-down'),
@JsonValue(r'coverage-drop')
coverageDrop(r'coverage-drop'),
@JsonValue(r'insufficient-coverage')
insufficientCoverage(r'insufficient-coverage'),
@JsonValue(r'learning')
learning(r'learning'),
@JsonValue(r'normal')
normal(r'normal');

const PrecursorTestAntecedentOneOf3ClassificationEnum(this.value);

final String value;

@override
String toString() => value;
}


