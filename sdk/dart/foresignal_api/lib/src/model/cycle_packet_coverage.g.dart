// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cycle_packet_coverage.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CyclePacketCoverageCWProxy {
  CyclePacketCoverage failingSources(List<String>? failingSources);

  CyclePacketCoverage degradedSources(List<String>? degradedSources);

  CyclePacketCoverage lowReportingDensityRegions(
    List<String>? lowReportingDensityRegions,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CyclePacketCoverage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CyclePacketCoverage(...).copyWith(id: 12, name: "My name")
  /// ```
  CyclePacketCoverage call({
    List<String>? failingSources,
    List<String>? degradedSources,
    List<String>? lowReportingDensityRegions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCyclePacketCoverage.copyWith(...)` or call `instanceOfCyclePacketCoverage.copyWith.fieldName(value)` for a single field.
class _$CyclePacketCoverageCWProxyImpl implements _$CyclePacketCoverageCWProxy {
  const _$CyclePacketCoverageCWProxyImpl(this._value);

  final CyclePacketCoverage _value;

  @override
  CyclePacketCoverage failingSources(List<String>? failingSources) =>
      call(failingSources: failingSources);

  @override
  CyclePacketCoverage degradedSources(List<String>? degradedSources) =>
      call(degradedSources: degradedSources);

  @override
  CyclePacketCoverage lowReportingDensityRegions(
    List<String>? lowReportingDensityRegions,
  ) => call(lowReportingDensityRegions: lowReportingDensityRegions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CyclePacketCoverage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CyclePacketCoverage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CyclePacketCoverage call({
    Object? failingSources = const $CopyWithPlaceholder(),
    Object? degradedSources = const $CopyWithPlaceholder(),
    Object? lowReportingDensityRegions = const $CopyWithPlaceholder(),
  }) {
    return CyclePacketCoverage(
      failingSources: failingSources == const $CopyWithPlaceholder()
          ? _value.failingSources
          // ignore: cast_nullable_to_non_nullable
          : failingSources as List<String>?,
      degradedSources: degradedSources == const $CopyWithPlaceholder()
          ? _value.degradedSources
          // ignore: cast_nullable_to_non_nullable
          : degradedSources as List<String>?,
      lowReportingDensityRegions:
          lowReportingDensityRegions == const $CopyWithPlaceholder()
          ? _value.lowReportingDensityRegions
          // ignore: cast_nullable_to_non_nullable
          : lowReportingDensityRegions as List<String>?,
    );
  }
}

extension $CyclePacketCoverageCopyWith on CyclePacketCoverage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCyclePacketCoverage.copyWith(...)` or `instanceOfCyclePacketCoverage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CyclePacketCoverageCWProxy get copyWith =>
      _$CyclePacketCoverageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CyclePacketCoverage _$CyclePacketCoverageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CyclePacketCoverage', json, ($checkedConvert) {
      final val = CyclePacketCoverage(
        failingSources: $checkedConvert(
          'failingSources',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        degradedSources: $checkedConvert(
          'degradedSources',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        lowReportingDensityRegions: $checkedConvert(
          'lowReportingDensityRegions',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CyclePacketCoverageToJson(
  CyclePacketCoverage instance,
) => <String, dynamic>{
  'failingSources': ?instance.failingSources,
  'degradedSources': ?instance.degradedSources,
  'lowReportingDensityRegions': ?instance.lowReportingDensityRegions,
};
