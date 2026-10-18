// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'workspace_context_state.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WorkspaceContextStateCWProxy {
  WorkspaceContextState workspaceId(String workspaceId);

  WorkspaceContextState contextVersion(int contextVersion);

  WorkspaceContextState context(WorkspaceContext context);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WorkspaceContextState(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WorkspaceContextState(...).copyWith(id: 12, name: "My name")
  /// ```
  WorkspaceContextState call({
    String workspaceId,
    int contextVersion,
    WorkspaceContext context,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWorkspaceContextState.copyWith(...)` or call `instanceOfWorkspaceContextState.copyWith.fieldName(value)` for a single field.
class _$WorkspaceContextStateCWProxyImpl
    implements _$WorkspaceContextStateCWProxy {
  const _$WorkspaceContextStateCWProxyImpl(this._value);

  final WorkspaceContextState _value;

  @override
  WorkspaceContextState workspaceId(String workspaceId) =>
      call(workspaceId: workspaceId);

  @override
  WorkspaceContextState contextVersion(int contextVersion) =>
      call(contextVersion: contextVersion);

  @override
  WorkspaceContextState context(WorkspaceContext context) =>
      call(context: context);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WorkspaceContextState(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WorkspaceContextState(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WorkspaceContextState call({
    Object? workspaceId = const $CopyWithPlaceholder(),
    Object? contextVersion = const $CopyWithPlaceholder(),
    Object? context = const $CopyWithPlaceholder(),
  }) {
    return WorkspaceContextState(
      workspaceId:
          workspaceId == const $CopyWithPlaceholder() || workspaceId == null
          ? _value.workspaceId
          // ignore: cast_nullable_to_non_nullable
          : workspaceId as String,
      contextVersion:
          contextVersion == const $CopyWithPlaceholder() ||
              contextVersion == null
          ? _value.contextVersion
          // ignore: cast_nullable_to_non_nullable
          : contextVersion as int,
      context: context == const $CopyWithPlaceholder() || context == null
          ? _value.context
          // ignore: cast_nullable_to_non_nullable
          : context as WorkspaceContext,
    );
  }
}

extension $WorkspaceContextStateCopyWith on WorkspaceContextState {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWorkspaceContextState.copyWith(...)` or `instanceOfWorkspaceContextState.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WorkspaceContextStateCWProxy get copyWith =>
      _$WorkspaceContextStateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WorkspaceContextState _$WorkspaceContextStateFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('WorkspaceContextState', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['workspaceId', 'contextVersion', 'context'],
  );
  final val = WorkspaceContextState(
    workspaceId: $checkedConvert('workspaceId', (v) => v as String),
    contextVersion: $checkedConvert(
      'contextVersion',
      (v) => (v as num).toInt(),
    ),
    context: $checkedConvert(
      'context',
      (v) => WorkspaceContext.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$WorkspaceContextStateToJson(
  WorkspaceContextState instance,
) => <String, dynamic>{
  'workspaceId': instance.workspaceId,
  'contextVersion': instance.contextVersion,
  'context': instance.context.toJson(),
};
