//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ntfy_message.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NtfyMessage {
  /// Returns a new [NtfyMessage] instance.
  NtfyMessage({

    required  this.id,

    required  this.time,

    required  this.event,

    required  this.topic,

     this.message,

     this.title,

     this.priority,

     this.tags,

     this.click,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// Epoch seconds
  @JsonKey(
    
    name: r'time',
    required: true,
    includeIfNull: false,
  )


  final int time;



  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final NtfyMessageEventEnum event;



  @JsonKey(
    
    name: r'topic',
    required: true,
    includeIfNull: false,
  )


  final String topic;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'priority',
    required: false,
    includeIfNull: false,
  )


  final int? priority;



  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



  @JsonKey(
    
    name: r'click',
    required: false,
    includeIfNull: false,
  )


  final String? click;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NtfyMessage &&
      other.id == id &&
      other.time == time &&
      other.event == event &&
      other.topic == topic &&
      other.message == message &&
      other.title == title &&
      other.priority == priority &&
      other.tags == tags &&
      other.click == click;

    @override
    int get hashCode =>
        id.hashCode +
        time.hashCode +
        event.hashCode +
        topic.hashCode +
        message.hashCode +
        title.hashCode +
        priority.hashCode +
        tags.hashCode +
        click.hashCode;

  factory NtfyMessage.fromJson(Map<String, dynamic> json) => _$NtfyMessageFromJson(json);

  Map<String, dynamic> toJson() => _$NtfyMessageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NtfyMessageEventEnum {
@JsonValue(r'message')
message(r'message');

const NtfyMessageEventEnum(this.value);

final String value;

@override
String toString() => value;
}


