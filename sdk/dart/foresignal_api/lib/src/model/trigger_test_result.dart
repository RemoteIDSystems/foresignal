//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/trigger_test_result_warnings_inner.dart';
import 'package:foresignal_api/src/model/record.dart';
import 'package:foresignal_api/src/model/trigger_test_result_window_evaluations_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'trigger_test_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TriggerTestResult {
  /// Returns a new [TriggerTestResult] instance.
  TriggerTestResult({

    required  this.ruleId,

     this.evaluatedFrom,

    required  this.matches,

     this.truncated,

     this.fromSeq,

     this.toSeq,

     this.matchCount,

     this.simulated,

     this.wouldFire,

     this.wouldSuppress,

     this.truncatedByRetention,

     this.enrich,

     this.warnings,

     this.approximate,

     this.approximateReason,

     this.windowEvaluations,
  });

  @JsonKey(
    
    name: r'ruleId',
    required: true,
    includeIfNull: false,
  )


  final String ruleId;



  @JsonKey(
    
    name: r'evaluatedFrom',
    required: false,
    includeIfNull: false,
  )


  final DateTime? evaluatedFrom;



  @JsonKey(
    
    name: r'matches',
    required: true,
    includeIfNull: false,
  )


  final List<Record> matches;



  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;



  @JsonKey(
    
    name: r'fromSeq',
    required: false,
    includeIfNull: false,
  )


  final int? fromSeq;



  @JsonKey(
    
    name: r'toSeq',
    required: false,
    includeIfNull: false,
  )


  final int? toSeq;



          // minimum: 0
  @JsonKey(
    
    name: r'matchCount',
    required: false,
    includeIfNull: false,
  )


  final int? matchCount;



      /// True when simulate=true (cooldown, cap and active windows applied)
  @JsonKey(
    
    name: r'simulated',
    required: false,
    includeIfNull: false,
  )


  final bool? simulated;



          // minimum: 0
  @JsonKey(
    
    name: r'wouldFire',
    required: false,
    includeIfNull: false,
  )


  final int? wouldFire;



          // minimum: 0
  @JsonKey(
    
    name: r'wouldSuppress',
    required: false,
    includeIfNull: false,
  )


  final int? wouldSuppress;



      /// fromSeq is below RetentionState.lowestRetainedSeq
  @JsonKey(
    
    name: r'truncatedByRetention',
    required: false,
    includeIfNull: false,
  )


  final bool? truncatedByRetention;



      /// True when the rule would enrich; no LLM is called by testTrigger
  @JsonKey(
    
    name: r'enrich',
    required: false,
    includeIfNull: false,
  )


  final bool? enrich;



      /// Warnings, e.g. DOWNSAMPLED_RANGE: match-mode results over downsampled history under-count; edge reasons are preserved
  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<TriggerTestResultWarningsInner>? warnings;



      /// True for windowed types (versions only)
  @JsonKey(
    
    name: r'approximate',
    required: false,
    includeIfNull: false,
  )


  final bool? approximate;



  @JsonKey(
    
    name: r'approximateReason',
    required: false,
    includeIfNull: false,
  )


  final String? approximateReason;



      /// Windowed types only
  @JsonKey(
    
    name: r'windowEvaluations',
    required: false,
    includeIfNull: false,
  )


  final List<TriggerTestResultWindowEvaluationsInner>? windowEvaluations;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TriggerTestResult &&
      other.ruleId == ruleId &&
      other.evaluatedFrom == evaluatedFrom &&
      other.matches == matches &&
      other.truncated == truncated &&
      other.fromSeq == fromSeq &&
      other.toSeq == toSeq &&
      other.matchCount == matchCount &&
      other.simulated == simulated &&
      other.wouldFire == wouldFire &&
      other.wouldSuppress == wouldSuppress &&
      other.truncatedByRetention == truncatedByRetention &&
      other.enrich == enrich &&
      other.warnings == warnings &&
      other.approximate == approximate &&
      other.approximateReason == approximateReason &&
      other.windowEvaluations == windowEvaluations;

    @override
    int get hashCode =>
        ruleId.hashCode +
        evaluatedFrom.hashCode +
        matches.hashCode +
        truncated.hashCode +
        fromSeq.hashCode +
        toSeq.hashCode +
        matchCount.hashCode +
        simulated.hashCode +
        wouldFire.hashCode +
        wouldSuppress.hashCode +
        truncatedByRetention.hashCode +
        enrich.hashCode +
        warnings.hashCode +
        approximate.hashCode +
        (approximateReason == null ? 0 : approximateReason.hashCode) +
        windowEvaluations.hashCode;

  factory TriggerTestResult.fromJson(Map<String, dynamic> json) => _$TriggerTestResultFromJson(json);

  Map<String, dynamic> toJson() => _$TriggerTestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

