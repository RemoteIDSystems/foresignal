// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'infra_impact_result_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InfraImpactResultSummaryCWProxy {
  InfraImpactResultSummary byType(Map<String, Map<String, int>>? byType);

  InfraImpactResultSummary byLabel(Map<String, int>? byLabel);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraImpactResultSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraImpactResultSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  InfraImpactResultSummary call({
    Map<String, Map<String, int>>? byType,
    Map<String, int>? byLabel,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInfraImpactResultSummary.copyWith(...)` or call `instanceOfInfraImpactResultSummary.copyWith.fieldName(value)` for a single field.
class _$InfraImpactResultSummaryCWProxyImpl
    implements _$InfraImpactResultSummaryCWProxy {
  const _$InfraImpactResultSummaryCWProxyImpl(this._value);

  final InfraImpactResultSummary _value;

  @override
  InfraImpactResultSummary byType(Map<String, Map<String, int>>? byType) =>
      call(byType: byType);

  @override
  InfraImpactResultSummary byLabel(Map<String, int>? byLabel) =>
      call(byLabel: byLabel);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraImpactResultSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraImpactResultSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InfraImpactResultSummary call({
    Object? byType = const $CopyWithPlaceholder(),
    Object? byLabel = const $CopyWithPlaceholder(),
  }) {
    return InfraImpactResultSummary(
      byType: byType == const $CopyWithPlaceholder()
          ? _value.byType
          // ignore: cast_nullable_to_non_nullable
          : byType as Map<String, Map<String, int>>?,
      byLabel: byLabel == const $CopyWithPlaceholder()
          ? _value.byLabel
          // ignore: cast_nullable_to_non_nullable
          : byLabel as Map<String, int>?,
    );
  }
}

extension $InfraImpactResultSummaryCopyWith on InfraImpactResultSummary {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInfraImpactResultSummary.copyWith(...)` or `instanceOfInfraImpactResultSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InfraImpactResultSummaryCWProxy get copyWith =>
      _$InfraImpactResultSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InfraImpactResultSummary _$InfraImpactResultSummaryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('InfraImpactResultSummary', json, ($checkedConvert) {
  final val = InfraImpactResultSummary(
    byType: $checkedConvert(
      'byType',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, Map<String, int>.from(e as Map)),
      ),
    ),
    byLabel: $checkedConvert(
      'byLabel',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, (e as num).toInt()),
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$InfraImpactResultSummaryToJson(
  InfraImpactResultSummary instance,
) => <String, dynamic>{
  'byType': ?instance.byType,
  'byLabel': ?instance.byLabel,
};
