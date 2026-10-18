//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/analysis_cycle_input_schedule.dart';
import 'package:foresignal_api/src/model/cycle_scope.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analysis_cycle_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalysisCycleInput {
  /// Returns a new [AnalysisCycleInput] instance.
  AnalysisCycleInput({

    required  this.name,

    required  this.cadence,

    required  this.schedule,

     this.scope,

     this.analystProfileId,

     this.roles,

     this.forecasterProviders,

     this.samples = 3,

     this.maxQuestions,

     this.maxTokensPerCycle,

     this.channelIds,

     this.notifyMinPriority,

     this.enabled,
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


  final AnalysisCycleInputCadenceEnum cadence;



  @JsonKey(
    
    name: r'schedule',
    required: true,
    includeIfNull: false,
  )


  final AnalysisCycleInputSchedule schedule;



  @JsonKey(
    
    name: r'scope',
    required: false,
    includeIfNull: false,
  )


  final CycleScope? scope;



      /// ULID identifier
  @JsonKey(
    
    name: r'analystProfileId',
    required: false,
    includeIfNull: false,
  )


  final String? analystProfileId;



  @JsonKey(
    
    name: r'roles',
    required: false,
    includeIfNull: false,
  )


  final Set<AnalysisCycleInputRolesEnum>? roles;



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
    required: false,
    includeIfNull: false,
  )


  final int? samples;



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


  final AnalysisCycleInputNotifyMinPriorityEnum? notifyMinPriority;



  @JsonKey(
    
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalysisCycleInput &&
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
      other.enabled == enabled;

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
        enabled.hashCode;

  factory AnalysisCycleInput.fromJson(Map<String, dynamic> json) => _$AnalysisCycleInputFromJson(json);

  Map<String, dynamic> toJson() => _$AnalysisCycleInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnalysisCycleInputCadenceEnum {
@JsonValue(r'daily')
daily(r'daily'),
@JsonValue(r'weekly')
weekly(r'weekly'),
@JsonValue(r'monthly')
monthly(r'monthly');

const AnalysisCycleInputCadenceEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnalysisCycleInputRolesEnum {
@JsonValue(r'analyst')
analyst(r'analyst'),
@JsonValue(r'red-team')
redTeam(r'red-team'),
@JsonValue(r'forecaster')
forecaster(r'forecaster'),
@JsonValue(r'proposer')
proposer(r'proposer');

const AnalysisCycleInputRolesEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnalysisCycleInputNotifyMinPriorityEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'normal')
normal(r'normal'),
@JsonValue(r'high')
high(r'high');

const AnalysisCycleInputNotifyMinPriorityEnum(this.value);

final String value;

@override
String toString() => value;
}


