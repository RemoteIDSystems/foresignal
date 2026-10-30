//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/problem_errors_inner.dart';
import 'package:foresignal_api/src/model/problem_references.dart';
import 'package:foresignal_api/src/model/ask_action.dart';
import 'package:foresignal_api/src/model/ask_delta_event.dart';
import 'package:foresignal_api/src/model/ask_response_usage.dart';
import 'package:foresignal_api/src/model/ask_citation.dart';
import 'package:foresignal_api/src/model/ask_progress_event.dart';
import 'package:foresignal_api/src/model/ask_warning_event.dart';
import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/ask_citation_corroboration.dart';
import 'package:foresignal_api/src/model/ask_response.dart';
import 'package:foresignal_api/src/model/ask_citation_provenance.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ask_stream200_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AskStream200Response {
  /// Returns a new [AskStream200Response] instance.
  AskStream200Response({

    required  this.tool,

    required  this.status,

     this.summary,

    required  this.text,

    required  this.id,

     this.seq,

    required  this.title,

    required  this.type,

     this.productId,

     this.frameKey,

     this.run,

     this.step,

     this.validTime,

     this.ageMinutes,

     this.provenance,

     this.corroboration,

    required  this.message,

    required  this.description,

     this.payload,

     this.result,

     this.createdAt,

     this.executedAt,

    required  this.threadId,

    required  this.answer,

    required  this.citations,

    required  this.proposedActions,

     this.warnings,

     this.mode,

     this.usage,

     this.stopReason,

     this.refusalCategory,

     this.detail,

     this.instance,

     this.code,

     this.errors,

     this.purgeHorizon,

     this.watermarkSeq,

     this.horizon,

     this.capUsd,

     this.spentUsd,

     this.estimateUsd,

     this.requiredScope,

     this.class_,

     this.retryAfterSeconds,

     this.references,

     this.reason,
  });

  @JsonKey(
    
    name: r'tool',
    required: true,
    includeIfNull: false,
  )


  final String tool;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final int status;



  @JsonKey(
    
    name: r'summary',
    required: false,
    includeIfNull: false,
  )


  final String? summary;



  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'seq',
    required: false,
    includeIfNull: false,
  )


  final int? seq;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final String type;



  @JsonKey(
    
    name: r'productId',
    required: false,
    includeIfNull: false,
  )


  final String? productId;



  @JsonKey(
    
    name: r'frameKey',
    required: false,
    includeIfNull: false,
  )


  final String? frameKey;



  @JsonKey(
    
    name: r'run',
    required: false,
    includeIfNull: false,
  )


  final String? run;



  @JsonKey(
    
    name: r'step',
    required: false,
    includeIfNull: false,
  )


  final int? step;



  @JsonKey(
    
    name: r'validTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validTime;



  @JsonKey(
    
    name: r'ageMinutes',
    required: false,
    includeIfNull: false,
  )


  final num? ageMinutes;



  @JsonKey(
    
    name: r'provenance',
    required: false,
    includeIfNull: false,
  )


  final AskCitationProvenance? provenance;



  @JsonKey(
    
    name: r'corroboration',
    required: false,
    includeIfNull: false,
  )


  final AskCitationCorroboration? corroboration;



  @JsonKey(
    
    name: r'message',
    required: true,
    includeIfNull: false,
  )


  final String message;



  @JsonKey(
    
    name: r'description',
    required: true,
    includeIfNull: false,
  )


  final String description;



  @JsonKey(
    
    name: r'payload',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? payload;



  @JsonKey(
    
    name: r'result',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? result;



  @JsonKey(
    
    name: r'createdAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? createdAt;



  @JsonKey(
    
    name: r'executedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? executedAt;



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


  final AskStream200ResponseModeEnum? mode;



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


  final AskStream200ResponseStopReasonEnum? stopReason;



      /// Set when the model declined (stopReason=refusal)
  @JsonKey(
    
    name: r'refusalCategory',
    required: false,
    includeIfNull: false,
  )


  final String? refusalCategory;



  @JsonKey(
    
    name: r'detail',
    required: false,
    includeIfNull: false,
  )


  final String? detail;



  @JsonKey(
    
    name: r'instance',
    required: false,
    includeIfNull: false,
  )


  final String? instance;



      /// Stable machine code
  @JsonKey(
    
    name: r'code',
    required: false,
    includeIfNull: false,
  )


  final String? code;



  @JsonKey(
    
    name: r'errors',
    required: false,
    includeIfNull: false,
  )


  final List<ProblemErrorsInner>? errors;



      /// RESYNC_REQUIRED: purge horizon seq
  @JsonKey(
    
    name: r'purgeHorizon',
    required: false,
    includeIfNull: false,
  )


  final int? purgeHorizon;



      /// RESYNC_REQUIRED: current watermark
  @JsonKey(
    
    name: r'watermarkSeq',
    required: false,
    includeIfNull: false,
  )


  final int? watermarkSeq;



      /// ASOF_BEYOND_RETENTION: earliest supported asOf
  @JsonKey(
    
    name: r'horizon',
    required: false,
    includeIfNull: false,
  )


  final DateTime? horizon;



      /// BUDGET_EXCEEDED: monthly money cap
  @JsonKey(
    
    name: r'capUsd',
    required: false,
    includeIfNull: false,
  )


  final num? capUsd;



      /// BUDGET_EXCEEDED: spent this month
  @JsonKey(
    
    name: r'spentUsd',
    required: false,
    includeIfNull: false,
  )


  final num? spentUsd;



      /// BUDGET_EXCEEDED: estimated cost of the refused call
  @JsonKey(
    
    name: r'estimateUsd',
    required: false,
    includeIfNull: false,
  )


  final num? estimateUsd;



      /// INSUFFICIENT_SCOPE: the scope the operation needs (023)
  @JsonKey(
    
    name: r'requiredScope',
    required: false,
    includeIfNull: false,
  )


  final AskStream200ResponseRequiredScopeEnum? requiredScope;



      /// RATE_LIMITED: the rate-limit class that refused the request (035)
  @JsonKey(
    
    name: r'class',
    required: false,
    includeIfNull: false,
  )


  final AskStream200ResponseClass_Enum? class_;



      /// RATE_LIMITED: seconds to wait, as in Retry-After (035)
  @JsonKey(
    
    name: r'retryAfterSeconds',
    required: false,
    includeIfNull: false,
  )


  final int? retryAfterSeconds;



  @JsonKey(
    
    name: r'references',
    required: false,
    includeIfNull: false,
  )


  final ProblemReferences? references;



      /// VALIDATION_FAILED from ImageGuard (043): the refusal (file-too-large, unknown-format, bigtiff, content-type-mismatch, truncated, side-too-long, too-many-pixels, multi-image, unreadable)
  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AskStream200Response &&
      other.tool == tool &&
      other.status == status &&
      other.summary == summary &&
      other.text == text &&
      other.id == id &&
      other.seq == seq &&
      other.title == title &&
      other.type == type &&
      other.productId == productId &&
      other.frameKey == frameKey &&
      other.run == run &&
      other.step == step &&
      other.validTime == validTime &&
      other.ageMinutes == ageMinutes &&
      other.provenance == provenance &&
      other.corroboration == corroboration &&
      other.message == message &&
      other.description == description &&
      other.payload == payload &&
      other.result == result &&
      other.createdAt == createdAt &&
      other.executedAt == executedAt &&
      other.threadId == threadId &&
      other.answer == answer &&
      other.citations == citations &&
      other.proposedActions == proposedActions &&
      other.warnings == warnings &&
      other.mode == mode &&
      other.usage == usage &&
      other.stopReason == stopReason &&
      other.refusalCategory == refusalCategory &&
      other.detail == detail &&
      other.instance == instance &&
      other.code == code &&
      other.errors == errors &&
      other.purgeHorizon == purgeHorizon &&
      other.watermarkSeq == watermarkSeq &&
      other.horizon == horizon &&
      other.capUsd == capUsd &&
      other.spentUsd == spentUsd &&
      other.estimateUsd == estimateUsd &&
      other.requiredScope == requiredScope &&
      other.class_ == class_ &&
      other.retryAfterSeconds == retryAfterSeconds &&
      other.references == references &&
      other.reason == reason;

    @override
    int get hashCode =>
        tool.hashCode +
        status.hashCode +
        (summary == null ? 0 : summary.hashCode) +
        text.hashCode +
        id.hashCode +
        (seq == null ? 0 : seq.hashCode) +
        title.hashCode +
        type.hashCode +
        (productId == null ? 0 : productId.hashCode) +
        (frameKey == null ? 0 : frameKey.hashCode) +
        (run == null ? 0 : run.hashCode) +
        (step == null ? 0 : step.hashCode) +
        (validTime == null ? 0 : validTime.hashCode) +
        (ageMinutes == null ? 0 : ageMinutes.hashCode) +
        (provenance == null ? 0 : provenance.hashCode) +
        (corroboration == null ? 0 : corroboration.hashCode) +
        message.hashCode +
        description.hashCode +
        payload.hashCode +
        (result == null ? 0 : result.hashCode) +
        createdAt.hashCode +
        (executedAt == null ? 0 : executedAt.hashCode) +
        threadId.hashCode +
        answer.hashCode +
        citations.hashCode +
        proposedActions.hashCode +
        warnings.hashCode +
        mode.hashCode +
        usage.hashCode +
        stopReason.hashCode +
        (refusalCategory == null ? 0 : refusalCategory.hashCode) +
        detail.hashCode +
        instance.hashCode +
        code.hashCode +
        errors.hashCode +
        (purgeHorizon == null ? 0 : purgeHorizon.hashCode) +
        (watermarkSeq == null ? 0 : watermarkSeq.hashCode) +
        (horizon == null ? 0 : horizon.hashCode) +
        (capUsd == null ? 0 : capUsd.hashCode) +
        (spentUsd == null ? 0 : spentUsd.hashCode) +
        (estimateUsd == null ? 0 : estimateUsd.hashCode) +
        (requiredScope == null ? 0 : requiredScope.hashCode) +
        (class_ == null ? 0 : class_.hashCode) +
        (retryAfterSeconds == null ? 0 : retryAfterSeconds.hashCode) +
        (references == null ? 0 : references.hashCode) +
        (reason == null ? 0 : reason.hashCode);

  factory AskStream200Response.fromJson(Map<String, dynamic> json) => _$AskStream200ResponseFromJson(json);

  Map<String, dynamic> toJson() => _$AskStream200ResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AskStream200ResponseModeEnum {
@JsonValue(r'tools')
tools(r'tools'),
@JsonValue(r'retrieval')
retrieval(r'retrieval');

const AskStream200ResponseModeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AskStream200ResponseStopReasonEnum {
@JsonValue(r'end')
end(r'end'),
@JsonValue(r'max_tokens')
maxTokens(r'max_tokens'),
@JsonValue(r'refusal')
refusal(r'refusal'),
@JsonValue(r'error')
error(r'error');

const AskStream200ResponseStopReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


/// INSUFFICIENT_SCOPE: the scope the operation needs (023)
enum AskStream200ResponseRequiredScopeEnum {
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'admin')
admin(r'admin');

const AskStream200ResponseRequiredScopeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// RATE_LIMITED: the rate-limit class that refused the request (035)
enum AskStream200ResponseClass_Enum {
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'tiles')
tiles(r'tiles'),
@JsonValue(r'stream')
stream(r'stream'),
@JsonValue(r'expensive')
expensive(r'expensive'),
@JsonValue(r'write')
write(r'write'),
@JsonValue(r'anonymous')
anonymous(r'anonymous'),
@JsonValue(r'metrics')
metrics(r'metrics');

const AskStream200ResponseClass_Enum(this.value);

final String value;

@override
String toString() => value;
}


