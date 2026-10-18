//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/analyst_profile_schedule.dart';
import 'package:foresignal_api/src/model/analyst_profile_scope.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_profile.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystProfile {
  /// Returns a new [AnalystProfile] instance.
  AnalystProfile({

    required  this.id,

    required  this.name,

     this.enabled = true,

    required  this.schedule,

     this.scope,

     this.lookback,

     this.instructions,

     this.providerId,

    required  this.effort,

     this.maxRunsPerDay = 48,

     this.maxTokensPerRun = 60000,

     this.maxToolCalls = 16,

     this.maxWallSeconds = 180,

     this.allowActions = false,

     this.allowedActions,

     this.maxAutoActionsPerDay = 5,

     this.channelIds,

     this.notifyMinPriority,

    required  this.ownerTokenId,

     this.nextRunAt,

     this.lastRunAt,

    required  this.createdAt,

    required  this.updatedAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    defaultValue: true,
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'schedule',
    required: true,
    includeIfNull: false,
  )


  final AnalystProfileSchedule schedule;



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
    required: true,
    includeIfNull: false,
  )


  final AnalystProfileEffortEnum effort;



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
    required: true,
    includeIfNull: false,
  )


  final bool allowActions;



      /// Extensible (128 adds proposal types)
  @JsonKey(
    
    name: r'allowedActions',
    required: false,
    includeIfNull: false,
  )


  final List<AnalystProfileAllowedActionsEnum>? allowedActions;



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


  final AnalystProfileNotifyMinPriorityEnum? notifyMinPriority;



  @JsonKey(
    
    name: r'ownerTokenId',
    required: true,
    includeIfNull: false,
  )


  final String ownerTokenId;



  @JsonKey(
    
    name: r'nextRunAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? nextRunAt;



  @JsonKey(
    
    name: r'lastRunAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastRunAt;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystProfile &&
      other.id == id &&
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
      other.notifyMinPriority == notifyMinPriority &&
      other.ownerTokenId == ownerTokenId &&
      other.nextRunAt == nextRunAt &&
      other.lastRunAt == lastRunAt &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
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
        notifyMinPriority.hashCode +
        ownerTokenId.hashCode +
        (nextRunAt == null ? 0 : nextRunAt.hashCode) +
        (lastRunAt == null ? 0 : lastRunAt.hashCode) +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory AnalystProfile.fromJson(Map<String, dynamic> json) => _$AnalystProfileFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystProfileToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnalystProfileEffortEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'medium')
medium(r'medium');

const AnalystProfileEffortEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnalystProfileAllowedActionsEnum {
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

const AnalystProfileAllowedActionsEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnalystProfileNotifyMinPriorityEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'normal')
normal(r'normal'),
@JsonValue(r'high')
high(r'high');

const AnalystProfileNotifyMinPriorityEnum(this.value);

final String value;

@override
String toString() => value;
}


