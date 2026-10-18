// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stats_series_series_inner_points_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StatsSeriesSeriesInnerPointsInnerCWProxy {
  StatsSeriesSeriesInnerPointsInner t(DateTime t);

  StatsSeriesSeriesInnerPointsInner value(int value);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsSeriesSeriesInnerPointsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsSeriesSeriesInnerPointsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  StatsSeriesSeriesInnerPointsInner call({DateTime t, int value});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStatsSeriesSeriesInnerPointsInner.copyWith(...)` or call `instanceOfStatsSeriesSeriesInnerPointsInner.copyWith.fieldName(value)` for a single field.
class _$StatsSeriesSeriesInnerPointsInnerCWProxyImpl
    implements _$StatsSeriesSeriesInnerPointsInnerCWProxy {
  const _$StatsSeriesSeriesInnerPointsInnerCWProxyImpl(this._value);

  final StatsSeriesSeriesInnerPointsInner _value;

  @override
  StatsSeriesSeriesInnerPointsInner t(DateTime t) => call(t: t);

  @override
  StatsSeriesSeriesInnerPointsInner value(int value) => call(value: value);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsSeriesSeriesInnerPointsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsSeriesSeriesInnerPointsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StatsSeriesSeriesInnerPointsInner call({
    Object? t = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
  }) {
    return StatsSeriesSeriesInnerPointsInner(
      t: t == const $CopyWithPlaceholder() || t == null
          ? _value.t
          // ignore: cast_nullable_to_non_nullable
          : t as DateTime,
      value: value == const $CopyWithPlaceholder() || value == null
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as int,
    );
  }
}

extension $StatsSeriesSeriesInnerPointsInnerCopyWith
    on StatsSeriesSeriesInnerPointsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStatsSeriesSeriesInnerPointsInner.copyWith(...)` or `instanceOfStatsSeriesSeriesInnerPointsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StatsSeriesSeriesInnerPointsInnerCWProxy get copyWith =>
      _$StatsSeriesSeriesInnerPointsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StatsSeriesSeriesInnerPointsInner _$StatsSeriesSeriesInnerPointsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StatsSeriesSeriesInnerPointsInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['t', 'value']);
  final val = StatsSeriesSeriesInnerPointsInner(
    t: $checkedConvert('t', (v) => DateTime.parse(v as String)),
    value: $checkedConvert('value', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$StatsSeriesSeriesInnerPointsInnerToJson(
  StatsSeriesSeriesInnerPointsInner instance,
) => <String, dynamic>{
  't': instance.t.toIso8601String(),
  'value': instance.value,
};
