// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'group_member.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GroupMemberCWProxy {
  GroupMember principal(PrincipalSummary principal);

  GroupMember role(GroupMemberRoleEnum role);

  GroupMember addedAt(DateTime? addedAt);

  GroupMember addedBy(PrincipalSummary? addedBy);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GroupMember(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GroupMember(...).copyWith(id: 12, name: "My name")
  /// ```
  GroupMember call({
    PrincipalSummary principal,
    GroupMemberRoleEnum role,
    DateTime? addedAt,
    PrincipalSummary? addedBy,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGroupMember.copyWith(...)` or call `instanceOfGroupMember.copyWith.fieldName(value)` for a single field.
class _$GroupMemberCWProxyImpl implements _$GroupMemberCWProxy {
  const _$GroupMemberCWProxyImpl(this._value);

  final GroupMember _value;

  @override
  GroupMember principal(PrincipalSummary principal) =>
      call(principal: principal);

  @override
  GroupMember role(GroupMemberRoleEnum role) => call(role: role);

  @override
  GroupMember addedAt(DateTime? addedAt) => call(addedAt: addedAt);

  @override
  GroupMember addedBy(PrincipalSummary? addedBy) => call(addedBy: addedBy);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GroupMember(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GroupMember(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GroupMember call({
    Object? principal = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? addedAt = const $CopyWithPlaceholder(),
    Object? addedBy = const $CopyWithPlaceholder(),
  }) {
    return GroupMember(
      principal: principal == const $CopyWithPlaceholder() || principal == null
          ? _value.principal
          // ignore: cast_nullable_to_non_nullable
          : principal as PrincipalSummary,
      role: role == const $CopyWithPlaceholder() || role == null
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as GroupMemberRoleEnum,
      addedAt: addedAt == const $CopyWithPlaceholder()
          ? _value.addedAt
          // ignore: cast_nullable_to_non_nullable
          : addedAt as DateTime?,
      addedBy: addedBy == const $CopyWithPlaceholder()
          ? _value.addedBy
          // ignore: cast_nullable_to_non_nullable
          : addedBy as PrincipalSummary?,
    );
  }
}

extension $GroupMemberCopyWith on GroupMember {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGroupMember.copyWith(...)` or `instanceOfGroupMember.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GroupMemberCWProxy get copyWith => _$GroupMemberCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GroupMember _$GroupMemberFromJson(Map<String, dynamic> json) =>
    $checkedCreate('GroupMember', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['principal', 'role']);
      final val = GroupMember(
        principal: $checkedConvert(
          'principal',
          (v) => PrincipalSummary.fromJson(v as Map<String, dynamic>),
        ),
        role: $checkedConvert(
          'role',
          (v) => $enumDecode(_$GroupMemberRoleEnumEnumMap, v),
        ),
        addedAt: $checkedConvert(
          'addedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        addedBy: $checkedConvert(
          'addedBy',
          (v) => v == null
              ? null
              : PrincipalSummary.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$GroupMemberToJson(GroupMember instance) =>
    <String, dynamic>{
      'principal': instance.principal.toJson(),
      'role': _$GroupMemberRoleEnumEnumMap[instance.role]!,
      'addedAt': ?instance.addedAt?.toIso8601String(),
      'addedBy': ?instance.addedBy?.toJson(),
    };

const _$GroupMemberRoleEnumEnumMap = {
  GroupMemberRoleEnum.member: 'member',
  GroupMemberRoleEnum.manager: 'manager',
};
