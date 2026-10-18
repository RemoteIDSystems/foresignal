//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/infra_rules_rules_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'infra_rules.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InfraRules {
  /// Returns a new [InfraRules] instance.
  InfraRules({

     this.version,

    required  this.rules,
  });

  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final String? version;



  @JsonKey(
    
    name: r'rules',
    required: true,
    includeIfNull: false,
  )


  final List<InfraRulesRulesInner> rules;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InfraRules &&
      other.version == version &&
      other.rules == rules;

    @override
    int get hashCode =>
        version.hashCode +
        rules.hashCode;

  factory InfraRules.fromJson(Map<String, dynamic> json) => _$InfraRulesFromJson(json);

  Map<String, dynamic> toJson() => _$InfraRulesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

