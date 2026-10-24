// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'source_relevance_analysis_template.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SourceRelevanceAnalysisTemplateCWProxy {
  SourceRelevanceAnalysisTemplate templateId(String templateId);

  SourceRelevanceAnalysisTemplate version(String version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceRelevanceAnalysisTemplate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceRelevanceAnalysisTemplate(...).copyWith(id: 12, name: "My name")
  /// ```
  SourceRelevanceAnalysisTemplate call({String templateId, String version});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSourceRelevanceAnalysisTemplate.copyWith(...)` or call `instanceOfSourceRelevanceAnalysisTemplate.copyWith.fieldName(value)` for a single field.
class _$SourceRelevanceAnalysisTemplateCWProxyImpl
    implements _$SourceRelevanceAnalysisTemplateCWProxy {
  const _$SourceRelevanceAnalysisTemplateCWProxyImpl(this._value);

  final SourceRelevanceAnalysisTemplate _value;

  @override
  SourceRelevanceAnalysisTemplate templateId(String templateId) =>
      call(templateId: templateId);

  @override
  SourceRelevanceAnalysisTemplate version(String version) =>
      call(version: version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceRelevanceAnalysisTemplate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceRelevanceAnalysisTemplate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SourceRelevanceAnalysisTemplate call({
    Object? templateId = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
  }) {
    return SourceRelevanceAnalysisTemplate(
      templateId:
          templateId == const $CopyWithPlaceholder() || templateId == null
          ? _value.templateId
          // ignore: cast_nullable_to_non_nullable
          : templateId as String,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String,
    );
  }
}

extension $SourceRelevanceAnalysisTemplateCopyWith
    on SourceRelevanceAnalysisTemplate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSourceRelevanceAnalysisTemplate.copyWith(...)` or `instanceOfSourceRelevanceAnalysisTemplate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SourceRelevanceAnalysisTemplateCWProxy get copyWith =>
      _$SourceRelevanceAnalysisTemplateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SourceRelevanceAnalysisTemplate _$SourceRelevanceAnalysisTemplateFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SourceRelevanceAnalysisTemplate', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['templateId', 'version']);
  final val = SourceRelevanceAnalysisTemplate(
    templateId: $checkedConvert('templateId', (v) => v as String),
    version: $checkedConvert('version', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$SourceRelevanceAnalysisTemplateToJson(
  SourceRelevanceAnalysisTemplate instance,
) => <String, dynamic>{
  'templateId': instance.templateId,
  'version': instance.version,
};
