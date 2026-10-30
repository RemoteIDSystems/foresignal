//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'delivery_target.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DeliveryTarget {
  /// Returns a new [DeliveryTarget] instance.
  DeliveryTarget({

    required  this.kind,

     this.channelId,

    required  this.format,

     this.options,
  });

  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final DeliveryTargetKindEnum kind;



      /// The notify channel (`listChannels`); required for `channel`, null otherwise
  @JsonKey(
    
    name: r'channelId',
    required: false,
    includeIfNull: false,
  )


  final String? channelId;



      /// The payload shape: `email` the HTML edition with the audio attached and a link to the site; `push` a headline, summary and link to the site, never the full body; `alert` the alert-notification shape; `html` the standalone page (`website`); `audio` the synthesized edition (`podcast`)
  @JsonKey(
    
    name: r'format',
    required: true,
    includeIfNull: false,
  )


  final DeliveryTargetFormatEnum format;



      /// Per-kind options, passed to the channel's renderer as they are
  @JsonKey(
    
    name: r'options',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? options;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DeliveryTarget &&
      other.kind == kind &&
      other.channelId == channelId &&
      other.format == format &&
      other.options == options;

    @override
    int get hashCode =>
        kind.hashCode +
        (channelId == null ? 0 : channelId.hashCode) +
        format.hashCode +
        options.hashCode;

  factory DeliveryTarget.fromJson(Map<String, dynamic> json) => _$DeliveryTargetFromJson(json);

  Map<String, dynamic> toJson() => _$DeliveryTargetToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DeliveryTargetKindEnum {
@JsonValue(r'channel')
channel(r'channel'),
@JsonValue(r'website')
website(r'website'),
@JsonValue(r'podcast')
podcast(r'podcast');

const DeliveryTargetKindEnum(this.value);

final String value;

@override
String toString() => value;
}


/// The payload shape: `email` the HTML edition with the audio attached and a link to the site; `push` a headline, summary and link to the site, never the full body; `alert` the alert-notification shape; `html` the standalone page (`website`); `audio` the synthesized edition (`podcast`)
enum DeliveryTargetFormatEnum {
@JsonValue(r'email')
email(r'email'),
@JsonValue(r'push')
push(r'push'),
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'html')
html(r'html'),
@JsonValue(r'audio')
audio(r'audio');

const DeliveryTargetFormatEnum(this.value);

final String value;

@override
String toString() => value;
}


