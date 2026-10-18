// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'assessment_bundle.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AssessmentBundleCWProxy {
  AssessmentBundle assessment(Assessment assessment);

  AssessmentBundle generatedAt(DateTime generatedAt);

  AssessmentBundle objects(List<AssessmentBundleObjectsInner> objects);

  AssessmentBundle manifests(List<AnalysisManifest>? manifests);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentBundle(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentBundle(...).copyWith(id: 12, name: "My name")
  /// ```
  AssessmentBundle call({
    Assessment assessment,
    DateTime generatedAt,
    List<AssessmentBundleObjectsInner> objects,
    List<AnalysisManifest>? manifests,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAssessmentBundle.copyWith(...)` or call `instanceOfAssessmentBundle.copyWith.fieldName(value)` for a single field.
class _$AssessmentBundleCWProxyImpl implements _$AssessmentBundleCWProxy {
  const _$AssessmentBundleCWProxyImpl(this._value);

  final AssessmentBundle _value;

  @override
  AssessmentBundle assessment(Assessment assessment) =>
      call(assessment: assessment);

  @override
  AssessmentBundle generatedAt(DateTime generatedAt) =>
      call(generatedAt: generatedAt);

  @override
  AssessmentBundle objects(List<AssessmentBundleObjectsInner> objects) =>
      call(objects: objects);

  @override
  AssessmentBundle manifests(List<AnalysisManifest>? manifests) =>
      call(manifests: manifests);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentBundle(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentBundle(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AssessmentBundle call({
    Object? assessment = const $CopyWithPlaceholder(),
    Object? generatedAt = const $CopyWithPlaceholder(),
    Object? objects = const $CopyWithPlaceholder(),
    Object? manifests = const $CopyWithPlaceholder(),
  }) {
    return AssessmentBundle(
      assessment:
          assessment == const $CopyWithPlaceholder() || assessment == null
          ? _value.assessment
          // ignore: cast_nullable_to_non_nullable
          : assessment as Assessment,
      generatedAt:
          generatedAt == const $CopyWithPlaceholder() || generatedAt == null
          ? _value.generatedAt
          // ignore: cast_nullable_to_non_nullable
          : generatedAt as DateTime,
      objects: objects == const $CopyWithPlaceholder() || objects == null
          ? _value.objects
          // ignore: cast_nullable_to_non_nullable
          : objects as List<AssessmentBundleObjectsInner>,
      manifests: manifests == const $CopyWithPlaceholder()
          ? _value.manifests
          // ignore: cast_nullable_to_non_nullable
          : manifests as List<AnalysisManifest>?,
    );
  }
}

extension $AssessmentBundleCopyWith on AssessmentBundle {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAssessmentBundle.copyWith(...)` or `instanceOfAssessmentBundle.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AssessmentBundleCWProxy get copyWith => _$AssessmentBundleCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AssessmentBundle _$AssessmentBundleFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AssessmentBundle', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['assessment', 'generatedAt', 'objects'],
      );
      final val = AssessmentBundle(
        assessment: $checkedConvert(
          'assessment',
          (v) => Assessment.fromJson(v as Map<String, dynamic>),
        ),
        generatedAt: $checkedConvert(
          'generatedAt',
          (v) => DateTime.parse(v as String),
        ),
        objects: $checkedConvert(
          'objects',
          (v) => (v as List<dynamic>)
              .map(
                (e) => AssessmentBundleObjectsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        manifests: $checkedConvert(
          'manifests',
          (v) => (v as List<dynamic>?)
              ?.map((e) => AnalysisManifest.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AssessmentBundleToJson(AssessmentBundle instance) =>
    <String, dynamic>{
      'assessment': instance.assessment.toJson(),
      'generatedAt': instance.generatedAt.toIso8601String(),
      'objects': instance.objects.map((e) => e.toJson()).toList(),
      'manifests': ?instance.manifests?.map((e) => e.toJson()).toList(),
    };
