//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ask_response_usage.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AskResponseUsage {
  /// Returns a new [AskResponseUsage] instance.
  AskResponseUsage({

     this.provider,

     this.model,

     this.inputTokens,

     this.outputTokens,

     this.toolCalls,

     this.durationMs,
  });

  @JsonKey(
    
    name: r'provider',
    required: false,
    includeIfNull: false,
  )


  final String? provider;



  @JsonKey(
    
    name: r'model',
    required: false,
    includeIfNull: false,
  )


  final String? model;



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
    
    name: r'toolCalls',
    required: false,
    includeIfNull: false,
  )


  final int? toolCalls;



  @JsonKey(
    
    name: r'durationMs',
    required: false,
    includeIfNull: false,
  )


  final int? durationMs;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AskResponseUsage &&
      other.provider == provider &&
      other.model == model &&
      other.inputTokens == inputTokens &&
      other.outputTokens == outputTokens &&
      other.toolCalls == toolCalls &&
      other.durationMs == durationMs;

    @override
    int get hashCode =>
        provider.hashCode +
        model.hashCode +
        inputTokens.hashCode +
        outputTokens.hashCode +
        toolCalls.hashCode +
        durationMs.hashCode;

  factory AskResponseUsage.fromJson(Map<String, dynamic> json) => _$AskResponseUsageFromJson(json);

  Map<String, dynamic> toJson() => _$AskResponseUsageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

