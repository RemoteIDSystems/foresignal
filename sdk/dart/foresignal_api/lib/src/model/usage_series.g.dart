// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'usage_series.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UsageSeriesCWProxy {
  UsageSeries sourceId(String sourceId);

  UsageSeries resolution(UsageSeriesResolutionEnum resolution);

  UsageSeries points(List<UsagePoint> points);

  UsageSeries totals(UsagePoint? totals);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UsageSeries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UsageSeries(...).copyWith(id: 12, name: "My name")
  /// ```
  UsageSeries call({
    String sourceId,
    UsageSeriesResolutionEnum resolution,
    List<UsagePoint> points,
    UsagePoint? totals,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfUsageSeries.copyWith(...)` or call `instanceOfUsageSeries.copyWith.fieldName(value)` for a single field.
class _$UsageSeriesCWProxyImpl implements _$UsageSeriesCWProxy {
  const _$UsageSeriesCWProxyImpl(this._value);

  final UsageSeries _value;

  @override
  UsageSeries sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  UsageSeries resolution(UsageSeriesResolutionEnum resolution) =>
      call(resolution: resolution);

  @override
  UsageSeries points(List<UsagePoint> points) => call(points: points);

  @override
  UsageSeries totals(UsagePoint? totals) => call(totals: totals);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UsageSeries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UsageSeries(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  UsageSeries call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? resolution = const $CopyWithPlaceholder(),
    Object? points = const $CopyWithPlaceholder(),
    Object? totals = const $CopyWithPlaceholder(),
  }) {
    return UsageSeries(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      resolution:
          resolution == const $CopyWithPlaceholder() || resolution == null
          ? _value.resolution
          // ignore: cast_nullable_to_non_nullable
          : resolution as UsageSeriesResolutionEnum,
      points: points == const $CopyWithPlaceholder() || points == null
          ? _value.points
          // ignore: cast_nullable_to_non_nullable
          : points as List<UsagePoint>,
      totals: totals == const $CopyWithPlaceholder()
          ? _value.totals
          // ignore: cast_nullable_to_non_nullable
          : totals as UsagePoint?,
    );
  }
}

extension $UsageSeriesCopyWith on UsageSeries {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfUsageSeries.copyWith(...)` or `instanceOfUsageSeries.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UsageSeriesCWProxy get copyWith => _$UsageSeriesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UsageSeries _$UsageSeriesFromJson(Map<String, dynamic> json) => $checkedCreate(
  'UsageSeries',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['sourceId', 'resolution', 'points']);
    final val = UsageSeries(
      sourceId: $checkedConvert('sourceId', (v) => v as String),
      resolution: $checkedConvert(
        'resolution',
        (v) => $enumDecode(_$UsageSeriesResolutionEnumEnumMap, v),
      ),
      points: $checkedConvert(
        'points',
        (v) => (v as List<dynamic>)
            .map((e) => UsagePoint.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
      totals: $checkedConvert(
        'totals',
        (v) =>
            v == null ? null : UsagePoint.fromJson(v as Map<String, dynamic>),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$UsageSeriesToJson(UsageSeries instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'resolution': _$UsageSeriesResolutionEnumEnumMap[instance.resolution]!,
      'points': instance.points.map((e) => e.toJson()).toList(),
      'totals': ?instance.totals?.toJson(),
    };

const _$UsageSeriesResolutionEnumEnumMap = {
  UsageSeriesResolutionEnum.minute: 'minute',
  UsageSeriesResolutionEnum.hour: 'hour',
  UsageSeriesResolutionEnum.day: 'day',
};
