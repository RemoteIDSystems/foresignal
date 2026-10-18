// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_platform_state_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialPlatformStateEventCWProxy {
  SocialPlatformStateEvent event(SocialPlatformStateEventEventEnum event);

  SocialPlatformStateEvent platformId(SocialPlatformId platformId);

  SocialPlatformStateEvent state(String state);

  SocialPlatformStateEvent previousState(String? previousState);

  SocialPlatformStateEvent reason(String? reason);

  SocialPlatformStateEvent spend(SocialSpend? spend);

  SocialPlatformStateEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformStateEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformStateEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialPlatformStateEvent call({
    SocialPlatformStateEventEventEnum event,
    SocialPlatformId platformId,
    String state,
    String? previousState,
    String? reason,
    SocialSpend? spend,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialPlatformStateEvent.copyWith(...)` or call `instanceOfSocialPlatformStateEvent.copyWith.fieldName(value)` for a single field.
class _$SocialPlatformStateEventCWProxyImpl
    implements _$SocialPlatformStateEventCWProxy {
  const _$SocialPlatformStateEventCWProxyImpl(this._value);

  final SocialPlatformStateEvent _value;

  @override
  SocialPlatformStateEvent event(SocialPlatformStateEventEventEnum event) =>
      call(event: event);

  @override
  SocialPlatformStateEvent platformId(SocialPlatformId platformId) =>
      call(platformId: platformId);

  @override
  SocialPlatformStateEvent state(String state) => call(state: state);

  @override
  SocialPlatformStateEvent previousState(String? previousState) =>
      call(previousState: previousState);

  @override
  SocialPlatformStateEvent reason(String? reason) => call(reason: reason);

  @override
  SocialPlatformStateEvent spend(SocialSpend? spend) => call(spend: spend);

  @override
  SocialPlatformStateEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformStateEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformStateEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialPlatformStateEvent call({
    Object? event = const $CopyWithPlaceholder(),
    Object? platformId = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? previousState = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? spend = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return SocialPlatformStateEvent(
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as SocialPlatformStateEventEventEnum,
      platformId:
          platformId == const $CopyWithPlaceholder() || platformId == null
          ? _value.platformId
          // ignore: cast_nullable_to_non_nullable
          : platformId as SocialPlatformId,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as String,
      previousState: previousState == const $CopyWithPlaceholder()
          ? _value.previousState
          // ignore: cast_nullable_to_non_nullable
          : previousState as String?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
      spend: spend == const $CopyWithPlaceholder()
          ? _value.spend
          // ignore: cast_nullable_to_non_nullable
          : spend as SocialSpend?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $SocialPlatformStateEventCopyWith on SocialPlatformStateEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialPlatformStateEvent.copyWith(...)` or `instanceOfSocialPlatformStateEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialPlatformStateEventCWProxy get copyWith =>
      _$SocialPlatformStateEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialPlatformStateEvent _$SocialPlatformStateEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialPlatformStateEvent', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['event', 'platformId', 'state', 'at']);
  final val = SocialPlatformStateEvent(
    event: $checkedConvert(
      'event',
      (v) => $enumDecode(_$SocialPlatformStateEventEventEnumEnumMap, v),
    ),
    platformId: $checkedConvert(
      'platformId',
      (v) => $enumDecode(_$SocialPlatformIdEnumMap, v),
    ),
    state: $checkedConvert('state', (v) => v as String),
    previousState: $checkedConvert('previousState', (v) => v as String?),
    reason: $checkedConvert('reason', (v) => v as String?),
    spend: $checkedConvert(
      'spend',
      (v) => v == null ? null : SocialSpend.fromJson(v as Map<String, dynamic>),
    ),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$SocialPlatformStateEventToJson(
  SocialPlatformStateEvent instance,
) => <String, dynamic>{
  'event': _$SocialPlatformStateEventEventEnumEnumMap[instance.event]!,
  'platformId': _$SocialPlatformIdEnumMap[instance.platformId]!,
  'state': instance.state,
  'previousState': ?instance.previousState,
  'reason': ?instance.reason,
  'spend': ?instance.spend?.toJson(),
  'at': instance.at.toIso8601String(),
};

const _$SocialPlatformStateEventEventEnumEnumMap = {
  SocialPlatformStateEventEventEnum.socialPeriodPlatformPeriodState:
      'social.platform.state',
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
