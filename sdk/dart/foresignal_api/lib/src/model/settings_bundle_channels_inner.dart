//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/channel_input_config.dart';
import 'package:foresignal_api/src/model/quiet_hours.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'settings_bundle_channels_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SettingsBundleChannelsInner {
  /// Returns a new [SettingsBundleChannelsInner] instance.
  SettingsBundleChannelsInner({

    required  this.name,

    required  this.type,

     this.config,

     this.secrets,

     this.events,

     this.ruleIds,

     this.minSeverity,

     this.titleTemplate,

     this.bodyTemplate,

     this.waitForEnrichmentSeconds = 0,

     this.enabled = true,

     this.timezone,

     this.quietHours,

     this.groupWindowSeconds = 0,

     this.backlogCap = 20,

     this.editInPlace = true,

     this.deferWhileActiveSeconds = 0,

     this.acceptExercise = false,

     this.language,

    required  this.id,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// teams accepts only Power Automate Workflows URLs (Office 365 connector URLs → 400); webpush is the built-in Push channel (031): created at startup with a fixed id, updatable but not creatable (400); its config takes quietHoursMode (defer|downgrade|drop); mqtt (060) publishes on the operator's broker (config MqttChannelConfig, secrets username, password, caCertPem): events on {prefix}/alerts/{severity}, {prefix}/alerts/updates, {prefix}/briefings/{briefingId} and {prefix}/system/events, plus retained state and optional Home Assistant discovery; a public broker without TLS or credentials is saved with the warning MQTT_INSECURE_PUBLIC_BROKER; meshtastic (084) sends compact alert lines through the node's MQTT JSON downlink (config MeshtasticChannelConfig, secrets username, password) and meshcore (084) through the MeshCore companion protocol (config MeshcoreChannelConfig); both refuse public or default channels with MESH_PUBLIC_CHANNEL, pass every transmission through a LoRa airtime governor and take only alert.resolved among lifecycle updates
  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final SettingsBundleChannelsInnerTypeEnum type;



  @JsonKey(
    
    name: r'config',
    required: false,
    includeIfNull: false,
  )


  final ChannelInputConfig? config;



      /// Write-only secret settings per the type's secretFields. A secretFields entry named `prefix.<Name>` (webhook `header.<Name>`) accepts any secret whose name starts with the prefix.
  @JsonKey(
    
    name: r'secrets',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? secrets;



      /// Prompt 018 accepts alert, briefing and system (default [\"alert\"]); the other values are subscribed by the prompts that introduce them
  @JsonKey(
    
    name: r'events',
    required: false,
    includeIfNull: false,
  )


  final List<SettingsBundleChannelsInnerEventsEnum>? events;



      /// null = all rules
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


  final SettingsBundleChannelsInnerMinSeverityEnum? minSeverity;



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



          // minimum: 0
          // maximum: 60
  @JsonKey(
    defaultValue: 0,
    name: r'waitForEnrichmentSeconds',
    required: false,
    includeIfNull: false,
  )


  final int? waitForEnrichmentSeconds;



  @JsonKey(
    defaultValue: true,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



      /// IANA timezone for quiet hours and `|local` template filters (default: the `server` setting's timezone, else UTC)
  @JsonKey(
    
    name: r'timezone',
    required: false,
    includeIfNull: false,
  )


  final String? timezone;



      /// Quiet hours in the channel timezone; null for none
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



      /// Receive exercise alerts
  @JsonKey(
    defaultValue: false,
    name: r'acceptExercise',
    required: false,
    includeIfNull: false,
  )


  final bool? acceptExercise;



      /// BCP 47 language for notifications on this channel (141), stored canonicalized (`pt-br` → `pt-BR`); null uses `notify.defaultLanguage`. When the primary language or script differs from it, title and body are translated through the 026 chat role (budget `translate`, low effort), code spans, URLs and placeholders untouched, cached 24 h; after 8 s, or on any failure, the original is sent with the title prefixed `[untranslated]`.
  @JsonKey(
    
    name: r'language',
    required: false,
    includeIfNull: false,
  )


  final String? language;



      /// The channel id; import upserts by it
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SettingsBundleChannelsInner &&
      other.name == name &&
      other.type == type &&
      other.config == config &&
      other.secrets == secrets &&
      other.events == events &&
      other.ruleIds == ruleIds &&
      other.minSeverity == minSeverity &&
      other.titleTemplate == titleTemplate &&
      other.bodyTemplate == bodyTemplate &&
      other.waitForEnrichmentSeconds == waitForEnrichmentSeconds &&
      other.enabled == enabled &&
      other.timezone == timezone &&
      other.quietHours == quietHours &&
      other.groupWindowSeconds == groupWindowSeconds &&
      other.backlogCap == backlogCap &&
      other.editInPlace == editInPlace &&
      other.deferWhileActiveSeconds == deferWhileActiveSeconds &&
      other.acceptExercise == acceptExercise &&
      other.language == language &&
      other.id == id;

    @override
    int get hashCode =>
        name.hashCode +
        type.hashCode +
        config.hashCode +
        secrets.hashCode +
        events.hashCode +
        (ruleIds == null ? 0 : ruleIds.hashCode) +
        minSeverity.hashCode +
        (titleTemplate == null ? 0 : titleTemplate.hashCode) +
        (bodyTemplate == null ? 0 : bodyTemplate.hashCode) +
        waitForEnrichmentSeconds.hashCode +
        enabled.hashCode +
        (timezone == null ? 0 : timezone.hashCode) +
        (quietHours == null ? 0 : quietHours.hashCode) +
        groupWindowSeconds.hashCode +
        backlogCap.hashCode +
        editInPlace.hashCode +
        deferWhileActiveSeconds.hashCode +
        acceptExercise.hashCode +
        (language == null ? 0 : language.hashCode) +
        id.hashCode;

  factory SettingsBundleChannelsInner.fromJson(Map<String, dynamic> json) => _$SettingsBundleChannelsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SettingsBundleChannelsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// teams accepts only Power Automate Workflows URLs (Office 365 connector URLs → 400); webpush is the built-in Push channel (031): created at startup with a fixed id, updatable but not creatable (400); its config takes quietHoursMode (defer|downgrade|drop); mqtt (060) publishes on the operator's broker (config MqttChannelConfig, secrets username, password, caCertPem): events on {prefix}/alerts/{severity}, {prefix}/alerts/updates, {prefix}/briefings/{briefingId} and {prefix}/system/events, plus retained state and optional Home Assistant discovery; a public broker without TLS or credentials is saved with the warning MQTT_INSECURE_PUBLIC_BROKER; meshtastic (084) sends compact alert lines through the node's MQTT JSON downlink (config MeshtasticChannelConfig, secrets username, password) and meshcore (084) through the MeshCore companion protocol (config MeshcoreChannelConfig); both refuse public or default channels with MESH_PUBLIC_CHANNEL, pass every transmission through a LoRa airtime governor and take only alert.resolved among lifecycle updates
enum SettingsBundleChannelsInnerTypeEnum {
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

const SettingsBundleChannelsInnerTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SettingsBundleChannelsInnerEventsEnum {
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

const SettingsBundleChannelsInnerEventsEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SettingsBundleChannelsInnerMinSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const SettingsBundleChannelsInnerMinSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


