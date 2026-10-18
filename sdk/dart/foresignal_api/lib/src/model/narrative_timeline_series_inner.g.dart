// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'narrative_timeline_series_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NarrativeTimelineSeriesInnerCWProxy {
  NarrativeTimelineSeriesInner platform(String platform);

  NarrativeTimelineSeriesInner points(
    List<NarrativeTimelineSeriesInnerPointsInner> points,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativeTimelineSeriesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativeTimelineSeriesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  NarrativeTimelineSeriesInner call({
    String platform,
    List<NarrativeTimelineSeriesInnerPointsInner> points,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNarrativeTimelineSeriesInner.copyWith(...)` or call `instanceOfNarrativeTimelineSeriesInner.copyWith.fieldName(value)` for a single field.
class _$NarrativeTimelineSeriesInnerCWProxyImpl
    implements _$NarrativeTimelineSeriesInnerCWProxy {
  const _$NarrativeTimelineSeriesInnerCWProxyImpl(this._value);

  final NarrativeTimelineSeriesInner _value;

  @override
  NarrativeTimelineSeriesInner platform(String platform) =>
      call(platform: platform);

  @override
  NarrativeTimelineSeriesInner points(
    List<NarrativeTimelineSeriesInnerPointsInner> points,
  ) => call(points: points);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativeTimelineSeriesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativeTimelineSeriesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NarrativeTimelineSeriesInner call({
    Object? platform = const $CopyWithPlaceholder(),
    Object? points = const $CopyWithPlaceholder(),
  }) {
    return NarrativeTimelineSeriesInner(
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as String,
      points: points == const $CopyWithPlaceholder() || points == null
          ? _value.points
          // ignore: cast_nullable_to_non_nullable
          : points as List<NarrativeTimelineSeriesInnerPointsInner>,
    );
  }
}

extension $NarrativeTimelineSeriesInnerCopyWith
    on NarrativeTimelineSeriesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNarrativeTimelineSeriesInner.copyWith(...)` or `instanceOfNarrativeTimelineSeriesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NarrativeTimelineSeriesInnerCWProxy get copyWith =>
      _$NarrativeTimelineSeriesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NarrativeTimelineSeriesInner _$NarrativeTimelineSeriesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NarrativeTimelineSeriesInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['platform', 'points']);
  final val = NarrativeTimelineSeriesInner(
    platform: $checkedConvert('platform', (v) => v as String),
    points: $checkedConvert(
      'points',
      (v) => (v as List<dynamic>)
          .map(
            (e) => NarrativeTimelineSeriesInnerPointsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$NarrativeTimelineSeriesInnerToJson(
  NarrativeTimelineSeriesInner instance,
) => <String, dynamic>{
  'platform': instance.platform,
  'points': instance.points.map((e) => e.toJson()).toList(),
};
