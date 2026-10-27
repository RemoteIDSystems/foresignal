// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'role_assignment.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RoleAssignmentCWProxy {
  RoleAssignment roles(List<String> roles);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RoleAssignment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RoleAssignment(...).copyWith(id: 12, name: "My name")
  /// ```
  RoleAssignment call({List<String> roles});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRoleAssignment.copyWith(...)` or call `instanceOfRoleAssignment.copyWith.fieldName(value)` for a single field.
class _$RoleAssignmentCWProxyImpl implements _$RoleAssignmentCWProxy {
  const _$RoleAssignmentCWProxyImpl(this._value);

  final RoleAssignment _value;

  @override
  RoleAssignment roles(List<String> roles) => call(roles: roles);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RoleAssignment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RoleAssignment(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RoleAssignment call({Object? roles = const $CopyWithPlaceholder()}) {
    return RoleAssignment(
      roles: roles == const $CopyWithPlaceholder() || roles == null
          ? _value.roles
          // ignore: cast_nullable_to_non_nullable
          : roles as List<String>,
    );
  }
}

extension $RoleAssignmentCopyWith on RoleAssignment {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRoleAssignment.copyWith(...)` or `instanceOfRoleAssignment.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RoleAssignmentCWProxy get copyWith => _$RoleAssignmentCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RoleAssignment _$RoleAssignmentFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RoleAssignment', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['roles']);
      final val = RoleAssignment(
        roles: $checkedConvert(
          'roles',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$RoleAssignmentToJson(RoleAssignment instance) =>
    <String, dynamic>{'roles': instance.roles};
