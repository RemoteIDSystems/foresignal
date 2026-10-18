// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'workspace.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WorkspaceCWProxy {
  Workspace id(String id);

  Workspace ref(String? ref);

  Workspace name(String name);

  Workspace owner(String owner);

  Workspace members(Map<String, WorkspaceMembersEnum> members);

  Workspace callerRole(WorkspaceCallerRoleEnum? callerRole);

  Workspace context(WorkspaceContext context);

  Workspace contextVersion(int contextVersion);

  Workspace layout(Map<String, Object>? layout);

  Workspace savedViewIds(List<String>? savedViewIds);

  Workspace investigationId(String? investigationId);

  Workspace private(bool private);

  Workspace createdAt(DateTime createdAt);

  Workspace updatedAt(DateTime updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Workspace(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Workspace(...).copyWith(id: 12, name: "My name")
  /// ```
  Workspace call({
    String id,
    String? ref,
    String name,
    String owner,
    Map<String, WorkspaceMembersEnum> members,
    WorkspaceCallerRoleEnum? callerRole,
    WorkspaceContext context,
    int contextVersion,
    Map<String, Object>? layout,
    List<String>? savedViewIds,
    String? investigationId,
    bool private,
    DateTime createdAt,
    DateTime updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWorkspace.copyWith(...)` or call `instanceOfWorkspace.copyWith.fieldName(value)` for a single field.
class _$WorkspaceCWProxyImpl implements _$WorkspaceCWProxy {
  const _$WorkspaceCWProxyImpl(this._value);

  final Workspace _value;

  @override
  Workspace id(String id) => call(id: id);

  @override
  Workspace ref(String? ref) => call(ref: ref);

  @override
  Workspace name(String name) => call(name: name);

  @override
  Workspace owner(String owner) => call(owner: owner);

  @override
  Workspace members(Map<String, WorkspaceMembersEnum> members) =>
      call(members: members);

  @override
  Workspace callerRole(WorkspaceCallerRoleEnum? callerRole) =>
      call(callerRole: callerRole);

  @override
  Workspace context(WorkspaceContext context) => call(context: context);

  @override
  Workspace contextVersion(int contextVersion) =>
      call(contextVersion: contextVersion);

  @override
  Workspace layout(Map<String, Object>? layout) => call(layout: layout);

  @override
  Workspace savedViewIds(List<String>? savedViewIds) =>
      call(savedViewIds: savedViewIds);

  @override
  Workspace investigationId(String? investigationId) =>
      call(investigationId: investigationId);

  @override
  Workspace private(bool private) => call(private: private);

  @override
  Workspace createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Workspace updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Workspace(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Workspace(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Workspace call({
    Object? id = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? owner = const $CopyWithPlaceholder(),
    Object? members = const $CopyWithPlaceholder(),
    Object? callerRole = const $CopyWithPlaceholder(),
    Object? context = const $CopyWithPlaceholder(),
    Object? contextVersion = const $CopyWithPlaceholder(),
    Object? layout = const $CopyWithPlaceholder(),
    Object? savedViewIds = const $CopyWithPlaceholder(),
    Object? investigationId = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return Workspace(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      owner: owner == const $CopyWithPlaceholder() || owner == null
          ? _value.owner
          // ignore: cast_nullable_to_non_nullable
          : owner as String,
      members: members == const $CopyWithPlaceholder() || members == null
          ? _value.members
          // ignore: cast_nullable_to_non_nullable
          : members as Map<String, WorkspaceMembersEnum>,
      callerRole: callerRole == const $CopyWithPlaceholder()
          ? _value.callerRole
          // ignore: cast_nullable_to_non_nullable
          : callerRole as WorkspaceCallerRoleEnum?,
      context: context == const $CopyWithPlaceholder() || context == null
          ? _value.context
          // ignore: cast_nullable_to_non_nullable
          : context as WorkspaceContext,
      contextVersion:
          contextVersion == const $CopyWithPlaceholder() ||
              contextVersion == null
          ? _value.contextVersion
          // ignore: cast_nullable_to_non_nullable
          : contextVersion as int,
      layout: layout == const $CopyWithPlaceholder()
          ? _value.layout
          // ignore: cast_nullable_to_non_nullable
          : layout as Map<String, Object>?,
      savedViewIds: savedViewIds == const $CopyWithPlaceholder()
          ? _value.savedViewIds
          // ignore: cast_nullable_to_non_nullable
          : savedViewIds as List<String>?,
      investigationId: investigationId == const $CopyWithPlaceholder()
          ? _value.investigationId
          // ignore: cast_nullable_to_non_nullable
          : investigationId as String?,
      private: private == const $CopyWithPlaceholder() || private == null
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
    );
  }
}

extension $WorkspaceCopyWith on Workspace {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWorkspace.copyWith(...)` or `instanceOfWorkspace.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WorkspaceCWProxy get copyWith => _$WorkspaceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Workspace _$WorkspaceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Workspace', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'name',
      'owner',
      'members',
      'context',
      'contextVersion',
      'private',
      'createdAt',
      'updatedAt',
    ],
  );
  final val = Workspace(
    id: $checkedConvert('id', (v) => v as String),
    ref: $checkedConvert('ref', (v) => v as String?),
    name: $checkedConvert('name', (v) => v as String),
    owner: $checkedConvert('owner', (v) => v as String),
    members: $checkedConvert(
      'members',
      (v) => (v as Map<String, dynamic>).map(
        (k, e) => MapEntry(k, $enumDecode(_$WorkspaceMembersEnumEnumMap, e)),
      ),
    ),
    callerRole: $checkedConvert(
      'callerRole',
      (v) => $enumDecodeNullable(_$WorkspaceCallerRoleEnumEnumMap, v),
    ),
    context: $checkedConvert(
      'context',
      (v) => WorkspaceContext.fromJson(v as Map<String, dynamic>),
    ),
    contextVersion: $checkedConvert(
      'contextVersion',
      (v) => (v as num).toInt(),
    ),
    layout: $checkedConvert(
      'layout',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    savedViewIds: $checkedConvert(
      'savedViewIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    investigationId: $checkedConvert('investigationId', (v) => v as String?),
    private: $checkedConvert('private', (v) => v as bool),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$WorkspaceToJson(Workspace instance) => <String, dynamic>{
  'id': instance.id,
  'ref': ?instance.ref,
  'name': instance.name,
  'owner': instance.owner,
  'members': instance.members.map(
    (k, e) => MapEntry(k, _$WorkspaceMembersEnumEnumMap[e]!),
  ),
  'callerRole': ?_$WorkspaceCallerRoleEnumEnumMap[instance.callerRole],
  'context': instance.context.toJson(),
  'contextVersion': instance.contextVersion,
  'layout': ?instance.layout,
  'savedViewIds': ?instance.savedViewIds,
  'investigationId': ?instance.investigationId,
  'private': instance.private,
  'createdAt': instance.createdAt.toIso8601String(),
  'updatedAt': instance.updatedAt.toIso8601String(),
};

const _$WorkspaceMembersEnumEnumMap = {
  WorkspaceMembersEnum.viewer: 'viewer',
  WorkspaceMembersEnum.editor: 'editor',
};

const _$WorkspaceCallerRoleEnumEnumMap = {
  WorkspaceCallerRoleEnum.owner: 'owner',
  WorkspaceCallerRoleEnum.editor: 'editor',
  WorkspaceCallerRoleEnum.viewer: 'viewer',
};
