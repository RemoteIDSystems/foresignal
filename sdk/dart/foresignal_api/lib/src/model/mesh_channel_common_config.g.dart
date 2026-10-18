// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mesh_channel_common_config.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MeshChannelCommonConfigCWProxy {
  MeshChannelCommonConfig region(MeshChannelCommonConfigRegionEnum region);

  MeshChannelCommonConfig airtimePreset(
    MeshChannelCommonConfigAirtimePresetEnum? airtimePreset,
  );

  MeshChannelCommonConfig airtimeCustom(
    MeshChannelCommonConfigAirtimeCustom? airtimeCustom,
  );

  MeshChannelCommonConfig dutyCyclePercent(num? dutyCyclePercent);

  MeshChannelCommonConfig minSpacing(String? minSpacing);

  MeshChannelCommonConfig maxMessagesPerHour(int? maxMessagesPerHour);

  MeshChannelCommonConfig profile(MeshChannelCommonConfigProfileEnum? profile);

  MeshChannelCommonConfig minSeverity(
    MeshChannelCommonConfigMinSeverityEnum? minSeverity,
  );

  MeshChannelCommonConfig reportsEnabled(bool? reportsEnabled);

  MeshChannelCommonConfig allowedSenders(
    Map<String, MeshChannelCommonConfigAllowedSendersEnum>? allowedSenders,
  );

  MeshChannelCommonConfig compactEvents(bool? compactEvents);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MeshChannelCommonConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MeshChannelCommonConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  MeshChannelCommonConfig call({
    MeshChannelCommonConfigRegionEnum region,
    MeshChannelCommonConfigAirtimePresetEnum? airtimePreset,
    MeshChannelCommonConfigAirtimeCustom? airtimeCustom,
    num? dutyCyclePercent,
    String? minSpacing,
    int? maxMessagesPerHour,
    MeshChannelCommonConfigProfileEnum? profile,
    MeshChannelCommonConfigMinSeverityEnum? minSeverity,
    bool? reportsEnabled,
    Map<String, MeshChannelCommonConfigAllowedSendersEnum>? allowedSenders,
    bool? compactEvents,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMeshChannelCommonConfig.copyWith(...)` or call `instanceOfMeshChannelCommonConfig.copyWith.fieldName(value)` for a single field.
class _$MeshChannelCommonConfigCWProxyImpl
    implements _$MeshChannelCommonConfigCWProxy {
  const _$MeshChannelCommonConfigCWProxyImpl(this._value);

  final MeshChannelCommonConfig _value;

  @override
  MeshChannelCommonConfig region(MeshChannelCommonConfigRegionEnum region) =>
      call(region: region);

  @override
  MeshChannelCommonConfig airtimePreset(
    MeshChannelCommonConfigAirtimePresetEnum? airtimePreset,
  ) => call(airtimePreset: airtimePreset);

  @override
  MeshChannelCommonConfig airtimeCustom(
    MeshChannelCommonConfigAirtimeCustom? airtimeCustom,
  ) => call(airtimeCustom: airtimeCustom);

  @override
  MeshChannelCommonConfig dutyCyclePercent(num? dutyCyclePercent) =>
      call(dutyCyclePercent: dutyCyclePercent);

  @override
  MeshChannelCommonConfig minSpacing(String? minSpacing) =>
      call(minSpacing: minSpacing);

  @override
  MeshChannelCommonConfig maxMessagesPerHour(int? maxMessagesPerHour) =>
      call(maxMessagesPerHour: maxMessagesPerHour);

  @override
  MeshChannelCommonConfig profile(
    MeshChannelCommonConfigProfileEnum? profile,
  ) => call(profile: profile);

  @override
  MeshChannelCommonConfig minSeverity(
    MeshChannelCommonConfigMinSeverityEnum? minSeverity,
  ) => call(minSeverity: minSeverity);

  @override
  MeshChannelCommonConfig reportsEnabled(bool? reportsEnabled) =>
      call(reportsEnabled: reportsEnabled);

  @override
  MeshChannelCommonConfig allowedSenders(
    Map<String, MeshChannelCommonConfigAllowedSendersEnum>? allowedSenders,
  ) => call(allowedSenders: allowedSenders);

  @override
  MeshChannelCommonConfig compactEvents(bool? compactEvents) =>
      call(compactEvents: compactEvents);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MeshChannelCommonConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MeshChannelCommonConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MeshChannelCommonConfig call({
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
  }) {
    return MeshChannelCommonConfig(
      region: region == const $CopyWithPlaceholder() || region == null
          ? _value.region
          // ignore: cast_nullable_to_non_nullable
          : region as MeshChannelCommonConfigRegionEnum,
      airtimePreset: airtimePreset == const $CopyWithPlaceholder()
          ? _value.airtimePreset
          // ignore: cast_nullable_to_non_nullable
          : airtimePreset as MeshChannelCommonConfigAirtimePresetEnum?,
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
          : profile as MeshChannelCommonConfigProfileEnum?,
      minSeverity: minSeverity == const $CopyWithPlaceholder()
          ? _value.minSeverity
          // ignore: cast_nullable_to_non_nullable
          : minSeverity as MeshChannelCommonConfigMinSeverityEnum?,
      reportsEnabled: reportsEnabled == const $CopyWithPlaceholder()
          ? _value.reportsEnabled
          // ignore: cast_nullable_to_non_nullable
          : reportsEnabled as bool?,
      allowedSenders: allowedSenders == const $CopyWithPlaceholder()
          ? _value.allowedSenders
          // ignore: cast_nullable_to_non_nullable
          : allowedSenders
                as Map<String, MeshChannelCommonConfigAllowedSendersEnum>?,
      compactEvents: compactEvents == const $CopyWithPlaceholder()
          ? _value.compactEvents
          // ignore: cast_nullable_to_non_nullable
          : compactEvents as bool?,
    );
  }
}

extension $MeshChannelCommonConfigCopyWith on MeshChannelCommonConfig {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMeshChannelCommonConfig.copyWith(...)` or `instanceOfMeshChannelCommonConfig.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MeshChannelCommonConfigCWProxy get copyWith =>
      _$MeshChannelCommonConfigCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MeshChannelCommonConfig _$MeshChannelCommonConfigFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MeshChannelCommonConfig', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['region']);
  final val = MeshChannelCommonConfig(
    region: $checkedConvert(
      'region',
      (v) => $enumDecode(_$MeshChannelCommonConfigRegionEnumEnumMap, v),
    ),
    airtimePreset: $checkedConvert(
      'airtimePreset',
      (v) => $enumDecodeNullable(
        _$MeshChannelCommonConfigAirtimePresetEnumEnumMap,
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
          $enumDecodeNullable(_$MeshChannelCommonConfigProfileEnumEnumMap, v),
    ),
    minSeverity: $checkedConvert(
      'minSeverity',
      (v) => $enumDecodeNullable(
        _$MeshChannelCommonConfigMinSeverityEnumEnumMap,
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
          $enumDecode(_$MeshChannelCommonConfigAllowedSendersEnumEnumMap, e),
        ),
      ),
    ),
    compactEvents: $checkedConvert('compactEvents', (v) => v as bool? ?? false),
  );
  return val;
});

Map<String, dynamic> _$MeshChannelCommonConfigToJson(
  MeshChannelCommonConfig instance,
) => <String, dynamic>{
  'region': _$MeshChannelCommonConfigRegionEnumEnumMap[instance.region]!,
  'airtimePreset':
      ?_$MeshChannelCommonConfigAirtimePresetEnumEnumMap[instance
          .airtimePreset],
  'airtimeCustom': ?instance.airtimeCustom?.toJson(),
  'dutyCyclePercent': ?instance.dutyCyclePercent,
  'minSpacing': ?instance.minSpacing,
  'maxMessagesPerHour': ?instance.maxMessagesPerHour,
  'profile': ?_$MeshChannelCommonConfigProfileEnumEnumMap[instance.profile],
  'minSeverity':
      ?_$MeshChannelCommonConfigMinSeverityEnumEnumMap[instance.minSeverity],
  'reportsEnabled': ?instance.reportsEnabled,
  'allowedSenders': ?instance.allowedSenders?.map(
    (k, e) =>
        MapEntry(k, _$MeshChannelCommonConfigAllowedSendersEnumEnumMap[e]!),
  ),
  'compactEvents': ?instance.compactEvents,
};

const _$MeshChannelCommonConfigRegionEnumEnumMap = {
  MeshChannelCommonConfigRegionEnum.EU_868: 'EU_868',
  MeshChannelCommonConfigRegionEnum.EU_433: 'EU_433',
  MeshChannelCommonConfigRegionEnum.UK_868: 'UK_868',
  MeshChannelCommonConfigRegionEnum.US: 'US',
  MeshChannelCommonConfigRegionEnum.ANZ: 'ANZ',
  MeshChannelCommonConfigRegionEnum.OTHER: 'OTHER',
};

const _$MeshChannelCommonConfigAirtimePresetEnumEnumMap = {
  MeshChannelCommonConfigAirtimePresetEnum.meshtasticLongfast:
      'meshtastic-longfast',
  MeshChannelCommonConfigAirtimePresetEnum.meshtasticMediumfast:
      'meshtastic-mediumfast',
  MeshChannelCommonConfigAirtimePresetEnum.custom: 'custom',
};

const _$MeshChannelCommonConfigProfileEnumEnumMap = {
  MeshChannelCommonConfigProfileEnum.text: 'text',
  MeshChannelCommonConfigProfileEnum.cbor: 'cbor',
};

const _$MeshChannelCommonConfigMinSeverityEnumEnumMap = {
  MeshChannelCommonConfigMinSeverityEnum.info: 'info',
  MeshChannelCommonConfigMinSeverityEnum.warning: 'warning',
  MeshChannelCommonConfigMinSeverityEnum.critical: 'critical',
};

const _$MeshChannelCommonConfigAllowedSendersEnumEnumMap = {
  MeshChannelCommonConfigAllowedSendersEnum.report: 'report',
  MeshChannelCommonConfigAllowedSendersEnum.ack: 'ack',
};
