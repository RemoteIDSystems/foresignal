// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analysis_series.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalysisSeriesCWProxy {
  AnalysisSeries ref(String ref);

  AnalysisSeries enabled(bool enabled);

  AnalysisSeries detectors(List<AnalysisSeriesDetectorsEnum> detectors);

  AnalysisSeries frequency(AnalysisSeriesFrequencyEnum frequency);

  AnalysisSeries family(String? family);

  AnalysisSeries regionCode(Object? regionCode);

  AnalysisSeries watchPairs(List<WatchPair>? watchPairs);

  AnalysisSeries origin(AnalysisSeriesOriginEnum origin);

  AnalysisSeries createdAt(DateTime? createdAt);

  AnalysisSeries lastRunAt(DateTime? lastRunAt);

  AnalysisSeries lastError(String? lastError);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisSeries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisSeries(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalysisSeries call({
    String ref,
    bool enabled,
    List<AnalysisSeriesDetectorsEnum> detectors,
    AnalysisSeriesFrequencyEnum frequency,
    String? family,
    Object? regionCode,
    List<WatchPair>? watchPairs,
    AnalysisSeriesOriginEnum origin,
    DateTime? createdAt,
    DateTime? lastRunAt,
    String? lastError,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalysisSeries.copyWith(...)` or call `instanceOfAnalysisSeries.copyWith.fieldName(value)` for a single field.
class _$AnalysisSeriesCWProxyImpl implements _$AnalysisSeriesCWProxy {
  const _$AnalysisSeriesCWProxyImpl(this._value);

  final AnalysisSeries _value;

  @override
  AnalysisSeries ref(String ref) => call(ref: ref);

  @override
  AnalysisSeries enabled(bool enabled) => call(enabled: enabled);

  @override
  AnalysisSeries detectors(List<AnalysisSeriesDetectorsEnum> detectors) =>
      call(detectors: detectors);

  @override
  AnalysisSeries frequency(AnalysisSeriesFrequencyEnum frequency) =>
      call(frequency: frequency);

  @override
  AnalysisSeries family(String? family) => call(family: family);

  @override
  AnalysisSeries regionCode(Object? regionCode) => call(regionCode: regionCode);

  @override
  AnalysisSeries watchPairs(List<WatchPair>? watchPairs) =>
      call(watchPairs: watchPairs);

  @override
  AnalysisSeries origin(AnalysisSeriesOriginEnum origin) =>
      call(origin: origin);

  @override
  AnalysisSeries createdAt(DateTime? createdAt) => call(createdAt: createdAt);

  @override
  AnalysisSeries lastRunAt(DateTime? lastRunAt) => call(lastRunAt: lastRunAt);

  @override
  AnalysisSeries lastError(String? lastError) => call(lastError: lastError);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisSeries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisSeries(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalysisSeries call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? detectors = const $CopyWithPlaceholder(),
    Object? frequency = const $CopyWithPlaceholder(),
    Object? family = const $CopyWithPlaceholder(),
    Object? regionCode = const $CopyWithPlaceholder(),
    Object? watchPairs = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? lastRunAt = const $CopyWithPlaceholder(),
    Object? lastError = const $CopyWithPlaceholder(),
  }) {
    return AnalysisSeries(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      detectors: detectors == const $CopyWithPlaceholder() || detectors == null
          ? _value.detectors
          // ignore: cast_nullable_to_non_nullable
          : detectors as List<AnalysisSeriesDetectorsEnum>,
      frequency: frequency == const $CopyWithPlaceholder() || frequency == null
          ? _value.frequency
          // ignore: cast_nullable_to_non_nullable
          : frequency as AnalysisSeriesFrequencyEnum,
      family: family == const $CopyWithPlaceholder()
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String?,
      regionCode: regionCode == const $CopyWithPlaceholder()
          ? _value.regionCode
          // ignore: cast_nullable_to_non_nullable
          : regionCode as Object?,
      watchPairs: watchPairs == const $CopyWithPlaceholder()
          ? _value.watchPairs
          // ignore: cast_nullable_to_non_nullable
          : watchPairs as List<WatchPair>?,
      origin: origin == const $CopyWithPlaceholder() || origin == null
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as AnalysisSeriesOriginEnum,
      createdAt: createdAt == const $CopyWithPlaceholder()
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime?,
      lastRunAt: lastRunAt == const $CopyWithPlaceholder()
          ? _value.lastRunAt
          // ignore: cast_nullable_to_non_nullable
          : lastRunAt as DateTime?,
      lastError: lastError == const $CopyWithPlaceholder()
          ? _value.lastError
          // ignore: cast_nullable_to_non_nullable
          : lastError as String?,
    );
  }
}

extension $AnalysisSeriesCopyWith on AnalysisSeries {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalysisSeries.copyWith(...)` or `instanceOfAnalysisSeries.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalysisSeriesCWProxy get copyWith => _$AnalysisSeriesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalysisSeries _$AnalysisSeriesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalysisSeries', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'ref',
          'enabled',
          'detectors',
          'frequency',
          'origin',
        ],
      );
      final val = AnalysisSeries(
        ref: $checkedConvert('ref', (v) => v as String),
        enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
        detectors: $checkedConvert(
          'detectors',
          (v) => (v as List<dynamic>)
              .map((e) => $enumDecode(_$AnalysisSeriesDetectorsEnumEnumMap, e))
              .toList(),
        ),
        frequency: $checkedConvert(
          'frequency',
          (v) => $enumDecode(_$AnalysisSeriesFrequencyEnumEnumMap, v),
        ),
        family: $checkedConvert('family', (v) => v as String?),
        regionCode: $checkedConvert('regionCode', (v) => v),
        watchPairs: $checkedConvert(
          'watchPairs',
          (v) => (v as List<dynamic>?)
              ?.map((e) => WatchPair.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        origin: $checkedConvert(
          'origin',
          (v) => $enumDecode(_$AnalysisSeriesOriginEnumEnumMap, v),
        ),
        createdAt: $checkedConvert(
          'createdAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        lastRunAt: $checkedConvert(
          'lastRunAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        lastError: $checkedConvert('lastError', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AnalysisSeriesToJson(AnalysisSeries instance) =>
    <String, dynamic>{
      'ref': instance.ref,
      'enabled': instance.enabled,
      'detectors': instance.detectors
          .map((e) => _$AnalysisSeriesDetectorsEnumEnumMap[e]!)
          .toList(),
      'frequency': _$AnalysisSeriesFrequencyEnumEnumMap[instance.frequency]!,
      'family': ?instance.family,
      'regionCode': ?instance.regionCode,
      'watchPairs': ?instance.watchPairs?.map((e) => e.toJson()).toList(),
      'origin': _$AnalysisSeriesOriginEnumEnumMap[instance.origin]!,
      'createdAt': ?instance.createdAt?.toIso8601String(),
      'lastRunAt': ?instance.lastRunAt?.toIso8601String(),
      'lastError': ?instance.lastError,
    };

const _$AnalysisSeriesDetectorsEnumEnumMap = {
  AnalysisSeriesDetectorsEnum.changepointCusum: 'changepoint-cusum',
  AnalysisSeriesDetectorsEnum.changepointBocpd: 'changepoint-bocpd',
  AnalysisSeriesDetectorsEnum.forecast: 'forecast',
};

const _$AnalysisSeriesFrequencyEnumEnumMap = {
  AnalysisSeriesFrequencyEnum.hour: 'hour',
  AnalysisSeriesFrequencyEnum.day: 'day',
  AnalysisSeriesFrequencyEnum.week: 'week',
};

const _$AnalysisSeriesOriginEnumEnumMap = {
  AnalysisSeriesOriginEnum.auto: 'auto',
  AnalysisSeriesOriginEnum.operator_: 'operator',
};
