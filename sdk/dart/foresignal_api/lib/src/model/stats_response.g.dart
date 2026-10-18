// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stats_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StatsResponseCWProxy {
  StatsResponse productId(String productId);

  StatsResponse frameKey(String? frameKey);

  StatsResponse validTime(DateTime? validTime);

  StatsResponse run(String? run);

  StatsResponse ageMinutes(num? ageMinutes);

  StatsResponse units(String? units);

  StatsResponse status(SampleStatus status);

  StatsResponse statusText(String? statusText);

  StatsResponse min(num? min);

  StatsResponse max(num? max);

  StatsResponse mean(num? mean);

  StatsResponse p95(num? p95);

  StatsResponse coveragePct(num? coveragePct);

  StatsResponse areaKm2(num? areaKm2);

  StatsResponse thresholds(List<StatsResponseThresholdsInner> thresholds);

  StatsResponse attribution(Attribution attribution);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsResponse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsResponse(...).copyWith(id: 12, name: "My name")
  /// ```
  StatsResponse call({
    String productId,
    String? frameKey,
    DateTime? validTime,
    String? run,
    num? ageMinutes,
    String? units,
    SampleStatus status,
    String? statusText,
    num? min,
    num? max,
    num? mean,
    num? p95,
    num? coveragePct,
    num? areaKm2,
    List<StatsResponseThresholdsInner> thresholds,
    Attribution attribution,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStatsResponse.copyWith(...)` or call `instanceOfStatsResponse.copyWith.fieldName(value)` for a single field.
class _$StatsResponseCWProxyImpl implements _$StatsResponseCWProxy {
  const _$StatsResponseCWProxyImpl(this._value);

  final StatsResponse _value;

  @override
  StatsResponse productId(String productId) => call(productId: productId);

  @override
  StatsResponse frameKey(String? frameKey) => call(frameKey: frameKey);

  @override
  StatsResponse validTime(DateTime? validTime) => call(validTime: validTime);

  @override
  StatsResponse run(String? run) => call(run: run);

  @override
  StatsResponse ageMinutes(num? ageMinutes) => call(ageMinutes: ageMinutes);

  @override
  StatsResponse units(String? units) => call(units: units);

  @override
  StatsResponse status(SampleStatus status) => call(status: status);

  @override
  StatsResponse statusText(String? statusText) => call(statusText: statusText);

  @override
  StatsResponse min(num? min) => call(min: min);

  @override
  StatsResponse max(num? max) => call(max: max);

  @override
  StatsResponse mean(num? mean) => call(mean: mean);

  @override
  StatsResponse p95(num? p95) => call(p95: p95);

  @override
  StatsResponse coveragePct(num? coveragePct) => call(coveragePct: coveragePct);

  @override
  StatsResponse areaKm2(num? areaKm2) => call(areaKm2: areaKm2);

  @override
  StatsResponse thresholds(List<StatsResponseThresholdsInner> thresholds) =>
      call(thresholds: thresholds);

  @override
  StatsResponse attribution(Attribution attribution) =>
      call(attribution: attribution);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsResponse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsResponse(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StatsResponse call({
    Object? productId = const $CopyWithPlaceholder(),
    Object? frameKey = const $CopyWithPlaceholder(),
    Object? validTime = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? ageMinutes = const $CopyWithPlaceholder(),
    Object? units = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? min = const $CopyWithPlaceholder(),
    Object? max = const $CopyWithPlaceholder(),
    Object? mean = const $CopyWithPlaceholder(),
    Object? p95 = const $CopyWithPlaceholder(),
    Object? coveragePct = const $CopyWithPlaceholder(),
    Object? areaKm2 = const $CopyWithPlaceholder(),
    Object? thresholds = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
  }) {
    return StatsResponse(
      productId: productId == const $CopyWithPlaceholder() || productId == null
          ? _value.productId
          // ignore: cast_nullable_to_non_nullable
          : productId as String,
      frameKey: frameKey == const $CopyWithPlaceholder()
          ? _value.frameKey
          // ignore: cast_nullable_to_non_nullable
          : frameKey as String?,
      validTime: validTime == const $CopyWithPlaceholder()
          ? _value.validTime
          // ignore: cast_nullable_to_non_nullable
          : validTime as DateTime?,
      run: run == const $CopyWithPlaceholder()
          ? _value.run
          // ignore: cast_nullable_to_non_nullable
          : run as String?,
      ageMinutes: ageMinutes == const $CopyWithPlaceholder()
          ? _value.ageMinutes
          // ignore: cast_nullable_to_non_nullable
          : ageMinutes as num?,
      units: units == const $CopyWithPlaceholder()
          ? _value.units
          // ignore: cast_nullable_to_non_nullable
          : units as String?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SampleStatus,
      statusText: statusText == const $CopyWithPlaceholder()
          ? _value.statusText
          // ignore: cast_nullable_to_non_nullable
          : statusText as String?,
      min: min == const $CopyWithPlaceholder()
          ? _value.min
          // ignore: cast_nullable_to_non_nullable
          : min as num?,
      max: max == const $CopyWithPlaceholder()
          ? _value.max
          // ignore: cast_nullable_to_non_nullable
          : max as num?,
      mean: mean == const $CopyWithPlaceholder()
          ? _value.mean
          // ignore: cast_nullable_to_non_nullable
          : mean as num?,
      p95: p95 == const $CopyWithPlaceholder()
          ? _value.p95
          // ignore: cast_nullable_to_non_nullable
          : p95 as num?,
      coveragePct: coveragePct == const $CopyWithPlaceholder()
          ? _value.coveragePct
          // ignore: cast_nullable_to_non_nullable
          : coveragePct as num?,
      areaKm2: areaKm2 == const $CopyWithPlaceholder()
          ? _value.areaKm2
          // ignore: cast_nullable_to_non_nullable
          : areaKm2 as num?,
      thresholds:
          thresholds == const $CopyWithPlaceholder() || thresholds == null
          ? _value.thresholds
          // ignore: cast_nullable_to_non_nullable
          : thresholds as List<StatsResponseThresholdsInner>,
      attribution:
          attribution == const $CopyWithPlaceholder() || attribution == null
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as Attribution,
    );
  }
}

extension $StatsResponseCopyWith on StatsResponse {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStatsResponse.copyWith(...)` or `instanceOfStatsResponse.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StatsResponseCWProxy get copyWith => _$StatsResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StatsResponse _$StatsResponseFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StatsResponse', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'productId',
          'status',
          'thresholds',
          'attribution',
        ],
      );
      final val = StatsResponse(
        productId: $checkedConvert('productId', (v) => v as String),
        frameKey: $checkedConvert('frameKey', (v) => v as String?),
        validTime: $checkedConvert(
          'validTime',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        run: $checkedConvert('run', (v) => v as String?),
        ageMinutes: $checkedConvert('ageMinutes', (v) => v as num?),
        units: $checkedConvert('units', (v) => v as String?),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$SampleStatusEnumMap, v),
        ),
        statusText: $checkedConvert('statusText', (v) => v as String?),
        min: $checkedConvert('min', (v) => v as num?),
        max: $checkedConvert('max', (v) => v as num?),
        mean: $checkedConvert('mean', (v) => v as num?),
        p95: $checkedConvert('p95', (v) => v as num?),
        coveragePct: $checkedConvert('coveragePct', (v) => v as num?),
        areaKm2: $checkedConvert('areaKm2', (v) => v as num?),
        thresholds: $checkedConvert(
          'thresholds',
          (v) => (v as List<dynamic>)
              .map(
                (e) => StatsResponseThresholdsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        attribution: $checkedConvert(
          'attribution',
          (v) => Attribution.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$StatsResponseToJson(StatsResponse instance) =>
    <String, dynamic>{
      'productId': instance.productId,
      'frameKey': ?instance.frameKey,
      'validTime': ?instance.validTime?.toIso8601String(),
      'run': ?instance.run,
      'ageMinutes': ?instance.ageMinutes,
      'units': ?instance.units,
      'status': _$SampleStatusEnumMap[instance.status]!,
      'statusText': ?instance.statusText,
      'min': ?instance.min,
      'max': ?instance.max,
      'mean': ?instance.mean,
      'p95': ?instance.p95,
      'coveragePct': ?instance.coveragePct,
      'areaKm2': ?instance.areaKm2,
      'thresholds': instance.thresholds.map((e) => e.toJson()).toList(),
      'attribution': instance.attribution.toJson(),
    };

const _$SampleStatusEnumMap = {
  SampleStatus.ok: 'ok',
  SampleStatus.noCoverage: 'no-coverage',
  SampleStatus.noData: 'no-data',
  SampleStatus.stale: 'stale',
  SampleStatus.unavailable: 'unavailable',
  SampleStatus.stalePosition: 'stale-position',
};
