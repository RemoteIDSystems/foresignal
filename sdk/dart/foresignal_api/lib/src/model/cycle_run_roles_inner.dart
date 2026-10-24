//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/cycle_run_roles_inner_prompt_template.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cycle_run_roles_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CycleRunRolesInner {
  /// Returns a new [CycleRunRolesInner] instance.
  CycleRunRolesInner({

    required  this.role,

    required  this.status,

     this.reason,

     this.provider,

     this.model,

     this.effort,

     this.inputTokens,

     this.outputTokens,

     this.cachedTokens,

     this.forecastsSubmitted,

     this.proposalIds,

     this.promptTemplate,

     this.warnings,
  });

  @JsonKey(
    
    name: r'role',
    required: true,
    includeIfNull: false,
  )


  final CycleRunRolesInnerRoleEnum role;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final CycleRunRolesInnerStatusEnum status;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;



  @JsonKey(
    
    name: r'provider',
    required: false,
    includeIfNull: false,
  )


  final String? provider;



  @JsonKey(
    
    name: r'model',
    required: false,
    includeIfNull: false,
  )


  final String? model;



      /// LLM effort
  @JsonKey(
    
    name: r'effort',
    required: false,
    includeIfNull: false,
  )


  final CycleRunRolesInnerEffortEnum? effort;



  @JsonKey(
    
    name: r'inputTokens',
    required: false,
    includeIfNull: false,
  )


  final int? inputTokens;



  @JsonKey(
    
    name: r'outputTokens',
    required: false,
    includeIfNull: false,
  )


  final int? outputTokens;



  @JsonKey(
    
    name: r'cachedTokens',
    required: false,
    includeIfNull: false,
  )


  final int? cachedTokens;



  @JsonKey(
    
    name: r'forecastsSubmitted',
    required: false,
    includeIfNull: false,
  )


  final int? forecastsSubmitted;



  @JsonKey(
    
    name: r'proposalIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? proposalIds;



  @JsonKey(
    
    name: r'promptTemplate',
    required: false,
    includeIfNull: false,
  )


  final CycleRunRolesInnerPromptTemplate? promptTemplate;



  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<String>? warnings;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CycleRunRolesInner &&
      other.role == role &&
      other.status == status &&
      other.reason == reason &&
      other.provider == provider &&
      other.model == model &&
      other.effort == effort &&
      other.inputTokens == inputTokens &&
      other.outputTokens == outputTokens &&
      other.cachedTokens == cachedTokens &&
      other.forecastsSubmitted == forecastsSubmitted &&
      other.proposalIds == proposalIds &&
      other.promptTemplate == promptTemplate &&
      other.warnings == warnings;

    @override
    int get hashCode =>
        role.hashCode +
        status.hashCode +
        (reason == null ? 0 : reason.hashCode) +
        (provider == null ? 0 : provider.hashCode) +
        (model == null ? 0 : model.hashCode) +
        (effort == null ? 0 : effort.hashCode) +
        inputTokens.hashCode +
        outputTokens.hashCode +
        cachedTokens.hashCode +
        forecastsSubmitted.hashCode +
        proposalIds.hashCode +
        (promptTemplate == null ? 0 : promptTemplate.hashCode) +
        warnings.hashCode;

  factory CycleRunRolesInner.fromJson(Map<String, dynamic> json) => _$CycleRunRolesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$CycleRunRolesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CycleRunRolesInnerRoleEnum {
@JsonValue(r'analyst')
analyst(r'analyst'),
@JsonValue(r'red-team')
redTeam(r'red-team'),
@JsonValue(r'forecaster')
forecaster(r'forecaster'),
@JsonValue(r'proposer')
proposer(r'proposer');

const CycleRunRolesInnerRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


enum CycleRunRolesInnerStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'skipped')
skipped(r'skipped'),
@JsonValue(r'failed')
failed(r'failed'),
@JsonValue(r'not-configured')
notConfigured(r'not-configured');

const CycleRunRolesInnerStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


/// LLM effort
enum CycleRunRolesInnerEffortEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'medium')
medium(r'medium'),
@JsonValue(r'high')
high(r'high');

const CycleRunRolesInnerEffortEnum(this.value);

final String value;

@override
String toString() => value;
}


