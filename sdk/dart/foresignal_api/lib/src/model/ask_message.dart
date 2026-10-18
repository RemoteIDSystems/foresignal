//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ask_citation.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ask_message.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AskMessage {
  /// Returns a new [AskMessage] instance.
  AskMessage({

    required  this.role,

    required  this.content,

     this.citations,

    required  this.createdAt,

     this.warnings,
  });

  @JsonKey(
    
    name: r'role',
    required: true,
    includeIfNull: false,
  )


  final AskMessageRoleEnum role;



  @JsonKey(
    
    name: r'content',
    required: true,
    includeIfNull: false,
  )


  final String content;



  @JsonKey(
    
    name: r'citations',
    required: false,
    includeIfNull: false,
  )


  final List<AskCitation>? citations;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



      /// Dropped citations and uncited-claim warnings, persisted with the message
  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<String>? warnings;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AskMessage &&
      other.role == role &&
      other.content == content &&
      other.citations == citations &&
      other.createdAt == createdAt &&
      other.warnings == warnings;

    @override
    int get hashCode =>
        role.hashCode +
        content.hashCode +
        citations.hashCode +
        createdAt.hashCode +
        warnings.hashCode;

  factory AskMessage.fromJson(Map<String, dynamic> json) => _$AskMessageFromJson(json);

  Map<String, dynamic> toJson() => _$AskMessageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AskMessageRoleEnum {
@JsonValue(r'user')
user(r'user'),
@JsonValue(r'assistant')
assistant(r'assistant');

const AskMessageRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


