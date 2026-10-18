// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mqtt_channel_config.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MqttChannelConfigCWProxy {
  MqttChannelConfig brokerUrl(String brokerUrl);

  MqttChannelConfig topicPrefix(String? topicPrefix);

  MqttChannelConfig qos(MqttChannelConfigQosEnum? qos);

  MqttChannelConfig protocolVersion(
    MqttChannelConfigProtocolVersionEnum? protocolVersion,
  );

  MqttChannelConfig messageExpiry(String? messageExpiry);

  MqttChannelConfig payloadTemplate(String? payloadTemplate);

  MqttChannelConfig publishHealth(bool? publishHealth);

  MqttChannelConfig publishSummary(bool? publishSummary);

  MqttChannelConfig summaryInterval(String? summaryInterval);

  MqttChannelConfig haDiscovery(bool? haDiscovery);

  MqttChannelConfig haPrefix(String? haPrefix);

  MqttChannelConfig haMaxRules(int? haMaxRules);

  MqttChannelConfig haMaxSources(int? haMaxSources);

  MqttChannelConfig haRetainDiscovery(bool? haRetainDiscovery);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MqttChannelConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MqttChannelConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  MqttChannelConfig call({
    String brokerUrl,
    String? topicPrefix,
    MqttChannelConfigQosEnum? qos,
    MqttChannelConfigProtocolVersionEnum? protocolVersion,
    String? messageExpiry,
    String? payloadTemplate,
    bool? publishHealth,
    bool? publishSummary,
    String? summaryInterval,
    bool? haDiscovery,
    String? haPrefix,
    int? haMaxRules,
    int? haMaxSources,
    bool? haRetainDiscovery,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMqttChannelConfig.copyWith(...)` or call `instanceOfMqttChannelConfig.copyWith.fieldName(value)` for a single field.
class _$MqttChannelConfigCWProxyImpl implements _$MqttChannelConfigCWProxy {
  const _$MqttChannelConfigCWProxyImpl(this._value);

  final MqttChannelConfig _value;

  @override
  MqttChannelConfig brokerUrl(String brokerUrl) => call(brokerUrl: brokerUrl);

  @override
  MqttChannelConfig topicPrefix(String? topicPrefix) =>
      call(topicPrefix: topicPrefix);

  @override
  MqttChannelConfig qos(MqttChannelConfigQosEnum? qos) => call(qos: qos);

  @override
  MqttChannelConfig protocolVersion(
    MqttChannelConfigProtocolVersionEnum? protocolVersion,
  ) => call(protocolVersion: protocolVersion);

  @override
  MqttChannelConfig messageExpiry(String? messageExpiry) =>
      call(messageExpiry: messageExpiry);

  @override
  MqttChannelConfig payloadTemplate(String? payloadTemplate) =>
      call(payloadTemplate: payloadTemplate);

  @override
  MqttChannelConfig publishHealth(bool? publishHealth) =>
      call(publishHealth: publishHealth);

  @override
  MqttChannelConfig publishSummary(bool? publishSummary) =>
      call(publishSummary: publishSummary);

  @override
  MqttChannelConfig summaryInterval(String? summaryInterval) =>
      call(summaryInterval: summaryInterval);

  @override
  MqttChannelConfig haDiscovery(bool? haDiscovery) =>
      call(haDiscovery: haDiscovery);

  @override
  MqttChannelConfig haPrefix(String? haPrefix) => call(haPrefix: haPrefix);

  @override
  MqttChannelConfig haMaxRules(int? haMaxRules) => call(haMaxRules: haMaxRules);

  @override
  MqttChannelConfig haMaxSources(int? haMaxSources) =>
      call(haMaxSources: haMaxSources);

  @override
  MqttChannelConfig haRetainDiscovery(bool? haRetainDiscovery) =>
      call(haRetainDiscovery: haRetainDiscovery);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MqttChannelConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MqttChannelConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MqttChannelConfig call({
    Object? brokerUrl = const $CopyWithPlaceholder(),
    Object? topicPrefix = const $CopyWithPlaceholder(),
    Object? qos = const $CopyWithPlaceholder(),
    Object? protocolVersion = const $CopyWithPlaceholder(),
    Object? messageExpiry = const $CopyWithPlaceholder(),
    Object? payloadTemplate = const $CopyWithPlaceholder(),
    Object? publishHealth = const $CopyWithPlaceholder(),
    Object? publishSummary = const $CopyWithPlaceholder(),
    Object? summaryInterval = const $CopyWithPlaceholder(),
    Object? haDiscovery = const $CopyWithPlaceholder(),
    Object? haPrefix = const $CopyWithPlaceholder(),
    Object? haMaxRules = const $CopyWithPlaceholder(),
    Object? haMaxSources = const $CopyWithPlaceholder(),
    Object? haRetainDiscovery = const $CopyWithPlaceholder(),
  }) {
    return MqttChannelConfig(
      brokerUrl: brokerUrl == const $CopyWithPlaceholder() || brokerUrl == null
          ? _value.brokerUrl
          // ignore: cast_nullable_to_non_nullable
          : brokerUrl as String,
      topicPrefix: topicPrefix == const $CopyWithPlaceholder()
          ? _value.topicPrefix
          // ignore: cast_nullable_to_non_nullable
          : topicPrefix as String?,
      qos: qos == const $CopyWithPlaceholder()
          ? _value.qos
          // ignore: cast_nullable_to_non_nullable
          : qos as MqttChannelConfigQosEnum?,
      protocolVersion: protocolVersion == const $CopyWithPlaceholder()
          ? _value.protocolVersion
          // ignore: cast_nullable_to_non_nullable
          : protocolVersion as MqttChannelConfigProtocolVersionEnum?,
      messageExpiry: messageExpiry == const $CopyWithPlaceholder()
          ? _value.messageExpiry
          // ignore: cast_nullable_to_non_nullable
          : messageExpiry as String?,
      payloadTemplate: payloadTemplate == const $CopyWithPlaceholder()
          ? _value.payloadTemplate
          // ignore: cast_nullable_to_non_nullable
          : payloadTemplate as String?,
      publishHealth: publishHealth == const $CopyWithPlaceholder()
          ? _value.publishHealth
          // ignore: cast_nullable_to_non_nullable
          : publishHealth as bool?,
      publishSummary: publishSummary == const $CopyWithPlaceholder()
          ? _value.publishSummary
          // ignore: cast_nullable_to_non_nullable
          : publishSummary as bool?,
      summaryInterval: summaryInterval == const $CopyWithPlaceholder()
          ? _value.summaryInterval
          // ignore: cast_nullable_to_non_nullable
          : summaryInterval as String?,
      haDiscovery: haDiscovery == const $CopyWithPlaceholder()
          ? _value.haDiscovery
          // ignore: cast_nullable_to_non_nullable
          : haDiscovery as bool?,
      haPrefix: haPrefix == const $CopyWithPlaceholder()
          ? _value.haPrefix
          // ignore: cast_nullable_to_non_nullable
          : haPrefix as String?,
      haMaxRules: haMaxRules == const $CopyWithPlaceholder()
          ? _value.haMaxRules
          // ignore: cast_nullable_to_non_nullable
          : haMaxRules as int?,
      haMaxSources: haMaxSources == const $CopyWithPlaceholder()
          ? _value.haMaxSources
          // ignore: cast_nullable_to_non_nullable
          : haMaxSources as int?,
      haRetainDiscovery: haRetainDiscovery == const $CopyWithPlaceholder()
          ? _value.haRetainDiscovery
          // ignore: cast_nullable_to_non_nullable
          : haRetainDiscovery as bool?,
    );
  }
}

extension $MqttChannelConfigCopyWith on MqttChannelConfig {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMqttChannelConfig.copyWith(...)` or `instanceOfMqttChannelConfig.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MqttChannelConfigCWProxy get copyWith =>
      _$MqttChannelConfigCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MqttChannelConfig _$MqttChannelConfigFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MqttChannelConfig', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['brokerUrl']);
  final val = MqttChannelConfig(
    brokerUrl: $checkedConvert('brokerUrl', (v) => v as String),
    topicPrefix: $checkedConvert(
      'topicPrefix',
      (v) => v as String? ?? 'foresignal',
    ),
    qos: $checkedConvert(
      'qos',
      (v) => $enumDecodeNullable(_$MqttChannelConfigQosEnumEnumMap, v),
    ),
    protocolVersion: $checkedConvert(
      'protocolVersion',
      (v) =>
          $enumDecodeNullable(_$MqttChannelConfigProtocolVersionEnumEnumMap, v),
    ),
    messageExpiry: $checkedConvert(
      'messageExpiry',
      (v) => v as String? ?? '24h',
    ),
    payloadTemplate: $checkedConvert('payloadTemplate', (v) => v as String?),
    publishHealth: $checkedConvert('publishHealth', (v) => v as bool? ?? true),
    publishSummary: $checkedConvert(
      'publishSummary',
      (v) => v as bool? ?? true,
    ),
    summaryInterval: $checkedConvert(
      'summaryInterval',
      (v) => v as String? ?? '60s',
    ),
    haDiscovery: $checkedConvert('haDiscovery', (v) => v as bool? ?? false),
    haPrefix: $checkedConvert(
      'haPrefix',
      (v) => v as String? ?? 'homeassistant',
    ),
    haMaxRules: $checkedConvert(
      'haMaxRules',
      (v) => (v as num?)?.toInt() ?? 50,
    ),
    haMaxSources: $checkedConvert(
      'haMaxSources',
      (v) => (v as num?)?.toInt() ?? 100,
    ),
    haRetainDiscovery: $checkedConvert(
      'haRetainDiscovery',
      (v) => v as bool? ?? false,
    ),
  );
  return val;
});

Map<String, dynamic> _$MqttChannelConfigToJson(
  MqttChannelConfig instance,
) => <String, dynamic>{
  'brokerUrl': instance.brokerUrl,
  'topicPrefix': ?instance.topicPrefix,
  'qos': ?_$MqttChannelConfigQosEnumEnumMap[instance.qos],
  'protocolVersion':
      ?_$MqttChannelConfigProtocolVersionEnumEnumMap[instance.protocolVersion],
  'messageExpiry': ?instance.messageExpiry,
  'payloadTemplate': ?instance.payloadTemplate,
  'publishHealth': ?instance.publishHealth,
  'publishSummary': ?instance.publishSummary,
  'summaryInterval': ?instance.summaryInterval,
  'haDiscovery': ?instance.haDiscovery,
  'haPrefix': ?instance.haPrefix,
  'haMaxRules': ?instance.haMaxRules,
  'haMaxSources': ?instance.haMaxSources,
  'haRetainDiscovery': ?instance.haRetainDiscovery,
};

const _$MqttChannelConfigQosEnumEnumMap = {
  MqttChannelConfigQosEnum.number0: 0,
  MqttChannelConfigQosEnum.number1: 1,
};

const _$MqttChannelConfigProtocolVersionEnumEnumMap = {
  MqttChannelConfigProtocolVersionEnum.n5: '5',
  MqttChannelConfigProtocolVersionEnum.n3period1Period1: '3.1.1',
};
