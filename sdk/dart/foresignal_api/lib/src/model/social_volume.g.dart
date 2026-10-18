// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_volume.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialVolumeCWProxy {
  SocialVolume scope(SocialVolumeScope scope);

  SocialVolume metric(SocialVolumeMetricEnum metric);

  SocialVolume interval(SocialVolumeIntervalEnum interval);

  SocialVolume since(DateTime since);

  SocialVolume until(DateTime until);

  SocialVolume series(List<SocialVolumeSeries> series);

  SocialVolume baseline(DetectorRef baseline);

  SocialVolume caveats(List<Caveat>? caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVolume(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVolume(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialVolume call({
    SocialVolumeScope scope,
    SocialVolumeMetricEnum metric,
    SocialVolumeIntervalEnum interval,
    DateTime since,
    DateTime until,
    List<SocialVolumeSeries> series,
    DetectorRef baseline,
    List<Caveat>? caveats,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialVolume.copyWith(...)` or call `instanceOfSocialVolume.copyWith.fieldName(value)` for a single field.
class _$SocialVolumeCWProxyImpl implements _$SocialVolumeCWProxy {
  const _$SocialVolumeCWProxyImpl(this._value);

  final SocialVolume _value;

  @override
  SocialVolume scope(SocialVolumeScope scope) => call(scope: scope);

  @override
  SocialVolume metric(SocialVolumeMetricEnum metric) => call(metric: metric);

  @override
  SocialVolume interval(SocialVolumeIntervalEnum interval) =>
      call(interval: interval);

  @override
  SocialVolume since(DateTime since) => call(since: since);

  @override
  SocialVolume until(DateTime until) => call(until: until);

  @override
  SocialVolume series(List<SocialVolumeSeries> series) => call(series: series);

  @override
  SocialVolume baseline(DetectorRef baseline) => call(baseline: baseline);

  @override
  SocialVolume caveats(List<Caveat>? caveats) => call(caveats: caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVolume(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVolume(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialVolume call({
    Object? scope = const $CopyWithPlaceholder(),
    Object? metric = const $CopyWithPlaceholder(),
    Object? interval = const $CopyWithPlaceholder(),
    Object? since = const $CopyWithPlaceholder(),
    Object? until = const $CopyWithPlaceholder(),
    Object? series = const $CopyWithPlaceholder(),
    Object? baseline = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
  }) {
    return SocialVolume(
      scope: scope == const $CopyWithPlaceholder() || scope == null
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as SocialVolumeScope,
      metric: metric == const $CopyWithPlaceholder() || metric == null
          ? _value.metric
          // ignore: cast_nullable_to_non_nullable
          : metric as SocialVolumeMetricEnum,
      interval: interval == const $CopyWithPlaceholder() || interval == null
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as SocialVolumeIntervalEnum,
      since: since == const $CopyWithPlaceholder() || since == null
          ? _value.since
          // ignore: cast_nullable_to_non_nullable
          : since as DateTime,
      until: until == const $CopyWithPlaceholder() || until == null
          ? _value.until
          // ignore: cast_nullable_to_non_nullable
          : until as DateTime,
      series: series == const $CopyWithPlaceholder() || series == null
          ? _value.series
          // ignore: cast_nullable_to_non_nullable
          : series as List<SocialVolumeSeries>,
      baseline: baseline == const $CopyWithPlaceholder() || baseline == null
          ? _value.baseline
          // ignore: cast_nullable_to_non_nullable
          : baseline as DetectorRef,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
    );
  }
}

extension $SocialVolumeCopyWith on SocialVolume {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialVolume.copyWith(...)` or `instanceOfSocialVolume.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialVolumeCWProxy get copyWith => _$SocialVolumeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialVolume _$SocialVolumeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialVolume', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'scope',
          'metric',
          'interval',
          'since',
          'until',
          'series',
          'baseline',
        ],
      );
      final val = SocialVolume(
        scope: $checkedConvert(
          'scope',
          (v) => SocialVolumeScope.fromJson(v as Map<String, dynamic>),
        ),
        metric: $checkedConvert(
          'metric',
          (v) => $enumDecode(_$SocialVolumeMetricEnumEnumMap, v),
        ),
        interval: $checkedConvert(
          'interval',
          (v) => $enumDecode(_$SocialVolumeIntervalEnumEnumMap, v),
        ),
        since: $checkedConvert('since', (v) => DateTime.parse(v as String)),
        until: $checkedConvert('until', (v) => DateTime.parse(v as String)),
        series: $checkedConvert(
          'series',
          (v) => (v as List<dynamic>)
              .map(
                (e) => SocialVolumeSeries.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
        baseline: $checkedConvert(
          'baseline',
          (v) => DetectorRef.fromJson(v as Map<String, dynamic>),
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

Map<String, dynamic> _$SocialVolumeToJson(SocialVolume instance) =>
    <String, dynamic>{
      'scope': instance.scope.toJson(),
      'metric': _$SocialVolumeMetricEnumEnumMap[instance.metric]!,
      'interval': _$SocialVolumeIntervalEnumEnumMap[instance.interval]!,
      'since': instance.since.toIso8601String(),
      'until': instance.until.toIso8601String(),
      'series': instance.series.map((e) => e.toJson()).toList(),
      'baseline': instance.baseline.toJson(),
      'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
    };

const _$SocialVolumeMetricEnumEnumMap = {
  SocialVolumeMetricEnum.posts: 'posts',
  SocialVolumeMetricEnum.uniqueAuthors: 'uniqueAuthors',
  SocialVolumeMetricEnum.reposts: 'reposts',
  SocialVolumeMetricEnum.engagement: 'engagement',
  SocialVolumeMetricEnum.denominator: 'denominator',
};

const _$SocialVolumeIntervalEnumEnumMap = {
  SocialVolumeIntervalEnum.PT1H: 'PT1H',
  SocialVolumeIntervalEnum.P1D: 'P1D',
};
