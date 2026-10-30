//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/briefing_run_delivery_results_inner.dart';
import 'package:foresignal_api/src/model/briefing_run_breaking.dart';
import 'package:foresignal_api/src/model/briefing_run_audio.dart';
import 'package:foresignal_api/src/model/ask_citation.dart';
import 'package:foresignal_api/src/model/digest_stats.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'briefing_run.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BriefingRun {
  /// Returns a new [BriefingRun] instance.
  BriefingRun({

     this.id,

     this.briefingId,

    required  this.status,

    required  this.startedAt,

     this.finishedAt,

    required  this.periodFrom,

    required  this.periodTo,

     this.mode,

    required  this.contentMarkdown,

     this.citations,

     this.stats,

     this.error,

     this.usage,

     this.deliveries,

     this.warnings,

     this.signature,

     this.audio,

     this.deliveryResults,

     this.breaking,
  });

      /// null for previews
  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final String? id;



  @JsonKey(
    
    name: r'briefingId',
    required: false,
    includeIfNull: false,
  )


  final String? briefingId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final BriefingRunStatusEnum status;



  @JsonKey(
    
    name: r'startedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime startedAt;



  @JsonKey(
    
    name: r'finishedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? finishedAt;



  @JsonKey(
    
    name: r'periodFrom',
    required: true,
    includeIfNull: false,
  )


  final DateTime periodFrom;



  @JsonKey(
    
    name: r'periodTo',
    required: true,
    includeIfNull: false,
  )


  final DateTime periodTo;



  @JsonKey(
    
    name: r'mode',
    required: false,
    includeIfNull: false,
  )


  final BriefingRunModeEnum? mode;



  @JsonKey(
    
    name: r'contentMarkdown',
    required: true,
    includeIfNull: false,
  )


  final String contentMarkdown;



  @JsonKey(
    
    name: r'citations',
    required: false,
    includeIfNull: false,
  )


  final List<AskCitation>? citations;



      /// DigestStats of the run: version counts by persist reason (new, changed, removed, edges; routine separately), active entities from record_live, alerts keyed by id, notable records with persisted seqs, health and contributor sections
  @JsonKey(
    
    name: r'stats',
    required: false,
    includeIfNull: false,
  )


  final DigestStats? stats;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;



  @JsonKey(
    
    name: r'usage',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? usage;



  @JsonKey(
    
    name: r'deliveries',
    required: false,
    includeIfNull: false,
  )


  final int? deliveries;



      /// Citation-verifier warnings (LLM mode) and fallback notes
  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<String>? warnings;



      /// Detached JWS (`header..signature`, Ed25519, b64:false) over the stored markdown bytes; verify against /.well-known/jwks.json
  @JsonKey(
    
    name: r'signature',
    required: false,
    includeIfNull: false,
  )


  final String? signature;



  @JsonKey(
    
    name: r'audio',
    required: false,
    includeIfNull: false,
  )


  final BriefingRunAudio? audio;



      /// The outcome of each of the briefing's `deliveryTargets` for this run (NP-004), in target order; null for a briefing without targets, which delivers to its channels as before
  @JsonKey(
    
    name: r'deliveryResults',
    required: false,
    includeIfNull: false,
  )


  final List<BriefingRunDeliveryResultsInner>? deliveryResults;



  @JsonKey(
    
    name: r'breaking',
    required: false,
    includeIfNull: false,
  )


  final BriefingRunBreaking? breaking;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BriefingRun &&
      other.id == id &&
      other.briefingId == briefingId &&
      other.status == status &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt &&
      other.periodFrom == periodFrom &&
      other.periodTo == periodTo &&
      other.mode == mode &&
      other.contentMarkdown == contentMarkdown &&
      other.citations == citations &&
      other.stats == stats &&
      other.error == error &&
      other.usage == usage &&
      other.deliveries == deliveries &&
      other.warnings == warnings &&
      other.signature == signature &&
      other.audio == audio &&
      other.deliveryResults == deliveryResults &&
      other.breaking == breaking;

    @override
    int get hashCode =>
        (id == null ? 0 : id.hashCode) +
        (briefingId == null ? 0 : briefingId.hashCode) +
        status.hashCode +
        startedAt.hashCode +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        periodFrom.hashCode +
        periodTo.hashCode +
        mode.hashCode +
        contentMarkdown.hashCode +
        citations.hashCode +
        stats.hashCode +
        (error == null ? 0 : error.hashCode) +
        (usage == null ? 0 : usage.hashCode) +
        deliveries.hashCode +
        warnings.hashCode +
        (signature == null ? 0 : signature.hashCode) +
        (audio == null ? 0 : audio.hashCode) +
        (deliveryResults == null ? 0 : deliveryResults.hashCode) +
        (breaking == null ? 0 : breaking.hashCode);

  factory BriefingRun.fromJson(Map<String, dynamic> json) => _$BriefingRunFromJson(json);

  Map<String, dynamic> toJson() => _$BriefingRunToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BriefingRunStatusEnum {
@JsonValue(r'running')
running(r'running'),
@JsonValue(r'succeeded')
succeeded(r'succeeded'),
@JsonValue(r'fallback')
fallback(r'fallback'),
@JsonValue(r'failed')
failed(r'failed');

const BriefingRunStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BriefingRunModeEnum {
@JsonValue(r'digest')
digest(r'digest'),
@JsonValue(r'llm')
llm(r'llm');

const BriefingRunModeEnum(this.value);

final String value;

@override
String toString() => value;
}


