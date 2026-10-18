//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_test_result_chat.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmTestResultChat {
  /// Returns a new [LlmTestResultChat] instance.
  LlmTestResultChat({

     this.model,

     this.latencyMs,

     this.inputTokens,

     this.outputTokens,

     this.text,

     this.cachedTokens,

     this.stopReason,
  });

  @JsonKey(
    
    name: r'model',
    required: false,
    includeIfNull: false,
  )


  final String? model;



  @JsonKey(
    
    name: r'latencyMs',
    required: false,
    includeIfNull: false,
  )


  final int? latencyMs;



  @JsonKey(
    
    name: r'inputTokens',
    required: false,
    includeIfNull: false,
  )


  final int? inputTokens;



  @JsonKey(
    
    name: r'outputTokens',
    required: false,
    includeIfNull: false,
  )


  final int? outputTokens;



  @JsonKey(
    
    name: r'text',
    required: false,
    includeIfNull: false,
  )


  final String? text;



  @JsonKey(
    
    name: r'cachedTokens',
    required: false,
    includeIfNull: false,
  )


  final int? cachedTokens;



  @JsonKey(
    
    name: r'stopReason',
    required: false,
    includeIfNull: false,
  )


  final LlmTestResultChatStopReasonEnum? stopReason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmTestResultChat &&
      other.model == model &&
      other.latencyMs == latencyMs &&
      other.inputTokens == inputTokens &&
      other.outputTokens == outputTokens &&
      other.text == text &&
      other.cachedTokens == cachedTokens &&
      other.stopReason == stopReason;

    @override
    int get hashCode =>
        model.hashCode +
        latencyMs.hashCode +
        inputTokens.hashCode +
        outputTokens.hashCode +
        text.hashCode +
        cachedTokens.hashCode +
        stopReason.hashCode;

  factory LlmTestResultChat.fromJson(Map<String, dynamic> json) => _$LlmTestResultChatFromJson(json);

  Map<String, dynamic> toJson() => _$LlmTestResultChatToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum LlmTestResultChatStopReasonEnum {
@JsonValue(r'end')
end(r'end'),
@JsonValue(r'tool_use')
toolUse(r'tool_use'),
@JsonValue(r'max_tokens')
maxTokens(r'max_tokens'),
@JsonValue(r'refusal')
refusal(r'refusal'),
@JsonValue(r'error')
error(r'error');

const LlmTestResultChatStopReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


