// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'prompt_template_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PromptTemplateSummaryCWProxy {
  PromptTemplateSummary id(String id);

  PromptTemplateSummary role(String? role);

  PromptTemplateSummary version(String version);

  PromptTemplateSummary versions(List<String> versions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PromptTemplateSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PromptTemplateSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  PromptTemplateSummary call({
    String id,
    String? role,
    String version,
    List<String> versions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPromptTemplateSummary.copyWith(...)` or call `instanceOfPromptTemplateSummary.copyWith.fieldName(value)` for a single field.
class _$PromptTemplateSummaryCWProxyImpl
    implements _$PromptTemplateSummaryCWProxy {
  const _$PromptTemplateSummaryCWProxyImpl(this._value);

  final PromptTemplateSummary _value;

  @override
  PromptTemplateSummary id(String id) => call(id: id);

  @override
  PromptTemplateSummary role(String? role) => call(role: role);

  @override
  PromptTemplateSummary version(String version) => call(version: version);

  @override
  PromptTemplateSummary versions(List<String> versions) =>
      call(versions: versions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PromptTemplateSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PromptTemplateSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PromptTemplateSummary call({
    Object? id = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? versions = const $CopyWithPlaceholder(),
  }) {
    return PromptTemplateSummary(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      role: role == const $CopyWithPlaceholder()
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as String?,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String,
      versions: versions == const $CopyWithPlaceholder() || versions == null
          ? _value.versions
          // ignore: cast_nullable_to_non_nullable
          : versions as List<String>,
    );
  }
}

extension $PromptTemplateSummaryCopyWith on PromptTemplateSummary {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPromptTemplateSummary.copyWith(...)` or `instanceOfPromptTemplateSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PromptTemplateSummaryCWProxy get copyWith =>
      _$PromptTemplateSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PromptTemplateSummary _$PromptTemplateSummaryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PromptTemplateSummary', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'role', 'version', 'versions']);
  final val = PromptTemplateSummary(
    id: $checkedConvert('id', (v) => v as String),
    role: $checkedConvert('role', (v) => v as String?),
    version: $checkedConvert('version', (v) => v as String),
    versions: $checkedConvert(
      'versions',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$PromptTemplateSummaryToJson(
  PromptTemplateSummary instance,
) => <String, dynamic>{
  'id': instance.id,
  'role': instance.role,
  'version': instance.version,
  'versions': instance.versions,
};
