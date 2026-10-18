// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stats_series_series_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StatsSeriesSeriesInnerCWProxy {
  StatsSeriesSeriesInner key(String? key);

  StatsSeriesSeriesInner points(List<StatsSeriesSeriesInnerPointsInner> points);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsSeriesSeriesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsSeriesSeriesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  StatsSeriesSeriesInner call({
    String? key,
    List<StatsSeriesSeriesInnerPointsInner> points,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStatsSeriesSeriesInner.copyWith(...)` or call `instanceOfStatsSeriesSeriesInner.copyWith.fieldName(value)` for a single field.
class _$StatsSeriesSeriesInnerCWProxyImpl
    implements _$StatsSeriesSeriesInnerCWProxy {
  const _$StatsSeriesSeriesInnerCWProxyImpl(this._value);

  final StatsSeriesSeriesInner _value;

  @override
  StatsSeriesSeriesInner key(String? key) => call(key: key);

  @override
  StatsSeriesSeriesInner points(
    List<StatsSeriesSeriesInnerPointsInner> points,
  ) => call(points: points);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsSeriesSeriesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsSeriesSeriesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StatsSeriesSeriesInner call({
    Object? key = const $CopyWithPlaceholder(),
    Object? points = const $CopyWithPlaceholder(),
  }) {
    return StatsSeriesSeriesInner(
      key: key == const $CopyWithPlaceholder()
          ? _value.key
          // ignore: cast_nullable_to_non_nullable
          : key as String?,
      points: points == const $CopyWithPlaceholder() || points == null
          ? _value.points
          // ignore: cast_nullable_to_non_nullable
          : points as List<StatsSeriesSeriesInnerPointsInner>,
    );
  }
}

extension $StatsSeriesSeriesInnerCopyWith on StatsSeriesSeriesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStatsSeriesSeriesInner.copyWith(...)` or `instanceOfStatsSeriesSeriesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StatsSeriesSeriesInnerCWProxy get copyWith =>
      _$StatsSeriesSeriesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StatsSeriesSeriesInner _$StatsSeriesSeriesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StatsSeriesSeriesInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['points']);
  final val = StatsSeriesSeriesInner(
    key: $checkedConvert('key', (v) => v as String?),
    points: $checkedConvert(
      'points',
      (v) => (v as List<dynamic>)
          .map(
            (e) => StatsSeriesSeriesInnerPointsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$StatsSeriesSeriesInnerToJson(
  StatsSeriesSeriesInner instance,
) => <String, dynamic>{
  'key': ?instance.key,
  'points': instance.points.map((e) => e.toJson()).toList(),
};
