//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/analyst_profile_schedule.dart';
import 'package:foresignal_api/src/model/analyst_profile_scope.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_profile_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystProfileInput {
  /// Returns a new [AnalystProfileInput] instance.
  AnalystProfileInput({

    required  this.name,

     this.enabled = true,

     this.schedule,

     this.scope,

     this.lookback,

     this.instructions,

     this.providerId,

     this.effort,

     this.maxRunsPerDay = 48,

     this.maxTokensPerRun = 60000,

     this.maxToolCalls = 16,

     this.maxWallSeconds = 180,

     this.allowActions = false,

     this.allowedActions,

     this.maxAutoActionsPerDay = 5,

     this.channelIds,

     this.notifyMinPriority,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    defaultValue: true,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



  @JsonKey(
    
    name: r'schedule',
    required: false,
    includeIfNull: false,
  )


  final AnalystProfileSchedule? schedule;



  @JsonKey(
    
    name: r'scope',
    required: false,
    includeIfNull: false,
  )


  final AnalystProfileScope? scope;



      /// Default: since the last run, capped at 7d; history tools may reach back 90d
  @JsonKey(
    
    name: r'lookback',
    required: false,
    includeIfNull: false,
  )


  final String? lookback;



  @JsonKey(
    
    name: r'instructions',
    required: false,
    includeIfNull: false,
  )


  final String? instructions;



      /// LLM provider; null uses the chat role
  @JsonKey(
    
    name: r'providerId',
    required: false,
    includeIfNull: false,
  )


  final String? providerId;



  @JsonKey(
    
    name: r'effort',
    required: false,
    includeIfNull: false,
  )


  final AnalystProfileInputEffortEnum? effort;



          // minimum: 1
  @JsonKey(
    defaultValue: 48,
    name: r'maxRunsPerDay',
    required: false,
    includeIfNull: false,
  )


  final int? maxRunsPerDay;



          // minimum: 1000
  @JsonKey(
    defaultValue: 60000,
    name: r'maxTokensPerRun',
    required: false,
    includeIfNull: false,
  )


  final int? maxTokensPerRun;



          // minimum: 1
  @JsonKey(
    defaultValue: 16,
    name: r'maxToolCalls',
    required: false,
    includeIfNull: false,
  )


  final int? maxToolCalls;



          // minimum: 10
  @JsonKey(
    defaultValue: 180,
    name: r'maxWallSeconds',
    required: false,
    includeIfNull: false,
  )


  final int? maxWallSeconds;



      /// Requires the caller to hold agent scope; restricted tokens cannot own an action-enabled profile
  @JsonKey(
    defaultValue: false,
    name: r'allowActions',
    required: false,
    includeIfNull: false,
  )


  final bool? allowActions;



      /// Extensible (128 adds proposal types)
  @JsonKey(
    
    name: r'allowedActions',
    required: false,
    includeIfNull: false,
  )


  final List<AnalystProfileInputAllowedActionsEnum>? allowedActions;



          // minimum: 0
  @JsonKey(
    defaultValue: 5,
    name: r'maxAutoActionsPerDay',
    required: false,
    includeIfNull: false,
  )


  final int? maxAutoActionsPerDay;



  @JsonKey(
    
    name: r'channelIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? channelIds;



  @JsonKey(
    
    name: r'notifyMinPriority',
    required: false,
    includeIfNull: false,
  )


  final AnalystProfileInputNotifyMinPriorityEnum? notifyMinPriority;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystProfileInput &&
      other.name == name &&
      other.enabled == enabled &&
      other.schedule == schedule &&
      other.scope == scope &&
      other.lookback == lookback &&
      other.instructions == instructions &&
      other.providerId == providerId &&
      other.effort == effort &&
      other.maxRunsPerDay == maxRunsPerDay &&
      other.maxTokensPerRun == maxTokensPerRun &&
      other.maxToolCalls == maxToolCalls &&
      other.maxWallSeconds == maxWallSeconds &&
      other.allowActions == allowActions &&
      other.allowedActions == allowedActions &&
      other.maxAutoActionsPerDay == maxAutoActionsPerDay &&
      other.channelIds == channelIds &&
      other.notifyMinPriority == notifyMinPriority;

    @override
    int get hashCode =>
        name.hashCode +
        enabled.hashCode +
        schedule.hashCode +
        scope.hashCode +
        (lookback == null ? 0 : lookback.hashCode) +
        (instructions == null ? 0 : instructions.hashCode) +
        (providerId == null ? 0 : providerId.hashCode) +
        effort.hashCode +
        maxRunsPerDay.hashCode +
        maxTokensPerRun.hashCode +
        maxToolCalls.hashCode +
        maxWallSeconds.hashCode +
        allowActions.hashCode +
        allowedActions.hashCode +
        maxAutoActionsPerDay.hashCode +
        channelIds.hashCode +
        notifyMinPriority.hashCode;

  factory AnalystProfileInput.fromJson(Map<String, dynamic> json) => _$AnalystProfileInputFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystProfileInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnalystProfileInputEffortEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'medium')
medium(r'medium');

const AnalystProfileInputEffortEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnalystProfileInputAllowedActionsEnum {
@JsonValue(r'propose_watch')
proposeWatch(r'propose_watch'),
@JsonValue(r'propose_intel_event')
proposeIntelEvent(r'propose_intel_event'),
@JsonValue(r'propose_investigation_item')
proposeInvestigationItem(r'propose_investigation_item'),
@JsonValue(r'annotate_event')
annotateEvent(r'annotate_event'),
@JsonValue(r'propose_hypothesis_set')
proposeHypothesisSet(r'propose_hypothesis_set'),
@JsonValue(r'propose_indicator')
proposeIndicator(r'propose_indicator'),
@JsonValue(r'propose_question')
proposeQuestion(r'propose_question'),
@JsonValue(r'propose_situation_merge')
proposeSituationMerge(r'propose_situation_merge'),
@JsonValue(r'propose_chronicle')
proposeChronicle(r'propose_chronicle');

const AnalystProfileInputAllowedActionsEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnalystProfileInputNotifyMinPriorityEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'normal')
normal(r'normal'),
@JsonValue(r'high')
high(r'high');

const AnalystProfileInputNotifyMinPriorityEnum(this.value);

final String value;

@override
String toString() => value;
}


