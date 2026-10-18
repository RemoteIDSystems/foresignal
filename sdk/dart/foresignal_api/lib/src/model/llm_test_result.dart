//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/llm_test_result_chat.dart';
import 'package:foresignal_api/src/model/llm_test_result_embedding.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_test_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmTestResult {
  /// Returns a new [LlmTestResult] instance.
  LlmTestResult({

    required  this.success,

     this.chat,

     this.embedding,

     this.error,

     this.refusalCategory,
  });

  @JsonKey(
    
    name: r'success',
    required: true,
    includeIfNull: false,
  )


  final bool success;



  @JsonKey(
    
    name: r'chat',
    required: false,
    includeIfNull: false,
  )


  final LlmTestResultChat? chat;



  @JsonKey(
    
    name: r'embedding',
    required: false,
    includeIfNull: false,
  )


  final LlmTestResultEmbedding? embedding;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;



  @JsonKey(
    
    name: r'refusalCategory',
    required: false,
    includeIfNull: false,
  )


  final String? refusalCategory;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmTestResult &&
      other.success == success &&
      other.chat == chat &&
      other.embedding == embedding &&
      other.error == error &&
      other.refusalCategory == refusalCategory;

    @override
    int get hashCode =>
        success.hashCode +
        chat.hashCode +
        (embedding == null ? 0 : embedding.hashCode) +
        (error == null ? 0 : error.hashCode) +
        (refusalCategory == null ? 0 : refusalCategory.hashCode);

  factory LlmTestResult.fromJson(Map<String, dynamic> json) => _$LlmTestResultFromJson(json);

  Map<String, dynamic> toJson() => _$LlmTestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

