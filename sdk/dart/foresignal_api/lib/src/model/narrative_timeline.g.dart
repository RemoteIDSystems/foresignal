// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'narrative_timeline.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NarrativeTimelineCWProxy {
  NarrativeTimeline narrativeId(String narrativeId);

  NarrativeTimeline interval(NarrativeTimelineIntervalEnum interval);

  NarrativeTimeline series(List<NarrativeTimelineSeriesInner> series);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativeTimeline(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativeTimeline(...).copyWith(id: 12, name: "My name")
  /// ```
  NarrativeTimeline call({
    String narrativeId,
    NarrativeTimelineIntervalEnum interval,
    List<NarrativeTimelineSeriesInner> series,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNarrativeTimeline.copyWith(...)` or call `instanceOfNarrativeTimeline.copyWith.fieldName(value)` for a single field.
class _$NarrativeTimelineCWProxyImpl implements _$NarrativeTimelineCWProxy {
  const _$NarrativeTimelineCWProxyImpl(this._value);

  final NarrativeTimeline _value;

  @override
  NarrativeTimeline narrativeId(String narrativeId) =>
      call(narrativeId: narrativeId);

  @override
  NarrativeTimeline interval(NarrativeTimelineIntervalEnum interval) =>
      call(interval: interval);

  @override
  NarrativeTimeline series(List<NarrativeTimelineSeriesInner> series) =>
      call(series: series);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativeTimeline(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativeTimeline(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NarrativeTimeline call({
    Object? narrativeId = const $CopyWithPlaceholder(),
    Object? interval = const $CopyWithPlaceholder(),
    Object? series = const $CopyWithPlaceholder(),
  }) {
    return NarrativeTimeline(
      narrativeId:
          narrativeId == const $CopyWithPlaceholder() || narrativeId == null
          ? _value.narrativeId
          // ignore: cast_nullable_to_non_nullable
          : narrativeId as String,
      interval: interval == const $CopyWithPlaceholder() || interval == null
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as NarrativeTimelineIntervalEnum,
      series: series == const $CopyWithPlaceholder() || series == null
          ? _value.series
          // ignore: cast_nullable_to_non_nullable
          : series as List<NarrativeTimelineSeriesInner>,
    );
  }
}

extension $NarrativeTimelineCopyWith on NarrativeTimeline {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNarrativeTimeline.copyWith(...)` or `instanceOfNarrativeTimeline.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NarrativeTimelineCWProxy get copyWith =>
      _$NarrativeTimelineCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NarrativeTimeline _$NarrativeTimelineFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NarrativeTimeline', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['narrativeId', 'interval', 'series'],
      );
      final val = NarrativeTimeline(
        narrativeId: $checkedConvert('narrativeId', (v) => v as String),
        interval: $checkedConvert(
          'interval',
          (v) => $enumDecode(_$NarrativeTimelineIntervalEnumEnumMap, v),
        ),
        series: $checkedConvert(
          'series',
          (v) => (v as List<dynamic>)
              .map(
                (e) => NarrativeTimelineSeriesInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$NarrativeTimelineToJson(NarrativeTimeline instance) =>
    <String, dynamic>{
      'narrativeId': instance.narrativeId,
      'interval': _$NarrativeTimelineIntervalEnumEnumMap[instance.interval]!,
      'series': instance.series.map((e) => e.toJson()).toList(),
    };

const _$NarrativeTimelineIntervalEnumEnumMap = {
  NarrativeTimelineIntervalEnum.PT5M: 'PT5M',
  NarrativeTimelineIntervalEnum.PT15M: 'PT15M',
  NarrativeTimelineIntervalEnum.PT1H: 'PT1H',
  NarrativeTimelineIntervalEnum.P1D: 'P1D',
};
