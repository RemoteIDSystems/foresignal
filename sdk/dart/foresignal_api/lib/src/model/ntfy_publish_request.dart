//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ntfy_publish_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NtfyPublishRequest {
  /// Returns a new [NtfyPublishRequest] instance.
  NtfyPublishRequest({

    required  this.topic,

     this.message,

     this.title,

     this.priority,

     this.tags,

     this.click,
  });

      /// The custom source id
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



          // minimum: 1
          // maximum: 5
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
    bool operator ==(Object other) => identical(this, other) || other is NtfyPublishRequest &&
      other.topic == topic &&
      other.message == message &&
      other.title == title &&
      other.priority == priority &&
      other.tags == tags &&
      other.click == click;

    @override
    int get hashCode =>
        topic.hashCode +
        message.hashCode +
        title.hashCode +
        priority.hashCode +
        tags.hashCode +
        click.hashCode;

  factory NtfyPublishRequest.fromJson(Map<String, dynamic> json) => _$NtfyPublishRequestFromJson(json);

  Map<String, dynamic> toJson() => _$NtfyPublishRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

