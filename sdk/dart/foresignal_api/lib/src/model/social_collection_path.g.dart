// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_collection_path.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialCollectionPathCWProxy {
  SocialCollectionPath path(SocialCollectionPathPathEnum path);

  SocialCollectionPath collectionType(SocialCollectionType collectionType);

  SocialCollectionPath enabled(bool enabled);

  SocialCollectionPath feedsBaselines(bool feedsBaselines);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCollectionPath(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCollectionPath(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialCollectionPath call({
    SocialCollectionPathPathEnum path,
    SocialCollectionType collectionType,
    bool enabled,
    bool feedsBaselines,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialCollectionPath.copyWith(...)` or call `instanceOfSocialCollectionPath.copyWith.fieldName(value)` for a single field.
class _$SocialCollectionPathCWProxyImpl
    implements _$SocialCollectionPathCWProxy {
  const _$SocialCollectionPathCWProxyImpl(this._value);

  final SocialCollectionPath _value;

  @override
  SocialCollectionPath path(SocialCollectionPathPathEnum path) =>
      call(path: path);

  @override
  SocialCollectionPath collectionType(SocialCollectionType collectionType) =>
      call(collectionType: collectionType);

  @override
  SocialCollectionPath enabled(bool enabled) => call(enabled: enabled);

  @override
  SocialCollectionPath feedsBaselines(bool feedsBaselines) =>
      call(feedsBaselines: feedsBaselines);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCollectionPath(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCollectionPath(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialCollectionPath call({
    Object? path = const $CopyWithPlaceholder(),
    Object? collectionType = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? feedsBaselines = const $CopyWithPlaceholder(),
  }) {
    return SocialCollectionPath(
      path: path == const $CopyWithPlaceholder() || path == null
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as SocialCollectionPathPathEnum,
      collectionType:
          collectionType == const $CopyWithPlaceholder() ||
              collectionType == null
          ? _value.collectionType
          // ignore: cast_nullable_to_non_nullable
          : collectionType as SocialCollectionType,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      feedsBaselines:
          feedsBaselines == const $CopyWithPlaceholder() ||
              feedsBaselines == null
          ? _value.feedsBaselines
          // ignore: cast_nullable_to_non_nullable
          : feedsBaselines as bool,
    );
  }
}

extension $SocialCollectionPathCopyWith on SocialCollectionPath {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialCollectionPath.copyWith(...)` or `instanceOfSocialCollectionPath.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialCollectionPathCWProxy get copyWith =>
      _$SocialCollectionPathCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialCollectionPath _$SocialCollectionPathFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialCollectionPath', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['path', 'collectionType', 'enabled', 'feedsBaselines'],
  );
  final val = SocialCollectionPath(
    path: $checkedConvert(
      'path',
      (v) => $enumDecode(_$SocialCollectionPathPathEnumEnumMap, v),
    ),
    collectionType: $checkedConvert(
      'collectionType',
      (v) => $enumDecode(_$SocialCollectionTypeEnumMap, v),
    ),
    enabled: $checkedConvert('enabled', (v) => v as bool),
    feedsBaselines: $checkedConvert('feedsBaselines', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$SocialCollectionPathToJson(
  SocialCollectionPath instance,
) => <String, dynamic>{
  'path': _$SocialCollectionPathPathEnumEnumMap[instance.path]!,
  'collectionType': _$SocialCollectionTypeEnumMap[instance.collectionType]!,
  'enabled': instance.enabled,
  'feedsBaselines': instance.feedsBaselines,
};

const _$SocialCollectionPathPathEnumEnumMap = {
  SocialCollectionPathPathEnum.stream: 'stream',
  SocialCollectionPathPathEnum.replay: 'replay',
  SocialCollectionPathPathEnum.search: 'search',
  SocialCollectionPathPathEnum.counts: 'counts',
  SocialCollectionPathPathEnum.trends: 'trends',
  SocialCollectionPathPathEnum.channelPoll: 'channel-poll',
  SocialCollectionPathPathEnum.accountPoll: 'account-poll',
  SocialCollectionPathPathEnum.hashtagSearch: 'hashtag-search',
  SocialCollectionPathPathEnum.cue: 'cue',
};

const _$SocialCollectionTypeEnumMap = {
  SocialCollectionType.complete: 'complete',
  SocialCollectionType.paged: 'paged',
  SocialCollectionType.sampled: 'sampled',
};
