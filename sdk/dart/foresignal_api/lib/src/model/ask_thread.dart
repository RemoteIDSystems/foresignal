//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ask_action.dart';
import 'package:foresignal_api/src/model/ask_message.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ask_thread.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AskThread {
  /// Returns a new [AskThread] instance.
  AskThread({

    required  this.id,

    required  this.title,

    required  this.createdAt,

     this.updatedAt,

     this.messages,

     this.actions,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



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
    
    name: r'messages',
    required: false,
    includeIfNull: false,
  )


  final List<AskMessage>? messages;



  @JsonKey(
    
    name: r'actions',
    required: false,
    includeIfNull: false,
  )


  final List<AskAction>? actions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AskThread &&
      other.id == id &&
      other.title == title &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt &&
      other.messages == messages &&
      other.actions == actions;

    @override
    int get hashCode =>
        id.hashCode +
        title.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode +
        messages.hashCode +
        actions.hashCode;

  factory AskThread.fromJson(Map<String, dynamic> json) => _$AskThreadFromJson(json);

  Map<String, dynamic> toJson() => _$AskThreadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

