//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attachment.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification_delivery.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NotificationDelivery {
  /// Returns a new [NotificationDelivery] instance.
  NotificationDelivery({

    required  this.id,

    required  this.event,

     this.alertId,

     this.briefingRunId,

    required  this.status,

    required  this.attempts,

     this.lastError,

     this.nextAttemptAt,

     this.deliveredAt,

     this.notificationId,

     this.kind,

     this.deferredReason,

     this.edited,

     this.note,

     this.escalationStep,

     this.attachmentLevel,

     this.attachments,

     this.shortCode,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final NotificationDeliveryEventEnum event;



  @JsonKey(
    
    name: r'alertId',
    required: false,
    includeIfNull: false,
  )


  final int? alertId;



  @JsonKey(
    
    name: r'briefingRunId',
    required: false,
    includeIfNull: false,
  )


  final String? briefingRunId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final NotificationDeliveryStatusEnum status;



  @JsonKey(
    
    name: r'attempts',
    required: true,
    includeIfNull: false,
  )


  final int attempts;



  @JsonKey(
    
    name: r'lastError',
    required: false,
    includeIfNull: false,
  )


  final String? lastError;



  @JsonKey(
    
    name: r'nextAttemptAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? nextAttemptAt;



  @JsonKey(
    
    name: r'deliveredAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? deliveredAt;



  @JsonKey(
    
    name: r'notificationId',
    required: false,
    includeIfNull: false,
  )


  final int? notificationId;



  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final NotificationDeliveryKindEnum? kind;



      /// `airtime` = held by a mesh channel's LoRa airtime governor (084); released as one digest line.
  @JsonKey(
    
    name: r'deferredReason',
    required: false,
    includeIfNull: false,
  )


  final NotificationDeliveryDeferredReasonEnum? deferredReason;



      /// An update delivered by editing the original message
  @JsonKey(
    
    name: r'edited',
    required: false,
    includeIfNull: false,
  )


  final bool? edited;



      /// e.g. folded into delivery <id>
  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;



      /// Set on escalation deliveries
  @JsonKey(
    
    name: r'escalationStep',
    required: false,
    includeIfNull: false,
  )


  final int? escalationStep;



      /// Size-control level chosen for this channel (800 px → 600 px → 256 px thumbnail → text only)
  @JsonKey(
    
    name: r'attachmentLevel',
    required: false,
    includeIfNull: false,
  )


  final NotificationDeliveryAttachmentLevelEnum? attachmentLevel;



      /// Media references sent (bytes load from the media store at send time, so retries work)
  @JsonKey(
    
    name: r'attachments',
    required: false,
    includeIfNull: false,
  )


  final List<Attachment>? attachments;



      /// 4-character Crockford base32 alert code used by mesh channels so `#E ACK <code>` finds the alert (084)
  @JsonKey(
    
    name: r'shortCode',
    required: false,
    includeIfNull: false,
  )


  final String? shortCode;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NotificationDelivery &&
      other.id == id &&
      other.event == event &&
      other.alertId == alertId &&
      other.briefingRunId == briefingRunId &&
      other.status == status &&
      other.attempts == attempts &&
      other.lastError == lastError &&
      other.nextAttemptAt == nextAttemptAt &&
      other.deliveredAt == deliveredAt &&
      other.notificationId == notificationId &&
      other.kind == kind &&
      other.deferredReason == deferredReason &&
      other.edited == edited &&
      other.note == note &&
      other.escalationStep == escalationStep &&
      other.attachmentLevel == attachmentLevel &&
      other.attachments == attachments &&
      other.shortCode == shortCode;

    @override
    int get hashCode =>
        id.hashCode +
        event.hashCode +
        (alertId == null ? 0 : alertId.hashCode) +
        (briefingRunId == null ? 0 : briefingRunId.hashCode) +
        status.hashCode +
        attempts.hashCode +
        (lastError == null ? 0 : lastError.hashCode) +
        (nextAttemptAt == null ? 0 : nextAttemptAt.hashCode) +
        (deliveredAt == null ? 0 : deliveredAt.hashCode) +
        notificationId.hashCode +
        kind.hashCode +
        (deferredReason == null ? 0 : deferredReason.hashCode) +
        edited.hashCode +
        (note == null ? 0 : note.hashCode) +
        (escalationStep == null ? 0 : escalationStep.hashCode) +
        (attachmentLevel == null ? 0 : attachmentLevel.hashCode) +
        attachments.hashCode +
        (shortCode == null ? 0 : shortCode.hashCode);

  factory NotificationDelivery.fromJson(Map<String, dynamic> json) => _$NotificationDeliveryFromJson(json);

  Map<String, dynamic> toJson() => _$NotificationDeliveryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NotificationDeliveryEventEnum {
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'briefing')
briefing(r'briefing'),
@JsonValue(r'system')
system(r'system'),
@JsonValue(r'social.divergence')
socialPeriodDivergence(r'social.divergence'),
@JsonValue(r'social.attention_gap')
socialPeriodAttentionGap(r'social.attention_gap'),
@JsonValue(r'analyst.note')
analystPeriodNote(r'analyst.note'),
@JsonValue(r'situation.activated')
situationPeriodActivated(r'situation.activated'),
@JsonValue(r'situation.escalated')
situationPeriodEscalated(r'situation.escalated'),
@JsonValue(r'finding.significant')
findingPeriodSignificant(r'finding.significant'),
@JsonValue(r'forecast.resolved')
forecastPeriodResolved(r'forecast.resolved'),
@JsonValue(r'forecast.resolution_needed')
forecastPeriodResolutionNeeded(r'forecast.resolution_needed'),
@JsonValue(r'hypothesis.band_changed')
hypothesisPeriodBandChanged(r'hypothesis.band_changed'),
@JsonValue(r'indicator.observed')
indicatorPeriodObserved(r'indicator.observed'),
@JsonValue(r'assumption.broken')
assumptionPeriodBroken(r'assumption.broken'),
@JsonValue(r'assessment.published')
assessmentPeriodPublished(r'assessment.published'),
@JsonValue(r'chronicle.proposed')
chroniclePeriodProposed(r'chronicle.proposed'),
@JsonValue(r'chronicle.cap_warning')
chroniclePeriodCapWarning(r'chronicle.cap_warning'),
@JsonValue(r'chronicle.gap')
chroniclePeriodGap(r'chronicle.gap'),
@JsonValue(r'mention')
mention(r'mention'),
@JsonValue(r'share')
share(r'share'),
@JsonValue(r'report.received')
reportPeriodReceived(r'report.received'),
@JsonValue(r'package.received')
packagePeriodReceived(r'package.received'),
@JsonValue(r'chronicle.access.requested')
chroniclePeriodAccessPeriodRequested(r'chronicle.access.requested'),
@JsonValue(r'saved-search.matches')
savedSearchPeriodMatches(r'saved-search.matches'),
@JsonValue(r'personal')
personal(r'personal');

const NotificationDeliveryEventEnum(this.value);

final String value;

@override
String toString() => value;
}


enum NotificationDeliveryStatusEnum {
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'delivered')
delivered(r'delivered'),
@JsonValue(r'failed')
failed(r'failed'),
@JsonValue(r'suppressed')
suppressed(r'suppressed'),
@JsonValue(r'unacked')
unacked(r'unacked');

const NotificationDeliveryStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum NotificationDeliveryKindEnum {
@JsonValue(r'send')
send(r'send'),
@JsonValue(r'update')
update(r'update'),
@JsonValue(r'followup')
followup(r'followup'),
@JsonValue(r'digest')
digest(r'digest');

const NotificationDeliveryKindEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `airtime` = held by a mesh channel's LoRa airtime governor (084); released as one digest line.
enum NotificationDeliveryDeferredReasonEnum {
@JsonValue(r'quiet_hours')
quietHours(r'quiet_hours'),
@JsonValue(r'paused')
paused(r'paused'),
@JsonValue(r'enrichment')
enrichment(r'enrichment'),
@JsonValue(r'grouping')
grouping(r'grouping'),
@JsonValue(r'presence')
presence(r'presence'),
@JsonValue(r'airtime')
airtime(r'airtime');

const NotificationDeliveryDeferredReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Size-control level chosen for this channel (800 px → 600 px → 256 px thumbnail → text only)
enum NotificationDeliveryAttachmentLevelEnum {
@JsonValue(r'map-800')
map800(r'map-800'),
@JsonValue(r'map-600')
map600(r'map-600'),
@JsonValue(r'thumbnail')
thumbnail(r'thumbnail'),
@JsonValue(r'text-only')
textOnly(r'text-only');

const NotificationDeliveryAttachmentLevelEnum(this.value);

final String value;

@override
String toString() => value;
}


