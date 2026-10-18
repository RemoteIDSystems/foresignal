// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'workspace_share_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WorkspaceShareInputCWProxy {
  WorkspaceShareInput expiresAt(DateTime? expiresAt);

  WorkspaceShareInput maxUses(int? maxUses);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WorkspaceShareInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WorkspaceShareInput(...).copyWith(id: 12, name: "My name")
  /// ```
  WorkspaceShareInput call({DateTime? expiresAt, int? maxUses});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWorkspaceShareInput.copyWith(...)` or call `instanceOfWorkspaceShareInput.copyWith.fieldName(value)` for a single field.
class _$WorkspaceShareInputCWProxyImpl implements _$WorkspaceShareInputCWProxy {
  const _$WorkspaceShareInputCWProxyImpl(this._value);

  final WorkspaceShareInput _value;

  @override
  WorkspaceShareInput expiresAt(DateTime? expiresAt) =>
      call(expiresAt: expiresAt);

  @override
  WorkspaceShareInput maxUses(int? maxUses) => call(maxUses: maxUses);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WorkspaceShareInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WorkspaceShareInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WorkspaceShareInput call({
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? maxUses = const $CopyWithPlaceholder(),
  }) {
    return WorkspaceShareInput(
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
      maxUses: maxUses == const $CopyWithPlaceholder()
          ? _value.maxUses
          // ignore: cast_nullable_to_non_nullable
          : maxUses as int?,
    );
  }
}

extension $WorkspaceShareInputCopyWith on WorkspaceShareInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWorkspaceShareInput.copyWith(...)` or `instanceOfWorkspaceShareInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WorkspaceShareInputCWProxy get copyWith =>
      _$WorkspaceShareInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WorkspaceShareInput _$WorkspaceShareInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WorkspaceShareInput', json, ($checkedConvert) {
      final val = WorkspaceShareInput(
        expiresAt: $checkedConvert(
          'expiresAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        maxUses: $checkedConvert('maxUses', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$WorkspaceShareInputToJson(
  WorkspaceShareInput instance,
) => <String, dynamic>{
  'expiresAt': ?instance.expiresAt?.toIso8601String(),
  'maxUses': ?instance.maxUses,
};
