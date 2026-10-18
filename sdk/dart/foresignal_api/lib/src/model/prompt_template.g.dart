// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'prompt_template.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PromptTemplateCWProxy {
  PromptTemplate id(String id);

  PromptTemplate version(String version);

  PromptTemplate role(String? role);

  PromptTemplate sha256(String sha256);

  PromptTemplate text(String text);

  PromptTemplate versions(List<String>? versions);

  PromptTemplate instructions(String? instructions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PromptTemplate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PromptTemplate(...).copyWith(id: 12, name: "My name")
  /// ```
  PromptTemplate call({
    String id,
    String version,
    String? role,
    String sha256,
    String text,
    List<String>? versions,
    String? instructions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPromptTemplate.copyWith(...)` or call `instanceOfPromptTemplate.copyWith.fieldName(value)` for a single field.
class _$PromptTemplateCWProxyImpl implements _$PromptTemplateCWProxy {
  const _$PromptTemplateCWProxyImpl(this._value);

  final PromptTemplate _value;

  @override
  PromptTemplate id(String id) => call(id: id);

  @override
  PromptTemplate version(String version) => call(version: version);

  @override
  PromptTemplate role(String? role) => call(role: role);

  @override
  PromptTemplate sha256(String sha256) => call(sha256: sha256);

  @override
  PromptTemplate text(String text) => call(text: text);

  @override
  PromptTemplate versions(List<String>? versions) => call(versions: versions);

  @override
  PromptTemplate instructions(String? instructions) =>
      call(instructions: instructions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PromptTemplate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PromptTemplate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PromptTemplate call({
    Object? id = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? sha256 = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
    Object? versions = const $CopyWithPlaceholder(),
    Object? instructions = const $CopyWithPlaceholder(),
  }) {
    return PromptTemplate(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String,
      role: role == const $CopyWithPlaceholder()
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as String?,
      sha256: sha256 == const $CopyWithPlaceholder() || sha256 == null
          ? _value.sha256
          // ignore: cast_nullable_to_non_nullable
          : sha256 as String,
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
      versions: versions == const $CopyWithPlaceholder()
          ? _value.versions
          // ignore: cast_nullable_to_non_nullable
          : versions as List<String>?,
      instructions: instructions == const $CopyWithPlaceholder()
          ? _value.instructions
          // ignore: cast_nullable_to_non_nullable
          : instructions as String?,
    );
  }
}

extension $PromptTemplateCopyWith on PromptTemplate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPromptTemplate.copyWith(...)` or `instanceOfPromptTemplate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PromptTemplateCWProxy get copyWith => _$PromptTemplateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PromptTemplate _$PromptTemplateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PromptTemplate', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'version', 'sha256', 'text']);
      final val = PromptTemplate(
        id: $checkedConvert('id', (v) => v as String),
        version: $checkedConvert('version', (v) => v as String),
        role: $checkedConvert('role', (v) => v as String?),
        sha256: $checkedConvert('sha256', (v) => v as String),
        text: $checkedConvert('text', (v) => v as String),
        versions: $checkedConvert(
          'versions',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        instructions: $checkedConvert('instructions', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PromptTemplateToJson(PromptTemplate instance) =>
    <String, dynamic>{
      'id': instance.id,
      'version': instance.version,
      'role': ?instance.role,
      'sha256': instance.sha256,
      'text': instance.text,
      'versions': ?instance.versions,
      'instructions': ?instance.instructions,
    };
