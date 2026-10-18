//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_test_antecedent_one_of.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorTestAntecedentOneOf {
  /// Returns a new [PrecursorTestAntecedentOneOf] instance.
  PrecursorTestAntecedentOneOf({

    required  this.rule,
  });

      /// Partial TriggerRuleInput: source/kind arrays, conditions, geofence
  @JsonKey(
    
    name: r'rule',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> rule;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorTestAntecedentOneOf &&
      other.rule == rule;

    @override
    int get hashCode =>
        rule.hashCode;

  factory PrecursorTestAntecedentOneOf.fromJson(Map<String, dynamic> json) => _$PrecursorTestAntecedentOneOfFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorTestAntecedentOneOfToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

