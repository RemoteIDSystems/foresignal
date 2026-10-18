//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_test_result_embedding.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmTestResultEmbedding {
  /// Returns a new [LlmTestResultEmbedding] instance.
  LlmTestResultEmbedding({

     this.model,

     this.dimensions,

     this.latencyMs,
  });

  @JsonKey(
    
    name: r'model',
    required: false,
    includeIfNull: false,
  )


  final String? model;



  @JsonKey(
    
    name: r'dimensions',
    required: false,
    includeIfNull: false,
  )


  final int? dimensions;



  @JsonKey(
    
    name: r'latencyMs',
    required: false,
    includeIfNull: false,
  )


  final int? latencyMs;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmTestResultEmbedding &&
      other.model == model &&
      other.dimensions == dimensions &&
      other.latencyMs == latencyMs;

    @override
    int get hashCode =>
        model.hashCode +
        dimensions.hashCode +
        latencyMs.hashCode;

  factory LlmTestResultEmbedding.fromJson(Map<String, dynamic> json) => _$LlmTestResultEmbeddingFromJson(json);

  Map<String, dynamic> toJson() => _$LlmTestResultEmbeddingToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

