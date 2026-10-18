// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialSettingsCWProxy {
  SocialSettings languages(List<String>? languages);

  SocialSettings minRegionBaseline(num? minRegionBaseline);

  SocialSettings velocity(SocialSettingsVelocity? velocity);

  SocialSettings llmClaimMinPosts(int? llmClaimMinPosts);

  SocialSettings llmClaimMinEvidenceGroups(int? llmClaimMinEvidenceGroups);

  SocialSettings keepMedia(bool? keepMedia);

  SocialSettings trackRecord(SocialSettingsTrackRecord? trackRecord);

  SocialSettings compare(SocialSettingsCompare? compare);

  SocialSettings attentionGap(SocialSettingsAttentionGap? attentionGap);

  SocialSettings cues(SocialSettingsCues? cues);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialSettings call({
    List<String>? languages,
    num? minRegionBaseline,
    SocialSettingsVelocity? velocity,
    int? llmClaimMinPosts,
    int? llmClaimMinEvidenceGroups,
    bool? keepMedia,
    SocialSettingsTrackRecord? trackRecord,
    SocialSettingsCompare? compare,
    SocialSettingsAttentionGap? attentionGap,
    SocialSettingsCues? cues,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialSettings.copyWith(...)` or call `instanceOfSocialSettings.copyWith.fieldName(value)` for a single field.
class _$SocialSettingsCWProxyImpl implements _$SocialSettingsCWProxy {
  const _$SocialSettingsCWProxyImpl(this._value);

  final SocialSettings _value;

  @override
  SocialSettings languages(List<String>? languages) =>
      call(languages: languages);

  @override
  SocialSettings minRegionBaseline(num? minRegionBaseline) =>
      call(minRegionBaseline: minRegionBaseline);

  @override
  SocialSettings velocity(SocialSettingsVelocity? velocity) =>
      call(velocity: velocity);

  @override
  SocialSettings llmClaimMinPosts(int? llmClaimMinPosts) =>
      call(llmClaimMinPosts: llmClaimMinPosts);

  @override
  SocialSettings llmClaimMinEvidenceGroups(int? llmClaimMinEvidenceGroups) =>
      call(llmClaimMinEvidenceGroups: llmClaimMinEvidenceGroups);

  @override
  SocialSettings keepMedia(bool? keepMedia) => call(keepMedia: keepMedia);

  @override
  SocialSettings trackRecord(SocialSettingsTrackRecord? trackRecord) =>
      call(trackRecord: trackRecord);

  @override
  SocialSettings compare(SocialSettingsCompare? compare) =>
      call(compare: compare);

  @override
  SocialSettings attentionGap(SocialSettingsAttentionGap? attentionGap) =>
      call(attentionGap: attentionGap);

  @override
  SocialSettings cues(SocialSettingsCues? cues) => call(cues: cues);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialSettings call({
    Object? languages = const $CopyWithPlaceholder(),
    Object? minRegionBaseline = const $CopyWithPlaceholder(),
    Object? velocity = const $CopyWithPlaceholder(),
    Object? llmClaimMinPosts = const $CopyWithPlaceholder(),
    Object? llmClaimMinEvidenceGroups = const $CopyWithPlaceholder(),
    Object? keepMedia = const $CopyWithPlaceholder(),
    Object? trackRecord = const $CopyWithPlaceholder(),
    Object? compare = const $CopyWithPlaceholder(),
    Object? attentionGap = const $CopyWithPlaceholder(),
    Object? cues = const $CopyWithPlaceholder(),
  }) {
    return SocialSettings(
      languages: languages == const $CopyWithPlaceholder()
          ? _value.languages
          // ignore: cast_nullable_to_non_nullable
          : languages as List<String>?,
      minRegionBaseline: minRegionBaseline == const $CopyWithPlaceholder()
          ? _value.minRegionBaseline
          // ignore: cast_nullable_to_non_nullable
          : minRegionBaseline as num?,
      velocity: velocity == const $CopyWithPlaceholder()
          ? _value.velocity
          // ignore: cast_nullable_to_non_nullable
          : velocity as SocialSettingsVelocity?,
      llmClaimMinPosts: llmClaimMinPosts == const $CopyWithPlaceholder()
          ? _value.llmClaimMinPosts
          // ignore: cast_nullable_to_non_nullable
          : llmClaimMinPosts as int?,
      llmClaimMinEvidenceGroups:
          llmClaimMinEvidenceGroups == const $CopyWithPlaceholder()
          ? _value.llmClaimMinEvidenceGroups
          // ignore: cast_nullable_to_non_nullable
          : llmClaimMinEvidenceGroups as int?,
      keepMedia: keepMedia == const $CopyWithPlaceholder()
          ? _value.keepMedia
          // ignore: cast_nullable_to_non_nullable
          : keepMedia as bool?,
      trackRecord: trackRecord == const $CopyWithPlaceholder()
          ? _value.trackRecord
          // ignore: cast_nullable_to_non_nullable
          : trackRecord as SocialSettingsTrackRecord?,
      compare: compare == const $CopyWithPlaceholder()
          ? _value.compare
          // ignore: cast_nullable_to_non_nullable
          : compare as SocialSettingsCompare?,
      attentionGap: attentionGap == const $CopyWithPlaceholder()
          ? _value.attentionGap
          // ignore: cast_nullable_to_non_nullable
          : attentionGap as SocialSettingsAttentionGap?,
      cues: cues == const $CopyWithPlaceholder()
          ? _value.cues
          // ignore: cast_nullable_to_non_nullable
          : cues as SocialSettingsCues?,
    );
  }
}

extension $SocialSettingsCopyWith on SocialSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialSettings.copyWith(...)` or `instanceOfSocialSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialSettingsCWProxy get copyWith => _$SocialSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialSettings _$SocialSettingsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialSettings', json, ($checkedConvert) {
      final val = SocialSettings(
        languages: $checkedConvert(
          'languages',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        minRegionBaseline: $checkedConvert(
          'minRegionBaseline',
          (v) => v as num? ?? 20,
        ),
        velocity: $checkedConvert(
          'velocity',
          (v) => v == null
              ? null
              : SocialSettingsVelocity.fromJson(v as Map<String, dynamic>),
        ),
        llmClaimMinPosts: $checkedConvert(
          'llmClaimMinPosts',
          (v) => (v as num?)?.toInt() ?? 50,
        ),
        llmClaimMinEvidenceGroups: $checkedConvert(
          'llmClaimMinEvidenceGroups',
          (v) => (v as num?)?.toInt() ?? 5,
        ),
        keepMedia: $checkedConvert('keepMedia', (v) => v as bool? ?? false),
        trackRecord: $checkedConvert(
          'trackRecord',
          (v) => v == null
              ? null
              : SocialSettingsTrackRecord.fromJson(v as Map<String, dynamic>),
        ),
        compare: $checkedConvert(
          'compare',
          (v) => v == null
              ? null
              : SocialSettingsCompare.fromJson(v as Map<String, dynamic>),
        ),
        attentionGap: $checkedConvert(
          'attentionGap',
          (v) => v == null
              ? null
              : SocialSettingsAttentionGap.fromJson(v as Map<String, dynamic>),
        ),
        cues: $checkedConvert(
          'cues',
          (v) => v == null
              ? null
              : SocialSettingsCues.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SocialSettingsToJson(SocialSettings instance) =>
    <String, dynamic>{
      'languages': ?instance.languages,
      'minRegionBaseline': ?instance.minRegionBaseline,
      'velocity': ?instance.velocity?.toJson(),
      'llmClaimMinPosts': ?instance.llmClaimMinPosts,
      'llmClaimMinEvidenceGroups': ?instance.llmClaimMinEvidenceGroups,
      'keepMedia': ?instance.keepMedia,
      'trackRecord': ?instance.trackRecord?.toJson(),
      'compare': ?instance.compare?.toJson(),
      'attentionGap': ?instance.attentionGap?.toJson(),
      'cues': ?instance.cues?.toJson(),
    };
