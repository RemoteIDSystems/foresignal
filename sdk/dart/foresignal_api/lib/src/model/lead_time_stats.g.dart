// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'lead_time_stats.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LeadTimeStatsCWProxy {
  LeadTimeStats generatedAt(DateTime generatedAt);

  LeadTimeStats trainingCutoff(DateTime? trainingCutoff);

  LeadTimeStats filters(LeadTimeStatsFilters? filters);

  LeadTimeStats groups(List<LeadTimeStatsGroupsInner> groups);

  LeadTimeStats caveats(List<Caveat>? caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LeadTimeStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LeadTimeStats(...).copyWith(id: 12, name: "My name")
  /// ```
  LeadTimeStats call({
    DateTime generatedAt,
    DateTime? trainingCutoff,
    LeadTimeStatsFilters? filters,
    List<LeadTimeStatsGroupsInner> groups,
    List<Caveat>? caveats,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLeadTimeStats.copyWith(...)` or call `instanceOfLeadTimeStats.copyWith.fieldName(value)` for a single field.
class _$LeadTimeStatsCWProxyImpl implements _$LeadTimeStatsCWProxy {
  const _$LeadTimeStatsCWProxyImpl(this._value);

  final LeadTimeStats _value;

  @override
  LeadTimeStats generatedAt(DateTime generatedAt) =>
      call(generatedAt: generatedAt);

  @override
  LeadTimeStats trainingCutoff(DateTime? trainingCutoff) =>
      call(trainingCutoff: trainingCutoff);

  @override
  LeadTimeStats filters(LeadTimeStatsFilters? filters) =>
      call(filters: filters);

  @override
  LeadTimeStats groups(List<LeadTimeStatsGroupsInner> groups) =>
      call(groups: groups);

  @override
  LeadTimeStats caveats(List<Caveat>? caveats) => call(caveats: caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LeadTimeStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LeadTimeStats(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LeadTimeStats call({
    Object? generatedAt = const $CopyWithPlaceholder(),
    Object? trainingCutoff = const $CopyWithPlaceholder(),
    Object? filters = const $CopyWithPlaceholder(),
    Object? groups = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
  }) {
    return LeadTimeStats(
      generatedAt:
          generatedAt == const $CopyWithPlaceholder() || generatedAt == null
          ? _value.generatedAt
          // ignore: cast_nullable_to_non_nullable
          : generatedAt as DateTime,
      trainingCutoff: trainingCutoff == const $CopyWithPlaceholder()
          ? _value.trainingCutoff
          // ignore: cast_nullable_to_non_nullable
          : trainingCutoff as DateTime?,
      filters: filters == const $CopyWithPlaceholder()
          ? _value.filters
          // ignore: cast_nullable_to_non_nullable
          : filters as LeadTimeStatsFilters?,
      groups: groups == const $CopyWithPlaceholder() || groups == null
          ? _value.groups
          // ignore: cast_nullable_to_non_nullable
          : groups as List<LeadTimeStatsGroupsInner>,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
    );
  }
}

extension $LeadTimeStatsCopyWith on LeadTimeStats {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLeadTimeStats.copyWith(...)` or `instanceOfLeadTimeStats.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LeadTimeStatsCWProxy get copyWith => _$LeadTimeStatsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LeadTimeStats _$LeadTimeStatsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LeadTimeStats', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['generatedAt', 'trainingCutoff', 'groups'],
      );
      final val = LeadTimeStats(
        generatedAt: $checkedConvert(
          'generatedAt',
          (v) => DateTime.parse(v as String),
        ),
        trainingCutoff: $checkedConvert(
          'trainingCutoff',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        filters: $checkedConvert(
          'filters',
          (v) => v == null
              ? null
              : LeadTimeStatsFilters.fromJson(v as Map<String, dynamic>),
        ),
        groups: $checkedConvert(
          'groups',
          (v) => (v as List<dynamic>)
              .map(
                (e) => LeadTimeStatsGroupsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        caveats: $checkedConvert(
          'caveats',
          (v) => (v as List<dynamic>?)
              ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$LeadTimeStatsToJson(LeadTimeStats instance) =>
    <String, dynamic>{
      'generatedAt': instance.generatedAt.toIso8601String(),
      'trainingCutoff': instance.trainingCutoff?.toIso8601String(),
      'filters': ?instance.filters?.toJson(),
      'groups': instance.groups.map((e) => e.toJson()).toList(),
      'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
    };
