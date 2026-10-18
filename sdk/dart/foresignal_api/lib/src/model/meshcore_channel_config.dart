//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/mesh_channel_common_config_airtime_custom.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'meshcore_channel_config.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MeshcoreChannelConfig {
  /// Returns a new [MeshcoreChannelConfig] instance.
  MeshcoreChannelConfig({

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

    required  this.companionUrl,

    required  this.channelIndex,

     this.dmContactPrefix,

     this.requireAck = false,

     this.dataType = 65349,
  });

  @JsonKey(
    
    name: r'region',
    required: true,
    includeIfNull: false,
  )


  final MeshcoreChannelConfigRegionEnum region;



      /// Default meshtastic-longfast for `meshtastic`, custom for `meshcore`; the operator confirms it matches the node
  @JsonKey(
    
    name: r'airtimePreset',
    required: false,
    includeIfNull: false,
  )


  final MeshcoreChannelConfigAirtimePresetEnum? airtimePreset;



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


  final MeshcoreChannelConfigProfileEnum? profile;



      /// The channel takes nothing below this severity, whatever its top-level minSeverity
  @JsonKey(
    
    name: r'minSeverity',
    required: false,
    includeIfNull: false,
  )


  final MeshcoreChannelConfigMinSeverityEnum? minSeverity;



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


  final Map<String, MeshcoreChannelConfigAllowedSendersEnum>? allowedSenders;



      /// Also send the federation compact messages (fired and resolved alerts, intelligence events) within this channel's airtime budget, through its profile (113)
  @JsonKey(
    defaultValue: false,
    name: r'compactEvents',
    required: false,
    includeIfNull: false,
  )


  final bool? compactEvents;



      /// LAN_URL tcp://host:port (explicit port, private address)
  @JsonKey(
    
    name: r'companionUrl',
    required: true,
    includeIfNull: false,
  )


  final String companionUrl;



          // minimum: 1
          // maximum: 7
  @JsonKey(
    
    name: r'channelIndex',
    required: true,
    includeIfNull: false,
  )


  final int channelIndex;



  @JsonKey(
    
    name: r'dmContactPrefix',
    required: false,
    includeIfNull: false,
  )


  final String? dmContactPrefix;



      /// DM only; unacknowledged deliveries end `unacked`
  @JsonKey(
    defaultValue: false,
    name: r'requireAck',
    required: false,
    includeIfNull: false,
  )


  final bool? requireAck;



      /// Channel-datagram data type for the CBOR profile (0xFF45)
          // minimum: 65280
          // maximum: 65534
  @JsonKey(
    defaultValue: 65349,
    name: r'dataType',
    required: false,
    includeIfNull: false,
  )


  final int? dataType;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MeshcoreChannelConfig &&
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
      other.companionUrl == companionUrl &&
      other.channelIndex == channelIndex &&
      other.dmContactPrefix == dmContactPrefix &&
      other.requireAck == requireAck &&
      other.dataType == dataType;

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
        companionUrl.hashCode +
        channelIndex.hashCode +
        (dmContactPrefix == null ? 0 : dmContactPrefix.hashCode) +
        requireAck.hashCode +
        dataType.hashCode;

  factory MeshcoreChannelConfig.fromJson(Map<String, dynamic> json) => _$MeshcoreChannelConfigFromJson(json);

  Map<String, dynamic> toJson() => _$MeshcoreChannelConfigToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MeshcoreChannelConfigRegionEnum {
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

const MeshcoreChannelConfigRegionEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Default meshtastic-longfast for `meshtastic`, custom for `meshcore`; the operator confirms it matches the node
enum MeshcoreChannelConfigAirtimePresetEnum {
@JsonValue(r'meshtastic-longfast')
meshtasticLongfast(r'meshtastic-longfast'),
@JsonValue(r'meshtastic-mediumfast')
meshtasticMediumfast(r'meshtastic-mediumfast'),
@JsonValue(r'custom')
custom(r'custom');

const MeshcoreChannelConfigAirtimePresetEnum(this.value);

final String value;

@override
String toString() => value;
}


enum MeshcoreChannelConfigProfileEnum {
@JsonValue(r'text')
text(r'text'),
@JsonValue(r'cbor')
cbor(r'cbor');

const MeshcoreChannelConfigProfileEnum(this.value);

final String value;

@override
String toString() => value;
}


/// The channel takes nothing below this severity, whatever its top-level minSeverity
enum MeshcoreChannelConfigMinSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const MeshcoreChannelConfigMinSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


enum MeshcoreChannelConfigAllowedSendersEnum {
@JsonValue(r'report')
report(r'report'),
@JsonValue(r'ack')
ack(r'ack');

const MeshcoreChannelConfigAllowedSendersEnum(this.value);

final String value;

@override
String toString() => value;
}


