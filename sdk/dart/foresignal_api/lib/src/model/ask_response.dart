//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ask_action.dart';
import 'package:foresignal_api/src/model/ask_response_usage.dart';
import 'package:foresignal_api/src/model/ask_citation.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ask_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AskResponse {
  /// Returns a new [AskResponse] instance.
  AskResponse({

    required  this.threadId,

    required  this.answer,

    required  this.citations,

    required  this.proposedActions,

     this.warnings,

     this.mode,

     this.usage,

     this.stopReason,

     this.refusalCategory,
  });

  @JsonKey(
    
    name: r'threadId',
    required: true,
    includeIfNull: false,
  )


  final String threadId;



      /// Markdown
  @JsonKey(
    
    name: r'answer',
    required: true,
    includeIfNull: false,
  )


  final String answer;



  @JsonKey(
    
    name: r'citations',
    required: true,
    includeIfNull: false,
  )


  final List<AskCitation> citations;



  @JsonKey(
    
    name: r'proposedActions',
    required: true,
    includeIfNull: false,
  )


  final List<AskAction> proposedActions;



  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<String>? warnings;



  @JsonKey(
    
    name: r'mode',
    required: false,
    includeIfNull: false,
  )


  final AskResponseModeEnum? mode;



  @JsonKey(
    
    name: r'usage',
    required: false,
    includeIfNull: false,
  )


  final AskResponseUsage? usage;



  @JsonKey(
    
    name: r'stopReason',
    required: false,
    includeIfNull: false,
  )


  final AskResponseStopReasonEnum? stopReason;



      /// Set when the model declined (stopReason=refusal)
  @JsonKey(
    
    name: r'refusalCategory',
    required: false,
    includeIfNull: false,
  )


  final String? refusalCategory;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AskResponse &&
      other.threadId == threadId &&
      other.answer == answer &&
      other.citations == citations &&
      other.proposedActions == proposedActions &&
      other.warnings == warnings &&
      other.mode == mode &&
      other.usage == usage &&
      other.stopReason == stopReason &&
      other.refusalCategory == refusalCategory;

    @override
    int get hashCode =>
        threadId.hashCode +
        answer.hashCode +
        citations.hashCode +
        proposedActions.hashCode +
        warnings.hashCode +
        mode.hashCode +
        usage.hashCode +
        stopReason.hashCode +
        (refusalCategory == null ? 0 : refusalCategory.hashCode);

  factory AskResponse.fromJson(Map<String, dynamic> json) => _$AskResponseFromJson(json);

  Map<String, dynamic> toJson() => _$AskResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AskResponseModeEnum {
@JsonValue(r'tools')
tools(r'tools'),
@JsonValue(r'retrieval')
retrieval(r'retrieval');

const AskResponseModeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AskResponseStopReasonEnum {
@JsonValue(r'end')
end(r'end'),
@JsonValue(r'max_tokens')
maxTokens(r'max_tokens'),
@JsonValue(r'refusal')
refusal(r'refusal'),
@JsonValue(r'error')
error(r'error');

const AskResponseStopReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


