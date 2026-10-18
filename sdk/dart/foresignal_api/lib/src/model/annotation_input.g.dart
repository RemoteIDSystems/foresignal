// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'annotation_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnnotationInputCWProxy {
  AnnotationInput kind(AnnotationInputKindEnum kind);

  AnnotationInput verdict(AnnotationInputVerdictEnum? verdict);

  AnnotationInput duplicateOfRef(String? duplicateOfRef);

  AnnotationInput body(String? body);

  AnnotationInput tags(List<String>? tags);

  AnnotationInput private(bool? private);

  AnnotationInput applyStatus(bool? applyStatus);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnnotationInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnnotationInput(...).copyWith(id: 12, name: "My name")
  /// ```
  AnnotationInput call({
    AnnotationInputKindEnum kind,
    AnnotationInputVerdictEnum? verdict,
    String? duplicateOfRef,
    String? body,
    List<String>? tags,
    bool? private,
    bool? applyStatus,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnnotationInput.copyWith(...)` or call `instanceOfAnnotationInput.copyWith.fieldName(value)` for a single field.
class _$AnnotationInputCWProxyImpl implements _$AnnotationInputCWProxy {
  const _$AnnotationInputCWProxyImpl(this._value);

  final AnnotationInput _value;

  @override
  AnnotationInput kind(AnnotationInputKindEnum kind) => call(kind: kind);

  @override
  AnnotationInput verdict(AnnotationInputVerdictEnum? verdict) =>
      call(verdict: verdict);

  @override
  AnnotationInput duplicateOfRef(String? duplicateOfRef) =>
      call(duplicateOfRef: duplicateOfRef);

  @override
  AnnotationInput body(String? body) => call(body: body);

  @override
  AnnotationInput tags(List<String>? tags) => call(tags: tags);

  @override
  AnnotationInput private(bool? private) => call(private: private);

  @override
  AnnotationInput applyStatus(bool? applyStatus) =>
      call(applyStatus: applyStatus);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnnotationInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnnotationInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnnotationInput call({
    Object? kind = const $CopyWithPlaceholder(),
    Object? verdict = const $CopyWithPlaceholder(),
    Object? duplicateOfRef = const $CopyWithPlaceholder(),
    Object? body = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? applyStatus = const $CopyWithPlaceholder(),
  }) {
    return AnnotationInput(
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as AnnotationInputKindEnum,
      verdict: verdict == const $CopyWithPlaceholder()
          ? _value.verdict
          // ignore: cast_nullable_to_non_nullable
          : verdict as AnnotationInputVerdictEnum?,
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
      applyStatus: applyStatus == const $CopyWithPlaceholder()
          ? _value.applyStatus
          // ignore: cast_nullable_to_non_nullable
          : applyStatus as bool?,
    );
  }
}

extension $AnnotationInputCopyWith on AnnotationInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnnotationInput.copyWith(...)` or `instanceOfAnnotationInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnnotationInputCWProxy get copyWith => _$AnnotationInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnnotationInput _$AnnotationInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnnotationInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['kind']);
      final val = AnnotationInput(
        kind: $checkedConvert(
          'kind',
          (v) => $enumDecode(_$AnnotationInputKindEnumEnumMap, v),
        ),
        verdict: $checkedConvert(
          'verdict',
          (v) => $enumDecodeNullable(_$AnnotationInputVerdictEnumEnumMap, v),
        ),
        duplicateOfRef: $checkedConvert('duplicateOfRef', (v) => v as String?),
        body: $checkedConvert('body', (v) => v as String?),
        tags: $checkedConvert(
          'tags',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        private: $checkedConvert('private', (v) => v as bool? ?? false),
        applyStatus: $checkedConvert('applyStatus', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$AnnotationInputToJson(AnnotationInput instance) =>
    <String, dynamic>{
      'kind': _$AnnotationInputKindEnumEnumMap[instance.kind]!,
      'verdict': ?_$AnnotationInputVerdictEnumEnumMap[instance.verdict],
      'duplicateOfRef': ?instance.duplicateOfRef,
      'body': ?instance.body,
      'tags': ?instance.tags,
      'private': ?instance.private,
      'applyStatus': ?instance.applyStatus,
    };

const _$AnnotationInputKindEnumEnumMap = {
  AnnotationInputKindEnum.note: 'note',
  AnnotationInputKindEnum.verdict: 'verdict',
};

const _$AnnotationInputVerdictEnumEnumMap = {
  AnnotationInputVerdictEnum.confirmed: 'confirmed',
  AnnotationInputVerdictEnum.rejected: 'rejected',
  AnnotationInputVerdictEnum.needsReview: 'needs-review',
  AnnotationInputVerdictEnum.duplicateOf: 'duplicate-of',
};
