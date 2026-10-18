// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_study_window_stats_placebo.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventStudyWindowStatsPlaceboCWProxy {
  EventStudyWindowStatsPlacebo count(int count);

  EventStudyWindowStatsPlacebo mean(num? mean);

  EventStudyWindowStatsPlacebo sd(num? sd);

  EventStudyWindowStatsPlacebo p05(num? p05);

  EventStudyWindowStatsPlacebo p50(num? p50);

  EventStudyWindowStatsPlacebo p95(num? p95);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyWindowStatsPlacebo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyWindowStatsPlacebo(...).copyWith(id: 12, name: "My name")
  /// ```
  EventStudyWindowStatsPlacebo call({
    int count,
    num? mean,
    num? sd,
    num? p05,
    num? p50,
    num? p95,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventStudyWindowStatsPlacebo.copyWith(...)` or call `instanceOfEventStudyWindowStatsPlacebo.copyWith.fieldName(value)` for a single field.
class _$EventStudyWindowStatsPlaceboCWProxyImpl
    implements _$EventStudyWindowStatsPlaceboCWProxy {
  const _$EventStudyWindowStatsPlaceboCWProxyImpl(this._value);

  final EventStudyWindowStatsPlacebo _value;

  @override
  EventStudyWindowStatsPlacebo count(int count) => call(count: count);

  @override
  EventStudyWindowStatsPlacebo mean(num? mean) => call(mean: mean);

  @override
  EventStudyWindowStatsPlacebo sd(num? sd) => call(sd: sd);

  @override
  EventStudyWindowStatsPlacebo p05(num? p05) => call(p05: p05);

  @override
  EventStudyWindowStatsPlacebo p50(num? p50) => call(p50: p50);

  @override
  EventStudyWindowStatsPlacebo p95(num? p95) => call(p95: p95);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyWindowStatsPlacebo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyWindowStatsPlacebo(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventStudyWindowStatsPlacebo call({
    Object? count = const $CopyWithPlaceholder(),
    Object? mean = const $CopyWithPlaceholder(),
    Object? sd = const $CopyWithPlaceholder(),
    Object? p05 = const $CopyWithPlaceholder(),
    Object? p50 = const $CopyWithPlaceholder(),
    Object? p95 = const $CopyWithPlaceholder(),
  }) {
    return EventStudyWindowStatsPlacebo(
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
      mean: mean == const $CopyWithPlaceholder()
          ? _value.mean
          // ignore: cast_nullable_to_non_nullable
          : mean as num?,
      sd: sd == const $CopyWithPlaceholder()
          ? _value.sd
          // ignore: cast_nullable_to_non_nullable
          : sd as num?,
      p05: p05 == const $CopyWithPlaceholder()
          ? _value.p05
          // ignore: cast_nullable_to_non_nullable
          : p05 as num?,
      p50: p50 == const $CopyWithPlaceholder()
          ? _value.p50
          // ignore: cast_nullable_to_non_nullable
          : p50 as num?,
      p95: p95 == const $CopyWithPlaceholder()
          ? _value.p95
          // ignore: cast_nullable_to_non_nullable
          : p95 as num?,
    );
  }
}

extension $EventStudyWindowStatsPlaceboCopyWith
    on EventStudyWindowStatsPlacebo {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventStudyWindowStatsPlacebo.copyWith(...)` or `instanceOfEventStudyWindowStatsPlacebo.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventStudyWindowStatsPlaceboCWProxy get copyWith =>
      _$EventStudyWindowStatsPlaceboCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventStudyWindowStatsPlacebo _$EventStudyWindowStatsPlaceboFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EventStudyWindowStatsPlacebo', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['count']);
  final val = EventStudyWindowStatsPlacebo(
    count: $checkedConvert('count', (v) => (v as num).toInt()),
    mean: $checkedConvert('mean', (v) => v as num?),
    sd: $checkedConvert('sd', (v) => v as num?),
    p05: $checkedConvert('p05', (v) => v as num?),
    p50: $checkedConvert('p50', (v) => v as num?),
    p95: $checkedConvert('p95', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$EventStudyWindowStatsPlaceboToJson(
  EventStudyWindowStatsPlacebo instance,
) => <String, dynamic>{
  'count': instance.count,
  'mean': ?instance.mean,
  'sd': ?instance.sd,
  'p05': ?instance.p05,
  'p50': ?instance.p50,
  'p95': ?instance.p95,
};
