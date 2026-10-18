// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'source_settings_update.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SourceSettingsUpdateCWProxy {
  SourceSettingsUpdate enabled(bool? enabled);

  SourceSettingsUpdate interval(String? interval);

  SourceSettingsUpdate params(Map<String, Object>? params);

  SourceSettingsUpdate budget(Budget? budget);

  SourceSettingsUpdate retention(String? retention);

  SourceSettingsUpdate embed(bool? embed);

  SourceSettingsUpdate ingestPolicy(IngestPolicy? ingestPolicy);

  SourceSettingsUpdate statsEnabled(bool? statsEnabled);

  SourceSettingsUpdate statsIncludePrivate(bool? statsIncludePrivate);

  SourceSettingsUpdate private(bool? private);

  SourceSettingsUpdate regionTagging(
    SourceSettingsUpdateRegionTaggingEnum? regionTagging,
  );

  SourceSettingsUpdate downsampleInterval(String? downsampleInterval);

  SourceSettingsUpdate egress(SourceSettingsUpdateEgressEnum? egress);

  SourceSettingsUpdate reliability(
    SourceSettingsUpdateReliabilityEnum? reliability,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceSettingsUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceSettingsUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  SourceSettingsUpdate call({
    bool? enabled,
    String? interval,
    Map<String, Object>? params,
    Budget? budget,
    String? retention,
    bool? embed,
    IngestPolicy? ingestPolicy,
    bool? statsEnabled,
    bool? statsIncludePrivate,
    bool? private,
    SourceSettingsUpdateRegionTaggingEnum? regionTagging,
    String? downsampleInterval,
    SourceSettingsUpdateEgressEnum? egress,
    SourceSettingsUpdateReliabilityEnum? reliability,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSourceSettingsUpdate.copyWith(...)` or call `instanceOfSourceSettingsUpdate.copyWith.fieldName(value)` for a single field.
class _$SourceSettingsUpdateCWProxyImpl
    implements _$SourceSettingsUpdateCWProxy {
  const _$SourceSettingsUpdateCWProxyImpl(this._value);

  final SourceSettingsUpdate _value;

  @override
  SourceSettingsUpdate enabled(bool? enabled) => call(enabled: enabled);

  @override
  SourceSettingsUpdate interval(String? interval) => call(interval: interval);

  @override
  SourceSettingsUpdate params(Map<String, Object>? params) =>
      call(params: params);

  @override
  SourceSettingsUpdate budget(Budget? budget) => call(budget: budget);

  @override
  SourceSettingsUpdate retention(String? retention) =>
      call(retention: retention);

  @override
  SourceSettingsUpdate embed(bool? embed) => call(embed: embed);

  @override
  SourceSettingsUpdate ingestPolicy(IngestPolicy? ingestPolicy) =>
      call(ingestPolicy: ingestPolicy);

  @override
  SourceSettingsUpdate statsEnabled(bool? statsEnabled) =>
      call(statsEnabled: statsEnabled);

  @override
  SourceSettingsUpdate statsIncludePrivate(bool? statsIncludePrivate) =>
      call(statsIncludePrivate: statsIncludePrivate);

  @override
  SourceSettingsUpdate private(bool? private) => call(private: private);

  @override
  SourceSettingsUpdate regionTagging(
    SourceSettingsUpdateRegionTaggingEnum? regionTagging,
  ) => call(regionTagging: regionTagging);

  @override
  SourceSettingsUpdate downsampleInterval(String? downsampleInterval) =>
      call(downsampleInterval: downsampleInterval);

  @override
  SourceSettingsUpdate egress(SourceSettingsUpdateEgressEnum? egress) =>
      call(egress: egress);

  @override
  SourceSettingsUpdate reliability(
    SourceSettingsUpdateReliabilityEnum? reliability,
  ) => call(reliability: reliability);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceSettingsUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceSettingsUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SourceSettingsUpdate call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? interval = const $CopyWithPlaceholder(),
    Object? params = const $CopyWithPlaceholder(),
    Object? budget = const $CopyWithPlaceholder(),
    Object? retention = const $CopyWithPlaceholder(),
    Object? embed = const $CopyWithPlaceholder(),
    Object? ingestPolicy = const $CopyWithPlaceholder(),
    Object? statsEnabled = const $CopyWithPlaceholder(),
    Object? statsIncludePrivate = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? regionTagging = const $CopyWithPlaceholder(),
    Object? downsampleInterval = const $CopyWithPlaceholder(),
    Object? egress = const $CopyWithPlaceholder(),
    Object? reliability = const $CopyWithPlaceholder(),
  }) {
    return SourceSettingsUpdate(
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      interval: interval == const $CopyWithPlaceholder()
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as String?,
      params: params == const $CopyWithPlaceholder()
          ? _value.params
          // ignore: cast_nullable_to_non_nullable
          : params as Map<String, Object>?,
      budget: budget == const $CopyWithPlaceholder()
          ? _value.budget
          // ignore: cast_nullable_to_non_nullable
          : budget as Budget?,
      retention: retention == const $CopyWithPlaceholder()
          ? _value.retention
          // ignore: cast_nullable_to_non_nullable
          : retention as String?,
      embed: embed == const $CopyWithPlaceholder()
          ? _value.embed
          // ignore: cast_nullable_to_non_nullable
          : embed as bool?,
      ingestPolicy: ingestPolicy == const $CopyWithPlaceholder()
          ? _value.ingestPolicy
          // ignore: cast_nullable_to_non_nullable
          : ingestPolicy as IngestPolicy?,
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
          : regionTagging as SourceSettingsUpdateRegionTaggingEnum?,
      downsampleInterval: downsampleInterval == const $CopyWithPlaceholder()
          ? _value.downsampleInterval
          // ignore: cast_nullable_to_non_nullable
          : downsampleInterval as String?,
      egress: egress == const $CopyWithPlaceholder()
          ? _value.egress
          // ignore: cast_nullable_to_non_nullable
          : egress as SourceSettingsUpdateEgressEnum?,
      reliability: reliability == const $CopyWithPlaceholder()
          ? _value.reliability
          // ignore: cast_nullable_to_non_nullable
          : reliability as SourceSettingsUpdateReliabilityEnum?,
    );
  }
}

extension $SourceSettingsUpdateCopyWith on SourceSettingsUpdate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSourceSettingsUpdate.copyWith(...)` or `instanceOfSourceSettingsUpdate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SourceSettingsUpdateCWProxy get copyWith =>
      _$SourceSettingsUpdateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SourceSettingsUpdate _$SourceSettingsUpdateFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SourceSettingsUpdate', json, ($checkedConvert) {
  final val = SourceSettingsUpdate(
    enabled: $checkedConvert('enabled', (v) => v as bool?),
    interval: $checkedConvert('interval', (v) => v as String?),
    params: $checkedConvert(
      'params',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    budget: $checkedConvert(
      'budget',
      (v) => v == null ? null : Budget.fromJson(v as Map<String, dynamic>),
    ),
    retention: $checkedConvert('retention', (v) => v as String?),
    embed: $checkedConvert('embed', (v) => v as bool?),
    ingestPolicy: $checkedConvert(
      'ingestPolicy',
      (v) =>
          v == null ? null : IngestPolicy.fromJson(v as Map<String, dynamic>),
    ),
    statsEnabled: $checkedConvert('statsEnabled', (v) => v as bool?),
    statsIncludePrivate: $checkedConvert(
      'statsIncludePrivate',
      (v) => v as bool?,
    ),
    private: $checkedConvert('private', (v) => v as bool?),
    regionTagging: $checkedConvert(
      'regionTagging',
      (v) => $enumDecodeNullable(
        _$SourceSettingsUpdateRegionTaggingEnumEnumMap,
        v,
      ),
    ),
    downsampleInterval: $checkedConvert(
      'downsampleInterval',
      (v) => v as String?,
    ),
    egress: $checkedConvert(
      'egress',
      (v) => $enumDecodeNullable(_$SourceSettingsUpdateEgressEnumEnumMap, v),
    ),
    reliability: $checkedConvert(
      'reliability',
      (v) =>
          $enumDecodeNullable(_$SourceSettingsUpdateReliabilityEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$SourceSettingsUpdateToJson(
  SourceSettingsUpdate instance,
) => <String, dynamic>{
  'enabled': ?instance.enabled,
  'interval': ?instance.interval,
  'params': ?instance.params,
  'budget': ?instance.budget?.toJson(),
  'retention': ?instance.retention,
  'embed': ?instance.embed,
  'ingestPolicy': ?instance.ingestPolicy?.toJson(),
  'statsEnabled': ?instance.statsEnabled,
  'statsIncludePrivate': ?instance.statsIncludePrivate,
  'private': ?instance.private,
  'regionTagging':
      ?_$SourceSettingsUpdateRegionTaggingEnumEnumMap[instance.regionTagging],
  'downsampleInterval': ?instance.downsampleInterval,
  'egress': ?_$SourceSettingsUpdateEgressEnumEnumMap[instance.egress],
  'reliability':
      ?_$SourceSettingsUpdateReliabilityEnumEnumMap[instance.reliability],
};

const _$SourceSettingsUpdateRegionTaggingEnumEnumMap = {
  SourceSettingsUpdateRegionTaggingEnum.eager: 'eager',
  SourceSettingsUpdateRegionTaggingEnum.lazy: 'lazy',
  SourceSettingsUpdateRegionTaggingEnum.off: 'off',
};

const _$SourceSettingsUpdateEgressEnumEnumMap = {
  SourceSettingsUpdateEgressEnum.OPEN: 'OPEN',
  SourceSettingsUpdateEgressEnum.LOCAL_ONLY: 'LOCAL_ONLY',
};

const _$SourceSettingsUpdateReliabilityEnumEnumMap = {
  SourceSettingsUpdateReliabilityEnum.A: 'A',
  SourceSettingsUpdateReliabilityEnum.B: 'B',
  SourceSettingsUpdateReliabilityEnum.C: 'C',
  SourceSettingsUpdateReliabilityEnum.D: 'D',
  SourceSettingsUpdateReliabilityEnum.E: 'E',
  SourceSettingsUpdateReliabilityEnum.F: 'F',
};
