//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ask_scope.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ask_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AskRequest {
  /// Returns a new [AskRequest] instance.
  AskRequest({

    required  this.question,

     this.threadId,

     this.scope,

     this.allowActions = false,
  });

  @JsonKey(
    
    name: r'question',
    required: true,
    includeIfNull: false,
  )


  final String question;



  @JsonKey(
    
    name: r'threadId',
    required: false,
    includeIfNull: false,
  )


  final String? threadId;



  @JsonKey(
    
    name: r'scope',
    required: false,
    includeIfNull: false,
  )


  final AskScope? scope;



  @JsonKey(
    defaultValue: false,
    name: r'allowActions',
    required: false,
    includeIfNull: false,
  )


  final bool? allowActions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AskRequest &&
      other.question == question &&
      other.threadId == threadId &&
      other.scope == scope &&
      other.allowActions == allowActions;

    @override
    int get hashCode =>
        question.hashCode +
        (threadId == null ? 0 : threadId.hashCode) +
        scope.hashCode +
        allowActions.hashCode;

  factory AskRequest.fromJson(Map<String, dynamic> json) => _$AskRequestFromJson(json);

  Map<String, dynamic> toJson() => _$AskRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

