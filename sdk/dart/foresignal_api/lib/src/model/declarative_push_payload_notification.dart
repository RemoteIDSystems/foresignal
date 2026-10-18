//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/push_payload.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'declarative_push_payload_notification.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DeclarativePushPayloadNotification {
  /// Returns a new [DeclarativePushPayloadNotification] instance.
  DeclarativePushPayloadNotification({

    required  this.title,

     this.body,

    required  this.navigate,

     this.lang,

     this.tag,

     this.silent,

     this.data,
  });

  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'body',
    required: false,
    includeIfNull: false,
  )


  final String? body;



      /// links.web ?? links.api (033: the confirm page for actions)
  @JsonKey(
    
    name: r'navigate',
    required: true,
    includeIfNull: false,
  )


  final String navigate;



  @JsonKey(
    
    name: r'lang',
    required: false,
    includeIfNull: false,
  )


  final String? lang;



  @JsonKey(
    
    name: r'tag',
    required: false,
    includeIfNull: false,
  )


  final String? tag;



  @JsonKey(
    
    name: r'silent',
    required: false,
    includeIfNull: false,
  )


  final bool? silent;



      /// Foresignal payload minus title/body
  @JsonKey(
    
    name: r'data',
    required: false,
    includeIfNull: false,
  )


  final PushPayload? data;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DeclarativePushPayloadNotification &&
      other.title == title &&
      other.body == body &&
      other.navigate == navigate &&
      other.lang == lang &&
      other.tag == tag &&
      other.silent == silent &&
      other.data == data;

    @override
    int get hashCode =>
        title.hashCode +
        body.hashCode +
        navigate.hashCode +
        lang.hashCode +
        tag.hashCode +
        silent.hashCode +
        data.hashCode;

  factory DeclarativePushPayloadNotification.fromJson(Map<String, dynamic> json) => _$DeclarativePushPayloadNotificationFromJson(json);

  Map<String, dynamic> toJson() => _$DeclarativePushPayloadNotificationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

