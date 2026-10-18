// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cycle_run_roles_inner_prompt_template.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CycleRunRolesInnerPromptTemplateCWProxy {
  CycleRunRolesInnerPromptTemplate id(String id);

  CycleRunRolesInnerPromptTemplate version(String version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CycleRunRolesInnerPromptTemplate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CycleRunRolesInnerPromptTemplate(...).copyWith(id: 12, name: "My name")
  /// ```
  CycleRunRolesInnerPromptTemplate call({String id, String version});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCycleRunRolesInnerPromptTemplate.copyWith(...)` or call `instanceOfCycleRunRolesInnerPromptTemplate.copyWith.fieldName(value)` for a single field.
class _$CycleRunRolesInnerPromptTemplateCWProxyImpl
    implements _$CycleRunRolesInnerPromptTemplateCWProxy {
  const _$CycleRunRolesInnerPromptTemplateCWProxyImpl(this._value);

  final CycleRunRolesInnerPromptTemplate _value;

  @override
  CycleRunRolesInnerPromptTemplate id(String id) => call(id: id);

  @override
  CycleRunRolesInnerPromptTemplate version(String version) =>
      call(version: version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CycleRunRolesInnerPromptTemplate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CycleRunRolesInnerPromptTemplate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CycleRunRolesInnerPromptTemplate call({
    Object? id = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
  }) {
    return CycleRunRolesInnerPromptTemplate(
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

extension $CycleRunRolesInnerPromptTemplateCopyWith
    on CycleRunRolesInnerPromptTemplate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCycleRunRolesInnerPromptTemplate.copyWith(...)` or `instanceOfCycleRunRolesInnerPromptTemplate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CycleRunRolesInnerPromptTemplateCWProxy get copyWith =>
      _$CycleRunRolesInnerPromptTemplateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CycleRunRolesInnerPromptTemplate _$CycleRunRolesInnerPromptTemplateFromJson(
  Map<String, dynamic> json,
) =>
    $checkedCreate('CycleRunRolesInnerPromptTemplate', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'version']);
      final val = CycleRunRolesInnerPromptTemplate(
        id: $checkedConvert('id', (v) => v as String),
        version: $checkedConvert('version', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$CycleRunRolesInnerPromptTemplateToJson(
  CycleRunRolesInnerPromptTemplate instance,
) => <String, dynamic>{'id': instance.id, 'version': instance.version};
