//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'escalation_policy_input_stop_on.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EscalationPolicyInputStopOn {
  /// Returns a new [EscalationPolicyInputStopOn] instance.
  EscalationPolicyInputStopOn({

     this.resolve = true,

     this.snooze = true,
  });

  @JsonKey(
    defaultValue: true,
    name: r'resolve',
    required: false,
    includeIfNull: false,
  )


  final bool? resolve;



  @JsonKey(
    defaultValue: true,
    name: r'snooze',
    required: false,
    includeIfNull: false,
  )


  final bool? snooze;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EscalationPolicyInputStopOn &&
      other.resolve == resolve &&
      other.snooze == snooze;

    @override
    int get hashCode =>
        resolve.hashCode +
        snooze.hashCode;

  factory EscalationPolicyInputStopOn.fromJson(Map<String, dynamic> json) => _$EscalationPolicyInputStopOnFromJson(json);

  Map<String, dynamic> toJson() => _$EscalationPolicyInputStopOnToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

