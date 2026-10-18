// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'attention_topic_social.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AttentionTopicSocialCWProxy {
  AttentionTopicSocial terms(Map<String, List<String>>? terms);

  AttentionTopicSocial hashtags(List<String>? hashtags);

  AttentionTopicSocial accounts(
    List<AttentionTopicSocialAccountsInner>? accounts,
  );

  AttentionTopicSocial channels(List<AttentionTopicSocialChannel>? channels);

  AttentionTopicSocial exclude(List<String>? exclude);

  AttentionTopicSocial platforms(List<String>? platforms);

  AttentionTopicSocial placeIds(List<String>? placeIds);

  AttentionTopicSocial regionCodes(List<String>? regionCodes);

  AttentionTopicSocial entityRefs(List<String>? entityRefs);

  AttentionTopicSocial mode(AttentionTopicSocialModeEnum? mode);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttentionTopicSocial(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttentionTopicSocial(...).copyWith(id: 12, name: "My name")
  /// ```
  AttentionTopicSocial call({
    Map<String, List<String>>? terms,
    List<String>? hashtags,
    List<AttentionTopicSocialAccountsInner>? accounts,
    List<AttentionTopicSocialChannel>? channels,
    List<String>? exclude,
    List<String>? platforms,
    List<String>? placeIds,
    List<String>? regionCodes,
    List<String>? entityRefs,
    AttentionTopicSocialModeEnum? mode,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAttentionTopicSocial.copyWith(...)` or call `instanceOfAttentionTopicSocial.copyWith.fieldName(value)` for a single field.
class _$AttentionTopicSocialCWProxyImpl
    implements _$AttentionTopicSocialCWProxy {
  const _$AttentionTopicSocialCWProxyImpl(this._value);

  final AttentionTopicSocial _value;

  @override
  AttentionTopicSocial terms(Map<String, List<String>>? terms) =>
      call(terms: terms);

  @override
  AttentionTopicSocial hashtags(List<String>? hashtags) =>
      call(hashtags: hashtags);

  @override
  AttentionTopicSocial accounts(
    List<AttentionTopicSocialAccountsInner>? accounts,
  ) => call(accounts: accounts);

  @override
  AttentionTopicSocial channels(List<AttentionTopicSocialChannel>? channels) =>
      call(channels: channels);

  @override
  AttentionTopicSocial exclude(List<String>? exclude) => call(exclude: exclude);

  @override
  AttentionTopicSocial platforms(List<String>? platforms) =>
      call(platforms: platforms);

  @override
  AttentionTopicSocial placeIds(List<String>? placeIds) =>
      call(placeIds: placeIds);

  @override
  AttentionTopicSocial regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  AttentionTopicSocial entityRefs(List<String>? entityRefs) =>
      call(entityRefs: entityRefs);

  @override
  AttentionTopicSocial mode(AttentionTopicSocialModeEnum? mode) =>
      call(mode: mode);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttentionTopicSocial(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttentionTopicSocial(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AttentionTopicSocial call({
    Object? terms = const $CopyWithPlaceholder(),
    Object? hashtags = const $CopyWithPlaceholder(),
    Object? accounts = const $CopyWithPlaceholder(),
    Object? channels = const $CopyWithPlaceholder(),
    Object? exclude = const $CopyWithPlaceholder(),
    Object? platforms = const $CopyWithPlaceholder(),
    Object? placeIds = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? entityRefs = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
  }) {
    return AttentionTopicSocial(
      terms: terms == const $CopyWithPlaceholder()
          ? _value.terms
          // ignore: cast_nullable_to_non_nullable
          : terms as Map<String, List<String>>?,
      hashtags: hashtags == const $CopyWithPlaceholder()
          ? _value.hashtags
          // ignore: cast_nullable_to_non_nullable
          : hashtags as List<String>?,
      accounts: accounts == const $CopyWithPlaceholder()
          ? _value.accounts
          // ignore: cast_nullable_to_non_nullable
          : accounts as List<AttentionTopicSocialAccountsInner>?,
      channels: channels == const $CopyWithPlaceholder()
          ? _value.channels
          // ignore: cast_nullable_to_non_nullable
          : channels as List<AttentionTopicSocialChannel>?,
      exclude: exclude == const $CopyWithPlaceholder()
          ? _value.exclude
          // ignore: cast_nullable_to_non_nullable
          : exclude as List<String>?,
      platforms: platforms == const $CopyWithPlaceholder()
          ? _value.platforms
          // ignore: cast_nullable_to_non_nullable
          : platforms as List<String>?,
      placeIds: placeIds == const $CopyWithPlaceholder()
          ? _value.placeIds
          // ignore: cast_nullable_to_non_nullable
          : placeIds as List<String>?,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      entityRefs: entityRefs == const $CopyWithPlaceholder()
          ? _value.entityRefs
          // ignore: cast_nullable_to_non_nullable
          : entityRefs as List<String>?,
      mode: mode == const $CopyWithPlaceholder()
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as AttentionTopicSocialModeEnum?,
    );
  }
}

extension $AttentionTopicSocialCopyWith on AttentionTopicSocial {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAttentionTopicSocial.copyWith(...)` or `instanceOfAttentionTopicSocial.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AttentionTopicSocialCWProxy get copyWith =>
      _$AttentionTopicSocialCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AttentionTopicSocial _$AttentionTopicSocialFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AttentionTopicSocial', json, ($checkedConvert) {
  final val = AttentionTopicSocial(
    terms: $checkedConvert(
      'terms',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) =>
            MapEntry(k, (e as List<dynamic>).map((e) => e as String).toList()),
      ),
    ),
    hashtags: $checkedConvert(
      'hashtags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    accounts: $checkedConvert(
      'accounts',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => AttentionTopicSocialAccountsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    channels: $checkedConvert(
      'channels',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) =>
                AttentionTopicSocialChannel.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    exclude: $checkedConvert(
      'exclude',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    platforms: $checkedConvert(
      'platforms',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    placeIds: $checkedConvert(
      'placeIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    regionCodes: $checkedConvert(
      'regionCodes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    entityRefs: $checkedConvert(
      'entityRefs',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    mode: $checkedConvert(
      'mode',
      (v) => $enumDecodeNullable(_$AttentionTopicSocialModeEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$AttentionTopicSocialToJson(
  AttentionTopicSocial instance,
) => <String, dynamic>{
  'terms': ?instance.terms,
  'hashtags': ?instance.hashtags,
  'accounts': ?instance.accounts?.map((e) => e.toJson()).toList(),
  'channels': ?instance.channels?.map((e) => e.toJson()).toList(),
  'exclude': ?instance.exclude,
  'platforms': ?instance.platforms,
  'placeIds': ?instance.placeIds,
  'regionCodes': ?instance.regionCodes,
  'entityRefs': ?instance.entityRefs,
  'mode': ?_$AttentionTopicSocialModeEnumEnumMap[instance.mode],
};

const _$AttentionTopicSocialModeEnumEnumMap = {
  AttentionTopicSocialModeEnum.standing: 'standing',
  AttentionTopicSocialModeEnum.cueOnly: 'cue-only',
};
