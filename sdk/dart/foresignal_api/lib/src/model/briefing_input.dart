//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/briefing_scope.dart';
import 'package:foresignal_api/src/model/briefing_input_audio.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'briefing_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BriefingInput {
  /// Returns a new [BriefingInput] instance.
  BriefingInput({

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


  final BriefingInputModeEnum? mode;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is BriefingInput &&
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
      other.audio == audio;

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
        (audio == null ? 0 : audio.hashCode);

  factory BriefingInput.fromJson(Map<String, dynamic> json) => _$BriefingInputFromJson(json);

  Map<String, dynamic> toJson() => _$BriefingInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BriefingInputModeEnum {
@JsonValue(r'digest')
digest(r'digest'),
@JsonValue(r'llm')
llm(r'llm');

const BriefingInputModeEnum(this.value);

final String value;

@override
String toString() => value;
}


