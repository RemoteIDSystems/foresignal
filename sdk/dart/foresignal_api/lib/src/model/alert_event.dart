//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/alert.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'alert_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AlertEvent {
  /// Returns a new [AlertEvent] instance.
  AlertEvent({

    required  this.type,

    required  this.alert,

     this.suppressed = false,

     this.exercise = false,
  });

      /// Lifecycle transition; SSE maps fired → `alert`, resolved → `alert.resolved`, all others → `alert.updated`
  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final AlertEventTypeEnum type;



  @JsonKey(
    
    name: r'alert',
    required: true,
    includeIfNull: false,
  )


  final Alert alert;



      /// Suppressed by a rule or alert snooze (not routed to channels; hidden on streams unless includeSuppressed)
  @JsonKey(
    defaultValue: false,
    name: r'suppressed',
    required: false,
    includeIfNull: false,
  )


  final bool? suppressed;



      /// Exercise alert
  @JsonKey(
    defaultValue: false,
    name: r'exercise',
    required: false,
    includeIfNull: false,
  )


  final bool? exercise;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AlertEvent &&
      other.type == type &&
      other.alert == alert &&
      other.suppressed == suppressed &&
      other.exercise == exercise;

    @override
    int get hashCode =>
        type.hashCode +
        alert.hashCode +
        suppressed.hashCode +
        exercise.hashCode;

  factory AlertEvent.fromJson(Map<String, dynamic> json) => _$AlertEventFromJson(json);

  Map<String, dynamic> toJson() => _$AlertEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Lifecycle transition; SSE maps fired → `alert`, resolved → `alert.resolved`, all others → `alert.updated`
enum AlertEventTypeEnum {
@JsonValue(r'fired')
fired(r'fired'),
@JsonValue(r'updated')
updated(r'updated'),
@JsonValue(r'acknowledged')
acknowledged(r'acknowledged'),
@JsonValue(r'snoozed')
snoozed(r'snoozed'),
@JsonValue(r'resolved')
resolved(r'resolved');

const AlertEventTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


