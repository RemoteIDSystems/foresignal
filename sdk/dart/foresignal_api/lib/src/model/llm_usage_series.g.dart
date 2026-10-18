// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_usage_series.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmUsageSeriesCWProxy {
  LlmUsageSeries provider(String? provider);

  LlmUsageSeries resolution(LlmUsageSeriesResolutionEnum resolution);

  LlmUsageSeries points(List<LlmUsagePoint> points);

  LlmUsageSeries totals(LlmUsagePoint? totals);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmUsageSeries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmUsageSeries(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmUsageSeries call({
    String? provider,
    LlmUsageSeriesResolutionEnum resolution,
    List<LlmUsagePoint> points,
    LlmUsagePoint? totals,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmUsageSeries.copyWith(...)` or call `instanceOfLlmUsageSeries.copyWith.fieldName(value)` for a single field.
class _$LlmUsageSeriesCWProxyImpl implements _$LlmUsageSeriesCWProxy {
  const _$LlmUsageSeriesCWProxyImpl(this._value);

  final LlmUsageSeries _value;

  @override
  LlmUsageSeries provider(String? provider) => call(provider: provider);

  @override
  LlmUsageSeries resolution(LlmUsageSeriesResolutionEnum resolution) =>
      call(resolution: resolution);

  @override
  LlmUsageSeries points(List<LlmUsagePoint> points) => call(points: points);

  @override
  LlmUsageSeries totals(LlmUsagePoint? totals) => call(totals: totals);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmUsageSeries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmUsageSeries(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmUsageSeries call({
    Object? provider = const $CopyWithPlaceholder(),
    Object? resolution = const $CopyWithPlaceholder(),
    Object? points = const $CopyWithPlaceholder(),
    Object? totals = const $CopyWithPlaceholder(),
  }) {
    return LlmUsageSeries(
      provider: provider == const $CopyWithPlaceholder()
          ? _value.provider
          // ignore: cast_nullable_to_non_nullable
          : provider as String?,
      resolution:
          resolution == const $CopyWithPlaceholder() || resolution == null
          ? _value.resolution
          // ignore: cast_nullable_to_non_nullable
          : resolution as LlmUsageSeriesResolutionEnum,
      points: points == const $CopyWithPlaceholder() || points == null
          ? _value.points
          // ignore: cast_nullable_to_non_nullable
          : points as List<LlmUsagePoint>,
      totals: totals == const $CopyWithPlaceholder()
          ? _value.totals
          // ignore: cast_nullable_to_non_nullable
          : totals as LlmUsagePoint?,
    );
  }
}

extension $LlmUsageSeriesCopyWith on LlmUsageSeries {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmUsageSeries.copyWith(...)` or `instanceOfLlmUsageSeries.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmUsageSeriesCWProxy get copyWith => _$LlmUsageSeriesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmUsageSeries _$LlmUsageSeriesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LlmUsageSeries', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['resolution', 'points']);
      final val = LlmUsageSeries(
        provider: $checkedConvert('provider', (v) => v as String?),
        resolution: $checkedConvert(
          'resolution',
          (v) => $enumDecode(_$LlmUsageSeriesResolutionEnumEnumMap, v),
        ),
        points: $checkedConvert(
          'points',
          (v) => (v as List<dynamic>)
              .map((e) => LlmUsagePoint.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        totals: $checkedConvert(
          'totals',
          (v) => v == null
              ? null
              : LlmUsagePoint.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$LlmUsageSeriesToJson(LlmUsageSeries instance) =>
    <String, dynamic>{
      'provider': ?instance.provider,
      'resolution': _$LlmUsageSeriesResolutionEnumEnumMap[instance.resolution]!,
      'points': instance.points.map((e) => e.toJson()).toList(),
      'totals': ?instance.totals?.toJson(),
    };

const _$LlmUsageSeriesResolutionEnumEnumMap = {
  LlmUsageSeriesResolutionEnum.minute: 'minute',
  LlmUsageSeriesResolutionEnum.hour: 'hour',
  LlmUsageSeriesResolutionEnum.day: 'day',
};
