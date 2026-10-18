// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_versus_observed_lead_times_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialVersusObservedLeadTimesInnerCWProxy {
  SocialVersusObservedLeadTimesInner claimType(String claimType);

  SocialVersusObservedLeadTimesInner platform(String platform);

  SocialVersusObservedLeadTimesInner regionCode(String? regionCode);

  SocialVersusObservedLeadTimesInner overPhysical(LeadTimeSummary overPhysical);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVersusObservedLeadTimesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVersusObservedLeadTimesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialVersusObservedLeadTimesInner call({
    String claimType,
    String platform,
    String? regionCode,
    LeadTimeSummary overPhysical,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialVersusObservedLeadTimesInner.copyWith(...)` or call `instanceOfSocialVersusObservedLeadTimesInner.copyWith.fieldName(value)` for a single field.
class _$SocialVersusObservedLeadTimesInnerCWProxyImpl
    implements _$SocialVersusObservedLeadTimesInnerCWProxy {
  const _$SocialVersusObservedLeadTimesInnerCWProxyImpl(this._value);

  final SocialVersusObservedLeadTimesInner _value;

  @override
  SocialVersusObservedLeadTimesInner claimType(String claimType) =>
      call(claimType: claimType);

  @override
  SocialVersusObservedLeadTimesInner platform(String platform) =>
      call(platform: platform);

  @override
  SocialVersusObservedLeadTimesInner regionCode(String? regionCode) =>
      call(regionCode: regionCode);

  @override
  SocialVersusObservedLeadTimesInner overPhysical(
    LeadTimeSummary overPhysical,
  ) => call(overPhysical: overPhysical);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVersusObservedLeadTimesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVersusObservedLeadTimesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialVersusObservedLeadTimesInner call({
    Object? claimType = const $CopyWithPlaceholder(),
    Object? platform = const $CopyWithPlaceholder(),
    Object? regionCode = const $CopyWithPlaceholder(),
    Object? overPhysical = const $CopyWithPlaceholder(),
  }) {
    return SocialVersusObservedLeadTimesInner(
      claimType: claimType == const $CopyWithPlaceholder() || claimType == null
          ? _value.claimType
          // ignore: cast_nullable_to_non_nullable
          : claimType as String,
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as String,
      regionCode: regionCode == const $CopyWithPlaceholder()
          ? _value.regionCode
          // ignore: cast_nullable_to_non_nullable
          : regionCode as String?,
      overPhysical:
          overPhysical == const $CopyWithPlaceholder() || overPhysical == null
          ? _value.overPhysical
          // ignore: cast_nullable_to_non_nullable
          : overPhysical as LeadTimeSummary,
    );
  }
}

extension $SocialVersusObservedLeadTimesInnerCopyWith
    on SocialVersusObservedLeadTimesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialVersusObservedLeadTimesInner.copyWith(...)` or `instanceOfSocialVersusObservedLeadTimesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialVersusObservedLeadTimesInnerCWProxy get copyWith =>
      _$SocialVersusObservedLeadTimesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialVersusObservedLeadTimesInner _$SocialVersusObservedLeadTimesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialVersusObservedLeadTimesInner', json, (
  $checkedConvert,
) {
  $checkKeys(
    json,
    requiredKeys: const ['claimType', 'platform', 'overPhysical'],
  );
  final val = SocialVersusObservedLeadTimesInner(
    claimType: $checkedConvert('claimType', (v) => v as String),
    platform: $checkedConvert('platform', (v) => v as String),
    regionCode: $checkedConvert('regionCode', (v) => v as String?),
    overPhysical: $checkedConvert(
      'overPhysical',
      (v) => LeadTimeSummary.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$SocialVersusObservedLeadTimesInnerToJson(
  SocialVersusObservedLeadTimesInner instance,
) => <String, dynamic>{
  'claimType': instance.claimType,
  'platform': instance.platform,
  'regionCode': ?instance.regionCode,
  'overPhysical': instance.overPhysical.toJson(),
};
