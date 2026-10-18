// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'observability_estimate.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ObservabilityEstimateCWProxy {
  ObservabilityEstimate sourceId(String sourceId);

  ObservabilityEstimate family(String family);

  ObservabilityEstimate regionCode(String? regionCode);

  ObservabilityEstimate cell(String? cell);

  ObservabilityEstimate band(String? band);

  ObservabilityEstimate periodStart(DateTime periodStart);

  ObservabilityEstimate periodEnd(DateTime periodEnd);

  ObservabilityEstimate pDetect(num pDetect);

  ObservabilityEstimate lo(num lo);

  ObservabilityEstimate hi(num hi);

  ObservabilityEstimate n(int n);

  ObservabilityEstimate method(ObservabilityEstimateMethodEnum method);

  ObservabilityEstimate confidence(ConfidenceLevel? confidence);

  ObservabilityEstimate computedAt(DateTime computedAt);

  ObservabilityEstimate manifestId(String? manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObservabilityEstimate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObservabilityEstimate(...).copyWith(id: 12, name: "My name")
  /// ```
  ObservabilityEstimate call({
    String sourceId,
    String family,
    String? regionCode,
    String? cell,
    String? band,
    DateTime periodStart,
    DateTime periodEnd,
    num pDetect,
    num lo,
    num hi,
    int n,
    ObservabilityEstimateMethodEnum method,
    ConfidenceLevel? confidence,
    DateTime computedAt,
    String? manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfObservabilityEstimate.copyWith(...)` or call `instanceOfObservabilityEstimate.copyWith.fieldName(value)` for a single field.
class _$ObservabilityEstimateCWProxyImpl
    implements _$ObservabilityEstimateCWProxy {
  const _$ObservabilityEstimateCWProxyImpl(this._value);

  final ObservabilityEstimate _value;

  @override
  ObservabilityEstimate sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  ObservabilityEstimate family(String family) => call(family: family);

  @override
  ObservabilityEstimate regionCode(String? regionCode) =>
      call(regionCode: regionCode);

  @override
  ObservabilityEstimate cell(String? cell) => call(cell: cell);

  @override
  ObservabilityEstimate band(String? band) => call(band: band);

  @override
  ObservabilityEstimate periodStart(DateTime periodStart) =>
      call(periodStart: periodStart);

  @override
  ObservabilityEstimate periodEnd(DateTime periodEnd) =>
      call(periodEnd: periodEnd);

  @override
  ObservabilityEstimate pDetect(num pDetect) => call(pDetect: pDetect);

  @override
  ObservabilityEstimate lo(num lo) => call(lo: lo);

  @override
  ObservabilityEstimate hi(num hi) => call(hi: hi);

  @override
  ObservabilityEstimate n(int n) => call(n: n);

  @override
  ObservabilityEstimate method(ObservabilityEstimateMethodEnum method) =>
      call(method: method);

  @override
  ObservabilityEstimate confidence(ConfidenceLevel? confidence) =>
      call(confidence: confidence);

  @override
  ObservabilityEstimate computedAt(DateTime computedAt) =>
      call(computedAt: computedAt);

  @override
  ObservabilityEstimate manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObservabilityEstimate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObservabilityEstimate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ObservabilityEstimate call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? family = const $CopyWithPlaceholder(),
    Object? regionCode = const $CopyWithPlaceholder(),
    Object? cell = const $CopyWithPlaceholder(),
    Object? band = const $CopyWithPlaceholder(),
    Object? periodStart = const $CopyWithPlaceholder(),
    Object? periodEnd = const $CopyWithPlaceholder(),
    Object? pDetect = const $CopyWithPlaceholder(),
    Object? lo = const $CopyWithPlaceholder(),
    Object? hi = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return ObservabilityEstimate(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      family: family == const $CopyWithPlaceholder() || family == null
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String,
      regionCode: regionCode == const $CopyWithPlaceholder()
          ? _value.regionCode
          // ignore: cast_nullable_to_non_nullable
          : regionCode as String?,
      cell: cell == const $CopyWithPlaceholder()
          ? _value.cell
          // ignore: cast_nullable_to_non_nullable
          : cell as String?,
      band: band == const $CopyWithPlaceholder()
          ? _value.band
          // ignore: cast_nullable_to_non_nullable
          : band as String?,
      periodStart:
          periodStart == const $CopyWithPlaceholder() || periodStart == null
          ? _value.periodStart
          // ignore: cast_nullable_to_non_nullable
          : periodStart as DateTime,
      periodEnd: periodEnd == const $CopyWithPlaceholder() || periodEnd == null
          ? _value.periodEnd
          // ignore: cast_nullable_to_non_nullable
          : periodEnd as DateTime,
      pDetect: pDetect == const $CopyWithPlaceholder() || pDetect == null
          ? _value.pDetect
          // ignore: cast_nullable_to_non_nullable
          : pDetect as num,
      lo: lo == const $CopyWithPlaceholder() || lo == null
          ? _value.lo
          // ignore: cast_nullable_to_non_nullable
          : lo as num,
      hi: hi == const $CopyWithPlaceholder() || hi == null
          ? _value.hi
          // ignore: cast_nullable_to_non_nullable
          : hi as num,
      n: n == const $CopyWithPlaceholder() || n == null
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as ObservabilityEstimateMethodEnum,
      confidence: confidence == const $CopyWithPlaceholder()
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as ConfidenceLevel?,
      computedAt:
          computedAt == const $CopyWithPlaceholder() || computedAt == null
          ? _value.computedAt
          // ignore: cast_nullable_to_non_nullable
          : computedAt as DateTime,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
    );
  }
}

extension $ObservabilityEstimateCopyWith on ObservabilityEstimate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfObservabilityEstimate.copyWith(...)` or `instanceOfObservabilityEstimate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ObservabilityEstimateCWProxy get copyWith =>
      _$ObservabilityEstimateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ObservabilityEstimate _$ObservabilityEstimateFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ObservabilityEstimate', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'sourceId',
      'family',
      'periodStart',
      'periodEnd',
      'pDetect',
      'lo',
      'hi',
      'n',
      'method',
      'computedAt',
    ],
  );
  final val = ObservabilityEstimate(
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    family: $checkedConvert('family', (v) => v as String),
    regionCode: $checkedConvert('regionCode', (v) => v as String?),
    cell: $checkedConvert('cell', (v) => v as String?),
    band: $checkedConvert('band', (v) => v as String?),
    periodStart: $checkedConvert(
      'periodStart',
      (v) => DateTime.parse(v as String),
    ),
    periodEnd: $checkedConvert('periodEnd', (v) => DateTime.parse(v as String)),
    pDetect: $checkedConvert('pDetect', (v) => v as num),
    lo: $checkedConvert('lo', (v) => v as num),
    hi: $checkedConvert('hi', (v) => v as num),
    n: $checkedConvert('n', (v) => (v as num).toInt()),
    method: $checkedConvert(
      'method',
      (v) => $enumDecode(_$ObservabilityEstimateMethodEnumEnumMap, v),
    ),
    confidence: $checkedConvert(
      'confidence',
      (v) => $enumDecodeNullable(_$ConfidenceLevelEnumMap, v),
    ),
    computedAt: $checkedConvert(
      'computedAt',
      (v) => DateTime.parse(v as String),
    ),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ObservabilityEstimateToJson(
  ObservabilityEstimate instance,
) => <String, dynamic>{
  'sourceId': instance.sourceId,
  'family': instance.family,
  'regionCode': ?instance.regionCode,
  'cell': ?instance.cell,
  'band': ?instance.band,
  'periodStart': instance.periodStart.toIso8601String(),
  'periodEnd': instance.periodEnd.toIso8601String(),
  'pDetect': instance.pDetect,
  'lo': instance.lo,
  'hi': instance.hi,
  'n': instance.n,
  'method': _$ObservabilityEstimateMethodEnumEnumMap[instance.method]!,
  'confidence': ?_$ConfidenceLevelEnumMap[instance.confidence],
  'computedAt': instance.computedAt.toIso8601String(),
  'manifestId': ?instance.manifestId,
};

const _$ObservabilityEstimateMethodEnumEnumMap = {
  ObservabilityEstimateMethodEnum.captureRecapture: 'capture-recapture',
  ObservabilityEstimateMethodEnum.presenceProxy: 'presence-proxy',
  ObservabilityEstimateMethodEnum.receiverRange: 'receiver-range',
  ObservabilityEstimateMethodEnum.declared: 'declared',
  ObservabilityEstimateMethodEnum.platformVolume: 'platform-volume',
};

const _$ConfidenceLevelEnumMap = {
  ConfidenceLevel.low: 'low',
  ConfidenceLevel.moderate: 'moderate',
  ConfidenceLevel.high: 'high',
};
