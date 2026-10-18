// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_window_stats.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorWindowStatsCWProxy {
  PrecursorWindowStats window(TimeRange window);

  PrecursorWindowStats n(int n);

  PrecursorWindowStats hits(int? hits);

  PrecursorWindowStats hitRate(num? hitRate);

  PrecursorWindowStats hitRateInterval(StatInterval? hitRateInterval);

  PrecursorWindowStats baseRate(num? baseRate);

  PrecursorWindowStats lift(num? lift);

  PrecursorWindowStats correlation(num? correlation);

  PrecursorWindowStats bestLag(Object? bestLag);

  PrecursorWindowStats p(num? p);

  PrecursorWindowStats adjustedP(num? adjustedP);

  PrecursorWindowStats verdict(PrecursorWindowStatsVerdictEnum verdict);

  PrecursorWindowStats reverse(PrecursorWindowStatsReverse? reverse);

  PrecursorWindowStats lowDetectabilityExcluded(int? lowDetectabilityExcluded);

  PrecursorWindowStats coverageGapFraction(num? coverageGapFraction);

  PrecursorWindowStats blockLength(int? blockLength);

  PrecursorWindowStats reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorWindowStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorWindowStats(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorWindowStats call({
    TimeRange window,
    int n,
    int? hits,
    num? hitRate,
    StatInterval? hitRateInterval,
    num? baseRate,
    num? lift,
    num? correlation,
    Object? bestLag,
    num? p,
    num? adjustedP,
    PrecursorWindowStatsVerdictEnum verdict,
    PrecursorWindowStatsReverse? reverse,
    int? lowDetectabilityExcluded,
    num? coverageGapFraction,
    int? blockLength,
    String? reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorWindowStats.copyWith(...)` or call `instanceOfPrecursorWindowStats.copyWith.fieldName(value)` for a single field.
class _$PrecursorWindowStatsCWProxyImpl
    implements _$PrecursorWindowStatsCWProxy {
  const _$PrecursorWindowStatsCWProxyImpl(this._value);

  final PrecursorWindowStats _value;

  @override
  PrecursorWindowStats window(TimeRange window) => call(window: window);

  @override
  PrecursorWindowStats n(int n) => call(n: n);

  @override
  PrecursorWindowStats hits(int? hits) => call(hits: hits);

  @override
  PrecursorWindowStats hitRate(num? hitRate) => call(hitRate: hitRate);

  @override
  PrecursorWindowStats hitRateInterval(StatInterval? hitRateInterval) =>
      call(hitRateInterval: hitRateInterval);

  @override
  PrecursorWindowStats baseRate(num? baseRate) => call(baseRate: baseRate);

  @override
  PrecursorWindowStats lift(num? lift) => call(lift: lift);

  @override
  PrecursorWindowStats correlation(num? correlation) =>
      call(correlation: correlation);

  @override
  PrecursorWindowStats bestLag(Object? bestLag) => call(bestLag: bestLag);

  @override
  PrecursorWindowStats p(num? p) => call(p: p);

  @override
  PrecursorWindowStats adjustedP(num? adjustedP) => call(adjustedP: adjustedP);

  @override
  PrecursorWindowStats verdict(PrecursorWindowStatsVerdictEnum verdict) =>
      call(verdict: verdict);

  @override
  PrecursorWindowStats reverse(PrecursorWindowStatsReverse? reverse) =>
      call(reverse: reverse);

  @override
  PrecursorWindowStats lowDetectabilityExcluded(
    int? lowDetectabilityExcluded,
  ) => call(lowDetectabilityExcluded: lowDetectabilityExcluded);

  @override
  PrecursorWindowStats coverageGapFraction(num? coverageGapFraction) =>
      call(coverageGapFraction: coverageGapFraction);

  @override
  PrecursorWindowStats blockLength(int? blockLength) =>
      call(blockLength: blockLength);

  @override
  PrecursorWindowStats reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorWindowStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorWindowStats(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorWindowStats call({
    Object? window = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
    Object? hits = const $CopyWithPlaceholder(),
    Object? hitRate = const $CopyWithPlaceholder(),
    Object? hitRateInterval = const $CopyWithPlaceholder(),
    Object? baseRate = const $CopyWithPlaceholder(),
    Object? lift = const $CopyWithPlaceholder(),
    Object? correlation = const $CopyWithPlaceholder(),
    Object? bestLag = const $CopyWithPlaceholder(),
    Object? p = const $CopyWithPlaceholder(),
    Object? adjustedP = const $CopyWithPlaceholder(),
    Object? verdict = const $CopyWithPlaceholder(),
    Object? reverse = const $CopyWithPlaceholder(),
    Object? lowDetectabilityExcluded = const $CopyWithPlaceholder(),
    Object? coverageGapFraction = const $CopyWithPlaceholder(),
    Object? blockLength = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return PrecursorWindowStats(
      window: window == const $CopyWithPlaceholder() || window == null
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as TimeRange,
      n: n == const $CopyWithPlaceholder() || n == null
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int,
      hits: hits == const $CopyWithPlaceholder()
          ? _value.hits
          // ignore: cast_nullable_to_non_nullable
          : hits as int?,
      hitRate: hitRate == const $CopyWithPlaceholder()
          ? _value.hitRate
          // ignore: cast_nullable_to_non_nullable
          : hitRate as num?,
      hitRateInterval: hitRateInterval == const $CopyWithPlaceholder()
          ? _value.hitRateInterval
          // ignore: cast_nullable_to_non_nullable
          : hitRateInterval as StatInterval?,
      baseRate: baseRate == const $CopyWithPlaceholder()
          ? _value.baseRate
          // ignore: cast_nullable_to_non_nullable
          : baseRate as num?,
      lift: lift == const $CopyWithPlaceholder()
          ? _value.lift
          // ignore: cast_nullable_to_non_nullable
          : lift as num?,
      correlation: correlation == const $CopyWithPlaceholder()
          ? _value.correlation
          // ignore: cast_nullable_to_non_nullable
          : correlation as num?,
      bestLag: bestLag == const $CopyWithPlaceholder()
          ? _value.bestLag
          // ignore: cast_nullable_to_non_nullable
          : bestLag as Object?,
      p: p == const $CopyWithPlaceholder()
          ? _value.p
          // ignore: cast_nullable_to_non_nullable
          : p as num?,
      adjustedP: adjustedP == const $CopyWithPlaceholder()
          ? _value.adjustedP
          // ignore: cast_nullable_to_non_nullable
          : adjustedP as num?,
      verdict: verdict == const $CopyWithPlaceholder() || verdict == null
          ? _value.verdict
          // ignore: cast_nullable_to_non_nullable
          : verdict as PrecursorWindowStatsVerdictEnum,
      reverse: reverse == const $CopyWithPlaceholder()
          ? _value.reverse
          // ignore: cast_nullable_to_non_nullable
          : reverse as PrecursorWindowStatsReverse?,
      lowDetectabilityExcluded:
          lowDetectabilityExcluded == const $CopyWithPlaceholder()
          ? _value.lowDetectabilityExcluded
          // ignore: cast_nullable_to_non_nullable
          : lowDetectabilityExcluded as int?,
      coverageGapFraction: coverageGapFraction == const $CopyWithPlaceholder()
          ? _value.coverageGapFraction
          // ignore: cast_nullable_to_non_nullable
          : coverageGapFraction as num?,
      blockLength: blockLength == const $CopyWithPlaceholder()
          ? _value.blockLength
          // ignore: cast_nullable_to_non_nullable
          : blockLength as int?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $PrecursorWindowStatsCopyWith on PrecursorWindowStats {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorWindowStats.copyWith(...)` or `instanceOfPrecursorWindowStats.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorWindowStatsCWProxy get copyWith =>
      _$PrecursorWindowStatsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorWindowStats _$PrecursorWindowStatsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorWindowStats', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['window', 'n', 'verdict']);
  final val = PrecursorWindowStats(
    window: $checkedConvert(
      'window',
      (v) => TimeRange.fromJson(v as Map<String, dynamic>),
    ),
    n: $checkedConvert('n', (v) => (v as num).toInt()),
    hits: $checkedConvert('hits', (v) => (v as num?)?.toInt()),
    hitRate: $checkedConvert('hitRate', (v) => v as num?),
    hitRateInterval: $checkedConvert(
      'hitRateInterval',
      (v) =>
          v == null ? null : StatInterval.fromJson(v as Map<String, dynamic>),
    ),
    baseRate: $checkedConvert('baseRate', (v) => v as num?),
    lift: $checkedConvert('lift', (v) => v as num?),
    correlation: $checkedConvert('correlation', (v) => v as num?),
    bestLag: $checkedConvert('bestLag', (v) => v),
    p: $checkedConvert('p', (v) => v as num?),
    adjustedP: $checkedConvert('adjustedP', (v) => v as num?),
    verdict: $checkedConvert(
      'verdict',
      (v) => $enumDecode(_$PrecursorWindowStatsVerdictEnumEnumMap, v),
    ),
    reverse: $checkedConvert(
      'reverse',
      (v) => v == null
          ? null
          : PrecursorWindowStatsReverse.fromJson(v as Map<String, dynamic>),
    ),
    lowDetectabilityExcluded: $checkedConvert(
      'lowDetectabilityExcluded',
      (v) => (v as num?)?.toInt(),
    ),
    coverageGapFraction: $checkedConvert(
      'coverageGapFraction',
      (v) => v as num?,
    ),
    blockLength: $checkedConvert('blockLength', (v) => (v as num?)?.toInt()),
    reason: $checkedConvert('reason', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$PrecursorWindowStatsToJson(
  PrecursorWindowStats instance,
) => <String, dynamic>{
  'window': instance.window.toJson(),
  'n': instance.n,
  'hits': ?instance.hits,
  'hitRate': ?instance.hitRate,
  'hitRateInterval': ?instance.hitRateInterval?.toJson(),
  'baseRate': ?instance.baseRate,
  'lift': ?instance.lift,
  'correlation': ?instance.correlation,
  'bestLag': ?instance.bestLag,
  'p': ?instance.p,
  'adjustedP': ?instance.adjustedP,
  'verdict': _$PrecursorWindowStatsVerdictEnumEnumMap[instance.verdict]!,
  'reverse': ?instance.reverse?.toJson(),
  'lowDetectabilityExcluded': ?instance.lowDetectabilityExcluded,
  'coverageGapFraction': ?instance.coverageGapFraction,
  'blockLength': ?instance.blockLength,
  'reason': ?instance.reason,
};

const _$PrecursorWindowStatsVerdictEnumEnumMap = {
  PrecursorWindowStatsVerdictEnum.insufficient: 'insufficient',
  PrecursorWindowStatsVerdictEnum.noEvidenceOfPrecedence:
      'no-evidence-of-precedence',
  PrecursorWindowStatsVerdictEnum.weak: 'weak',
  PrecursorWindowStatsVerdictEnum.notable: 'notable',
  PrecursorWindowStatsVerdictEnum.validated: 'validated',
  PrecursorWindowStatsVerdictEnum.failed: 'failed',
};
