//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/channel_warnings_inner.dart';
import 'package:foresignal_api/src/model/channel_input_config.dart';
import 'package:foresignal_api/src/model/quiet_hours.dart';
import 'package:foresignal_api/src/model/channel_interactive_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'channel.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Channel {
  /// Returns a new [Channel] instance.
  Channel({

    required  this.id,

    required  this.name,

    required  this.type,

     this.config,

     this.secretFields,

    required  this.events,

     this.ruleIds,

     this.minSeverity,

     this.titleTemplate,

     this.bodyTemplate,

     this.waitForEnrichmentSeconds,

    required  this.enabled,

     this.createdAt,

     this.updatedAt,

     this.lastDeliveryAt,

     this.failedLast24h,

     this.timezone,

     this.quietHours,

     this.groupWindowSeconds = 0,

     this.backlogCap = 20,

     this.editInPlace = true,

     this.deferWhileActiveSeconds = 0,

     this.warnings,

     this.interactive,

     this.acceptExercise = false,

     this.language,

     this.ownerPrincipalId,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ChannelTypeEnum type;



  @JsonKey(
    
    name: r'config',
    required: false,
    includeIfNull: false,
  )


  final ChannelInputConfig? config;



      /// Names of the secret fields that are set (values are never returned)
  @JsonKey(
    
    name: r'secretFields',
    required: false,
    includeIfNull: false,
  )


  final List<String>? secretFields;



  @JsonKey(
    
    name: r'events',
    required: true,
    includeIfNull: false,
  )


  final List<ChannelEventsEnum> events;



  @JsonKey(
    
    name: r'ruleIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? ruleIds;



  @JsonKey(
    
    name: r'minSeverity',
    required: false,
    includeIfNull: false,
  )


  final ChannelMinSeverityEnum? minSeverity;



  @JsonKey(
    
    name: r'titleTemplate',
    required: false,
    includeIfNull: false,
  )


  final String? titleTemplate;



  @JsonKey(
    
    name: r'bodyTemplate',
    required: false,
    includeIfNull: false,
  )


  final String? bodyTemplate;



  @JsonKey(
    
    name: r'waitForEnrichmentSeconds',
    required: false,
    includeIfNull: false,
  )


  final int? waitForEnrichmentSeconds;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'createdAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;



  @JsonKey(
    
    name: r'lastDeliveryAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastDeliveryAt;



  @JsonKey(
    
    name: r'failedLast24h',
    required: false,
    includeIfNull: false,
  )


  final int? failedLast24h;



      /// IANA timezone for quiet hours and `|local` template filters (default: the `server` setting's timezone, else UTC)
  @JsonKey(
    
    name: r'timezone',
    required: false,
    includeIfNull: false,
  )


  final String? timezone;



  @JsonKey(
    
    name: r'quietHours',
    required: false,
    includeIfNull: false,
  )


  final QuietHours? quietHours;



      /// > 0 holds the first alert per (channel, rule) for the window and sends one grouped message
          // minimum: 0
  @JsonKey(
    defaultValue: 0,
    name: r'groupWindowSeconds',
    required: false,
    includeIfNull: false,
  )


  final int? groupWindowSeconds;



      /// Pending sends above this are folded into one digest (\"+N more alerts\")
          // minimum: 1
  @JsonKey(
    defaultValue: 20,
    name: r'backlogCap',
    required: false,
    includeIfNull: false,
  )


  final int? backlogCap;



      /// Edit the original message on lifecycle updates where the type supports it (Discord, Telegram, Matrix)
  @JsonKey(
    defaultValue: true,
    name: r'editInPlace',
    required: false,
    includeIfNull: false,
  )


  final bool? editInPlace;



      /// Defer non-critical alerts while a desktop client is active (033); for phone-type channels
          // minimum: 0
          // maximum: 600
  @JsonKey(
    defaultValue: 0,
    name: r'deferWhileActiveSeconds',
    required: false,
    includeIfNull: false,
  )


  final int? deferWhileActiveSeconds;



      /// Non-fatal configuration warnings, e.g. MQTT_INSECURE_PUBLIC_BROKER (060); absent when there are none
  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<ChannelWarningsInner>? warnings;



      /// Interactive chat-command status for telegram/matrix channels (061)
  @JsonKey(
    
    name: r'interactive',
    required: false,
    includeIfNull: false,
  )


  final ChannelInteractiveStatus? interactive;



      /// Receives exercise alerts
  @JsonKey(
    defaultValue: false,
    name: r'acceptExercise',
    required: false,
    includeIfNull: false,
  )


  final bool? acceptExercise;



      /// BCP 47; when it differs from `notify.defaultLanguage` (primary language or script) title and body are translated (026 chat role, budget `translate`), cached 24 h, the original with an `[untranslated]` title marker after 8 s or on failure (141)
  @JsonKey(
    
    name: r'language',
    required: false,
    includeIfNull: false,
  )


  final String? language;



      /// Set for principal-owned channels created through notification preferences (130)
  @JsonKey(
    
    name: r'ownerPrincipalId',
    required: false,
    includeIfNull: false,
  )


  final String? ownerPrincipalId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Channel &&
      other.id == id &&
      other.name == name &&
      other.type == type &&
      other.config == config &&
      other.secretFields == secretFields &&
      other.events == events &&
      other.ruleIds == ruleIds &&
      other.minSeverity == minSeverity &&
      other.titleTemplate == titleTemplate &&
      other.bodyTemplate == bodyTemplate &&
      other.waitForEnrichmentSeconds == waitForEnrichmentSeconds &&
      other.enabled == enabled &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt &&
      other.lastDeliveryAt == lastDeliveryAt &&
      other.failedLast24h == failedLast24h &&
      other.timezone == timezone &&
      other.quietHours == quietHours &&
      other.groupWindowSeconds == groupWindowSeconds &&
      other.backlogCap == backlogCap &&
      other.editInPlace == editInPlace &&
      other.deferWhileActiveSeconds == deferWhileActiveSeconds &&
      other.warnings == warnings &&
      other.interactive == interactive &&
      other.acceptExercise == acceptExercise &&
      other.language == language &&
      other.ownerPrincipalId == ownerPrincipalId;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        type.hashCode +
        config.hashCode +
        secretFields.hashCode +
        events.hashCode +
        (ruleIds == null ? 0 : ruleIds.hashCode) +
        minSeverity.hashCode +
        (titleTemplate == null ? 0 : titleTemplate.hashCode) +
        (bodyTemplate == null ? 0 : bodyTemplate.hashCode) +
        waitForEnrichmentSeconds.hashCode +
        enabled.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode +
        (lastDeliveryAt == null ? 0 : lastDeliveryAt.hashCode) +
        failedLast24h.hashCode +
        (timezone == null ? 0 : timezone.hashCode) +
        (quietHours == null ? 0 : quietHours.hashCode) +
        groupWindowSeconds.hashCode +
        backlogCap.hashCode +
        editInPlace.hashCode +
        deferWhileActiveSeconds.hashCode +
        warnings.hashCode +
        (interactive == null ? 0 : interactive.hashCode) +
        acceptExercise.hashCode +
        (language == null ? 0 : language.hashCode) +
        (ownerPrincipalId == null ? 0 : ownerPrincipalId.hashCode);

  factory Channel.fromJson(Map<String, dynamic> json) => _$ChannelFromJson(json);

  Map<String, dynamic> toJson() => _$ChannelToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChannelTypeEnum {
@JsonValue(r'webhook')
webhook(r'webhook'),
@JsonValue(r'slack')
slack(r'slack'),
@JsonValue(r'mattermost')
mattermost(r'mattermost'),
@JsonValue(r'discord')
discord(r'discord'),
@JsonValue(r'teams')
teams(r'teams'),
@JsonValue(r'google-chat')
googleChat(r'google-chat'),
@JsonValue(r'telegram')
telegram(r'telegram'),
@JsonValue(r'matrix')
matrix(r'matrix'),
@JsonValue(r'ntfy')
ntfy(r'ntfy'),
@JsonValue(r'gotify')
gotify(r'gotify'),
@JsonValue(r'pushover')
pushover(r'pushover'),
@JsonValue(r'email')
email(r'email'),
@JsonValue(r'webpush')
webpush(r'webpush'),
@JsonValue(r'homeassistant')
homeassistant(r'homeassistant'),
@JsonValue(r'apprise')
apprise(r'apprise'),
@JsonValue(r'zulip')
zulip(r'zulip'),
@JsonValue(r'mqtt')
mqtt(r'mqtt'),
@JsonValue(r'meshtastic')
meshtastic(r'meshtastic'),
@JsonValue(r'meshcore')
meshcore(r'meshcore');

const ChannelTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ChannelEventsEnum {
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'briefing')
briefing(r'briefing'),
@JsonValue(r'system')
system(r'system'),
@JsonValue(r'chronicle.proposed')
chroniclePeriodProposed(r'chronicle.proposed'),
@JsonValue(r'chronicle.cap_warning')
chroniclePeriodCapWarning(r'chronicle.cap_warning'),
@JsonValue(r'chronicle.gap')
chroniclePeriodGap(r'chronicle.gap'),
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
savedSearchPeriodMatches(r'saved-search.matches');

const ChannelEventsEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ChannelMinSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const ChannelMinSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


