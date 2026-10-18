//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'gotify_message.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GotifyMessage {
  /// Returns a new [GotifyMessage] instance.
  GotifyMessage({

    required  this.id,

    required  this.appid,

    required  this.date,

     this.title,

    required  this.message,

     this.priority,

     this.extras,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'appid',
    required: true,
    includeIfNull: false,
  )


  final GotifyMessageAppidEnum appid;



  @JsonKey(
    
    name: r'date',
    required: true,
    includeIfNull: false,
  )


  final DateTime date;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'message',
    required: true,
    includeIfNull: false,
  )


  final String message;



  @JsonKey(
    
    name: r'priority',
    required: false,
    includeIfNull: false,
  )


  final int? priority;



  @JsonKey(
    
    name: r'extras',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? extras;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GotifyMessage &&
      other.id == id &&
      other.appid == appid &&
      other.date == date &&
      other.title == title &&
      other.message == message &&
      other.priority == priority &&
      other.extras == extras;

    @override
    int get hashCode =>
        id.hashCode +
        appid.hashCode +
        date.hashCode +
        title.hashCode +
        message.hashCode +
        priority.hashCode +
        extras.hashCode;

  factory GotifyMessage.fromJson(Map<String, dynamic> json) => _$GotifyMessageFromJson(json);

  Map<String, dynamic> toJson() => _$GotifyMessageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum GotifyMessageAppidEnum {
@JsonValue(1)
number1('1');

const GotifyMessageAppidEnum(this.value);

final String value;

@override
String toString() => value;
}


