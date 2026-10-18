//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mqtt_channel_config.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MqttChannelConfig {
  /// Returns a new [MqttChannelConfig] instance.
  MqttChannelConfig({

    required  this.brokerUrl,

     this.topicPrefix = 'foresignal',

     this.qos,

     this.protocolVersion,

     this.messageExpiry = '24h',

     this.payloadTemplate,

     this.publishHealth = true,

     this.publishSummary = true,

     this.summaryInterval = '60s',

     this.haDiscovery = false,

     this.haPrefix = 'homeassistant',

     this.haMaxRules = 50,

     this.haMaxSources = 100,

     this.haRetainDiscovery = false,
  });

      /// mqtt://, mqtts://, ws:// or wss:// (WebSocket URLs may carry a path); no credentials, query or fragment. The host passes the outbound URL policy (profile LAN_ALLOWED)
  @JsonKey(
    
    name: r'brokerUrl',
    required: true,
    includeIfNull: false,
  )


  final String brokerUrl;



      /// Topic prefix; leading and trailing slashes are dropped. No + or # wildcards
  @JsonKey(
    defaultValue: 'foresignal',
    name: r'topicPrefix',
    required: false,
    includeIfNull: false,
  )


  final String? topicPrefix;



      /// QoS of event messages; a delivery succeeds on PUBACK (1) or a completed write (0). Retained state and discovery always use QoS 1
  @JsonKey(
    
    name: r'qos',
    required: false,
    includeIfNull: false,
  )


  final MqttChannelConfigQosEnum? qos;



  @JsonKey(
    
    name: r'protocolVersion',
    required: false,
    includeIfNull: false,
  )


  final MqttChannelConfigProtocolVersionEnum? protocolVersion;



      /// MQTT 5 message expiry of non-retained event messages (ignored on 3.1.1). ISO-8601 duration or shorthand
  @JsonKey(
    defaultValue: '24h',
    name: r'messageExpiry',
    required: false,
    includeIfNull: false,
  )


  final String? messageExpiry;



      /// Optional JSON with JSON-escaped {{…}} placeholders, like webhook `bodyTemplate`; replaces every event payload except the channel test
  @JsonKey(
    
    name: r'payloadTemplate',
    required: false,
    includeIfNull: false,
  )


  final String? payloadTemplate;



  @JsonKey(
    defaultValue: true,
    name: r'publishHealth',
    required: false,
    includeIfNull: false,
  )


  final bool? publishHealth;



  @JsonKey(
    defaultValue: true,
    name: r'publishSummary',
    required: false,
    includeIfNull: false,
  )


  final bool? publishSummary;



      /// How often `{prefix}/summary` is checked (at least 10s); it is published only when the glance changed. ISO-8601 duration (PT1M) or shorthand (60s, 15m)
  @JsonKey(
    defaultValue: '60s',
    name: r'summaryInterval',
    required: false,
    includeIfNull: false,
  )


  final String? summaryInterval;



  @JsonKey(
    defaultValue: false,
    name: r'haDiscovery',
    required: false,
    includeIfNull: false,
  )


  final bool? haDiscovery;



      /// Home Assistant discovery prefix; Foresignal subscribes to `{haPrefix}/status` for the birth message
  @JsonKey(
    defaultValue: 'homeassistant',
    name: r'haPrefix',
    required: false,
    includeIfNull: false,
  )


  final String? haPrefix;



      /// Most enabled rules given a `problem` binary_sensor, critical first, then by name
          // minimum: 0
          // maximum: 500
  @JsonKey(
    defaultValue: 50,
    name: r'haMaxRules',
    required: false,
    includeIfNull: false,
  )


  final int? haMaxRules;



      /// Most non-private, non-disabled sources given a `problem` binary_sensor, by id
          // minimum: 0
          // maximum: 500
  @JsonKey(
    defaultValue: 100,
    name: r'haMaxSources',
    required: false,
    includeIfNull: false,
  )


  final int? haMaxSources;



      /// Retain the discovery config. Off by default: Home Assistant prefers discovery re-published on its birth message, because retained configs can leave ghost entities
  @JsonKey(
    defaultValue: false,
    name: r'haRetainDiscovery',
    required: false,
    includeIfNull: false,
  )


  final bool? haRetainDiscovery;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MqttChannelConfig &&
      other.brokerUrl == brokerUrl &&
      other.topicPrefix == topicPrefix &&
      other.qos == qos &&
      other.protocolVersion == protocolVersion &&
      other.messageExpiry == messageExpiry &&
      other.payloadTemplate == payloadTemplate &&
      other.publishHealth == publishHealth &&
      other.publishSummary == publishSummary &&
      other.summaryInterval == summaryInterval &&
      other.haDiscovery == haDiscovery &&
      other.haPrefix == haPrefix &&
      other.haMaxRules == haMaxRules &&
      other.haMaxSources == haMaxSources &&
      other.haRetainDiscovery == haRetainDiscovery;

    @override
    int get hashCode =>
        brokerUrl.hashCode +
        topicPrefix.hashCode +
        qos.hashCode +
        protocolVersion.hashCode +
        messageExpiry.hashCode +
        (payloadTemplate == null ? 0 : payloadTemplate.hashCode) +
        publishHealth.hashCode +
        publishSummary.hashCode +
        summaryInterval.hashCode +
        haDiscovery.hashCode +
        haPrefix.hashCode +
        haMaxRules.hashCode +
        haMaxSources.hashCode +
        haRetainDiscovery.hashCode;

  factory MqttChannelConfig.fromJson(Map<String, dynamic> json) => _$MqttChannelConfigFromJson(json);

  Map<String, dynamic> toJson() => _$MqttChannelConfigToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// QoS of event messages; a delivery succeeds on PUBACK (1) or a completed write (0). Retained state and discovery always use QoS 1
enum MqttChannelConfigQosEnum {
@JsonValue(0)
number0('0'),
@JsonValue(1)
number1('1');

const MqttChannelConfigQosEnum(this.value);

final String value;

@override
String toString() => value;
}


enum MqttChannelConfigProtocolVersionEnum {
@JsonValue(r'5')
n5(r'5'),
@JsonValue(r'3.1.1')
n3period1Period1(r'3.1.1');

const MqttChannelConfigProtocolVersionEnum(this.value);

final String value;

@override
String toString() => value;
}


