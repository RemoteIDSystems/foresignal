// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'group.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GroupCWProxy {
  Group id(String id);

  Group name(String name);

  Group description(String? description);

  Group createdBy(PrincipalSummary createdBy);

  Group memberCount(int memberCount);

  Group members(List<GroupMember>? members);

  Group createdAt(DateTime createdAt);

  Group updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Group(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Group(...).copyWith(id: 12, name: "My name")
  /// ```
  Group call({
    String id,
    String name,
    String? description,
    PrincipalSummary createdBy,
    int memberCount,
    List<GroupMember>? members,
    DateTime createdAt,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGroup.copyWith(...)` or call `instanceOfGroup.copyWith.fieldName(value)` for a single field.
class _$GroupCWProxyImpl implements _$GroupCWProxy {
  const _$GroupCWProxyImpl(this._value);

  final Group _value;

  @override
  Group id(String id) => call(id: id);

  @override
  Group name(String name) => call(name: name);

  @override
  Group description(String? description) => call(description: description);

  @override
  Group createdBy(PrincipalSummary createdBy) => call(createdBy: createdBy);

  @override
  Group memberCount(int memberCount) => call(memberCount: memberCount);

  @override
  Group members(List<GroupMember>? members) => call(members: members);

  @override
  Group createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Group updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Group(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Group(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Group call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? memberCount = const $CopyWithPlaceholder(),
    Object? members = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return Group(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      createdBy: createdBy == const $CopyWithPlaceholder() || createdBy == null
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as PrincipalSummary,
      memberCount:
          memberCount == const $CopyWithPlaceholder() || memberCount == null
          ? _value.memberCount
          // ignore: cast_nullable_to_non_nullable
          : memberCount as int,
      members: members == const $CopyWithPlaceholder()
          ? _value.members
          // ignore: cast_nullable_to_non_nullable
          : members as List<GroupMember>?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $GroupCopyWith on Group {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGroup.copyWith(...)` or `instanceOfGroup.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GroupCWProxy get copyWith => _$GroupCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Group _$GroupFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Group', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['id', 'name', 'createdBy', 'memberCount', 'createdAt'],
  );
  final val = Group(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    description: $checkedConvert('description', (v) => v as String?),
    createdBy: $checkedConvert(
      'createdBy',
      (v) => PrincipalSummary.fromJson(v as Map<String, dynamic>),
    ),
    memberCount: $checkedConvert('memberCount', (v) => (v as num).toInt()),
    members: $checkedConvert(
      'members',
      (v) => (v as List<dynamic>?)
          ?.map((e) => GroupMember.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$GroupToJson(Group instance) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
  'description': ?instance.description,
  'createdBy': instance.createdBy.toJson(),
  'memberCount': instance.memberCount,
  'members': ?instance.members?.map((e) => e.toJson()).toList(),
  'createdAt': instance.createdAt.toIso8601String(),
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
};
