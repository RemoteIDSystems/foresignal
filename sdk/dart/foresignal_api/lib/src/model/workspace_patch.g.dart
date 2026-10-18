// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'workspace_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WorkspacePatchCWProxy {
  WorkspacePatch name(String? name);

  WorkspacePatch members(Map<String, WorkspacePatchMembersEnum>? members);

  WorkspacePatch savedViewIds(List<String>? savedViewIds);

  WorkspacePatch investigationId(String? investigationId);

  WorkspacePatch private(bool? private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WorkspacePatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WorkspacePatch(...).copyWith(id: 12, name: "My name")
  /// ```
  WorkspacePatch call({
    String? name,
    Map<String, WorkspacePatchMembersEnum>? members,
    List<String>? savedViewIds,
    String? investigationId,
    bool? private,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWorkspacePatch.copyWith(...)` or call `instanceOfWorkspacePatch.copyWith.fieldName(value)` for a single field.
class _$WorkspacePatchCWProxyImpl implements _$WorkspacePatchCWProxy {
  const _$WorkspacePatchCWProxyImpl(this._value);

  final WorkspacePatch _value;

  @override
  WorkspacePatch name(String? name) => call(name: name);

  @override
  WorkspacePatch members(Map<String, WorkspacePatchMembersEnum>? members) =>
      call(members: members);

  @override
  WorkspacePatch savedViewIds(List<String>? savedViewIds) =>
      call(savedViewIds: savedViewIds);

  @override
  WorkspacePatch investigationId(String? investigationId) =>
      call(investigationId: investigationId);

  @override
  WorkspacePatch private(bool? private) => call(private: private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WorkspacePatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WorkspacePatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WorkspacePatch call({
    Object? name = const $CopyWithPlaceholder(),
    Object? members = const $CopyWithPlaceholder(),
    Object? savedViewIds = const $CopyWithPlaceholder(),
    Object? investigationId = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
  }) {
    return WorkspacePatch(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      members: members == const $CopyWithPlaceholder()
          ? _value.members
          // ignore: cast_nullable_to_non_nullable
          : members as Map<String, WorkspacePatchMembersEnum>?,
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

extension $WorkspacePatchCopyWith on WorkspacePatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWorkspacePatch.copyWith(...)` or `instanceOfWorkspacePatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WorkspacePatchCWProxy get copyWith => _$WorkspacePatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WorkspacePatch _$WorkspacePatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WorkspacePatch', json, ($checkedConvert) {
      final val = WorkspacePatch(
        name: $checkedConvert('name', (v) => v as String?),
        members: $checkedConvert(
          'members',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) =>
                MapEntry(k, $enumDecode(_$WorkspacePatchMembersEnumEnumMap, e)),
          ),
        ),
        savedViewIds: $checkedConvert(
          'savedViewIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        investigationId: $checkedConvert(
          'investigationId',
          (v) => v as String?,
        ),
        private: $checkedConvert('private', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$WorkspacePatchToJson(WorkspacePatch instance) =>
    <String, dynamic>{
      'name': ?instance.name,
      'members': ?instance.members?.map(
        (k, e) => MapEntry(k, _$WorkspacePatchMembersEnumEnumMap[e]!),
      ),
      'savedViewIds': ?instance.savedViewIds,
      'investigationId': ?instance.investigationId,
      'private': ?instance.private,
    };

const _$WorkspacePatchMembersEnumEnumMap = {
  WorkspacePatchMembersEnum.viewer: 'viewer',
  WorkspacePatchMembersEnum.editor: 'editor',
};
