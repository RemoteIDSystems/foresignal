//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/channel_type_capabilities.dart';
import 'package:foresignal_api/src/model/channel_type_config_fields_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'channel_type.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChannelType {
  /// Returns a new [ChannelType] instance.
  ChannelType({

    required  this.id,

    required  this.name,

     this.description,

    required  this.configFields,

    required  this.secretFields,

     this.events,

     this.capabilities,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final ChannelTypeIdEnum id;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'configFields',
    required: true,
    includeIfNull: false,
  )


  final List<ChannelTypeConfigFieldsInner> configFields;



      /// Write-only fields; a name of the form `prefix.<Name>` is a wildcard (webhook `header.<Name>` sends any such secret as the header <Name>)
  @JsonKey(
    
    name: r'secretFields',
    required: true,
    includeIfNull: false,
  )


  final List<ChannelTypeConfigFieldsInner> secretFields;



      /// The events a channel of this type can subscribe to: every `Channel.events` value an instance channel accepts
  @JsonKey(
    
    name: r'events',
    required: false,
    includeIfNull: false,
  )


  final List<ChannelTypeEventsEnum>? events;



  @JsonKey(
    
    name: r'capabilities',
    required: false,
    includeIfNull: false,
  )


  final ChannelTypeCapabilities? capabilities;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChannelType &&
      other.id == id &&
      other.name == name &&
      other.description == description &&
      other.configFields == configFields &&
      other.secretFields == secretFields &&
      other.events == events &&
      other.capabilities == capabilities;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        description.hashCode +
        configFields.hashCode +
        secretFields.hashCode +
        events.hashCode +
        capabilities.hashCode;

  factory ChannelType.fromJson(Map<String, dynamic> json) => _$ChannelTypeFromJson(json);

  Map<String, dynamic> toJson() => _$ChannelTypeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChannelTypeIdEnum {
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

const ChannelTypeIdEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ChannelTypeEventsEnum {
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

const ChannelTypeEventsEnum(this.value);

final String value;

@override
String toString() => value;
}


