// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_platform_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialPlatformSettingsCWProxy {
  SocialPlatformSettings enabled(bool enabled);

  SocialPlatformSettings mode(String? mode);

  SocialPlatformSettings channels(List<String>? channels);

  SocialPlatformSettings deletionSync(bool? deletionSync);

  SocialPlatformSettings postRetention(String? postRetention);

  SocialPlatformSettings budget(Budget? budget);

  SocialPlatformSettings monthlyCapUsd(num? monthlyCapUsd);

  SocialPlatformSettings prices(List<SocialPlatformPrice>? prices);

  SocialPlatformSettings pricesAsOf(DateTime? pricesAsOf);

  SocialPlatformSettings options(SocialPlatformSettingsOptions? options);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialPlatformSettings call({
    bool enabled,
    String? mode,
    List<String>? channels,
    bool? deletionSync,
    String? postRetention,
    Budget? budget,
    num? monthlyCapUsd,
    List<SocialPlatformPrice>? prices,
    DateTime? pricesAsOf,
    SocialPlatformSettingsOptions? options,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialPlatformSettings.copyWith(...)` or call `instanceOfSocialPlatformSettings.copyWith.fieldName(value)` for a single field.
class _$SocialPlatformSettingsCWProxyImpl
    implements _$SocialPlatformSettingsCWProxy {
  const _$SocialPlatformSettingsCWProxyImpl(this._value);

  final SocialPlatformSettings _value;

  @override
  SocialPlatformSettings enabled(bool enabled) => call(enabled: enabled);

  @override
  SocialPlatformSettings mode(String? mode) => call(mode: mode);

  @override
  SocialPlatformSettings channels(List<String>? channels) =>
      call(channels: channels);

  @override
  SocialPlatformSettings deletionSync(bool? deletionSync) =>
      call(deletionSync: deletionSync);

  @override
  SocialPlatformSettings postRetention(String? postRetention) =>
      call(postRetention: postRetention);

  @override
  SocialPlatformSettings budget(Budget? budget) => call(budget: budget);

  @override
  SocialPlatformSettings monthlyCapUsd(num? monthlyCapUsd) =>
      call(monthlyCapUsd: monthlyCapUsd);

  @override
  SocialPlatformSettings prices(List<SocialPlatformPrice>? prices) =>
      call(prices: prices);

  @override
  SocialPlatformSettings pricesAsOf(DateTime? pricesAsOf) =>
      call(pricesAsOf: pricesAsOf);

  @override
  SocialPlatformSettings options(SocialPlatformSettingsOptions? options) =>
      call(options: options);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialPlatformSettings call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? channels = const $CopyWithPlaceholder(),
    Object? deletionSync = const $CopyWithPlaceholder(),
    Object? postRetention = const $CopyWithPlaceholder(),
    Object? budget = const $CopyWithPlaceholder(),
    Object? monthlyCapUsd = const $CopyWithPlaceholder(),
    Object? prices = const $CopyWithPlaceholder(),
    Object? pricesAsOf = const $CopyWithPlaceholder(),
    Object? options = const $CopyWithPlaceholder(),
  }) {
    return SocialPlatformSettings(
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      mode: mode == const $CopyWithPlaceholder()
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as String?,
      channels: channels == const $CopyWithPlaceholder()
          ? _value.channels
          // ignore: cast_nullable_to_non_nullable
          : channels as List<String>?,
      deletionSync: deletionSync == const $CopyWithPlaceholder()
          ? _value.deletionSync
          // ignore: cast_nullable_to_non_nullable
          : deletionSync as bool?,
      postRetention: postRetention == const $CopyWithPlaceholder()
          ? _value.postRetention
          // ignore: cast_nullable_to_non_nullable
          : postRetention as String?,
      budget: budget == const $CopyWithPlaceholder()
          ? _value.budget
          // ignore: cast_nullable_to_non_nullable
          : budget as Budget?,
      monthlyCapUsd: monthlyCapUsd == const $CopyWithPlaceholder()
          ? _value.monthlyCapUsd
          // ignore: cast_nullable_to_non_nullable
          : monthlyCapUsd as num?,
      prices: prices == const $CopyWithPlaceholder()
          ? _value.prices
          // ignore: cast_nullable_to_non_nullable
          : prices as List<SocialPlatformPrice>?,
      pricesAsOf: pricesAsOf == const $CopyWithPlaceholder()
          ? _value.pricesAsOf
          // ignore: cast_nullable_to_non_nullable
          : pricesAsOf as DateTime?,
      options: options == const $CopyWithPlaceholder()
          ? _value.options
          // ignore: cast_nullable_to_non_nullable
          : options as SocialPlatformSettingsOptions?,
    );
  }
}

extension $SocialPlatformSettingsCopyWith on SocialPlatformSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialPlatformSettings.copyWith(...)` or `instanceOfSocialPlatformSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialPlatformSettingsCWProxy get copyWith =>
      _$SocialPlatformSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialPlatformSettings _$SocialPlatformSettingsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialPlatformSettings', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['enabled']);
  final val = SocialPlatformSettings(
    enabled: $checkedConvert('enabled', (v) => v as bool),
    mode: $checkedConvert('mode', (v) => v as String?),
    channels: $checkedConvert(
      'channels',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    deletionSync: $checkedConvert('deletionSync', (v) => v as bool?),
    postRetention: $checkedConvert('postRetention', (v) => v as String?),
    budget: $checkedConvert(
      'budget',
      (v) => v == null ? null : Budget.fromJson(v as Map<String, dynamic>),
    ),
    monthlyCapUsd: $checkedConvert('monthlyCapUsd', (v) => v as num?),
    prices: $checkedConvert(
      'prices',
      (v) => (v as List<dynamic>?)
          ?.map((e) => SocialPlatformPrice.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    pricesAsOf: $checkedConvert(
      'pricesAsOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    options: $checkedConvert(
      'options',
      (v) => v == null
          ? null
          : SocialPlatformSettingsOptions.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$SocialPlatformSettingsToJson(
  SocialPlatformSettings instance,
) => <String, dynamic>{
  'enabled': instance.enabled,
  'mode': ?instance.mode,
  'channels': ?instance.channels,
  'deletionSync': ?instance.deletionSync,
  'postRetention': ?instance.postRetention,
  'budget': ?instance.budget?.toJson(),
  'monthlyCapUsd': ?instance.monthlyCapUsd,
  'prices': ?instance.prices?.map((e) => e.toJson()).toList(),
  'pricesAsOf': ?instance.pricesAsOf?.toIso8601String(),
  'options': ?instance.options?.toJson(),
};
