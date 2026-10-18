// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'lead_time_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LeadTimeSummaryCWProxy {
  LeadTimeSummary n(int n);

  LeadTimeSummary median(num? median);

  LeadTimeSummary p25(num? p25);

  LeadTimeSummary p75(num? p75);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LeadTimeSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LeadTimeSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  LeadTimeSummary call({int n, num? median, num? p25, num? p75});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLeadTimeSummary.copyWith(...)` or call `instanceOfLeadTimeSummary.copyWith.fieldName(value)` for a single field.
class _$LeadTimeSummaryCWProxyImpl implements _$LeadTimeSummaryCWProxy {
  const _$LeadTimeSummaryCWProxyImpl(this._value);

  final LeadTimeSummary _value;

  @override
  LeadTimeSummary n(int n) => call(n: n);

  @override
  LeadTimeSummary median(num? median) => call(median: median);

  @override
  LeadTimeSummary p25(num? p25) => call(p25: p25);

  @override
  LeadTimeSummary p75(num? p75) => call(p75: p75);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LeadTimeSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LeadTimeSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LeadTimeSummary call({
    Object? n = const $CopyWithPlaceholder(),
    Object? median = const $CopyWithPlaceholder(),
    Object? p25 = const $CopyWithPlaceholder(),
    Object? p75 = const $CopyWithPlaceholder(),
  }) {
    return LeadTimeSummary(
      n: n == const $CopyWithPlaceholder() || n == null
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int,
      median: median == const $CopyWithPlaceholder()
          ? _value.median
          // ignore: cast_nullable_to_non_nullable
          : median as num?,
      p25: p25 == const $CopyWithPlaceholder()
          ? _value.p25
          // ignore: cast_nullable_to_non_nullable
          : p25 as num?,
      p75: p75 == const $CopyWithPlaceholder()
          ? _value.p75
          // ignore: cast_nullable_to_non_nullable
          : p75 as num?,
    );
  }
}

extension $LeadTimeSummaryCopyWith on LeadTimeSummary {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLeadTimeSummary.copyWith(...)` or `instanceOfLeadTimeSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LeadTimeSummaryCWProxy get copyWith => _$LeadTimeSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LeadTimeSummary _$LeadTimeSummaryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LeadTimeSummary', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['n']);
      final val = LeadTimeSummary(
        n: $checkedConvert('n', (v) => (v as num).toInt()),
        median: $checkedConvert('median', (v) => v as num?),
        p25: $checkedConvert('p25', (v) => v as num?),
        p75: $checkedConvert('p75', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$LeadTimeSummaryToJson(LeadTimeSummary instance) =>
    <String, dynamic>{
      'n': instance.n,
      'median': ?instance.median,
      'p25': ?instance.p25,
      'p75': ?instance.p75,
    };
