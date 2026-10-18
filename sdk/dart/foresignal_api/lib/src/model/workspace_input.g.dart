// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'workspace_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WorkspaceInputCWProxy {
  WorkspaceInput name(String name);

  WorkspaceInput members(Map<String, WorkspaceInputMembersEnum>? members);

  WorkspaceInput context(WorkspaceContext? context);

  WorkspaceInput layout(Map<String, Object>? layout);

  WorkspaceInput savedViewIds(List<String>? savedViewIds);

  WorkspaceInput investigationId(String? investigationId);

  WorkspaceInput private(bool? private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WorkspaceInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WorkspaceInput(...).copyWith(id: 12, name: "My name")
  /// ```
  WorkspaceInput call({
    String name,
    Map<String, WorkspaceInputMembersEnum>? members,
    WorkspaceContext? context,
    Map<String, Object>? layout,
    List<String>? savedViewIds,
    String? investigationId,
    bool? private,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWorkspaceInput.copyWith(...)` or call `instanceOfWorkspaceInput.copyWith.fieldName(value)` for a single field.
class _$WorkspaceInputCWProxyImpl implements _$WorkspaceInputCWProxy {
  const _$WorkspaceInputCWProxyImpl(this._value);

  final WorkspaceInput _value;

  @override
  WorkspaceInput name(String name) => call(name: name);

  @override
  WorkspaceInput members(Map<String, WorkspaceInputMembersEnum>? members) =>
      call(members: members);

  @override
  WorkspaceInput context(WorkspaceContext? context) => call(context: context);

  @override
  WorkspaceInput layout(Map<String, Object>? layout) => call(layout: layout);

  @override
  WorkspaceInput savedViewIds(List<String>? savedViewIds) =>
      call(savedViewIds: savedViewIds);

  @override
  WorkspaceInput investigationId(String? investigationId) =>
      call(investigationId: investigationId);

  @override
  WorkspaceInput private(bool? private) => call(private: private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WorkspaceInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WorkspaceInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WorkspaceInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? members = const $CopyWithPlaceholder(),
    Object? context = const $CopyWithPlaceholder(),
    Object? layout = const $CopyWithPlaceholder(),
    Object? savedViewIds = const $CopyWithPlaceholder(),
    Object? investigationId = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
  }) {
    return WorkspaceInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      members: members == const $CopyWithPlaceholder()
          ? _value.members
          // ignore: cast_nullable_to_non_nullable
          : members as Map<String, WorkspaceInputMembersEnum>?,
      context: context == const $CopyWithPlaceholder()
          ? _value.context
          // ignore: cast_nullable_to_non_nullable
          : context as WorkspaceContext?,
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
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
    );
  }
}

extension $WorkspaceInputCopyWith on WorkspaceInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWorkspaceInput.copyWith(...)` or `instanceOfWorkspaceInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WorkspaceInputCWProxy get copyWith => _$WorkspaceInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WorkspaceInput _$WorkspaceInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('WorkspaceInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['name']);
  final val = WorkspaceInput(
    name: $checkedConvert('name', (v) => v as String),
    members: $checkedConvert(
      'members',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) =>
            MapEntry(k, $enumDecode(_$WorkspaceInputMembersEnumEnumMap, e)),
      ),
    ),
    context: $checkedConvert(
      'context',
      (v) => v == null
          ? null
          : WorkspaceContext.fromJson(v as Map<String, dynamic>),
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
    private: $checkedConvert('private', (v) => v as bool? ?? false),
  );
  return val;
});

Map<String, dynamic> _$WorkspaceInputToJson(WorkspaceInput instance) =>
    <String, dynamic>{
      'name': instance.name,
      'members': ?instance.members?.map(
        (k, e) => MapEntry(k, _$WorkspaceInputMembersEnumEnumMap[e]!),
      ),
      'context': ?instance.context?.toJson(),
      'layout': ?instance.layout,
      'savedViewIds': ?instance.savedViewIds,
      'investigationId': ?instance.investigationId,
      'private': ?instance.private,
    };

const _$WorkspaceInputMembersEnumEnumMap = {
  WorkspaceInputMembersEnum.viewer: 'viewer',
  WorkspaceInputMembersEnum.editor: 'editor',
};
