// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analysis_series_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalysisSeriesInputCWProxy {
  AnalysisSeriesInput ref(String ref);

  AnalysisSeriesInput enabled(bool? enabled);

  AnalysisSeriesInput detectors(
    List<AnalysisSeriesInputDetectorsEnum>? detectors,
  );

  AnalysisSeriesInput frequency(AnalysisSeriesInputFrequencyEnum? frequency);

  AnalysisSeriesInput family(String? family);

  AnalysisSeriesInput regionCode(Object? regionCode);

  AnalysisSeriesInput watchPairs(List<WatchPair>? watchPairs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisSeriesInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisSeriesInput(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalysisSeriesInput call({
    String ref,
    bool? enabled,
    List<AnalysisSeriesInputDetectorsEnum>? detectors,
    AnalysisSeriesInputFrequencyEnum? frequency,
    String? family,
    Object? regionCode,
    List<WatchPair>? watchPairs,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalysisSeriesInput.copyWith(...)` or call `instanceOfAnalysisSeriesInput.copyWith.fieldName(value)` for a single field.
class _$AnalysisSeriesInputCWProxyImpl implements _$AnalysisSeriesInputCWProxy {
  const _$AnalysisSeriesInputCWProxyImpl(this._value);

  final AnalysisSeriesInput _value;

  @override
  AnalysisSeriesInput ref(String ref) => call(ref: ref);

  @override
  AnalysisSeriesInput enabled(bool? enabled) => call(enabled: enabled);

  @override
  AnalysisSeriesInput detectors(
    List<AnalysisSeriesInputDetectorsEnum>? detectors,
  ) => call(detectors: detectors);

  @override
  AnalysisSeriesInput frequency(AnalysisSeriesInputFrequencyEnum? frequency) =>
      call(frequency: frequency);

  @override
  AnalysisSeriesInput family(String? family) => call(family: family);

  @override
  AnalysisSeriesInput regionCode(Object? regionCode) =>
      call(regionCode: regionCode);

  @override
  AnalysisSeriesInput watchPairs(List<WatchPair>? watchPairs) =>
      call(watchPairs: watchPairs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisSeriesInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisSeriesInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalysisSeriesInput call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? detectors = const $CopyWithPlaceholder(),
    Object? frequency = const $CopyWithPlaceholder(),
    Object? family = const $CopyWithPlaceholder(),
    Object? regionCode = const $CopyWithPlaceholder(),
    Object? watchPairs = const $CopyWithPlaceholder(),
  }) {
    return AnalysisSeriesInput(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      detectors: detectors == const $CopyWithPlaceholder()
          ? _value.detectors
          // ignore: cast_nullable_to_non_nullable
          : detectors as List<AnalysisSeriesInputDetectorsEnum>?,
      frequency: frequency == const $CopyWithPlaceholder()
          ? _value.frequency
          // ignore: cast_nullable_to_non_nullable
          : frequency as AnalysisSeriesInputFrequencyEnum?,
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
    );
  }
}

extension $AnalysisSeriesInputCopyWith on AnalysisSeriesInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalysisSeriesInput.copyWith(...)` or `instanceOfAnalysisSeriesInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalysisSeriesInputCWProxy get copyWith =>
      _$AnalysisSeriesInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalysisSeriesInput _$AnalysisSeriesInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalysisSeriesInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['ref']);
      final val = AnalysisSeriesInput(
        ref: $checkedConvert('ref', (v) => v as String),
        enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
        detectors: $checkedConvert(
          'detectors',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) =>
                    $enumDecode(_$AnalysisSeriesInputDetectorsEnumEnumMap, e),
              )
              .toList(),
        ),
        frequency: $checkedConvert(
          'frequency',
          (v) =>
              $enumDecodeNullable(_$AnalysisSeriesInputFrequencyEnumEnumMap, v),
        ),
        family: $checkedConvert('family', (v) => v as String?),
        regionCode: $checkedConvert('regionCode', (v) => v),
        watchPairs: $checkedConvert(
          'watchPairs',
          (v) => (v as List<dynamic>?)
              ?.map((e) => WatchPair.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AnalysisSeriesInputToJson(
  AnalysisSeriesInput instance,
) => <String, dynamic>{
  'ref': instance.ref,
  'enabled': ?instance.enabled,
  'detectors': ?instance.detectors
      ?.map((e) => _$AnalysisSeriesInputDetectorsEnumEnumMap[e]!)
      .toList(),
  'frequency': ?_$AnalysisSeriesInputFrequencyEnumEnumMap[instance.frequency],
  'family': ?instance.family,
  'regionCode': ?instance.regionCode,
  'watchPairs': ?instance.watchPairs?.map((e) => e.toJson()).toList(),
};

const _$AnalysisSeriesInputDetectorsEnumEnumMap = {
  AnalysisSeriesInputDetectorsEnum.changepointCusum: 'changepoint-cusum',
  AnalysisSeriesInputDetectorsEnum.changepointBocpd: 'changepoint-bocpd',
  AnalysisSeriesInputDetectorsEnum.forecast: 'forecast',
};

const _$AnalysisSeriesInputFrequencyEnumEnumMap = {
  AnalysisSeriesInputFrequencyEnum.hour: 'hour',
  AnalysisSeriesInputFrequencyEnum.day: 'day',
  AnalysisSeriesInputFrequencyEnum.week: 'week',
};
