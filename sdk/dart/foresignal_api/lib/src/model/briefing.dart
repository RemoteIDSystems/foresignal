//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/briefing_scope.dart';
import 'package:foresignal_api/src/model/briefing_input_audio.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'briefing.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Briefing {
  /// Returns a new [Briefing] instance.
  Briefing({

    required  this.name,

     this.enabled = true,

    required  this.schedule,

     this.timezone = 'UTC',

     this.scope,

     this.lookback,

     this.mode,

     this.instructions,

     this.channelIds,

     this.maxItemsPerKind = 5,

     this.audio,

    required  this.id,

    required  this.createdAt,

     this.updatedAt,

     this.nextRunAt,

     this.lastRunAt,

     this.lastRunStatus,

     this.warnings,
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



      /// 5-field cron
  @JsonKey(
    
    name: r'schedule',
    required: true,
    includeIfNull: false,
  )


  final String schedule;



  @JsonKey(
    defaultValue: 'UTC',
    name: r'timezone',
    required: false,
    includeIfNull: false,
  )


  final String? timezone;



  @JsonKey(
    
    name: r'scope',
    required: false,
    includeIfNull: false,
  )


  final BriefingScope? scope;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'lookback',
    required: false,
    includeIfNull: false,
  )


  final String? lookback;



  @JsonKey(
    
    name: r'mode',
    required: false,
    includeIfNull: false,
  )


  final BriefingModeEnum? mode;



  @JsonKey(
    
    name: r'instructions',
    required: false,
    includeIfNull: false,
  )


  final String? instructions;



  @JsonKey(
    
    name: r'channelIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? channelIds;



          // minimum: 1
          // maximum: 50
  @JsonKey(
    defaultValue: 5,
    name: r'maxItemsPerKind',
    required: false,
    includeIfNull: false,
  )


  final int? maxItemsPerKind;



  @JsonKey(
    
    name: r'audio',
    required: false,
    includeIfNull: false,
  )


  final BriefingInputAudio? audio;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



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
    
    name: r'lastRunStatus',
    required: false,
    includeIfNull: false,
  )


  final String? lastRunStatus;



      /// Non-fatal validation warnings, e.g. a channel not subscribed to `briefing`
  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<String>? warnings;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Briefing &&
      other.name == name &&
      other.enabled == enabled &&
      other.schedule == schedule &&
      other.timezone == timezone &&
      other.scope == scope &&
      other.lookback == lookback &&
      other.mode == mode &&
      other.instructions == instructions &&
      other.channelIds == channelIds &&
      other.maxItemsPerKind == maxItemsPerKind &&
      other.audio == audio &&
      other.id == id &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt &&
      other.nextRunAt == nextRunAt &&
      other.lastRunAt == lastRunAt &&
      other.lastRunStatus == lastRunStatus &&
      other.warnings == warnings;

    @override
    int get hashCode =>
        name.hashCode +
        enabled.hashCode +
        schedule.hashCode +
        timezone.hashCode +
        scope.hashCode +
        (lookback == null ? 0 : lookback.hashCode) +
        mode.hashCode +
        (instructions == null ? 0 : instructions.hashCode) +
        channelIds.hashCode +
        maxItemsPerKind.hashCode +
        (audio == null ? 0 : audio.hashCode) +
        id.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode +
        (nextRunAt == null ? 0 : nextRunAt.hashCode) +
        (lastRunAt == null ? 0 : lastRunAt.hashCode) +
        (lastRunStatus == null ? 0 : lastRunStatus.hashCode) +
        warnings.hashCode;

  factory Briefing.fromJson(Map<String, dynamic> json) => _$BriefingFromJson(json);

  Map<String, dynamic> toJson() => _$BriefingToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BriefingModeEnum {
@JsonValue(r'digest')
digest(r'digest'),
@JsonValue(r'llm')
llm(r'llm');

const BriefingModeEnum(this.value);

final String value;

@override
String toString() => value;
}


