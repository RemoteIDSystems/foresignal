// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'lead_time_stats_filters.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LeadTimeStatsFiltersCWProxy {
  LeadTimeStatsFilters claimType(String? claimType);

  LeadTimeStatsFilters platform(String? platform);

  LeadTimeStatsFilters region(String? region);

  LeadTimeStatsFilters foundBy(LeadTimeStatsFiltersFoundByEnum? foundBy);

  LeadTimeStatsFilters since(DateTime? since);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LeadTimeStatsFilters(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LeadTimeStatsFilters(...).copyWith(id: 12, name: "My name")
  /// ```
  LeadTimeStatsFilters call({
    String? claimType,
    String? platform,
    String? region,
    LeadTimeStatsFiltersFoundByEnum? foundBy,
    DateTime? since,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLeadTimeStatsFilters.copyWith(...)` or call `instanceOfLeadTimeStatsFilters.copyWith.fieldName(value)` for a single field.
class _$LeadTimeStatsFiltersCWProxyImpl
    implements _$LeadTimeStatsFiltersCWProxy {
  const _$LeadTimeStatsFiltersCWProxyImpl(this._value);

  final LeadTimeStatsFilters _value;

  @override
  LeadTimeStatsFilters claimType(String? claimType) =>
      call(claimType: claimType);

  @override
  LeadTimeStatsFilters platform(String? platform) => call(platform: platform);

  @override
  LeadTimeStatsFilters region(String? region) => call(region: region);

  @override
  LeadTimeStatsFilters foundBy(LeadTimeStatsFiltersFoundByEnum? foundBy) =>
      call(foundBy: foundBy);

  @override
  LeadTimeStatsFilters since(DateTime? since) => call(since: since);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LeadTimeStatsFilters(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LeadTimeStatsFilters(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LeadTimeStatsFilters call({
    Object? claimType = const $CopyWithPlaceholder(),
    Object? platform = const $CopyWithPlaceholder(),
    Object? region = const $CopyWithPlaceholder(),
    Object? foundBy = const $CopyWithPlaceholder(),
    Object? since = const $CopyWithPlaceholder(),
  }) {
    return LeadTimeStatsFilters(
      claimType: claimType == const $CopyWithPlaceholder()
          ? _value.claimType
          // ignore: cast_nullable_to_non_nullable
          : claimType as String?,
      platform: platform == const $CopyWithPlaceholder()
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as String?,
      region: region == const $CopyWithPlaceholder()
          ? _value.region
          // ignore: cast_nullable_to_non_nullable
          : region as String?,
      foundBy: foundBy == const $CopyWithPlaceholder()
          ? _value.foundBy
          // ignore: cast_nullable_to_non_nullable
          : foundBy as LeadTimeStatsFiltersFoundByEnum?,
      since: since == const $CopyWithPlaceholder()
          ? _value.since
          // ignore: cast_nullable_to_non_nullable
          : since as DateTime?,
    );
  }
}

extension $LeadTimeStatsFiltersCopyWith on LeadTimeStatsFilters {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLeadTimeStatsFilters.copyWith(...)` or `instanceOfLeadTimeStatsFilters.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LeadTimeStatsFiltersCWProxy get copyWith =>
      _$LeadTimeStatsFiltersCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LeadTimeStatsFilters _$LeadTimeStatsFiltersFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('LeadTimeStatsFilters', json, ($checkedConvert) {
  final val = LeadTimeStatsFilters(
    claimType: $checkedConvert('claimType', (v) => v as String?),
    platform: $checkedConvert('platform', (v) => v as String?),
    region: $checkedConvert('region', (v) => v as String?),
    foundBy: $checkedConvert(
      'foundBy',
      (v) => $enumDecodeNullable(_$LeadTimeStatsFiltersFoundByEnumEnumMap, v),
    ),
    since: $checkedConvert(
      'since',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$LeadTimeStatsFiltersToJson(
  LeadTimeStatsFilters instance,
) => <String, dynamic>{
  'claimType': ?instance.claimType,
  'platform': ?instance.platform,
  'region': ?instance.region,
  'foundBy': ?_$LeadTimeStatsFiltersFoundByEnumEnumMap[instance.foundBy],
  'since': ?instance.since?.toIso8601String(),
};

const _$LeadTimeStatsFiltersFoundByEnumEnumMap = {
  LeadTimeStatsFiltersFoundByEnum.organic: 'organic',
  LeadTimeStatsFiltersFoundByEnum.cue: 'cue',
};
