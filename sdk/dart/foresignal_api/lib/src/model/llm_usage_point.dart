//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_usage_point.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmUsagePoint {
  /// Returns a new [LlmUsagePoint] instance.
  LlmUsagePoint({

    required  this.t,

    required  this.requests,

    required  this.inputTokens,

    required  this.outputTokens,

     this.cachedTokens,

     this.errors,

     this.refused,
  });

  @JsonKey(
    
    name: r't',
    required: true,
    includeIfNull: false,
  )


  final DateTime t;



  @JsonKey(
    
    name: r'requests',
    required: true,
    includeIfNull: false,
  )


  final int requests;



  @JsonKey(
    
    name: r'inputTokens',
    required: true,
    includeIfNull: false,
  )


  final int inputTokens;



  @JsonKey(
    
    name: r'outputTokens',
    required: true,
    includeIfNull: false,
  )


  final int outputTokens;



  @JsonKey(
    
    name: r'cachedTokens',
    required: false,
    includeIfNull: false,
  )


  final int? cachedTokens;



  @JsonKey(
    
    name: r'errors',
    required: false,
    includeIfNull: false,
  )


  final int? errors;



  @JsonKey(
    
    name: r'refused',
    required: false,
    includeIfNull: false,
  )


  final int? refused;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmUsagePoint &&
      other.t == t &&
      other.requests == requests &&
      other.inputTokens == inputTokens &&
      other.outputTokens == outputTokens &&
      other.cachedTokens == cachedTokens &&
      other.errors == errors &&
      other.refused == refused;

    @override
    int get hashCode =>
        t.hashCode +
        requests.hashCode +
        inputTokens.hashCode +
        outputTokens.hashCode +
        cachedTokens.hashCode +
        errors.hashCode +
        refused.hashCode;

  factory LlmUsagePoint.fromJson(Map<String, dynamic> json) => _$LlmUsagePointFromJson(json);

  Map<String, dynamic> toJson() => _$LlmUsagePointToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

