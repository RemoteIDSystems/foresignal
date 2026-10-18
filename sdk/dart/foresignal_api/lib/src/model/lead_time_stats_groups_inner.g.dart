// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'lead_time_stats_groups_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LeadTimeStatsGroupsInnerCWProxy {
  LeadTimeStatsGroupsInner claimType(String claimType);

  LeadTimeStatsGroupsInner platform(String platform);

  LeadTimeStatsGroupsInner regionCode(String? regionCode);

  LeadTimeStatsGroupsInner foundBy(LeadTimeStatsGroupsInnerFoundByEnum foundBy);

  LeadTimeStatsGroupsInner comparisons(int comparisons);

  LeadTimeStatsGroupsInner outOfSample(int outOfSample);

  LeadTimeStatsGroupsInner overPhysical(LeadTimeSummary overPhysical);

  LeadTimeStatsGroupsInner overNews(LeadTimeSummary overNews);

  LeadTimeStatsGroupsInner overOfficial(LeadTimeSummary overOfficial);

  LeadTimeStatsGroupsInner outOfSampleOverPhysical(
    LeadTimeSummary? outOfSampleOverPhysical,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LeadTimeStatsGroupsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LeadTimeStatsGroupsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  LeadTimeStatsGroupsInner call({
    String claimType,
    String platform,
    String? regionCode,
    LeadTimeStatsGroupsInnerFoundByEnum foundBy,
    int comparisons,
    int outOfSample,
    LeadTimeSummary overPhysical,
    LeadTimeSummary overNews,
    LeadTimeSummary overOfficial,
    LeadTimeSummary? outOfSampleOverPhysical,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLeadTimeStatsGroupsInner.copyWith(...)` or call `instanceOfLeadTimeStatsGroupsInner.copyWith.fieldName(value)` for a single field.
class _$LeadTimeStatsGroupsInnerCWProxyImpl
    implements _$LeadTimeStatsGroupsInnerCWProxy {
  const _$LeadTimeStatsGroupsInnerCWProxyImpl(this._value);

  final LeadTimeStatsGroupsInner _value;

  @override
  LeadTimeStatsGroupsInner claimType(String claimType) =>
      call(claimType: claimType);

  @override
  LeadTimeStatsGroupsInner platform(String platform) =>
      call(platform: platform);

  @override
  LeadTimeStatsGroupsInner regionCode(String? regionCode) =>
      call(regionCode: regionCode);

  @override
  LeadTimeStatsGroupsInner foundBy(
    LeadTimeStatsGroupsInnerFoundByEnum foundBy,
  ) => call(foundBy: foundBy);

  @override
  LeadTimeStatsGroupsInner comparisons(int comparisons) =>
      call(comparisons: comparisons);

  @override
  LeadTimeStatsGroupsInner outOfSample(int outOfSample) =>
      call(outOfSample: outOfSample);

  @override
  LeadTimeStatsGroupsInner overPhysical(LeadTimeSummary overPhysical) =>
      call(overPhysical: overPhysical);

  @override
  LeadTimeStatsGroupsInner overNews(LeadTimeSummary overNews) =>
      call(overNews: overNews);

  @override
  LeadTimeStatsGroupsInner overOfficial(LeadTimeSummary overOfficial) =>
      call(overOfficial: overOfficial);

  @override
  LeadTimeStatsGroupsInner outOfSampleOverPhysical(
    LeadTimeSummary? outOfSampleOverPhysical,
  ) => call(outOfSampleOverPhysical: outOfSampleOverPhysical);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LeadTimeStatsGroupsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LeadTimeStatsGroupsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LeadTimeStatsGroupsInner call({
    Object? claimType = const $CopyWithPlaceholder(),
    Object? platform = const $CopyWithPlaceholder(),
    Object? regionCode = const $CopyWithPlaceholder(),
    Object? foundBy = const $CopyWithPlaceholder(),
    Object? comparisons = const $CopyWithPlaceholder(),
    Object? outOfSample = const $CopyWithPlaceholder(),
    Object? overPhysical = const $CopyWithPlaceholder(),
    Object? overNews = const $CopyWithPlaceholder(),
    Object? overOfficial = const $CopyWithPlaceholder(),
    Object? outOfSampleOverPhysical = const $CopyWithPlaceholder(),
  }) {
    return LeadTimeStatsGroupsInner(
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
      foundBy: foundBy == const $CopyWithPlaceholder() || foundBy == null
          ? _value.foundBy
          // ignore: cast_nullable_to_non_nullable
          : foundBy as LeadTimeStatsGroupsInnerFoundByEnum,
      comparisons:
          comparisons == const $CopyWithPlaceholder() || comparisons == null
          ? _value.comparisons
          // ignore: cast_nullable_to_non_nullable
          : comparisons as int,
      outOfSample:
          outOfSample == const $CopyWithPlaceholder() || outOfSample == null
          ? _value.outOfSample
          // ignore: cast_nullable_to_non_nullable
          : outOfSample as int,
      overPhysical:
          overPhysical == const $CopyWithPlaceholder() || overPhysical == null
          ? _value.overPhysical
          // ignore: cast_nullable_to_non_nullable
          : overPhysical as LeadTimeSummary,
      overNews: overNews == const $CopyWithPlaceholder() || overNews == null
          ? _value.overNews
          // ignore: cast_nullable_to_non_nullable
          : overNews as LeadTimeSummary,
      overOfficial:
          overOfficial == const $CopyWithPlaceholder() || overOfficial == null
          ? _value.overOfficial
          // ignore: cast_nullable_to_non_nullable
          : overOfficial as LeadTimeSummary,
      outOfSampleOverPhysical:
          outOfSampleOverPhysical == const $CopyWithPlaceholder()
          ? _value.outOfSampleOverPhysical
          // ignore: cast_nullable_to_non_nullable
          : outOfSampleOverPhysical as LeadTimeSummary?,
    );
  }
}

extension $LeadTimeStatsGroupsInnerCopyWith on LeadTimeStatsGroupsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLeadTimeStatsGroupsInner.copyWith(...)` or `instanceOfLeadTimeStatsGroupsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LeadTimeStatsGroupsInnerCWProxy get copyWith =>
      _$LeadTimeStatsGroupsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LeadTimeStatsGroupsInner _$LeadTimeStatsGroupsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('LeadTimeStatsGroupsInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'claimType',
      'platform',
      'foundBy',
      'comparisons',
      'outOfSample',
      'overPhysical',
      'overNews',
      'overOfficial',
    ],
  );
  final val = LeadTimeStatsGroupsInner(
    claimType: $checkedConvert('claimType', (v) => v as String),
    platform: $checkedConvert('platform', (v) => v as String),
    regionCode: $checkedConvert('regionCode', (v) => v as String?),
    foundBy: $checkedConvert(
      'foundBy',
      (v) => $enumDecode(_$LeadTimeStatsGroupsInnerFoundByEnumEnumMap, v),
    ),
    comparisons: $checkedConvert('comparisons', (v) => (v as num).toInt()),
    outOfSample: $checkedConvert('outOfSample', (v) => (v as num).toInt()),
    overPhysical: $checkedConvert(
      'overPhysical',
      (v) => LeadTimeSummary.fromJson(v as Map<String, dynamic>),
    ),
    overNews: $checkedConvert(
      'overNews',
      (v) => LeadTimeSummary.fromJson(v as Map<String, dynamic>),
    ),
    overOfficial: $checkedConvert(
      'overOfficial',
      (v) => LeadTimeSummary.fromJson(v as Map<String, dynamic>),
    ),
    outOfSampleOverPhysical: $checkedConvert(
      'outOfSampleOverPhysical',
      (v) => v == null
          ? null
          : LeadTimeSummary.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$LeadTimeStatsGroupsInnerToJson(
  LeadTimeStatsGroupsInner instance,
) => <String, dynamic>{
  'claimType': instance.claimType,
  'platform': instance.platform,
  'regionCode': ?instance.regionCode,
  'foundBy': _$LeadTimeStatsGroupsInnerFoundByEnumEnumMap[instance.foundBy]!,
  'comparisons': instance.comparisons,
  'outOfSample': instance.outOfSample,
  'overPhysical': instance.overPhysical.toJson(),
  'overNews': instance.overNews.toJson(),
  'overOfficial': instance.overOfficial.toJson(),
  'outOfSampleOverPhysical': ?instance.outOfSampleOverPhysical?.toJson(),
};

const _$LeadTimeStatsGroupsInnerFoundByEnumEnumMap = {
  LeadTimeStatsGroupsInnerFoundByEnum.organic: 'organic',
  LeadTimeStatsGroupsInnerFoundByEnum.cue: 'cue',
};
