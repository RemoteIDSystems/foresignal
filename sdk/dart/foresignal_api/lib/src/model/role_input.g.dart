// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'role_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RoleInputCWProxy {
  RoleInput name(String name);

  RoleInput description(String? description);

  RoleInput permissions(List<String> permissions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RoleInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RoleInput(...).copyWith(id: 12, name: "My name")
  /// ```
  RoleInput call({String name, String? description, List<String> permissions});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRoleInput.copyWith(...)` or call `instanceOfRoleInput.copyWith.fieldName(value)` for a single field.
class _$RoleInputCWProxyImpl implements _$RoleInputCWProxy {
  const _$RoleInputCWProxyImpl(this._value);

  final RoleInput _value;

  @override
  RoleInput name(String name) => call(name: name);

  @override
  RoleInput description(String? description) => call(description: description);

  @override
  RoleInput permissions(List<String> permissions) =>
      call(permissions: permissions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RoleInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RoleInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RoleInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? permissions = const $CopyWithPlaceholder(),
  }) {
    return RoleInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      permissions:
          permissions == const $CopyWithPlaceholder() || permissions == null
          ? _value.permissions
          // ignore: cast_nullable_to_non_nullable
          : permissions as List<String>,
    );
  }
}

extension $RoleInputCopyWith on RoleInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRoleInput.copyWith(...)` or `instanceOfRoleInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RoleInputCWProxy get copyWith => _$RoleInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RoleInput _$RoleInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RoleInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'permissions']);
      final val = RoleInput(
        name: $checkedConvert('name', (v) => v as String),
        description: $checkedConvert('description', (v) => v as String?),
        permissions: $checkedConvert(
          'permissions',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$RoleInputToJson(RoleInput instance) => <String, dynamic>{
  'name': instance.name,
  'description': ?instance.description,
  'permissions': instance.permissions,
};
