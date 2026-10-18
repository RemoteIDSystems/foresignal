//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/mesh_channel_common_config_airtime_custom.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'meshtastic_channel_config.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MeshtasticChannelConfig {
  /// Returns a new [MeshtasticChannelConfig] instance.
  MeshtasticChannelConfig({

    required  this.region,

     this.airtimePreset,

     this.airtimeCustom,

     this.dutyCyclePercent = 1.0,

     this.minSpacing = '30s',

     this.maxMessagesPerHour = 20,

     this.profile,

     this.minSeverity,

     this.reportsEnabled = false,

     this.allowedSenders,

     this.compactEvents = false,

    required  this.brokerUrl,

     this.root = 'msh/local',

    required  this.gatewayNodeNum,

    required  this.channelIndex,

    required  this.channelName,

     this.toNodeNum,

     this.topicSuffixNodeId = false,

     this.licensedMode = false,

    required  this.privateChannelConfirmed,
  });

  @JsonKey(
    
    name: r'region',
    required: true,
    includeIfNull: false,
  )


  final MeshtasticChannelConfigRegionEnum region;



      /// Default meshtastic-longfast for `meshtastic`, custom for `meshcore`; the operator confirms it matches the node
  @JsonKey(
    
    name: r'airtimePreset',
    required: false,
    includeIfNull: false,
  )


  final MeshtasticChannelConfigAirtimePresetEnum? airtimePreset;



  @JsonKey(
    
    name: r'airtimeCustom',
    required: false,
    includeIfNull: false,
  )


  final MeshChannelCommonConfigAirtimeCustom? airtimeCustom;



      /// Must not exceed the region's regulatory ceiling
          // minimum: 0
          // maximum: 10
  @JsonKey(
    defaultValue: 1.0,
    name: r'dutyCyclePercent',
    required: false,
    includeIfNull: false,
  )


  final num? dutyCyclePercent;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    defaultValue: '30s',
    name: r'minSpacing',
    required: false,
    includeIfNull: false,
  )


  final String? minSpacing;



          // minimum: 1
  @JsonKey(
    defaultValue: 20,
    name: r'maxMessagesPerHour',
    required: false,
    includeIfNull: false,
  )


  final int? maxMessagesPerHour;



  @JsonKey(
    
    name: r'profile',
    required: false,
    includeIfNull: false,
  )


  final MeshtasticChannelConfigProfileEnum? profile;



      /// The channel takes nothing below this severity, whatever its top-level minSeverity
  @JsonKey(
    
    name: r'minSeverity',
    required: false,
    includeIfNull: false,
  )


  final MeshtasticChannelConfigMinSeverityEnum? minSeverity;



  @JsonKey(
    defaultValue: false,
    name: r'reportsEnabled',
    required: false,
    includeIfNull: false,
  )


  final bool? reportsEnabled;



      /// Sender to role. A sender is a Meshtastic node id (`!xxxxxxxx`), a MeshCore key prefix (`mc:` + 12 hex) or `name:` + the advertised name: MeshCore channel messages name their sender only by its advertised name, so a MeshCore channel report is matched by `name:` (084)
  @JsonKey(
    
    name: r'allowedSenders',
    required: false,
    includeIfNull: false,
  )


  final Map<String, MeshtasticChannelConfigAllowedSendersEnum>? allowedSenders;



      /// Also send the federation compact messages (fired and resolved alerts, intelligence events) within this channel's airtime budget, through its profile (113)
  @JsonKey(
    defaultValue: false,
    name: r'compactEvents',
    required: false,
    includeIfNull: false,
  )


  final bool? compactEvents;



      /// LAN_URL broker (never *.meshtastic.org)
  @JsonKey(
    
    name: r'brokerUrl',
    required: true,
    includeIfNull: false,
  )


  final String brokerUrl;



  @JsonKey(
    defaultValue: 'msh/local',
    name: r'root',
    required: false,
    includeIfNull: false,
  )


  final String? root;



      /// Unsigned decimal node number used as `from`
  @JsonKey(
    
    name: r'gatewayNodeNum',
    required: true,
    includeIfNull: false,
  )


  final String gatewayNodeNum;



          // minimum: 1
          // maximum: 7
  @JsonKey(
    
    name: r'channelIndex',
    required: true,
    includeIfNull: false,
  )


  final int channelIndex;



  @JsonKey(
    
    name: r'channelName',
    required: true,
    includeIfNull: false,
  )


  final String channelName;



      /// Direct message target
  @JsonKey(
    
    name: r'toNodeNum',
    required: false,
    includeIfNull: false,
  )


  final String? toNodeNum;



  @JsonKey(
    defaultValue: false,
    name: r'topicSuffixNodeId',
    required: false,
    includeIfNull: false,
  )


  final bool? topicSuffixNodeId;



      /// Must be false (ham mode disables encryption)
  @JsonKey(
    defaultValue: false,
    name: r'licensedMode',
    required: false,
    includeIfNull: false,
  )


  final bool? licensedMode;



      /// Must be true
  @JsonKey(
    
    name: r'privateChannelConfirmed',
    required: true,
    includeIfNull: false,
  )


  final bool privateChannelConfirmed;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MeshtasticChannelConfig &&
      other.region == region &&
      other.airtimePreset == airtimePreset &&
      other.airtimeCustom == airtimeCustom &&
      other.dutyCyclePercent == dutyCyclePercent &&
      other.minSpacing == minSpacing &&
      other.maxMessagesPerHour == maxMessagesPerHour &&
      other.profile == profile &&
      other.minSeverity == minSeverity &&
      other.reportsEnabled == reportsEnabled &&
      other.allowedSenders == allowedSenders &&
      other.compactEvents == compactEvents &&
      other.brokerUrl == brokerUrl &&
      other.root == root &&
      other.gatewayNodeNum == gatewayNodeNum &&
      other.channelIndex == channelIndex &&
      other.channelName == channelName &&
      other.toNodeNum == toNodeNum &&
      other.topicSuffixNodeId == topicSuffixNodeId &&
      other.licensedMode == licensedMode &&
      other.privateChannelConfirmed == privateChannelConfirmed;

    @override
    int get hashCode =>
        region.hashCode +
        airtimePreset.hashCode +
        (airtimeCustom == null ? 0 : airtimeCustom.hashCode) +
        dutyCyclePercent.hashCode +
        minSpacing.hashCode +
        maxMessagesPerHour.hashCode +
        profile.hashCode +
        minSeverity.hashCode +
        reportsEnabled.hashCode +
        allowedSenders.hashCode +
        compactEvents.hashCode +
        brokerUrl.hashCode +
        root.hashCode +
        gatewayNodeNum.hashCode +
        channelIndex.hashCode +
        channelName.hashCode +
        (toNodeNum == null ? 0 : toNodeNum.hashCode) +
        topicSuffixNodeId.hashCode +
        licensedMode.hashCode +
        privateChannelConfirmed.hashCode;

  factory MeshtasticChannelConfig.fromJson(Map<String, dynamic> json) => _$MeshtasticChannelConfigFromJson(json);

  Map<String, dynamic> toJson() => _$MeshtasticChannelConfigToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MeshtasticChannelConfigRegionEnum {
@JsonValue(r'EU_868')
EU_868(r'EU_868'),
@JsonValue(r'EU_433')
EU_433(r'EU_433'),
@JsonValue(r'UK_868')
UK_868(r'UK_868'),
@JsonValue(r'US')
US(r'US'),
@JsonValue(r'ANZ')
ANZ(r'ANZ'),
@JsonValue(r'OTHER')
OTHER(r'OTHER');

const MeshtasticChannelConfigRegionEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Default meshtastic-longfast for `meshtastic`, custom for `meshcore`; the operator confirms it matches the node
enum MeshtasticChannelConfigAirtimePresetEnum {
@JsonValue(r'meshtastic-longfast')
meshtasticLongfast(r'meshtastic-longfast'),
@JsonValue(r'meshtastic-mediumfast')
meshtasticMediumfast(r'meshtastic-mediumfast'),
@JsonValue(r'custom')
custom(r'custom');

const MeshtasticChannelConfigAirtimePresetEnum(this.value);

final String value;

@override
String toString() => value;
}


enum MeshtasticChannelConfigProfileEnum {
@JsonValue(r'text')
text(r'text'),
@JsonValue(r'cbor')
cbor(r'cbor');

const MeshtasticChannelConfigProfileEnum(this.value);

final String value;

@override
String toString() => value;
}


/// The channel takes nothing below this severity, whatever its top-level minSeverity
enum MeshtasticChannelConfigMinSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const MeshtasticChannelConfigMinSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


enum MeshtasticChannelConfigAllowedSendersEnum {
@JsonValue(r'report')
report(r'report'),
@JsonValue(r'ack')
ack(r'ack');

const MeshtasticChannelConfigAllowedSendersEnum(this.value);

final String value;

@override
String toString() => value;
}


