//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'gotify_message_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GotifyMessageInput {
  /// Returns a new [GotifyMessageInput] instance.
  GotifyMessageInput({

     this.title,

    required  this.message,

     this.priority,

     this.extras,
  });

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



          // minimum: 0
          // maximum: 10
  @JsonKey(
    
    name: r'priority',
    required: false,
    includeIfNull: false,
  )


  final int? priority;



      /// `extras[\"foresignal::record\"].externalId` sets the externalId
  @JsonKey(
    
    name: r'extras',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? extras;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GotifyMessageInput &&
      other.title == title &&
      other.message == message &&
      other.priority == priority &&
      other.extras == extras;

    @override
    int get hashCode =>
        title.hashCode +
        message.hashCode +
        priority.hashCode +
        extras.hashCode;

  factory GotifyMessageInput.fromJson(Map<String, dynamic> json) => _$GotifyMessageInputFromJson(json);

  Map<String, dynamic> toJson() => _$GotifyMessageInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

