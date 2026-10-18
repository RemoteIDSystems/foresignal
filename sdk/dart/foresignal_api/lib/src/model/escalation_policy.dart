//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/escalation_step.dart';
import 'package:foresignal_api/src/model/escalation_policy_input_stop_on.dart';
import 'package:foresignal_api/src/model/escalation_policy_input_repeat.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'escalation_policy.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EscalationPolicy {
  /// Returns a new [EscalationPolicy] instance.
  EscalationPolicy({

    required  this.name,

    required  this.steps,

     this.repeat,

     this.stopOn,

    required  this.id,

    required  this.createdAt,

    required  this.updatedAt,

     this.ruleCount,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'steps',
    required: true,
    includeIfNull: false,
  )


  final List<EscalationStep> steps;



  @JsonKey(
    
    name: r'repeat',
    required: false,
    includeIfNull: false,
  )


  final EscalationPolicyInputRepeat? repeat;



  @JsonKey(
    
    name: r'stopOn',
    required: false,
    includeIfNull: false,
  )


  final EscalationPolicyInputStopOn? stopOn;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime updatedAt;



      /// Rules referencing it
  @JsonKey(
    
    name: r'ruleCount',
    required: false,
    includeIfNull: false,
  )


  final int? ruleCount;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EscalationPolicy &&
      other.name == name &&
      other.steps == steps &&
      other.repeat == repeat &&
      other.stopOn == stopOn &&
      other.id == id &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt &&
      other.ruleCount == ruleCount;

    @override
    int get hashCode =>
        name.hashCode +
        steps.hashCode +
        (repeat == null ? 0 : repeat.hashCode) +
        stopOn.hashCode +
        id.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode +
        ruleCount.hashCode;

  factory EscalationPolicy.fromJson(Map<String, dynamic> json) => _$EscalationPolicyFromJson(json);

  Map<String, dynamic> toJson() => _$EscalationPolicyToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

