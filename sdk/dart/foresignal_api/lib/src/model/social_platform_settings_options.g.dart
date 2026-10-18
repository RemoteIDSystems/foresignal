// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_platform_settings_options.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialPlatformSettingsOptionsCWProxy {
  SocialPlatformSettingsOptions instances(List<String>? instances);

  SocialPlatformSettingsOptions replay(bool? replay);

  SocialPlatformSettingsOptions standingCounts(bool? standingCounts);

  SocialPlatformSettingsOptions fullArchive(bool? fullArchive);

  SocialPlatformSettingsOptions comments(bool? comments);

  SocialPlatformSettingsOptions maxResults(int? maxResults);

  SocialPlatformSettingsOptions fieldsParam(
    SocialPlatformSettingsOptionsFieldsParamEnum? fieldsParam,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformSettingsOptions(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformSettingsOptions(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialPlatformSettingsOptions call({
    List<String>? instances,
    bool? replay,
    bool? standingCounts,
    bool? fullArchive,
    bool? comments,
    int? maxResults,
    SocialPlatformSettingsOptionsFieldsParamEnum? fieldsParam,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialPlatformSettingsOptions.copyWith(...)` or call `instanceOfSocialPlatformSettingsOptions.copyWith.fieldName(value)` for a single field.
class _$SocialPlatformSettingsOptionsCWProxyImpl
    implements _$SocialPlatformSettingsOptionsCWProxy {
  const _$SocialPlatformSettingsOptionsCWProxyImpl(this._value);

  final SocialPlatformSettingsOptions _value;

  @override
  SocialPlatformSettingsOptions instances(List<String>? instances) =>
      call(instances: instances);

  @override
  SocialPlatformSettingsOptions replay(bool? replay) => call(replay: replay);

  @override
  SocialPlatformSettingsOptions standingCounts(bool? standingCounts) =>
      call(standingCounts: standingCounts);

  @override
  SocialPlatformSettingsOptions fullArchive(bool? fullArchive) =>
      call(fullArchive: fullArchive);

  @override
  SocialPlatformSettingsOptions comments(bool? comments) =>
      call(comments: comments);

  @override
  SocialPlatformSettingsOptions maxResults(int? maxResults) =>
      call(maxResults: maxResults);

  @override
  SocialPlatformSettingsOptions fieldsParam(
    SocialPlatformSettingsOptionsFieldsParamEnum? fieldsParam,
  ) => call(fieldsParam: fieldsParam);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformSettingsOptions(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformSettingsOptions(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialPlatformSettingsOptions call({
    Object? instances = const $CopyWithPlaceholder(),
    Object? replay = const $CopyWithPlaceholder(),
    Object? standingCounts = const $CopyWithPlaceholder(),
    Object? fullArchive = const $CopyWithPlaceholder(),
    Object? comments = const $CopyWithPlaceholder(),
    Object? maxResults = const $CopyWithPlaceholder(),
    Object? fieldsParam = const $CopyWithPlaceholder(),
  }) {
    return SocialPlatformSettingsOptions(
      instances: instances == const $CopyWithPlaceholder()
          ? _value.instances
          // ignore: cast_nullable_to_non_nullable
          : instances as List<String>?,
      replay: replay == const $CopyWithPlaceholder()
          ? _value.replay
          // ignore: cast_nullable_to_non_nullable
          : replay as bool?,
      standingCounts: standingCounts == const $CopyWithPlaceholder()
          ? _value.standingCounts
          // ignore: cast_nullable_to_non_nullable
          : standingCounts as bool?,
      fullArchive: fullArchive == const $CopyWithPlaceholder()
          ? _value.fullArchive
          // ignore: cast_nullable_to_non_nullable
          : fullArchive as bool?,
      comments: comments == const $CopyWithPlaceholder()
          ? _value.comments
          // ignore: cast_nullable_to_non_nullable
          : comments as bool?,
      maxResults: maxResults == const $CopyWithPlaceholder()
          ? _value.maxResults
          // ignore: cast_nullable_to_non_nullable
          : maxResults as int?,
      fieldsParam: fieldsParam == const $CopyWithPlaceholder()
          ? _value.fieldsParam
          // ignore: cast_nullable_to_non_nullable
          : fieldsParam as SocialPlatformSettingsOptionsFieldsParamEnum?,
    );
  }
}

extension $SocialPlatformSettingsOptionsCopyWith
    on SocialPlatformSettingsOptions {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialPlatformSettingsOptions.copyWith(...)` or `instanceOfSocialPlatformSettingsOptions.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialPlatformSettingsOptionsCWProxy get copyWith =>
      _$SocialPlatformSettingsOptionsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialPlatformSettingsOptions _$SocialPlatformSettingsOptionsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialPlatformSettingsOptions', json, ($checkedConvert) {
  final val = SocialPlatformSettingsOptions(
    instances: $checkedConvert(
      'instances',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    replay: $checkedConvert('replay', (v) => v as bool?),
    standingCounts: $checkedConvert('standingCounts', (v) => v as bool?),
    fullArchive: $checkedConvert('fullArchive', (v) => v as bool?),
    comments: $checkedConvert('comments', (v) => v as bool?),
    maxResults: $checkedConvert(
      'maxResults',
      (v) => (v as num?)?.toInt() ?? 10,
    ),
    fieldsParam: $checkedConvert(
      'fieldsParam',
      (v) => $enumDecodeNullable(
        _$SocialPlatformSettingsOptionsFieldsParamEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$SocialPlatformSettingsOptionsToJson(
  SocialPlatformSettingsOptions instance,
) => <String, dynamic>{
  'instances': ?instance.instances,
  'replay': ?instance.replay,
  'standingCounts': ?instance.standingCounts,
  'fullArchive': ?instance.fullArchive,
  'comments': ?instance.comments,
  'maxResults': ?instance.maxResults,
  'fieldsParam':
      ?_$SocialPlatformSettingsOptionsFieldsParamEnumEnumMap[instance
          .fieldsParam],
};

const _$SocialPlatformSettingsOptionsFieldsParamEnumEnumMap = {
  SocialPlatformSettingsOptionsFieldsParamEnum.post: 'post',
  SocialPlatformSettingsOptionsFieldsParamEnum.tweet: 'tweet',
};
