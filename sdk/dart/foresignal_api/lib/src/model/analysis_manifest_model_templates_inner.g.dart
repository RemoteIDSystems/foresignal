// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analysis_manifest_model_templates_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalysisManifestModelTemplatesInnerCWProxy {
  AnalysisManifestModelTemplatesInner templateId(String templateId);

  AnalysisManifestModelTemplatesInner version(String version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisManifestModelTemplatesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisManifestModelTemplatesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalysisManifestModelTemplatesInner call({String templateId, String version});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalysisManifestModelTemplatesInner.copyWith(...)` or call `instanceOfAnalysisManifestModelTemplatesInner.copyWith.fieldName(value)` for a single field.
class _$AnalysisManifestModelTemplatesInnerCWProxyImpl
    implements _$AnalysisManifestModelTemplatesInnerCWProxy {
  const _$AnalysisManifestModelTemplatesInnerCWProxyImpl(this._value);

  final AnalysisManifestModelTemplatesInner _value;

  @override
  AnalysisManifestModelTemplatesInner templateId(String templateId) =>
      call(templateId: templateId);

  @override
  AnalysisManifestModelTemplatesInner version(String version) =>
      call(version: version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisManifestModelTemplatesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisManifestModelTemplatesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalysisManifestModelTemplatesInner call({
    Object? templateId = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
  }) {
    return AnalysisManifestModelTemplatesInner(
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

extension $AnalysisManifestModelTemplatesInnerCopyWith
    on AnalysisManifestModelTemplatesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalysisManifestModelTemplatesInner.copyWith(...)` or `instanceOfAnalysisManifestModelTemplatesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalysisManifestModelTemplatesInnerCWProxy get copyWith =>
      _$AnalysisManifestModelTemplatesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalysisManifestModelTemplatesInner
_$AnalysisManifestModelTemplatesInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalysisManifestModelTemplatesInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['templateId', 'version']);
      final val = AnalysisManifestModelTemplatesInner(
        templateId: $checkedConvert('templateId', (v) => v as String),
        version: $checkedConvert('version', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$AnalysisManifestModelTemplatesInnerToJson(
  AnalysisManifestModelTemplatesInner instance,
) => <String, dynamic>{
  'templateId': instance.templateId,
  'version': instance.version,
};
