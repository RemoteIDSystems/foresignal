// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'assessment_prompt_templates_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AssessmentPromptTemplatesInnerCWProxy {
  AssessmentPromptTemplatesInner id(String id);

  AssessmentPromptTemplatesInner version(String version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentPromptTemplatesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentPromptTemplatesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  AssessmentPromptTemplatesInner call({String id, String version});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAssessmentPromptTemplatesInner.copyWith(...)` or call `instanceOfAssessmentPromptTemplatesInner.copyWith.fieldName(value)` for a single field.
class _$AssessmentPromptTemplatesInnerCWProxyImpl
    implements _$AssessmentPromptTemplatesInnerCWProxy {
  const _$AssessmentPromptTemplatesInnerCWProxyImpl(this._value);

  final AssessmentPromptTemplatesInner _value;

  @override
  AssessmentPromptTemplatesInner id(String id) => call(id: id);

  @override
  AssessmentPromptTemplatesInner version(String version) =>
      call(version: version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentPromptTemplatesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentPromptTemplatesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AssessmentPromptTemplatesInner call({
    Object? id = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
  }) {
    return AssessmentPromptTemplatesInner(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String,
    );
  }
}

extension $AssessmentPromptTemplatesInnerCopyWith
    on AssessmentPromptTemplatesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAssessmentPromptTemplatesInner.copyWith(...)` or `instanceOfAssessmentPromptTemplatesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AssessmentPromptTemplatesInnerCWProxy get copyWith =>
      _$AssessmentPromptTemplatesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AssessmentPromptTemplatesInner _$AssessmentPromptTemplatesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AssessmentPromptTemplatesInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'version']);
  final val = AssessmentPromptTemplatesInner(
    id: $checkedConvert('id', (v) => v as String),
    version: $checkedConvert('version', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$AssessmentPromptTemplatesInnerToJson(
  AssessmentPromptTemplatesInner instance,
) => <String, dynamic>{'id': instance.id, 'version': instance.version};
