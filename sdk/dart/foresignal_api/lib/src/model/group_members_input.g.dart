// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'group_members_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GroupMembersInputCWProxy {
  GroupMembersInput members(List<GroupMemberInput> members);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GroupMembersInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GroupMembersInput(...).copyWith(id: 12, name: "My name")
  /// ```
  GroupMembersInput call({List<GroupMemberInput> members});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGroupMembersInput.copyWith(...)` or call `instanceOfGroupMembersInput.copyWith.fieldName(value)` for a single field.
class _$GroupMembersInputCWProxyImpl implements _$GroupMembersInputCWProxy {
  const _$GroupMembersInputCWProxyImpl(this._value);

  final GroupMembersInput _value;

  @override
  GroupMembersInput members(List<GroupMemberInput> members) =>
      call(members: members);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GroupMembersInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GroupMembersInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GroupMembersInput call({Object? members = const $CopyWithPlaceholder()}) {
    return GroupMembersInput(
      members: members == const $CopyWithPlaceholder() || members == null
          ? _value.members
          // ignore: cast_nullable_to_non_nullable
          : members as List<GroupMemberInput>,
    );
  }
}

extension $GroupMembersInputCopyWith on GroupMembersInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGroupMembersInput.copyWith(...)` or `instanceOfGroupMembersInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GroupMembersInputCWProxy get copyWith =>
      _$GroupMembersInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GroupMembersInput _$GroupMembersInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('GroupMembersInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['members']);
      final val = GroupMembersInput(
        members: $checkedConvert(
          'members',
          (v) => (v as List<dynamic>)
              .map((e) => GroupMemberInput.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$GroupMembersInputToJson(GroupMembersInput instance) =>
    <String, dynamic>{
      'members': instance.members.map((e) => e.toJson()).toList(),
    };
