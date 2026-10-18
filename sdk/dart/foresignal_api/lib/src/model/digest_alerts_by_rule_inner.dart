//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'digest_alerts_by_rule_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DigestAlertsByRuleInner {
  /// Returns a new [DigestAlertsByRuleInner] instance.
  DigestAlertsByRuleInner({

    required  this.ruleId,

     this.ruleName,

    required  this.count,
  });

  @JsonKey(
    
    name: r'ruleId',
    required: true,
    includeIfNull: false,
  )


  final String ruleId;



  @JsonKey(
    
    name: r'ruleName',
    required: false,
    includeIfNull: false,
  )


  final String? ruleName;



  @JsonKey(
    
    name: r'count',
    required: true,
    includeIfNull: false,
  )


  final int count;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DigestAlertsByRuleInner &&
      other.ruleId == ruleId &&
      other.ruleName == ruleName &&
      other.count == count;

    @override
    int get hashCode =>
        ruleId.hashCode +
        ruleName.hashCode +
        count.hashCode;

  factory DigestAlertsByRuleInner.fromJson(Map<String, dynamic> json) => _$DigestAlertsByRuleInnerFromJson(json);

  Map<String, dynamic> toJson() => _$DigestAlertsByRuleInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

