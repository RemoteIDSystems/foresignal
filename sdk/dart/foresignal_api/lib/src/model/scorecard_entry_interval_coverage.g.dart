// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'scorecard_entry_interval_coverage.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ScorecardEntryIntervalCoverageCWProxy {
  ScorecardEntryIntervalCoverage p05p95(num? p05p95);

  ScorecardEntryIntervalCoverage p25p75(num? p25p75);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ScorecardEntryIntervalCoverage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ScorecardEntryIntervalCoverage(...).copyWith(id: 12, name: "My name")
  /// ```
  ScorecardEntryIntervalCoverage call({num? p05p95, num? p25p75});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfScorecardEntryIntervalCoverage.copyWith(...)` or call `instanceOfScorecardEntryIntervalCoverage.copyWith.fieldName(value)` for a single field.
class _$ScorecardEntryIntervalCoverageCWProxyImpl
    implements _$ScorecardEntryIntervalCoverageCWProxy {
  const _$ScorecardEntryIntervalCoverageCWProxyImpl(this._value);

  final ScorecardEntryIntervalCoverage _value;

  @override
  ScorecardEntryIntervalCoverage p05p95(num? p05p95) => call(p05p95: p05p95);

  @override
  ScorecardEntryIntervalCoverage p25p75(num? p25p75) => call(p25p75: p25p75);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ScorecardEntryIntervalCoverage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ScorecardEntryIntervalCoverage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ScorecardEntryIntervalCoverage call({
    Object? p05p95 = const $CopyWithPlaceholder(),
    Object? p25p75 = const $CopyWithPlaceholder(),
  }) {
    return ScorecardEntryIntervalCoverage(
      p05p95: p05p95 == const $CopyWithPlaceholder()
          ? _value.p05p95
          // ignore: cast_nullable_to_non_nullable
          : p05p95 as num?,
      p25p75: p25p75 == const $CopyWithPlaceholder()
          ? _value.p25p75
          // ignore: cast_nullable_to_non_nullable
          : p25p75 as num?,
    );
  }
}

extension $ScorecardEntryIntervalCoverageCopyWith
    on ScorecardEntryIntervalCoverage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfScorecardEntryIntervalCoverage.copyWith(...)` or `instanceOfScorecardEntryIntervalCoverage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ScorecardEntryIntervalCoverageCWProxy get copyWith =>
      _$ScorecardEntryIntervalCoverageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ScorecardEntryIntervalCoverage _$ScorecardEntryIntervalCoverageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ScorecardEntryIntervalCoverage', json, ($checkedConvert) {
  final val = ScorecardEntryIntervalCoverage(
    p05p95: $checkedConvert('p05p95', (v) => v as num?),
    p25p75: $checkedConvert('p25p75', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$ScorecardEntryIntervalCoverageToJson(
  ScorecardEntryIntervalCoverage instance,
) => <String, dynamic>{'p05p95': ?instance.p05p95, 'p25p75': ?instance.p25p75};
