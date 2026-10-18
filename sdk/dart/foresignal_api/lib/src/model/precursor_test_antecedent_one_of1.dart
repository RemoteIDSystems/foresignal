//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_test_antecedent_one_of1.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorTestAntecedentOneOf1 {
  /// Returns a new [PrecursorTestAntecedentOneOf1] instance.
  PrecursorTestAntecedentOneOf1({

    required  this.alertRuleId,
  });

  @JsonKey(
    
    name: r'alertRuleId',
    required: true,
    includeIfNull: false,
  )


  final String alertRuleId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorTestAntecedentOneOf1 &&
      other.alertRuleId == alertRuleId;

    @override
    int get hashCode =>
        alertRuleId.hashCode;

  factory PrecursorTestAntecedentOneOf1.fromJson(Map<String, dynamic> json) => _$PrecursorTestAntecedentOneOf1FromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorTestAntecedentOneOf1ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

