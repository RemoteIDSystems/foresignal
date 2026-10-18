// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'group_member_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GroupMemberInputCWProxy {
  GroupMemberInput principalId(String principalId);

  GroupMemberInput role(GroupMemberInputRoleEnum? role);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GroupMemberInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GroupMemberInput(...).copyWith(id: 12, name: "My name")
  /// ```
  GroupMemberInput call({String principalId, GroupMemberInputRoleEnum? role});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGroupMemberInput.copyWith(...)` or call `instanceOfGroupMemberInput.copyWith.fieldName(value)` for a single field.
class _$GroupMemberInputCWProxyImpl implements _$GroupMemberInputCWProxy {
  const _$GroupMemberInputCWProxyImpl(this._value);

  final GroupMemberInput _value;

  @override
  GroupMemberInput principalId(String principalId) =>
      call(principalId: principalId);

  @override
  GroupMemberInput role(GroupMemberInputRoleEnum? role) => call(role: role);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GroupMemberInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GroupMemberInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GroupMemberInput call({
    Object? principalId = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
  }) {
    return GroupMemberInput(
      principalId:
          principalId == const $CopyWithPlaceholder() || principalId == null
          ? _value.principalId
          // ignore: cast_nullable_to_non_nullable
          : principalId as String,
      role: role == const $CopyWithPlaceholder()
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as GroupMemberInputRoleEnum?,
    );
  }
}

extension $GroupMemberInputCopyWith on GroupMemberInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGroupMemberInput.copyWith(...)` or `instanceOfGroupMemberInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GroupMemberInputCWProxy get copyWith => _$GroupMemberInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GroupMemberInput _$GroupMemberInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('GroupMemberInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['principalId']);
      final val = GroupMemberInput(
        principalId: $checkedConvert('principalId', (v) => v as String),
        role: $checkedConvert(
          'role',
          (v) => $enumDecodeNullable(_$GroupMemberInputRoleEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$GroupMemberInputToJson(GroupMemberInput instance) =>
    <String, dynamic>{
      'principalId': instance.principalId,
      'role': ?_$GroupMemberInputRoleEnumEnumMap[instance.role],
    };

const _$GroupMemberInputRoleEnumEnumMap = {
  GroupMemberInputRoleEnum.member: 'member',
  GroupMemberInputRoleEnum.manager: 'manager',
};
