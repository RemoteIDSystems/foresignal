//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'channel_actions_config.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChannelActionsConfig {
  /// Returns a new [ChannelActionsConfig] instance.
  ChannelActionsConfig({

     this.enabled,

     this.list,

     this.links = true,

     this.reactions = false,
  });

      /// Default true for ntfy, webpush, telegram, matrix, pushover; false for every other type (confirm-page links in the body are an opt-in there)
  @JsonKey(
    
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



  @JsonKey(
    
    name: r'list',
    required: false,
    includeIfNull: false,
  )


  final List<ChannelActionsConfigListEnum>? list;



      /// Add confirm-page links to the body for channels without buttons
  @JsonKey(
    defaultValue: true,
    name: r'links',
    required: false,
    includeIfNull: false,
  )


  final bool? links;



      /// Matrix: a ✅ reaction from `allowedSenders` acknowledges
  @JsonKey(
    defaultValue: false,
    name: r'reactions',
    required: false,
    includeIfNull: false,
  )


  final bool? reactions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChannelActionsConfig &&
      other.enabled == enabled &&
      other.list == list &&
      other.links == links &&
      other.reactions == reactions;

    @override
    int get hashCode =>
        enabled.hashCode +
        list.hashCode +
        links.hashCode +
        reactions.hashCode;

  factory ChannelActionsConfig.fromJson(Map<String, dynamic> json) => _$ChannelActionsConfigFromJson(json);

  Map<String, dynamic> toJson() => _$ChannelActionsConfigToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChannelActionsConfigListEnum {
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

const ChannelActionsConfigListEnum(this.value);

final String value;

@override
String toString() => value;
}


