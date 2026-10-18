// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'annotation_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnnotationPatchCWProxy {
  AnnotationPatch verdict(AnnotationPatchVerdictEnum? verdict);

  AnnotationPatch duplicateOfRef(String? duplicateOfRef);

  AnnotationPatch body(String? body);

  AnnotationPatch tags(List<String>? tags);

  AnnotationPatch private(bool? private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnnotationPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnnotationPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  AnnotationPatch call({
    AnnotationPatchVerdictEnum? verdict,
    String? duplicateOfRef,
    String? body,
    List<String>? tags,
    bool? private,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnnotationPatch.copyWith(...)` or call `instanceOfAnnotationPatch.copyWith.fieldName(value)` for a single field.
class _$AnnotationPatchCWProxyImpl implements _$AnnotationPatchCWProxy {
  const _$AnnotationPatchCWProxyImpl(this._value);

  final AnnotationPatch _value;

  @override
  AnnotationPatch verdict(AnnotationPatchVerdictEnum? verdict) =>
      call(verdict: verdict);

  @override
  AnnotationPatch duplicateOfRef(String? duplicateOfRef) =>
      call(duplicateOfRef: duplicateOfRef);

  @override
  AnnotationPatch body(String? body) => call(body: body);

  @override
  AnnotationPatch tags(List<String>? tags) => call(tags: tags);

  @override
  AnnotationPatch private(bool? private) => call(private: private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnnotationPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnnotationPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnnotationPatch call({
    Object? verdict = const $CopyWithPlaceholder(),
    Object? duplicateOfRef = const $CopyWithPlaceholder(),
    Object? body = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
  }) {
    return AnnotationPatch(
      verdict: verdict == const $CopyWithPlaceholder()
          ? _value.verdict
          // ignore: cast_nullable_to_non_nullable
          : verdict as AnnotationPatchVerdictEnum?,
      duplicateOfRef: duplicateOfRef == const $CopyWithPlaceholder()
          ? _value.duplicateOfRef
          // ignore: cast_nullable_to_non_nullable
          : duplicateOfRef as String?,
      body: body == const $CopyWithPlaceholder()
          ? _value.body
          // ignore: cast_nullable_to_non_nullable
          : body as String?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
    );
  }
}

extension $AnnotationPatchCopyWith on AnnotationPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnnotationPatch.copyWith(...)` or `instanceOfAnnotationPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnnotationPatchCWProxy get copyWith => _$AnnotationPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnnotationPatch _$AnnotationPatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnnotationPatch', json, ($checkedConvert) {
      final val = AnnotationPatch(
        verdict: $checkedConvert(
          'verdict',
          (v) => $enumDecodeNullable(_$AnnotationPatchVerdictEnumEnumMap, v),
        ),
        duplicateOfRef: $checkedConvert('duplicateOfRef', (v) => v as String?),
        body: $checkedConvert('body', (v) => v as String?),
        tags: $checkedConvert(
          'tags',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        private: $checkedConvert('private', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$AnnotationPatchToJson(AnnotationPatch instance) =>
    <String, dynamic>{
      'verdict': ?_$AnnotationPatchVerdictEnumEnumMap[instance.verdict],
      'duplicateOfRef': ?instance.duplicateOfRef,
      'body': ?instance.body,
      'tags': ?instance.tags,
      'private': ?instance.private,
    };

const _$AnnotationPatchVerdictEnumEnumMap = {
  AnnotationPatchVerdictEnum.confirmed: 'confirmed',
  AnnotationPatchVerdictEnum.rejected: 'rejected',
  AnnotationPatchVerdictEnum.needsReview: 'needs-review',
  AnnotationPatchVerdictEnum.duplicateOf: 'duplicate-of',
};
