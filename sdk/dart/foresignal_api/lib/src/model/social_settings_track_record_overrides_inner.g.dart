// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_settings_track_record_overrides_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialSettingsTrackRecordOverridesInnerCWProxy {
  SocialSettingsTrackRecordOverridesInner platform(String platform);

  SocialSettingsTrackRecordOverridesInner account(String? account);

  SocialSettingsTrackRecordOverridesInner channel(String? channel);

  SocialSettingsTrackRecordOverridesInner reliability(
    SocialSettingsTrackRecordOverridesInnerReliabilityEnum reliability,
  );

  SocialSettingsTrackRecordOverridesInner note(String? note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsTrackRecordOverridesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsTrackRecordOverridesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialSettingsTrackRecordOverridesInner call({
    String platform,
    String? account,
    String? channel,
    SocialSettingsTrackRecordOverridesInnerReliabilityEnum reliability,
    String? note,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialSettingsTrackRecordOverridesInner.copyWith(...)` or call `instanceOfSocialSettingsTrackRecordOverridesInner.copyWith.fieldName(value)` for a single field.
class _$SocialSettingsTrackRecordOverridesInnerCWProxyImpl
    implements _$SocialSettingsTrackRecordOverridesInnerCWProxy {
  const _$SocialSettingsTrackRecordOverridesInnerCWProxyImpl(this._value);

  final SocialSettingsTrackRecordOverridesInner _value;

  @override
  SocialSettingsTrackRecordOverridesInner platform(String platform) =>
      call(platform: platform);

  @override
  SocialSettingsTrackRecordOverridesInner account(String? account) =>
      call(account: account);

  @override
  SocialSettingsTrackRecordOverridesInner channel(String? channel) =>
      call(channel: channel);

  @override
  SocialSettingsTrackRecordOverridesInner reliability(
    SocialSettingsTrackRecordOverridesInnerReliabilityEnum reliability,
  ) => call(reliability: reliability);

  @override
  SocialSettingsTrackRecordOverridesInner note(String? note) =>
      call(note: note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsTrackRecordOverridesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsTrackRecordOverridesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialSettingsTrackRecordOverridesInner call({
    Object? platform = const $CopyWithPlaceholder(),
    Object? account = const $CopyWithPlaceholder(),
    Object? channel = const $CopyWithPlaceholder(),
    Object? reliability = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
  }) {
    return SocialSettingsTrackRecordOverridesInner(
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as String,
      account: account == const $CopyWithPlaceholder()
          ? _value.account
          // ignore: cast_nullable_to_non_nullable
          : account as String?,
      channel: channel == const $CopyWithPlaceholder()
          ? _value.channel
          // ignore: cast_nullable_to_non_nullable
          : channel as String?,
      reliability:
          reliability == const $CopyWithPlaceholder() || reliability == null
          ? _value.reliability
          // ignore: cast_nullable_to_non_nullable
          : reliability
                as SocialSettingsTrackRecordOverridesInnerReliabilityEnum,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
    );
  }
}

extension $SocialSettingsTrackRecordOverridesInnerCopyWith
    on SocialSettingsTrackRecordOverridesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialSettingsTrackRecordOverridesInner.copyWith(...)` or `instanceOfSocialSettingsTrackRecordOverridesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialSettingsTrackRecordOverridesInnerCWProxy get copyWith =>
      _$SocialSettingsTrackRecordOverridesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialSettingsTrackRecordOverridesInner
_$SocialSettingsTrackRecordOverridesInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialSettingsTrackRecordOverridesInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['platform', 'reliability']);
      final val = SocialSettingsTrackRecordOverridesInner(
        platform: $checkedConvert('platform', (v) => v as String),
        account: $checkedConvert('account', (v) => v as String?),
        channel: $checkedConvert('channel', (v) => v as String?),
        reliability: $checkedConvert(
          'reliability',
          (v) => $enumDecode(
            _$SocialSettingsTrackRecordOverridesInnerReliabilityEnumEnumMap,
            v,
          ),
        ),
        note: $checkedConvert('note', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SocialSettingsTrackRecordOverridesInnerToJson(
  SocialSettingsTrackRecordOverridesInner instance,
) => <String, dynamic>{
  'platform': instance.platform,
  'account': ?instance.account,
  'channel': ?instance.channel,
  'reliability':
      _$SocialSettingsTrackRecordOverridesInnerReliabilityEnumEnumMap[instance
          .reliability]!,
  'note': ?instance.note,
};

const _$SocialSettingsTrackRecordOverridesInnerReliabilityEnumEnumMap = {
  SocialSettingsTrackRecordOverridesInnerReliabilityEnum.A: 'A',
  SocialSettingsTrackRecordOverridesInnerReliabilityEnum.B: 'B',
  SocialSettingsTrackRecordOverridesInnerReliabilityEnum.C: 'C',
  SocialSettingsTrackRecordOverridesInnerReliabilityEnum.D: 'D',
  SocialSettingsTrackRecordOverridesInnerReliabilityEnum.E: 'E',
  SocialSettingsTrackRecordOverridesInnerReliabilityEnum.F: 'F',
};
