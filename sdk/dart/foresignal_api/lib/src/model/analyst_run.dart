//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/analyst_cursor.dart';
import 'package:foresignal_api/src/model/analyst_run_input_counts.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_run.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystRun {
  /// Returns a new [AnalystRun] instance.
  AnalystRun({

    required  this.id,

    required  this.profileId,

    required  this.status,

     this.startedAt,

     this.finishedAt,

     this.cursorFrom,

     this.cursorTo,

     this.inputCounts,

     this.provider,

     this.model,

     this.effort,

     this.tokensIn,

     this.tokensOut,

     this.cachedTokens,

     this.toolCalls,

     this.durationMs,

     this.notes,

     this.proposals,

     this.autoExecuted,

     this.citationsDropped,

     this.claimsDowngraded,

     this.warnings,

     this.reason,

     this.promptHash,

     this.detectorVersion,

     this.content,

     this.manifestId,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// ULID identifier
  @JsonKey(
    
    name: r'profileId',
    required: true,
    includeIfNull: false,
  )


  final String profileId;



      /// queued/running until finished; `noop` made no LLM call
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final AnalystRunStatusEnum status;



  @JsonKey(
    
    name: r'startedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? startedAt;



  @JsonKey(
    
    name: r'finishedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? finishedAt;



  @JsonKey(
    
    name: r'cursorFrom',
    required: false,
    includeIfNull: false,
  )


  final AnalystCursor? cursorFrom;



  @JsonKey(
    
    name: r'cursorTo',
    required: false,
    includeIfNull: false,
  )


  final AnalystCursor? cursorTo;



  @JsonKey(
    
    name: r'inputCounts',
    required: false,
    includeIfNull: false,
  )


  final AnalystRunInputCounts? inputCounts;



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
    
    name: r'effort',
    required: false,
    includeIfNull: false,
  )


  final AnalystRunEffortEnum? effort;



  @JsonKey(
    
    name: r'tokensIn',
    required: false,
    includeIfNull: false,
  )


  final int? tokensIn;



  @JsonKey(
    
    name: r'tokensOut',
    required: false,
    includeIfNull: false,
  )


  final int? tokensOut;



  @JsonKey(
    
    name: r'cachedTokens',
    required: false,
    includeIfNull: false,
  )


  final int? cachedTokens;



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



  @JsonKey(
    
    name: r'notes',
    required: false,
    includeIfNull: false,
  )


  final int? notes;



  @JsonKey(
    
    name: r'proposals',
    required: false,
    includeIfNull: false,
  )


  final int? proposals;



  @JsonKey(
    
    name: r'autoExecuted',
    required: false,
    includeIfNull: false,
  )


  final int? autoExecuted;



  @JsonKey(
    
    name: r'citationsDropped',
    required: false,
    includeIfNull: false,
  )


  final int? citationsDropped;



  @JsonKey(
    
    name: r'claimsDowngraded',
    required: false,
    includeIfNull: false,
  )


  final int? claimsDowngraded;



  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<String>? warnings;



      /// Failure, refusal or budget reason
  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;



      /// sha256 of system prompt + tools + instructions
  @JsonKey(
    
    name: r'promptHash',
    required: false,
    includeIfNull: false,
  )


  final String? promptHash;



  @JsonKey(
    
    name: r'detectorVersion',
    required: false,
    includeIfNull: false,
  )


  final String? detectorVersion;



      /// Prompt and response content, stored only when llm.logContent=true
  @JsonKey(
    
    name: r'content',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? content;



      /// The analysis manifest of this output (prompt 116); null for outputs written before it
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystRun &&
      other.id == id &&
      other.profileId == profileId &&
      other.status == status &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt &&
      other.cursorFrom == cursorFrom &&
      other.cursorTo == cursorTo &&
      other.inputCounts == inputCounts &&
      other.provider == provider &&
      other.model == model &&
      other.effort == effort &&
      other.tokensIn == tokensIn &&
      other.tokensOut == tokensOut &&
      other.cachedTokens == cachedTokens &&
      other.toolCalls == toolCalls &&
      other.durationMs == durationMs &&
      other.notes == notes &&
      other.proposals == proposals &&
      other.autoExecuted == autoExecuted &&
      other.citationsDropped == citationsDropped &&
      other.claimsDowngraded == claimsDowngraded &&
      other.warnings == warnings &&
      other.reason == reason &&
      other.promptHash == promptHash &&
      other.detectorVersion == detectorVersion &&
      other.content == content &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        id.hashCode +
        profileId.hashCode +
        status.hashCode +
        (startedAt == null ? 0 : startedAt.hashCode) +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        cursorFrom.hashCode +
        cursorTo.hashCode +
        inputCounts.hashCode +
        (provider == null ? 0 : provider.hashCode) +
        (model == null ? 0 : model.hashCode) +
        (effort == null ? 0 : effort.hashCode) +
        tokensIn.hashCode +
        tokensOut.hashCode +
        cachedTokens.hashCode +
        toolCalls.hashCode +
        durationMs.hashCode +
        notes.hashCode +
        proposals.hashCode +
        autoExecuted.hashCode +
        citationsDropped.hashCode +
        claimsDowngraded.hashCode +
        warnings.hashCode +
        (reason == null ? 0 : reason.hashCode) +
        (promptHash == null ? 0 : promptHash.hashCode) +
        detectorVersion.hashCode +
        (content == null ? 0 : content.hashCode) +
        (manifestId == null ? 0 : manifestId.hashCode);

  factory AnalystRun.fromJson(Map<String, dynamic> json) => _$AnalystRunFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystRunToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// queued/running until finished; `noop` made no LLM call
enum AnalystRunStatusEnum {
@JsonValue(r'queued')
queued(r'queued'),
@JsonValue(r'running')
running(r'running'),
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'noop')
noop(r'noop'),
@JsonValue(r'partial')
partial(r'partial'),
@JsonValue(r'failed')
failed(r'failed'),
@JsonValue(r'budget_exceeded')
budgetExceeded(r'budget_exceeded');

const AnalystRunStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnalystRunEffortEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'medium')
medium(r'medium');

const AnalystRunEffortEnum(this.value);

final String value;

@override
String toString() => value;
}


