// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_cue_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialCueInputCWProxy {
  SocialCueInput placeId(String? placeId);

  SocialCueInput regionCode(String? regionCode);

  SocialCueInput bbox(String? bbox);

  SocialCueInput entityRef(String? entityRef);

  SocialCueInput situationId(String? situationId);

  SocialCueInput terms(List<String>? terms);

  SocialCueInput families(List<String>? families);

  SocialCueInput platforms(List<SocialPlatformId>? platforms);

  SocialCueInput window(TimeRange window);

  SocialCueInput maxCostUsd(num? maxCostUsd);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCueInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCueInput(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialCueInput call({
    String? placeId,
    String? regionCode,
    String? bbox,
    String? entityRef,
    String? situationId,
    List<String>? terms,
    List<String>? families,
    List<SocialPlatformId>? platforms,
    TimeRange window,
    num? maxCostUsd,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialCueInput.copyWith(...)` or call `instanceOfSocialCueInput.copyWith.fieldName(value)` for a single field.
class _$SocialCueInputCWProxyImpl implements _$SocialCueInputCWProxy {
  const _$SocialCueInputCWProxyImpl(this._value);

  final SocialCueInput _value;

  @override
  SocialCueInput placeId(String? placeId) => call(placeId: placeId);

  @override
  SocialCueInput regionCode(String? regionCode) => call(regionCode: regionCode);

  @override
  SocialCueInput bbox(String? bbox) => call(bbox: bbox);

  @override
  SocialCueInput entityRef(String? entityRef) => call(entityRef: entityRef);

  @override
  SocialCueInput situationId(String? situationId) =>
      call(situationId: situationId);

  @override
  SocialCueInput terms(List<String>? terms) => call(terms: terms);

  @override
  SocialCueInput families(List<String>? families) => call(families: families);

  @override
  SocialCueInput platforms(List<SocialPlatformId>? platforms) =>
      call(platforms: platforms);

  @override
  SocialCueInput window(TimeRange window) => call(window: window);

  @override
  SocialCueInput maxCostUsd(num? maxCostUsd) => call(maxCostUsd: maxCostUsd);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCueInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCueInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialCueInput call({
    Object? placeId = const $CopyWithPlaceholder(),
    Object? regionCode = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? entityRef = const $CopyWithPlaceholder(),
    Object? situationId = const $CopyWithPlaceholder(),
    Object? terms = const $CopyWithPlaceholder(),
    Object? families = const $CopyWithPlaceholder(),
    Object? platforms = const $CopyWithPlaceholder(),
    Object? window = const $CopyWithPlaceholder(),
    Object? maxCostUsd = const $CopyWithPlaceholder(),
  }) {
    return SocialCueInput(
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      regionCode: regionCode == const $CopyWithPlaceholder()
          ? _value.regionCode
          // ignore: cast_nullable_to_non_nullable
          : regionCode as String?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as String?,
      entityRef: entityRef == const $CopyWithPlaceholder()
          ? _value.entityRef
          // ignore: cast_nullable_to_non_nullable
          : entityRef as String?,
      situationId: situationId == const $CopyWithPlaceholder()
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String?,
      terms: terms == const $CopyWithPlaceholder()
          ? _value.terms
          // ignore: cast_nullable_to_non_nullable
          : terms as List<String>?,
      families: families == const $CopyWithPlaceholder()
          ? _value.families
          // ignore: cast_nullable_to_non_nullable
          : families as List<String>?,
      platforms: platforms == const $CopyWithPlaceholder()
          ? _value.platforms
          // ignore: cast_nullable_to_non_nullable
          : platforms as List<SocialPlatformId>?,
      window: window == const $CopyWithPlaceholder() || window == null
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as TimeRange,
      maxCostUsd: maxCostUsd == const $CopyWithPlaceholder()
          ? _value.maxCostUsd
          // ignore: cast_nullable_to_non_nullable
          : maxCostUsd as num?,
    );
  }
}

extension $SocialCueInputCopyWith on SocialCueInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialCueInput.copyWith(...)` or `instanceOfSocialCueInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialCueInputCWProxy get copyWith => _$SocialCueInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialCueInput _$SocialCueInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialCueInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['window']);
      final val = SocialCueInput(
        placeId: $checkedConvert('placeId', (v) => v as String?),
        regionCode: $checkedConvert('regionCode', (v) => v as String?),
        bbox: $checkedConvert('bbox', (v) => v as String?),
        entityRef: $checkedConvert('entityRef', (v) => v as String?),
        situationId: $checkedConvert('situationId', (v) => v as String?),
        terms: $checkedConvert(
          'terms',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        families: $checkedConvert(
          'families',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        platforms: $checkedConvert(
          'platforms',
          (v) => (v as List<dynamic>?)
              ?.map((e) => $enumDecode(_$SocialPlatformIdEnumMap, e))
              .toList(),
        ),
        window: $checkedConvert(
          'window',
          (v) => TimeRange.fromJson(v as Map<String, dynamic>),
        ),
        maxCostUsd: $checkedConvert('maxCostUsd', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$SocialCueInputToJson(SocialCueInput instance) =>
    <String, dynamic>{
      'placeId': ?instance.placeId,
      'regionCode': ?instance.regionCode,
      'bbox': ?instance.bbox,
      'entityRef': ?instance.entityRef,
      'situationId': ?instance.situationId,
      'terms': ?instance.terms,
      'families': ?instance.families,
      'platforms': ?instance.platforms
          ?.map((e) => _$SocialPlatformIdEnumMap[e]!)
          .toList(),
      'window': instance.window.toJson(),
      'maxCostUsd': ?instance.maxCostUsd,
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
