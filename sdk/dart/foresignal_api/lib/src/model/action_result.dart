//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'action_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ActionResult {
  /// Returns a new [ActionResult] instance.
  ActionResult({

    required  this.action,

    required  this.alertId,

    required  this.result,
  });

  @JsonKey(
    
    name: r'action',
    required: true,
    includeIfNull: false,
  )


  final ActionResultActionEnum action;



  @JsonKey(
    
    name: r'alertId',
    required: true,
    includeIfNull: false,
  )


  final int alertId;



      /// e.g. acknowledged, snoozed, muted, already-acknowledged, already-resolved
  @JsonKey(
    
    name: r'result',
    required: true,
    includeIfNull: false,
  )


  final String result;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ActionResult &&
      other.action == action &&
      other.alertId == alertId &&
      other.result == result;

    @override
    int get hashCode =>
        action.hashCode +
        alertId.hashCode +
        result.hashCode;

  factory ActionResult.fromJson(Map<String, dynamic> json) => _$ActionResultFromJson(json);

  Map<String, dynamic> toJson() => _$ActionResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ActionResultActionEnum {
@JsonValue(r'ack')
ack(r'ack'),
@JsonValue(r'snooze')
snooze(r'snooze'),
@JsonValue(r'mute')
mute(r'mute'),
@JsonValue(r'feedback:useful')
feedbackColonUseful(r'feedback:useful'),
@JsonValue(r'feedback:noise')
feedbackColonNoise(r'feedback:noise');

const ActionResultActionEnum(this.value);

final String value;

@override
String toString() => value;
}


