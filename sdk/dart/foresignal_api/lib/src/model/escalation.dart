//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'escalation.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Escalation {
  /// Returns a new [Escalation] instance.
  Escalation({

    required  this.alertId,

    required  this.policyId,

     this.nextStep,

     this.nextAt,

     this.repeatsDone,

    required  this.state,

     this.stoppedReason,

     this.updatedAt,
  });

  @JsonKey(
    
    name: r'alertId',
    required: true,
    includeIfNull: false,
  )


  final int alertId;



  @JsonKey(
    
    name: r'policyId',
    required: true,
    includeIfNull: false,
  )


  final String policyId;



  @JsonKey(
    
    name: r'nextStep',
    required: false,
    includeIfNull: false,
  )


  final int? nextStep;



  @JsonKey(
    
    name: r'nextAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? nextAt;



  @JsonKey(
    
    name: r'repeatsDone',
    required: false,
    includeIfNull: false,
  )


  final int? repeatsDone;



  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final EscalationStateEnum state;



  @JsonKey(
    
    name: r'stoppedReason',
    required: false,
    includeIfNull: false,
  )


  final String? stoppedReason;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Escalation &&
      other.alertId == alertId &&
      other.policyId == policyId &&
      other.nextStep == nextStep &&
      other.nextAt == nextAt &&
      other.repeatsDone == repeatsDone &&
      other.state == state &&
      other.stoppedReason == stoppedReason &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        alertId.hashCode +
        policyId.hashCode +
        nextStep.hashCode +
        (nextAt == null ? 0 : nextAt.hashCode) +
        repeatsDone.hashCode +
        state.hashCode +
        (stoppedReason == null ? 0 : stoppedReason.hashCode) +
        updatedAt.hashCode;

  factory Escalation.fromJson(Map<String, dynamic> json) => _$EscalationFromJson(json);

  Map<String, dynamic> toJson() => _$EscalationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EscalationStateEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'stopped')
stopped(r'stopped'),
@JsonValue(r'exhausted')
exhausted(r'exhausted');

const EscalationStateEnum(this.value);

final String value;

@override
String toString() => value;
}


