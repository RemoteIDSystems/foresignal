//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/analysis_cycle_input_schedule.dart';
import 'package:foresignal_api/src/model/cycle_scope.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analysis_cycle.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalysisCycle {
  /// Returns a new [AnalysisCycle] instance.
  AnalysisCycle({

    required  this.name,

    required  this.cadence,

    required  this.schedule,

    required  this.scope,

     this.analystProfileId,

    required  this.roles,

     this.forecasterProviders,

     this.samples = 3,

     this.maxQuestions,

     this.maxTokensPerCycle,

     this.channelIds,

     this.notifyMinPriority,

    required  this.enabled,

    required  this.id,

     this.builtIn,

     this.nextRunAt,

     this.lastRunId,

    required  this.createdAt,

     this.updatedAt,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'cadence',
    required: true,
    includeIfNull: false,
  )


  final AnalysisCycleCadenceEnum cadence;



  @JsonKey(
    
    name: r'schedule',
    required: true,
    includeIfNull: false,
  )


  final AnalysisCycleInputSchedule schedule;



  @JsonKey(
    
    name: r'scope',
    required: true,
    includeIfNull: false,
  )


  final CycleScope scope;



      /// ULID identifier
  @JsonKey(
    
    name: r'analystProfileId',
    required: false,
    includeIfNull: false,
  )


  final String? analystProfileId;



  @JsonKey(
    
    name: r'roles',
    required: true,
    includeIfNull: false,
  )


  final Set<AnalysisCycleRolesEnum> roles;



      /// Provider ids for the forecaster role; default the profile's
  @JsonKey(
    
    name: r'forecasterProviders',
    required: false,
    includeIfNull: false,
  )


  final List<String>? forecasterProviders;



          // minimum: 1
          // maximum: 7
  @JsonKey(
    defaultValue: 3,
    name: r'samples',
    required: true,
    includeIfNull: false,
  )


  final int samples;



      /// Default 15 daily, 40 weekly, 60 monthly
          // minimum: 0
  @JsonKey(
    
    name: r'maxQuestions',
    required: false,
    includeIfNull: false,
  )


  final int? maxQuestions;



      /// Default 300k daily, 1.2M weekly, 2M monthly
          // minimum: 0
  @JsonKey(
    
    name: r'maxTokensPerCycle',
    required: false,
    includeIfNull: false,
  )


  final int? maxTokensPerCycle;



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


  final AnalysisCycleNotifyMinPriorityEnum? notifyMinPriority;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'builtIn',
    required: false,
    includeIfNull: false,
  )


  final bool? builtIn;



  @JsonKey(
    
    name: r'nextRunAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? nextRunAt;



      /// ULID identifier
  @JsonKey(
    
    name: r'lastRunId',
    required: false,
    includeIfNull: false,
  )


  final String? lastRunId;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalysisCycle &&
      other.name == name &&
      other.cadence == cadence &&
      other.schedule == schedule &&
      other.scope == scope &&
      other.analystProfileId == analystProfileId &&
      other.roles == roles &&
      other.forecasterProviders == forecasterProviders &&
      other.samples == samples &&
      other.maxQuestions == maxQuestions &&
      other.maxTokensPerCycle == maxTokensPerCycle &&
      other.channelIds == channelIds &&
      other.notifyMinPriority == notifyMinPriority &&
      other.enabled == enabled &&
      other.id == id &&
      other.builtIn == builtIn &&
      other.nextRunAt == nextRunAt &&
      other.lastRunId == lastRunId &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        name.hashCode +
        cadence.hashCode +
        schedule.hashCode +
        scope.hashCode +
        (analystProfileId == null ? 0 : analystProfileId.hashCode) +
        roles.hashCode +
        forecasterProviders.hashCode +
        samples.hashCode +
        maxQuestions.hashCode +
        maxTokensPerCycle.hashCode +
        channelIds.hashCode +
        notifyMinPriority.hashCode +
        enabled.hashCode +
        id.hashCode +
        builtIn.hashCode +
        (nextRunAt == null ? 0 : nextRunAt.hashCode) +
        (lastRunId == null ? 0 : lastRunId.hashCode) +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory AnalysisCycle.fromJson(Map<String, dynamic> json) => _$AnalysisCycleFromJson(json);

  Map<String, dynamic> toJson() => _$AnalysisCycleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnalysisCycleCadenceEnum {
@JsonValue(r'daily')
daily(r'daily'),
@JsonValue(r'weekly')
weekly(r'weekly'),
@JsonValue(r'monthly')
monthly(r'monthly');

const AnalysisCycleCadenceEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnalysisCycleRolesEnum {
@JsonValue(r'analyst')
analyst(r'analyst'),
@JsonValue(r'red-team')
redTeam(r'red-team'),
@JsonValue(r'forecaster')
forecaster(r'forecaster'),
@JsonValue(r'proposer')
proposer(r'proposer');

const AnalysisCycleRolesEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnalysisCycleNotifyMinPriorityEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'normal')
normal(r'normal'),
@JsonValue(r'high')
high(r'high');

const AnalysisCycleNotifyMinPriorityEnum(this.value);

final String value;

@override
String toString() => value;
}


