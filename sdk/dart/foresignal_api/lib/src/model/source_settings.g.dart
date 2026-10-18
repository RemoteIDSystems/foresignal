// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'source_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SourceSettingsCWProxy {
  SourceSettings enabled(bool enabled);

  SourceSettings interval(String interval);

  SourceSettings params(Map<String, Object> params);

  SourceSettings budget(Budget budget);

  SourceSettings retention(String? retention);

  SourceSettings embed(bool? embed);

  SourceSettings credentialsSet(bool credentialsSet);

  SourceSettings updatedAt(DateTime? updatedAt);

  SourceSettings ingestPolicy(IngestPolicy? ingestPolicy);

  SourceSettings effectiveIngestPolicy(IngestPolicy? effectiveIngestPolicy);

  SourceSettings effectiveRetention(String? effectiveRetention);

  SourceSettings statsEnabled(bool? statsEnabled);

  SourceSettings statsIncludePrivate(bool? statsIncludePrivate);

  SourceSettings private(bool? private);

  SourceSettings regionTagging(SourceSettingsRegionTaggingEnum? regionTagging);

  SourceSettings downsampleInterval(String? downsampleInterval);

  SourceSettings egress(SourceSettingsEgressEnum? egress);

  SourceSettings reliability(SourceSettingsReliabilityEnum? reliability);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  SourceSettings call({
    bool enabled,
    String interval,
    Map<String, Object> params,
    Budget budget,
    String? retention,
    bool? embed,
    bool credentialsSet,
    DateTime? updatedAt,
    IngestPolicy? ingestPolicy,
    IngestPolicy? effectiveIngestPolicy,
    String? effectiveRetention,
    bool? statsEnabled,
    bool? statsIncludePrivate,
    bool? private,
    SourceSettingsRegionTaggingEnum? regionTagging,
    String? downsampleInterval,
    SourceSettingsEgressEnum? egress,
    SourceSettingsReliabilityEnum? reliability,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSourceSettings.copyWith(...)` or call `instanceOfSourceSettings.copyWith.fieldName(value)` for a single field.
class _$SourceSettingsCWProxyImpl implements _$SourceSettingsCWProxy {
  const _$SourceSettingsCWProxyImpl(this._value);

  final SourceSettings _value;

  @override
  SourceSettings enabled(bool enabled) => call(enabled: enabled);

  @override
  SourceSettings interval(String interval) => call(interval: interval);

  @override
  SourceSettings params(Map<String, Object> params) => call(params: params);

  @override
  SourceSettings budget(Budget budget) => call(budget: budget);

  @override
  SourceSettings retention(String? retention) => call(retention: retention);

  @override
  SourceSettings embed(bool? embed) => call(embed: embed);

  @override
  SourceSettings credentialsSet(bool credentialsSet) =>
      call(credentialsSet: credentialsSet);

  @override
  SourceSettings updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  @override
  SourceSettings ingestPolicy(IngestPolicy? ingestPolicy) =>
      call(ingestPolicy: ingestPolicy);

  @override
  SourceSettings effectiveIngestPolicy(IngestPolicy? effectiveIngestPolicy) =>
      call(effectiveIngestPolicy: effectiveIngestPolicy);

  @override
  SourceSettings effectiveRetention(String? effectiveRetention) =>
      call(effectiveRetention: effectiveRetention);

  @override
  SourceSettings statsEnabled(bool? statsEnabled) =>
      call(statsEnabled: statsEnabled);

  @override
  SourceSettings statsIncludePrivate(bool? statsIncludePrivate) =>
      call(statsIncludePrivate: statsIncludePrivate);

  @override
  SourceSettings private(bool? private) => call(private: private);

  @override
  SourceSettings regionTagging(
    SourceSettingsRegionTaggingEnum? regionTagging,
  ) => call(regionTagging: regionTagging);

  @override
  SourceSettings downsampleInterval(String? downsampleInterval) =>
      call(downsampleInterval: downsampleInterval);

  @override
  SourceSettings egress(SourceSettingsEgressEnum? egress) =>
      call(egress: egress);

  @override
  SourceSettings reliability(SourceSettingsReliabilityEnum? reliability) =>
      call(reliability: reliability);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SourceSettings call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? interval = const $CopyWithPlaceholder(),
    Object? params = const $CopyWithPlaceholder(),
    Object? budget = const $CopyWithPlaceholder(),
    Object? retention = const $CopyWithPlaceholder(),
    Object? embed = const $CopyWithPlaceholder(),
    Object? credentialsSet = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? ingestPolicy = const $CopyWithPlaceholder(),
    Object? effectiveIngestPolicy = const $CopyWithPlaceholder(),
    Object? effectiveRetention = const $CopyWithPlaceholder(),
    Object? statsEnabled = const $CopyWithPlaceholder(),
    Object? statsIncludePrivate = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? regionTagging = const $CopyWithPlaceholder(),
    Object? downsampleInterval = const $CopyWithPlaceholder(),
    Object? egress = const $CopyWithPlaceholder(),
    Object? reliability = const $CopyWithPlaceholder(),
  }) {
    return SourceSettings(
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      interval: interval == const $CopyWithPlaceholder() || interval == null
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as String,
      params: params == const $CopyWithPlaceholder() || params == null
          ? _value.params
          // ignore: cast_nullable_to_non_nullable
          : params as Map<String, Object>,
      budget: budget == const $CopyWithPlaceholder() || budget == null
          ? _value.budget
          // ignore: cast_nullable_to_non_nullable
          : budget as Budget,
      retention: retention == const $CopyWithPlaceholder()
          ? _value.retention
          // ignore: cast_nullable_to_non_nullable
          : retention as String?,
      embed: embed == const $CopyWithPlaceholder()
          ? _value.embed
          // ignore: cast_nullable_to_non_nullable
          : embed as bool?,
      credentialsSet:
          credentialsSet == const $CopyWithPlaceholder() ||
              credentialsSet == null
          ? _value.credentialsSet
          // ignore: cast_nullable_to_non_nullable
          : credentialsSet as bool,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      ingestPolicy: ingestPolicy == const $CopyWithPlaceholder()
          ? _value.ingestPolicy
          // ignore: cast_nullable_to_non_nullable
          : ingestPolicy as IngestPolicy?,
      effectiveIngestPolicy:
          effectiveIngestPolicy == const $CopyWithPlaceholder()
          ? _value.effectiveIngestPolicy
          // ignore: cast_nullable_to_non_nullable
          : effectiveIngestPolicy as IngestPolicy?,
      effectiveRetention: effectiveRetention == const $CopyWithPlaceholder()
          ? _value.effectiveRetention
          // ignore: cast_nullable_to_non_nullable
          : effectiveRetention as String?,
      statsEnabled: statsEnabled == const $CopyWithPlaceholder()
          ? _value.statsEnabled
          // ignore: cast_nullable_to_non_nullable
          : statsEnabled as bool?,
      statsIncludePrivate: statsIncludePrivate == const $CopyWithPlaceholder()
          ? _value.statsIncludePrivate
          // ignore: cast_nullable_to_non_nullable
          : statsIncludePrivate as bool?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
      regionTagging: regionTagging == const $CopyWithPlaceholder()
          ? _value.regionTagging
          // ignore: cast_nullable_to_non_nullable
          : regionTagging as SourceSettingsRegionTaggingEnum?,
      downsampleInterval: downsampleInterval == const $CopyWithPlaceholder()
          ? _value.downsampleInterval
          // ignore: cast_nullable_to_non_nullable
          : downsampleInterval as String?,
      egress: egress == const $CopyWithPlaceholder()
          ? _value.egress
          // ignore: cast_nullable_to_non_nullable
          : egress as SourceSettingsEgressEnum?,
      reliability: reliability == const $CopyWithPlaceholder()
          ? _value.reliability
          // ignore: cast_nullable_to_non_nullable
          : reliability as SourceSettingsReliabilityEnum?,
    );
  }
}

extension $SourceSettingsCopyWith on SourceSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSourceSettings.copyWith(...)` or `instanceOfSourceSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SourceSettingsCWProxy get copyWith => _$SourceSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SourceSettings _$SourceSettingsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SourceSettings', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'enabled',
      'interval',
      'params',
      'budget',
      'credentialsSet',
    ],
  );
  final val = SourceSettings(
    enabled: $checkedConvert('enabled', (v) => v as bool),
    interval: $checkedConvert('interval', (v) => v as String),
    params: $checkedConvert(
      'params',
      (v) =>
          (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
    ),
    budget: $checkedConvert(
      'budget',
      (v) => Budget.fromJson(v as Map<String, dynamic>),
    ),
    retention: $checkedConvert('retention', (v) => v as String?),
    embed: $checkedConvert('embed', (v) => v as bool?),
    credentialsSet: $checkedConvert('credentialsSet', (v) => v as bool),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    ingestPolicy: $checkedConvert(
      'ingestPolicy',
      (v) =>
          v == null ? null : IngestPolicy.fromJson(v as Map<String, dynamic>),
    ),
    effectiveIngestPolicy: $checkedConvert(
      'effectiveIngestPolicy',
      (v) =>
          v == null ? null : IngestPolicy.fromJson(v as Map<String, dynamic>),
    ),
    effectiveRetention: $checkedConvert(
      'effectiveRetention',
      (v) => v as String?,
    ),
    statsEnabled: $checkedConvert('statsEnabled', (v) => v as bool? ?? true),
    statsIncludePrivate: $checkedConvert(
      'statsIncludePrivate',
      (v) => v as bool? ?? false,
    ),
    private: $checkedConvert('private', (v) => v as bool?),
    regionTagging: $checkedConvert(
      'regionTagging',
      (v) => $enumDecodeNullable(_$SourceSettingsRegionTaggingEnumEnumMap, v),
    ),
    downsampleInterval: $checkedConvert(
      'downsampleInterval',
      (v) => v as String?,
    ),
    egress: $checkedConvert(
      'egress',
      (v) => $enumDecodeNullable(_$SourceSettingsEgressEnumEnumMap, v),
    ),
    reliability: $checkedConvert(
      'reliability',
      (v) => $enumDecodeNullable(_$SourceSettingsReliabilityEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$SourceSettingsToJson(
  SourceSettings instance,
) => <String, dynamic>{
  'enabled': instance.enabled,
  'interval': instance.interval,
  'params': instance.params,
  'budget': instance.budget.toJson(),
  'retention': ?instance.retention,
  'embed': ?instance.embed,
  'credentialsSet': instance.credentialsSet,
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'ingestPolicy': ?instance.ingestPolicy?.toJson(),
  'effectiveIngestPolicy': ?instance.effectiveIngestPolicy?.toJson(),
  'effectiveRetention': ?instance.effectiveRetention,
  'statsEnabled': ?instance.statsEnabled,
  'statsIncludePrivate': ?instance.statsIncludePrivate,
  'private': ?instance.private,
  'regionTagging':
      ?_$SourceSettingsRegionTaggingEnumEnumMap[instance.regionTagging],
  'downsampleInterval': ?instance.downsampleInterval,
  'egress': ?_$SourceSettingsEgressEnumEnumMap[instance.egress],
  'reliability': ?_$SourceSettingsReliabilityEnumEnumMap[instance.reliability],
};

const _$SourceSettingsRegionTaggingEnumEnumMap = {
  SourceSettingsRegionTaggingEnum.eager: 'eager',
  SourceSettingsRegionTaggingEnum.lazy: 'lazy',
  SourceSettingsRegionTaggingEnum.off: 'off',
};

const _$SourceSettingsEgressEnumEnumMap = {
  SourceSettingsEgressEnum.OPEN: 'OPEN',
  SourceSettingsEgressEnum.LOCAL_ONLY: 'LOCAL_ONLY',
};

const _$SourceSettingsReliabilityEnumEnumMap = {
  SourceSettingsReliabilityEnum.A: 'A',
  SourceSettingsReliabilityEnum.B: 'B',
  SourceSettingsReliabilityEnum.C: 'C',
  SourceSettingsReliabilityEnum.D: 'D',
  SourceSettingsReliabilityEnum.E: 'E',
  SourceSettingsReliabilityEnum.F: 'F',
};
