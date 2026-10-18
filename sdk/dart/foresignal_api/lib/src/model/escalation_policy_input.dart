//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/escalation_step.dart';
import 'package:foresignal_api/src/model/escalation_policy_input_stop_on.dart';
import 'package:foresignal_api/src/model/escalation_policy_input_repeat.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'escalation_policy_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EscalationPolicyInput {
  /// Returns a new [EscalationPolicyInput] instance.
  EscalationPolicyInput({

    required  this.name,

    required  this.steps,

     this.repeat,

     this.stopOn,
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





    @override
    bool operator ==(Object other) => identical(this, other) || other is EscalationPolicyInput &&
      other.name == name &&
      other.steps == steps &&
      other.repeat == repeat &&
      other.stopOn == stopOn;

    @override
    int get hashCode =>
        name.hashCode +
        steps.hashCode +
        (repeat == null ? 0 : repeat.hashCode) +
        stopOn.hashCode;

  factory EscalationPolicyInput.fromJson(Map<String, dynamic> json) => _$EscalationPolicyInputFromJson(json);

  Map<String, dynamic> toJson() => _$EscalationPolicyInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

