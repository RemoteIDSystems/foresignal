//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'escalation_policy_input_repeat.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EscalationPolicyInputRepeat {
  /// Returns a new [EscalationPolicyInputRepeat] instance.
  EscalationPolicyInputRepeat({

    required  this.everyMinutes,

    required  this.maxTimes,
  });

          // minimum: 1
  @JsonKey(
    
    name: r'everyMinutes',
    required: true,
    includeIfNull: false,
  )


  final int everyMinutes;



          // minimum: 1
          // maximum: 10
  @JsonKey(
    
    name: r'maxTimes',
    required: true,
    includeIfNull: false,
  )


  final int maxTimes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EscalationPolicyInputRepeat &&
      other.everyMinutes == everyMinutes &&
      other.maxTimes == maxTimes;

    @override
    int get hashCode =>
        everyMinutes.hashCode +
        maxTimes.hashCode;

  factory EscalationPolicyInputRepeat.fromJson(Map<String, dynamic> json) => _$EscalationPolicyInputRepeatFromJson(json);

  Map<String, dynamic> toJson() => _$EscalationPolicyInputRepeatToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

