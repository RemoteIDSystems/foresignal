//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/alert_signal.dart';
import 'package:foresignal_api/src/model/alert.dart';
import 'package:foresignal_api/src/model/alert_context.dart';
import 'package:foresignal_api/src/model/alert_event.dart';
import 'package:foresignal_api/src/model/ask_citation.dart';
import 'package:foresignal_api/src/model/region_tags.dart';
import 'package:foresignal_api/src/model/record.dart';
import 'package:foresignal_api/src/model/alert_record_snapshot.dart';
import 'package:foresignal_api/src/model/reset_event.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stream_alerts200_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StreamAlerts200Response {
  /// Returns a new [StreamAlerts200Response] instance.
  StreamAlerts200Response({

    required  this.type,

    required  this.alert,

     this.suppressed = false,

     this.exercise = false,

    required  this.reason,

     this.resumeAfter,

     this.watermarkSeq,

    required  this.id,

    required  this.ruleId,

     this.ruleName,

    required  this.seq,

    required  this.source_,

    required  this.externalId,

     this.kind,

    required  this.firedAt,

    required  this.message,

     this.acknowledgedAt,

    required  this.severity,

     this.summary,

     this.enrichedAt,

     this.record,

    required  this.rev,

    required  this.state,

     this.observedAt,

     this.acknowledgedBy,

     this.resolvedAt,

     this.resolvedBy,

     this.resolvedSeq,

     this.feedback,

     this.feedbackAt,

     this.note,

     this.snoozedUntil,

     this.suppressedReason,

     this.tags,

     this.recordSnapshot,

     this.summaryCitations,

     this.sensitive,

     this.region,

     this.context,

     this.exerciseRunId,

     this.signals,
  });

      /// Lifecycle transition; SSE maps fired → `alert`, resolved → `alert.resolved`, all others → `alert.updated`
  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final StreamAlerts200ResponseTypeEnum type;



  @JsonKey(
    
    name: r'alert',
    required: true,
    includeIfNull: false,
  )


  final Alert alert;



  @JsonKey(
    defaultValue: false,
    name: r'suppressed',
    required: false,
    includeIfNull: false,
  )


  final bool? suppressed;



      /// Exercise replay alert (never counted in real state or statistics)
  @JsonKey(
    defaultValue: false,
    name: r'exercise',
    required: false,
    includeIfNull: false,
  )


  final bool? exercise;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final StreamAlerts200ResponseReasonEnum reason;



      /// Last delivered seq to resume from (null for resync_required)
  @JsonKey(
    
    name: r'resumeAfter',
    required: false,
    includeIfNull: false,
  )


  final int? resumeAfter;



  @JsonKey(
    
    name: r'watermarkSeq',
    required: false,
    includeIfNull: false,
  )


  final int? watermarkSeq;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'ruleId',
    required: true,
    includeIfNull: false,
  )


  final String ruleId;



      /// The rule's current name; null when the rule was deleted (its alerts are kept)
  @JsonKey(
    
    name: r'ruleName',
    required: false,
    includeIfNull: false,
  )


  final String? ruleName;



  @JsonKey(
    
    name: r'seq',
    required: true,
    includeIfNull: false,
  )


  final int seq;



  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final String source_;



  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;



  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final String? kind;



  @JsonKey(
    
    name: r'firedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime firedAt;



  @JsonKey(
    
    name: r'message',
    required: true,
    includeIfNull: false,
  )


  final String message;



  @JsonKey(
    
    name: r'acknowledgedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? acknowledgedAt;



  @JsonKey(
    
    name: r'severity',
    required: true,
    includeIfNull: false,
  )


  final StreamAlerts200ResponseSeverityEnum severity;



      /// LLM context summary when the rule has enrich=true (030): at most about 120 words with [source:externalId@seq] and [alert:id] citations (unverifiable ones removed); null until enriched, and when enrichment was skipped or failed
  @JsonKey(
    
    name: r'summary',
    required: false,
    includeIfNull: false,
  )


  final String? summary;



      /// When the summary was stored (030); null until enriched
  @JsonKey(
    
    name: r'enrichedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? enrichedAt;



      /// The version (seq) if still retained; otherwise use recordSnapshot
  @JsonKey(
    
    name: r'record',
    required: false,
    includeIfNull: false,
  )


  final Record? record;



      /// Monotonic revision (SSE id on /v1/alerts/stream); bumps on every change
  @JsonKey(
    
    name: r'rev',
    required: true,
    includeIfNull: false,
  )


  final int rev;



  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final StreamAlerts200ResponseStateEnum state;



      /// observedAt of the matching version (cooldown axis)
  @JsonKey(
    
    name: r'observedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? observedAt;



      /// Token name
  @JsonKey(
    
    name: r'acknowledgedBy',
    required: false,
    includeIfNull: false,
  )


  final String? acknowledgedBy;



  @JsonKey(
    
    name: r'resolvedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? resolvedAt;



      /// inverse_edge, tombstone, expiry, stale, admin or token:<name>
  @JsonKey(
    
    name: r'resolvedBy',
    required: false,
    includeIfNull: false,
  )


  final String? resolvedBy;



  @JsonKey(
    
    name: r'resolvedSeq',
    required: false,
    includeIfNull: false,
  )


  final int? resolvedSeq;



  @JsonKey(
    
    name: r'feedback',
    required: false,
    includeIfNull: false,
  )


  final StreamAlerts200ResponseFeedbackEnum? feedback;



  @JsonKey(
    
    name: r'feedbackAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? feedbackAt;



  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;



  @JsonKey(
    
    name: r'snoozedUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? snoozedUntil;



  @JsonKey(
    
    name: r'suppressedReason',
    required: false,
    includeIfNull: false,
  )


  final StreamAlerts200ResponseSuppressedReasonEnum? suppressedReason;



  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



  @JsonKey(
    
    name: r'recordSnapshot',
    required: false,
    includeIfNull: false,
  )


  final AlertRecordSnapshot? recordSnapshot;



      /// Verified citations of the enrichment summary (030); empty until enriched
  @JsonKey(
    
    name: r'summaryCitations',
    required: false,
    includeIfNull: false,
  )


  final List<AskCitation>? summaryCitations;



      /// Alert from a rule referencing a watchlist (036): hidden from anonymous and feed-key principals
  @JsonKey(
    
    name: r'sensitive',
    required: false,
    includeIfNull: false,
  )


  final bool? sensitive;



      /// Region tags snapshotted at fire time (040)
  @JsonKey(
    
    name: r'region',
    required: false,
    includeIfNull: false,
  )


  final RegionTags? region;



  @JsonKey(
    
    name: r'context',
    required: false,
    includeIfNull: false,
  )


  final AlertContext? context;



      /// ULID identifier
  @JsonKey(
    
    name: r'exerciseRunId',
    required: false,
    includeIfNull: false,
  )


  final String? exerciseRunId;



      /// Implicit quality signals recorded for this alert
  @JsonKey(
    
    name: r'signals',
    required: false,
    includeIfNull: false,
  )


  final List<AlertSignal>? signals;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StreamAlerts200Response &&
      other.type == type &&
      other.alert == alert &&
      other.suppressed == suppressed &&
      other.exercise == exercise &&
      other.reason == reason &&
      other.resumeAfter == resumeAfter &&
      other.watermarkSeq == watermarkSeq &&
      other.id == id &&
      other.ruleId == ruleId &&
      other.ruleName == ruleName &&
      other.seq == seq &&
      other.source_ == source_ &&
      other.externalId == externalId &&
      other.kind == kind &&
      other.firedAt == firedAt &&
      other.message == message &&
      other.acknowledgedAt == acknowledgedAt &&
      other.severity == severity &&
      other.summary == summary &&
      other.enrichedAt == enrichedAt &&
      other.record == record &&
      other.rev == rev &&
      other.state == state &&
      other.observedAt == observedAt &&
      other.acknowledgedBy == acknowledgedBy &&
      other.resolvedAt == resolvedAt &&
      other.resolvedBy == resolvedBy &&
      other.resolvedSeq == resolvedSeq &&
      other.feedback == feedback &&
      other.feedbackAt == feedbackAt &&
      other.note == note &&
      other.snoozedUntil == snoozedUntil &&
      other.suppressedReason == suppressedReason &&
      other.tags == tags &&
      other.recordSnapshot == recordSnapshot &&
      other.summaryCitations == summaryCitations &&
      other.sensitive == sensitive &&
      other.region == region &&
      other.context == context &&
      other.exerciseRunId == exerciseRunId &&
      other.signals == signals;

    @override
    int get hashCode =>
        type.hashCode +
        alert.hashCode +
        suppressed.hashCode +
        exercise.hashCode +
        reason.hashCode +
        (resumeAfter == null ? 0 : resumeAfter.hashCode) +
        (watermarkSeq == null ? 0 : watermarkSeq.hashCode) +
        id.hashCode +
        ruleId.hashCode +
        (ruleName == null ? 0 : ruleName.hashCode) +
        seq.hashCode +
        source_.hashCode +
        externalId.hashCode +
        kind.hashCode +
        firedAt.hashCode +
        message.hashCode +
        (acknowledgedAt == null ? 0 : acknowledgedAt.hashCode) +
        severity.hashCode +
        (summary == null ? 0 : summary.hashCode) +
        (enrichedAt == null ? 0 : enrichedAt.hashCode) +
        record.hashCode +
        rev.hashCode +
        state.hashCode +
        observedAt.hashCode +
        (acknowledgedBy == null ? 0 : acknowledgedBy.hashCode) +
        (resolvedAt == null ? 0 : resolvedAt.hashCode) +
        (resolvedBy == null ? 0 : resolvedBy.hashCode) +
        (resolvedSeq == null ? 0 : resolvedSeq.hashCode) +
        (feedback == null ? 0 : feedback.hashCode) +
        (feedbackAt == null ? 0 : feedbackAt.hashCode) +
        (note == null ? 0 : note.hashCode) +
        (snoozedUntil == null ? 0 : snoozedUntil.hashCode) +
        (suppressedReason == null ? 0 : suppressedReason.hashCode) +
        tags.hashCode +
        (recordSnapshot == null ? 0 : recordSnapshot.hashCode) +
        summaryCitations.hashCode +
        sensitive.hashCode +
        (region == null ? 0 : region.hashCode) +
        (context == null ? 0 : context.hashCode) +
        (exerciseRunId == null ? 0 : exerciseRunId.hashCode) +
        signals.hashCode;

  factory StreamAlerts200Response.fromJson(Map<String, dynamic> json) => _$StreamAlerts200ResponseFromJson(json);

  Map<String, dynamic> toJson() => _$StreamAlerts200ResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Lifecycle transition; SSE maps fired → `alert`, resolved → `alert.resolved`, all others → `alert.updated`
enum StreamAlerts200ResponseTypeEnum {
@JsonValue(r'fired')
fired(r'fired'),
@JsonValue(r'updated')
updated(r'updated'),
@JsonValue(r'acknowledged')
acknowledged(r'acknowledged'),
@JsonValue(r'snoozed')
snoozed(r'snoozed'),
@JsonValue(r'resolved')
resolved(r'resolved');

const StreamAlerts200ResponseTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum StreamAlerts200ResponseReasonEnum {
@JsonValue(r'buffer_overflow')
bufferOverflow(r'buffer_overflow'),
@JsonValue(r'replay_cap')
replayCap(r'replay_cap'),
@JsonValue(r'resync_required')
resyncRequired(r'resync_required');

const StreamAlerts200ResponseReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


enum StreamAlerts200ResponseSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const StreamAlerts200ResponseSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


enum StreamAlerts200ResponseStateEnum {
@JsonValue(r'firing')
firing(r'firing'),
@JsonValue(r'acknowledged')
acknowledged(r'acknowledged'),
@JsonValue(r'resolved')
resolved(r'resolved');

const StreamAlerts200ResponseStateEnum(this.value);

final String value;

@override
String toString() => value;
}


enum StreamAlerts200ResponseFeedbackEnum {
@JsonValue(r'useful')
useful(r'useful'),
@JsonValue(r'noise')
noise(r'noise'),
@JsonValue(r'late')
late_(r'late'),
@JsonValue(r'false_positive')
falsePositive(r'false_positive');

const StreamAlerts200ResponseFeedbackEnum(this.value);

final String value;

@override
String toString() => value;
}


enum StreamAlerts200ResponseSuppressedReasonEnum {
@JsonValue(r'alert_snoozed')
alertSnoozed(r'alert_snoozed'),
@JsonValue(r'rule_snoozed')
ruleSnoozed(r'rule_snoozed');

const StreamAlerts200ResponseSuppressedReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


