// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'assessment_coverage.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AssessmentCoverageCWProxy {
  AssessmentCoverage situations(
    List<AssessmentCoverageSituationsInner>? situations,
  );

  AssessmentCoverage blindspotStoryIds(List<String>? blindspotStoryIds);

  AssessmentCoverage divergenceFindingIds(List<String>? divergenceFindingIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentCoverage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentCoverage(...).copyWith(id: 12, name: "My name")
  /// ```
  AssessmentCoverage call({
    List<AssessmentCoverageSituationsInner>? situations,
    List<String>? blindspotStoryIds,
    List<String>? divergenceFindingIds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAssessmentCoverage.copyWith(...)` or call `instanceOfAssessmentCoverage.copyWith.fieldName(value)` for a single field.
class _$AssessmentCoverageCWProxyImpl implements _$AssessmentCoverageCWProxy {
  const _$AssessmentCoverageCWProxyImpl(this._value);

  final AssessmentCoverage _value;

  @override
  AssessmentCoverage situations(
    List<AssessmentCoverageSituationsInner>? situations,
  ) => call(situations: situations);

  @override
  AssessmentCoverage blindspotStoryIds(List<String>? blindspotStoryIds) =>
      call(blindspotStoryIds: blindspotStoryIds);

  @override
  AssessmentCoverage divergenceFindingIds(List<String>? divergenceFindingIds) =>
      call(divergenceFindingIds: divergenceFindingIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentCoverage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentCoverage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AssessmentCoverage call({
    Object? situations = const $CopyWithPlaceholder(),
    Object? blindspotStoryIds = const $CopyWithPlaceholder(),
    Object? divergenceFindingIds = const $CopyWithPlaceholder(),
  }) {
    return AssessmentCoverage(
      situations: situations == const $CopyWithPlaceholder()
          ? _value.situations
          // ignore: cast_nullable_to_non_nullable
          : situations as List<AssessmentCoverageSituationsInner>?,
      blindspotStoryIds: blindspotStoryIds == const $CopyWithPlaceholder()
          ? _value.blindspotStoryIds
          // ignore: cast_nullable_to_non_nullable
          : blindspotStoryIds as List<String>?,
      divergenceFindingIds: divergenceFindingIds == const $CopyWithPlaceholder()
          ? _value.divergenceFindingIds
          // ignore: cast_nullable_to_non_nullable
          : divergenceFindingIds as List<String>?,
    );
  }
}

extension $AssessmentCoverageCopyWith on AssessmentCoverage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAssessmentCoverage.copyWith(...)` or `instanceOfAssessmentCoverage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AssessmentCoverageCWProxy get copyWith =>
      _$AssessmentCoverageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AssessmentCoverage _$AssessmentCoverageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AssessmentCoverage', json, ($checkedConvert) {
      final val = AssessmentCoverage(
        situations: $checkedConvert(
          'situations',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => AssessmentCoverageSituationsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        blindspotStoryIds: $checkedConvert(
          'blindspotStoryIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        divergenceFindingIds: $checkedConvert(
          'divergenceFindingIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AssessmentCoverageToJson(AssessmentCoverage instance) =>
    <String, dynamic>{
      'situations': ?instance.situations?.map((e) => e.toJson()).toList(),
      'blindspotStoryIds': ?instance.blindspotStoryIds,
      'divergenceFindingIds': ?instance.divergenceFindingIds,
    };
