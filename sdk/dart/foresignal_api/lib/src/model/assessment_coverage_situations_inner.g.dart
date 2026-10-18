// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'assessment_coverage_situations_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AssessmentCoverageSituationsInnerCWProxy {
  AssessmentCoverageSituationsInner situationId(String situationId);

  AssessmentCoverageSituationsInner coverage(CoverageStats coverage);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentCoverageSituationsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentCoverageSituationsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  AssessmentCoverageSituationsInner call({
    String situationId,
    CoverageStats coverage,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAssessmentCoverageSituationsInner.copyWith(...)` or call `instanceOfAssessmentCoverageSituationsInner.copyWith.fieldName(value)` for a single field.
class _$AssessmentCoverageSituationsInnerCWProxyImpl
    implements _$AssessmentCoverageSituationsInnerCWProxy {
  const _$AssessmentCoverageSituationsInnerCWProxyImpl(this._value);

  final AssessmentCoverageSituationsInner _value;

  @override
  AssessmentCoverageSituationsInner situationId(String situationId) =>
      call(situationId: situationId);

  @override
  AssessmentCoverageSituationsInner coverage(CoverageStats coverage) =>
      call(coverage: coverage);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentCoverageSituationsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentCoverageSituationsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AssessmentCoverageSituationsInner call({
    Object? situationId = const $CopyWithPlaceholder(),
    Object? coverage = const $CopyWithPlaceholder(),
  }) {
    return AssessmentCoverageSituationsInner(
      situationId:
          situationId == const $CopyWithPlaceholder() || situationId == null
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String,
      coverage: coverage == const $CopyWithPlaceholder() || coverage == null
          ? _value.coverage
          // ignore: cast_nullable_to_non_nullable
          : coverage as CoverageStats,
    );
  }
}

extension $AssessmentCoverageSituationsInnerCopyWith
    on AssessmentCoverageSituationsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAssessmentCoverageSituationsInner.copyWith(...)` or `instanceOfAssessmentCoverageSituationsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AssessmentCoverageSituationsInnerCWProxy get copyWith =>
      _$AssessmentCoverageSituationsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AssessmentCoverageSituationsInner _$AssessmentCoverageSituationsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AssessmentCoverageSituationsInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['situationId', 'coverage']);
  final val = AssessmentCoverageSituationsInner(
    situationId: $checkedConvert('situationId', (v) => v as String),
    coverage: $checkedConvert(
      'coverage',
      (v) => CoverageStats.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$AssessmentCoverageSituationsInnerToJson(
  AssessmentCoverageSituationsInner instance,
) => <String, dynamic>{
  'situationId': instance.situationId,
  'coverage': instance.coverage.toJson(),
};
