// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'object_acl_entry_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ObjectAclEntryInputCWProxy {
  ObjectAclEntryInput grantee(String grantee);

  ObjectAclEntryInput role(ObjectAclEntryInputRoleEnum role);

  ObjectAclEntryInput expiresAt(DateTime? expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectAclEntryInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectAclEntryInput(...).copyWith(id: 12, name: "My name")
  /// ```
  ObjectAclEntryInput call({
    String grantee,
    ObjectAclEntryInputRoleEnum role,
    DateTime? expiresAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfObjectAclEntryInput.copyWith(...)` or call `instanceOfObjectAclEntryInput.copyWith.fieldName(value)` for a single field.
class _$ObjectAclEntryInputCWProxyImpl implements _$ObjectAclEntryInputCWProxy {
  const _$ObjectAclEntryInputCWProxyImpl(this._value);

  final ObjectAclEntryInput _value;

  @override
  ObjectAclEntryInput grantee(String grantee) => call(grantee: grantee);

  @override
  ObjectAclEntryInput role(ObjectAclEntryInputRoleEnum role) =>
      call(role: role);

  @override
  ObjectAclEntryInput expiresAt(DateTime? expiresAt) =>
      call(expiresAt: expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectAclEntryInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectAclEntryInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ObjectAclEntryInput call({
    Object? grantee = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
  }) {
    return ObjectAclEntryInput(
      grantee: grantee == const $CopyWithPlaceholder() || grantee == null
          ? _value.grantee
          // ignore: cast_nullable_to_non_nullable
          : grantee as String,
      role: role == const $CopyWithPlaceholder() || role == null
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as ObjectAclEntryInputRoleEnum,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
    );
  }
}

extension $ObjectAclEntryInputCopyWith on ObjectAclEntryInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfObjectAclEntryInput.copyWith(...)` or `instanceOfObjectAclEntryInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ObjectAclEntryInputCWProxy get copyWith =>
      _$ObjectAclEntryInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ObjectAclEntryInput _$ObjectAclEntryInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ObjectAclEntryInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['grantee', 'role']);
      final val = ObjectAclEntryInput(
        grantee: $checkedConvert('grantee', (v) => v as String),
        role: $checkedConvert(
          'role',
          (v) => $enumDecode(_$ObjectAclEntryInputRoleEnumEnumMap, v),
        ),
        expiresAt: $checkedConvert(
          'expiresAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ObjectAclEntryInputToJson(
  ObjectAclEntryInput instance,
) => <String, dynamic>{
  'grantee': instance.grantee,
  'role': _$ObjectAclEntryInputRoleEnumEnumMap[instance.role]!,
  'expiresAt': ?instance.expiresAt?.toIso8601String(),
};

const _$ObjectAclEntryInputRoleEnumEnumMap = {
  ObjectAclEntryInputRoleEnum.viewer: 'viewer',
  ObjectAclEntryInputRoleEnum.commenter: 'commenter',
  ObjectAclEntryInputRoleEnum.editor: 'editor',
  ObjectAclEntryInputRoleEnum.owner: 'owner',
};
