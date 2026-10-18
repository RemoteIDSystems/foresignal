//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/hypothesis_set_input.dart';
import 'package:foresignal_api/src/model/hypothesis_set_draft_warnings_inner.dart';
import 'package:foresignal_api/src/model/hypothesis_set_draft_usage.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_set_draft.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisSetDraft {
  /// Returns a new [HypothesisSetDraft] instance.
  HypothesisSetDraft({

    required  this.set_,

    required  this.valid,

    required  this.warnings,

     this.toolCalls,

     this.model,

     this.effort,

     this.usage,

     this.manifestId,

     this.textOrigin,
  });

  @JsonKey(
    
    name: r'set',
    required: true,
    includeIfNull: false,
  )


  final HypothesisSetInput set_;



      /// Structure, lint and rule specs passed
  @JsonKey(
    
    name: r'valid',
    required: true,
    includeIfNull: false,
  )


  final bool valid;



  @JsonKey(
    
    name: r'warnings',
    required: true,
    includeIfNull: false,
  )


  final List<HypothesisSetDraftWarningsInner> warnings;



      /// Tools called, in order (must start with `get_base_rate`, `find_analogs`)
  @JsonKey(
    
    name: r'toolCalls',
    required: false,
    includeIfNull: false,
  )


  final List<String>? toolCalls;



  @JsonKey(
    
    name: r'model',
    required: false,
    includeIfNull: false,
  )


  final String? model;



      /// LLM effort
  @JsonKey(
    
    name: r'effort',
    required: false,
    includeIfNull: false,
  )


  final HypothesisSetDraftEffortEnum? effort;



  @JsonKey(
    
    name: r'usage',
    required: false,
    includeIfNull: false,
  )


  final HypothesisSetDraftUsage? usage;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;



  @JsonKey(
    
    name: r'textOrigin',
    required: false,
    includeIfNull: false,
  )


  final HypothesisSetDraftTextOriginEnum? textOrigin;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisSetDraft &&
      other.set_ == set_ &&
      other.valid == valid &&
      other.warnings == warnings &&
      other.toolCalls == toolCalls &&
      other.model == model &&
      other.effort == effort &&
      other.usage == usage &&
      other.manifestId == manifestId &&
      other.textOrigin == textOrigin;

    @override
    int get hashCode =>
        set_.hashCode +
        valid.hashCode +
        warnings.hashCode +
        toolCalls.hashCode +
        (model == null ? 0 : model.hashCode) +
        effort.hashCode +
        usage.hashCode +
        (manifestId == null ? 0 : manifestId.hashCode) +
        textOrigin.hashCode;

  factory HypothesisSetDraft.fromJson(Map<String, dynamic> json) => _$HypothesisSetDraftFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisSetDraftToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// LLM effort
enum HypothesisSetDraftEffortEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'medium')
medium(r'medium'),
@JsonValue(r'high')
high(r'high');

const HypothesisSetDraftEffortEnum(this.value);

final String value;

@override
String toString() => value;
}


enum HypothesisSetDraftTextOriginEnum {
@JsonValue(r'llm')
llm(r'llm');

const HypothesisSetDraftTextOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


