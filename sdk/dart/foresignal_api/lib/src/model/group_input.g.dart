// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'group_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GroupInputCWProxy {
  GroupInput name(String name);

  GroupInput description(String? description);

  GroupInput members(List<GroupMemberInput>? members);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GroupInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GroupInput(...).copyWith(id: 12, name: "My name")
  /// ```
  GroupInput call({
    String name,
    String? description,
    List<GroupMemberInput>? members,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGroupInput.copyWith(...)` or call `instanceOfGroupInput.copyWith.fieldName(value)` for a single field.
class _$GroupInputCWProxyImpl implements _$GroupInputCWProxy {
  const _$GroupInputCWProxyImpl(this._value);

  final GroupInput _value;

  @override
  GroupInput name(String name) => call(name: name);

  @override
  GroupInput description(String? description) => call(description: description);

  @override
  GroupInput members(List<GroupMemberInput>? members) => call(members: members);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GroupInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GroupInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GroupInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? members = const $CopyWithPlaceholder(),
  }) {
    return GroupInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      members: members == const $CopyWithPlaceholder()
          ? _value.members
          // ignore: cast_nullable_to_non_nullable
          : members as List<GroupMemberInput>?,
    );
  }
}

extension $GroupInputCopyWith on GroupInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGroupInput.copyWith(...)` or `instanceOfGroupInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GroupInputCWProxy get copyWith => _$GroupInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GroupInput _$GroupInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('GroupInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name']);
      final val = GroupInput(
        name: $checkedConvert('name', (v) => v as String),
        description: $checkedConvert('description', (v) => v as String?),
        members: $checkedConvert(
          'members',
          (v) => (v as List<dynamic>?)
              ?.map((e) => GroupMemberInput.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$GroupInputToJson(GroupInput instance) =>
    <String, dynamic>{
      'name': instance.name,
      'description': ?instance.description,
      'members': ?instance.members?.map((e) => e.toJson()).toList(),
    };
