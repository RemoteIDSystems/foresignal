// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'meshtastic_channel_config.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MeshtasticChannelConfigCWProxy {
  MeshtasticChannelConfig region(MeshtasticChannelConfigRegionEnum region);

  MeshtasticChannelConfig airtimePreset(
    MeshtasticChannelConfigAirtimePresetEnum? airtimePreset,
  );

  MeshtasticChannelConfig airtimeCustom(
    MeshChannelCommonConfigAirtimeCustom? airtimeCustom,
  );

  MeshtasticChannelConfig dutyCyclePercent(num? dutyCyclePercent);

  MeshtasticChannelConfig minSpacing(String? minSpacing);

  MeshtasticChannelConfig maxMessagesPerHour(int? maxMessagesPerHour);

  MeshtasticChannelConfig profile(MeshtasticChannelConfigProfileEnum? profile);

  MeshtasticChannelConfig minSeverity(
    MeshtasticChannelConfigMinSeverityEnum? minSeverity,
  );

  MeshtasticChannelConfig reportsEnabled(bool? reportsEnabled);

  MeshtasticChannelConfig allowedSenders(
    Map<String, MeshtasticChannelConfigAllowedSendersEnum>? allowedSenders,
  );

  MeshtasticChannelConfig compactEvents(bool? compactEvents);

  MeshtasticChannelConfig brokerUrl(String brokerUrl);

  MeshtasticChannelConfig root(String? root);

  MeshtasticChannelConfig gatewayNodeNum(String gatewayNodeNum);

  MeshtasticChannelConfig channelIndex(int channelIndex);

  MeshtasticChannelConfig channelName(String channelName);

  MeshtasticChannelConfig toNodeNum(String? toNodeNum);

  MeshtasticChannelConfig topicSuffixNodeId(bool? topicSuffixNodeId);

  MeshtasticChannelConfig licensedMode(bool? licensedMode);

  MeshtasticChannelConfig privateChannelConfirmed(bool privateChannelConfirmed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MeshtasticChannelConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MeshtasticChannelConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  MeshtasticChannelConfig call({
    MeshtasticChannelConfigRegionEnum region,
    MeshtasticChannelConfigAirtimePresetEnum? airtimePreset,
    MeshChannelCommonConfigAirtimeCustom? airtimeCustom,
    num? dutyCyclePercent,
    String? minSpacing,
    int? maxMessagesPerHour,
    MeshtasticChannelConfigProfileEnum? profile,
    MeshtasticChannelConfigMinSeverityEnum? minSeverity,
    bool? reportsEnabled,
    Map<String, MeshtasticChannelConfigAllowedSendersEnum>? allowedSenders,
    bool? compactEvents,
    String brokerUrl,
    String? root,
    String gatewayNodeNum,
    int channelIndex,
    String channelName,
    String? toNodeNum,
    bool? topicSuffixNodeId,
    bool? licensedMode,
    bool privateChannelConfirmed,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMeshtasticChannelConfig.copyWith(...)` or call `instanceOfMeshtasticChannelConfig.copyWith.fieldName(value)` for a single field.
class _$MeshtasticChannelConfigCWProxyImpl
    implements _$MeshtasticChannelConfigCWProxy {
  const _$MeshtasticChannelConfigCWProxyImpl(this._value);

  final MeshtasticChannelConfig _value;

  @override
  MeshtasticChannelConfig region(MeshtasticChannelConfigRegionEnum region) =>
      call(region: region);

  @override
  MeshtasticChannelConfig airtimePreset(
    MeshtasticChannelConfigAirtimePresetEnum? airtimePreset,
  ) => call(airtimePreset: airtimePreset);

  @override
  MeshtasticChannelConfig airtimeCustom(
    MeshChannelCommonConfigAirtimeCustom? airtimeCustom,
  ) => call(airtimeCustom: airtimeCustom);

  @override
  MeshtasticChannelConfig dutyCyclePercent(num? dutyCyclePercent) =>
      call(dutyCyclePercent: dutyCyclePercent);

  @override
  MeshtasticChannelConfig minSpacing(String? minSpacing) =>
      call(minSpacing: minSpacing);

  @override
  MeshtasticChannelConfig maxMessagesPerHour(int? maxMessagesPerHour) =>
      call(maxMessagesPerHour: maxMessagesPerHour);

  @override
  MeshtasticChannelConfig profile(
    MeshtasticChannelConfigProfileEnum? profile,
  ) => call(profile: profile);

  @override
  MeshtasticChannelConfig minSeverity(
    MeshtasticChannelConfigMinSeverityEnum? minSeverity,
  ) => call(minSeverity: minSeverity);

  @override
  MeshtasticChannelConfig reportsEnabled(bool? reportsEnabled) =>
      call(reportsEnabled: reportsEnabled);

  @override
  MeshtasticChannelConfig allowedSenders(
    Map<String, MeshtasticChannelConfigAllowedSendersEnum>? allowedSenders,
  ) => call(allowedSenders: allowedSenders);

  @override
  MeshtasticChannelConfig compactEvents(bool? compactEvents) =>
      call(compactEvents: compactEvents);

  @override
  MeshtasticChannelConfig brokerUrl(String brokerUrl) =>
      call(brokerUrl: brokerUrl);

  @override
  MeshtasticChannelConfig root(String? root) => call(root: root);

  @override
  MeshtasticChannelConfig gatewayNodeNum(String gatewayNodeNum) =>
      call(gatewayNodeNum: gatewayNodeNum);

  @override
  MeshtasticChannelConfig channelIndex(int channelIndex) =>
      call(channelIndex: channelIndex);

  @override
  MeshtasticChannelConfig channelName(String channelName) =>
      call(channelName: channelName);

  @override
  MeshtasticChannelConfig toNodeNum(String? toNodeNum) =>
      call(toNodeNum: toNodeNum);

  @override
  MeshtasticChannelConfig topicSuffixNodeId(bool? topicSuffixNodeId) =>
      call(topicSuffixNodeId: topicSuffixNodeId);

  @override
  MeshtasticChannelConfig licensedMode(bool? licensedMode) =>
      call(licensedMode: licensedMode);

  @override
  MeshtasticChannelConfig privateChannelConfirmed(
    bool privateChannelConfirmed,
  ) => call(privateChannelConfirmed: privateChannelConfirmed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MeshtasticChannelConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MeshtasticChannelConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MeshtasticChannelConfig call({
    Object? region = const $CopyWithPlaceholder(),
    Object? airtimePreset = const $CopyWithPlaceholder(),
    Object? airtimeCustom = const $CopyWithPlaceholder(),
    Object? dutyCyclePercent = const $CopyWithPlaceholder(),
    Object? minSpacing = const $CopyWithPlaceholder(),
    Object? maxMessagesPerHour = const $CopyWithPlaceholder(),
    Object? profile = const $CopyWithPlaceholder(),
    Object? minSeverity = const $CopyWithPlaceholder(),
    Object? reportsEnabled = const $CopyWithPlaceholder(),
    Object? allowedSenders = const $CopyWithPlaceholder(),
    Object? compactEvents = const $CopyWithPlaceholder(),
    Object? brokerUrl = const $CopyWithPlaceholder(),
    Object? root = const $CopyWithPlaceholder(),
    Object? gatewayNodeNum = const $CopyWithPlaceholder(),
    Object? channelIndex = const $CopyWithPlaceholder(),
    Object? channelName = const $CopyWithPlaceholder(),
    Object? toNodeNum = const $CopyWithPlaceholder(),
    Object? topicSuffixNodeId = const $CopyWithPlaceholder(),
    Object? licensedMode = const $CopyWithPlaceholder(),
    Object? privateChannelConfirmed = const $CopyWithPlaceholder(),
  }) {
    return MeshtasticChannelConfig(
      region: region == const $CopyWithPlaceholder() || region == null
          ? _value.region
          // ignore: cast_nullable_to_non_nullable
          : region as MeshtasticChannelConfigRegionEnum,
      airtimePreset: airtimePreset == const $CopyWithPlaceholder()
          ? _value.airtimePreset
          // ignore: cast_nullable_to_non_nullable
          : airtimePreset as MeshtasticChannelConfigAirtimePresetEnum?,
      airtimeCustom: airtimeCustom == const $CopyWithPlaceholder()
          ? _value.airtimeCustom
          // ignore: cast_nullable_to_non_nullable
          : airtimeCustom as MeshChannelCommonConfigAirtimeCustom?,
      dutyCyclePercent: dutyCyclePercent == const $CopyWithPlaceholder()
          ? _value.dutyCyclePercent
          // ignore: cast_nullable_to_non_nullable
          : dutyCyclePercent as num?,
      minSpacing: minSpacing == const $CopyWithPlaceholder()
          ? _value.minSpacing
          // ignore: cast_nullable_to_non_nullable
          : minSpacing as String?,
      maxMessagesPerHour: maxMessagesPerHour == const $CopyWithPlaceholder()
          ? _value.maxMessagesPerHour
          // ignore: cast_nullable_to_non_nullable
          : maxMessagesPerHour as int?,
      profile: profile == const $CopyWithPlaceholder()
          ? _value.profile
          // ignore: cast_nullable_to_non_nullable
          : profile as MeshtasticChannelConfigProfileEnum?,
      minSeverity: minSeverity == const $CopyWithPlaceholder()
          ? _value.minSeverity
          // ignore: cast_nullable_to_non_nullable
          : minSeverity as MeshtasticChannelConfigMinSeverityEnum?,
      reportsEnabled: reportsEnabled == const $CopyWithPlaceholder()
          ? _value.reportsEnabled
          // ignore: cast_nullable_to_non_nullable
          : reportsEnabled as bool?,
      allowedSenders: allowedSenders == const $CopyWithPlaceholder()
          ? _value.allowedSenders
          // ignore: cast_nullable_to_non_nullable
          : allowedSenders
                as Map<String, MeshtasticChannelConfigAllowedSendersEnum>?,
      compactEvents: compactEvents == const $CopyWithPlaceholder()
          ? _value.compactEvents
          // ignore: cast_nullable_to_non_nullable
          : compactEvents as bool?,
      brokerUrl: brokerUrl == const $CopyWithPlaceholder() || brokerUrl == null
          ? _value.brokerUrl
          // ignore: cast_nullable_to_non_nullable
          : brokerUrl as String,
      root: root == const $CopyWithPlaceholder()
          ? _value.root
          // ignore: cast_nullable_to_non_nullable
          : root as String?,
      gatewayNodeNum:
          gatewayNodeNum == const $CopyWithPlaceholder() ||
              gatewayNodeNum == null
          ? _value.gatewayNodeNum
          // ignore: cast_nullable_to_non_nullable
          : gatewayNodeNum as String,
      channelIndex:
          channelIndex == const $CopyWithPlaceholder() || channelIndex == null
          ? _value.channelIndex
          // ignore: cast_nullable_to_non_nullable
          : channelIndex as int,
      channelName:
          channelName == const $CopyWithPlaceholder() || channelName == null
          ? _value.channelName
          // ignore: cast_nullable_to_non_nullable
          : channelName as String,
      toNodeNum: toNodeNum == const $CopyWithPlaceholder()
          ? _value.toNodeNum
          // ignore: cast_nullable_to_non_nullable
          : toNodeNum as String?,
      topicSuffixNodeId: topicSuffixNodeId == const $CopyWithPlaceholder()
          ? _value.topicSuffixNodeId
          // ignore: cast_nullable_to_non_nullable
          : topicSuffixNodeId as bool?,
      licensedMode: licensedMode == const $CopyWithPlaceholder()
          ? _value.licensedMode
          // ignore: cast_nullable_to_non_nullable
          : licensedMode as bool?,
      privateChannelConfirmed:
          privateChannelConfirmed == const $CopyWithPlaceholder() ||
              privateChannelConfirmed == null
          ? _value.privateChannelConfirmed
          // ignore: cast_nullable_to_non_nullable
          : privateChannelConfirmed as bool,
    );
  }
}

extension $MeshtasticChannelConfigCopyWith on MeshtasticChannelConfig {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMeshtasticChannelConfig.copyWith(...)` or `instanceOfMeshtasticChannelConfig.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MeshtasticChannelConfigCWProxy get copyWith =>
      _$MeshtasticChannelConfigCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MeshtasticChannelConfig _$MeshtasticChannelConfigFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MeshtasticChannelConfig', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'region',
      'brokerUrl',
      'gatewayNodeNum',
      'channelIndex',
      'channelName',
      'privateChannelConfirmed',
    ],
  );
  final val = MeshtasticChannelConfig(
    region: $checkedConvert(
      'region',
      (v) => $enumDecode(_$MeshtasticChannelConfigRegionEnumEnumMap, v),
    ),
    airtimePreset: $checkedConvert(
      'airtimePreset',
      (v) => $enumDecodeNullable(
        _$MeshtasticChannelConfigAirtimePresetEnumEnumMap,
        v,
      ),
    ),
    airtimeCustom: $checkedConvert(
      'airtimeCustom',
      (v) => v == null
          ? null
          : MeshChannelCommonConfigAirtimeCustom.fromJson(
              v as Map<String, dynamic>,
            ),
    ),
    dutyCyclePercent: $checkedConvert(
      'dutyCyclePercent',
      (v) => v as num? ?? 1.0,
    ),
    minSpacing: $checkedConvert('minSpacing', (v) => v as String? ?? '30s'),
    maxMessagesPerHour: $checkedConvert(
      'maxMessagesPerHour',
      (v) => (v as num?)?.toInt() ?? 20,
    ),
    profile: $checkedConvert(
      'profile',
      (v) =>
          $enumDecodeNullable(_$MeshtasticChannelConfigProfileEnumEnumMap, v),
    ),
    minSeverity: $checkedConvert(
      'minSeverity',
      (v) => $enumDecodeNullable(
        _$MeshtasticChannelConfigMinSeverityEnumEnumMap,
        v,
      ),
    ),
    reportsEnabled: $checkedConvert(
      'reportsEnabled',
      (v) => v as bool? ?? false,
    ),
    allowedSenders: $checkedConvert(
      'allowedSenders',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(
          k,
          $enumDecode(_$MeshtasticChannelConfigAllowedSendersEnumEnumMap, e),
        ),
      ),
    ),
    compactEvents: $checkedConvert('compactEvents', (v) => v as bool? ?? false),
    brokerUrl: $checkedConvert('brokerUrl', (v) => v as String),
    root: $checkedConvert('root', (v) => v as String? ?? 'msh/local'),
    gatewayNodeNum: $checkedConvert('gatewayNodeNum', (v) => v as String),
    channelIndex: $checkedConvert('channelIndex', (v) => (v as num).toInt()),
    channelName: $checkedConvert('channelName', (v) => v as String),
    toNodeNum: $checkedConvert('toNodeNum', (v) => v as String?),
    topicSuffixNodeId: $checkedConvert(
      'topicSuffixNodeId',
      (v) => v as bool? ?? false,
    ),
    licensedMode: $checkedConvert('licensedMode', (v) => v as bool? ?? false),
    privateChannelConfirmed: $checkedConvert(
      'privateChannelConfirmed',
      (v) => v as bool,
    ),
  );
  return val;
});

Map<String, dynamic> _$MeshtasticChannelConfigToJson(
  MeshtasticChannelConfig instance,
) => <String, dynamic>{
  'region': _$MeshtasticChannelConfigRegionEnumEnumMap[instance.region]!,
  'airtimePreset':
      ?_$MeshtasticChannelConfigAirtimePresetEnumEnumMap[instance
          .airtimePreset],
  'airtimeCustom': ?instance.airtimeCustom?.toJson(),
  'dutyCyclePercent': ?instance.dutyCyclePercent,
  'minSpacing': ?instance.minSpacing,
  'maxMessagesPerHour': ?instance.maxMessagesPerHour,
  'profile': ?_$MeshtasticChannelConfigProfileEnumEnumMap[instance.profile],
  'minSeverity':
      ?_$MeshtasticChannelConfigMinSeverityEnumEnumMap[instance.minSeverity],
  'reportsEnabled': ?instance.reportsEnabled,
  'allowedSenders': ?instance.allowedSenders?.map(
    (k, e) =>
        MapEntry(k, _$MeshtasticChannelConfigAllowedSendersEnumEnumMap[e]!),
  ),
  'compactEvents': ?instance.compactEvents,
  'brokerUrl': instance.brokerUrl,
  'root': ?instance.root,
  'gatewayNodeNum': instance.gatewayNodeNum,
  'channelIndex': instance.channelIndex,
  'channelName': instance.channelName,
  'toNodeNum': ?instance.toNodeNum,
  'topicSuffixNodeId': ?instance.topicSuffixNodeId,
  'licensedMode': ?instance.licensedMode,
  'privateChannelConfirmed': instance.privateChannelConfirmed,
};

const _$MeshtasticChannelConfigRegionEnumEnumMap = {
  MeshtasticChannelConfigRegionEnum.EU_868: 'EU_868',
  MeshtasticChannelConfigRegionEnum.EU_433: 'EU_433',
  MeshtasticChannelConfigRegionEnum.UK_868: 'UK_868',
  MeshtasticChannelConfigRegionEnum.US: 'US',
  MeshtasticChannelConfigRegionEnum.ANZ: 'ANZ',
  MeshtasticChannelConfigRegionEnum.OTHER: 'OTHER',
};

const _$MeshtasticChannelConfigAirtimePresetEnumEnumMap = {
  MeshtasticChannelConfigAirtimePresetEnum.meshtasticLongfast:
      'meshtastic-longfast',
  MeshtasticChannelConfigAirtimePresetEnum.meshtasticMediumfast:
      'meshtastic-mediumfast',
  MeshtasticChannelConfigAirtimePresetEnum.custom: 'custom',
};

const _$MeshtasticChannelConfigProfileEnumEnumMap = {
  MeshtasticChannelConfigProfileEnum.text: 'text',
  MeshtasticChannelConfigProfileEnum.cbor: 'cbor',
};

const _$MeshtasticChannelConfigMinSeverityEnumEnumMap = {
  MeshtasticChannelConfigMinSeverityEnum.info: 'info',
  MeshtasticChannelConfigMinSeverityEnum.warning: 'warning',
  MeshtasticChannelConfigMinSeverityEnum.critical: 'critical',
};

const _$MeshtasticChannelConfigAllowedSendersEnumEnumMap = {
  MeshtasticChannelConfigAllowedSendersEnum.report: 'report',
  MeshtasticChannelConfigAllowedSendersEnum.ack: 'ack',
};
