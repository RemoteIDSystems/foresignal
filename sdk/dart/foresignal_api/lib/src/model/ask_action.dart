//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ask_action.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AskAction {
  /// Returns a new [AskAction] instance.
  AskAction({

    required  this.id,

    required  this.type,

    required  this.description,

     this.payload,

    required  this.status,

     this.result,

     this.createdAt,

     this.executedAt,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final AskActionTypeEnum type;



  @JsonKey(
    
    name: r'description',
    required: true,
    includeIfNull: false,
  )


  final String description;



  @JsonKey(
    
    name: r'payload',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? payload;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final AskActionStatusEnum status;



  @JsonKey(
    
    name: r'result',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? result;



  @JsonKey(
    
    name: r'createdAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? createdAt;



  @JsonKey(
    
    name: r'executedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? executedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AskAction &&
      other.id == id &&
      other.type == type &&
      other.description == description &&
      other.payload == payload &&
      other.status == status &&
      other.result == result &&
      other.createdAt == createdAt &&
      other.executedAt == executedAt;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        description.hashCode +
        payload.hashCode +
        status.hashCode +
        (result == null ? 0 : result.hashCode) +
        createdAt.hashCode +
        (executedAt == null ? 0 : executedAt.hashCode);

  factory AskAction.fromJson(Map<String, dynamic> json) => _$AskActionFromJson(json);

  Map<String, dynamic> toJson() => _$AskActionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AskActionTypeEnum {
@JsonValue(r'create_watch')
createWatch(r'create_watch'),
@JsonValue(r'update_watch')
updateWatch(r'update_watch'),
@JsonValue(r'create_briefing')
createBriefing(r'create_briefing'),
@JsonValue(r'acknowledge_alert')
acknowledgeAlert(r'acknowledge_alert'),
@JsonValue(r'alert_status')
alertStatus(r'alert_status'),
@JsonValue(r'snooze_alert')
snoozeAlert(r'snooze_alert'),
@JsonValue(r'create_view')
createView(r'create_view'),
@JsonValue(r'set_alert_feedback')
setAlertFeedback(r'set_alert_feedback'),
@JsonValue(r'apply_tuning_suggestion')
applyTuningSuggestion(r'apply_tuning_suggestion');

const AskActionTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AskActionStatusEnum {
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'executed')
executed(r'executed'),
@JsonValue(r'rejected')
rejected(r'rejected'),
@JsonValue(r'failed')
failed(r'failed');

const AskActionStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


