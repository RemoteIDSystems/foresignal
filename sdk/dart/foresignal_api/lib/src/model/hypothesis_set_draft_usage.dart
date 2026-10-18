//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_set_draft_usage.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisSetDraftUsage {
  /// Returns a new [HypothesisSetDraftUsage] instance.
  HypothesisSetDraftUsage({

     this.inputTokens,

     this.outputTokens,

     this.cachedTokens,
  });

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
    
    name: r'cachedTokens',
    required: false,
    includeIfNull: false,
  )


  final int? cachedTokens;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisSetDraftUsage &&
      other.inputTokens == inputTokens &&
      other.outputTokens == outputTokens &&
      other.cachedTokens == cachedTokens;

    @override
    int get hashCode =>
        inputTokens.hashCode +
        outputTokens.hashCode +
        cachedTokens.hashCode;

  factory HypothesisSetDraftUsage.fromJson(Map<String, dynamic> json) => _$HypothesisSetDraftUsageFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisSetDraftUsageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

