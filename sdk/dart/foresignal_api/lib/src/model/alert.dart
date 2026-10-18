//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/alert_signal.dart';
import 'package:foresignal_api/src/model/alert_context.dart';
import 'package:foresignal_api/src/model/ask_citation.dart';
import 'package:foresignal_api/src/model/region_tags.dart';
import 'package:foresignal_api/src/model/record.dart';
import 'package:foresignal_api/src/model/alert_record_snapshot.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'alert.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Alert {
  /// Returns a new [Alert] instance.
  Alert({

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

     this.suppressed = false,

     this.suppressedReason,

     this.tags,

     this.recordSnapshot,

     this.summaryCitations,

     this.sensitive,

     this.region,

     this.context,

     this.exercise = false,

     this.exerciseRunId,

     this.signals,
  });

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


  final AlertSeverityEnum severity;



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


  final AlertStateEnum state;



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


  final AlertFeedbackEnum? feedback;



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
    defaultValue: false,
    name: r'suppressed',
    required: false,
    includeIfNull: false,
  )


  final bool? suppressed;



  @JsonKey(
    
    name: r'suppressedReason',
    required: false,
    includeIfNull: false,
  )


  final AlertSuppressedReasonEnum? suppressedReason;



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



      /// Exercise replay alert (never counted in real state or statistics)
  @JsonKey(
    defaultValue: false,
    name: r'exercise',
    required: false,
    includeIfNull: false,
  )


  final bool? exercise;



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
    bool operator ==(Object other) => identical(this, other) || other is Alert &&
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
      other.suppressed == suppressed &&
      other.suppressedReason == suppressedReason &&
      other.tags == tags &&
      other.recordSnapshot == recordSnapshot &&
      other.summaryCitations == summaryCitations &&
      other.sensitive == sensitive &&
      other.region == region &&
      other.context == context &&
      other.exercise == exercise &&
      other.exerciseRunId == exerciseRunId &&
      other.signals == signals;

    @override
    int get hashCode =>
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
        suppressed.hashCode +
        (suppressedReason == null ? 0 : suppressedReason.hashCode) +
        tags.hashCode +
        (recordSnapshot == null ? 0 : recordSnapshot.hashCode) +
        summaryCitations.hashCode +
        sensitive.hashCode +
        (region == null ? 0 : region.hashCode) +
        (context == null ? 0 : context.hashCode) +
        exercise.hashCode +
        (exerciseRunId == null ? 0 : exerciseRunId.hashCode) +
        signals.hashCode;

  factory Alert.fromJson(Map<String, dynamic> json) => _$AlertFromJson(json);

  Map<String, dynamic> toJson() => _$AlertToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AlertSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const AlertSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AlertStateEnum {
@JsonValue(r'firing')
firing(r'firing'),
@JsonValue(r'acknowledged')
acknowledged(r'acknowledged'),
@JsonValue(r'resolved')
resolved(r'resolved');

const AlertStateEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AlertFeedbackEnum {
@JsonValue(r'useful')
useful(r'useful'),
@JsonValue(r'noise')
noise(r'noise'),
@JsonValue(r'late')
late_(r'late'),
@JsonValue(r'false_positive')
falsePositive(r'false_positive');

const AlertFeedbackEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AlertSuppressedReasonEnum {
@JsonValue(r'alert_snoozed')
alertSnoozed(r'alert_snoozed'),
@JsonValue(r'rule_snoozed')
ruleSnoozed(r'rule_snoozed');

const AlertSuppressedReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


