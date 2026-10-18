//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'base_rate_definition_condition.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BaseRateDefinitionCondition {
  /// Returns a new [BaseRateDefinitionCondition] instance.
  BaseRateDefinitionCondition({

    required  this.family,

    required  this.withinDays,
  });

  @JsonKey(
    
    name: r'family',
    required: true,
    includeIfNull: false,
  )


  final String family;



          // minimum: 1
          // maximum: 365
  @JsonKey(
    
    name: r'withinDays',
    required: true,
    includeIfNull: false,
  )


  final int withinDays;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BaseRateDefinitionCondition &&
      other.family == family &&
      other.withinDays == withinDays;

    @override
    int get hashCode =>
        family.hashCode +
        withinDays.hashCode;

  factory BaseRateDefinitionCondition.fromJson(Map<String, dynamic> json) => _$BaseRateDefinitionConditionFromJson(json);

  Map<String, dynamic> toJson() => _$BaseRateDefinitionConditionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

