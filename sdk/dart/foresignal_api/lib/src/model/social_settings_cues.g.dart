// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_settings_cues.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialSettingsCuesCWProxy {
  SocialSettingsCues enabled(bool? enabled);

  SocialSettingsCues triggers(SocialSettingsCuesTriggers? triggers);

  SocialSettingsCues maxPerDay(int? maxPerDay);

  SocialSettingsCues maxCostUsdPerCue(num? maxCostUsdPerCue);

  SocialSettingsCues platforms(List<SocialPlatformId>? platforms);

  SocialSettingsCues xaiXSearch(SocialSettingsCuesXaiXSearch? xaiXSearch);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsCues(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsCues(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialSettingsCues call({
    bool? enabled,
    SocialSettingsCuesTriggers? triggers,
    int? maxPerDay,
    num? maxCostUsdPerCue,
    List<SocialPlatformId>? platforms,
    SocialSettingsCuesXaiXSearch? xaiXSearch,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialSettingsCues.copyWith(...)` or call `instanceOfSocialSettingsCues.copyWith.fieldName(value)` for a single field.
class _$SocialSettingsCuesCWProxyImpl implements _$SocialSettingsCuesCWProxy {
  const _$SocialSettingsCuesCWProxyImpl(this._value);

  final SocialSettingsCues _value;

  @override
  SocialSettingsCues enabled(bool? enabled) => call(enabled: enabled);

  @override
  SocialSettingsCues triggers(SocialSettingsCuesTriggers? triggers) =>
      call(triggers: triggers);

  @override
  SocialSettingsCues maxPerDay(int? maxPerDay) => call(maxPerDay: maxPerDay);

  @override
  SocialSettingsCues maxCostUsdPerCue(num? maxCostUsdPerCue) =>
      call(maxCostUsdPerCue: maxCostUsdPerCue);

  @override
  SocialSettingsCues platforms(List<SocialPlatformId>? platforms) =>
      call(platforms: platforms);

  @override
  SocialSettingsCues xaiXSearch(SocialSettingsCuesXaiXSearch? xaiXSearch) =>
      call(xaiXSearch: xaiXSearch);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsCues(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsCues(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialSettingsCues call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? triggers = const $CopyWithPlaceholder(),
    Object? maxPerDay = const $CopyWithPlaceholder(),
    Object? maxCostUsdPerCue = const $CopyWithPlaceholder(),
    Object? platforms = const $CopyWithPlaceholder(),
    Object? xaiXSearch = const $CopyWithPlaceholder(),
  }) {
    return SocialSettingsCues(
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      triggers: triggers == const $CopyWithPlaceholder()
          ? _value.triggers
          // ignore: cast_nullable_to_non_nullable
          : triggers as SocialSettingsCuesTriggers?,
      maxPerDay: maxPerDay == const $CopyWithPlaceholder()
          ? _value.maxPerDay
          // ignore: cast_nullable_to_non_nullable
          : maxPerDay as int?,
      maxCostUsdPerCue: maxCostUsdPerCue == const $CopyWithPlaceholder()
          ? _value.maxCostUsdPerCue
          // ignore: cast_nullable_to_non_nullable
          : maxCostUsdPerCue as num?,
      platforms: platforms == const $CopyWithPlaceholder()
          ? _value.platforms
          // ignore: cast_nullable_to_non_nullable
          : platforms as List<SocialPlatformId>?,
      xaiXSearch: xaiXSearch == const $CopyWithPlaceholder()
          ? _value.xaiXSearch
          // ignore: cast_nullable_to_non_nullable
          : xaiXSearch as SocialSettingsCuesXaiXSearch?,
    );
  }
}

extension $SocialSettingsCuesCopyWith on SocialSettingsCues {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialSettingsCues.copyWith(...)` or `instanceOfSocialSettingsCues.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialSettingsCuesCWProxy get copyWith =>
      _$SocialSettingsCuesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialSettingsCues _$SocialSettingsCuesFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialSettingsCues', json, ($checkedConvert) {
  final val = SocialSettingsCues(
    enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
    triggers: $checkedConvert(
      'triggers',
      (v) => v == null
          ? null
          : SocialSettingsCuesTriggers.fromJson(v as Map<String, dynamic>),
    ),
    maxPerDay: $checkedConvert('maxPerDay', (v) => (v as num?)?.toInt() ?? 50),
    maxCostUsdPerCue: $checkedConvert(
      'maxCostUsdPerCue',
      (v) => v as num? ?? 1.0,
    ),
    platforms: $checkedConvert(
      'platforms',
      (v) => (v as List<dynamic>?)
          ?.map((e) => $enumDecode(_$SocialPlatformIdEnumMap, e))
          .toList(),
    ),
    xaiXSearch: $checkedConvert(
      'xaiXSearch',
      (v) => v == null
          ? null
          : SocialSettingsCuesXaiXSearch.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$SocialSettingsCuesToJson(SocialSettingsCues instance) =>
    <String, dynamic>{
      'enabled': ?instance.enabled,
      'triggers': ?instance.triggers?.toJson(),
      'maxPerDay': ?instance.maxPerDay,
      'maxCostUsdPerCue': ?instance.maxCostUsdPerCue,
      'platforms': ?instance.platforms
          ?.map((e) => _$SocialPlatformIdEnumMap[e]!)
          .toList(),
      'xaiXSearch': ?instance.xaiXSearch?.toJson(),
    };

const _$SocialPlatformIdEnumMap = {
  SocialPlatformId.blueskyJetstream: 'bluesky-jetstream',
  SocialPlatformId.blueskySearch: 'bluesky-search',
  SocialPlatformId.mastodon: 'mastodon',
  SocialPlatformId.reddit: 'reddit',
  SocialPlatformId.youtube: 'youtube',
  SocialPlatformId.x: 'x',
  SocialPlatformId.threads: 'threads',
  SocialPlatformId.instagramHashtags: 'instagram-hashtags',
};
